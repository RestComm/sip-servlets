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

package org.mobicents.servlet.sip.proxy;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ResponseType;

public class ProxyBranchTimerTask extends TimerTask{

	private static final Logger logger = Logger.getLogger(ProxyBranchTimerTask.class);
	private ProxyBranchImpl proxyBranch;
	private ResponseType responseType;
	
	public ProxyBranchTimerTask(ProxyBranchImpl proxyBranch, ResponseType responseType)
	{
		this.proxyBranch = proxyBranch;
		this.responseType = responseType;
	}
	
	@Override
	public void run()
	{
		try {
			if(proxyBranch != null) {
				proxyBranch.onTimeout(this.responseType);
			}
		} catch (Exception e) {
			logger.error("Problem in timeout task", e);
		} finally {
			this.proxyBranch = null;
		}
	}
	
	@Override
	public boolean cancel() {
		proxyBranch = null;
		responseType = null;
		return super.cancel();
	}

}
