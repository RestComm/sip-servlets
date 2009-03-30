package org.mobicents.ipbx.session.call.framework;

import java.util.HashMap;

import javax.servlet.sip.SipSession;

import org.mobicents.servlet.sip.seam.entrypoint.media.MediaControllerHolder;

/**
 * Central place for all IVRHelpers in the application.
 * 
 * @author vralev
 *
 */
public class IVRHelperManager {
	
	private static IVRHelperManager ivrHelperManager;
	
	private HashMap<SipSession, MediaSessionStore> mediaSessionMap =
		new HashMap<SipSession, MediaSessionStore>();
	
	public void put(SipSession sipSession, MediaSessionStore mediaSessionStore) {
		mediaSessionMap.put(sipSession, mediaSessionStore);
	}
	
	public void remove(SipSession sipSession) {
		mediaSessionMap.remove(sipSession);
	}
	
	public MediaSessionStore getMediaSessionStore(SipSession sipSession) {
		return mediaSessionMap.get(sipSession);
	}
	
	public IVRHelper getIVRHelper(SipSession sipSession) {
		return new IVRHelper(sipSession, getMediaSessionStore(sipSession),
				MediaControllerHolder.instance().getMediaController(sipSession));
	}
	
	public synchronized static IVRHelperManager instance() {
		if(ivrHelperManager == null) {
			ivrHelperManager = new IVRHelperManager();
		}
		return ivrHelperManager;
	}
	
	
}
