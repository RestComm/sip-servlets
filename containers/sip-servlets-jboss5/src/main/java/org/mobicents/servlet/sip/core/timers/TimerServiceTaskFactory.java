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

import org.jboss.web.tomcat.service.session.ClusteredSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskData;
import org.mobicents.timers.TimerTaskFactory;

/**
 * Allow to recreate a sip servlet timer task upon failover
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class TimerServiceTaskFactory implements TimerTaskFactory {
	
	private ClusteredSipManager<? extends OutgoingDistributableSessionData> sipManager;
	
	public TimerServiceTaskFactory(ClusteredSipManager<? extends OutgoingDistributableSessionData> sipManager) {
		this.sipManager = sipManager;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.timers.TimerTaskFactory#newTimerTask(org.mobicents.timers.TimerTaskData)
	 */
	public TimerTask newTimerTask(TimerTaskData data) {
		MobicentsSipApplicationSession sipApplicationSession = sipManager.getSipApplicationSession(((TimerServiceTaskData)data).getKey(), false);
		if(sipApplicationSession.getTimer((String)data.getTaskID()) == null) {
			if(((SipContext)sipManager.getContainer()).getConcurrencyControlMode() != ConcurrencyControlMode.SipApplicationSession) {
				// http://code.google.com/p/sipservlets/issues/detail?id=90
				final Thread currentThread = Thread.currentThread();
				final ClassLoader currentThreadClassLoader = currentThread.getContextClassLoader();				
				try {
					currentThread.setContextClassLoader(sipApplicationSession.getSipContext().getSipContextClassLoader());
					return new TimerServiceTask(sipManager, null, (TimerServiceTaskData)data);
				}
				finally {
					currentThread.setContextClassLoader(currentThreadClassLoader);
				}
			}
		}
		// returning null to avoid recovery since it was already recovered above
		return null;
	}

}