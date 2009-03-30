package org.mobicents.ipbx.session.call.framework;

import javax.servlet.sip.SipSession;

import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.audio.MsRecordRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.mscontrol.events.pkg.MsAudio;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;
import org.mobicents.servlet.sip.seam.entrypoint.media.MsProviderContainer;

/**
 * This class is for convinience when you want to execute media operations on the endpoints
 * and MsLink/MsConnection objects assigned to the current session. Whenever you establish
 * an MsConnection or MsLink the mediaSessionStore is updated and the objects are assigned
 * to you SipSession (mediaSessionStore is session-scoped).
 * 
 * @author vralev
 *
 */
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

	/**
	 * Play an announcement to the specified endpoint using either the MsConnection object or
	 * the MsLink object stored in the mediaSessionStore. The MsLink object takes precedence
	 * because it is likely the terminating object too.
	 * 
	 * Allowed Endpoints for this operation are IVR, Announcement and Conference.
	 * 
	 * @param file
	 */
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
	
	/**
	 * Play an announcement AND detect DTMF to the specified endpoint using either the MsConnection object or
	 * the MsLink object stored in the mediaSessionStore. The MsLink object takes precedence
	 * because it is likely the terminating object too.
	 * 
	 * Note that DTMF detecting introduces an overhead, especially the inband DTMF detection.
	 * 
	 * Allowed Endpoints for this operation are IVR and Conference
	 * 
	 * @param file
	 */
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
	
	/**
	 * Detect DTMF on the endpoint specified in mediaSession store using either the MsLink
	 * (higher precedence) or the MsConnection.
	 * 
	 * Allowed Endpoints for this operation are IVR and Conference
	 */
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
	
	/**
	 * Record to a file. Note the file should be in this format "file:///somewhere/file.wav
	 * It is using the endpoint and the link or connection specified in mediaSessionStore.
	 * 
	 * Allowed Endpoints for this operation are IVR and Conference
	 * 
	 * @param file
	 */
	public void record(String file) {
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
		MsRecordRequestedSignal record = (MsRecordRequestedSignal) eventFactory.createRequestedSignal(MsAudio.RECORD);
		record.setFile(file);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAudio.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);

		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { record };
		MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { dtmf, onFailed };

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

	/**
	 * Stops any previously executed commands.
	 */
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
