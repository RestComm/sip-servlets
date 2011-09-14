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

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * Tests from RFC3261 §19.1.4 URI Comparison
 */
public class SipURITest extends junit.framework.TestCase {

	static String[][] equal = {
			{"sip:%61lice@atlanta.com;transport=TCP", "sip:alice@AtlanTa.CoM;Transport=tcp"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;newparam=5"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;lr"},
			{"sip:carol@chicago.com;security=on", "sip:carol@chicago.com;newparam=5"},
			{"sip:alice@atlanta.com?subject=project%20x&priority=urgent", "sip:alice@atlanta.com?priority=urgent&subject=project%20x"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com;security=on"},
			{"sip:carol@chicago.com;security=on", "sip:carol@chicago.com"},
			{"sip:[0:0:0:0:0:0:0:1%1]:5070;transport=udp", "sip:[0:0:0:0:0:0:0:1%1]:5070;transport=udp"}
	};
	
	static String[][] different = {
			{"sip:alice@atlanta.com", "sip:ALICE@atlanta.com"},
			{"sip:bob@biloxi.com", "sip:bob@biloxi.com:5060"},
			{"sip:bob@biloxi.com", "sip:bob@biloxi.com;transport=tcp"},
			{"sip:carol@chicago.com;newparam=6", "sip:carol@chicago.com;newparam=5"},
			{"sip:carol@chicago.com", "sip:carol@chicago.com?Subject=next%20meeting"},
			{"sip:carol@chicago.com?Subject=next%20meeting", "sip:carol@chicago.com?Subject=another%20meeting"},
			{"sip:carol@chicago.com;security=off", "sip:carol@chicago.com;security=on"}
	};
	
	private SipFactoryImpl sipFactory;
	
	public void setUp() {		
		sipFactory = new SipFactoryImpl();
		sipFactory.initialize("gov.nist", true);
	}
	
	private SipURI sipUri(String uri) throws Exception {
		return (SipURI) sipFactory.createURI(uri);
	}
	
	public void testEqual() throws Exception {
		for (int i = 0; i < equal.length; i++) {
			SipURI uri1 = sipUri(equal[i][0]);
			SipURI uri2 = sipUri(equal[i][1]);
			assertTrue(uri1 + " is different as " + uri2, uri1.equals(uri2));
			assertTrue(uri2 + " is different as " + uri1, uri2.equals(uri1));
		}		
	}
	
	public void testDifferent() throws Exception {
		for (int i = 0; i < different.length; i++) {
			SipURI uri1 = sipUri(different[i][0]);
			SipURI uri2 = sipUri(different[i][1]);
			assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
			assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
		}
	}
	
	public void testEscaping() throws Exception {
		SipURI uri1 = sipUri("sip:alice@atlanta.com;transport=TCP?Subject=SIP%20Servlets");
		assertTrue(uri1.getUser() + " is different as alice" , uri1.getUser().equals("alice"));
		assertTrue(uri1.toString() + " is different as sip:alice@atlanta.com;transport=TCP?Subject=SIP%20Servlets" , uri1.toString().equals("sip:alice@atlanta.com;transport=TCP?Subject=SIP%20Servlets"));		
		uri1 = sipUri("sip:alice@example.com;transport=tcp?Subject=SIP%20Servlets");
		assertTrue(uri1.getHeader("Subject") + " is different as SIP Servlets" , uri1.getHeader("Subject").equals("SIP Servlets"));		
		assertTrue(uri1.toString() + " is different as sip:alice@example.com;transport=tcp?Subject=SIP%20Servlets" , uri1.toString().equals("sip:alice@example.com;transport=tcp?Subject=SIP%20Servlets"));
//		uri1 = sipUri("sip:annc@ms.example.net;play=file://fs.example.net//clips/my-intro.dvi;content-type=video/mpeg%3bencode%3d314M-25/625-50");
//		SipURI uri2 = sipUri("sip:annc@ms.example.net;play=file://fs.example.net//clips/my-intro.dvi;content-type=video/mpeg;encode=314M-25/625-50");		
//		assertTrue(uri1.getParameter("content-type") + " is different as video/mpeg;encode=314M-25/625-50" , uri1.getParameter("content-type").equals("video/mpeg;encode=314M-25/625-50"));
//		assertTrue(uri1.toString() + " is different as sip:annc@ms.example.net;play=file://fs.example.net//clips/my-intro.dvi;content-type=video/mpeg%3bencode%3d314M-25/625-50" , uri1.equals(uri2));
	}	
	
	public void testNullUser() throws Exception {
		SipURI uri1 = sipUri("sip:atlanta.com;transport=TCP?Subject=SIP%20Servlets");
		assertNotNull(uri1);
		assertNull(uri1.getUser());
		assertNotNull(uri1.getHost());
		assertEquals("TCP", uri1.getTransportParam());
		assertTrue(uri1.getHeader("Subject") + " is different as SIP Servlets" , uri1.getHeader("Subject").equals("SIP Servlets"));
	}
	
	public void testParams() throws Exception {
		URI uri = sipFactory.createURI("sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
		uri.setParameter("Key", "val");
		String s = uri.toString();
		assertEquals("sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;Key=val", s);
	}
	
	public void testBrackets() throws Exception {
		try {
			String uriString = "<sip:1004@172.16.0.99;user=phone>";
			sipFactory.createURI(uriString);
			fail(uriString + " should throw a ServletParseException because the angle brackets are not allowed");
		} catch (ServletParseException e) {
		}				
	}
}