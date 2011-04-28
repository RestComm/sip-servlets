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
