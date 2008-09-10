package org.jboss.mobicents.seam.listeners;

import java.io.File;
import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.impl.common.events.EventID;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsSignalDetector;
import org.mobicents.mscontrol.MsSignalGenerator;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Vladimir Ralev
 * @author Jean Deruelle
 *
 */
public class MediaConnectionListener implements MsConnectionListener {
	private static Log logger = LogFactory.getLog(MediaConnectionListener.class);
	
	public static final String IVR_JNDI_NAME = "media/endpoint/IVR";
	
	private SipServletRequest inviteRequest;	
	
	public void connectionCreated(MsConnectionEvent event) {		
		MsConnection connection = event.getConnection();
		String sdp = connection.getLocalDescriptor();
		try {
			inviteRequest.setContentLength(sdp.length());
			inviteRequest.setContent(sdp.getBytes(), "application/sdp");						
			inviteRequest.send();
		} catch (IOException e) {
			logger.error("An unexpected exception occured while sending the request", e);
		}								
		logger.info("Local Media Connection created " + event.getEventID());
	}

	public void connectionDeleted(MsConnectionEvent event) {
		logger.info("Local Media Connection deleted " + event.getEventID());
		
	}

	public void connectionModifed(MsConnectionEvent event) {
		logger.info("Remote Media Connection created. Endpoints connected " + event.getEventID());
		MsConnection connection = event.getConnection();
		String endpoint = connection.getEndpoint();
		
		MsSignalGenerator generator = connection.getSession().getProvider().getSignalGenerator(endpoint);
		String pathToAudioDirectory = (String) inviteRequest.getSession().getApplicationSession().getAttribute("audioFilePath");
		if(inviteRequest.getSession().getApplicationSession().getAttribute("orderApproval") != null) {
			java.io.File speech = new File("speech.wav");
			if(inviteRequest.getSession().getApplicationSession().getAttribute("adminApproval") != null) {
				speech = new File("adminspeech.wav");	
			}			
			logger.info("Playing confirmation announcement : " + "file://" + speech.getAbsolutePath());
			generator.apply(EventID.PLAY, connection, new String[]{"file://" + speech.getAbsolutePath()});
			logger.info("announcement confirmation played. waiting for DTMF ");
			listenToDTMF(connection, pathToAudioDirectory);
		} else if (inviteRequest.getSession().getApplicationSession().getAttribute("deliveryDate") != null) {			
			String announcementFile = pathToAudioDirectory + "OrderDeliveryDate.wav";
			logger.info("Playing Delivery Date Announcement : " + announcementFile);
			generator.apply(EventID.PLAY, connection, new String[]{announcementFile});
			logger.info("Delivery Date Announcement played. waiting for DTMF ");
			listenToDTMF(connection, pathToAudioDirectory);
		} else if (inviteRequest.getSession().getApplicationSession().getAttribute("shipping") != null) {			
			java.io.File speech = new File("shipping.wav");
			logger.info("Playing shipping announcement : " + "file://" + speech.getAbsolutePath());
			MediaResourceListener mediaResourceListener = new MediaResourceListener(inviteRequest.getSession(), connection);
			generator.addResourceListener(mediaResourceListener);
			generator.apply(EventID.PLAY, connection, new String[]{"file://" + speech.getAbsolutePath()});
			logger.info("shipping announcement played. tearing down the call");
		}				
	}

	private void listenToDTMF(MsConnection connection, String pathToAudioDirectory) {
		String endpoint = connection.getEndpoint();		
		MsSignalGenerator generator = connection.getSession().getProvider().getSignalGenerator(endpoint);
		MsSignalDetector dtmfDetector = connection.getSession().getProvider().getSignalDetector(endpoint);
		DTMFListener dtmfListener = new DTMFListener(dtmfDetector, connection, inviteRequest.getSession(), pathToAudioDirectory);
		dtmfDetector.addResourceListener(dtmfListener);
		generator.addResourceListener(dtmfListener);
		dtmfDetector.receive(EventID.DTMF, connection, new String[] {});			
		inviteRequest.getSession().setAttribute("DTMFSession", DTMFListener.DTMF_SESSION_STARTED);
	}

	public void txFailed(MsConnectionEvent event) {
		logger.info("Transaction failed on event "+ event.getEventID() + "with message " + event.getMessage());
		
	}

	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
	}

	public void connectionInitialized(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
