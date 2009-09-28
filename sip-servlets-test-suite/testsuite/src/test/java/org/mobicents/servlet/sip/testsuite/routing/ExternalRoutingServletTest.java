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
package org.mobicents.servlet.sip.testsuite.routing;

import java.io.File;
import java.util.Properties;

import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipUnitServletTestCase;

/**
 * This test starts 2 sip servlets container 
 * one on 5069 that has the AR setup to ROUTE to the other server
 * on port 5070 that has the LocationServiceSipServlet application installed.
 * 
 * Then the test sends a REGISTER that goes through server 1 that routes it to server 2
 * Location Service sends back OK and it is routed back to UAC => test green
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ExternalRoutingServletTest extends SipUnitServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ExternalRoutingServletTest.class);
	
	private static final int TIMEOUT = 5000;
	
	private SipStack sipStackSender;
	private SipPhone sipPhoneSender;	
	SipEmbedded closestTomcat;
	
	public ExternalRoutingServletTest(String name) {
		super(name);
		autoDeployOnStartup = false;
		startTomcatOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
				"location-service-context", 
				"location-service"));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
		+ projectHome
		+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
		+ "org/mobicents/servlet/sip/testsuite/routing/locationservice-dar.properties";
	}
	
	protected String getDarConfigurationFileForClosestServer() {
		return "file:///"
		+ projectHome
		+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
		+ "org/mobicents/servlet/sip/testsuite/routing/externalrouting-dar.properties";
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();		
		System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", "org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider");
	}

	public SipStack makeStack(String transport, int port) throws Exception {
		Properties properties = new Properties();
		String peerHostPort1 = "127.0.0.1:5069";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ "udp");
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + transport + "_"
				+ port);
		properties.setProperty("sipunit.BINDADDR", "127.0.0.1");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/callforwarding_debug_" + port + ".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/callforwarding_server_" + port + ".txt");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		
		return new SipStack(transport, port, properties);		
	}
	
	public void setupPhone(String fromAddress, String toAddress) throws Exception {			
		sipStackSender = makeStack(SipStack.PROTOCOL_UDP, 5080);			
		sipPhoneSender = sipStackSender.createSipPhone("localhost",
				SipStack.PROTOCOL_UDP, 5069, fromAddress);		
	}
	
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
		closestTomcat.initTomcat(tomcatBasePath);						
		closestTomcat.addSipConnector("SIP-Servlet-Closest-Tomcat-Server", sipIpAddress, 5069, ListeningPoint.UDP);						
		closestTomcat.startTomcat();		
		Thread.sleep(TIMEOUT);
		setupPhone("sip:sender@sip-servlets.com", "sip:receiver@sip-servlets.com");
		boolean registerOK = sipPhoneSender.register(null, 3600);
		assertTrue(registerOK);
	}	

	@Override
	public void tearDown() throws Exception {					
		Thread.sleep(1000);
		if(sipPhoneSender != null) {
			sipPhoneSender.dispose();	
		}
		if(sipStackSender != null) {
			sipStackSender.dispose();	
		}
		closestTomcat.stopTomcat();
		super.tearDown();
	}


}
