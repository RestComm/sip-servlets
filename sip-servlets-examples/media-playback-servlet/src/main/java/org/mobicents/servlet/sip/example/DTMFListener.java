package org.mobicents.servlet.sip.example;
import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsNotificationListener;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsEventIdentifier;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfNotifyEvent;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;

public class DTMFListener implements MsNotificationListener {
	private static Logger logger = Logger.getLogger(DTMFListener.class);
	
	MsEventFactory eventFactory;
	MsLink link;
	
	public DTMFListener(MsEventFactory eventFactory, MsLink link) {
		this.eventFactory = eventFactory;
		this.link = link;
	}
	
	public void update(MsNotifyEvent evt) {
		MsEventIdentifier identifier = evt.getEventID();
        if (identifier.equals(DTMF.TONE)) {
            MsDtmfNotifyEvent event = (MsDtmfNotifyEvent) evt;
            String signal = event.getSequence();
			
			logger.info("DTMF received " + signal);			
        }
        MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
		MsRequestedSignal[] signals = new MsRequestedSignal[] {};
		MsRequestedEvent[] events = new MsRequestedEvent[] { dtmf };

		link.getEndpoints()[0].execute(signals, events, link);
	}

	public void resourceCreated(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void resourceInvalid(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
