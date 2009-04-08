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

/**
 * Media controller is a per-SipSession entity responisble for the audio RTP stream associated with the SipSession.
 * From here you can contruct the audio path (or the chain of endpoints) that the RTP will pass through for processing).
 * You can construct MsLink or MsConnection objects from here that are under Seam Telco Framework management. When a
 * MsLink or MsConnection is under Telco Framework management the events produced from these objects will be passed
 * as Seam events and can be captured byt subscribing methods with the @Observer annotation.
 * 
 * You don't have to install your own listeners, the framework will do that for you. Avoid creating MsLinks and 
 * MsConnections with the MSC-API methods when possible.
 * 
 * @author vralev
 *
 */
public class MediaController {
	
	private SipSession sipSession;
	private MsSession msSession;
	
	public MediaController(SipSession sipSession, MsSession msSession) {
		this.sipSession = sipSession;
		this.msSession = msSession;
	}
	
	/**
	 * Create a connection managed by the framework. Events produced by the connection will be delivered through
	 * Seam events.
	 * 
	 * @param endpoint
	 * @return
	 */
	public MsConnection createConnection(String endpoint) {
		MsConnection msConnection = msSession.createNetworkConnection(endpoint);
		msConnection.addConnectionListener(new ConnectionListener(sipSession));
		return msConnection;
	}
	
	/**
	 * Create a link managed by the framework. Events produced by the link will be delivered through
	 * Seam events. No need to install your own listener.
	 * 
	 * @param mode
	 * @return
	 */
	public MsLink createLink(MsLinkMode mode) {
		MsLink link = msSession.createLink(mode);
		link.addLinkListener(new LinkListener(sipSession));
		return link;
	}
	
	
	/**
	 * Execute a a request or signal in a managed way. The Events will be delivered as Seam events. no need to
	 * install your own listeners.
	 * 
	 * @param endpoint
	 * @param signals
	 * @param events
	 */
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
	
	/**
	 * Execute a a request or signal in a managed way. The Events will be delivered as Seam events. no need to
	 * install your own listeners.
	 * 
	 * @param endpoint
	 * @param signals
	 * @param events
	 * @param connection
	 */
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
	
	/**
	 * Execute a a request or signal in a managed way. The Events will be delivered as Seam events. no need to
	 * install your own listeners.
	 * 
	 * @param endpoint
	 * @param signals
	 * @param events
	 * @param link
	 */
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
