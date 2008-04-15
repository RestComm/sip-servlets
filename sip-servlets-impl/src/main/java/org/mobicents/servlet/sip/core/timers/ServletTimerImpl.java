package org.mobicents.servlet.sip.core.timers;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.TimerListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.session.SipServletApplicationImpl;

public class ServletTimerImpl implements ServletTimer, Runnable {

	private SipServletApplicationImpl _appSession;
	/**
	 * Logger for this class
	 */
	private static Log logger = LogFactory.getLog(ServletTimerImpl.class
			.getCanonicalName());
	/**
	 * A future dalayed scheduled action to be run
	 */
	private ScheduledFuture<?> _future;

	/**
	 * Information object passed upon creation
	 */
	private Serializable _info;

	/**
	 * Absolute time in milliseconds for next execution.
	 */
	private long _scheduledExecutionTime = 0;

	/**
	 * Delay between timers
	 */
	private long _delay = 0;

	/**
	 * Period between executions. Only applicable for repeating timers.
	 */
	private long _period = 0;

	/**
	 * Number of times execution has happened. Used to determine wheather we are
	 * late
	 */
	private long _numInvocations = 0;

	/**
	 * Absolute time for first execution. Used when firing event on fixed rate.
	 */
	private long _firstExecution = 0;

	/**
	 * Whether executions should be scheduled with fixed delay. Fixed delay ==
	 * true means than
	 * 
	 * @see java.util.Timer for semantics.
	 */
	private boolean _fixedDelay = false;

	/**
	 * Whether this timer is persistent.
	 */
	private boolean _persistent = true;

	/**
	 * Whether this timer has been successfully cancelled. Used for debugging.
	 */
	@SuppressWarnings("unused")
	private Boolean iscanceled = null;

	/**
	 * Registered listener that will get a timeout event when executed.
	 */
	private TimerListener _listener;

	/**
	 * Whether execution should be repeated.
	 */
	private boolean _isRepeatingTimer = true;

	/**
	 * Lock that prevents simultaneous execution. Note! Do NOT call methods in
	 * the associated sip session within synchronization of this lock since
	 * there may be a dead lock.
	 */
	private final Object _TIMER_LOCK = new Object();

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
	   public ServletTimerImpl(Serializable info, long delay, TimerListener listener,SipServletApplicationImpl appSession)
	   {
	      this(info, delay, false, 0, listener, appSession);
	      _isRepeatingTimer = false;
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
	         long period, TimerListener listener,SipServletApplicationImpl appSession)
	   {
	      _info = info;
	      _delay = delay;
	      _scheduledExecutionTime = delay + System.currentTimeMillis();
	      _fixedDelay = fixedDelay;
	      _period = period;
	      _listener = listener;
	      _appSession=appSession;
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
	   public void cancel(boolean mayInterruptIfRunning)
	   {
		   SipServletApplicationImpl appSessionToCancelThisTimersFrom = null;
	      synchronized (_TIMER_LOCK)
	      {
	         if (_future != null)
	         {
	            // need to force cancel to get rid of
	            // the task which is currently scheduled
	            boolean res = _future.cancel(mayInterruptIfRunning);
	            // used for debugging/optimizeIt purpose
	            // kan be kept in production code since object should
	            // be due for gc anyway....
	            iscanceled = new Boolean(res);
	            appSessionToCancelThisTimersFrom = _appSession;
	            _future = null;
	         }
	      }
	      if (appSessionToCancelThisTimersFrom != null)
	      {
	         appSessionToCancelThisTimersFrom.timerCanceled(this);
	      }
	   }



	   

	
	
	/**
	 * Getter for delay property.
	 * 
	 * @return
	 */
	public long getDelay() {
		return this._delay;
	}

	/**
	 * Getter for period property
	 * 
	 * @return
	 */
	public long getPeriod() {
		return this._period;
	}

	public SipApplicationSession getApplicationSession() {

		synchronized (_TIMER_LOCK) {
			return this._appSession;
		}

	}

	public Serializable getInfo() {

		return this._info;
	}

	public long scheduledExecutionTime() {
		synchronized (_TIMER_LOCK) {
			return this._scheduledExecutionTime;
		}

	}

	public void setFuture(ScheduledFuture<?> f) {
		synchronized (_TIMER_LOCK) {
			this._future = f;
		}

	}

	public boolean canRun()
	{
		return !this._future.isCancelled() && !this._future.isDone();
		
	}
	
	
	
	
	/*
	    * (non-Javadoc)
	    * 
	    * @see java.lang.Object#toString()
	    */
	   public String toString()
	   {
	      StringBuilder sb = new StringBuilder();
	      sb.append("Info = ").append(_info).append('\n');
	      sb.append("Scheduled execution time = ").append(_scheduledExecutionTime)
	            .append('\n');
	      sb.append("Time now = ").append(System.currentTimeMillis()).append('\n');
	      sb.append("SipApplicationSession = ").append(_appSession).append('\n');
	      sb.append("ScheduledFuture = ").append(_future).append('\n');
	      sb.append("Delay = ").append(_delay).append('\n');
	      return sb.toString();
	   }
	
	
	
	
	/**
	 * Method that actually
	 */
	public void run() {

		try {
			_listener.timeout(this);
		} finally {
			if (_isRepeatingTimer) {
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
		synchronized (_TIMER_LOCK) {
			if (_fixedDelay) {
				_scheduledExecutionTime = _period + System.currentTimeMillis();
			} else {
				if (_firstExecution == 0) {
					// save timestamp of first execution
					_firstExecution = _scheduledExecutionTime;
				}
				_scheduledExecutionTime = _firstExecution
						+ (++_numInvocations * _period);
			}
		}
	}

}
