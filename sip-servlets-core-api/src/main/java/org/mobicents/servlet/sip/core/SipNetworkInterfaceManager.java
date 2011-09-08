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
 * @author jean.deruelle@gmail.com
 *
 */
public interface SipNetworkInterfaceManager {

	MobicentsExtendedListeningPoint findMatchingListeningPoint(String transport, boolean b);
	MobicentsExtendedListeningPoint findMatchingListeningPoint(
			javax.sip.address.SipURI outboundInterfaceURI, boolean b);
	
	Iterator<MobicentsExtendedListeningPoint> getExtendedListeningPoints();
	
	List<SipURI> getOutboundInterfaces();
	
	void addExtendedListeningPoint(
			MobicentsExtendedListeningPoint extendedListeningPoint);
	void removeExtendedListeningPoint(
			MobicentsExtendedListeningPoint extendedListeningPoint);
	boolean findUsePublicAddress(Message message);
		

}
