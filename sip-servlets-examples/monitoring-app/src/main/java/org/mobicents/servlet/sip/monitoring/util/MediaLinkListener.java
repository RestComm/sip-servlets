/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.servlet.sip.monitoring.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkListener;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MediaLinkListener implements MsLinkListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(MediaLinkListener.class);
	
	private MsLink link;
	private MsConnection connection;
	private SipServletRequest inviteRequest;
	
	public MediaLinkListener(MsLink link, MsConnection connection, SipServletRequest sipServletRequest) {
		this.link = link;
		this.inviteRequest = sipServletRequest;
		this.connection = connection;
	}
	
	public void linkCreated(MsLinkEvent evt) {
		logger.info("PR-IVR link created " + evt);
	}

	public void linkConnected(MsLinkEvent evt) {
		logger.info("link connected " + link.getEndpoints()[0].getLocalName() +
				" " + link.getEndpoints()[1].getLocalName());

		inviteRequest.getSession().setAttribute("link", link);
		if(Boolean.TRUE.equals(inviteRequest.getSession().getAttribute("playAnnouncement"))) {
			playAnnouncement(connection, link, inviteRequest.getSession(), (String)inviteRequest.getSession().getAttribute("fileName"), (String)inviteRequest.getSession().getAttribute("alertId"), (String)inviteRequest.getSession().getAttribute("feedbackUrl"));
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

	public static void playAnnouncement(MsConnection connection, MsLink link, SipSession sipSession, String fileName, String alertId, String feedbackUrl) {
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
        
		DTMFListener dtmfListener = new DTMFListener(alertId, feedbackUrl);
		link.addNotificationListener(dtmfListener);
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
	
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed, dtmf };
		
		try {
			java.io.File speech = new File(new URI(fileName));
//			java.io.File speech = new File(fileName);
			logger.info("Playing confirmation announcement : " + "file:///"+ speech.getAbsolutePath());
	        play.setURL("file:///"+ speech.getAbsolutePath().replace('\\', '/'));
		} catch (URISyntaxException e) {
			logger.error("unexpected exception while setting the URL for the media file to play", e);
		}
        endpoint.execute(requestedSignals, requestedEvents, link);
		logger.info("Waiting for DTMF at the same time..");			
	}
}
