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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class ProxyTimerServiceImpl extends Timer implements ProxyTimerService {

	private static final Logger logger = Logger.getLogger(ProxyTimerServiceImpl.class
			.getName());
	private AtomicBoolean started = new AtomicBoolean(false);
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#cancel(org.mobicents.servlet.sip.proxy.ProxyBranchTimerTask)
	 */
	public void cancel(TimerTask task) {
		task.cancel();
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#isStarted()
	 */
	public boolean isStarted() {
		return started.get();
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#schedule(org.mobicents.servlet.sip.proxy.ProxyBranchTimerTask, long)
	 */
	public void schedule(TimerTask task, long delay) {
		super.schedule(task, delay);
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#stop()
	 */
	public void stop() {		
		started.set(false);
		super.cancel();
		if(logger.isDebugEnabled()) {
			logger.debug("Stopped proxy timer service "+ this);
		}
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#start()
	 */
	public void start() {	
		started.set(true);
		if(logger.isDebugEnabled()) {
			logger.debug("Started proxy timer service "+ this);
		}
	}
}
