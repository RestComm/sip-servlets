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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class DefaultSipApplicationSessionTimerService extends
		ScheduledThreadPoolExecutor implements
		SipApplicationSessionTimerService {

	private static final Logger logger = Logger.getLogger(DefaultSipApplicationSessionTimerService.class
			.getName());
	
	// Counts the number of cancelled tasks
    private static volatile int numCancelled = 0;
	/**
	 * @param corePoolSize
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize) {
		super(corePoolSize);
		schedulePurgeTaskIfNeeded();
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize,
			ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
		schedulePurgeTaskIfNeeded();
	}

	/**
	 * @param corePoolSize
	 * @param handler
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize,
			RejectedExecutionHandler handler) {
		super(corePoolSize, handler);
		schedulePurgeTaskIfNeeded();
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 * @param handler
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize,
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
		schedulePurgeTaskIfNeeded();
	}
	
	private void schedulePurgeTaskIfNeeded() {
		int purgePeriod = StaticServiceHolder.sipStandardService.getCanceledTimerTasksPurgePeriod();
		if(purgePeriod > 0) {
			Runnable r = new Runnable() {			
				public void run() {
					try {
						if(logger.isDebugEnabled()) {
							logger.debug("Purging canceled timer tasks...");
						}
						purge();
						if(logger.isDebugEnabled()) {
							logger.debug("Purging canceled timer tasks completed.");
						}						
					}
					catch (Exception e) {
						logger.error("failed to execute purge",e);
					}
				}
			};
			scheduleWithFixedDelay(r, purgePeriod, purgePeriod, TimeUnit.MINUTES);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#createSipApplicationSessionTimerTask()
	 */
	public SipApplicationSessionTimerTask createSipApplicationSessionTimerTask(MobicentsSipApplicationSession sipApplicationSession) {		
		return new DefaultSasTimerTask(sipApplicationSession);
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#remove(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask)
	 */
	public boolean cancel(SipApplicationSessionTimerTask expirationTimerTask) {
		//CANCEL needs to remove the shceduled timer see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6602600
		//to improve perf		
		ScheduledFuture<MobicentsSipApplicationSession> future = ((DefaultSasTimerTask)expirationTimerTask).getScheduledFuture();
		if(future != null) {
			boolean removed = super.remove((Runnable) future);
			if(logger.isDebugEnabled()) {
				logger.debug("expiration timer on sip application session " + expirationTimerTask.getSipApplicationSession().getKey() + " removed : " + removed);
			}					
			boolean cancelled = future.cancel(true);
			if(logger.isDebugEnabled()) {
				logger.debug("expiration timer on sip application session " + expirationTimerTask.getSipApplicationSession().getKey() + " Cancelled : " + cancelled);
			}
			future = null;
			// Purge is expensive when called frequently, only call it every now and then.
	        // We do not sync the numCancelled variable. We dont care about correctness of
	        // the number, and we will still call purge rought once on every 25 cancels.
	        numCancelled++;
	        if(numCancelled % 100 == 0) {
	            super.purge();
	        }	
	        return cancelled;
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("expiration timer future is null, thus cannot be Cancelled");
			}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#schedule(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask, long, java.util.concurrent.TimeUnit)
	 */
	public SipApplicationSessionTimerTask schedule(
			SipApplicationSessionTimerTask expirationTimerTask, long delay,
			TimeUnit unit) {
		if(logger.isDebugEnabled()) {
			logger.debug("Scheduling sip application session "+ expirationTimerTask.getSipApplicationSession().getKey() +" to expire in " + (delay / (double) 1000 / (double) 60) + " minutes");
		}
		((DefaultSasTimerTask)expirationTimerTask).setScheduledFuture((ScheduledFuture<MobicentsSipApplicationSession>)super.schedule(expirationTimerTask, delay, unit));
		return expirationTimerTask;
	}

	public void start() {
		prestartAllCoreThreads();
		if(logger.isInfoEnabled()) {
			logger.info("Started timer service "+ this);
		}
	}
	
	public void stop() {		
		super.shutdownNow();
		if(logger.isInfoEnabled()) {
			logger.info("Stopped timer service "+ this);
		}
	}

	public boolean isStarted() {
		return super.isTerminated();
	}

}