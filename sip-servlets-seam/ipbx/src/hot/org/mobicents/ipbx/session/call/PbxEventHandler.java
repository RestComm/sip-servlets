package org.mobicents.ipbx.session.call;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.URI;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.entity.Binding;
import org.mobicents.ipbx.entity.CallState;
import org.mobicents.ipbx.entity.PstnGatewayAccount;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.session.CallAction;
import org.mobicents.ipbx.session.call.model.CallParticipant;
import org.mobicents.ipbx.session.call.model.CallParticipantManager;
import org.mobicents.ipbx.session.call.model.CallStateManager;
import org.mobicents.ipbx.session.call.model.Conference;
import org.mobicents.ipbx.session.call.model.ConferenceManager;
import org.mobicents.ipbx.session.configuration.PbxConfiguration;
import org.mobicents.ipbx.session.security.SimpleSipAuthenticator;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;


@Name("pbxEventHandler")
@Scope(ScopeType.STATELESS)
public class PbxEventHandler {
	@Logger Log log;
	@In MsSession msSession;
	@In SipSession sipSession;
	@In MediaController mediaController;
	@In ConferenceManager conferenceManager;
	@In CallStateManager callStateManager;
	@In CallParticipantManager callParticipantManager;
	@In(create=true) SimpleSipAuthenticator sipAuthenticator;
	@In SipFactory sipFactory;
	@In PbxConfiguration pbxConfiguration;
	@In MsEventFactory eventFactory;
	
	@In(create=true) CallAction callAction;
	
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";	
	
	@Observer("INVITE")
	public void doInvite(SipServletRequest request) {
		String fromUri = request.getFrom().getURI().toString();
		String toUri = request.getTo().getURI().toString();
		
		// Authentication just by user name for now
		Registration fromRegistration = sipAuthenticator.authenticate(fromUri);
		Registration toRegistration = sipAuthenticator.findRegistration(toUri);
		
		if(fromRegistration == null || toRegistration == null) {
			try {
				request.createResponse(404).send();
			} catch (IOException e) {
				log.error("Can't send 404 response");
			}
			return;
		}
		
		Binding toBinding = null;
		if(toRegistration.getBindings() != null) {
			if(toRegistration.getBindings().size() > 0) {
				toBinding = toRegistration.getBindings().iterator().next();
				toUri = toBinding.getContactAddress();
			}
		}
		
		
		String fromUser = fromRegistration.getUser().getName();
		String toUser = toRegistration.getUser().getName();
		
		// Ringing
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		
		// Extract sdp
		Object sdpObj = null;
		try {
			sipServletResponse.send();
			sdpObj = request.getContent();
		} catch (IOException e) {}
		byte[] sdpBytes = (byte[]) sdpObj;
		String sdp = new String(sdpBytes);
		
		// Create call participants to join a call
		CallParticipant fromParticipant = callParticipantManager.getCallParticipant(
				fromUri);
		fromParticipant.setName(fromUser);
		fromParticipant.setRegistration(fromRegistration);
		fromParticipant.setUri(fromRegistration.getUri());
		fromParticipant.setInitiator(true);
		
		CallParticipant toParticipant = callParticipantManager.getCallParticipant(
				toUri);
		toParticipant.setName(toUser);
		toParticipant.setRegistration(toRegistration);
		toParticipant.setUri(toUri);
		toParticipant.setBinding(toBinding);
		
		//Store the request/session in the registration
		fromParticipant.setInitialRequest(request);
		
		// Store reusable info for later in this session
		sipSession.setAttribute("fromParticipant", fromParticipant);
		sipSession.setAttribute("toParticipant", toParticipant);
		sipSession.setAttribute("participant", fromParticipant);
		sipSession.setAttribute("firstSipMessage", request);
		
		Conference conf = null;
		
		// If the callee is in a call already we must ask to join that call
		if(!CallState.CONNECTED.equals(toParticipant.getCallState())) {
		// Otherwise just call all his registered phones and join them to a new
		// conference where you can talk to him
			conf = conferenceManager.getNewConference();
			fromParticipant.setConference(conf);
			toParticipant.setConference(conf);
			fromParticipant.setCallState(CallState.CONNECTING);
			
			callAction.call(fromParticipant, toParticipant);
		} else {
			conf = toParticipant.getConference();
			fromParticipant.setConference(conf);
			fromParticipant.setCallState(CallState.ASKING);
		}
		callStateManager.getCurrentState(toUser).setIncoming(fromParticipant);
		callStateManager.getCurrentState(fromUser).setOutgoing(toParticipant);
		
		mediaController.createConnection(PR_JNDI_NAME).modify("$", sdp);
		Events.instance().raiseEvent("incomingCall", request);
	}
	
	@Observer("RESPONSE")
	public void doResponse(SipServletResponse response)
			throws ServletException, IOException {
		if(response.getMethod().equalsIgnoreCase("INVITE")) {
			CallParticipant participant = (CallParticipant) 
				sipSession.getAttribute("participant");
			
			participant.setCallState(CallState.CONNECTING);
			
			// Just handle the response normally by updating the state
			int status = response.getStatus();
			
			if(status == 180) {
				participant.setCallState(CallState.RINGING);
			} else if (status == 200) {
				Object sdpObj = response.getContent();
				byte[] sdpBytes = (byte[]) sdpObj;
				String sdp = new String(sdpBytes);
				
				sipSession.setAttribute("firstSipMessage", response);
				mediaController.createConnection(PR_JNDI_NAME).modify("$", sdp);
			} else if(status == 401 || status == 407) {
				Integer authAttempts = (Integer) sipSession.getAttribute("authAttempts");
				if(authAttempts == null) authAttempts = 0;
				
				sipSession.setAttribute("authAttempts", authAttempts++);
				
				// If it doesn't succeed 3 times give up on authentication
				if(authAttempts > 3) return;
				
				// So we dialed a PSTN phone. Use auth info from the settings
				PstnGatewayAccount account = participant.getPstnGatewayAccount();
				if(account != null) {
					AuthInfo authInfo = sipFactory.createAuthInfo();
					authInfo.addAuthInfo(response.getStatus(), 
                            response.getChallengeRealms().next(), 
                            account.getUsername(), 
                            account.getPassword());
					SipServletRequest challengeRequest = response.getSession().createRequest(
							response.getRequest().getMethod());

					challengeRequest.addAuthHeader(response, authInfo);
					challengeRequest.send();

				}
			} else if(status >= 300){
				quitConference(participant, participant.getConference());
			}
		}
	}
	
	@Observer("BYE")
	public void doBye(SipServletRequest request) throws ServletException,
			IOException {
		log.info("Got BYE request:\n" + request);
		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();		
		
		// Clean up the mess in the models
		CallParticipant participant = (CallParticipant) 
			sipSession.getAttribute("participant");
				
		Conference conference = participant.getConference();
		quitConference(participant, conference);		
	}
	
	private void quitConference(CallParticipant participant, Conference conf) throws IOException {
		if(conf != null) {
			participant.setCallState(CallState.DISCONNECTED);
			participant.setConference(null);
			
			// Remove the call from the callee GUI
			callStateManager.getCurrentState(participant.getName()).removeCall(participant);

			CallParticipant[] callParticipants = conf.getParticipants();
			log.info("number of participants left in the conference = #0", callParticipants.length);
			if(callParticipants.length == 1) {
				CallParticipant cp = callParticipants[0];
				callStateManager.getCurrentState(cp.getName()).endCall(cp);
			}
			
			// Remove the call from other users' GUIs
			for(CallParticipant cp : callParticipants) {
				callStateManager.getCurrentState(cp.getName()).removeCall(participant);
			}
			
			// Release media stuff if possible
			if(participant.getMsLink() != null) 
				participant.getMsLink().release();
			if(participant.getMsConnection() != null) 
				participant.getMsConnection().release();
			participant.setMsConnection(null);
			participant.setMsLink(null);
		}
	}
	
	@Observer("connectionOpen")
	public void connectionOpen(MsConnectionEvent event) {
		SipServletMessage sipMessage = (SipServletMessage) sipSession.getAttribute("firstSipMessage");
		if(sipMessage instanceof SipServletRequest) {
			SipServletRequest request = (SipServletRequest) sipMessage;
			connectionOpenRequest(event, request);
		} else {
			SipServletResponse response = (SipServletResponse) sipMessage;
			connectionOpenResponse(event, response);
		}
	}

	private void connectionOpenResponse(MsConnectionEvent event, SipServletResponse response) {
		CallParticipant participant = 
			(CallParticipant) sipSession.getAttribute("participant");
		MsConnection connection = event.getConnection();
		participant.setMsConnection(connection);

		String sdp = event.getConnection().getLocalDescriptor();
		SipServletRequest ack = response.createAck();

		try {
			ack.setContent(sdp, "application/sdp");
			ack.send();
		} catch (Exception e) {
			log.error(e);
		}

		if(participant.getConference() != null) {
			mediaController.createLink(MsLinkMode.FULL_DUPLEX)
			.join(participant.getConference().getEndpointName(),
					connection.getEndpoint().getLocalName());
		}

	}
	
	private void connectionOpenRequest(MsConnectionEvent event, SipServletRequest inviteRequest) {
		CallParticipant participant = 
			(CallParticipant) sipSession.getAttribute("participant");
		MsConnection connection = event.getConnection();
		participant.setMsConnection(connection);

		String sdp = event.getConnection().getLocalDescriptor();
		SipServletResponse sipServletResponse = inviteRequest
				.createResponse(SipServletResponse.SC_OK);

		try {
			sipServletResponse.setContent(sdp, "application/sdp");
			sipServletResponse.send();
		} catch (Exception e) {
			log.error(e);
		}
		
		if(participant.getConference() != null) {
			mediaController.createLink(MsLinkMode.FULL_DUPLEX)
			.join(participant.getConference().getEndpointName(),
					connection.getEndpoint().getLocalName());
		}
	}
	
	
	@Observer("linkConnected")
	public synchronized void doLinkConnected(MsLinkEvent event) {
		MsEndpoint endpoint = event.getSource().getEndpoints()[0];
		CallParticipant participant = 
			(CallParticipant) sipSession.getAttribute("participant");
		Conference conf = participant.getConference();
		// We should upgrade the call state to "active" for all participants (most already have it)
		CallParticipant[] ps = conf.getParticipants();
		for(CallParticipant p : ps) {
			if(!p.getUri().equals(participant.getUri())) {

				if(p.getCallState().equals(CallState.CONNECTED)) {
					p.setCallState(CallState.CONNECTED);
					try {
						endRingback(p.getMsLink(), endpoint);
					} catch (Exception e) {}
					callStateManager.getCurrentState(p.getName()).setOngoing(participant);
					callStateManager.getCurrentState(participant.getName()).setOngoing(p);
				}
				participant.setCallState(CallState.CONNECTED);
				
			}

		}

		
		// And if this is the first participant in the conference, let's determine which endpoint we use
		if(participant != null) {
			participant.setMsLink(event.getSource());
			if(conf.getEndpoint() == null) {
				conf.setEndpoint(endpoint);
				playRingback(event.getSource(), endpoint);
			}
		}
	}
	
	public void playRingback(MsLink link, MsEndpoint endpoint) {		
		String tone = pbxConfiguration.getProperty("pbx.default.ringback.tone");
		log.info("playing following ringbacktone #0", tone);
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		play.setURL(tone);
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted};
        
        mediaController.execute(endpoint, requestedSignals, requestedEvents, link);
	}
	
	private void endRingback(MsLink link, MsEndpoint endpoint) {

		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] {};
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] {};
        
        mediaController.execute(link.getEndpoints()[0], requestedSignals, requestedEvents, link);
	}

	// Extract username from URI
	private String getUser(URI uri) {
		String user = null;
		if(uri.isSipURI()) {
			SipURI suri = (SipURI) uri;
			user = suri.getUser();
		} else {
			TelURL turi = (TelURL) uri;
			user = turi.getPhoneNumber();
		}
		return user;
	}
}