package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import static org.jboss.seam.annotations.Install.BUILT_IN;

@Name("mediaController")
@Scope(ScopeType.APPLICATION)
@Install(precedence=BUILT_IN)
@Startup
public class MediaController {
	
	@In(required=false) private SipSession sipSession;
	@In(required=false) private MsSession msSession;
	
	public MediaController() {
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
		endpoint.addNotificationListener(new NotificationListener(sipSession, msSession, endpoint, null));
	}
	
	public void execute(MsEndpoint endpoint,
			MsRequestedSignal[] signals,
			MsRequestedEvent[] events,
			MsConnection connection) {
		endpoint.execute(signals, events, connection);
		connection.addNotificationListener(new NotificationListener(sipSession, msSession, endpoint, connection));
	}
	
	public void execute(MsEndpoint endpoint,
			MsRequestedSignal[] signals,
			MsRequestedEvent[] events,
			MsLink link) {
		endpoint.execute(signals, events, link);
		link.addNotificationListener(new NotificationListener(sipSession, msSession, endpoint, link));
	}
	
}
