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

import javax.servlet.sip.SipFactory;

/**
 * Interface Extension that adds extra features to the JSR 289 SipFactory interface to allow proxying of orphaned requests.</br>
 * It adds the following capabilities : 
 * 
 * <ul> 		
 * 		<li>Requests and responses that do NOT have associated SipSession or SipApplicationSession will still be delivered to the main servlet of the application this factory belongs to. Such requests and responses are called "orphaned".</li> 	
 *      <li>The requests and responses will be proxied outside the container to their regular destination after being delivered to the application</li>
 *      <li>When orphaned requests or responses are delivered to the application you can check this with SipServletRequest.isOrhpan() and SipServletResponse.isOrphan()</li>	
 * 		<li>The sessions for these requests are lost and getSession() methods will cause brand new sessions to be created. These new sessions are unrelated to the lost sessions.</li>
 * </ul>
 * 
 * You can turn on and off this feature on demand from your application at any time. By default it is off.
 * 
 * This feature is useful for stateless applications that don't want the memory overhead of
 * sessions and dialogs. Inherently when there are no sessions there is no need for session
 * replication in clusters. You can start a session in the first stage of the call (INVITE-OK-ACK)
 * then invalidate the session and the subsequent requests will still be proxied to the correct
 * destination while you application is still on the path and notified of the messages.
 * 
 * Good use cases for this feature are:
 * <ul>
 * <li>Long calls proxied through the server that are being accounted in a remote database instead of sessions</li>
 * <li>Proxy applications that must scale linearly in a cluster of Sip Servlets Application Servers</li>
 * </ul>
 * 
 * 
 * @author jean.deruelle@gmail.com
 * @author vladimir.ralev@gmail.com
 * 
 * @since 1.6
 */
public interface SipFactoryExt extends SipFactory {
	/**
	 * This flag specifies if the current application can receive subsequent requests after their session has been lost or invalidated.
	 * This feature can't be used with chained applications. Only single application can be on the path of the requests.
	 * @return
	 */
	boolean isRouteOrphanRequests();
	
	/**
	 * This flag specifies if the current application can receive subsequent requests after their session has been lost or invalidated.
	 * This feature can't be used with chained applications. Only single application can be on the path of the requests.
	 * @return
	 */
	void setRouteOrphanRequests(boolean routeOrphanRequets);
}
