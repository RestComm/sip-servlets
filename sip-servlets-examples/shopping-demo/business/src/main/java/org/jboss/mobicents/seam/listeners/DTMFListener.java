package org.jboss.mobicents.seam.listeners;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.util.DTMFUtils;
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

public class DTMFListener implements MsNotificationListener{
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	
	private static Logger logger = Logger.getLogger(DTMFListener.class);
	
	MsEventFactory eventFactory;
	MsLink link;
	SipSession session;
	private String pathToAudioDirectory;
	
	public DTMFListener(MsEventFactory eventFactory, MsLink link, SipSession session, String pathToAudioDirectory) {
		this.eventFactory = eventFactory;
		this.link = link;
		this.pathToAudioDirectory = pathToAudioDirectory;
		this.session = session;
	}
	
	public void update(MsNotifyEvent evt) {
		logger.info("event FQN " + evt.getEventID().getFqn());
		MsEventIdentifier identifier = evt.getEventID();
        if (identifier.equals(DTMF.TONE)) {
            MsDtmfNotifyEvent event = (MsDtmfNotifyEvent) evt;
            String signal = event.getSequence();
			
			if(session.getAttribute("orderApproval") != null) {
				if(session.getAttribute("adminApproval") != null) {
					logger.info("admin approval in progress.");
					DTMFUtils.adminApproval(session, signal, pathToAudioDirectory);
				} else {
					logger.info("customer approval in progress.");
					DTMFUtils.orderApproval(session, signal, pathToAudioDirectory);
				}
			} else if(session.getAttribute("deliveryDate") != null) {
				logger.info("delivery date update in progress.");
				if(!DTMFUtils.updateDeliveryDate(session, signal)) {				
					MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
					MsRequestedSignal[] signals = new MsRequestedSignal[] {};
					MsRequestedEvent[] events = new MsRequestedEvent[] { dtmf };
	
					link.getEndpoints()[0].execute(signals, events, link);
				}
			}				
        }       
	}

	public void resourceCreated(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void resourceInvalid(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
