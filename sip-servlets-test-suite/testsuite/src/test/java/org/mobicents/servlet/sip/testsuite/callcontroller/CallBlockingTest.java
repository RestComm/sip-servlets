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

package org.mobicents.servlet.sip.testsuite.callcontroller;

import java.util.Properties;

import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipUnitServletTestCase;

public class CallBlockingTest extends SipUnitServletTestCase {

	private static transient Logger logger = Logger.getLogger(CallBlockingTest.class);

	private SipStack sipStackSender;
	private SipPhone sipPhoneSender;	

	private static final int TIMEOUT = 20000;	

	public CallBlockingTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {		
		sipPhoneSender.dispose();		
		sipStackSender.dispose();		
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-blocking-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-blocking-servlet-dar.properties";
	}

	public SipStack makeStack(String transport, int port) throws Exception {
		Properties properties = new Properties();
		String peerHostPort1 = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ "udp");
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + transport + "_"
				+ port);
		properties.setProperty("sipunit.BINDADDR", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/simplesipservlettest_debug_port" + port + ".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/simplesipservlettest_log_port" + port + ".txt");
		
		return new SipStack(transport, port, properties);		
	}

	public void setupPhone() throws Exception {
			sipStackSender = makeStack(SipStack.PROTOCOL_UDP, 5080);					
			sipPhoneSender = sipStackSender.createSipPhone("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "",
					SipStack.PROTOCOL_UDP, 5070, "sip:blocked-sender@sip-servlets.com");		
	}

	public void init() throws Exception {
		setupPhone();
	}

	// Check if we receive a FORBIDDEN response for our invite
	public void testCallBlockingInvite() throws Exception {
		init();
		SipCall sender = sipPhoneSender.createSipCall();
		assertTrue(sender.initiateOutgoingCall("sip:receiver@sip-servlets.com", null));
		assertTrue(sender.waitOutgoingCallResponse(TIMEOUT));	
		assertResponseReceived(Response.FORBIDDEN, sender);
	}
}
