package org.mobicents.servlet.sip.core.session;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.TimerListener;

public class AgregatingListener implements TimerListener {

	
	SipServletApplicationImpl _app=null;
	
	
	
	public AgregatingListener(SipServletApplicationImpl _app) {
		super();
		this._app = _app;
	}



	public void timeout(ServletTimer timer) {
		
		if(timer.getInfo().equals(this._app.getEndObject()))
		{
			this._app.expirationTimerFired();
		}else
		{
			
			for(TimerListener l:this._app.getListeners().getTimerListeners())
			{
				l.timeout(timer);
			}
			
		}

	}

}
