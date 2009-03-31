package org.mobicents.ipbx.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
import org.mobicents.ipbx.session.call.model.Conference;
import org.mobicents.ipbx.session.call.model.ConferenceManager;
import org.mobicents.ipbx.session.call.model.CurrentWorkspaceState;
import org.mobicents.ipbx.session.call.model.WorkspaceStateManager;
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
	@In(required=false) CurrentWorkspaceState currentWorkspaceState;
	
	public void dialParticipant(final CallParticipant participant) {
		Conference conf = participant.getConference();
		String fromUri = getAuthUri(participant);
		if(fromUri == null) fromUri = conf.getConferenceUri();
		try {
			// Don't use this because we might call it from SIP context (faces context not available)
			//FacesContext context = FacesContext.getCurrentInstance();
			//ConvergedHttpSession session = (ConvergedHttpSession) context.getExternalContext().getSession(false);

			//SipApplicationSession appSession = session.getApplicationSession();//sipFactory.createApplicationSession();
			SipApplicationSession appSession = sipFactory.createApplicationSession();
			Address from = sipFactory.createAddress(fromUri);
			Address to = sipFactory.createAddress(participant.getUri());
			URI requestURI = sipFactory.createURI(participant.getUri());
			SipServletRequest request = sipFactory.createRequest(appSession, 
					"INVITE", from, to);
			request.getSession().setAttribute("participant", participant);
			if(user != null) request.getSession().setAttribute("user", user);
			participant.setCallState(CallState.CONNECTING);
			request.setRequestURI(requestURI);
			request.send();
			participant.setInitialRequest(request);
			String timeout = PbxConfiguration.getProperty("pbx.call.timeout");
			new Timer().schedule(new TimeoutTask(participant), Integer.parseInt(timeout)) ;
			
			String initiatorUser = participant.getName();
			
			// If we are running in a web context the web user is the initiator
			if(user != null) {
				initiatorUser = user.getName();
			}
			
			WorkspaceStateManager.instance().getWorkspace(initiatorUser).setOutgoing(participant);
			for(CallParticipant cp : conf.getParticipants(CallState.INCALL)) {
				if(cp.getName().equals(participant.getName())) {
					if(!cp.getName().equals(initiatorUser)) {
						WorkspaceStateManager.instance().getWorkspace(cp.getName()).setIncoming(participant);
					}
				}
			}
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
	
	/**
	 * Establish call with all uris  registered for the user under the supplied uri
	 * 
	 * @param toUri
	 */
	public void establishCallByUser(String toUri) {
		LinkedList<CallParticipant> fromParticipants = new LinkedList<CallParticipant>();
		
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);
		User fromUser = entityManager.find(User.class, this.user.getId());
		
		Conference conf = null;
		boolean alreadyInCall = false;
		
		// Determine if we are in any calls right now. If yes, take a conference 
		// endpoint to join the outgoing call
		String[] callableUris = fromUser.getCallableUris();
		for(String uri : callableUris) {
			CallParticipant p = CallParticipantManager.instance().getExistingCallParticipant(uri);
			if(p != null) {
				if(p.getConference() != null && p.getCallState().equals(CallState.INCALL)) {
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
			for(String uri : fromUser.getCallableUris()) {
				CallParticipant fromParticipant = CallParticipantManager.instance().getCallParticipant(uri);
				fromParticipant.setConference(conf);
				fromParticipants.add(fromParticipant);
			}
			
		}
		
		// Some error checking - if no active phones, don't call and remind the user to select a phone
		if(fromParticipants.size()==0) {
			try {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("No phone selected", "You must register a phone and select it from the menu"));
			} catch (Exception e) {}
			return;
		}
		// Call the guy on all possible phones
		String[] toCallableUris = null;
		if(toRegistration != null) {
			toCallableUris = toRegistration.getUser().getCallableUris();
			
		}
		if(toCallableUris == null || toCallableUris.length == 0) {
			toCallableUris = new String[] {toUri};
		}
		for(String uri : toCallableUris) {
			CallParticipant toParticipant = CallParticipantManager.instance().getCallParticipant(uri);
			toParticipant.setConference(conf);
			String remoteUserName = "Unknown user";
			if(toRegistration != null) remoteUserName = toRegistration.getUser().getName();
			toParticipant.setName(remoteUserName);
			dialParticipant(toParticipant);
		}
		
		
		// Call my phones if there is no ongoing call
		if(!alreadyInCall) {
			Iterator<CallParticipant> iterator = fromParticipants.iterator();
			while(iterator.hasNext())  {
				CallParticipant next = iterator.next();
				next.setName(fromUser.getName());
				dialParticipant(next);
			}
		}
		
	}
	
	/**
	 * Establish call with all uris  registered for the user under the supplied uri
	 * 
	 * @param toUri
	 */
	public void establishCallByRegistration(String toUri) {
		LinkedList<CallParticipant> fromParticipants = new LinkedList<CallParticipant>();
		
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);
		User fromUser = entityManager.find(User.class, this.user.getId());
		
		Conference conf = null;
		boolean alreadyInCall = false;
		
		// Determine if we are in any calls right now. If yes, take a conference 
		// endpoint to join the outgoing call
		String[] callableUris = fromUser.getCallableUris();
		for(String uri : callableUris) {
			CallParticipant p = CallParticipantManager.instance().getExistingCallParticipant(uri);
			if(p != null) {
				if(p.getConference() != null && p.getCallState().equals(CallState.INCALL)) {
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
			for(String uri : fromUser.getCallableUris()) {
				CallParticipant fromParticipant = CallParticipantManager.instance().getCallParticipant(uri);
				fromParticipant.setConference(conf);
				fromParticipants.add(fromParticipant);
			}
			
		}
		
		// Some error checking - if no active phones, don't call and remind the user to select a phone
		if(fromParticipants.size()==0) {
			try {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("No phone selected", "You must register a phone and select it from the menu"));
			} catch (Exception e) {}
			return;
		}
		// Call the guy on all possible phones
		String[] toCallableUris = null;
		if(toRegistration != null) {
			toCallableUris = toRegistration.getUser().getCallableUris();
			
		}
		if(toCallableUris == null || toCallableUris.length == 0) {
			toCallableUris = new String[] {toUri};
		}
		for(String uri : toCallableUris) {
			CallParticipant toParticipant = CallParticipantManager.instance().getCallParticipant(uri);
			toParticipant.setConference(conf);
			String remoteUserName = "Unknown user";
			if(toRegistration != null) remoteUserName = toRegistration.getUser().getName();
			toParticipant.setName(remoteUserName);
			dialParticipant(toParticipant);
		}
		
		
		// Call my phones if there is no ongoing call
		if(!alreadyInCall) {
			Iterator<CallParticipant> iterator = fromParticipants.iterator();
			while(iterator.hasNext())  {
				CallParticipant next = iterator.next();
				next.setName(fromUser.getName());
				dialParticipant(next);
			}
		}
		
	}
	
	/**
	 * Establish call with all uris  registered for the user under the supplied uri
	 * 
	 * @param toUri
	 */
	public void establishCallToRegisteredPhone(String toUri) {
		Conference conf = currentWorkspaceState.getConference();

		// Call the guy on all possible phones
		String[] toCallableUris = new String[] {toUri};
		
		for(String uri : toCallableUris) {
			CallParticipant toParticipant = CallParticipantManager.instance().getCallParticipant(uri);
			toParticipant.setConference(conf);
			String remoteUserName = user.getName();
			toParticipant.setName(remoteUserName);
			dialParticipant(toParticipant);
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
