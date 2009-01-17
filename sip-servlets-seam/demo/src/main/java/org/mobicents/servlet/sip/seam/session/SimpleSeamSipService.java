package org.mobicents.servlet.sip.seam.session;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.async.*;
import org.jboss.seam.annotations.bpm.*;
import org.jboss.seam.log.Log;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;


@Name("simpleSeamSipService")
@Scope(ScopeType.STATELESS)
@Transactional
public class SimpleSeamSipService {
	@Logger Log log;
	@In MediaController mediaController;
	@In SipSession sipSession;
	
	@In(scope=ScopeType.APPLICATION, required=false)
	@Out(scope=ScopeType.APPLICATION, required=false)
	String conferenceEndpointName;
	
	@Observer("INVITE")
	@CreateProcess(definition = "demo")
	public void doInvite(SipServletRequest request) throws Exception {
		String sdp = new String((byte[]) request.getContent());
		request.createResponse(180).send();
		sipSession.setAttribute("inviteRequest", request);
		if (conferenceEndpointName == null)
			conferenceEndpointName = "media/trunk/Conference/$";
		mediaController.createConnection(conferenceEndpointName).modify("$",
				sdp);
	}

	@Observer("connectionOpen")
	public void doConnectionOpen(MsConnectionEvent event) {
		conferenceEndpointName = event.getConnection().getEndpoint()
				.getLocalName();
		SipServletRequest request = (SipServletRequest) sipSession
				.getAttribute("inviteRequest");
		SipServletResponse response = request.createResponse(200);
		try {
			response.setContent(event.getConnection().getLocalDescriptor(),
					"application/sdp");
			response.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Observer( { "BYE", "REGISTER" })
	public void sayOK(SipServletRequest request) throws Exception {
		log.info("BYE-received");
		request.createResponse(200).send();
	}

}