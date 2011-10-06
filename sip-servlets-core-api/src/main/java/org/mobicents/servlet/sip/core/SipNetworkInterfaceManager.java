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

import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipURI;
import javax.sip.message.Message;


/**
 * Placeholder for all sip network interfaces mapped to a sip
 * standard service. It should allow one to query for local and external address 
 * (discovered by STUN) of a specific interface. <br/>
 * 
 * It will also allow various queries against its network interfaces to discover
 * the right one to use. Those queries could be cached in order to improve performance 
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface SipNetworkInterfaceManager {

	/**
	 * Find a listening point for the given transport, if strict is false the method is allowed to return a listening point for a different transport
	 * @param transport the transport
	 * @param strict if strict is false the method is allowed to return a listening point for a different transport
	 * @return the listeniong point or null if no listening point are found and strict is true
	 */
	MobicentsExtendedListeningPoint findMatchingListeningPoint(String transport, boolean strict);
	/**
	 * Find a listening point for the given URI, if strict is false the method is allowed to return a listening point for a different transport
	 * @param outboundInterfaceURI the URI to use
	 * @param strict if strict is false the method is allowed to return a listening point for a different transport
	 * @return the listeniong point or null if no listening point are found and strict is true
	 */
	MobicentsExtendedListeningPoint findMatchingListeningPoint(
			javax.sip.address.SipURI outboundInterfaceURI, boolean strict);
	/**
	 * Retrieves all the listening points
	 * @return all the listening points
	 */
	Iterator<MobicentsExtendedListeningPoint> getExtendedListeningPoints();
	/**
	 * Retrieves all the listening points as URIs
	 * @return all the listening points as URIs
	 */
	List<SipURI> getOutboundInterfaces();
	/**
	 * Adds a new listening point
	 * @param extendedListeningPoint the listening point to add
	 */
	void addExtendedListeningPoint(
			MobicentsExtendedListeningPoint extendedListeningPoint);
	/**
	 * Removes a new listening point
	 * @param extendedListeningPoint the listening point to remove
	 */
	void removeExtendedListeningPoint(
			MobicentsExtendedListeningPoint extendedListeningPoint);
	/**
	 * Find whehter or not the public ip address found by STUN Discovery should be used for this message
	 * @param message the message to check
	 * @return true if the public ip address found by STUN Discovery for this message should be used
	 */
	boolean findUsePublicAddress(Message message);
	/**
	 * Retrieve the first matching listening Point corresponding to the 
	 * ipAddress port and transport given in parameter.
	 *
	 * @param ipAddress the ip address
	 * @param port the port
	 * @param transport the transport
	 * @return Retrieve the first matching listening point corresponding to the ipAddress port and transport.
	 * If none has been found, null is returned.
	 */
	MobicentsExtendedListeningPoint findMatchingListeningPoint(String host,
			int port, String transport);
		

}
