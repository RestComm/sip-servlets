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

package org.mobicents.servlet.sip.testsuite.address;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;

import org.mobicents.servlet.sip.message.SipFactoryImpl;

public class UrnSchemeTest extends junit.framework.TestCase {

	//Non regression test for issue 2289
	//https://code.google.com/p/mobicents/issues/detail?id=2289
	
	private SipFactoryImpl sipFactory;
	private javax.sip.address.AddressFactory jainAddressFactory;
	private String urn = "urn:service:sos";
	private String scheme = "urn";
	
	public void setUp() {		
		sipFactory = new SipFactoryImpl();
		sipFactory.initialize("gov.nist", true);

		try {
			jainAddressFactory = SipFactory.getInstance().createAddressFactory();
		} catch (PeerUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void testJainAddress() throws Exception {
		javax.sip.address.Address urnJainAddr = jainAddressFactory.createAddress(urn); 
		assertTrue(urn.equalsIgnoreCase(urnJainAddr.getURI().toString()));
		assertTrue(scheme.equals(urnJainAddr.getURI().getScheme()));
	}
	
	public void testMobicentsAddress() throws Exception {
		javax.servlet.sip.Address urnMobicentsAddr = sipFactory.createAddress(urn);
		assertTrue(urn.equals(urnMobicentsAddr.getURI().toString()));
		assertTrue(scheme.equals(urnMobicentsAddr.getURI().getScheme()));
	}

}
