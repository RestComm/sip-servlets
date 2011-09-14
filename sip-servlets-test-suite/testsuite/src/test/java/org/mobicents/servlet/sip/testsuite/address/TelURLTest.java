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

import javax.servlet.sip.TelURL;

import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Tests for creating telUrl
 */
public class TelURLTest extends junit.framework.TestCase {

	static String[][] equal = {
			{"tel:+358-555-1234567;postd=pp22", "tel:+358-555-1234567;POSTD=PP22"},
			//they should be equivalent but jain sip performs a string comparison
//			{"tel:+358-555-1234567;postd=pp22;isub=1411", "tel:+358-555-1234567;isub=1411;postd=pp22"}
	};
	
	static String[][] different = {
			{"tel:+358-555-1234567;postd=pp23@foo.com;user=phone", "tel:+358-555-1234567;POSTD=PP22@foo.com;user=phone"}
	};
	
	private SipFactoryImpl sipFactory;
	
	public void setUp() {
		sipFactory = new SipFactoryImpl();
		sipFactory.initialize("gov.nist", true);
	}
	
	private TelURL telUrl(String uri) throws Exception {
		return (TelURL) sipFactory.createURI(uri);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			TelURL uri1 = telUrl(equal[i][0]);
			TelURL uri2 = telUrl(equal[i][1]);
			assertTrue(uri1 + " is different as " + uri2, uri1.equals(uri2));
			assertTrue(uri2 + " is different as " + uri1, uri2.equals(uri1));
		}		
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			TelURL uri1 = telUrl(different[i][0]);
			TelURL uri2 = telUrl(different[i][1]);
			assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
			assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
		}
	}
}