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

package org.mobicents.servlet.sip.testsuite.proxy.forking;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sip.SipProvider;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.proxy.Shootist;
import org.mobicents.servlet.sip.testsuite.simple.forking.Proxy;
import org.mobicents.servlet.sip.testsuite.simple.forking.Shootme;

public class ProxySipServletDownstreamProxyForkingTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ProxySipServletDownstreamProxyForkingTest.class);		
	private static final int TIMEOUT = 40000;	
//	private static final int TIMEOUT = 100000000;
	
	public ProxySipServletDownstreamProxyForkingTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
		addSipConnectorOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
                containerPort = NetworkPortAssigner.retrieveNextPort();
		super.setUp();												
	}
	
	// non regression test for Issue 2390 http://code.google.com/p/mobicents/issues/detail?id=2390
	// Proxy implementation not forwarding additional 2xx responses from downstream fork
	public void testDownstreamProxyForking() throws Exception {
        int shootme1Port = NetworkPortAssigner.retrieveNextPort();
        Shootme shootme1 = new Shootme(shootme1Port, true, 1000);
        SipProvider shootmeProvider = shootme1.createProvider();
        shootmeProvider.addSipListener(shootme1);
        int shootme2Port = NetworkPortAssigner.retrieveNextPort();
        Shootme shootme2 = new Shootme(shootme2Port, true, 2500);
        SipProvider shootme2Provider = shootme2.createProvider();
        shootme2Provider.addSipListener(shootme2);
        shootme2.setWaitBeforeFinalResponse(9000);
        int proxyPort = NetworkPortAssigner.retrieveNextPort();
		Proxy proxy = new Proxy(proxyPort,new int[]{shootme1Port, shootme2Port});
		SipProvider provider = proxy.createSipProvider();
        provider.addSipListener(proxy);
        int shootistPort = NetworkPortAssigner.retrieveNextPort();
        int cont2Port = NetworkPortAssigner.retrieveNextPort();
        Shootist shootist = new Shootist(true,shootistPort, String.valueOf(cont2Port));
        shootist.pauseBeforeBye = 20000;
        shootist.setFromHost("sip-servlets.com");
        
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, cont2Port, listeningPointTransport);
		tomcat.startTomcat();
		Map<String, String> params= new HashMap<String, String>();
		params.put("route", "sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + proxyPort);
		params.put("timeToWaitForBye", "20000");
		params.put("dontSetRURI", "true");
                params.put( "servletContainerPort", String.valueOf(cont2Port)); 
                params.put( "testPort", String.valueOf(shootistPort)); 
                params.put( "receiverPort", String.valueOf(proxyPort));                 
		SipStandardContext sipContext = deployApplication(projectHome + 
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", 
                        params, null);
		shootist.init("forward-sender-downstream-proxy", false, null);
		Thread.sleep(TIMEOUT);
		proxy.stop();
		shootme1.stop();
		shootme2.stop();
		assertTrue(shootist.isForkedResponseReceived());
		assertTrue(shootme1.isAckSeen());		
	}
	
	@Override
	protected Properties getSipStackProperties() {
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
		"true");
		sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
				"32");
		sipStackProperties.setProperty(SipStandardService.DEBUG_LOG_STACK_PROP, 
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-debug.txt");
		sipStackProperties.setProperty(SipStandardService.SERVER_LOG_STACK_PROP,
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-messages.xml");
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
		sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");		
		sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "10");
		sipStackProperties.setProperty(SipStandardService.LOOSE_DIALOG_VALIDATION, "true");
		sipStackProperties.setProperty(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
		return sipStackProperties;
	}
	
	@Override
	protected void tearDown() throws Exception {					
		logger.info("Test completed");
		super.tearDown();
	}
}