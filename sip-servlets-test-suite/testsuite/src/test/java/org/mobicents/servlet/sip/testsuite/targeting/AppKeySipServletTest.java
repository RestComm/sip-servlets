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
package org.mobicents.servlet.sip.testsuite.targeting;
import javax.sip.SipProvider;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test is aimed to test the @SipApplicationKey annotation and check that this is compliant with JSR 289 
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class AppKeySipServletTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(AppKeySipServletTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;
	
	public AppKeySipServletTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
		initTomcatOnStartup = false;
		addSipConnectorOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/appkey-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/targeting/appkey-sip-servlet-dar.properties";
	}
		
	protected String getWilcardDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/targeting/wilcard-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, true);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();			
	}
	
	/**
	 * This test gets a REGISTER and later an INVITE that are created by the same app session through 
	 * sipSessionsUtil.getApplicationSessionByKey("appkeytest", true);
	 * It tests that the 2 requests have different Call Id. 
	 * @throws Exception
	 */
	public void testAppKey() throws Exception {
		tomcat.initTomcat(tomcatBasePath);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());		
	}
	
	/**
	 * This test tests the wildcard feature of the AR
	 * @throws Exception
	 */
	public void testWildcard() throws Exception {
		tomcat.setDarConfigurationFilePath(getWilcardDarConfigurationFile());
		tomcat.initTomcat(tomcatBasePath);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
		tomcat.startTomcat();
		deployApplication();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());		
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}
}