package org.mobicents.ipbx.session;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.EntityManager;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;
import javax.servlet.sip.SipSession.State;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.entity.Binding;
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
	@Logger 
	private static Log log;
	@In(required=false,value="sessionUser") User user;
	@In DataLoader dataLoader;
	@In CallParticipantManager callParticipantManager;
	@In CallStateManager callStateManager;
	@In ConferenceManager conferenceManager;
	@In PbxConfiguration pbxConfiguration;
	@In(create=true) SimpleSipAuthenticator sipAuthenticator;
	@In SipFactory sipFactory;
	@In EntityManager sipEntityManager;
	
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
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);					
			
		String contactUri = toUri;
		if(toRegistration != null && toRegistration.getBindings() != null) {
			if(toRegistration.getBindings().size() > 0) {
				Binding toBinding = toRegistration.getBindings().iterator().next();
				contactUri = toBinding.getContactAddress();
				toParticipant.setBinding(toBinding);
			}
		}
		final String requestUri = contactUri;
		log.info("Calling " + requestUri + " from " + fromUri + " to " + toUri);
		// We need a new thread here, because the thread-local storage used by seam from the Web part
		// will collide with the Sip part (the session contexts are different).
		Thread thread = new Thread() {
			public void run() {
				try {
					SipApplicationSession appSession = sipFactory.createApplicationSession();
					Address from = sipFactory.createAddress(fromUri);
					Address to = sipFactory.createAddress(toUri);
					URI requestURI = sipFactory.createURI(requestUri);
					SipServletRequest request = sipFactory.createRequest(appSession, 
							"INVITE", from, to);
					request.getSession().setAttribute("toParticipant", toParticipant);
					request.getSession().setAttribute("fromParticipant", fromParticipant);
					request.getSession().setAttribute("participant", toParticipant);
					if(user != null) request.getSession().setAttribute("user", user);
					toParticipant.setCallState(CallState.CONNECTING);
					request.setRequestURI(requestURI);
					request.send();
					toParticipant.setInitialRequest(request);
					String timeout = pbxConfiguration.getProperty("pbx.call.timeout");
					new Timer().schedule(new TimeoutTask(toParticipant), Integer.parseInt(timeout)) ;
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
		LinkedList<CallParticipant> fromParticipants = new LinkedList<CallParticipant>();
		
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);
		User user = sipEntityManager.find(User.class, this.user.getId());
		
		Conference conf = null;
		boolean alreadyInCall = false;
		
		// Determine if we are in any calls right now. If yes, take a conference 
		// endpoint to join the outgoing call
		String[] callableUris = user.getCallableUris();
		for(String uri : callableUris) {
			CallParticipant p = callParticipantManager.getExistingCallParticipant(uri);
			if(p != null) {
				if(p.getConference() != null && p.getCallState().equals(CallState.CONNECTED)) {
					alreadyInCall = true;
					conf = p.getConference();
					fromParticipants.add(p);
				}
			}
		}
		// If there are no active calls just create a new conference where the 
		// conversation will take place
		if(conf == null) {			
			conf = conferenceManager.getNewConference();
			
			//Add the participants selected in the My Phones panel 
			for(String uri : user.getCallableUris()) {
				CallParticipant fromParticipant = callParticipantManager.getCallParticipant(uri);
				fromParticipant.setConference(conf);
				fromParticipants.add(fromParticipant);
			}
			
		}
		//Add the participant on which call has been clicked in the Contacts Panel
		CallParticipant toParticipant = callParticipantManager.getCallParticipant(toUri);

		toParticipant.setConference(conf);
		toParticipant.setCallState(CallState.CONNECTING);
		if(toRegistration != null && toParticipant.getName() == null) {
			toParticipant.setName(toRegistration.getUser().getName());
		}
		for(CallParticipant cp : fromParticipants) {
			cp.setName(user.getName());
			call(cp, toParticipant);
			callStateManager.getCurrentState(user.getName()).setOutgoing(toParticipant);
			callStateManager.getCurrentState(toParticipant.getName()).setIncoming(cp);
		}
		
		if(!alreadyInCall) { 
			//TODO: it doesnt matter what we put as fromParticipant here, but do it more cleanly
			call(toParticipant, fromParticipants.iterator().next());
		}
		
	}
	
	static class TimeoutTask  extends TimerTask {
		private CallParticipant participant;
		public TimeoutTask(CallParticipant participant)  {
			this.participant = participant;
		}
		public void run () {
			try {
			   boolean active = this.participant.getInitialRequest().getSession().getState().equals(State.CONFIRMED);
			   if(!active) {
				   CallStateManager.getUserState(participant.getName()).endCall(participant);
			   }
			} catch (Exception ex) {
				// No important
			}
		}
	}
	
}
