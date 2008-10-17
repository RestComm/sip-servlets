package org.mobicents.servlet.sip.example;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
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
	private static Log logger = LogFactory.getLog(DTMFListener.class);
	
	MsEventFactory eventFactory;
	MsConnection connection;
	
	public DTMFListener(MsEventFactory eventFactory, MsConnection connection) {
		this.eventFactory = eventFactory;
		this.connection = connection;
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

		connection.getEndpoint().execute(signals, events, connection);
	}

	public void resourceCreated(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void resourceInvalid(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
