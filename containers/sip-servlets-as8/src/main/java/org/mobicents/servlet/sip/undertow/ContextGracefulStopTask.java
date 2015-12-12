/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.servlet.sip.undertow;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipContext;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class ContextGracefulStopTask implements Runnable {
	private static final Logger logger = Logger.getLogger(ContextGracefulStopTask.class);		
	SipContext sipContext;
	long timeToWait;
	long startTime;	

	public ContextGracefulStopTask(SipContext context, long timeToWait) {
		sipContext = context;
		this.timeToWait = timeToWait;
		startTime = System.currentTimeMillis();
	}

	public void run() {		
		int numberOfActiveSipApplicationSessions = sipContext.getSipManager().getActiveSipApplicationSessions();
		int numberOfActiveHttpSessions = ((UndertowSipManager) ((SipContextImpl)sipContext).getSessionManager()).getActiveSessions().size();
		if(logger.isTraceEnabled()) {
			logger.trace("ContextGracefulStopTask running for context " + sipContext.getApplicationName() + ", number of Sip Application Sessions still active " + numberOfActiveSipApplicationSessions + " number of HTTP Sessions still active " + numberOfActiveHttpSessions);
		}
		boolean stopPrematuraly = false;
		long currentTime = System.currentTimeMillis();
		// if timeToWait is positive, then we check the time since the task started, if the time is greater than timeToWait we can safely stop the context 
		if(timeToWait > 0 && ((currentTime - startTime) > timeToWait)) {
			stopPrematuraly = true;			
		}
		if((numberOfActiveSipApplicationSessions <= 0 &&  numberOfActiveHttpSessions <= 0) || stopPrematuraly) {
			try {
				((SipContextImpl)sipContext).stop();					
			} catch (ServletException e) {
				logger.error("Couldn't gracefully stop context " + sipContext.getApplicationName(), e);
			}
		}
	}
}
