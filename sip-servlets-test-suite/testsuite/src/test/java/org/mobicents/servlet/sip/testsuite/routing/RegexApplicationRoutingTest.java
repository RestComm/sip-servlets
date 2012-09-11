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

package org.mobicents.servlet.sip.testsuite.routing;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;
import org.mobicents.servlet.sip.testsuite.simple.ShootmeSipServletTest;

/**
 * This test starts 1 sip servlets container 
 * on port 5070 that has 2 services Shootme and Shootist.
 * 
 * Then the shootist sends an INVITE that goes to Shootme.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class RegexApplicationRoutingTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ShootmeSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000;
	
	
	TestSipListener sender;
	TestSipListener registerReciever;
	
	ProtocolObjects senderProtocolObjects;	
	ProtocolObjects registerRecieverProtocolObjects;	
	
	boolean ok = true;
	
	public RegexApplicationRoutingTest(String name) {
		super(name);
		createTomcatOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		if(ok) {
			return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/routing/regex-ok-sip-servlet-dar.properties";
		} else {
			return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/routing/regex-ko-sip-servlet-dar.properties";
		}
	}
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		
		registerRecieverProtocolObjects =new ProtocolObjects(
				"registerReciever", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		
		registerReciever = new TestSipListener(5058, 5070, registerRecieverProtocolObjects, true);
		SipProvider registerRecieverProvider = registerReciever.createProvider();			
		
		registerRecieverProvider.addSipListener(registerReciever);
		
		registerRecieverProtocolObjects.start();		
	}
	
	public void testRegexMatching() throws Exception {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		createTomcat();
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testRegexNotMatching() throws Exception {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		ok = false;
		createTomcat();
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"additionalParameterableHeader","nonParameterableHeader"}, new String[] {"none","none"}, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(404,sender.getFinalResponseStatus());
	}	

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		registerRecieverProtocolObjects.destroy();
		tomcat.stopTomcat();
		logger.info("Test completed");
		super.tearDown();
	}


}
