package org.mobicents.servlet.sip.seam.entrypoint.media;

import javax.servlet.sip.SipSession;

import org.jboss.seam.core.Events;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.servlet.sip.seam.entrypoint.SeamEntrypointUtils;

public class ConnectionListener implements MsConnectionListener{

	private SipSession sipSession;
	
	public ConnectionListener(SipSession sipSession) {
		this.sipSession = sipSession;
	}
	
	private void postEvent(String eventName, MsConnectionEvent event) {
		SeamEntrypointUtils.beginEvent(sipSession);
		Events.instance().raiseEvent(eventName, event);
		SeamEntrypointUtils.endEvent();
	}
	
	public void connectionCreated(MsConnectionEvent arg0) {
		postEvent("connectionCreated", arg0);
	}

	public void connectionDisconnected(MsConnectionEvent arg0) {
		postEvent("connectionDisconnected", arg0);
	}

	public void connectionFailed(MsConnectionEvent arg0) {
		postEvent("connectionFailed", arg0);
	}

	public void connectionHalfOpen(MsConnectionEvent arg0) {
		postEvent("connectionHalfOpen", arg0);
	}

	public void connectionModeRecvOnly(MsConnectionEvent arg0) {
		postEvent("connectionModeRecvOnly", arg0);
	}

	public void connectionModeSendOnly(MsConnectionEvent arg0) {
		postEvent("connectionModeSendOnly", arg0);
	}

	public void connectionModeSendRecv(MsConnectionEvent arg0) {
		postEvent("connectionModeSendRecv", arg0);
	}

	public void connectionOpen(MsConnectionEvent arg0) {
		postEvent("connectionOpen", arg0);
	}

}
