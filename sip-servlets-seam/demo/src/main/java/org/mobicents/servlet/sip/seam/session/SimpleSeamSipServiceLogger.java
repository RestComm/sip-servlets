package org.mobicents.servlet.sip.seam.session;

import javax.servlet.sip.SipServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.log.Log;

@Scope(ScopeType.STATELESS)
@Name("loggerService")
public class SimpleSeamSipServiceLogger {
	@Logger Log log;
	@Observer("INVITE")
	@Asynchronous
	public void doInvite(SipServletRequest request) {
		log.info("Logging INVITE request ");
	}
}
