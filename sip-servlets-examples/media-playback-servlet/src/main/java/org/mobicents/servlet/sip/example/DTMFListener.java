package org.mobicents.servlet.sip.example;
import org.mobicents.mscontrol.*;
import org.mobicents.mscontrol.signal.Basic;

public class DTMFListener implements MsResourceListener{

	MsSignalDetector dtmfDetector;
	MsConnection connection;
	
	public DTMFListener(MsSignalDetector detector, MsConnection connection) {
		this.dtmfDetector = detector;
		this.connection = connection;
	}
	
	public void update(MsNotifyEvent event) {
		event = event;
		System.out.println("DTMF: " + event.getMessage());
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
		dtmfDetector.receive(Basic.DTMF, connection, new String[] {});
	}

}
