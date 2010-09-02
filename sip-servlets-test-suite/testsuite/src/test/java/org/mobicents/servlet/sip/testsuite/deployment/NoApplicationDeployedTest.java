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
package org.mobicents.servlet.sip.testsuite.deployment;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Test that when no application has been deployed an INVITE targeted at the container with no port in the RURI sends back a 404
 * Regression test for Issue 670
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class NoApplicationDeployedTest extends SipServletTestCase {

	private static transient Logger logger = Logger.getLogger(NoApplicationDeployedTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;
	
	/**
	 * @param name
	 */
	public NoApplicationDeployedTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		addSipConnectorOnStartup =  false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		senderProtocolObjects = new ProtocolObjects("sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null);
	}
	
	@Override
	protected void deployApplication() {
		// test to deploy nothing
		
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
		+ projectHome
		+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
		+ "org/mobicents/servlet/sip/testsuite/routing/empty-dar.properties";
	}
	
	public void testNoAppDeployed404() throws Exception {
		tomcat.addSipConnector(serverName, sipIpAddress, 5060, listeningPointTransport);
		super.tomcat.startTomcat();		
		
		sender = new TestSipListener(5080, 5060, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "container";
		String toHost = "127.0.0.1";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(404, sender.getFinalResponseStatus());
	}
	
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}
}
