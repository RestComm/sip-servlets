package org.jboss.mobicents.seam.listeners;

import java.io.File;
import java.io.IOException;

import javax.servlet.sip.SipServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsSignalDetector;
import org.mobicents.mscontrol.MsSignalGenerator;
import org.mobicents.mscontrol.signal.Announcement;
import org.mobicents.mscontrol.signal.Basic;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Vladimir Ralev
 * @author Jean Deruelle
 *
 */
public class MediaConnectionListener implements MsConnectionListener {
	private static Log logger = LogFactory.getLog(MediaConnectionListener.class);
	
	private SipServletRequest inviteRequest;	
	
	public void connectionCreated(MsConnectionEvent event) {		
		MsConnection connection = event.getConnection();
		String sdp = connection.getLocalDescriptor();
		try {
			inviteRequest.setContentLength(sdp.length());
			inviteRequest.setContent(sdp.getBytes(), "application/sdp");						
			inviteRequest.send();
		} catch (IOException e) {
			logger.error("An unexpected exception occured while sending the request", e);
		}								
		logger.info("Local Media Connection created " + event.getEventID());
	}

	public void connectionDeleted(MsConnectionEvent event) {
		logger.info("Local Media Connection deleted " + event.getEventID());
		
	}

	public void connectionModifed(MsConnectionEvent event) {
		logger.info("Remote Media Connection created. Endpoints connected " + event.getEventID());
		MsConnection connection = event.getConnection();
		String endpoint = connection.getEndpoint();
		MsSignalGenerator generator = connection.getSession().getProvider().getSignalGenerator(endpoint);		
		java.io.File speech = new File("speech.wav");
		logger.info("Playing confirmation announcement");
		generator.apply(Announcement.PLAY, new String[]{
				"file://" + speech.getAbsolutePath()
				//"http://www.geocities.com/v_ralev/RecordAfterTone.wav"
				//"file:///home/vralev/mobicents/mobicents-google/examples/call-controller2/src/org/mobicents/slee/examples/callcontrol/voicemail/audiofiles/RecordAfterTone.wav"
				});		
		MsSignalDetector dtmfDetector = connection.getSession().getProvider().getSignalDetector(endpoint);
		DTMFListener dtmfListener = new DTMFListener(dtmfDetector, connection);
		dtmfDetector.addResourceListener(dtmfListener);
		generator.addResourceListener(dtmfListener);
		dtmfDetector.receive(Basic.DTMF, connection, new String[] {});
		logger.info("announcement confirmation played. waiting for DTMF ");		
		inviteRequest.getSession().setAttribute("DTMFSession", DTMFListener.DTMF_SESSION_STARTED);
	}

	public void txFailed(MsConnectionEvent event) {
		logger.info("Transaction failed on event "+ event.getEventID() + "with message " + event.getMessage());
		
	}

	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
	}

}
