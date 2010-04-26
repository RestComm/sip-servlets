package org.mobicents.servlet.sip.conference.server.media;

import java.util.HashMap;

public class ConferenceCenter {
	private HashMap<String, Conference> conferences = new HashMap<String, Conference> ();
	
	public ConferenceCenter() {
	}
	
	
	public synchronized Conference getConference(String key) {
		Conference conf = conferences.get(key);
	
		if(conf == null) {
			conf = new Conference(key);
			conferences.put(key, conf);
			return conf;
		} else {
			return conf;
		}
	}
	
	public synchronized void removeConference(String key) {
		conferences.remove(key);
	}
	
	public String[] getConferences() {
		return conferences.keySet().toArray(new String[]{});
	}
	
	private static ConferenceCenter conferenceCenter;
	
	public synchronized static ConferenceCenter getInstance() {
		if(conferenceCenter == null) {
			conferenceCenter = new ConferenceCenter();
		}
		return conferenceCenter;
	}
}
