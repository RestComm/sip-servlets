package org.mobicents.servlet.sip.example;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.impl.common.events.EventID;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.MsResourceListener;
import org.mobicents.mscontrol.MsSignalDetector;

public class DTMFListener implements MsResourceListener{
	private static Log logger = LogFactory.getLog(DTMFListener.class);
	
	MsSignalDetector dtmfDetector;
	MsConnection connection;
	
	public DTMFListener(MsSignalDetector detector, MsConnection connection) {
		this.dtmfDetector = detector;
		this.connection = connection;
	}
	
	public void update(MsNotifyEvent event) {
		logger.info("DTMF: " + event.getMessage());
		dtmfDetector.receive(EventID.DTMF, connection, new String[] {});
	}

	public void resourceCreated(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void resourceInvalid(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
