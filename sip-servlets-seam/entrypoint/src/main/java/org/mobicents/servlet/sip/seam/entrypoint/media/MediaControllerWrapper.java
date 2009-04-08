package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;
import org.mobicents.mscontrol.MsSession;

/**
 * This class provides the session scoped media controller for each SIP session.
 * 
 * @author vralev
 *
 */
@Name("mediaController")
@Scope(ScopeType.SESSION)
@Startup
public class MediaControllerWrapper {
	@In(required=false) SipSession sipSession;
	@In(required=false) MsSession msSession;
	
	@Unwrap
	public synchronized MediaController getMediaController() {
		// If not SIP session
		if(sipSession == null) return null;
		
		MediaController mediaController = MediaControllerHolder.instance().getMediaController(sipSession);
		if(mediaController == null) {
			mediaController = new MediaController(sipSession, msSession);
			MediaControllerHolder.instance().putMediaController(sipSession, mediaController);
		}
		return mediaController;
	}
	
	//TODO: Add observer on session destroyed
}
