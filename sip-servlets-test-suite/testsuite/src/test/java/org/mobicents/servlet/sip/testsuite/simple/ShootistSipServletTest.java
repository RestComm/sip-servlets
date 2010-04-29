/*
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.Request;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootistSipServletTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ShootistSipServletTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
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
		super.setUp();												
	}
	
	public void testShootist() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());		
	}
	
	public void testShootistSetContact() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		String userName = "sip:+34666666666@127.0.0.1:5080;pres-list=mylist";
		params.put("username", userName);
		params.put("useStringFactory", "true");
		deployApplication(params);
		Thread.sleep(TIMEOUT);
		ToHeader toHeader = (ToHeader) receiver.getInviteRequest().getHeader(ToHeader.NAME);
		assertEquals("To: <sip:+34666666666@127.0.0.1:5080;pres-list=mylist>", toHeader.toString().trim());
		assertTrue(receiver.getByeReceived());		
	}
	
	/**
	 * non regression test for Issue 755 http://code.google.com/p/mobicents/issues/detail?id=755
	 * SipURI parameters not escaped
	 */
	public void testShootistSetEscapedParam() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.removeConnector(sipConnector);
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
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport, sipStackProperties);
		tomcat.startTomcat();
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);				
		
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, "tcp", null);
		tomcat.startTomcat();
		deployApplication();
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
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		deployApplication("method", "REGISTER");
		Thread.sleep(TIMEOUT);		
		assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));		
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}
}