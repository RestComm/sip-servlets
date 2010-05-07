package org.jboss.mobicents.seam.util;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;

public class EndCallWhenPlaybackCompletedListener implements MediaEventListener<PlayerEvent> {
	private static Logger logger = Logger.getLogger(EndCallWhenPlaybackCompletedListener.class);
	
	private SipSession sipSession;
	
	public EndCallWhenPlaybackCompletedListener(SipSession sipSession) {
		this.sipSession = sipSession;
	}
	public void onEvent(PlayerEvent event) {
		try {
			logger.info("ENDING CALL ");
			Player player = event.getSource();
			MediaGroup mg = player.getContainer();
			if (event.isSuccessful()
					&& (PlayerEvent.PLAY_COMPLETED == event.getEventType())) {
				MediaSession session =(MediaSession)sipSession.getAttribute("mediaSession");
				session.release();
				Thread.sleep(1500);
				
				SipServletRequest byeRequest = sipSession.createRequest("BYE");				
				byeRequest.send();
				
			}

		}
		catch (Exception e) {
			logger.error("Error", e);
		}
	}

}