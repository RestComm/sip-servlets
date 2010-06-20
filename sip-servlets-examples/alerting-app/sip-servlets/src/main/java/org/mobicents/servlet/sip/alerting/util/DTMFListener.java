package org.mobicents.servlet.sip.alerting.util;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;

import org.apache.log4j.Logger;

public class DTMFListener implements MediaEventListener<SignalDetectorEvent>{
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

	public void onEvent(SignalDetectorEvent event) {
		String signal = event.getSignalString();
		DTMFUtils.answerBack(alertId, signal, feedbackUrl);		
	}
}
