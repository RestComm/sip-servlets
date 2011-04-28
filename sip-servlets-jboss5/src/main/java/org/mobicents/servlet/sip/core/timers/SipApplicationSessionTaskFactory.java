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

import org.apache.log4j.Logger;
import org.jboss.web.tomcat.service.session.ClusteredSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskData;
import org.mobicents.timers.TimerTaskFactory;

/**
 * Allow to recreate a sip application session timer task upon failover
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipApplicationSessionTaskFactory implements TimerTaskFactory {
	
	private static final Logger logger = Logger.getLogger(SipApplicationSessionTaskFactory.class);
	
	private ClusteredSipManager<? extends OutgoingDistributableSessionData> sipManager;
	
	public SipApplicationSessionTaskFactory(ClusteredSipManager<? extends OutgoingDistributableSessionData> sipManager) {
		this.sipManager = sipManager;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.timers.TimerTaskFactory#newTimerTask(org.mobicents.timers.TimerTaskData)
	 */
	public TimerTask newTimerTask(TimerTaskData data) {	
		SipApplicationSessionTaskData sasData = (SipApplicationSessionTaskData)data;
		MobicentsSipApplicationSession sipApplicationSession = sipManager.getSipApplicationSession(sasData.getKey(), false);
		if(sipApplicationSession != null) {
			if(sipApplicationSession.getExpirationTimerTask() == null) {
				if(((SipContext)sipManager.getContainer()).getConcurrencyControlMode() != ConcurrencyControlMode.SipApplicationSession) {
					if(logger.isDebugEnabled()) {
						logger.debug("sip application session for key " + sasData.getKey() + " was found");						
					}
					FaultTolerantSasTimerTask faultTolerantSasTimerTask = new FaultTolerantSasTimerTask(sipApplicationSession, sasData);
					if(sipApplicationSession != null) {
						sipApplicationSession.setExpirationTimerTask(faultTolerantSasTimerTask);
					}
					return faultTolerantSasTimerTask;
				} 
			}
		} else {
			logger.debug("sip application session for key " + sasData.getKey() + " was not found neither locally or in the cache, not recovering the sas timer task");
		}
		// returning null to avoid recovery since it was already recovered above
		return null;
	}

}
