package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsNotifyEvent;

/**
 * This is a structure that carries information associated with an event produced by the Media Server.
 * The MsNotifyEvent is the original event and the rest are just the related objects for the event - 
 * the link is the MsLink or the MsConnection object where the event occured. The endpoint is the endpoint
 * where the event occured. And of course the SipSession is the the SIP session where the event occured.
 * 
 * @author vralev
 *
 */
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
