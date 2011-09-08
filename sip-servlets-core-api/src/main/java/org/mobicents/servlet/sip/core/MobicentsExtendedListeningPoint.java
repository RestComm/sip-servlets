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

import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;

import org.mobicents.servlet.sip.SipConnector;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsExtendedListeningPoint {

	SipProvider getSipProvider();
	ListeningPoint getListeningPoint();
	boolean isUseStaticAddress();
	String getGlobalIpAddress();
	String getHost(boolean findUsePublicAddress);
	int getPort();
	String getIpAddress(boolean usePublicAddress);
	String getTransport();
	int getGlobalPort();
	SipConnector getSipConnector();
	List<String> getIpAddresses();
	
	ViaHeader createViaHeader(String branch, boolean usePublicAddress);
	ContactHeader createContactHeader(String displayName, String userName,
			boolean usePublicAddress);
	SipURI createRecordRouteURI(boolean usePublicAddress);

}
