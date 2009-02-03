package org.mobicents.ipbx.session.call.framework;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;

@Name("connectionIVRHelper")
@Scope(ScopeType.APPLICATION)
@Startup(depends={"mediaController"})
public class ConnectionIVRHelper {
	
	@In(required=false) SipSession sipSession;
	@In(required=false) MsEventFactory eventFactory;
	@In(required=false) MediaController mediaController;
	@In(required=false) MediaSessionStore mediaSessionStore;
	
	public void playAnnouncement(String file) {
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		play.setURL(file);
		
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed};
        
        MsConnection msConnection = mediaSessionStore.getMsConnection();
        mediaController.execute(msConnection.getEndpoint(), requestedSignals, requestedEvents, msConnection);
		
	}
	
	public void playAnnouncementWithDtmf(String file) {
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		play.setURL(file);
		
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
	
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed, dtmf };
        
        MsConnection msConnection = mediaSessionStore.getMsConnection();
        mediaController.execute(msConnection.getEndpoint(), requestedSignals, requestedEvents, msConnection);
	}
	
	public void detectDtmf() {
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
	
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] {  };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { dtmf };
        
        MsConnection msConnection = mediaSessionStore.getMsConnection();  
        mediaController.execute(msConnection.getEndpoint(), requestedSignals, requestedEvents, msConnection);
	}
	
	public void endAll() {
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] {  };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] {  };
        
        MsConnection msConnection = mediaSessionStore.getMsConnection();  
        mediaController.execute(msConnection.getEndpoint(), requestedSignals, requestedEvents, msConnection);
	}
	
}
