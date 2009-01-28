package org.mobicents.ipbx.session.call.model;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("conferenceManager")
@Scope(ScopeType.APPLICATION)
@Startup
public class ConferenceManager {
	private static AtomicInteger confId = new AtomicInteger(0);

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
			conf.setName(confIdString);
			conferences.put(confIdString, conf);
		}
		return conferences.get(confIdString);
	}
	
	synchronized public void removeConference(String name) {
		conferences.remove(name);
	}
}
