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

package org.mobicents.servlet.sip.conference.server;

import java.io.IOException;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.conference.client.SipGwtConferenceConsole;
import org.mobicents.servlet.sip.conference.server.media.ConferenceCenter;
import org.mobicents.servlet.sip.conference.server.media.ConferenceParticipant;
import org.mobicents.servlet.sip.conference.server.media.PhoneConferenceParticipant;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * sip message).
 * 
 * @author Vladimir Ralev
 *
 */
public class ConferenceConnectionListener implements MediaEventListener<SdpPortManagerEvent>{
	private static Log logger = LogFactory.getLog(ConferenceConnectionListener.class);
	
	private SipServletMessage sipMessage;
	
	public ConferenceConnectionListener(SipServletMessage message) {
		this.sipMessage = message;
	}

	public SipServletMessage getSipMessage() {
		return sipMessage;
	}
	

	public void onEvent(SdpPortManagerEvent event) {
		if(sipMessage instanceof SipServletRequest) {
			onEventRequest(event, (SipServletRequest)sipMessage);
		} else {
			onEventResponse(event, (SipServletResponse)sipMessage);
		}
	}
	
	public void onEventResponse(SdpPortManagerEvent event, SipServletResponse response) {
		logger.info("connection opened " + event);

		String sdp = new String(event.getMediaServerSdp());
		SipServletRequest ack = response.createAck();

		try {
			ack.setContent(sdp, "application/sdp");
			ack.send();
		} catch (Exception e) {
			logger.error(e);
		}
		NetworkConnection conn = event.getSource().getContainer();
		MediaSession mediaSession = event.getSource().getMediaSession();
		String callerName = response.getTo().getURI().toString();

		ConferenceParticipant participant = new PhoneConferenceParticipant(
				callerName, conn, mediaSession, sipMessage);

		String key = SipGwtConferenceConsole.CONFERENCE_NAME;

		ConferenceCenter.getInstance().getConference(key).joinParticipant(
				participant);
	}

	public void onEventRequest(SdpPortManagerEvent event, SipServletRequest request) {

		SdpPortManager sdpmana = event.getSource();
		NetworkConnection conn = sdpmana.getContainer();
		MediaSession mediaSession = event.getSource().getMediaSession();

		SipSession sipSession = (SipSession) mediaSession
				.getAttribute("SIP_SESSION");
		sipSession.removeAttribute("UNANSWERED_INVITE");

		if (event.isSuccessful()) {
			SipServletResponse resp = request
					.createResponse(SipServletResponse.SC_OK);
			try {
				byte[] sdp = event.getMediaServerSdp();

				resp.setContent(sdp, "application/sdp");
				// Send 200 OK
				resp.send();
				if (logger.isDebugEnabled()) {
					logger.debug("Sent OK Response for INVITE");
				}

				sipSession.setAttribute("NETWORK_CONNECTION", conn);
				SipServletRequest inviteRequest = (SipServletRequest) sipMessage;
				String callerName = inviteRequest.getFrom().getURI().toString();

				ConferenceParticipant participant = new PhoneConferenceParticipant(
						callerName, conn, mediaSession, sipMessage);
				
				String key = ((SipURI) inviteRequest.getTo().getURI()).getUser();

				ConferenceCenter.getInstance().getConference(key).joinParticipant(
						participant);
			} catch (Exception e) {
				logger.error(e);

				// Clean up
				sipSession.getApplicationSession().invalidate();
				mediaSession.release();
			}
		} else {
			try {
				if (SdpPortManagerEvent.SDP_NOT_ACCEPTABLE.equals(event
						.getError())) {

					if (logger.isDebugEnabled()) {
						logger
								.debug("Sending SipServletResponse.SC_NOT_ACCEPTABLE_HERE for INVITE");
					}
					// Send 488 error response to INVITE
					request.createResponse(
							SipServletResponse.SC_NOT_ACCEPTABLE_HERE)
							.send();
				} else if (SdpPortManagerEvent.RESOURCE_UNAVAILABLE
						.equals(event.getError())) {
					if (logger.isDebugEnabled()) {
						logger
								.debug("Sending SipServletResponse.SC_BUSY_HERE for INVITE");
					}
					// Send 486 error response to INVITE
					request.createResponse(SipServletResponse.SC_BUSY_HERE)
							.send();
				} else {
					if (logger.isDebugEnabled()) {
						logger
								.debug("Sending SipServletResponse.SC_SERVER_INTERNAL_ERROR for INVITE");
					}
					// Some unknown error. Send 500 error response to INVITE
					request.createResponse(
							SipServletResponse.SC_SERVER_INTERNAL_ERROR)
							.send();
				}
				// Clean up media session
				sipSession.removeAttribute("MEDIA_SESSION");
				mediaSession.release();
			} catch (Exception e) {
				logger.error(e);

				// Clean up
				sipSession.getApplicationSession().invalidate();
				mediaSession.release();
			}
		}
	}

}
