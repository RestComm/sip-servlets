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

package org.mobicents.servlet.sip.core.proxy;

import java.util.Map;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;

import org.mobicents.javax.servlet.sip.ProxyExt;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;

/**
 * Extension to the Proxy interface from JSR 289
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsProxy extends Proxy, ProxyExt {

	MobicentsProxyBranch getFinalBranchForSubsequentRequests();
	Map getTransactionMap();
	boolean isTerminationSent();
	void setAckReceived(boolean equalsIgnoreCase);
	void setOriginalRequest(MobicentsSipServletRequest sipServletRequest);
	// https://code.google.com/p/sipservlets/issues/detail?id=266
	void cancelAllExcept(ProxyBranch except, String[] protocol, int[] reasonCode, String[] reasonText, boolean throwExceptionIfCannotCancel);
}
