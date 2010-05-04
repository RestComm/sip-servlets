package org.jboss.mobicents.seam.listeners;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.util.DTMFUtils;

public class DTMFListener implements MediaEventListener<SignalDetectorEvent>{
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	
	private static Logger logger = Logger.getLogger(DTMFListener.class);
	
	MediaGroup mg;
	SipSession session;
	private String pathToAudioDirectory;

	public DTMFListener(MediaGroup mg, SipSession session, String pathToAudioDirectory) {
		this.mg = mg;
		this.pathToAudioDirectory = pathToAudioDirectory;
		this.session = session;
	}


	public void onEvent(SignalDetectorEvent arg0) {
		String signal = arg0.getSignalString();
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
				try {
					mg.getSignalDetector().receiveSignals(1, null, null, null);
				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
}
