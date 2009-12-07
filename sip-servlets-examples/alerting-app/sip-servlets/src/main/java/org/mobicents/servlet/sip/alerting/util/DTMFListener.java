package org.mobicents.servlet.sip.alerting.util;
import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsNotificationListener;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.events.MsEventIdentifier;
import org.mobicents.mscontrol.events.dtmf.MsDtmfNotifyEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;

public class DTMFListener implements MsNotificationListener {

	private static final long serialVersionUID = 1L;
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	private static Logger logger = Logger.getLogger(DTMFListener.class);
	private String alertId;
	private String feedbackUrl;
	
	public DTMFListener(String alertId, String feedbackUrl) {
		this.alertId = alertId;
		this.feedbackUrl = feedbackUrl;
	}
	
	public void update(MsNotifyEvent event) {
		MsEventIdentifier identifier = event.getEventID();
		logger.info("event FQN " + identifier.getFqn());		
        if (identifier.equals(DTMF.TONE)) {
            MsDtmfNotifyEvent dtmfEvent = (MsDtmfNotifyEvent) event;
            String signal = dtmfEvent.getSequence();
			
			DTMFUtils.answerBack(alertId, signal, feedbackUrl);		
        }       
	}

	public void resourceCreated(MsNotifyEvent event) {

		
	}

	public void resourceInvalid(MsNotifyEvent event) {
		// TODO Auto-generated method stub
		
	}

}
