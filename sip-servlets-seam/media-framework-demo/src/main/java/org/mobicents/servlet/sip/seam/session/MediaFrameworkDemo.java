package org.mobicents.servlet.sip.seam.session;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.bpm.CreateProcess;
import org.jboss.seam.log.Log;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;
import org.mobicents.servlet.sip.seam.media.framework.IVRHelper;
import org.mobicents.servlet.sip.seam.media.framework.MediaEventDispatcher;
import org.mobicents.servlet.sip.seam.media.framework.MediaSessionStore;


@Name("mediaFrameworkDemo")
@Scope(ScopeType.STATELESS)
@Transactional
public class MediaFrameworkDemo {
	@Logger Log log;
	@In MediaController mediaController;
	@In SipSession sipSession;
	@In MediaSessionStore mediaSessionStore;
	@In IVRHelper ivrHelper;
	@In MediaEventDispatcher mediaEventDispatcher;
	
	@In(scope=ScopeType.APPLICATION, required=false)
	@Out(scope=ScopeType.APPLICATION, required=false)
	
	String conferenceEndpointName;
	
	private final String announcement = 
		"http://mobicents.googlecode.com/svn/branches/servers/media/1.x.y/examples/mms-demo/web/src/main/webapp/audio/welcome.wav";
	
	@Observer("INVITE")
	public void doInvite(SipServletRequest request) throws Exception {
		// Extract SDP from the SIp message
		String sdp = new String((byte[]) request.getContent());
		
		// Tell the other side to ring (status 180)
		request.createResponse(SipServletResponse.SC_RINGING).send();
		
		// Store the INVITE request in the sip session
		sipSession.setAttribute("inviteRequest", request);
		
		// If this is the first INVITE in the app, then we must start a new conference
		if (conferenceEndpointName == null)
			conferenceEndpointName = "media/trunk/Conference/$";
		
		// Create a connection between the UA and the conference endpoint
		mediaController.createConnection(conferenceEndpointName).modify("$",
				sdp); // also updates the SDP in Media Server to match capabilities of UA
	}

	@Observer("storeConnectionOpen")
	public void doConnectionOpen(MsConnectionEvent event) throws IOException {
		// Save this connection where the framework can read it
		// mediaSessionStore.setMsConnection(event.getConnection());// This is done automatically in STF 2.0
		
		// The conference endpoint is now assiged after we are connected, so save it too
		conferenceEndpointName = event.getConnection().getEndpoint()
				.getLocalName();
		
		// Recall the INVITE request that we saved in doInvite
		SipServletRequest request = (SipServletRequest) sipSession
				.getAttribute("inviteRequest");
		
		// Make OK (status 200) to tell the other side that the call is established
		SipServletResponse response = request.createResponse(SipServletResponse.SC_OK);
		
		// Put the SDP inside the OK message to tell what codecs and so on we agree with
		response.setContent(event.getConnection().getLocalDescriptor(),
				"application/sdp");
		
		// Now actually send the message
		response.send();
		
		// And start listening for DTMF signals
		ivrHelper.detectDtmf();
	}
	
	@Observer("DTMF")
	public void dtmf(String button) {
		// If the other side presses the button "0" stop the playback
		if("0".equals(button)) {
			ivrHelper.endAll();
		} else {
			// otherwise play announcement
			ivrHelper.playAnnouncementWithDtmf(announcement);
		}
		// Also log the DTMF buttons pressed so far in this session
		log.info("Current DTMF Stack for the SIP Session: "
				+ mediaEventDispatcher.getDtmfArchive(sipSession));
	}

	// Just say OK to these messages.
	@Observer( { "BYE", "REGISTER" })
	public void sayOK(SipServletRequest request) throws Exception {
		request.createResponse(200).send();
		
		// And clean up the connections
		MsConnection connection = mediaSessionStore.getMsConnection();
		connection.release();
	}

}