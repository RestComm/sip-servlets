package org.mobicents.servlet.sip.example;

import java.io.File;
import java.io.IOException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkListener;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsProvider;
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
 *
 */
public class MediaConnectionListener implements MsConnectionListener{
	private static Logger logger = Logger.getLogger(MediaConnectionListener.class);
	public static final String IVR_JNDI_NAME = "media/trunk/IVR/$";
	
	private SipServletRequest inviteRequest;
	
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
		try {
			inviteRequest.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
		} catch (IOException e) {
			logger.error("Unexpected exception while sending the error response", e);
		}
	}
	
	public void connectionHalfOpen(MsConnectionEvent arg0) {
		logger.info("connection half opened" + arg0);
	}

	public void connectionOpen(MsConnectionEvent event) {
		logger.info("connection opened " + event);
		final MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		final MsSession session = connection.getSession();
		final MsLink link = session.createLink(MsLinkMode.FULL_DUPLEX);
		inviteRequest.getSession().setAttribute("link", link);
		link.addLinkListener(new MsLinkListener() {

			public void linkCreated(MsLinkEvent evt) {
				logger.info("PR-IVR link created " + evt);
			}

			public void linkConnected(MsLinkEvent evt) {
				logger.info("link connected " + link.getEndpoints()[0].getLocalName() +
						" " + link.getEndpoints()[1].getLocalName());
				MsProvider provider = session.getProvider();
				MsEventFactory eventFactory = provider.getEventFactory();
				java.io.File speech = new File("speech.wav");
				
				// Let us request for Announcement Complete event or Failure
				// in case if it happens
				MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
				onCompleted.setEventAction(MsEventAction.NOTIFY);

				MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
				onFailed.setEventAction(MsEventAction.NOTIFY);
				
				MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
				
				play.setURL("file:///" + speech.getAbsolutePath().replace('\\', '/'));
				
				DTMFListener dtmfListener = new DTMFListener(eventFactory, link);
				provider.addNotificationListener(dtmfListener);
				MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
				
				MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
		        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { //onCompleted, onFailed,
		        		dtmf };
				
		        logger.info("Executing requests...");
		        link.getEndpoints()[0].execute(requestedSignals, requestedEvents, (MsLink) link);		
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
		
		String sdp = event.getConnection().getLocalDescriptor();
		SipServletResponse sipServletResponse = inviteRequest.createResponse(SipServletResponse.SC_OK);
		try {
			sipServletResponse.setContent(sdp, "application/sdp");
			sipServletResponse.send();
		} catch (Exception e) {e.printStackTrace();}

	}


	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
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
