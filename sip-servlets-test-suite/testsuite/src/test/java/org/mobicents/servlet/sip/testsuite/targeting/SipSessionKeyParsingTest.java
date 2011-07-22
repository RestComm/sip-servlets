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

package org.mobicents.servlet.sip.testsuite.targeting;

import gov.nist.javax.sip.Utils;

import java.text.ParseException;
import java.util.Random;
import java.util.UUID;

import junit.framework.TestCase;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * Non Regression Test for Issue 2423
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipSessionKeyParsingTest extends TestCase {

	/**
	 * @param name
	 */
	public SipSessionKeyParsingTest(String name) {
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

	
	public void testSipSessionKeyAndParsing() throws ParseException {
		SipSessionKey sipSessionKey = new SipSessionKey("" + new Random().nextInt(10000000), null,  Utils.getInstance().generateCallIdentifier("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ""), "" + UUID.randomUUID(), "ApplicationNameTest");
		String sipSessionKeyStringified = sipSessionKey.toString();
		
		SipSessionKey sipSessionKeyParsed =
			SessionManagerUtil.parseSipSessionKey(sipSessionKeyStringified);
		System.out.println(sipSessionKey);
		System.out.println(sipSessionKeyParsed);
		assertEquals(sipSessionKey, sipSessionKeyParsed);
		assertEquals(sipSessionKey.toString(), sipSessionKeyParsed.toString());		
	}
	
	public void testSipSessionKeyToTagSet() throws ParseException {
		SipSessionKey sipSessionKeyNoToTag = new SipSessionKey("" + new Random().nextInt(10000000), null,  Utils.getInstance().generateCallIdentifier("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ""), "" + UUID.randomUUID(), "ApplicationNameTest");
		
		SipSessionKey sipSessionKeyDifferentFromTagAndToTag = new SipSessionKey("" + new Random().nextInt(10000000), "" + new Random().nextInt(10000000),  Utils.getInstance().generateCallIdentifier("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ""), "" + UUID.randomUUID(), "ApplicationNameTest");
		
		SipSessionKey sipSessionKeySameFromTagAndDifferentToTag = new SipSessionKey(sipSessionKeyNoToTag.getFromTag(), "" + new Random().nextInt(10000000),  sipSessionKeyNoToTag.getCallId(), sipSessionKeyNoToTag.getApplicationSessionId(), "ApplicationNameTest");
		
		SipSessionKey sipSessionKeySameFromTagAndToTag = new SipSessionKey(sipSessionKeyNoToTag.getFromTag(), sipSessionKeySameFromTagAndDifferentToTag.getToTag(),  sipSessionKeyNoToTag.getCallId(), sipSessionKeyNoToTag.getApplicationSessionId(), "ApplicationNameTest");
		String sipSessionKeySameFromTagAndToTagStringified = sipSessionKeySameFromTagAndToTag.toString();
		
		System.out.println(sipSessionKeyNoToTag);
		System.out.println(sipSessionKeyDifferentFromTagAndToTag);
		System.out.println(sipSessionKeySameFromTagAndDifferentToTag);
		System.out.println(sipSessionKeySameFromTagAndToTag);
		
		SipSessionKey sipSessionKeyParsed =
			SessionManagerUtil.parseSipSessionKey(sipSessionKeySameFromTagAndToTagStringified);
		System.out.println(sipSessionKeyParsed);
		assertEquals(sipSessionKeySameFromTagAndToTag, sipSessionKeyParsed);
		assertEquals(sipSessionKeySameFromTagAndToTag.toString(), sipSessionKeyParsed.toString());		
				
		assertFalse(sipSessionKeyNoToTag.equals(sipSessionKeyDifferentFromTagAndToTag));
		assertTrue(sipSessionKeyNoToTag.equals(sipSessionKeySameFromTagAndDifferentToTag));
		assertTrue(sipSessionKeyNoToTag.equals(sipSessionKeySameFromTagAndToTag));
		assertTrue(sipSessionKeySameFromTagAndDifferentToTag.equals(sipSessionKeySameFromTagAndToTag));
				
	}
	
	public void testEquals() {
		EqualsVerifier.forClass(SipSessionKey.class).verify();
	}
}
