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

import javax.servlet.sip.Address;

import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Tests from RFC3261 §19.1.4 URI Comparison
 */
public class AddressTest extends junit.framework.TestCase {

	static String[][] equal = {
			{"\"Alice\" <sip:%61lice@bea.com;transport=TCP;lr>;q=0.6;expires=3601", "\"Alice02\" <sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"},
			{"<sip:%61lice@bea.com;transport=TCP;lr>;expires=3601;q=0.6", " <sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"},
			{"<sip:%61lice@bea.com;transport=TCP;lr>;q=0.6", "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"}
	};
	
	static String[][] different = {
			{"<sip:%61lice@bea.com;transport=TCP;lr>;q=0.5", "<sip:alice@BeA.CoM;Transport=tcp;lr>;q=0.6;expires=3601"},
	};
	
	private SipFactoryImpl sipFactory;
	
	public void setUp() {
		sipFactory = new SipFactoryImpl();
		sipFactory.initialize("gov.nist", true);
	}
	
	private Address address(String address) throws Exception {
		return (Address) sipFactory.createAddress(address);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			Address uri1 = address(equal[i][0]);
			Address uri2 = address(equal[i][1]);
			assertTrue(uri1 + " is different as " + uri2, uri1.equals(uri2));
			assertTrue(uri2 + " is different as " + uri1, uri2.equals(uri1));
		}		
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			Address uri1 = address(different[i][0]);
			Address uri2 = address(different[i][1]);
			assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
			assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
		}
	}
}