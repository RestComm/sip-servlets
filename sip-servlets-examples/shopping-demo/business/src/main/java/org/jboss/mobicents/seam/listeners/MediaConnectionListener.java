package org.jboss.mobicents.seam.listeners;

import java.io.File;
import java.io.IOException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkListener;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsSession;
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
//		handleMedia(event);
//		logger.info("connection hal opened " + event);
//		MsConnection connection = event.getConnection();
//		String sdp = connection.getLocalDescriptor();
//		try {
//			inviteRequest.setContentLength(sdp.length());
//			inviteRequest.setContent(sdp.getBytes(), "application/sdp");						
//			inviteRequest.send();
//		} catch (IOException e) {
//			logger.error("An unexpected exception occured while sending the request", e);
//		}								
//		logger.info("Local Media Connection half created " + event.getEventID());
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

	public void connectionModeRecvOnly(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionModeSendOnly(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionModeSendRecv(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	protected void handleMedia(MsConnectionEvent event) {
		logger.info("Remote Media Connection created. Endpoints connected " + event.getEventID());
		
		final MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		final MsSession session = connection.getSession();
		final MsLink link = session.createLink(MsLinkMode.FULL_DUPLEX);
		link.addLinkListener(new MsLinkListener() {

			public void linkCreated(MsLinkEvent evt) {
				logger.info("PR-IVR link created " + evt);
			}

			public void linkConnected(MsLinkEvent evt) {
				logger.info("link connected " + link.getEndpoints()[0].getLocalName() +
						" " + link.getEndpoints()[1].getLocalName());
//				MsProvider provider = session.getProvider();
//				MsEventFactory eventFactory = provider.getEventFactory();
				inviteRequest.getSession().setAttribute("link", link);
				if((Boolean.TRUE).equals(inviteRequest.getSession().getAttribute("playAnnouncement"))) {
					playAnnouncement(connection, link, inviteRequest.getSession(), (String)inviteRequest.getSession().getAttribute("audioFilePath"));
				}
			}

			public void linkDisconnected(MsLinkEvent evt) {
				logger.info("link disconnected " + evt);
			}

			public void linkFailed(MsLinkEvent evt) {
				logger.info("link failed " + evt);
			}

			public void modeFullDuplex(MsLinkEvent evt) {
				logger.info("link mode full duplex" + evt);
			}

			public void modeHalfDuplex(MsLinkEvent evt) {
				logger.info("link mode half duplex" + evt);
			}
		});
		
		String log = "Linking " + endpoint.getLocalName() + " to IVR";
		logger.info(log);
		
		link.join(IVR_JNDI_NAME, endpoint.getLocalName());
	}

	
	public static void playAnnouncement(MsConnection connection, MsLink link, SipSession sipSession, String pathToAudioDirectory) {
		//playing the file
		MsEventFactory eventFactory = (MsEventFactory) connection.getSession().getProvider().getEventFactory();
		
		MsEndpoint endpoint = link.getEndpoints()[0];			
		
		// Let us request for Announcement Complete event or Failure
		// in case if it happens
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
        
		DTMFListener dtmfListener = new DTMFListener(eventFactory, link, sipSession, pathToAudioDirectory);
		link.addNotificationListener(dtmfListener);
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
	
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed, dtmf };
		
		if(sipSession.getAttribute("orderApproval") != null) {
			java.io.File speech = new File("speech.wav");
			if(sipSession.getAttribute("adminApproval") != null) {
				speech = new File("adminspeech.wav");	
			}			
			logger.info("Playing confirmation announcement : " + "file:///" + speech.getAbsolutePath());
	        play.setURL("file:///"+ speech.getAbsolutePath().replace('\\', '/'));

	        endpoint.execute(requestedSignals, requestedEvents, link);
	        
			logger.info("Waiting for DTMF at the same time..");
		} else if (sipSession.getAttribute("deliveryDate") != null) {			
			String announcementFile = pathToAudioDirectory + "OrderDeliveryDate.wav";
			logger.info("Playing Delivery Date Announcement : " + announcementFile);
			play.setURL(announcementFile.replace('\\', '/'));
			endpoint.execute(requestedSignals, requestedEvents, link);
			
			logger.info("Waiting for DTMF at the same time..");
		} else if (sipSession.getAttribute("shipping") != null) {			
			java.io.File speech = new File("shipping.wav");
			logger.info("Playing shipping announcement : " + "file:///" + speech.getAbsolutePath().replace('\\', '/'));
			MediaResourceListener mediaResourceListener = new MediaResourceListener(sipSession, link, connection);
			link.addNotificationListener(mediaResourceListener);
			play.setURL("file:///"+ speech.getAbsolutePath().replace('\\', '/'));
			endpoint.execute(requestedSignals, requestedEvents, link);
			
			logger.info("shipping announcement played. tearing down the call");
		}		
	}
}
