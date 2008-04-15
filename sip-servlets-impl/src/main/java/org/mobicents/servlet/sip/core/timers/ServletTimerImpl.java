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
import java.rmi.server.UID;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.TimerListener;

import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;

public class ServletTimerImpl implements ServletTimer, Runnable {

	private SipApplicationSessionImpl appSession;
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
	private boolean persistent = true;

	/**
	 * Whether this timer has been successfully cancelled. Used for debugging.
	 */
	@SuppressWarnings("unused")
	private Boolean isCanceled = null;

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
			TimerListener listener, SipApplicationSessionImpl appSession) {
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
			SipApplicationSessionImpl appSession) {
		this.id = new UID().toString();
		this.info = info;
		this.delay = delay;
		this.scheduledExecutionTime = delay + System.currentTimeMillis();
		this.fixedDelay = fixedDelay;
		this.period = period;
		this.listener = listener;
		this.appSession = appSession;
	}

	public void cancel() {
		cancel(false);
	}

	/**
	 * 
	 * Cancel this timer, possibly by also interrupting the thread (from the
	 * thread pool) running the task. Note that interupting the thread may have
	 * undesired consequences.
	 * 
	 * @param mayInterruptIfRunning
	 */
	public void cancel(boolean mayInterruptIfRunning) {
		SipApplicationSessionImpl appSessionToCancelThisTimersFrom = null;
		synchronized (TIMER_LOCK) {
			if (future != null) {
				// need to force cancel to get rid of
				// the task which is currently scheduled
				boolean res = future.cancel(mayInterruptIfRunning);
				// used for debugging/optimizeIt purpose
				// kan be kept in production code since object should
				// be due for gc anyway....
				isCanceled = new Boolean(res);
				appSessionToCancelThisTimersFrom = appSession;
				future = null;
			}
		}
		if (appSessionToCancelThisTimersFrom != null) {
			appSessionToCancelThisTimersFrom.removeServletTimer(this);
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

	public SipApplicationSession getApplicationSession() {

		synchronized (TIMER_LOCK) {
			return this.appSession;
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
		sb.append("SipApplicationSession = ").append(appSession).append('\n');
		sb.append("ScheduledFuture = ").append(future).append('\n');
		sb.append("Delay = ").append(delay).append('\n');
		return sb.toString();
	}

	/**
	 * Method that actually
	 */
	public void run() {

		try {
			listener.timeout(this);
		} finally {
			if (isRepeatingTimer) {
				estimateNextExecution();
			} else {
				// this non-repeating timer is now "ready"
				// and should not be included in the list of active timers
				// The application may already have canceled() the timer though
				cancel(); // dont bother about return value....
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
				scheduledExecutionTime = firstExecution
						+ (++numInvocations * period);
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

}
