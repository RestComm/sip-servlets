/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.sip.TimerListener;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;

public class ServletTimerImpl implements MobicentsServletTimer, Runnable {
	private static final Logger logger = Logger.getLogger(ServletTimerImpl.class);
	
	private MobicentsSipApplicationSessionKey appSessionKey;
	private SipManager sipManager;
	/**
	 * Logger for this class
	 */
//	private static Log logger = LogFactory.getLog(ServletTimerImpl.class
//			.getCanonicalName());
	/**
	 * A future dalayed scheduled action to be run
	 */
	private ScheduledFuture<?> future;

	/**
	 * Information object passed upon creation
	 */
	private Serializable info;

	/**
	 * Absolute time in milliseconds for next execution.
	 */
	private long scheduledExecutionTime = 0;

	/**
	 * Delay between timers
	 */
	private long delay = 0;

	/**
	 * Period between executions. Only applicable for repeating timers.
	 */
	private long period = 0;

	/**
	 * Number of times execution has happened. Used to determine wheather we are
	 * late
	 */
	private long numInvocations = 0;

	/**
	 * Absolute time for first execution. Used when firing event on fixed rate.
	 */
	private long firstExecution = 0;

	/**
	 * Whether executions should be scheduled with fixed delay. Fixed delay ==
	 * true means than
	 * 
	 * @see java.util.Timer for semantics.
	 */
	private boolean fixedDelay = false;

	/**
	 * Whether this timer is persistent.
	 */
	private boolean persistent = false;

	/**
	 * Whether this timer has been successfully cancelled. Used for debugging.
	 */
	private boolean isCanceled = false;

	/**
	 * Timer unique id
	 */
	String id = null;
	/**
	 * Registered listener that will get a timeout event when executed.
	 */
	private TimerListener listener;

	/**
	 * Whether execution should be repeated.
	 */
	private boolean isRepeatingTimer = true;

	/**
	 * Lock that prevents simultaneous execution. Note! Do NOT call methods in
	 * the associated sip session within synchronization of this lock since
	 * there may be a dead lock.
	 */
	private final Object TIMER_LOCK = new Object();

	/**
	 * Constructor for non-repeating timer.
	 * 
	 * @param info
	 *            Information about the timer
	 * @param delay
	 *            Delay until execution
	 * @param listener
	 *            Listener that will get timeout events.
	 */
	public ServletTimerImpl(Serializable info, long delay,
			TimerListener listener, MobicentsSipApplicationSession appSession) {
		this(info, delay, false, 0, listener, appSession);
		isRepeatingTimer = false;		
	}

	/**
	 * Constructor for repeating times
	 * 
	 * @param info
	 *            Information about the timer
	 * @param delay
	 *            Delay until first execution
	 * @param fixedDelay
	 *            Whether fixed delay mode should be used
	 * @param period
	 *            Period between execution
	 * @param listener
	 *            Listener that will get timeout events.
	 */
	public ServletTimerImpl(Serializable info, long delay, boolean fixedDelay,
			long period, TimerListener listener,
			MobicentsSipApplicationSession appSession) {
		this.id = UUID.randomUUID().toString();
		this.info = info;
		this.delay = delay;
		this.scheduledExecutionTime = delay + System.currentTimeMillis();
		this.fixedDelay = fixedDelay;
		this.period = period;
		this.listener = listener;
		this.appSessionKey = appSession.getKey();
		this.sipManager= appSession.getSipContext().getSipManager();
	}

	public void cancel() {
		cancel(false, true);
	}

	/**
	 * 
	 * Cancel this timer, possibly by also interrupting the thread (from the
	 * thread pool) running the task. Note that interupting the thread may have
	 * undesired consequences.
	 * 
	 * @param mayInterruptIfRunning 
	 * @param updateAppSessionReadyToInvalidateState boolean to update or not the readyToInvalidateState upon removing a servlet Timer,
	 * this is useful when we process a remoteInvalidation to make sure we don't load and update the sip session from the cache
	 */
	public void cancel(boolean mayInterruptIfRunning, boolean updateAppSessionReadyToInvalidateState) {
		MobicentsSipApplicationSession appSessionToCancelThisTimersFrom = null;
		synchronized (TIMER_LOCK) {
			if (future != null) {
				// need to force cancel to get rid of
				// the task which is currently scheduled
				future.cancel(mayInterruptIfRunning);
				isCanceled = true;
				// used for debugging/optimizeIt purpose
				// kan be kept in production code since object should
				// be due for gc anyway....
				appSessionToCancelThisTimersFrom = getApplicationSession();
				future = null;
				// Issue 2367 removing the reference to the Serializable info so that it doesn't linger into memory
				info = null;
			}
		}
		if (appSessionToCancelThisTimersFrom != null && updateAppSessionReadyToInvalidateState) {
			if(logger.isDebugEnabled()) {
				logger.debug("removing servlet timer " + id + " from sip application session " + appSessionToCancelThisTimersFrom + " and updating its ready to invalidate state " + updateAppSessionReadyToInvalidateState);
			}
			appSessionToCancelThisTimersFrom.removeServletTimer(this, updateAppSessionReadyToInvalidateState);			
		}
	}

	/**
	 * Getter for delay property.
	 * 
	 * @return
	 */
	public long getDelay() {
		return this.delay;
	}

	/**
	 * Getter for period property
	 * 
	 * @return
	 */
	public long getPeriod() {
		return this.period;
	}

	public MobicentsSipApplicationSession getApplicationSession() {

		synchronized (TIMER_LOCK) {
			return sipManager.getSipApplicationSession(appSessionKey, false);
		}

	}
	
	public void setApplicationSession(MobicentsSipApplicationSession sipApplicationSession) {
		if(sipApplicationSession != null) {
			synchronized (TIMER_LOCK) {
				this.appSessionKey = sipApplicationSession.getKey();
			}
		}

	}

	public Serializable getInfo() {

		return this.info;
	}

	public long scheduledExecutionTime() {
		synchronized (TIMER_LOCK) {
			return this.scheduledExecutionTime;
		}

	}

	public void setFuture(ScheduledFuture<?> f) {
		synchronized (TIMER_LOCK) {
			this.future = f;
		}

	}

	public boolean canRun() {
		return !this.future.isCancelled() && !this.future.isDone();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Info = ").append(info).append('\n');
		sb.append("Scheduled execution time = ").append(scheduledExecutionTime)
				.append('\n');
		sb.append("Time now = ").append(System.currentTimeMillis())
				.append('\n');
		sb.append("SipApplicationSession = ").append(appSessionKey).append('\n');
		sb.append("ScheduledFuture = ").append(future).append('\n');
		sb.append("Delay = ").append(delay).append('\n');
		return sb.toString();
	}

	/**
	 * Method that actually
	 */
	public void run() {

		final MobicentsSipApplicationSession sipApplicationSession = getApplicationSession();
		SipContext sipContext = sipApplicationSession.getSipContext();
		
		if(logger.isDebugEnabled()) {
			logger.debug("running Servlet Timer " + id + " for sip application session " + sipApplicationSession);
		}
		
		boolean batchStarted = false;
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader cl = sipContext.getSipContextClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			// http://code.google.com/p/sipservlets/issues/detail?id=135
			sipContext.bindThreadBindingListener();
			sipContext.enterSipApp(sipApplicationSession, null, false);
			batchStarted = sipContext.enterSipAppHa(true);
			listener.timeout(this);
		} catch(Throwable t) {
			logger.error("An unexpected exception happened in the timer callback!",t);
		} finally {		
			try {
				// http://code.google.com/p/sipservlets/issues/detail?id=135
				sipContext.unbindThreadBindingListener();
				Thread.currentThread().setContextClassLoader(oldClassLoader);
				if (isRepeatingTimer) {
					estimateNextExecution();
				} else {
					// this non-repeating timer is now "ready"
					// and should not be included in the list of active timers
					// The application may already have canceled() the timer though
					cancel(); // dont bother about return value....
				}
				if(logger.isDebugEnabled()) {
					logger.debug("Servlet Timer " + id + " for sip application session " + sipApplicationSession + " ended");
				}
			} finally {
				sipContext.exitSipAppHa(null, null, batchStarted);
				sipContext.exitSipApp(sipApplicationSession, null);
			}
		}

	}

	/**
	 * Helper to calculate when next execution time is.
	 * 
	 */
	private void estimateNextExecution() {
		synchronized (TIMER_LOCK) {
			if (fixedDelay) {
				scheduledExecutionTime = period + System.currentTimeMillis();
			} else {
				if (firstExecution == 0) {
					// save timestamp of first execution
					firstExecution = scheduledExecutionTime;
				}
				long now = System.currentTimeMillis();
				long executedTime = (numInvocations++ * period);
				scheduledExecutionTime = firstExecution + executedTime;
				if(logger.isDebugEnabled()) {
					logger.debug("next execution estimated to run at " + scheduledExecutionTime);
				}
				if(logger.isDebugEnabled()) {
					logger.debug("current time is " + now);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ServletTimer#getId()
	 */
	public String getId() {		
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTimeRemaining() {
		synchronized (TIMER_LOCK) {
			return scheduledExecutionTime - System.currentTimeMillis();
		}
	}

	/**
	 * @return the isCanceled
	 */
	public boolean isCanceled() {
		return isCanceled;
	}

}
