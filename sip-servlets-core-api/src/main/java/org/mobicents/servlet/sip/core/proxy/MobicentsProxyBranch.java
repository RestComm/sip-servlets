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

package org.mobicents.servlet.sip.core.proxy;

import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.URI;

import org.mobicents.javax.servlet.sip.ProxyBranchExt;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;

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

	String getTargetURI();

	void cancel1xxTimer();

	void updateTimer(boolean b, MobicentsSipApplicationSession sipApplicationSession);

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
