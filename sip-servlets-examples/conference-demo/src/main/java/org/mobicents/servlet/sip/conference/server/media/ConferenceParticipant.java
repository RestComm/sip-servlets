package org.mobicents.servlet.sip.conference.server.media;

import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsSession;

public abstract class ConferenceParticipant {

	public abstract MsEndpoint getEndpoint();

	public abstract MsSession getSession();

	public abstract void join(Conference conference);
	
	public abstract void kick(Conference conference);
	
	public abstract void mute(Conference conference);
	
	public abstract void leave(Conference conference);
	
	protected String name;
	
	public String getName() {
		return name;
	}
	
	

}