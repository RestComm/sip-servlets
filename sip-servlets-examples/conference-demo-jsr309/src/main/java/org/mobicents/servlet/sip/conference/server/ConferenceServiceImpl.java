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

import java.util.Iterator;

import javax.annotation.Resource;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;

import org.mobicents.servlet.sip.conference.client.ConferenceService;
import org.mobicents.servlet.sip.conference.client.ParticipantInfo;
import org.mobicents.servlet.sip.conference.server.media.AnnouncementConferenceParticipant;
import org.mobicents.servlet.sip.conference.server.media.Conference;
import org.mobicents.servlet.sip.conference.server.media.ConferenceCenter;
import org.mobicents.servlet.sip.conference.server.media.ConferenceParticipant;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConferenceServiceImpl extends RemoteServiceServlet implements ConferenceService{

	@Resource
	private static SipFactory sipFactory;
	
	public ParticipantInfo[] getParticipants(String conference, Boolean refresh) {
		
		Conference conf = ConferenceCenter.getInstance().getConference(conference);
		if(refresh) {
			try {
				synchronized (conf) {
					conf.wait(30000);
				}
			} catch (InterruptedException e) {
				return new ParticipantInfo[] {};
			}
		}
		int length = conf.getParticipantNames().length;
		ParticipantInfo[] result = new ParticipantInfo[length];
		int q = 0;
		Iterator<ConferenceParticipant> it = conf.getParticipants().values().iterator();
		while(it.hasNext()) {
			ConferenceParticipant p = it.next();
			result[q] = new ParticipantInfo();
			result[q].name = p.getName();
			result[q].muted = p.isMuted();
			q++;
		}
		return result;
	}

	public void joinAnnouncement(String name, String conference, String url) {
		ConferenceCenter center = ConferenceCenter.getInstance();
		AnnouncementConferenceParticipant ann = new AnnouncementConferenceParticipant(
				name, url);
		center.getConference(conference).joinParticipant(ann);
		
	}

	public void kick(String user, String conference) {
		Conference conf = ConferenceCenter.getInstance().getConference(conference);
		conf.kick(user);
	}

	public void mute(String user, String conference) {
		Conference conf = ConferenceCenter.getInstance().getConference(conference);
		conf.mute(user);
		synchronized (conf) {
			conf.notifyAll();
		}
	}

	public void joinSipPhone(String namr, String conference, String url) {
		try {
			SipApplicationSession appSession = sipFactory.createApplicationSession();
			Address from = sipFactory.createAddress("sip:sip-servlets-conference@sip-servlets.com");
			Address to = sipFactory.createAddress(url);
			SipServletRequest request = sipFactory.createRequest(appSession, "INVITE", from, to);

			request.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void unmute(String user, String conference) {
		Conference conf = ConferenceCenter.getInstance().getConference(conference);
		conf.unmute(user);
		synchronized (conf) {
			conf.notifyAll();
		}
	}

}
