package org.mobicents.servlet.sip.seam.entrypoint;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.sip.SipServlet;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;

@Name("sipFactory")
@Scope(ScopeType.APPLICATION)
@Startup
public class SipFactory {
	@Unwrap
	public javax.servlet.sip.SipFactory getFactroy() {
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		javax.servlet.sip.SipFactory factory = (javax.servlet.sip.SipFactory) ctx.getAttribute(SipServlet.SIP_FACTORY);
		return factory;
		
	}
	
}
