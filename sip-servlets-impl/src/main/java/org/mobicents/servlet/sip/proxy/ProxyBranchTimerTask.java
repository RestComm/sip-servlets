/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
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

package org.mobicents.servlet.sip.proxy;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ResponseType;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;

public class ProxyBranchTimerTask extends TimerTask{

	private static final Logger logger = Logger.getLogger(ProxyBranchTimerTask.class);
	private ProxyBranchImpl proxyBranch;
	private ResponseType responseType;
	private MobicentsSipApplicationSession sipApplicationSession;
	
	public ProxyBranchTimerTask(ProxyBranchImpl proxyBranch, ResponseType responseType, MobicentsSipApplicationSession sipApplicationSession)
	{
		this.proxyBranch = proxyBranch;
		this.responseType = responseType;
		this.sipApplicationSession = sipApplicationSession;
	}
	
	@Override
	public void run()
	{
		final SipContext sipContext = sipApplicationSession.getSipContext();
		boolean batchStarted = false;
		try {
				// https://github.com/Mobicents/sip-servlets/issues/70 This timer task needs to be executed 
				// only if the sipapplicationsession is not currently in use or there is concurrency issues
				sipContext.enterSipApp(sipApplicationSession, null, false, true);
				batchStarted = sipContext.enterSipAppHa(true);
				if(proxyBranch != null) {
					proxyBranch.onTimeout(this.responseType);
				}
		} catch (Exception e) {
			logger.error("Problem in timeout task", e);
		} finally {
			this.proxyBranch = null;
			sipContext.exitSipAppHa(null, null, batchStarted);
			sipContext.exitSipApp(sipApplicationSession, null);
		}
	}
	
	@Override
	public boolean cancel() {
		proxyBranch = null;
		responseType = null;
		return super.cancel();
	}

}
