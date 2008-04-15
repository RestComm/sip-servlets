package org.mobicents.servlet.sip.core.timers;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.session.SipServletApplicationImpl;



public class TimerServiceImpl implements TimerService {
	
	private static Log logger = LogFactory.getLog(TimerServiceImpl.class
			.getName());
	
	private ExecutorServiceWrapper eService=ExecutorServiceWrapper.getInstance();
	
	private static TimerServiceImpl instance = new TimerServiceImpl();

	public static TimerServiceImpl getInstance() {
		return instance;
	}
	
	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, boolean isPersistent, Serializable info) {
			
		return this.createTimer(appSession, delay, isPersistent, info, false);
	}

	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, long period, boolean fixedDelay, boolean isPersistent,
			Serializable info) {
		// TODO Auto-generated method stub
		return this.createTimer(appSession, delay, period, fixedDelay, isPersistent, info,false);
	}
	
	

	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, long period, boolean fixedDelay, boolean isPersistent,
			Serializable info, boolean isExpirationTimer) {
		if (period < 1) {
			throw new IllegalArgumentException(
					"Period should be greater than 0");
		}
		SipServletApplicationImpl ap = (SipServletApplicationImpl) appSession;
		
		if (ap.isValid() == false) {
			throw new IllegalStateException("Sip application session has been invalidated!!!");
		}
		
		if (!isExpirationTimer && !ap.hasTimerListeners()) {
			throw new IllegalStateException("No Timer listeners have been configured for this application ");
		}
		TimerListener l=ap.getAgregatingListener();
		ServletTimerImpl st = createTimerLocaly(l, delay, period, fixedDelay,isPersistent, info,ap);
		
		// as.addServletTimer(st);
		return st;
	}

	
	

	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, boolean isPersistent, Serializable info, boolean isExpirationTimer) {
		
		SipServletApplicationImpl ap=(SipServletApplicationImpl)appSession;
		
		if (ap.isValid() == false) {
			throw new IllegalStateException("Sip application session has been invalidated!!!");
		}
		
		if (!isExpirationTimer && !ap.hasTimerListeners()) {
			throw new IllegalStateException("No Timer listeners have been configured for this application ");
		}
		TimerListener l=ap.getAgregatingListener();
		ServletTimerImpl st = createTimerLocaly(l, delay, isPersistent, info,ap);
		
		
		return st;
	
	}
	
	
	
	private ServletTimerImpl createTimerLocaly(TimerListener listener, long delay,
			boolean isPersistent, Serializable info, SipServletApplicationImpl ap) {		
		final TimerListener l = listener;
		final ServletTimerImpl st = new ServletTimerImpl(info, delay, l,ap);
		// logger.log(Level.FINE, "starting timer
		// at:"+System.currentTimeMillis());
		ScheduledFuture<?> f = eService.schedule(st, delay,	TimeUnit.MILLISECONDS);
		st.setFuture(f);
		ap.timerScheduled(st);
		if (isPersistent) {
	
			persist(st);
		} 
		return st;
	}
	
	private ServletTimerImpl createTimerLocaly(TimerListener listener, long delay,
			long period, boolean fixedDelay, boolean isPersistent,
			Serializable info, SipServletApplicationImpl ap) {
		final TimerListener l = listener;
		final ServletTimerImpl st = new ServletTimerImpl(info, delay, fixedDelay, period, l,ap);
		// logger.log(Level.FINE, "starting timer
		// at:"+System.currentTimeMillis());
		ScheduledFuture<?> f = null;
		if (fixedDelay) {
			f = eService.scheduleWithFixedDelay(st, delay, period,
					TimeUnit.MILLISECONDS);
		} else {
			f = eService.scheduleAtFixedRate(st, delay, period,
					TimeUnit.MILLISECONDS);
		}
		st.setFuture(f);
		ap.timerScheduled(st);
		if (isPersistent) {
			
			persist(st);
		} 
		return st;
	}

	private void persist(ServletTimerImpl st) {
		// TODO - implement persistance
		
	}
	
}
