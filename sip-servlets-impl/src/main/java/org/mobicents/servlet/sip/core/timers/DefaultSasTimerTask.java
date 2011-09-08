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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionEventType;

/**
 * Timer task that will notify the listeners that the sip application session has expired 
 * It is an improved timer task that is delayed every time setLastAccessedTime is called on it.
 * It is delayed of lastAccessedTime + lifetime
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class DefaultSasTimerTask implements SipApplicationSessionTimerTask {
	
	private static final Logger logger = Logger.getLogger(DefaultSasTimerTask.class);
	
	private MobicentsSipApplicationSession sipApplicationSession;
	
	protected transient ScheduledFuture<MobicentsSipApplicationSession> expirationTimerFuture;
	
	public DefaultSasTimerTask(MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		this.sipApplicationSession = mobicentsSipApplicationSession;
	}
	
	@SuppressWarnings("unchecked")
	public void run() {	
		try {
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
		} catch (Throwable t) {
			logger.error("Timer problem", t);
		}
	}

	private void tryToExpire() {
		final SipContext sipContext = getSipApplicationSession().getSipContext();
		sipContext.enterSipApp(getSipApplicationSession(), null, false);
		boolean batchStarted = sipContext.enterSipAppHa(true);
		try {
			getSipApplicationSession().setExpirationTimerTask(null);
			getSipApplicationSession().notifySipApplicationSessionListeners(SipApplicationSessionEventType.EXPIRATION);
			//It is possible that the application grant an extension to the lifetime of the session, thus the sip application
			//should not be treated as expired.
			if(getDelay() <= 0) {
				getSipApplicationSession().setExpired(true);
				if(getSipApplicationSession().isValidInternal()) {			
					getSipApplicationSession().invalidate(true);				
				}
			} else {
				// Issue 1773 : http://code.google.com/p/mobicents/issues/detail?id=1773 
				// this has been commented out because of JSR 289, Section 6.1.2 SipApplicationSession Lifetime :
				// "Servlets can register for application session timeout notifications using the SipApplicationSessionListener interface. 
				// In the sessionExpired() callback method, the application may request an extension of the application session lifetime 
				// by invoking setExpires() on the timed out SipApplicationSession giving as an argument the number of minutes until the session expires again"
				// Even sending a message out will not start the expiration timer anew indirectly otherwise it makes some TCK tests fail
				// Fix for Issue 1698				
//				if(getSipApplicationSession().getExpirationTimerTask() == null) {					
//					long sleep = sipApplicationSession.getExpirationTimeInternal() - System.currentTimeMillis();
//					if(logger.isDebugEnabled()) {
//						logger.debug("expiration timer task is null so the application has extended the session lifetime indirectly by sending an indialog request, rescheduling the sip app session " + sipApplicationSession.getId() + "to expire in " + sleep + " ms");
//					}
//					if(sleep > 0) {
//						final SipApplicationSessionTimerTask expirationTimerTask = sipContext.getSipApplicationSessionTimerService().createSipApplicationSessionTimerTask(sipApplicationSession);
//						sipApplicationSession.setExpirationTimerTask(expirationTimerTask);					
//						expirationTimerFuture = (ScheduledFuture<MobicentsSipApplicationSession>) sipContext.getSipApplicationSessionTimerService().schedule(expirationTimerTask, sleep, TimeUnit.MILLISECONDS);
//					}
//				} else {
					if(logger.isDebugEnabled()) {
						if(getSipApplicationSession().getExpirationTimerTask() != null) {
							logger.debug("expiration timer task is non null so the application has extended the session lifetime directly through setExpires");
						}
					}
//				}
			}
		} finally {							
			sipContext.exitSipAppHa(null, null, batchStarted);
			sipContext.exitSipApp(getSipApplicationSession(), null);
			setSipApplicationSession(null);
		}
	}				
	
	public long getDelay() {
		if(sipApplicationSession != null) {
			return sipApplicationSession.getExpirationTimeInternal() - System.currentTimeMillis();
		}
		if(logger.isDebugEnabled()) {
			logger.debug("sipapplicationsession has been nullified, return -1");
		}
		return -1;
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

	public void setScheduledFuture(ScheduledFuture<MobicentsSipApplicationSession> schedule) {
		expirationTimerFuture = schedule;
	}						
	
	public ScheduledFuture<MobicentsSipApplicationSession> getScheduledFuture() {
		return expirationTimerFuture;
	}
} 
