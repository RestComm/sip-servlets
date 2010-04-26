package org.mobicents.servlet.sip.conference.server.media;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.mediagroup.MediaGroup;


public abstract class ConferenceParticipant{

	public abstract MediaGroup getMediaGroup();

	public abstract MediaSession getSession();

	public abstract void join(Conference conference);
	
	public abstract void kick(Conference conference);
	
	public abstract void mute(Conference conference);
	
	public abstract void unmute(Conference conference);
	
	public abstract void leave(Conference conference);
	
	public boolean isMuted() {
		return muted;
	}
	
	protected boolean muted = false;
	
	protected String name;
	
	public String getName() {
		return name;
	}
	
	

}