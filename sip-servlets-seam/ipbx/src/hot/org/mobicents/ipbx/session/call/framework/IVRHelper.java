package org.mobicents.ipbx.session.call.framework;

import javax.servlet.sip.SipSession;

import org.jboss.seam.annotations.In;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;
import org.mobicents.servlet.sip.seam.entrypoint.media.MsProviderContainer;

public class IVRHelper {
	SipSession sipSession;
	MsEventFactory eventFactory;
	MediaController mediaController;
	MediaSessionStore mediaSessionStore;
	
	public IVRHelper(SipSession sipSession,
			MediaSessionStore mediaSessionStore,
			MediaController mediaController) {
		this.sipSession = sipSession;
		this.mediaSessionStore = mediaSessionStore;
		this.mediaController = mediaController;
		this.eventFactory = MsProviderContainer.msProvider.getEventFactory();
	}

	public void playAnnouncement(String file) {
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		play.setURL(file);
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed};
        
        MsLink msLink = mediaSessionStore.getMsLink();
        MsConnection msConnection = mediaSessionStore.getMsConnection();
        
        if(msLink != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msLink);
        } else if (msConnection != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msConnection);
        }
		
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
        
        MsLink msLink = mediaSessionStore.getMsLink();
        MsConnection msConnection = mediaSessionStore.getMsConnection();
        
        if(msLink != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msLink);
        } else if (msConnection != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msConnection);
        }
	}
	
	public void detectDtmf() {
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
	
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] {  };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { dtmf };
        
        MsLink msLink = mediaSessionStore.getMsLink();
        MsConnection msConnection = mediaSessionStore.getMsConnection();
        
        if(msLink != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msLink);
        } else if (msConnection != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msConnection);
        }
	}
	
	public void endAll() {
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] {  };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { };
        
        MsLink msLink = mediaSessionStore.getMsLink();
        MsConnection msConnection = mediaSessionStore.getMsConnection();
        
        if(msLink != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msLink);
        } else if (msConnection != null) {
        	mediaController.execute(
        			mediaSessionStore.getMsEndpoint(), 
        			requestedSignals, 
        			requestedEvents, 
        			msConnection);
        }
	}
}
