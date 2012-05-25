/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.core.timers;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.TimerListener;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;

public class TimerServiceImpl implements SipServletTimerService {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TimerServiceImpl.class
			.getName());
	
	public static final int SCHEDULER_THREAD_POOL_DEFAULT_SIZE = 4;
	
	private transient ScheduledThreadPoolExecutor scheduledExecutor;
	
	public TimerServiceImpl(SipService sipService) {		
		scheduledExecutor = new ScheduledThreadPoolExecutor(SCHEDULER_THREAD_POOL_DEFAULT_SIZE);
		int purgePeriod = sipService.getCanceledTimerTasksPurgePeriod();
		if(purgePeriod > 0) {
			Runnable r = new Runnable() {			
				public void run() {
					try {
						if(logger.isDebugEnabled()) {
							logger.debug("Purging canceled timer tasks...");
						}
						scheduledExecutor.purge();
						if(logger.isDebugEnabled()) {
							logger.debug("Purging canceled timer tasks completed.");
						}						
					}
					catch (Exception e) {
						logger.error("failed to execute purge",e);
					}
				}
			};
			scheduledExecutor.scheduleWithFixedDelay(r, purgePeriod, purgePeriod, TimeUnit.MINUTES);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerService#createTimer(javax.servlet.sip.SipApplicationSession, long, boolean, java.io.Serializable)
	 */
	public ServletTimer createTimer(SipApplicationSession appSession,
			long delay, boolean isPersistent, Serializable info) {			
		
		MobicentsSipApplicationSession sipApplicationSessionImpl =(MobicentsSipApplicationSession)appSession;
		
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
		MobicentsSipApplicationSession sipApplicationSessionImpl = (MobicentsSipApplicationSession) appSession;
		
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
			boolean isPersistent, Serializable info, MobicentsSipApplicationSession sipApplicationSession) {				
		ServletTimerImpl servletTimer = new ServletTimerImpl(info, delay, listener, sipApplicationSession);
		
		if(logger.isDebugEnabled()) {
			logger.debug("Scheduling Timer "+ servletTimer.getId() +" to expire in " + delay + " ms");
		}
		
		ScheduledFuture<?> future = scheduledExecutor.schedule(servletTimer, delay, TimeUnit.MILLISECONDS);
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
			Serializable info, MobicentsSipApplicationSession sipApplicationSession) {		
		final ServletTimerImpl servletTimer = new ServletTimerImpl(
				info, delay, fixedDelay, period, listener, sipApplicationSession);
		ScheduledFuture<?> future = null;
		if (fixedDelay) {
			if(logger.isDebugEnabled()) {
				logger.debug("Scheduling Timer "+ servletTimer.getId() +" to expire in " + delay + " ms" + " with fixed delay of " + period);
			}
			
			future = scheduledExecutor.scheduleWithFixedDelay(servletTimer, delay, period,
					TimeUnit.MILLISECONDS);
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Scheduling Timer "+ servletTimer.getId() +" to expire in " + delay + " ms" + " at fixed rate of " + period);
			}
			
			future = scheduledExecutor.scheduleAtFixedRate(servletTimer, delay, period,
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
	
	public void stop() {
		scheduledExecutor.shutdownNow();
		if(logger.isInfoEnabled()) {
			logger.info("Stopped timer service "+ this);
		}
	}

	public void start() {
		scheduledExecutor.prestartAllCoreThreads();		
	}

	public boolean isStarted() {		
		return scheduledExecutor.isTerminated();
	}
	
}