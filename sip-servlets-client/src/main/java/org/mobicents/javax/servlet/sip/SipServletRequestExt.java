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

import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;


/**
 * Interface Extension that adds extra features to the JSR 289 SipServetRequest interface.</br>
 * It adds the following capabilities : 
 * 
 * <ul> 		
 * 		<li>
 * 			
 * 		</li> 		
 * </ul>
 * 
 * @author jean.deruelle@gmail.com
 * @since 1.5
 */
public interface SipServletRequestExt extends SipServletRequest {
	/**
	 * This method allows the addition of the appropriate authentication header(s) to the request that was challenged with a challenge response.<br/>
	 * It allows also to cache the credentials so that if a response containing an Authentication-Info header with a nextnonce, the credentials can be reused
	 * to generate the correct header
	 * @param challengeResponse The challenge response (401/407) receieved from a UAS/Proxy.
	 * @param authInfo The AuthInfo object that will add the Authentication headers to the request.
	 * @param cacheCredentials true if the user's credentials should be cached
	 */
	void addAuthHeader(SipServletResponse challengeResponse, AuthInfo authInfo, boolean cacheCredentials);
	
	/**
	 * This method allows the addition of the appropriate authentication header(s) to the request that was challenged with a challenge response without needing 
	 * the creation and/or maintenance of the AuthInfo object.<br/>
	 * It allows also to cache the credentials so that if a response containing an Authentication-Info header with a nextnonce, the credentials can be reused
	 * to generate the correct header 
	 * 
	 * @param challengeResponse the challenge response (401/407) receieved from a UAS/Proxy.
	 * @param username username
	 * @param password password
	 * @param cacheCredentials true if the user's credentials should be cached for that realm
	 */
	void addAuthHeader(SipServletResponse challengeResponse, String username, String password, boolean cacheCredentials);
	
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
