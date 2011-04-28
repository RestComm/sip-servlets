/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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