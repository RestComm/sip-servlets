/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.servlet.sip.testsuite.targeting;

import java.text.ParseException;

import junit.framework.TestCase;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;

/**
 * Non Regression Test for Issue 2423
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipApplicationSessionKeyParsingTest extends TestCase {

	/**
	 * @param name
	 */
	public SipApplicationSessionKeyParsingTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testSipApplicationSessionKeyAndParsing() throws ParseException {
		SipApplicationSessionKey sipApplicationSessionKey = new SipApplicationSessionKey(null, "ApplicationNameTest", null);
		String sipApplicationSessionKeyStringified = sipApplicationSessionKey.toString();
		
		SipApplicationSessionKey sipApplicationSessionKeyParsed =
			SessionManagerUtil.parseSipApplicationSessionKey(sipApplicationSessionKeyStringified);
		System.out.println(sipApplicationSessionKey);
		System.out.println(sipApplicationSessionKeyParsed);
		assertEquals(sipApplicationSessionKey, sipApplicationSessionKeyParsed);
		assertEquals(sipApplicationSessionKey.toString(), sipApplicationSessionKeyParsed.toString());		
	}
	
	public void testSipApplicationSessionKeyGeneratedAndParsing() throws ParseException {		
		String appGeneratedKey = "myGeneratedKey";
		SipApplicationSessionKey sipApplicationSessionKey = new SipApplicationSessionKey(null, "ApplicationNameTest", appGeneratedKey);
		String sipApplicationSessionKeyStringified = sipApplicationSessionKey.toString();
		
		SipApplicationSessionKey sipApplicationSessionKeyParsed =
			SessionManagerUtil.parseSipApplicationSessionKey(sipApplicationSessionKeyStringified);
		System.out.println(sipApplicationSessionKey);
		System.out.println(sipApplicationSessionKeyParsed);
		assertEquals(sipApplicationSessionKey, sipApplicationSessionKeyParsed);
		assertEquals(sipApplicationSessionKey.toString(), sipApplicationSessionKeyParsed.toString());		
		assertTrue(sipApplicationSessionKey.toString().startsWith(appGeneratedKey));
		
		SipApplicationSessionKey sipApplicationSessionKeyDifferentAppGeneratedKey = new SipApplicationSessionKey(sipApplicationSessionKeyParsed.getId(), "ApplicationNameTest", "myDifferentGeneratedKey");		
		System.out.println(sipApplicationSessionKeyDifferentAppGeneratedKey);				
		assertFalse(sipApplicationSessionKeyDifferentAppGeneratedKey.toString().equals(sipApplicationSessionKeyParsed.toString()));	
	}
	
	public void testEquals() {
		EqualsVerifier.forClass(SipApplicationSessionKey.class).verify();
	}
}
