package org.mobicents.ipbx.session.call.model;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConferenceManager {
	
	private static ConferenceManager conferenceManager;
	
	public static ConferenceManager instance() {
		if(conferenceManager == null) {
			conferenceManager = new ConferenceManager();
		}
		return conferenceManager;
	}
	
	private AtomicInteger confId = new AtomicInteger(0);

	private HashMap<String, Conference> conferences = 
		new HashMap<String, Conference>();
	
	synchronized public void addConference(String name, Conference conf) {
		conferences.put(name, conf);
	}
	
	synchronized public Conference getConference(String name) {
		return conferences.get(name);
	}
	
	synchronized public Conference getNewConference() {
		String confIdString = confId.toString();
		confId.incrementAndGet();
		if(conferences.get(confIdString) == null) {
			Conference conf = new Conference();
			conf.setId(confIdString);
			conferences.put(confIdString, conf);
		}
		return conferences.get(confIdString);
	}
	
	synchronized public void removeConference(String name) {
		conferences.remove(name);
	}
}
