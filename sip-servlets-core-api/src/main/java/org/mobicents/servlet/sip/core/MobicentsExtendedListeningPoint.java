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

package org.mobicents.servlet.sip.core;

import gov.nist.javax.sip.ListeningPointExt;

import java.util.List;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;

import org.mobicents.servlet.sip.SipConnector;

/**
 * Represents a JAIN SIP Based Connector listening for Incoming Request
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsExtendedListeningPoint {

	/**
	 * Get the JAIN SIP provider assiociated with this listening point
	 * @return the JAIN SIP provider assiociated with this listening point
	 */
	SipProvider getSipProvider();
	/**
	 * Get the JAIN SIP listening point associated with this listening point
	 * @return the JAIN SIP listening point associated with this listening point
	 */
	ListeningPointExt getListeningPoint();
	/**
	 * true if a static address should be used
	 * @return
	 */
	boolean isUseStaticAddress();
	/**
	 * Returns the value of the global IP Address found through STUN discovery
	 * @return the value of the global IP Address found through STUN discovery
	 */
	String getGlobalIpAddress();
	/**
	 * Get the host ip address
	 * @param findUsePublicAddress whehter or not to return the ip address found through STUN discovery
	 * @return
	 */
	String getHost(boolean findUsePublicAddress);
	/**
	 * Port of the connector
	 * @return port of the connector
	 */
	int getPort();
	/**
	 * Get the host ip address
	 * @param findUsePublicAddress whehter or not to return the ip address found through STUN discovery
	 * @return
	 */
	String getIpAddress(boolean usePublicAddress);
	/**
	 * Transport of the connector
	 * @return transport of the connector
	 */
	String getTransport();
	/**
	 * Returns the value of the global port found through STUN discovery
	 * @return the value of the global port found through STUN discovery
	 */
	int getGlobalPort();
	/**
	 * Get the corresponding Sip Connector
	 * @return
	 */
	SipConnector getSipConnector();
	/**
	 * get the list of ip addresses this connector is tied to (in case of binding to 0.0.0.0 there can be many) 
	 * @return the list of ip addresses this connector is tied to (in case of binding to 0.0.0.0 there can be many)
	 */
	List<String> getIpAddresses();
	/**
	 * Create a Via Header based on this SIP Listening Point
	 * @param branch branch value to use for the via header to create, null will return an auto generated one or will be assigend later by the stack
	 * @param usePublicAddress wether or not to use the ip address found by STUN discovery
	 * @return a Via Header based on this SIP Listening Point
	 */
	ViaHeader createViaHeader(String branch, boolean usePublicAddress);
	/**
	 * Create a Contact Header based on this SIP Listening Point
	 * @param displayName the display name to use for the contact header to create
	 * @param userName the user name to use for the contact header to create
	 * @param usePublicAddress wether or not to use the ip address found by STUN discovery
	 * @return a Contact Header based on this SIP Listening Point
	 */
	ContactHeader createContactHeader(String displayName, String userName,
			boolean usePublicAddress);
	/**
	 * Create a Contact Header based on this SIP Listening Point
	 * @param displayName the display name to use for the contact header to create
	 * @param userName the user name to use for the contact header to create
	 * @param usePublicAddress wether or not to use the ip address found by STUN discovery
	 * @param outboundInterface the outbound interface ip address to be used for the host part of the Contact header
	 * @return a Contact Header based on this SIP Listening Point
	 */
	ContactHeader createContactHeader(String displayName, String userName,
			boolean usePublicAddress, String outboundInterface);
	/**
	 * Create a Record Route Header based on this SIP Listening Point
	 * @param usePublicAddress wether or not to use the ip address found by STUN discovery
	 * @return a Record Route Header based on this SIP Listening Point
	 */
	SipURI createRecordRouteURI(boolean usePublicAddress);
	
	/**
	 * return true if the ip address maps to 0.0.0.0.
	 * If it's true call getIpAddresses ti get the real list of ip addresses it maps too
	 * @return true if the ip adress is a any local address
	 */
	boolean isAnyLocalAddress();

}
