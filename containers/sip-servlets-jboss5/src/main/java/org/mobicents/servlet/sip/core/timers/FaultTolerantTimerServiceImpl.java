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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.TimerListener;

import org.apache.log4j.Logger;
import org.jboss.web.tomcat.service.session.ClusteredSipManager;
import org.jboss.web.tomcat.service.session.ClusteredSipServletTimerService;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.timers.FaultTolerantScheduler;
import org.mobicents.timers.PeriodicScheduleStrategy;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskFactory;

/**
 * Fault Tolerant Timer Service implementation allowing to shcedule ServletTimers that can be failed over
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class FaultTolerantTimerServiceImpl implements ClusteredSipServletTimerService {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FaultTolerantTimerServiceImpl.class
			.getName());
	public static final int SCHEDULER_THREAD_POOL_DEFAULT_SIZE = 10;
	public static final String NAME = "MSS_FT_Timers";
	
	private AtomicBoolean started = new AtomicBoolean(false);
	private FaultTolerantScheduler scheduledExecutor;
	private ClusteredSipManager<? extends OutgoingDistributableSessionData> sipManager;
	
	public FaultTolerantTimerServiceImpl(DistributableSipManager sipManager) {
		this.sipManager = (ClusteredSipManager<? extends OutgoingDistributableSessionData>)sipManager;			
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
		TimerServiceTask servletTimer = createTimerLocaly(delay, isPersistent, info, sipApplicationSessionImpl);				
		
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
		TimerServiceTask servletTimer = createTimerLocaly(delay, period, fixedDelay,isPersistent, info,sipApplicationSessionImpl);			
		
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
	private TimerServiceTask createTimerLocaly(long delay,
			boolean isPersistent, Serializable info, MobicentsSipApplicationSession sipApplicationSession) {
				
		
		final TimerListener listener = sipApplicationSession.getSipContext().getListeners().getTimerListener();
		final ServletTimerImpl servletTimer = new ServletTimerImpl(info, delay, listener, sipApplicationSession);
		final TimerServiceTaskData timerTaskData = new TimerServiceTaskData(servletTimer.getId(), System.currentTimeMillis() + delay, -1, null);		
		final TimerServiceTask timerServiceTask = new TimerServiceTask(sipManager, servletTimer, timerTaskData);
		
		if(logger.isDebugEnabled()) {
			logger.debug("Scheduling Timer "+ servletTimer.getId() +" to expire in " + delay + " ms");
		}
		
		getScheduler().schedule(timerServiceTask);				
		sipApplicationSession.addServletTimer(timerServiceTask);
		if (isPersistent) {
			persist(servletTimer);
		} 
		return timerServiceTask;
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
	private TimerServiceTask createTimerLocaly(long delay, 
			long period, boolean fixedDelay, boolean isPersistent,
			Serializable info, MobicentsSipApplicationSession sipApplicationSession) {
		final TimerListener listener = sipApplicationSession.getSipContext().getListeners().getTimerListener();
		final ServletTimerImpl servletTimer = new ServletTimerImpl(
				info, delay, fixedDelay, period, listener, sipApplicationSession);
		PeriodicScheduleStrategy periodicScheduleStrategy = PeriodicScheduleStrategy.withFixedDelay;
		if(!fixedDelay) {
			if(logger.isDebugEnabled()) {
				logger.debug("Scheduling Timer "+ servletTimer.getId() +" to expire in " + delay + " ms" + " at fixed rate of " + period);
			}
			periodicScheduleStrategy = PeriodicScheduleStrategy.atFixedRate;
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Scheduling Timer "+ servletTimer.getId() +" to expire in " + delay + " ms" + " with fixed delay of " + period);
			}
		}
		final TimerServiceTaskData timerTaskData = new TimerServiceTaskData(servletTimer.getId(), System.currentTimeMillis() + delay, period, periodicScheduleStrategy);		
		final TimerServiceTask timerServiceTask = new TimerServiceTask(sipManager, servletTimer, timerTaskData);
				
		getScheduler().schedule(timerServiceTask);			
		sipApplicationSession.addServletTimer(timerServiceTask);
		if (isPersistent) {			
			persist(servletTimer);
		} 
		return timerServiceTask;
	}

	/**
	 * 
	 * @param st
	 */
	private void persist(ServletTimerImpl st) {
		// TODO - implement persistance
		
	}
	
	public FaultTolerantScheduler getScheduler() {
		if(scheduledExecutor == null) {
			TimerTaskFactory timerTaskFactory = new TimerServiceTaskFactory(this.sipManager);
			scheduledExecutor = new FaultTolerantScheduler(NAME + ((SipContext)sipManager.getContainer()).getApplicationNameHashed(), SCHEDULER_THREAD_POOL_DEFAULT_SIZE, this.sipManager.getMobicentsCluster(), (byte) 1, this.sipManager.getMobicentsCluster().getMobicentsCache().getJBossCache().getConfiguration().getRuntimeConfig().getTransactionManager(), timerTaskFactory, ((SipContext)sipManager.getContainer()).getSipApplicationDispatcher().getSipService().getCanceledTimerTasksPurgePeriod());
		}
		return scheduledExecutor;
	}
	
	public void stop() {
		started.set(false);		
		if(scheduledExecutor != null) {
			if(logger.isInfoEnabled()) {
				logger.info("Sip Servlet Timer Tasks remaining in the FaultTolerantScheduler " + 
						scheduledExecutor.getLocalRunningTasks().size() + " for application " + 
						((SipContext)sipManager.getContainer()).getApplicationName());
			}
			scheduledExecutor.stop();		
		}		
		if(logger.isInfoEnabled()) {
			logger.info("Stopped Sip Servlets Timer Service for application " + ((SipContext)sipManager.getContainer()).getApplicationName());			
		}
	}
	
	public void start() {
		// we need to make sure the scheduler are created to be able to fail over fault tolerant timers
		// we can't create it before because the mobicents cluster is not yet initialized
		getScheduler();
		started.set(true);
		if(logger.isInfoEnabled()) {
			logger.info("Started Sip Servlets Timer Service for application " + ((SipContext)sipManager.getContainer()).getApplicationName());			
		}
	}

	public boolean isStarted() {
		return started.get();
	}

	public void cancel(String timerId) {
		if(logger.isDebugEnabled()) {
			logger.debug("Cancelling ServletTimer " + timerId);
		}
		getScheduler().cancel(timerId);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.web.tomcat.service.session.ClusteredSipServletTimerService#rescheduleTimerLocally(org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession, java.lang.String)
	 */
	public ServletTimer rescheduleTimerLocally(MobicentsSipApplicationSession sipApplicationSession, String timerId) {		
		TimerTask timerTask = getScheduler().getLocalRunningTask(timerId);
		if(timerTask == null) {
			TimerServiceTaskData timerTaskData = (TimerServiceTaskData) getScheduler().getTimerTaskData(timerId);
			
			if(timerTaskData != null) {
				// we recreate the task locally
				PeriodicScheduleStrategy periodicScheduleStrategy = timerTaskData.getPeriodicScheduleStrategy();
				if(logger.isDebugEnabled()) {
					logger.debug("Timer Task " + timerId + " is not present locally, but on another node, cancelling the remote one and rescheduling it locally with strategy " + periodicScheduleStrategy + ", delay " + timerTaskData.getDelay() + ", period " + timerTaskData.getPeriod());
				}
				// we cancel it, this will cause the remote owner node to remove it and cancel its local task
				cancel(timerId);				
				boolean fixedDelay = false;
				ServletTimerImpl servletTimerImpl = null;
				if(periodicScheduleStrategy != null) {
					if(periodicScheduleStrategy == PeriodicScheduleStrategy.withFixedDelay) {
						fixedDelay = true;
					}
					servletTimerImpl = new ServletTimerImpl(timerTaskData.getData(), timerTaskData.getDelay(), fixedDelay, timerTaskData.getPeriod(), sipApplicationSession.getSipContext().getListeners().getTimerListener(), sipApplicationSession);
				} else {
					servletTimerImpl = new ServletTimerImpl(timerTaskData.getData(), timerTaskData.getDelay(), sipApplicationSession.getSipContext().getListeners().getTimerListener(), sipApplicationSession);
				}
				TimerServiceTask timerServiceTask = new TimerServiceTask(sipManager, servletTimerImpl, timerTaskData);
				
				// and reset its start time to the correct one
				timerServiceTask.beforeRecover();				
				// and reschedule it locally
				getScheduler().schedule(timerServiceTask, false);
				return timerServiceTask;
			} else {
//				if(logger.isWarningEnabled()) {
					logger.warn("Timer Task " + timerId + " is not present locally, nor on another node, not possible to reschedule it.");
//				}
				return null;
				// and reschedule it locally
//				getScheduler().schedule(timerServiceTask);
			}
//			return timerServiceTask;
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("Timer Task " + timerId + " is already present locally no need to reschedule it.");
			}
			return null;
		}
	}
	
}
 