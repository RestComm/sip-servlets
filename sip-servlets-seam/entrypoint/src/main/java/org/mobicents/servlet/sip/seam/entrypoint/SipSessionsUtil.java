package org.mobicents.servlet.sip.seam.entrypoint;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.sip.SipServlet;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;

@Name("sipSessionsUtil")
@Scope(ScopeType.APPLICATION)
@Startup
public class SipSessionsUtil {
	@Unwrap
	public javax.servlet.sip.SipSessionsUtil getSessionsUtil() {
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		javax.servlet.sip.SipSessionsUtil sipSessionsUtil = (javax.servlet.sip.SipSessionsUtil) ctx.getAttribute(SipServlet.SIP_SESSIONS_UTIL);
		return sipSessionsUtil;
		
	}
	
}
