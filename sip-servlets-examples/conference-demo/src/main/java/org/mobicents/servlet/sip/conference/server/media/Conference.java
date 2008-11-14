package org.mobicents.servlet.sip.conference.server.media;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsProvider;


public class Conference {
	public final static String CONFERENCE_ENDPOINT_QUERY = "media/trunk/Conference/$";
	
	private static Log logger = LogFactory.getLog(Conference.class);
	
	private MsEndpoint conferenceEndpoint = null;
	
	private ConcurrentHashMap<String, ConferenceParticipant> participants = 
		new ConcurrentHashMap<String, ConferenceParticipant>();
	
	private MsProvider provider;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private boolean waitForEndpoint;
	
	private String key;
	
	public Conference(MsProvider provider, String key) {
		this.provider = provider;
		this.key = key;
	}
	
	public synchronized void joinParticipant(final ConferenceParticipant participant) {
		logger.info("Adding user " + participant.getName());
		final Conference thisConference = this;
		
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
	
	public MsEndpoint getConferenceEndpoint() {
		if(waitForEndpoint) {
			for(int q=0; q<10; q++) {
				if(conferenceEndpoint != null) break;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
		return conferenceEndpoint;
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
	
	public String getConferenceEndpointName() {
		String confEnpName = null;
		if(getConferenceEndpoint() != null) {
			confEnpName = getConferenceEndpoint().getLocalName();
		} else {
			confEnpName = Conference.CONFERENCE_ENDPOINT_QUERY;
		}
		setWaitForEndpoint(true);
		return confEnpName;
	}
	
	void setConferenceEndpoint(MsEndpoint endpoint) {
		if(conferenceEndpoint != null) return;
		conferenceEndpoint = endpoint;
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
