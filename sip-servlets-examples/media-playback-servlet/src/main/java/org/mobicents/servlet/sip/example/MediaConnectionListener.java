package org.mobicents.servlet.sip.example;

import java.io.File;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Vladimir Ralev
 *
 */
public class MediaConnectionListener implements MsConnectionListener{
	
	private SipServletRequest inviteRequest;
	
	public void connectionCreated(MsConnectionEvent event) {
		String sdp = event.getConnection().getLocalDescriptor();
		SipServletResponse sipServletResponse = inviteRequest.createResponse(SipServletResponse.SC_OK);
		try {
			sipServletResponse.setContent(sdp, "application/sdp");
			sipServletResponse.send();
		} catch (Exception e) {e.printStackTrace();}
		MsConnection connection = event.getConnection();
		MsEndpoint endpoint = connection.getEndpoint();
		MsProvider provider = connection.getSession().getProvider();
		MsEventFactory eventFactory = provider.getEventFactory();
		java.io.File speech = new File("speech.wav");
		
		// Let us request for Announcement Complete event or Failure
		// in case if it happens
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		
		play.setURL("file://" + speech.getAbsolutePath());
		
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed };
		
		endpoint.execute(requestedSignals, requestedEvents, connection);		

		DTMFListener dtmfListener = new DTMFListener(eventFactory, connection);
		provider.addNotificationListener(dtmfListener);
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
		MsRequestedSignal[] signals = new MsRequestedSignal[] {};
		MsRequestedEvent[] events = new MsRequestedEvent[] { dtmf };

		endpoint.execute(signals, events, connection);
	}

	public void connectionDeleted(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionModifed(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionInitialized(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void txFailed(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
	}

	public void connectionDisconnected(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionFailed(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionHalfOpen(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionOpen(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
