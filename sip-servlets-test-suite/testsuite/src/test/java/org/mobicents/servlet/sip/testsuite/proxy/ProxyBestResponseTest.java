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

package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.HashMap;
import java.util.Map;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyBestResponseTest extends SipServletTestCase {	
	private static final String SESSION_EXPIRED = "sessionExpired";
	private static final String SESSION_READY_TO_INVALIDATE = "sessionReadyToInvalidate";
	private static final String SIP_SESSION_READY_TO_INVALIDATE = "sipSessionReadyToInvalidate";
	private static transient Logger logger = Logger.getLogger(ProxyBestResponseTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener neutral;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	ProtocolObjects neutralProto;


	private static final int TIMEOUT = 20000;

	public ProxyBestResponseTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
                containerPort = NetworkPortAssigner.retrieveNextPort();
		super.setUp();
        }
        
        public void setupPhones() throws Exception {
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		neutralProto = new ProtocolObjects("neutral",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
                
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
                //use cutme port 5056 to receive second invite in second branch
                int neutralPort = NetworkPortAssigner.retrieveNextPort();
		neutral = new TestSipListener(neutralPort, containerPort, neutralProto, false);
		neutral.setRecordRoutingProxyTesting(true);
		SipProvider neutralProvider = neutral.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		neutralProvider.addSipListener(neutral);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
		neutralProto.start();
                
                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort)); 
                params.put( "testPort", String.valueOf(senderPort)); 
                params.put( "receiverPort", String.valueOf(receiverPort));
                params.put( "cutmePort", String.valueOf(neutralPort));                
                deployApplication(projectHome + 
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", 
                        params, null);                 
	}


	/**
         * non-regression test for https://telestax.desk.com/agent/case/1805
	 */
	public void testProxyBestRes() throws Exception {
		setupPhones();
		String fromName = "sequential";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setFinalResponseToSend(Response.SERVER_TIMEOUT);
                neutral.setFinalResponseToSend(Response.BUSY_HERE);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);		
		assertEquals(Response.BUSY_HERE, sender.getFinalResponseStatus());	
	}       
        
	/**
         * non-regression test for https://telestax.desk.com/agent/case/1805
	 */
	public void testProxyBestRes6XXwins() throws Exception {
		setupPhones();
		String fromName = "sequential";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setFinalResponseToSend(Response.DECLINE);
                neutral.setFinalResponseToSend(Response.BUSY_HERE);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);		
		assertEquals(Response.DECLINE, sender.getFinalResponseStatus());	
	}        
        
	

	@Override
	public void tearDown() throws Exception {
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();		
		neutralProto.destroy();
		logger.info("Test completed");
		super.tearDown();
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
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}	
}
