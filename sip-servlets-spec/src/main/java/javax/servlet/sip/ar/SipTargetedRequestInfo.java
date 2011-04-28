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

package javax.servlet.sip.ar;

/**
 * The SipTargetedRequestInfo class encapsulates the information that the container 
 * provides to the application router when the container calls the SipApplicationRouter.getNextApplication() 
 * method and the initial request is a targeted one.
 * 
 *  @since 1.1
 */
public class SipTargetedRequestInfo {
	private String applicationName;
	private SipTargetedRequestType type;
	
	/**
	 * Creates a SipTargetedRequestInfo object containing information necessary to help the application router make its application selection decision when the request is targeted. 
	 * This information includes the type of targeted request and the name of the targeted application. 
	 * @param type Targeted request type {ENCODED_URI, JOIN, REPLACES}
	 * @param applicationName The name of the application targeted by the request.
	 */	
	public SipTargetedRequestInfo(SipTargetedRequestType type, String applicationName) {
		this.type = type;
		this.applicationName = applicationName;
	}
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @return the type
	 */
	public SipTargetedRequestType getType() {
		return type;
	} 
	
	
}
