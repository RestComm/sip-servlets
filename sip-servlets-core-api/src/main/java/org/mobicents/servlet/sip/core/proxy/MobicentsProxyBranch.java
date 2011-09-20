/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.mobicents.servlet.sip.core.proxy;

import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.URI;

import org.mobicents.javax.servlet.sip.ProxyBranchExt;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;

/**
 * Extension to the ProxyBranch interface from JSR 289
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsProxyBranch extends ProxyBranch, ProxyBranchExt {

	boolean isTimedOut();

	boolean isCanceled();

	void start();

	URI getTargetURI();

	void cancel1xxTimer();

	void updateTimer(boolean b);

	void cancelTimer();

	MobicentsSipServletRequest getPrackOriginalRequest();

	void setResponse(MobicentsSipServletResponse sipServletResponse);
	/**
	 * A callback. Here we receive all responses from the proxied requests we have sent.
	 * 
	 * @param response
	 * @throws DispatcherException 
	 */
	void onResponse(MobicentsSipServletResponse sipServletResponse, int status) throws DispatcherException;

	void proxySubsequentRequest(MobicentsSipServletRequest sipServletRequest);

}
