package org.jboss.mobicents.seam.listeners;
import javax.servlet.sip.SipSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.util.DTMFUtils;
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

public class DTMFListener implements MsNotificationListener{
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	
	private static Log logger = LogFactory.getLog(DTMFListener.class);
	
	MsEventFactory eventFactory;
	MsConnection connection;
	SipSession session;
	private String pathToAudioDirectory;
	
	public DTMFListener(MsEventFactory eventFactory, MsConnection connection, SipSession session, String pathToAudioDirectory) {
		this.eventFactory = eventFactory;
		this.connection = connection;
		this.pathToAudioDirectory = pathToAudioDirectory;
		this.session = session;
	}
	
	public void update(MsNotifyEvent evt) {
		MsEventIdentifier identifier = evt.getEventID();
        if (identifier.equals(DTMF.TONE)) {
            MsDtmfNotifyEvent event = (MsDtmfNotifyEvent) evt;
            String signal = event.getSequence();
			
			if(session.getApplicationSession().getAttribute("orderApproval") != null) {
				if(session.getApplicationSession().getAttribute("adminApproval") != null) {
					logger.info("admin approval in progress.");
					DTMFUtils.adminApproval(session, signal, pathToAudioDirectory);
				} else {
					logger.info("customer approval in progress.");
					DTMFUtils.orderApproval(session, signal, pathToAudioDirectory);
				}
			} else if(session.getApplicationSession().getAttribute("deliveryDate") != null) {
				logger.info("delivery date update in progress.");
				DTMFUtils.updateDeliveryDate(session, signal);
			}				
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
