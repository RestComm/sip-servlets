/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
import org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class DefaultSipApplicationSessionTimerService extends
		ScheduledThreadPoolExecutor implements
		SipApplicationSessionTimerService {

	private static final Logger logger = Logger.getLogger(DefaultSipApplicationSessionTimerService.class
			.getName());
	
	/**
	 * @param corePoolSize
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize) {
		super(corePoolSize);
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize,
			ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	/**
	 * @param corePoolSize
	 * @param handler
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize,
			RejectedExecutionHandler handler) {
		super(corePoolSize, handler);
	}

	/**
	 * @param corePoolSize
	 * @param threadFactory
	 * @param handler
	 */
	public DefaultSipApplicationSessionTimerService(int corePoolSize,
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, threadFactory, handler);
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#remove(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask)
	 */
	public boolean remove(SipApplicationSessionTimerTask expirationTimerTask) {		
		return super.remove(expirationTimerTask);
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#schedule(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask, long, java.util.concurrent.TimeUnit)
	 */
	public ScheduledFuture<?> schedule(
			SipApplicationSessionTimerTask expirationTimerTask, long delay,
			TimeUnit unit) {
		if(logger.isDebugEnabled()) {
			logger.debug("Scheduling sip application session "+ expirationTimerTask.getSipApplicationSession().getKey() +" to expire in " + (delay / (double) 1000 / (double) 60) + " minutes");
		}
		return super.schedule(expirationTimerTask, delay, unit);
	}

}
