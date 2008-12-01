package org.mobicents.anpbx.session;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.async.*;
import org.jboss.seam.annotations.bpm.*;
import org.jboss.seam.log.Log;


@Name("simpleSeamSipService")
@Scope(ScopeType.STATELESS)
@Transactional
public class SimpleSeamSipService {
	@Logger Log log;
	@Observer("INVITE")
	@CreateProcess(definition="demo")
	public void doInvite(SipServletRequest request)
	throws Exception {
		request.createResponse(180).send();
		Thread.sleep(100);
		request.createResponse(200).send();
	}

	@Observer("ACK")
	@StartTask(taskId="2") @EndTask()
	@Asynchronous
	public void doAck(SipServletRequest request)
	throws Exception {
	}

	@Observer({"REGISTER","BYE"})
	public void sayOK(SipServletRequest request)
	throws Exception {
		request.createResponse(200).send();
	}

	@Observer({"RESPONSE"})
	public void sayOK(SipServletResponse response)
	throws Exception {
	}
}