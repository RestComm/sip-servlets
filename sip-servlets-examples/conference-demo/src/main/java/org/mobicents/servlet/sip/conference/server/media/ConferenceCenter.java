package org.mobicents.servlet.sip.conference.server.media;

import java.util.HashMap;

import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;

public class ConferenceCenter {
	private HashMap<String, Conference> conferences = new HashMap<String, Conference> ();
	private MsProvider provider;
	
	public ConferenceCenter() {
		getProvider();
	}
	
	public MsProvider getProvider() {
		if(provider == null) {
			MsPeer peer = null;
			try {
				peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			provider = peer.getProvider();
		}
		return provider;
	}
	
	public synchronized Conference getConference(String key) {
		Conference conf = conferences.get(key);
	
		if(conf == null) {
			conf = new Conference(provider, key);
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
