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
 * This test ensures that an application with no appname cannot be deployed successfully into Mobicents Sip Servlets
 * @author jean.deruelle@gmail.com
 *
 */
public class DeploymentTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(DeploymentTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	

	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;	
	
	public DeploymentTest(String name) {
		super(name);
		autoDeployOnStartup = false;
		startTomcatOnStartup = false;
	}

	public void deployNoAppNameApplication() {
		assertFalse(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/no-app-name-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	public void deployNoMainServletApplication() {
		assertFalse(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/no-main-servlet-app/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}
	
	public void deployShootmeApplication(String webContextName) {
		if(webContextName == null) {
			webContextName = "sip-test";
		}
		assertTrue(tomcat.deployContext(
			projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
			"sip-test-context", webContextName));
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
			"org/mobicents/servlet/sip/testsuite/deployment/blank dir/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
	}
	
	public void testDeployApplicationWithNoAppName() throws Exception {
		tomcat.startTomcat();
		deployNoAppNameApplication();
	}
	
	public void testNoMainServletApp() throws Exception {
		tomcat.startTomcat();
		deployNoMainServletApplication();
	}
	
	public void testBlankSpaceInDarPath() throws Exception {
		tomcat.startTomcat();
		deployShootmeApplication(null);
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());
		
		senderProtocolObjects.destroy();	
	}
	
	/**
	 * Issue 1411 http://code.google.com/p/mobicents/issues/detail?id=1411
	 * Sip Connectors should be removed after removing all Sip Servlets
	 * @throws Exception
	 */
	public void testBYEOnSipServletDestroy() throws Exception {
		tomcat.startTomcat();
		deployShootmeApplication(null);
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		String fromName = "testByeOnDestroy";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());		
		
		senderProtocolObjects.destroy();	
	}
	
	/**
	 * Issue 1417 http://code.google.com/p/mobicents/issues/detail?id=1417
	 * Deploy 2 applications with the same app-name should fail
	 * @throws Exception
	 */
	public void testDeploy2AppsWithSameAppName() throws Exception {
		tomcat.startTomcat();
		deployShootmeApplication(null);
		try {
			deployShootmeApplication("second-test-app");
			fail("Should see an illegalStateException saying that an app of that app-name is already deployed");
		} catch (IllegalStateException e) {
			logger.info(e.getMessage());
		}
	}

	@Override
	protected void tearDown() throws Exception {					
		super.tearDown();
	}

	@Override
	protected void deployApplication() {
		
	}


}
