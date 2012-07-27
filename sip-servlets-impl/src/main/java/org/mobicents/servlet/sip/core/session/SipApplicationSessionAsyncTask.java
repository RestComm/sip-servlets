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
import org.mobicents.javax.servlet.sip.SipApplicationSessionAsynchronousWork;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Wrapper around the SipApplicationSessionAsynchronousWork to make sure the work is done in a thread safe manner
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipApplicationSessionAsyncTask implements Runnable {
	private static final Logger logger = Logger.getLogger(SipApplicationSessionAsyncTask.class);	
	private SipApplicationSessionKey key;
	private SipApplicationSessionAsynchronousWork work;
	private SipFactoryImpl sipFactoryImpl;
	
	public SipApplicationSessionAsyncTask(SipApplicationSessionKey key,
			SipApplicationSessionAsynchronousWork work, SipFactoryImpl sipFactory) {
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
									
			MobicentsSipApplicationSession sipApplicationSession = sipManager.getSipApplicationSession(key, false);
					
			if(sipApplicationSession != null) {				
				ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
				boolean batchStarted = false;
				try {
					sipContext.enterSipContext();	
					sipContext.enterSipApp(sipApplicationSession, null, false);
					batchStarted = sipContext.enterSipAppHa(true);
					
					work.doAsynchronousWork(sipApplicationSession);
				} catch(Throwable t) {
					logger.error("An unexpected exception happened in the SipApplicationSessionAsynchronousWork callback on sip application session " + key, t);
				} finally {
					sipContext.exitSipAppHa(null, null, batchStarted);
					sipContext.exitSipApp(sipApplicationSession, null);
					sipContext.exitSipContext(oldClassLoader);			
				}
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("SipApplicationSession " + key + " couldn't be found, it may have been already invalidated.");
				}
			}
		}
	}
}
