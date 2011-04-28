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

package org.mobicents.javax.servlet.sip;

import java.util.EventListener;

import javax.servlet.sip.ProxyBranch;

/**
 * Interface Extension that adds extra features to the JSR 289 ProxyBranch capabilities.</br>
 * 
 * @apiviz.uses org.mobicents.javax.servlet.sip.ResponseType
 * @apiviz.uses javax.servlet.sip.ProxyBranch 
 * @author jean.deruelle@gmail.com
 * 
 */
public interface ProxyBranchListener extends EventListener {

	/**
	 * When a search timeout timer expires on a proxy branch, either on final responses or 1xx response, 
	 * this triggers a call to this method on all classes implementing this listener interface
	 * @param responseType type of response (1xx or final) for which the timer expired
	 * @param proxyBranch the proxy branch on which the timer expired
	 */
	void onProxyBranchResponseTimeout(ResponseType responseType, ProxyBranch proxyBranch);
}
