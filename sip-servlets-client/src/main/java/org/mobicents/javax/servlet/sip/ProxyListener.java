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
package org.mobicents.javax.servlet.sip;

import java.util.EventListener;

/**
 * Interface Extension that adds extra features to the JSR 289 Proxy capabilities.</br>
 * 
 * @apiviz.uses org.mobicents.javax.servlet.sip.ResponseType
 * @author jean.deruelle@gmail.com
 *
 */
public interface ProxyListener extends EventListener {

	/**
	 * When a search timeout timer expires in the proxy, either on final responses or 1xx response, 
	 * this triggers a call to this method on all classes implementing this listener interface
	 * @param responseType type of response (1xx or final) for which the timer expired
	 */
	void onProxyResponseTimeout(ResponseType responseType);
}
