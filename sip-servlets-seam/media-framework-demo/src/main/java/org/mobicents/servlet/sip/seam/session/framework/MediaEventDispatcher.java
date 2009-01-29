package org.mobicents.servlet.sip.seam.session.framework;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.core.Events;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.events.MsEventIdentifier;
import org.mobicents.mscontrol.events.dtmf.MsDtmfNotifyEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.mscontrol.events.pkg.MsAudio;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaEvent;

@Name("mediaEventDispatcher")
@Scope(ScopeType.APPLICATION)
@Install(precedence=FRAMEWORK)
@Startup
public class MediaEventDispatcher {
	private ConcurrentHashMap<Object, StringBuffer> dtmfBuffer =
		new ConcurrentHashMap<Object, StringBuffer>();
	
	private void addNumber(Object object, String number) {
		if(dtmfBuffer.get(object) == null) {
			dtmfBuffer.put(object, new StringBuffer());
		}
		dtmfBuffer.get(object).append(number);
	}
	
	public void reset(Object object) {
		dtmfBuffer.put(object, new StringBuffer());
	}
	
	public String getDtmfArchive(Object object) {
		return dtmfBuffer.get(object).toString();
	}
	
	public String getDtmfArchive(Object object, int digits) {
		String stringDigits = getDtmfArchive(object);
		int numDigits = stringDigits.length();
		return stringDigits.substring(numDigits - digits, numDigits);
	}
	
	@Observer("mediaEvent")
	public void doMediaEvent(MediaEvent mediaEvent) {
		MsEventIdentifier identifier = mediaEvent.getMsNotifyEvent().getEventID();
        if (identifier.equals(DTMF.TONE)) {
            MsDtmfNotifyEvent event = (MsDtmfNotifyEvent) mediaEvent.getMsNotifyEvent();
            String signal = event.getSequence();
            addNumber(mediaEvent.getLink(), signal);
            addNumber(mediaEvent.getEndpoint(), signal);
            addNumber(mediaEvent.getSipSession(), signal);
            Events.instance().raiseEvent("DTMF", signal);
        } else if(identifier.equals(MsAnnouncement.COMPLETED)) {
        	Events.instance().raiseEvent("announcementComplete");
        } else if(identifier.equals(MsAnnouncement.FAILED)) {
        	Events.instance().raiseEvent("announcementFailed");
        } else if(identifier.equals(MsAudio.FAILED)) {
        	Events.instance().raiseEvent("audioFailed");
        }
        
	}
	
	@Observer("sipSessionDestroyed")
	public void doSipSessionDestroyed(SipSession sipSession) {
		dtmfBuffer.remove(sipSession);
	}
	
	@Observer("linkDisconnected")
	public void doLinkDisconnected(MsLinkEvent linkEvent) {
		dtmfBuffer.remove(linkEvent.getSource());
	}
	
	@Observer("connectionDisconnected")
	public void doConnectionDisconnected(MsConnectionEvent connectionEvent) {
		dtmfBuffer.remove(connectionEvent.getConnection());
	}

}
