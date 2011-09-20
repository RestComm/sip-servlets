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

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.mobicents.javax.servlet.sip.SipFactoryExt;


/**
 * Extension of the SipFactory interface from Sip Servlets Spec giving access to the sip application dispatcher 
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsSipFactory extends SipFactoryExt {
	/**
	 * Retrieves the Sip Application Dispatcher
	 * @return the Sip Application Dispatcher
	 */
	SipApplicationDispatcher getSipApplicationDispatcher();

	MobicentsSipServletMessageFactory getMobicentsSipServletMessageFactory();
	
	/**
	 * Retrieves the JAIN SIP Address Factory
	 * @return the JAIN SIP Address Factory
	 */
	AddressFactory getAddressFactory();
	/**
	 * Retrieves the JAIN SIP Header Factory
	 * @return the JAIN SIP  Header Factory
	 */
	HeaderFactory getHeaderFactory();
	/**
	 * Retrieves the JAIN SIP Message Factory
	 * @return the JAIN SIP Message Factory
	 */
	MessageFactory getMessageFactory();
	/**
	 * Retrieves the JAIN SIP Factory
	 * @return the JAIN SIP Factory
	 */
	SipFactory getJainSipFactory();
	/**
	 * Initializes the JAIN SIP SipFactory with the given path name
	 * @param sipPathName path name to use to initialize the JAIN SIP Stack
	 * @param usePrettyEncoding allow to display the headers on multiple lines or not
	 */
	void initialize(String sipPathName, boolean usePrettyEncoding);
}
