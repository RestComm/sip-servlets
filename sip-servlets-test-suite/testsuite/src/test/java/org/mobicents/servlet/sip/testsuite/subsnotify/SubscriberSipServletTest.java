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

package org.mobicents.servlet.sip.testsuite.subsnotify;
import gov.nist.javax.sip.header.SubscriptionState;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SubscriberSipServletTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(SubscriberSipServletTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	private static final String[] SUBSCRIPTION_STATES = new String[]{
		SubscriptionState.PENDING.toLowerCase(), SubscriptionState.ACTIVE.toLowerCase(), SubscriptionState.TERMINATED.toLowerCase() 
	};
	
	private static final String SESSION_INVALIDATED = new String("sipSessionReadyToBeInvalidated");	
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;
	
	public SubscriberSipServletTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/subscriber-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}
	
	public SipStandardContext deployApplication(String name, String value) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/subscriber-servlet/src/main/sipapp");
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

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/subsnotify/subscriber-sip-servlet-dar.properties";
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
	
	/*
	 * Test the fact that a sip servlet send a SUBSCRIBE (here 2 subscribes for different event ie subscriptions)
	 * and receive NOTIFYs. 
	 * Check that everything works correctly included the Sip Session Termination upon receiving a NOTIFY
	 * containing Subscription State of Terminated.
	 */
	public void testSipServletSendsSubscribe() throws InterruptedException {
		deployApplication();
//		receiver.sendInvite();
		Thread.sleep(TIMEOUT*2);
		assertEquals(6, receiver.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present", receiver.getAllSubscriptionState().contains(subscriptionState));	
		}				
		Thread.sleep(TIMEOUT);
		assertEquals(1, receiver.getAllMessagesContent().size());
		assertTrue("session not invalidated after receiving Terminated Subscription State", receiver.getAllMessagesContent().contains(SESSION_INVALIDATED));
		
	}
	
	/*
	 * Issue 1123 : http://code.google.com/p/mobicents/issues/detail?id=1123
	 * Multipart type is not supported
	 */
	public void testSipServletsReceiveNotifyMultipart() throws Exception {
		deployApplication("testMultipart", "testMultipart");
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = receiverProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = receiverProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String content = "--50UBfW7LSCVLtggUPe5z\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <nXYxAE@pres.vancouver.example.com>\r\nContent-Type: application/rlmi+xml;charset=\"UTF-8\"\r\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<list xmlns=\"urn:ietf:params:xml:ns:rlmi\" uri=\"sip:adam-friends@pres.vancouver.example.com\" version=\"1\" fullState=\"true\">\n<name xml:lang=\"en\">Buddy List at COM</name>\n<name xml:lang=\"de\">Liste der Freunde an COM</name>\n<resource uri=\"sip:bob@vancouver.example.com\"\">\n<name>Bob Smith</name>\n<instance id=\"juwigmtboe\" state=\"active\" cid=\"bUZBsM@pres.vancouver.example.com\"/>\n</resource>\n<resource uri=\"sip:dave@vancouver.example.com\">\n<name>Dave Jones</name>\n<instance id=\"hqzsuxtfyq\" state=\"active\" cid=\"ZvSvkz@pres.vancouver.example.com\"/>\n</resource>\n<resource uri=\"sip:ed@dallas.example.net\">\n<name>Ed at NET</name>\n</resource>\n<resource uri=\"sip:adam-friends@stockholm.example.org\">\n<name xml:lang=\"en\">My Friends at ORG</name>\n<name xml:lang=\"de\">Meine Freunde an ORG</name>\n</resource>\n</list>\n\n--50UBfW7LSCVLtggUPe5z\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <bUZBsM@pres.vancouver.example.com>\r\nContent-Type: application/pidf+xml;charset=\"UTF-8\"\r\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:bob@vancouver.example.com\">\n<tuple id=\"sg89ae\">\n<status>\n<basic>open</basic>\n</status>\n<contact priority=\"1.0\">sip:bob@vancouver.example.com</contact>\n</tuple>\n</presence>\n\n--50UBfW7LSCVLtggUPe5z\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <ZvSvkz@pres.vancouver.example.com>\r\nContent-Type: application/pidf+xml;charset=\"UTF-8\"\r\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:dave@vancouver.example.com\">\n<tuple id=\"slie74\">\n<status>\n<basic>closed</basic>\n</status>\n</tuple>\n</presence>\n\n--50UBfW7LSCVLtggUPe5z--";
		
		receiver.sendSipRequest(Request.NOTIFY, fromAddress, toAddress, content, null, false, new String[] {ContentTypeHeader.NAME, SubscriptionStateHeader.NAME, EventHeader.NAME}, new String[]{"multipart/related;type=\"application/rlmi+xml\";start=\"<nXYxAE@pres.vancouver.example.com>\";boundary=\"50UBfW7LSCVLtggUPe5z\"", "pending", "presence"}, true);
		Thread.sleep(TIMEOUT);
		assertEquals(200, receiver.getFinalResponseStatus());
		assertTrue(receiver.getAllMessagesContent().size() > 0);
		assertTrue(receiver.getAllMessagesContent().contains("3"));
	}
	
	/*
	 * Issue 1123 : http://code.google.com/p/mobicents/issues/detail?id=1123
	 * Multipart type is not supported
	 */
	public void testSipServletsReceiveSendNotifyMultipart() throws Exception {
		deployApplication("testSendMultipart", "testSendMultipart");
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = receiverProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = receiverProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String content = "--50UBfW7LSCVLtggUPe5z\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <nXYxAE@pres.vancouver.example.com>\r\nContent-Type: application/rlmi+xml;charset=\"UTF-8\"\r\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<list xmlns=\"urn:ietf:params:xml:ns:rlmi\" uri=\"sip:adam-friends@pres.vancouver.example.com\" version=\"1\" fullState=\"true\">\n<name xml:lang=\"en\">Buddy List at COM</name>\n<name xml:lang=\"de\">Liste der Freunde an COM</name>\n<resource uri=\"sip:bob@vancouver.example.com\"\">\n<name>Bob Smith</name>\n<instance id=\"juwigmtboe\" state=\"active\" cid=\"bUZBsM@pres.vancouver.example.com\"/>\n</resource>\n<resource uri=\"sip:dave@vancouver.example.com\">\n<name>Dave Jones</name>\n<instance id=\"hqzsuxtfyq\" state=\"active\" cid=\"ZvSvkz@pres.vancouver.example.com\"/>\n</resource>\n<resource uri=\"sip:ed@dallas.example.net\">\n<name>Ed at NET</name>\n</resource>\n<resource uri=\"sip:adam-friends@stockholm.example.org\">\n<name xml:lang=\"en\">My Friends at ORG</name>\n<name xml:lang=\"de\">Meine Freunde an ORG</name>\n</resource>\n</list>\n\n--50UBfW7LSCVLtggUPe5z\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <bUZBsM@pres.vancouver.example.com>\r\nContent-Type: application/pidf+xml;charset=\"UTF-8\"\r\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:bob@vancouver.example.com\">\n<tuple id=\"sg89ae\">\n<status>\n<basic>open</basic>\n</status>\n<contact priority=\"1.0\">sip:bob@vancouver.example.com</contact>\n</tuple>\n</presence>\n\n--50UBfW7LSCVLtggUPe5z\r\nContent-Transfer-Encoding: binary\r\nContent-ID: <ZvSvkz@pres.vancouver.example.com>\r\nContent-Type: application/pidf+xml;charset=\"UTF-8\"\r\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" entity=\"sip:dave@vancouver.example.com\">\n<tuple id=\"slie74\">\n<status>\n<basic>closed</basic>\n</status>\n</tuple>\n</presence>\n\n--50UBfW7LSCVLtggUPe5z--";
		
		receiver.sendSipRequest(Request.NOTIFY, fromAddress, toAddress, content, null, false, new String[] {ContentTypeHeader.NAME, SubscriptionStateHeader.NAME, EventHeader.NAME}, new String[]{"multipart/related;type=\"application/rlmi+xml\";start=\"<nXYxAE@pres.vancouver.example.com>\";boundary=\"50UBfW7LSCVLtggUPe5z\"", "pending", "presence"}, true);
		Thread.sleep(TIMEOUT);
		assertEquals(200, receiver.getFinalResponseStatus());
		assertTrue(receiver.getAllMessagesContent().size() > 0);
		assertTrue(receiver.getAllMessagesContent().contains("3"));
	}
	
	// http://code.google.com/p/mobicents/issues/detail?id=2182
	public void testSipServletsReceiveNotifyMimeMultipartWhitespace() throws Exception {
		deployApplication("testMimeMultipartWhitespaces", "testMimeMultipartWhitespaces");
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = receiverProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = receiverProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String content = "--1A713B7EE42ABF467A11E416\r\n" + "Content-Type: application/pidf+xml\r\n"
		+ "Content-ID: <317A166E4BEB17EA1A24F417@telecomsys.com>\r\n" + "\r\n"
		+ "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"\r\n"
		+ "xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"\r\n"
		+ "xmlns:ca=\"urn:ietf:params:xml:ns:pidf:geopriv10:civicAddr\"\r\n"
		+ "xmlns:gml=\"http://www.opengis.net/gml\"\r\n" + "entity=\"pres:www.telecomsys.com\">\r\n"
		+ "<tuple id=\"TCS_SLDB\">\r\n" + "<status>\r\n" + "<gp:geopriv>\r\n" + "<gp:location-info>\r\n"
		+ "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"urn:ogc:def:crs:EPSG::4326\">\r\n"
		+ "<gml:pos>30.8686739957541 -97.0195399766783</gml:pos>\r\n" + "</gml:Point>\r\n"
		+ "</gp:location-info>\r\n" + "<gp:usage-rules>\r\n"
		+ "<gp:retransmission-allowed>yes</gp:retransmission-allowed>\r\n" + "</gp:usage-rules>\r\n"
		+ "<gp:method>Derived</gp:method>\r\n" + "<gp:provided-by>www.telecomsys.com</gp:provided-by>\r\n"
		+ "</gp:geopriv>\r\n" + "</status>\r\n" + "<timestamp>2010-05-17T17:20:55Z</timestamp>\r\n"
		+ "</tuple>\r\n" + "</presence>\r\n" + "--1A713B7EE42ABF467A11E416\r\n"
		+ "Content-Type: application/sdp\r\n" + "\r\n" + "v=0\r\n"
		+ "o=user1 53655765 2353687637 IN IP4 10.15.90.101\r\n" + "s=-\r\n" + "c=IN IP4 10.15.90.101\r\n"
		+ "t=0 0\r\n" + "m=audio 6001 RTP/AVP 0\r\n" + "a=rtpmap:0 PCMU/8000\r\n";
		receiver.sendSipRequest(Request.NOTIFY, fromAddress, toAddress, content, null, false, new String[] {ContentTypeHeader.NAME, SubscriptionStateHeader.NAME, EventHeader.NAME}, new String[]{"multipart/related;type=\"application/rlmi+xml\";boundary=\"1A713B7EE42ABF467A11E416\"", "pending", "presence"}, true);
		Thread.sleep(TIMEOUT);
		assertEquals(200, receiver.getFinalResponseStatus());
		assertTrue(receiver.getAllMessagesContent().size() > 0);
		assertTrue(receiver.getAllMessagesContent().contains("2"));
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}
}