package org.mobicents.servlet.sip.example;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsSignalGenerator;
import org.mobicents.mscontrol.signal.Announcement;

/**
 * This class is registered in the media server to be notified on media connection
 * events. It also carries connection-specific application data (the original
 * INVITE request).
 * 
 * @author Vladimir Ralev
 *
 */
public class MediaConnectionListener implements MsConnectionListener{
	
	private SipServletRequest inviteRequest;
	
	public void connectionCreated(MsConnectionEvent event) {
		String sdp = event.getConnection().getLocalDescriptor();
		SipServletResponse sipServletResponse = inviteRequest.createResponse(SipServletResponse.SC_OK);
		try {
			sipServletResponse.setContent(sdp, "application/sdp");
			sipServletResponse.send();
		} catch (Exception e) {e.printStackTrace();}
		MsConnection connection = event.getConnection();
		String endpoint = connection.getEndpoint();
		MsSignalGenerator gen = connection.getSession().getProvider().getSignalGenerator(endpoint);
		gen.apply(Announcement.PLAY, new String[]{
				"http://www.geocities.com/v_ralev/RecordAfterTone.wav"
				//"file:///home/vralev/mobicents/mobicents-google/examples/call-controller2/src/org/mobicents/slee/examples/callcontrol/voicemail/audiofiles/RecordAfterTone.wav"
				});
		
	}

	public void connectionDeleted(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void connectionModifed(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void txFailed(MsConnectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public SipServletRequest getInviteRequest() {
		return inviteRequest;
	}

	public void setInviteRequest(SipServletRequest inviteRequest) {
		this.inviteRequest = inviteRequest;
	}

}
