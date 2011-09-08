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
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Request;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test is aimed to test the Chapter "15.11.3 The Encode URI Mechanism" compliancy of JSR 289
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class EncodeURISipServletTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(EncodeURISipServletTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;
	
	public EncodeURISipServletTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
		initTomcatOnStartup = false;
		addSipConnectorOnStartup = false;
	}

	@Override
	public void deployApplication() {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("encodeRequestURI");
		applicationParameter.setValue("true");
		context.addApplicationParameter(applicationParameter);		
		assertTrue(tomcat.deployContext(context));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/targeting/encode-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();			
	}
	
	/**
	 * This test gets an INVITE with an encoded URI, it extracts it and send it back in a new INVITE not related to the same dialog
	 * to make sure the same application session is used 
	 * @throws Exception
	 */
	public void testEncodeURI() throws Exception {
		tomcat.initTomcat(tomcatBasePath, null);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		Request invite = receiver.getInviteRequest();
		
		String fromName = "encodedUri";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = receiverProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "mss";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = receiverProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		SipURI requestURI = (SipURI) toAddress.clone();
		requestURI.setParameter("org.mobicents.servlet.sip.ApplicationSessionKey", ((SipURI)invite.getRequestURI()).getParameter("org.mobicents.servlet.sip.ApplicationSessionKey"));
		
		receiver.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestURI);		
		Thread.sleep(TIMEOUT);
		assertEquals(200, receiver.getFinalResponseStatus());			
	}		

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}
}