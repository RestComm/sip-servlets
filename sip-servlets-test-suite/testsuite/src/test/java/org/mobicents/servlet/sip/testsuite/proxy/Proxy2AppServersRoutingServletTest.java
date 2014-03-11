/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.testsuite.proxy;

import java.io.File;
import java.util.ListIterator;
import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ViaHeader;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.SipUnitServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class Proxy2AppServersRoutingServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(Proxy2AppServersRoutingServletTest.class);
	
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;
	
	SipEmbedded closestTomcat;
	
	TestSipListener sender;
	TestSipListener receiver;
	
	ProtocolObjects senderProtocolObjects;	
	ProtocolObjects recieverProtocolObjects;	
	
	public Proxy2AppServersRoutingServletTest(String name) {
		super(name);
		autoDeployOnStartup = false;
		startTomcatOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat
				.deployContext(
						projectHome
								+ "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
						"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/proxy-dar.properties";
	}
	
	protected String getDarConfigurationFileForClosestServer() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/proxy-dar.properties";
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();		
		System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", "org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider");
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		
		recieverProtocolObjects =new ProtocolObjects(
				"reciever", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		
		receiver = new TestSipListener(5057, 5070, recieverProtocolObjects, false);
		SipProvider registerRecieverProvider = receiver.createProvider();			
		
		registerRecieverProvider.addSipListener(receiver);
		
		recieverProtocolObjects.start();	
	}

	public SipStack makeStack(String transport, int port) throws Exception {
		Properties properties = new Properties();
		String peerHostPort1 = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5069";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ "udp");
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + transport + "_"
				+ port);
		properties.setProperty("sipunit.BINDADDR", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/callforwarding_debug_5069.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/callforwarding_server_5069.txt");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		
		return new SipStack(transport, port, properties);		
	}
	
	/* This test starts 2 sip servlets container 
	 * one on 5070 that has the proxy app to ROUTE to the other server on 5069
	 * one on port 5069 that has the proxy application to route to UAS.
	 * 
	 * Tests for number of Via Header in ACK to be correct 3 instead of 2
	 */
	public void testExternalRouting() throws Exception {
		//start the most remote server first
		tomcat.startTomcat();
		deployApplication();
		//create and start the closest server
		//starting tomcat
		closestTomcat = new SipEmbedded("SIP-Servlet-Closest-Tomcat-Server", serviceFullClassName);
		closestTomcat.setLoggingFilePath(  
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + closestTomcat.getLoggingFilePath());
		closestTomcat.setDarConfigurationFilePath(getDarConfigurationFileForClosestServer());
		closestTomcat.initTomcat(tomcatBasePath, null);						
		closestTomcat.addSipConnector("SIP-Servlet-Closest-Tomcat-Server", sipIpAddress, 5069, ListeningPoint.UDP);						
		closestTomcat.startTomcat();	
		closestTomcat.deployContext(
						projectHome
								+ "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
						"sip-test-context", "sip-test");
		Thread.sleep(TIMEOUT);
		
		String fromName = "test-unique-location-urn-uri1-push-route-app-server";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String r = "requestUri";
		String ra = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5057";
		SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
				r, ra);
		sender.setRecordRoutingProxyTesting(true);
		receiver.setRecordRoutingProxyTesting(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		logger.info("ack received "+ receiver.getAckRequest());
		assertNotNull(receiver.getAckRequest());		
		ListIterator<ViaHeader> viaHeaders = (ListIterator<ViaHeader>) receiver.getAckRequest().getHeaders(ViaHeader.NAME);
		int count = 0;
		while(viaHeaders.hasNext()) {
			count++;
			viaHeaders.next();			
		}
		assertEquals(3, count);
	}	

	@Override
	public void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		recieverProtocolObjects.destroy();		
		closestTomcat.stopTomcat();
		logger.info("Test completed");
		super.tearDown();
	}


}
