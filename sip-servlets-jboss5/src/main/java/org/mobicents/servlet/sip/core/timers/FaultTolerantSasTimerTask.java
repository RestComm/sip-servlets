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

import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.timers.TimerTask;
import org.mobicents.timers.TimerTaskData;

/**
 * Implementation of the Sip Application Session Expiration Timer being fault tolerant meaning 
 * that if a node crash it will be able to be fired automatically on another node when it should.
 * 
 * It uses the Mobicents Cluster project as an underlying implementation for failing over the timers
 * 
 * It acts as a wrapper around the existing default sip application session timer.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class FaultTolerantSasTimerTask extends TimerTask implements SipApplicationSessionTimerTask {
	protected SipApplicationSessionTimerTask sipApplicationSessionTimerTask;
	private MobicentsSipApplicationSession sipApplicationSession;
	
	public FaultTolerantSasTimerTask(
			MobicentsSipApplicationSession mobicentsSipApplicationSession) {		
		super(init(mobicentsSipApplicationSession));
		sipApplicationSessionTimerTask = new DefaultSasTimerTask(mobicentsSipApplicationSession);
		sipApplicationSession = mobicentsSipApplicationSession;
	}

	public FaultTolerantSasTimerTask(
			MobicentsSipApplicationSession mobicentsSipApplicationSession,
			SipApplicationSessionTaskData sasData) {
		super(sasData);
		sipApplicationSessionTimerTask = new DefaultSasTimerTask(mobicentsSipApplicationSession);
		sipApplicationSession = mobicentsSipApplicationSession;
	}

	private static TimerTaskData init(MobicentsSipApplicationSession sipApplicationSession) {
		SipApplicationSessionTaskData timerTaskData = new SipApplicationSessionTaskData(sipApplicationSession);
		return timerTaskData;
	}

	@Override
	public void run() {
		sipApplicationSessionTimerTask.run();
	}

	public MobicentsSipApplicationSession getSipApplicationSession() {
		return sipApplicationSession;
	}

	public long getDelay() {
		return sipApplicationSessionTimerTask.getDelay();
	}

}
