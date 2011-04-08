package org.mobicents.servlet.sip.alerting.util;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Jean Deruelle
 *
 */
public class MediaConnectionListener implements MediaEventListener<SdpPortManagerEvent> {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MediaConnectionListener.class);
	
	public static void playAnnouncement(MediaGroup mg, SipSession sipSession) {

		try {
			java.net.URI uri = (java.net.URI) sipSession.getAttribute("speechUri");
		
			logger.info("Playing alert announcement : " + uri);

			mg.getPlayer().play(uri, null, null);
			mg.getSignalDetector().receiveSignals(1, null, null, null);

			logger.info("Waiting for DTMF at the same time..");

		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void onEvent(SdpPortManagerEvent arg0) {
		
	}	
}
