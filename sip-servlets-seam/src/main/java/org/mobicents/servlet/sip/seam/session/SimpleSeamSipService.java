package org.mobicents.servlet.sip.seam.session;

import javax.servlet.sip.SipServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;


@Name("simpleSeamSipService")
@Scope(ScopeType.STATELESS)
@Transactional
public class SimpleSeamSipService {
	@Logger Log log;
	@In SessionMessageCounter sessionMessageCounter;
	
	public void inc() {
		sessionMessageCounter.increment();
		log.info("Processed SIP messages " + sessionMessageCounter.getMessages());
	}
	
	@Observer("INVITE")
	public void doInvite(SipServletRequest request) throws Exception {
		inc();
		request.createResponse(180).send();
		Thread.sleep(100);
		request.createResponse(200).send();
	}
	
	@Observer("ACK")
	public void doAck(SipServletRequest request) throws Exception {
		inc();
	}
	
	@Observer({"REGISTER","BYE"})
	public void doRegister(SipServletRequest request) throws Exception {
		inc();
		request.createResponse(200).send();
	}
}
