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
	public void doConnectionOpen(MsConnectionEvent event) throws IOException {
		conferenceEndpointName = event.getConnection().getEndpoint()
				.getLocalName();
		SipServletRequest request = (SipServletRequest) sipSession
				.getAttribute("inviteRequest");
		SipServletResponse response = request.createResponse(200);
		response.setContent(event.getConnection().getLocalDescriptor(),
				"application/sdp");
		response.send();
	}

	@Observer( { "BYE", "REGISTER" })
	public void sayOK(SipServletRequest request) throws Exception {
		request.createResponse(200).send();
	}

}