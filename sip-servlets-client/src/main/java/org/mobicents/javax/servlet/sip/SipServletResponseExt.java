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

import javax.servlet.sip.SipServletResponse;


/**
 * Interface Extension that adds extra features to the JSR 289 SipServetResponse interface.</br>
 * It adds the following capabilities : 
 * 
 * <ul> 		
 * 		<li>
 * 			
 * 		</li> 		
 * </ul>
 * 
 * @author vladimir.ralev@gmail.com
 * @since 1.5
 */
public interface SipServletResponseExt extends SipServletResponse {
	
	/**
	 * This flag indicates that the sessions for this request has been lost. getSession and getApplicationSession() will return null 
	 * and these request will just be proxied outside the container. This feature can only work for proxy applications with main-servlet
	 * declarations. This feature is enabled by the setRouteOrphanRequests method in SipFactoryExt.
	 * 
	 * @return
	 */
	boolean isOrphan();
	
	/**
	 * This flag indicates that the sessions for this request has been lost. getSession and getApplicationSession() will return null 
	 * and these request will just be proxied outside the container. This feature can only work for proxy applications with main-servlet
	 * declarations. This feature is enabled by the setRouteOrphanRequests method in SipFactoryExt.
	 * 
	 * @return
	 */
	void setOrphan(boolean orphan);
}
