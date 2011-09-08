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

import org.mobicents.servlet.sip.SipFactories;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsSipFactoriesImpl implements MobicentsSipFactories {

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactories#getAddressFactory()
	 */
	public AddressFactory getAddressFactory() {		
		return SipFactories.addressFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactories#getHeaderFactory()
	 */
	public HeaderFactory getHeaderFactory() {
		return SipFactories.headerFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactories#getMessageFactory()
	 */
	public MessageFactory getMessageFactory() {
		return SipFactories.messageFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactories#getSipFactory()
	 */
	public SipFactory getSipFactory() {
		return SipFactories.sipFactory;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.MobicentsSipFactories#initialize(java.lang.String, boolean)
	 */
	public void initialize(String sipPathName, boolean usePrettyEncoding) {
		SipFactories.initialize(sipPathName, usePrettyEncoding);
	}

}
