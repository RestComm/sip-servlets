/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.core.session;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipApplicationSessionAsynchronousWork;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;

/**
 * Wrapper around the SipApplicationSessionAsynchronousWork to make sure the work is done in a thread safe manner
 * 
 * @author jean.deruelle@telestax.com
 *
 */
public class SipApplicationSessionAsyncTask implements Runnable {
	private static final Logger logger = Logger.getLogger(SipApplicationSessionAsyncTask.class);	
	private SipApplicationSessionKey key;
	private SipApplicationSessionAsynchronousWork work;
	private MobicentsSipFactory sipFactoryImpl;
	
	public SipApplicationSessionAsyncTask(SipApplicationSessionKey key,
			SipApplicationSessionAsynchronousWork work, MobicentsSipFactory sipFactory) {
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
					if(logger.isDebugEnabled()) {
						logger.debug("Asynchronous work for sip app session " + key + " scheduled to run once the sipappsession lock is available.");
					}
					sipContext.enterSipContext();	
					sipContext.enterSipApp(sipApplicationSession, null, false, true);
					batchStarted = sipContext.enterSipAppHa(true);
					if(logger.isDebugEnabled()) {
						logger.debug("Starting Asynchronous work for sip app session " + key);
					}
					work.doAsynchronousWork(sipApplicationSession);
					if(logger.isDebugEnabled()) {
						logger.debug("Done with Asynchronous work for sip app session " + key);
					}
				} catch(Throwable t) {
					logger.error("An unexpected exception happened in the SipApplicationSessionAsynchronousWork callback on sip application session " + key, t);
				} finally {
					if(logger.isDebugEnabled()) {
						logger.debug("Exiting Asynchronous work for sip app session " + key);
					}
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
