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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.jboss.web.tomcat.service.session.ClusteredSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.timers.FaultTolerantScheduler;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskFactory;

/**
 * Fault Tolerant implementation of SipApplicationSessionTimerService allowing to 
 * shedule Sip application session Timers that can be failed over
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class FaultTolerantSasTimerService implements ClusteredSipApplicationSessionTimerService {

	private static final Logger logger = Logger.getLogger(FaultTolerantSasTimerService.class
			.getName());
	public static final String NAME = "MSS_FT_SAS_Timers";
	
	private FaultTolerantScheduler scheduledExecutor;
	private ClusteredSipManager<? extends OutgoingDistributableSessionData> sipManager;
	private int corePoolSize;
	private AtomicBoolean started = new AtomicBoolean(false);
	
	public FaultTolerantSasTimerService(DistributableSipManager sipManager, int corePoolSize) {
		this.corePoolSize = corePoolSize;
		this.sipManager = (ClusteredSipManager<? extends OutgoingDistributableSessionData>)sipManager;	
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#createSipApplicationSessionTimerTask(org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession)
	 */
	public SipApplicationSessionTimerTask createSipApplicationSessionTimerTask(
			MobicentsSipApplicationSession sipApplicationSession) {
		return new FaultTolerantSasTimerTask(sipManager, sipApplicationSession);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerService#schedule(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask, long, java.util.concurrent.TimeUnit)
	 */
	public SipApplicationSessionTimerTask schedule(SipApplicationSessionTimerTask expirationTimerTask, 
            long delay, 
            TimeUnit unit) {			
		if(expirationTimerTask instanceof FaultTolerantSasTimerTask) {
			if(getScheduler().getTimerTaskData(expirationTimerTask.getSipApplicationSession().getId()) == null) {
				FaultTolerantSasTimerTask faultTolerantSasTimerTask = (FaultTolerantSasTimerTask)expirationTimerTask;
				faultTolerantSasTimerTask.getData().setStartTime(System.currentTimeMillis() + delay);
				if(logger.isDebugEnabled()) {
					logger.debug("Scheduling sip application session "+ expirationTimerTask.getSipApplicationSession().getKey() +" to expire in " + (delay / (double) 1000 / (double) 60) + " minutes");
				}
				getScheduler().schedule(faultTolerantSasTimerTask);
				return faultTolerantSasTimerTask;
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("sip application session expiration timer "+ expirationTimerTask.getSipApplicationSession().getKey() +" is already present in the cache not scheduling it again");
				}
				return null;
			}
		} 				
		throw new IllegalArgumentException("the task to schedule is not an instance of FaultTolerantSasTimerTask");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerService#cancel(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask)
	 */
	public boolean cancel(SipApplicationSessionTimerTask expirationTimerTask) {
		if(expirationTimerTask instanceof FaultTolerantSasTimerTask) {
			TimerTask cancelledTask = getScheduler().cancel(((FaultTolerantSasTimerTask)expirationTimerTask).getData().getTaskID());
			if(cancelledTask == null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Task "+ expirationTimerTask.getSipApplicationSession().getKey() +" couldn't be cancelled because it was not found locally");
				}
				return false;
			}
			return true;
			
		} 				
		throw new IllegalArgumentException("the task to remove is not an instance of FaultTolerantSasTimerTask");
	}
	
	public void purge() {
//		super.purge();
		// method not exposed by Mobicents FaultTolerantScheduler
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerService#stop()
	 */
	public void stop() {
//		return super.shutdownNow();
		// method not exposed by Mobicents FaultTolerantScheduler
		if(logger.isInfoEnabled()) {
			logger.info("Sip Application Session Expiration Timer Tasks remaining in the FaultTolerantScheduler " + 
					scheduledExecutor.getLocalRunningTasks().size() + " for application " + 
					((SipContext)sipManager.getContainer()).getApplicationName());
		}
		started.set(false);
		if(scheduledExecutor != null) {
			scheduledExecutor.shutdownNow();
		}		
		if(logger.isInfoEnabled()) {
			logger.info("Stopped Sip Application Session Expiration Timer Service for application " + ((SipContext)sipManager.getContainer()).getApplicationName());
		}
	}
	
	private FaultTolerantScheduler getScheduler() {
		if(scheduledExecutor == null) {
			TimerTaskFactory timerTaskFactory = new SipApplicationSessionTaskFactory(this.sipManager);
			scheduledExecutor = new FaultTolerantScheduler(NAME + ((SipContext)sipManager.getContainer()).getApplicationNameHashed(), corePoolSize, this.sipManager.getMobicentsCluster(), (byte) 0, this.sipManager.getMobicentsCluster().getMobicentsCache().getJBossCache().getConfiguration().getRuntimeConfig().getTransactionManager(), timerTaskFactory, ((SipContext)sipManager.getContainer()).getSipApplicationDispatcher().getSipService().getCanceledTimerTasksPurgePeriod());
		}
		return scheduledExecutor;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#init()
	 */
	public void start() {		
		// we need to start the scheduler upon init so that the local listener gets registered and can failover timers
		getScheduler();
		started.set(true);
		if(logger.isInfoEnabled()) {
			logger.info("Started Sip Application Session Expiration Timer Service for application " + ((SipContext)sipManager.getContainer()).getApplicationName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerService#isStarted()
	 */
	public boolean isStarted() {
		return started.get();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.ClusteredSipApplicationSessionTimerService#rescheduleTimerLocally(org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession)
	 */
	public void rescheduleTimerLocally(MobicentsSipApplicationSession sipApplicationSession) {
		final String taskId = sipApplicationSession.getId();
		TimerTask timerTask = getScheduler().getLocalRunningTask(taskId);
		if(timerTask == null) {
			SipApplicationSessionTaskData timerTaskData = (SipApplicationSessionTaskData) getScheduler().getTimerTaskData(taskId);
			// we recreate the task locally 
			FaultTolerantSasTimerTask faultTolerantSasTimerTask = new FaultTolerantSasTimerTask(sipApplicationSession, timerTaskData);
			sipApplicationSession.setExpirationTimerTask(faultTolerantSasTimerTask);
			if(timerTaskData != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("SAS Task for sip application session " + taskId + " is not present locally, but on another node, cancelling the remote one and rescheduling it locally.");
				}
				// we cancel it, this will cause the remote owner node to remove it and cancel its local task 
				getScheduler().cancel(taskId);
				// and reset its start time to the correct one
				faultTolerantSasTimerTask.beforeRecover();				
				// and reschedule it locally
				getScheduler().schedule(faultTolerantSasTimerTask, false);
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("SAS Task for sip application session " + taskId + " is not present locally, nor on another node, nothing can be done.");
				}
			}
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("SAS Task for sip application session " + taskId + " is already present locally no need to reschedule it.");
			}
		}
	}
}
