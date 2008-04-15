/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;



public class TimerServiceImpl implements TimerService {
	
	private static Log logger = LogFactory.getLog(TimerServiceImpl.class
			.getName());
	
	private ExecutorServiceWrapper eService = ExecutorServiceWrapper.getInstance();
	
	private static final TimerServiceImpl instance = new TimerServiceImpl();
	
	// using Threadsafe static lazy initialization from joshua block
	public static TimerServiceImpl getInstance() {
		return instance;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerService#createTimer(javax.servlet.sip.SipApplicationSession, long, boolean, java.io.Serializable)
	 */
	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, boolean isPersistent, Serializable info) {			
		
		SipApplicationSessionImpl sipApplicationSessionImpl =(SipApplicationSessionImpl)appSession;
		
		if (sipApplicationSessionImpl.isValid() == false) {
			throw new IllegalStateException("Sip application session has been invalidated!!!");
		}
		
		if (!sipApplicationSessionImpl.hasTimerListener()) {
			throw new IllegalStateException("No Timer listeners have been configured for this application ");
		}
		TimerListener listener = sipApplicationSessionImpl.getSipContext().getListeners().getTimerListener();
		ServletTimerImpl servletTimer = createTimerLocaly(listener, delay, isPersistent, info, sipApplicationSessionImpl);				
		
		return servletTimer;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerService#createTimer(javax.servlet.sip.SipApplicationSession, long, long, boolean, boolean, java.io.Serializable)
	 */
	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, long period, boolean fixedDelay, boolean isPersistent,
			Serializable info) {
		if (period < 1) {
			throw new IllegalArgumentException(
					"Period should be greater than 0");
		}
		SipApplicationSessionImpl sipApplicationSessionImpl = (SipApplicationSessionImpl) appSession;
		
		if (sipApplicationSessionImpl.isValid() == false) {
			throw new IllegalStateException("Sip application session has been invalidated!!!");
		}
		
		if (!sipApplicationSessionImpl.hasTimerListener()) {
			throw new IllegalStateException("No Timer listeners have been configured for this application ");
		}
		TimerListener timerListener = sipApplicationSessionImpl.getSipContext().getListeners().getTimerListener();
		ServletTimerImpl servletTimer = createTimerLocaly(timerListener , delay, period, fixedDelay,isPersistent, info,sipApplicationSessionImpl);			
		
		return servletTimer;
	}
		
	/**
	 * 
	 * @param listener
	 * @param delay
	 * @param isPersistent
	 * @param info
	 * @param sipApplicationSession
	 * @return
	 */
	private ServletTimerImpl createTimerLocaly(TimerListener listener, long delay,
			boolean isPersistent, Serializable info, SipApplicationSessionImpl sipApplicationSession) {				
		ServletTimerImpl servletTimer = new ServletTimerImpl(info, delay, listener, sipApplicationSession);
		// logger.log(Level.FINE, "starting timer
		// at:"+System.currentTimeMillis());
		ScheduledFuture<?> future = eService.schedule(servletTimer, delay, TimeUnit.MILLISECONDS);
		servletTimer.setFuture(future);
//		sipApplicationSession.timerScheduled(st);
		sipApplicationSession.addServletTimer(servletTimer);
		if (isPersistent) {
			persist(servletTimer);
		} 
		return servletTimer;
	}
	/**
	 * 
	 * @param listener
	 * @param delay
	 * @param period
	 * @param fixedDelay
	 * @param isPersistent
	 * @param info
	 * @param sipApplicationSession
	 * @return
	 */
	private ServletTimerImpl createTimerLocaly(TimerListener listener, long delay,
			long period, boolean fixedDelay, boolean isPersistent,
			Serializable info, SipApplicationSessionImpl sipApplicationSession) {		
		final ServletTimerImpl servletTimer = new ServletTimerImpl(
				info, delay, fixedDelay, period, listener, sipApplicationSession);
		ScheduledFuture<?> future = null;
		if (fixedDelay) {
			future = eService.scheduleWithFixedDelay(servletTimer, delay, period,
					TimeUnit.MILLISECONDS);
		} else {
			future = eService.scheduleAtFixedRate(servletTimer, delay, period,
					TimeUnit.MILLISECONDS);
		}
		servletTimer.setFuture(future);
//		sipApplicationSession.timerScheduled(servletTimer);
		sipApplicationSession.addServletTimer(servletTimer);
		if (isPersistent) {			
			persist(servletTimer);
		} 
		return servletTimer;
	}

	/**
	 * 
	 * @param st
	 */
	private void persist(ServletTimerImpl st) {
		// TODO - implement persistance
		
	}
	
}
