package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsNotificationListener;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;

public class MediaController {
	
	private SipSession sipSession;
	private MsSession msSession;
	
	public MediaController(SipSession sipSession, MsSession msSession) {
		this.sipSession = sipSession;
		this.msSession = msSession;
	}
	
	public MsConnection createConnection(String endpoint) {
		MsConnection msConnection = msSession.createNetworkConnection(endpoint);
		msConnection.addConnectionListener(new ConnectionListener(sipSession));
		return msConnection;
	}
	
	public MsLink createLink(MsLinkMode mode) {
		MsLink link = msSession.createLink(mode);
		link.addLinkListener(new LinkListener(sipSession));
		return link;
	}
	
	public void execute(MsEndpoint endpoint,
			MsRequestedSignal[] signals,
			MsRequestedEvent[] events) {
		endpoint.execute(signals, events);
		if(MediaControllerHolder.instance().listenerMap.get(endpoint) == null) {
			MsNotificationListener newListener = 
				new NotificationListener(sipSession, msSession, endpoint, null);
			MediaControllerHolder.instance().listenerMap.put(endpoint, newListener);
			endpoint.addNotificationListener(newListener);
		}
	}
	
	public void execute(MsEndpoint endpoint,
			MsRequestedSignal[] signals,
			MsRequestedEvent[] events,
			MsConnection connection) {
		endpoint.execute(signals, events, connection);
		if(MediaControllerHolder.instance().listenerMap.get(connection) == null) {
			MsNotificationListener newListener = 
				new NotificationListener(sipSession, msSession, endpoint, connection);
			MediaControllerHolder.instance().listenerMap.put(connection, newListener);
			connection.addNotificationListener(newListener);
		}
	}
	
	public void execute(MsEndpoint endpoint,
			MsRequestedSignal[] signals,
			MsRequestedEvent[] events,
			MsLink link) {
		endpoint.execute(signals, events, link);
		if(MediaControllerHolder.instance().listenerMap.get(link) == null) {
			MsNotificationListener newListener = 
				new NotificationListener(sipSession, msSession, endpoint, link);
			MediaControllerHolder.instance().listenerMap.put(link, newListener);
			link.addNotificationListener(newListener);
		}
	}
	
}
