package org.mobicents.ipbx.session;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.Address;
import javax.servlet.sip.ConvergedHttpSession;
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
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.entity.Binding;
import org.mobicents.ipbx.entity.CallState;
import org.mobicents.ipbx.entity.PstnGatewayAccount;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.entity.User;
import org.mobicents.ipbx.session.call.model.CallParticipant;
import org.mobicents.ipbx.session.call.model.CallParticipantManager;
import org.mobicents.ipbx.session.call.model.WorkspaceStateManager;
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
	@In(create=true) SimpleSipAuthenticator sipAuthenticator;
	@In SipFactory sipFactory;
	@In EntityManager entityManager;
	
	public void call(final CallParticipant fromParticipant, final CallParticipant toParticipant) {
		String fromUriTmp = getAuthUri(toParticipant);
		if(fromUriTmp == null) fromUriTmp = fromParticipant.getUri();
		final String fromUri = fromUriTmp;
		final String toUri = toParticipant.getUri();
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

		try {
			// Don't use this because we might call it from SIP context (faces context not available)
			//FacesContext context = FacesContext.getCurrentInstance();
			//ConvergedHttpSession session = (ConvergedHttpSession) context.getExternalContext().getSession(false);

			//SipApplicationSession appSession = session.getApplicationSession();//sipFactory.createApplicationSession();
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
			String timeout = PbxConfiguration.getProperty("pbx.call.timeout");
			new Timer().schedule(new TimeoutTask(toParticipant), Integer.parseInt(timeout)) ;
		} catch (Exception e) {
			log.error("Error", e);
		}

	}
	
	private String getAuthUri(CallParticipant cp) {
		PstnGatewayAccount account = cp.getPstnGatewayAccount();
		
		if(account == null) return null;
		
		String uri = "sip:" + account.getUsername() + "@" + account.getHostname();
		return uri;
	}
	
	public void call(String toUri) {
		LinkedList<CallParticipant> fromParticipants = new LinkedList<CallParticipant>();
		
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);
		User user = entityManager.find(User.class, this.user.getId());
		
		Conference conf = null;
		boolean alreadyInCall = false;
		
		// Determine if we are in any calls right now. If yes, take a conference 
		// endpoint to join the outgoing call
		String[] callableUris = user.getCallableUris();
		for(String uri : callableUris) {
			CallParticipant p = CallParticipantManager.instance().getExistingCallParticipant(uri);
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
			conf = ConferenceManager.instance().getNewConference();
			
			//Add the participants selected in the My Phones panel 
			for(String uri : user.getCallableUris()) {
				CallParticipant fromParticipant = CallParticipantManager.instance().getCallParticipant(uri);
				fromParticipant.setConference(conf);
				fromParticipants.add(fromParticipant);
			}
			
		}
		if(fromParticipants.size()==0) {
			try {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("No phone selected", "You must register a phone and select it from the menu"));
			} catch (Exception e) {}
			return;
		}
		//Add the participant on which call has been clicked in the Contacts Panel
		CallParticipant toParticipant = CallParticipantManager.instance().getCallParticipant(toUri);

		toParticipant.setConference(conf);
		toParticipant.setCallState(CallState.CONNECTING);
		if(toRegistration != null && toParticipant.getName() == null) {
			toParticipant.setName(toRegistration.getUser().getName());
		}
		for(CallParticipant cp : fromParticipants) {
			cp.setName(user.getName());
			call(cp, toParticipant);
			WorkspaceStateManager.instance().getWorkspace(user.getName()).setOutgoing(toParticipant);
			WorkspaceStateManager.instance().getWorkspace(toParticipant.getName()).setIncoming(cp);
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
				   WorkspaceStateManager.instance().getWorkspace(participant.getName()).endCall(participant);
			   }
			} catch (Exception ex) {
				// No important
			}
		}
	}
	
}
