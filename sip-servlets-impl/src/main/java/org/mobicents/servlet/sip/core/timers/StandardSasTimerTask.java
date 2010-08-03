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

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionEventType;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Timer task that will notify the listeners that the sip application session has expired 
 * It is an improved timer task that is delayed every time setLastAccessedTime is called on it.
 * It is delayed of lastAccessedTime + lifetime
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class StandardSasTimerTask extends TimerTask implements SipApplicationSessionTimerTask {
	
	private static final Logger logger = Logger.getLogger(StandardSasTimerTask.class);
	
	private MobicentsSipApplicationSession sipApplicationSession;
	
	public StandardSasTimerTask(MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		this.sipApplicationSession = mobicentsSipApplicationSession;
	}
	
	@SuppressWarnings("unchecked")
	public void run() {	
		if(logger.isDebugEnabled()) {
			logger.debug("initial kick off of SipApplicationSessionTimerTask running for sip application session " + sipApplicationSession.getId());
		}
					
		long sleep = getDelay();
		if(sleep > 0) {
			// if the session has been accessed since we started it, put it to sleep
			if(logger.isDebugEnabled()) {
				logger.debug("expirationTime is " + sipApplicationSession.getExpirationTimeInternal() + 
						", now is " + System.currentTimeMillis() +
						" sleeping for " + sleep / 1000L + " seconds");
			}
			final SipContext sipContext = sipApplicationSession.getSipContext();
			final SipApplicationSessionTimerTask expirationTimerTask = sipContext.getSipApplicationSessionTimerService().createSipApplicationSessionTimerTask(sipApplicationSession);			
			sipApplicationSession.setExpirationTimerTask(expirationTimerTask);					
			sipContext.getSipApplicationSessionTimerService().schedule(expirationTimerTask, sleep, TimeUnit.MILLISECONDS);
		} else {
			tryToExpire();
		}
	}

	private void tryToExpire() {
		final SipContext sipContext = getSipApplicationSession().getSipContext();
		sipContext.enterSipApp(getSipApplicationSession(), null);
		sipContext.enterSipAppHa(true);
		try {
			getSipApplicationSession().setExpirationTimerTask(null);
			getSipApplicationSession().notifySipApplicationSessionListeners(SipApplicationSessionEventType.EXPIRATION);
			//It is possible that the application grant an extension to the lifetime of the session, thus the sip application
			//should not be treated as expired.
			if(getDelay() <= 0) {
				
				getSipApplicationSession().setExpired(true);
				if(getSipApplicationSession().isValidInternal()) {			
					getSipApplicationSession().invalidate();				
				}
			} else {
				if(getSipApplicationSession().getExpirationTimerTask() == null) {					
					long sleep = sipApplicationSession.getExpirationTimeInternal() - System.currentTimeMillis();
					if(logger.isDebugEnabled()) {
						logger.debug("expiration timer task is null so the application has extended the session lifetime indirectly by sending an indialog request, rescheduling the sip app session " + sipApplicationSession.getId() + "to expire in " + sleep + " ms");
					}
					if(sleep > 0) {
						final SipApplicationSessionTimerTask expirationTimerTask = sipContext.getSipApplicationSessionTimerService().createSipApplicationSessionTimerTask(sipApplicationSession);
						sipApplicationSession.setExpirationTimerTask(expirationTimerTask);					
						sipContext.getSipApplicationSessionTimerService().schedule(expirationTimerTask, sleep, TimeUnit.MILLISECONDS);
					}
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("expiration timer task is non null so the application has extended the session lifetime directly through setExpires");
					}
				}
			}
		} finally {							
			sipContext.exitSipAppHa(null, null);
			sipContext.exitSipApp(getSipApplicationSession(), null);
			setSipApplicationSession(null);
		}
	}			
	
	@Override
	public boolean cancel() {
		// can cause NPE on race condition where a task has been cancelled
		// See Issue 1419 NullPointerException in StandardSasTimerTask
		// http://code.google.com/p/mobicents/issues/detail?id=1419
//		sipApplicationSession = null;
		return super.cancel();
	}
	
	public long getDelay() {
		return sipApplicationSession.getExpirationTimeInternal() - System.currentTimeMillis();
	}

	/**
	 * @param sipApplicationSession the sipApplicationSession to set
	 */
	public void setSipApplicationSession(MobicentsSipApplicationSession sipApplicationSession) {
		this.sipApplicationSession = sipApplicationSession;
	}

	/**
	 * @return the sipApplicationSession
	 */
	public MobicentsSipApplicationSession getSipApplicationSession() {
		return sipApplicationSession;
	}
} 
