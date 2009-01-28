package org.mobicents.ipbx.session;

import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.mobicents.ipbx.entity.CallState;
import org.mobicents.ipbx.entity.PstnGatewayAccount;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.entity.User;
import org.mobicents.ipbx.session.call.model.CallParticipant;
import org.mobicents.ipbx.session.call.model.CallParticipantManager;
import org.mobicents.ipbx.session.call.model.CallStateManager;
import org.mobicents.ipbx.session.call.model.Conference;
import org.mobicents.ipbx.session.call.model.ConferenceManager;
import org.mobicents.ipbx.session.configuration.PbxConfiguration;
import org.mobicents.ipbx.session.security.SimpleSipAuthenticator;

@Name("callAction")
@Scope(ScopeType.STATELESS)
public class CallAction {
	@In(required=false,value="sessionUser") User user;
	@In DataLoader dataLoader;
	@In CallParticipantManager callParticipantManager;
	@In CallStateManager callStateManager;
	@In ConferenceManager conferenceManager;
	@In PbxConfiguration pbxConfiguration;
	@In(create=true) SimpleSipAuthenticator sipAuthenticator;
	@In SipFactory sipFactory;
	
	public void call(final CallParticipant fromParticipant,
			final CallParticipant toParticipant) {
		String fromUri = getAuthUri(fromParticipant);
		String toUri = toParticipant.getUri();
		call(fromParticipant, toParticipant, fromUri, toUri);
	}
	
	private String getAuthUri(CallParticipant cp) {
		PstnGatewayAccount account = cp.getPstnGatewayAccount();
		
		if(account == null) return cp.getUri();
		
		String uri = "sip:" + account.getUsername() + "@" + account.getHostname();
		return uri;
	}
	
	public void call(final CallParticipant fromParticipant,
			final CallParticipant toParticipant, final String fromUri, final String toUri) {
		//ServletContext ctx = (ServletContext) javax.faces.context.FacesContext
		//	.getCurrentInstance().getExternalContext().getContext();
		final SipFactory sipFactory = this.sipFactory;//(SipFactory) ctx.getAttribute(SipServlet.SIP_FACTORY);
		
		// We need a new thread here, because the thread-local storage used by seam from the Web part
		// will collide with the Sip part (the session contexts are different).
		Thread thread = new Thread() {
			public void run() {
				try {
					SipApplicationSession appSession = sipFactory.createApplicationSession();
					Address from = sipFactory.createAddress(fromUri);
					Address to = sipFactory.createAddress(toUri);
					SipServletRequest request = sipFactory.createRequest(appSession, 
							"INVITE", from, to);
					request.getSession().setAttribute("toParticipant", toParticipant);
					request.getSession().setAttribute("fromParticipant", fromParticipant);
					request.getSession().setAttribute("participant", toParticipant);
					if(user != null) request.getSession().setAttribute("user", user);
					toParticipant.setCallState(CallState.CONNECTING);
					request.send();
					toParticipant.setInitialRequest(request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void call(String toUri) {
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);
		//if(toRegistration == null) {
		//	throw new RuntimeException("User not found " + toUri);
		//}
		
		Conference conf = null;
		CallParticipant fromParticipant = null;
		Registration someSelected = null;
		
		
		Iterator<Registration> regs = user.getRegistrations().iterator();
		while(regs.hasNext()) {
			Registration reg = regs.next();
			if(reg.isSelected()) {
				someSelected = reg;
				fromParticipant = callParticipantManager.getExistingCallParticipant(reg.getUri());
				if(fromParticipant != null) {
					if(CallState.CONNECTED.equals(fromParticipant.getCallState())) {
						conf = fromParticipant.getConference();
						if(conf != null) break;
					}
				}
			}
		}
		if(conf == null) {
			conf = conferenceManager.getNewConference();
			fromParticipant = callParticipantManager.getCallParticipant(someSelected.getUri());
			fromParticipant.setConference(conf);
			
		}
		fromParticipant.setName(user.getName());
		
		
		CallParticipant toParticipant = callParticipantManager.getCallParticipant(toUri);

		toParticipant.setConference(conf);
		toParticipant.setCallState(CallState.CONNECTING);
		if(toRegistration != null && toParticipant.getName() == null) {
			toParticipant.setName(toRegistration.getUser().getName());
		}

		call(fromParticipant, toParticipant);
		call(toParticipant, fromParticipant);
		callStateManager.getCurrentState(fromParticipant.getName()).setOutgoing(toParticipant);
		callStateManager.getCurrentState(toParticipant.getName()).setIncoming(fromParticipant);
	}
}
