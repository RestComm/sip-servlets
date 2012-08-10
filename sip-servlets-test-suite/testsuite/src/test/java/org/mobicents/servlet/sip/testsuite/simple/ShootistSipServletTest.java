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
package org.mobicents.servlet.sip.testsuite.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootistSipServletTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ShootistSipServletTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
	private static final int DIALOG_TIMEOUT = 40000;
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;
	
	public ShootistSipServletTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}
	
	public SipStandardContext deployApplication(String name, String value) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		context.setConcurrencyControlMode(ConcurrencyControlMode.SipApplicationSession);
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName(name);
		applicationParameter.setValue(value);
		context.addApplicationParameter(applicationParameter);
		assertTrue(tomcat.deployContext(context));
		return context;
	}
	
	public SipStandardContext deployApplication(Map<String, String> params) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setConcurrencyControlMode(ConcurrencyControlMode.SipApplicationSession);
		context.setManager(new SipStandardManager());
		for (Entry<String, String> param : params.entrySet()) {
			ApplicationParameter applicationParameter = new ApplicationParameter();
			applicationParameter.setName(param.getKey());
			applicationParameter.setValue(param.getValue());
			context.addApplicationParameter(applicationParameter);
		}
		assertTrue(tomcat.deployContext(context));
		return context;
	}
	

	public SipStandardContext deployApplication(Map<String, String> params,ConcurrencyControlMode concurrencyControlMode) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		if(concurrencyControlMode != null) {
			context.setConcurrencyControlMode(concurrencyControlMode);
		}
		for (Entry<String, String> param : params.entrySet()) {
			ApplicationParameter applicationParameter = new ApplicationParameter();
			applicationParameter.setName(param.getKey());
			applicationParameter.setValue(param.getValue());
			context.addApplicationParameter(applicationParameter);
		}
		assertTrue(tomcat.deployContext(context));
		return context;
	}
	
	public SipStandardContext deployApplicationServletListenerTest() {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("testServletListener");
		applicationParameter.setValue("true");
		context.addApplicationParameter(applicationParameter);
		assertTrue(tomcat.deployContext(context));
		return context;
	}	

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {

        System.setProperty( "javax.net.ssl.keyStore",  ShootistSipServletTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.trustStore", ShootistSipServletTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
		super.setUp();												
	}
	
	public void testShootist() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
	}
	// Also Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693
	public void testShootistCancel() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setWaitForCancel(true);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("cancel", "true");
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		assertTrue(receiver.isCancelReceived());	
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertTrue(allMessagesContent.size() >= 2);
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}
	
	public void testShootistCancelServletTimerCancelConcurrency() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setWaitForCancel(true);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("cancel", "true");
		params.put("servletTimer", "500");
		deployApplication(params, ConcurrencyControlMode.SipApplicationSession);
		
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		assertTrue(receiver.isCancelReceived());	
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));		
	}
	
	/*
	 * Non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2450
	 */	
	public void testShootistCancelServletTimerConcurrency() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setWaitForCancel(true);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("cancel", "true");
		params.put("servletTimer", "0");
		deployApplication(params, ConcurrencyControlMode.SipApplicationSession);
		
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		assertTrue(receiver.isCancelReceived());	
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));		
	}
	
	public void testShootistEarlyMediaChange() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		LinkedList<Integer> responses = new LinkedList<Integer>();
		responses.add(180);
		responses.add(183);
		responses.add(183);
		responses.add(183);
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);

		receiver.setProvisionalResponsesToSend(responses);
		receiver.setWaitForCancel(true);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("cancel", "true");
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		assertTrue(receiver.isCancelReceived());	
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		
		assertTrue("earlyMedia", receiver.getMessageRequest().getHeader("EarlyMediaResponses").toString().contains("3"));
		assertTrue(allMessagesContent.size() >= 2);
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}
	
	public void testShootistSetContact() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue((receiver.getInviteRequest().getHeader("Contact").toString().contains("uriparam=urivalue")));
		assertTrue((receiver.getInviteRequest().getHeader("Contact").toString().contains("headerparam1=headervalue1")));
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 676 http://code.google.com/p/mobicents/issues/detail?id=676
	 *  Tags not removed when using SipFactory.createRequest() 
	 */
	public void testShootistSetToTag() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("toTag", "callernwPort1241042500479");
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 732 http://code.google.com/p/mobicents/issues/detail?id=732
	 * duplicate parameters when using sipFactory.createAddress(uri) with a uri having parameters
	 */
	public void testShootistSetToParam() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("toParam", "http://yaris.research.att.com:23280/vxml/test.jsp");
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 1105 http://code.google.com/p/mobicents/issues/detail?id=1105
	 * sipFactory.createRequest(sipApplicationSession, "METHOD", fromString, toString) function does not handle URI parameters properly 
	 */
	public void testShootistSetToWithParam() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		String userName = "sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist";
		params.put("username", userName);
		params.put("useStringFactory", "true");
		deployApplication(params);
		Thread.sleep(TIMEOUT);
		ToHeader toHeader = (ToHeader) receiver.getInviteRequest().getHeader(ToHeader.NAME);
		assertEquals("To: <sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist>", toHeader.toString().trim());
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 145 http://code.google.com/p/sipservlets/issues/detail?id=145
	 */
	public void testShootistUserNameNull() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		String userName = "nullTest";
		params.put("username", userName);
		deployApplication(params);
		Thread.sleep(TIMEOUT);
		FromHeader fromHeader = (FromHeader) receiver.getInviteRequest().getHeader(FromHeader.NAME);
		assertEquals("sip:here.com", fromHeader.getAddress().getURI().toString().trim());
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 755 http://code.google.com/p/mobicents/issues/detail?id=755
	 * SipURI parameters not escaped
	 */
	public void testShootistSetEscapedParam() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		String toParamValue = "http://yaris.research.att.com:23280/vxml/test.jsp?toto=tata";
		deployApplication("toParam", toParamValue);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
		ToHeader toHeader = (ToHeader) receiver.getInviteRequest().getHeader(ToHeader.NAME);
		String toParam = ((SipURI)toHeader.getAddress().getURI()).getParameter("toParam");
		logger.info(toParam);
		assertEquals(toParamValue , RFC2396UrlDecoder.decode(toParam));
	}
	
	/**
	 * non regression test for Issue 859 http://code.google.com/p/mobicents/issues/detail?id=859
	 * JAIN SIP ACK Creation not interoperable with Microsoft OCS 
	 */
	public void testJainSipAckCreationViaParams() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setTestAckViaParam(true);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		tomcat.startTomcat();		
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 1025 http://code.google.com/p/mobicents/issues/detail?id=1025
	 * sipservletlistner called twice on redeploy
	 */
	public void testShootistSipServletListener() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		SipStandardContext context = deployApplicationServletListenerTest();
		Thread.sleep(TIMEOUT);
		tomcat.undeployContext(context);		
		Thread.sleep(TIMEOUT);
		context = deployApplicationServletListenerTest();
		Thread.sleep(TIMEOUT);
		assertEquals(2, receiver.getAllMessagesContent().size());
		context.reload();
		Thread.sleep(TIMEOUT);
		assertEquals(3, receiver.getAllMessagesContent().size());
	}
	
	/**
	 * non regression test for Issue 1090 http://code.google.com/p/mobicents/issues/detail?id=1090
	 * 	Content-Type is not mandatory if Content-Length is 0
	 */
	public void testShootistContentLength() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("testContentLength", "testContentLength");
		Thread.sleep(TIMEOUT);
		assertNotNull(receiver.getMessageRequest());		
	}	
	
	
	public void testShootistCallerSendsBye() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, true);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());		
	}
	
	public void testShootistUserAgentHeader() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.removeConnector(sipConnector);
		tomcat.stopTomcat();
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-"
				+ sipIpAddress + "-" + 5070);
		sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
				"off");
		sipStackProperties.setProperty(
				"gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE",
				"64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",
				"true");
		sipStackProperties.setProperty("org.mobicents.servlet.sip.USER_AGENT_HEADER",
			"MobicentsSipServletsUserAgent");
		tomcat.getSipService().setSipStackPropertiesFile(null);
		tomcat.getSipService().setSipStackProperties(sipStackProperties);
		tomcat.getSipService().init();
		tomcat.restartTomcat();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());	
		Request invite = receiver.getInviteRequest();
		UserAgentHeader userAgentHeader = (UserAgentHeader) invite.getHeader(UserAgentHeader.NAME);
		assertNotNull(userAgentHeader);
		assertTrue(userAgentHeader.toString().contains("MobicentsSipServletsUserAgent"));
	}
	
	/**
	 * non regression test for Issue 1150 http://code.google.com/p/mobicents/issues/detail?id=1150
	 * 	Contact header contains "transport" parameter even when there are two connectors (UDP and TCP)
	 */
	public void testShootistContactTransport() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);				
		
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);	
		assertFalse(((SipURI)contactHeader.getAddress().getURI()).toString().contains("transport=udp"));
		String contact = contactHeader.getAddress().toString();
		assertTrue(contact.contains("BigGuy@"));
		assertTrue(contact.contains("from display"));
	}
	
	public void testShootistContactTlsTransport() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);				
		
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5072, ListeningPoint.TLS);
		tomcat.startTomcat();
		deployApplication("secureRURI", "true");
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);	
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("sips:"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("5072"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
		String viaString = receiver.getInviteRequest().getHeader(ViaHeader.NAME).toString();
		assertTrue(viaString.toLowerCase().contains("tls"));
		assertTrue(viaString.toLowerCase().contains("5072"));
	}
	
	/**
	 * non regression test for Issue 2269 http://code.google.com/p/mobicents/issues/detail?id=2269
	 * Wrong Contact header scheme URI in case TLS call with 'sip:' scheme
	 */
	public void testShootistContactNonSecureURITlsTransport() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);				
		
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5072, ListeningPoint.TLS);
		tomcat.startTomcat();
		deployApplication("transportRURI", "tls");
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);	
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("sip:"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("5072"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
		String viaString = receiver.getInviteRequest().getHeader(ViaHeader.NAME).toString();
		assertTrue(viaString.toLowerCase().contains("tls"));
		assertTrue(viaString.toLowerCase().contains("5072"));
	}
	
	/**
	 * non regression test for Issue 1150 http://code.google.com/p/mobicents/issues/detail?id=1150
	 * 	Contact header contains "transport" parameter even when there are two connectors (UDP and TCP)
	 */
	public void testShootistOutboundInterfaceTransport() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", ListeningPoint.TCP, AUTODIALOG, null, null, null);				
		
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
		tomcat.startTomcat();
		deployApplication("outboundInterface", "tcp");
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);	
		assertFalse(((SipURI)contactHeader.getAddress().getURI()).toString().contains("transport=udp"));
	}
	
	/**
	 * non regression test for Issue 1412 http://code.google.com/p/mobicents/issues/detail?id=1412
	 * Contact header is added to REGISTER request by container
	 */
	public void testShootistRegister() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("method", "REGISTER");
		Thread.sleep(TIMEOUT);		
		assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));		
	}
	
	/**
	 * non regression test for http://code.google.com/p/mobicents/issues/detail?id=2288
	 * SipServletRequest.send() throws IllegalStateException instead of IOException
	 */
	public void testShootistIOException() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("testIOException", "example.com");
		Thread.sleep(TIMEOUT);		
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertEquals(1,allMessagesContent.size());
		assertTrue("IOException not thrown", allMessagesContent.contains("IOException thrown"));		
	}
	
	/**
	 * non regression test for http://code.google.com/p/mobicents/issues/detail?id=2288
	 * SipServletRequest.send() throws IllegalStateException instead of IOException
	 */
	public void testShootistIOExceptionTransportChange() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, ListeningPoint.TCP);
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		params.put("transportRURI", "tcp");
		params.put("testIOException", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		deployApplication(params);
		Thread.sleep(TIMEOUT);		
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertEquals(1,allMessagesContent.size());
		assertTrue("IOException not thrown", allMessagesContent.contains("IOException thrown"));		
	}
	
	/**
	 * non regression test for Issue 2269 http://code.google.com/p/mobicents/issues/detail?id=2269
	 * Wrong Contact header scheme URI in case TLS call with request URI 'sip:' scheme and contact is uri is secure with "sips"
	 */
	public void testShootistRegisterContactNonSecureURITlsTransport() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);				
		
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5072, ListeningPoint.TLS);
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		params.put("transportRURI", "tls");
		params.put("method", "REGISTER");
		deployApplication(params);
		tomcat.startTomcat();
		Thread.sleep(TIMEOUT);
		ContactHeader contactHeader = (ContactHeader) receiver.getRegisterReceived().getHeader(ContactHeader.NAME);
		assertNotNull(contactHeader);	
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("sip:"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("5072"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
	}
	
	/**
	 * non regression test for Issue 1547 http://code.google.com/p/mobicents/issues/detail?id=1547
	 * Can't add a Proxy-Authorization using SipServletMessage.addHeader
	 */
	public void testShootistProxyAuthorization() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		params.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"");
		params.put("headerToAdd", "Proxy-Authorization");
		deployApplication(params);
		Thread.sleep(TIMEOUT);		
		assertNotNull(receiver.getInviteRequest().getHeader(ProxyAuthorizationHeader.NAME));		
		assertNotNull(receiver.getInviteRequest().getHeader(ProxyAuthenticateHeader.NAME));
	}
	
	/**
	 * non regression test for Issue 2798 http://code.google.com/p/mobicents/issues/detail?id=2798
	 * Can't add an Authorization using SipServletMessage.addHeader
	 */
	public void testShootistAuthorization() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		params.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"");
		params.put("headerToAdd", "Authorization");
		deployApplication(params);
		Thread.sleep(TIMEOUT);		
		assertNotNull(receiver.getInviteRequest().getHeader(AuthorizationHeader.NAME));				
	}
	
	// Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693
	public void testShootistErrorResponse() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
		receiver.setFinalResponseToSend(Response.SERVER_INTERNAL_ERROR);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();		
		deployApplication("testErrorResponse", "true");
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}
	
	// Tests Issue 143 http://code.google.com/p/mobicents/issues/detail?id=143
	public void testShootist422Response() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
		receiver.setFinalResponseToSend(422);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();		
		deployApplication("testErrorResponse", "true");
		Thread.sleep(TIMEOUT);				
		receiver.setFinalResponseToSend(200);
		Thread.sleep(TIMEOUT);				
		assertTrue(receiver.isAckReceived());
	}
		
		
	// Test for SS spec 11.1.6 transaction timeout notification
	// Test Issue 2580 http://code.google.com/p/mobicents/issues/detail?id=2580
	public void testTransactionTimeoutResponse() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setDropRequest(true);
		List<Integer> provisionalResponsesToSend = receiver.getProvisionalResponsesToSend();
		provisionalResponsesToSend.clear();
		
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		tomcat.startTomcat();
		deployApplication();
		
		Thread.sleep(DIALOG_TIMEOUT);
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
			
		}
		assertTrue(receiver.txTimeoutReceived);
	}	

	/*
	 * http://code.google.com/p/mobicents/issues/detail?id=2902
	 */
	public void testShootistRemoteAddrAndPort() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("testRemoteAddrAndPort", "true");
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
	}	

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}
}