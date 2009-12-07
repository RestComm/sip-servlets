package org.mobicents.servlet.sip.alerting.util;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsSession;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Jean Deruelle
 *
 */
public class MediaConnectionListener implements MsConnectionListener {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MediaConnectionListener.class);
	
	public static final String IVR_JNDI_NAME = "media/trunk/IVR/$";
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";
	
	private SipServletRequest inviteRequest;	

	public void txFailed(MsConnectionEvent event) {
		logger.info("Transaction failed on event "+ event.getEventID() + "with message " + event.getMessage());
		
	}

	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
	}
	
	public void connectionCreated(MsConnectionEvent event) {		
		logger.info("connection created " + event);
	}

	public void connectionInitialized(MsConnectionEvent arg0) {
		logger.info("connection initialized " + arg0);
	}

	public void connectionDisconnected(MsConnectionEvent event) {
		logger.info("connection disconnected " + event);
	}

	public void connectionFailed(MsConnectionEvent arg0) {
		logger.info("connection failed " + arg0);
	}

	public void connectionHalfOpen(MsConnectionEvent event) {
		logger.info("connection half opened " + event);
		MsConnection connection = event.getConnection();
		String sdp = connection.getLocalDescriptor();
		try {
			inviteRequest.setContentLength(sdp.length());
			inviteRequest.setContent(sdp.getBytes(), "application/sdp");						
			inviteRequest.send();
		} catch (IOException e) {
			logger.error("An unexpected exception occured while sending the request", e);
		}								
		logger.info("Local Media Connection half created " + event.getEventID());
	}

	public void connectionOpen(MsConnectionEvent event) {
		logger.info("connection opened " + event);
		handleMedia(event);

	}

	public void connectionModeRecvOnly(MsConnectionEvent event) {
		logger.info("connection mode recv only " + event);
		
	}

	public void connectionModeSendOnly(MsConnectionEvent event) {
		logger.info("connection mode send only " + event);
		
	}

	public void connectionModeSendRecv(MsConnectionEvent event) {
		logger.info("connection mod send/recv " + event);
		
	}
	
	protected void handleMedia(MsConnectionEvent event) {
		logger.info("Remote Media Connection created. Endpoints connected " + event.getEventID());
		
		final MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		final MsSession session = connection.getSession();
		final MsLink link = session.createLink(MsLinkMode.FULL_DUPLEX);
		link.addLinkListener(new MediaLinkListener(link, connection, inviteRequest));			
		logger.info("Linking " + endpoint.getLocalName() + " to IVR");		
		link.join(IVR_JNDI_NAME, endpoint.getLocalName());
	}

	
	
}
