package org.mobicents.servlet.sip.seam.entrypoint.media;

import java.util.HashMap;

import javax.servlet.sip.SipSession;

import org.mobicents.mscontrol.MsNotificationListener;

/**
 * We need separate class to hold the static objects that is not under Seam management, because 
 * javassist creates another class with another static instance and causes confusion.
 * 
 * Static objects in this case are safe.
 * 
 * @author vralev
 *
 */
public class MediaControllerHolder {
	public HashMap<Object, MsNotificationListener> listenerMap = 
		new HashMap<Object, MsNotificationListener>();
	
	private HashMap<SipSession, MediaController> mediaControllers =
		new HashMap<SipSession, MediaController>();
	
	public MediaController getMediaController(SipSession sipSession) {
		return mediaControllers.get(sipSession);
	}
	
	public void putMediaController(SipSession sipSession, MediaController mediaController) {
		mediaControllers.put(sipSession, mediaController);
	}
	
	public void removeMediaController(SipSession sipSession) {
		mediaControllers.remove(sipSession);
	}
	
	private static MediaControllerHolder mediaControllerHolder;
	
	public synchronized static MediaControllerHolder instance() {
		if(mediaControllerHolder == null) {
			mediaControllerHolder = new MediaControllerHolder();
		}
		return mediaControllerHolder;
	}
}
