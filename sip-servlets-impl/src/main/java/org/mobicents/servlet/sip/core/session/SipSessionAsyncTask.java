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

package org.mobicents.servlet.sip.core.session;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipSessionAsynchronousWork;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Wrapper around the AsynchronousWork to make sure the work is done in a thread safe manner
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipSessionAsyncTask implements Runnable {
	private static final Logger logger = Logger.getLogger(SipSessionAsyncTask.class);	
	private SipSessionKey key;
	private SipSessionAsynchronousWork work;
	private SipFactoryImpl sipFactoryImpl;
	
	public SipSessionAsyncTask(SipSessionKey key,
			SipSessionAsynchronousWork work, SipFactoryImpl sipFactory) {
		this.key = key;
		this.work = work;
		this.sipFactoryImpl = sipFactory;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final SipContext sipContext = sipFactoryImpl.getSipApplicationDispatcher().findSipApplication(key.getApplicationName());
		if(sipContext != null) {
			SipManager sipManager = sipContext.getSipManager();
			final SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
					key.getApplicationName(), 
					key.getApplicationSessionId());								
			MobicentsSipApplicationSession sipApplicationSession = sipManager.getSipApplicationSession(sipApplicationSessionKey, false);
			MobicentsSipSession sipSession = sipManager.getSipSession(key, false, sipFactoryImpl, sipApplicationSession);	
					
			if(sipSession != null) {				
				boolean batchStarted = false;
				ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
				try {
					ClassLoader cl = sipContext.getSipContextClassLoader();
					Thread.currentThread().setContextClassLoader(cl);
					// http://code.google.com/p/sipservlets/issues/detail?id=135
					sipContext.bindThreadBindingListener();
					sipContext.enterSipApp(sipApplicationSession, sipSession, false);
					batchStarted = sipContext.enterSipAppHa(true);
					
					work.doAsynchronousWork(sipSession);
				} catch(Throwable t) {
					logger.error("An unexpected exception happened in the SipSessionAsynchronousWork callback on sip session " + key, t);
				} finally {
					sipContext.exitSipAppHa(null, null, batchStarted);
					sipContext.exitSipApp(sipApplicationSession, sipSession);
					// http://code.google.com/p/sipservlets/issues/detail?id=135
					sipContext.unbindThreadBindingListener();
					Thread.currentThread().setContextClassLoader(oldClassLoader);					
				}
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("SipSession " + key + " couldn't be found, it may have been already invalidated.");
				}
			}
		}
	}
}
