package org.jboss.mobicents.seam.listeners;

import java.io.File;
import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

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
	
	public static final String IVR_JNDI_NAME = "media/trunk/IVR/$";
	
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

	public void connectionDisconnected(MsConnectionEvent arg0) {
		logger.info("connection disconnected " + arg0);
	}

	public void connectionFailed(MsConnectionEvent arg0) {
		logger.info("connection failed " + arg0);
	}

	public void connectionHalfOpen(MsConnectionEvent event) {
		logger.info("connection hal opened " + event);
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
		logger.info("Remote Media Connection created. Endpoints connected " + event.getEventID());
		MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		MsProvider provider = connection.getSession().getProvider();
		
		MsEventFactory eventFactory = provider.getEventFactory();
		String pathToAudioDirectory = (String) inviteRequest.getSession().getApplicationSession().getAttribute("audioFilePath");
		
		// Let us request for Announcement Complete event or Failure
		// in case if it happens
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
        
		DTMFListener dtmfListener = new DTMFListener(eventFactory, connection, inviteRequest.getSession(), pathToAudioDirectory);
		connection.addNotificationListener(dtmfListener);
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
	
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed, dtmf };
		
		if(inviteRequest.getSession().getApplicationSession().getAttribute("orderApproval") != null) {
			java.io.File speech = new File("speech.wav");
			if(inviteRequest.getSession().getApplicationSession().getAttribute("adminApproval") != null) {
				speech = new File("adminspeech.wav");	
			}			
			logger.info("Playing confirmation announcement : " + "file://" + speech.getAbsolutePath());
	        play.setURL("file://"+ speech.getAbsolutePath());

	        endpoint.execute(requestedSignals, requestedEvents, connection);
	        
			logger.info("Waiting for DTMF at the same time..");
		} else if (inviteRequest.getSession().getApplicationSession().getAttribute("deliveryDate") != null) {			
			String announcementFile = pathToAudioDirectory + "OrderDeliveryDate.wav";
			logger.info("Playing Delivery Date Announcement : " + announcementFile);
			play.setURL(announcementFile);
			endpoint.execute(requestedSignals, requestedEvents, connection);
			
			logger.info("Waiting for DTMF at the same time..");
		} else if (inviteRequest.getSession().getApplicationSession().getAttribute("shipping") != null) {			
			java.io.File speech = new File("shipping.wav");
			logger.info("Playing shipping announcement : " + "file://" + speech.getAbsolutePath());
			MediaResourceListener mediaResourceListener = new MediaResourceListener(inviteRequest.getSession(), connection);
			connection.addNotificationListener(mediaResourceListener);
			play.setURL("file://"+ speech.getAbsolutePath());
			endpoint.execute(requestedSignals, requestedEvents, connection);
			
			logger.info("shipping announcement played. tearing down the call");
		}		
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
