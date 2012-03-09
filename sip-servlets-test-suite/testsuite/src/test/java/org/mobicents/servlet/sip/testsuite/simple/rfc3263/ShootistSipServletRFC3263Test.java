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

package org.mobicents.servlet.sip.testsuite.simple.rfc3263;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.header.ContactHeader;
import javax.sip.header.RouteHeader;
import javax.sip.message.Response;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSLookupPerformer;
import org.mobicents.ext.javax.sip.dns.DefaultDNSLookupPerformer;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;
import org.mobicents.servlet.sip.testsuite.simple.ShootistSipServletTest;
import org.xbill.DNS.DClass;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;

public class ShootistSipServletRFC3263Test extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ShootistSipServletRFC3263Test.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 80000;	
	private static final int DIALOG_TIMEOUT = 40000;
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener receiver;
	ProtocolObjects receiverProtocolObjects;
	
	TestSipListener badReceiver;
	ProtocolObjects badReceiverProtocolObjects;
	
	public ShootistSipServletRFC3263Test(String name) {
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
		 System.setProperty( "javax.net.ssl.keyStore",  ShootistSipServletTest.class.getResource("testkeys").getPath() );
	        System.setProperty( "javax.net.ssl.trustStore", ShootistSipServletTest.class.getResource("testkeys").getPath() );
	        System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
	        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
        super.setUp();		
	}
	
	/*
	 * Making sure the procedures of retrying the next hop of RFC 3263 are working
	 */
	public void testShootist() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		badReceiverProtocolObjects =new ProtocolObjects(
				"bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		badReceiver = new TestSipListener(5081, 5070, badReceiverProtocolObjects, false);
		SipProvider badReceiverProvider = badReceiver.createProvider();			
		badReceiverProvider.addSipListener(badReceiver);
		badReceiverProtocolObjects.start();
		badReceiver.setDropRequest(true);
		String host = "mobicents.org";
		
		tomcat.startTomcat();
		
		mockDNSLookup(host);		
		
		deployApplication("host", host);
		Thread.sleep(TIMEOUT);
		RouteHeader routeHeader = (RouteHeader) receiver.getInviteRequest().getHeader(RouteHeader.NAME);
		assertNull(routeHeader);
		assertFalse(badReceiver.getByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	/*
	 * Non Regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=3140
	 */
	public void testShootistTLSwithTLSConnector() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", ListeningPoint.TLS, AUTODIALOG, null, null, null);
		receiver = new TestSipListener(5081, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		badReceiverProtocolObjects =new ProtocolObjects(
				"bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		badReceiver = new TestSipListener(5081, 5070, badReceiverProtocolObjects, false);
		SipProvider badReceiverProvider = badReceiver.createProvider();			
		badReceiverProvider.addSipListener(badReceiver);
		badReceiverProtocolObjects.start();
		badReceiver.setDropRequest(true);
		String host = "mobicents.org";
		
		tomcat.startTomcat();
		tomcat.removeConnector(sipConnector);
		tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TLS);
		
		mockDNSLookup(host);		
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("method", "REGISTER");
		params.put("secureRURI", "true");
		deployApplication(params);
		
		Thread.sleep(TIMEOUT);
		RouteHeader routeHeader = (RouteHeader) receiver.getRegisterReceived().getHeader(RouteHeader.NAME);
		assertNull(routeHeader);
		assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));		
	}
	
	/*
	 * Non Regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=3140
	 */
	public void testShootistTLSwithTCPConnector() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", ListeningPoint.TCP, AUTODIALOG, null, null, null);
		receiver = new TestSipListener(5081, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		badReceiverProtocolObjects =new ProtocolObjects(
				"bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		badReceiver = new TestSipListener(5081, 5070, badReceiverProtocolObjects, false);
		SipProvider badReceiverProvider = badReceiver.createProvider();			
		badReceiverProvider.addSipListener(badReceiver);
		badReceiverProtocolObjects.start();
		badReceiver.setDropRequest(true);
		String host = "mobicents.org";
		
		tomcat.startTomcat();
		tomcat.removeConnector(sipConnector);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, ListeningPoint.TCP);
		
		mockDNSLookup(host);		
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("method", "REGISTER");
		params.put("secureRURI", "true");
		deployApplication(params);
		
		Thread.sleep(TIMEOUT);
		RouteHeader routeHeader = (RouteHeader) receiver.getRegisterReceived().getHeader(RouteHeader.NAME);
		assertNull(routeHeader);
		assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));		
	}
	
	/*
	 * Non Regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2346
	 */
	public void testShootistRouteCheck() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
				
		String host = "mobicents.org";
		
		tomcat.startTomcat();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("route", "sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;lr");
		deployApplication(params);
		
		Thread.sleep(TIMEOUT/4);
		ListIterator<RouteHeader> routeHeaders = receiver.getInviteRequest().getHeaders(RouteHeader.NAME);
		assertNotNull(routeHeaders);
		RouteHeader routeHeader = routeHeaders.next();
		assertFalse(routeHeaders.hasNext());
		assertEquals("sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;lr", routeHeader.getAddress().getURI().toString());
		assertTrue(receiver.getByeReceived());
	}

	private void mockDNSLookup(String host) throws TextParseException {
		DNSLookupPerformer dnsLookupPerformer = mock(DefaultDNSLookupPerformer.class);
		//mocking the DNS Lookups to match our test cases
		tomcat.getSipService().getSipApplicationDispatcher().getDNSServerLocator().setDnsLookupPerformer(dnsLookupPerformer);
		
		Set<String> supportedTransports = new HashSet<String>();
		supportedTransports.add(TRANSPORT.toUpperCase());
		supportedTransports.add(ListeningPoint.TCP.toUpperCase());
		supportedTransports.add(ListeningPoint.TLS.toUpperCase());
		
		List<NAPTRRecord> mockedNAPTRRecords = new LinkedList<NAPTRRecord>();
		// mocking the name because localhost is not absolute and localhost. cannot be resolved 
		Name name = mock(Name.class);
		when(name.isAbsolute()).thenReturn(true);
		when(name.toString()).thenReturn("localhost");
		mockedNAPTRRecords.add(new NAPTRRecord(new Name(host + "."), DClass.IN, 1000, 0, 0, "s", "SIP+D2U", "", new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + ".")));		
		when(dnsLookupPerformer.performNAPTRLookup(host, false, supportedTransports)).thenReturn(mockedNAPTRRecords);
		List<Record> mockedSRVRecords = new LinkedList<Record>();
		mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5080, name));
		mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 0, 0, 5081, name));
		when(dnsLookupPerformer.performSRVLookup("_sip._" + TRANSPORT.toLowerCase() + "." + host)).thenReturn(mockedSRVRecords);
		List<Record> mockedSRVTCPRecords = new LinkedList<Record>();
		mockedSRVTCPRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5081, name));
//		mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5081, name));
		when(dnsLookupPerformer.performSRVLookup("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host)).thenReturn(mockedSRVTCPRecords);
		
		List<Record> mockedSRVTLSRecords = new LinkedList<Record>();
		mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5081, name));
//		mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5081, name));
		when(dnsLookupPerformer.performSRVLookup("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host)).thenReturn(mockedSRVTLSRecords);
	}
	/*
	 * Making sure the procedures of retrying the next hop of RFC 3263 are working
	 * and that the same hop is used for CANCEL
	 */
	public void testShootistCancel() throws Exception {
//		receiver.sendInvite();
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setWaitForCancel(true);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		
		badReceiverProtocolObjects =new ProtocolObjects(
				"bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		badReceiver = new TestSipListener(5081, 5070, badReceiverProtocolObjects, false);
		SipProvider badReceiverProvider = badReceiver.createProvider();			
		badReceiverProvider.addSipListener(badReceiver);
		badReceiverProtocolObjects.start();
		badReceiver.setDropRequest(true);
		
		String host = "mobicents.org";
		
		mockDNSLookup(host);
		
		receiverProtocolObjects.start();
		tomcat.startTomcat();
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("cancelOn1xx", "true");
		deployApplication(params);
		
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		assertFalse(badReceiver.getByeReceived());
		assertFalse(badReceiver.isCancelReceived());
		RouteHeader routeHeader = (RouteHeader) receiver.getInviteRequest().getHeader(RouteHeader.NAME);
		assertNull(routeHeader);
		assertTrue(receiver.isCancelReceived());	
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertTrue(allMessagesContent.size() >= 2);
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}
	
	/*
	 * Making sure the procedures of retrying the next hop of RFC 3263 are working
	 * and that the ACK to an error response uses the same hop
	 */
	public void testShootistErrorResponse() throws Exception {
		Map<String, String> additionalProps = new HashMap<String, String>();
		additionalProps.put(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
		
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null, additionalProps);
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
		receiver.setFinalResponseToSend(Response.SERVER_INTERNAL_ERROR);		
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		badReceiverProtocolObjects =new ProtocolObjects(
				"bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null, additionalProps);
		badReceiver = new TestSipListener(5081, 5070, badReceiverProtocolObjects, false);
		SipProvider badReceiverProvider = badReceiver.createProvider();			
		badReceiverProvider.addSipListener(badReceiver);
		badReceiverProtocolObjects.start();
		badReceiver.setDropRequest(true);
		String host = "mobicents.org";
		
		tomcat.startTomcat();
		
		mockDNSLookup(host);		
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("testErrorResponse", "true");
		deployApplication(params);
		
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		assertFalse(badReceiver.isAckReceived());
		assertTrue(receiver.isAckReceived());	
		RouteHeader routeHeader = (RouteHeader) receiver.getInviteRequest().getHeader(RouteHeader.NAME);
		assertNull(routeHeader);
		List<String> allMessagesContent = receiver.getAllMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();	
		if(badReceiverProtocolObjects != null) {
			badReceiverProtocolObjects.destroy();
		}
		logger.info("Test completed");
		super.tearDown();
	}
}