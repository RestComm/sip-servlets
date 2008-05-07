package org.jboss.mobicents.seam.listeners;
import javax.servlet.sip.SipSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.util.DTMFUtils;
import org.mobicents.media.server.impl.common.events.EventID;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.MsResourceListener;
import org.mobicents.mscontrol.MsSignalDetector;

public class DTMFListener implements MsResourceListener{
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	
	private static Log logger = LogFactory.getLog(DTMFListener.class);
	
	MsSignalDetector dtmfDetector;
	MsConnection connection;
	SipSession session;
	private String pathToAudioDirectory;
	
	public DTMFListener(MsSignalDetector detector, MsConnection connection, SipSession session, String pathToAudioDirectory) {
		this.dtmfDetector = detector;
		this.connection = connection;
		this.pathToAudioDirectory = pathToAudioDirectory;
		this.session = session;
	}
	
	public void update(MsNotifyEvent event) {
		dtmfDetector.receive(EventID.DTMF, connection, new String[] {});
		logger.info("DTMF: " + event.getMessage());
		String signal = event.getMessage();		
		/**
		 * Concurency issue:
		 * 20:56:33,012 ERROR [STDERR] Exception in thread "Thread-104" 
20:56:33,020 ERROR [STDERR] java.util.ConcurrentModificationException
20:56:33,020 ERROR [STDERR] 	at java.util.AbstractList$Itr.checkForComodification(AbstractList.java:372)
20:56:33,020 ERROR [STDERR] 	at java.util.AbstractList$Itr.next(AbstractList.java:343)
20:56:33,020 ERROR [STDERR] 	at org.mobicents.media.server.impl.dtmf.BaseDtmfDetector.sendEvent(BaseDtmfDetector.java:54)
20:56:33,020 ERROR [STDERR] 	at org.mobicents.media.server.impl.dtmf.DtmfBuffer.push(DtmfBuffer.java:66)
20:56:33,020 ERROR [STDERR] 	at org.mobicents.media.server.impl.dtmf.InbandDetector.transferData(InbandDetector.java:147)
20:56:33,020 ERROR [STDERR] 	at org.mobicents.media.server.impl.jmf.splitter.SplitterOutputStream.push(SplitterOutputStream.java:97)
20:56:33,020 ERROR [STDERR] 	at org.mobicents.media.server.impl.jmf.splitter.MediaSplitter.transferData(MediaSplitter.java:88)
20:56:33,020 ERROR [STDERR] 	at org.mobicents.media.server.impl.rtp.ReceiveStream.run(ReceiveStream.java:127)
20:56:33,020 ERROR [STDERR] 	at java.lang.Thread.run(Thread.java:619)
		 */
		if(session.getApplicationSession().getAttribute("orderApproval") != null) {
			if(session.getApplicationSession().getAttribute("adminApproval") != null) {
				logger.info("customer approval in progress.");
				DTMFUtils.adminApproval(session, signal, pathToAudioDirectory);
			} else {
				logger.info("customer approval in progress.");
				DTMFUtils.orderApproval(session, signal, pathToAudioDirectory);
			}
		} else if(session.getApplicationSession().getAttribute("deliveryDate") != null) {
			DTMFUtils.updateDeliveryDate(session, signal);
		}				
	}

}
