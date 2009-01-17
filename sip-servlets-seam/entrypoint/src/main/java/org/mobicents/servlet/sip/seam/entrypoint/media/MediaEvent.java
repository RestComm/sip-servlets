package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsNotifyEvent;

public class MediaEvent {
	private SipSession sipSession;
	private MsEndpoint endpoint;
	private Object link;
	private MsNotifyEvent msNotifyEvent;
	
	public MsNotifyEvent getMsNotifyEvent() {
		return msNotifyEvent;
	}

	public void setMsNotifyEvent(MsNotifyEvent msNotifyEvent) {
		this.msNotifyEvent = msNotifyEvent;
	}

	public Object getLink() {
		return link;
	}

	public void setLink(Object link) {
		this.link = link;
	}

	public SipSession getSipSession() {
		return sipSession;
	}

	public void setSipSession(SipSession sipSession) {
		this.sipSession = sipSession;
	}

	public MsEndpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(MsEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public MediaEvent(SipSession sipSession, MsEndpoint endpoint, MsNotifyEvent notifyEvent, Object link) {
		this.sipSession = sipSession;
		this.endpoint = endpoint;
		this.msNotifyEvent = notifyEvent;
		this.link = link;
	}
}
