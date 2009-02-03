package org.mobicents.servlet.sip.seam.entrypoint;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.sip.SipServlet;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;

@Name("timerService")
@Scope(ScopeType.APPLICATION)
@Startup
public class TimerService {
	@Unwrap
	public javax.servlet.sip.TimerService getTimerService() {
		ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		javax.servlet.sip.TimerService timerService = (javax.servlet.sip.TimerService) ctx.getAttribute(SipServlet.TIMER_SERVICE);
		return timerService;
		
	}
	
}
