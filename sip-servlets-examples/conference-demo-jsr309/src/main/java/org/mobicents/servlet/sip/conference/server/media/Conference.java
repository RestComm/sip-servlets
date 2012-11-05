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

package org.mobicents.servlet.sip.conference.server.media;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.resource.enums.ParameterEnum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.conference.server.MsControlObjects;


public class Conference {
	public final static String CONFERENCE_ENDPOINT_QUERY = "media/trunk/Conference/$";
	
	private static Log logger = LogFactory.getLog(Conference.class);
	
	private MediaMixer mixer = null;
	
	private ConcurrentHashMap<String, ConferenceParticipant> participants = 
		new ConcurrentHashMap<String, ConferenceParticipant>();
	
	private MediaSession mediaSession;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private boolean waitForEndpoint;
	
	private String key;
	
	public Conference(String key) {
		this.key = key;
	}
	
	public synchronized void joinParticipant(final ConferenceParticipant participant) {
		logger.info("Adding user " + participant.getName());
		final Conference thisConference = this;
		if(mediaSession==null)
			mediaSession=participant.getSession();
		executor.execute(new Runnable() {

			public void run() {
				participants.put(participant.getName(), participant);
				participant.join(thisConference);
				synchronized (thisConference) {
					thisConference.notifyAll();
				}
			}
			
		});

	}
	
	public void removeParticipant(String participant) {
		logger.info("Removing user " + participant);
		ConferenceParticipant p = participants.get(participant);
		removeParticipant(p);
	}
	
	public synchronized void removeParticipant(ConferenceParticipant participant) {
		logger.info("Removing user " + participant.getName());
		participant.leave(this);
		participants.remove(participant.getName());
		/*
		int numSipPhoneParticipants = 0;
		for(ConferenceParticipant p: participants.values()) {
			if(p instanceof EndpointConferenceParticipant) {
				numSipPhoneParticipants++;
			}
		}
		if(numSipPhoneParticipants == 0)
			ConferenceCenter.getInstance().removeConference(key);
		*/
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	public synchronized MediaMixer getMixer() {
		if(mixer == null) {
			try {
//				MediaSession createMediaSession = MsControlObjects.msControlFactory.createMediaSession();
				Parameters createParameters = mediaSession.createParameters();
				createParameters.put(ParameterEnum.MAX_PORTS, 100);
				mixer = mediaSession.createMediaMixer(MediaMixer.AUDIO, createParameters);
			} catch (MsControlException e) {
				logger.error(e);
			}
		}
		return mixer;
	}
	
	public String[] getParticipantNames() {
		return this.participants.keySet().toArray(new String[] {});
	}
	
	public Map<String, ConferenceParticipant> getParticipants() {
		return participants;
	}
	
	public void kick(String participantName) {
		this.participants.get(participantName).kick(this);
		this.participants.remove(participantName);
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	public void mute(String participantName) {
		this.participants.get(participantName).mute(this);
	}
	
	public void unmute(String participantName) {
		this.participants.get(participantName).unmute(this);
	}
	
	
	void setMixer(MediaMixer mixer) {
		if(mixer != null) return;
		this.mixer = mixer;
	}

	public synchronized boolean isWaitForEndpoint() {
		return waitForEndpoint;
	}

	private synchronized void setWaitForEndpoint(boolean waitForEndpoint) {
		this.waitForEndpoint = waitForEndpoint;
	}

	public String getKey() {
		return key;
	}
}
