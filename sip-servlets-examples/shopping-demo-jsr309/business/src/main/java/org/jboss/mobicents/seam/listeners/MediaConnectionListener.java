package org.jboss.mobicents.seam.listeners;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.util.EndCallWhenPlaybackCompletedListener;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Vladimir Ralev
 * @author Jean Deruelle
 *
 */
public class MediaConnectionListener implements MediaEventListener<SdpPortManagerEvent> {
	private static Logger logger = Logger.getLogger(MediaConnectionListener.class);
	
	
	private SipServletRequest inviteRequest;	


	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
	}
	
	public static void playAnnouncement(MediaGroup mg, SipSession sipSession, String pathToAudioDirectory) {

		try {
			java.net.URI uri = (java.net.URI) sipSession.getAttribute("speechUri");
			if(sipSession.getAttribute("orderApproval") != null) {
		
				logger.info("Playing confirmation announcement : " + uri);

				mg.getPlayer().play(uri, null, null);
				mg.getSignalDetector().receiveSignals(1, null, null, null);

				logger.info("Waiting for DTMF at the same time..");
			} else if (sipSession.getAttribute("deliveryDate") != null) {			
				logger.info("Playing Delivery Date Announcement : " + uri);
				mg.getPlayer().play(uri, null, null);
				mg.getSignalDetector().receiveSignals(1, null, null, null);

				logger.info("Waiting for DTMF at the same time..");
			} else if (sipSession.getAttribute("shipping") != null) {			
				logger.info("Playing shipping announcement : " + uri);

				mg.getPlayer().play(uri, null, null);
				mg.getPlayer().addListener(new EndCallWhenPlaybackCompletedListener(sipSession));

				logger.info("shipping announcement played. tearing down the call");
			}		
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void onEvent(SdpPortManagerEvent arg0) {
		if((Boolean.TRUE).equals(inviteRequest.getSession().getAttribute("playAnnouncement"))) {
			NetworkConnection connection = (NetworkConnection) inviteRequest.getSession().getAttribute("connection");
			MediaGroup mg =(MediaGroup)inviteRequest.getSession().getAttribute("mediaGroup");
			try {
				playAnnouncement(mg, inviteRequest.getSession(), (String)inviteRequest.getSession().getAttribute("audioFilePath"));
			}catch (Exception e) {
				logger.error(e);
			}

		}

	}
}
