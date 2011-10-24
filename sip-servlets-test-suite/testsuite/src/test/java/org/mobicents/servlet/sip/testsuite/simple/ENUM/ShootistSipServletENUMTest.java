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

package org.mobicents.servlet.sip.testsuite.simple.ENUM;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sip.SipProvider;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSLookupPerformer;
import org.mobicents.ext.javax.sip.dns.DefaultDNSLookupPerformer;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;
import org.xbill.DNS.DClass;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;

public class ShootistSipServletENUMTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ShootistSipServletENUMTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener receiver;
	ProtocolObjects receiverProtocolObjects;
	
	TestSipListener badReceiver;
	ProtocolObjects badReceiverProtocolObjects;
	
	public ShootistSipServletENUMTest(String name) {
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

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
        super.setUp();		
	}
	
	/*
	 * testing the DNSResolver MSS extension
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
		String host = "+358-555-1234567";
		
		tomcat.startTomcat();
		
		mockDNSLookup(host);		
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("enum", host);
		params.put("urlType", "tel");
		deployApplication(params);
		
		Thread.sleep(TIMEOUT);
		assertFalse(badReceiver.getByeReceived());
		assertTrue(receiver.getByeReceived());
		assertEquals("sip:jean@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080", receiver.getInviteRequest().getRequestURI().toString());
	}

	private void mockDNSLookup(String host) throws TextParseException {
		DNSLookupPerformer dnsLookupPerformer = mock(DefaultDNSLookupPerformer.class);
		//mocking the DNS Lookups to match our test cases
		tomcat.getSipService().getSipApplicationDispatcher().getDNSServerLocator().setDnsLookupPerformer(dnsLookupPerformer);
		
		Set<String> supportedTransports = new HashSet<String>();
		supportedTransports.add(TRANSPORT.toUpperCase());
		
		List<NAPTRRecord> mockedNAPTRRecords = new LinkedList<NAPTRRecord>();
		// mocking the name because " + System.getProperty("org.mobicents.testsuite.testhostaddr") + " is not absolute and " + System.getProperty("org.mobicents.testsuite.testhostaddr") + ". cannot be resolved 
		Name name = mock(Name.class);
		when(name.isAbsolute()).thenReturn(true);
		when(name.toString()).thenReturn("!^.*$!sip:jean@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080!");
		mockedNAPTRRecords.add(new NAPTRRecord(new Name("7.6.5.4.3.2.1.5.5.5.8.5.3.e164.arpa" + "."), DClass.IN, 1000, 0, 0, "s", "E2U+sip", "!^.*$!sip:jean@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080!", name));		
		when(dnsLookupPerformer.performNAPTRLookup("7.6.5.4.3.2.1.5.5.5.8.5.3.e164.arpa", false, supportedTransports)).thenReturn(mockedNAPTRRecords);
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		badReceiverProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}
}