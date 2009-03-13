package org.mobicents.servlet.sip.conference.server;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsNotificationListener;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.servlet.sip.conference.client.SipGwtConferenceConsole;
import org.mobicents.servlet.sip.conference.server.media.AnnouncementConferenceParticipant;
import org.mobicents.servlet.sip.conference.server.media.ConferenceCenter;
import org.mobicents.servlet.sip.conference.server.media.ConferenceParticipant;
import org.mobicents.servlet.sip.conference.server.media.EndpointConferenceParticipant;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * sip message).
 * 
 * @author Vladimir Ralev
 *
 */
public class ConferenceConnectionListener implements MsConnectionListener, MsNotificationListener{
	private static Log logger = LogFactory.getLog(ConferenceConnectionListener.class);
	
	private SipServletMessage sipMessage;
	private MsProvider provider;
	
	public ConferenceConnectionListener(SipServletMessage message) {
		this.sipMessage = message;
	}
	
	public void connectionCreated(MsConnectionEvent event) {
		logger.info("connection created " + event);
	}

	public void connectionInitialized(MsConnectionEvent arg0) {
		logger.info("connection initialized " + arg0);
	}

	public void connectionDisconnected(MsConnectionEvent arg0) {
		logger.info("connection disconnected " + arg0);
	}

	public void connectionFailed(MsConnectionEvent arg0) {
		logger.error("connection failed " + arg0);
		if(sipMessage instanceof SipServletRequest) {
			SipServletRequest inviteRequest = (SipServletRequest) sipMessage;
			try {
				inviteRequest.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
			} catch (IOException e) {
				logger.error("Unexpected exception while sending the error response", e);
			}
		}
	}
	
	public void connectionHalfOpen(MsConnectionEvent arg0) {
		logger.info("connection half opened" + arg0);
	}
	
	public void connectionOpen(MsConnectionEvent event) {
		if(sipMessage instanceof SipServletRequest) {
			SipServletRequest request = (SipServletRequest) sipMessage;
			connectionOpenRequest(event, request);
		} else {
			SipServletResponse response = (SipServletResponse) sipMessage;
			connectionOpenResponse(event, response);
		}
	}

	public void connectionOpenResponse(MsConnectionEvent event, SipServletResponse response) {
		logger.info("connection opened " + event);

		String sdp = event.getConnection().getLocalDescriptor();
		SipServletRequest ack = response.createAck();

		try {
			ack.setContent(sdp, "application/sdp");
			ack.send();
		} catch (Exception e) {
			logger.error(e);
		}
		provider = ConferenceCenter.getInstance().getProvider();
		MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		MsSession session = connection.getSession();
		String callerName = response.getTo().getURI().toString();

		ConferenceParticipant participant = new EndpointConferenceParticipant(
				callerName, endpoint, session, response, connection);

		String key = SipGwtConferenceConsole.CONFERENCE_NAME;

		ConferenceCenter.getInstance().getConference(key).joinParticipant(
				participant);
	}
	
	public void connectionOpenRequest(MsConnectionEvent event, SipServletRequest inviteRequest) {
		logger.info("connection opened " + event);

		String sdp = event.getConnection().getLocalDescriptor();
		SipServletResponse sipServletResponse = inviteRequest
				.createResponse(SipServletResponse.SC_OK);

		try {
			sipServletResponse.setContent(sdp, "application/sdp");
			sipServletResponse.send();
		} catch (Exception e) {
			logger.error(e);
		}
		provider = ConferenceCenter.getInstance().getProvider();
		MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		MsSession session = connection.getSession();
		String callerName = inviteRequest.getFrom().getURI().toString();

		ConferenceParticipant participant = new EndpointConferenceParticipant(
				callerName, endpoint, session, inviteRequest, connection);

		String key = ((SipURI) inviteRequest.getTo().getURI()).getUser();

		ConferenceCenter.getInstance().getConference(key).joinParticipant(
				participant);
	}

	public SipServletMessage getSipMessage() {
		return sipMessage;
	}
    
    public void update(MsNotifyEvent evt) {
    	logger.info(evt);
    }

	public void connectionModeRecvOnly(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionModeSendOnly(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionModeSendRecv(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
