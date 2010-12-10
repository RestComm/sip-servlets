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

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;


/**
 * Interface Extension that adds extra features to the JSR 289 SipServetRequest interface.</br>
 * It adds the following capabilities : 
 * 
 * <ul> 		
 * 		<li>
 * 			Allows for applications to copy the record route headers on the responses to subsequent INVITE requests 
 * where the dialog route set shouldn't normally be changed to cope with non compliant RFC 3261 servers. 
 * 		</li> 		
 * </ul>
 * 
 * @author jean.deruelle@gmail.com
 * @since 1.5
 */
public interface SipServletRequestExt extends SipServletRequest {
	/**
	 * Creates a response for this request with the specified status code and reason phrase.
	 * @param statusCode the status code for the response
	 * @param reasonPhrase reason phrase to appear in response line
	 * @param copyRecordRouteHeaders whether or not to copy the record route headers on the response to a subsequent INVITE request
	 * @return response object with specified status code and reason phrase and eventually the record route from the request copied in the response
	 * 
	 * @throws IllegalArgumentException if the statuscode is not a valid SIP status code
	 * @throws IllegalStateException if this request has already been responded to with a final status code
	 */
	SipServletResponse createResponse(int statusCode, String reasonPhrase, boolean copyRecordRouteHeaders);
}
