package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.jboss.seam.core.Events;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsNotificationListener;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.servlet.sip.seam.entrypoint.SeamEntrypointUtils;

public class NotificationListener implements MsNotificationListener {

	private SipSession sipSession;
	private MsEndpoint endpoint;
	private Object link;
	
	private void postEvent(String eventName, MediaEvent event) {
		SeamEntrypointUtils.beginEvent(sipSession);
		Events.instance().raiseEvent(eventName, event);
		SeamEntrypointUtils.endEvent();
	}
	
	public NotificationListener(SipSession sipSession, MsEndpoint endpoint, Object link) {
		this.sipSession = sipSession;
		this.endpoint = endpoint;
		this.link = link;
	}
	
	public void update(MsNotifyEvent arg0) {
		postEvent("mediaEvent", new MediaEvent(sipSession, endpoint, arg0, link));
	}

}
