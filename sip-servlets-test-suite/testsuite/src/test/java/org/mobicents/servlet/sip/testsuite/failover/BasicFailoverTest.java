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
package org.mobicents.servlet.sip.testsuite.failover;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.failover.SipStandardBalancerNodeService;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;
import org.mobicents.tools.sip.balancer.NodeRegisterImpl;
import org.mobicents.tools.sip.balancer.RouterImpl;
import org.mobicents.tools.sip.balancer.SIPBalancerForwarder;

/**
 * This test starts 2 sip servlets container 
 * one on 5070 and one on port 5071 that have the SimpleSipServlet application installed.
 * A Load Balancer listening on port 5060 is virtually present in front of them and proxy statelessly requests from its other port 5065 to the first alive server. 
 * 
 * The test executes a comple call flow, then server 1 is shut down.
 * The test executes another call flow that should still complete.
 *  
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class BasicFailoverTest extends SipServletTestCase {
	
	private static final String SECOND_SERVER_NAME = "SIP-Servlet-Second-Tomcat-Server";

	private static final String SIP_SERVICE_CLASS_NAME = "org.mobicents.servlet.sip.startup.failover.SipStandardBalancerNodeService";

	private static transient Logger logger = Logger.getLogger(BasicFailoverTest.class);
	
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 15000;
	private static final int CANCEL_TIMEOUT = 500;
	InetAddress balancerAddress = null;
	private final static int BALANCER_EXTERNAL_PORT = 5060;
	private final static int BALANCER_INTERNAL_PORT = 5065;
	private NodeRegisterImpl reg;
	private SIPBalancerForwarder fwd;
	private SipEmbedded secondTomcatServer;

	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;	
	ProtocolObjects	receiverProtocolObjects;

	
	public BasicFailoverTest(String name) {
		super(name);
		createTomcatOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"simple-service-context", 
				"simple-service"));		
	}

	public void deployApplication(SipEmbedded sipEmbedded) {
		assertTrue(sipEmbedded.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"simple-service-context", 
				"simple-service"));		
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
		+ projectHome
		+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
		+ "org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
	}
	
	private void deploySpeedDialApplication(SipEmbedded sipEmbedded) {
		assertTrue(sipEmbedded.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/speed-dial-servlet/src/main/sipapp",
				"speed-dial-context", 
				"speed-dial"));
	}
	
	public void deployLocationServiceApplication(SipEmbedded sipEmbedded) {
		assertTrue(sipEmbedded.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
				"location-service-context", 
				"location-service"));	
	}
	
	protected String getLocationServiceDarConfigurationFile() {
		return "file:///"
		+ projectHome
		+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
		+ "org/mobicents/servlet/sip/testsuite/composition/speeddial-locationservice-dar.properties";
	}

	public void deployCallForwardingApplication(SipEmbedded sipEmbedded) {
		assertTrue(sipEmbedded.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test"));
	}

	protected String getDarConfigurationFileCallForwarding() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-b2bua-servlet-dar.properties";
	}
	
	public void deployShootistApplication(SipEmbedded sipEmbedded, boolean reInvite) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("encodeRequestURI");
		applicationParameter.setValue("true");
		context.addApplicationParameter(applicationParameter);
		if(reInvite) {
			ApplicationParameter applicationParameter2 = new ApplicationParameter();
			applicationParameter2.setName("username");
			applicationParameter2.setValue("reinvite");
			context.addApplicationParameter(applicationParameter2);
		}
		assertTrue(sipEmbedded.deployContext(context));
	}		

	protected String getDarConfigurationFileShootist() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
	}
	
	@Override
	public void setUp() throws Exception {
		System.setProperty("java.util.logging.config.file", projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar + "logging.properties");
		serverName = "SIP-Servlet-First-Tomcat-Server";
		serviceFullClassName = SIP_SERVICE_CLASS_NAME;
		super.setUp();
		balancerAddress=InetAddress.getByName("127.0.0.1");				
	}
	
	
	public void testBasicFailover() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, "127.0.0.1:5060");
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFile(), tomcatBasePath, 5070);
		deployApplication(tomcat);		
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFile(), getTomcatBackupHomePath(), 5071);
		deployApplication(secondTomcatServer);		
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		sender.setOkToByeReceived(false);
		toUser = "receiver2";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		sender.setOkToByeReceived(false);
		secondTomcatServer.stopTomcat();
	}	
	
	public void testBasicFailoverUAC() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"failover-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null);
		receiver = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFileShootist(), tomcatBasePath, 5070);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFileShootist(), getTomcatBackupHomePath(), 5071);
		//first test
		Thread.sleep(TIMEOUT);
		deployShootistApplication(tomcat, false);
		Thread.sleep(TIMEOUT);
		
		assertTrue(receiver.getByeReceived());
		
		tomcat.stopTomcat();
		receiver.setByeReceived(false);
		deployShootistApplication(secondTomcatServer, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
	}		
	
	public void testBasicFailoverUACCalleeSendsBye() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"failover-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null);
		receiver = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, receiverProtocolObjects, true);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFileShootist(), tomcatBasePath, 5070);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFileShootist(), getTomcatBackupHomePath(), 5071);		
		//first test
		Thread.sleep(TIMEOUT);
		deployShootistApplication(tomcat, false);
		Thread.sleep(TIMEOUT);
		
		assertTrue(receiver.getOkToByeReceived());
		
		tomcat.stopTomcat();
		receiver.setOkToByeReceived(false);
		deployShootistApplication(secondTomcatServer, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());
	}
	
	public void testBasicFailoverUACReInvite() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"failover-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null);
		receiver = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFileShootist(), tomcatBasePath, 5070);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFileShootist(), getTomcatBackupHomePath(), 5071);
		//first test
		Thread.sleep(TIMEOUT);
		deployShootistApplication(tomcat, true);
		Thread.sleep(TIMEOUT);
		
		assertTrue(receiver.getByeReceived());
		
		tomcat.stopTomcat();
		receiver.setByeReceived(false);
		deployShootistApplication(secondTomcatServer, true);	
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
	}
	
	public void testBasicFailoverUASReInviteStickiness() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"failover-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFile(), tomcatBasePath, 5070);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFile(), getTomcatBackupHomePath(), 5071);
		//first test
		Thread.sleep(TIMEOUT);
		deployApplication(tomcat);
		deployApplication(secondTomcatServer);
		Thread.sleep(TIMEOUT);
		
		String fromName = "isendreinvite";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setSendReinvite(true);
		sender.setSendBye(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());						
	}
	
	public void testBasicFailoverCancelTest() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFile(), tomcatBasePath, 5070);
		deployApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFile(), getTomcatBackupHomePath(), 5071);
		deployApplication(secondTomcatServer);		
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "cancel";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(CANCEL_TIMEOUT);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		sender.setOkToByeReceived(false);
		toUser = "receiver2";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(CANCEL_TIMEOUT);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		secondTomcatServer.stopTomcat();
	}
	
	public void testBasicFailoverSpeedDialLocationService() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"sdls-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("sdls-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getLocationServiceDarConfigurationFile(), tomcatBasePath, 5070);
		deployLocationServiceApplication(tomcat);
		deploySpeedDialApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getLocationServiceDarConfigurationFile(), getTomcatBackupHomePath(), 5071);
		deployLocationServiceApplication(secondTomcatServer);
		deploySpeedDialApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "6";
		String toSipAddress = "127.0.0.1:5090";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		sender.setOkToByeReceived(false);
		receiver.setByeReceived(false);
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		toUser = "6";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		sender.setOkToByeReceived(false);
		receiver.setByeReceived(false);
		secondTomcatServer.stopTomcat();
	}
	
	public void testBasicFailoverSpeedDialLocationServiceCalleeSendsBye() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"sdls-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("sdls-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, true);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getLocationServiceDarConfigurationFile(), tomcatBasePath, 5070);
		deployLocationServiceApplication(tomcat);
		deploySpeedDialApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getLocationServiceDarConfigurationFile(), getTomcatBackupHomePath(), 5071);
		deployLocationServiceApplication(secondTomcatServer);
		deploySpeedDialApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "6";
		String toSipHost = "127.0.0.1:5090";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());
		assertTrue(receiver.getOkToByeReceived());
		sender.setOkToByeReceived(false);
		receiver.setByeReceived(false);
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		toUser = "6";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipHost);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());
		assertTrue(receiver.getOkToByeReceived());
		sender.setOkToByeReceived(false);
		receiver.setByeReceived(false);
		secondTomcatServer.stopTomcat();
	}
	
	public void testBasicFailoverCancelSpeedDialLocationService() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"sdls-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("sdls-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		receiver.setWaitForCancel(true);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getLocationServiceDarConfigurationFile(), tomcatBasePath, 5070);
		deployLocationServiceApplication(tomcat);
		deploySpeedDialApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getLocationServiceDarConfigurationFile(), getTomcatBackupHomePath(), 5071);
		deployLocationServiceApplication(secondTomcatServer);
		deploySpeedDialApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "6";
		String toSipAddress = "127.0.0.1:5090";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		toUser = "6";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
		secondTomcatServer.stopTomcat();
	}
	
	public void testBasicFailoverCallForwardingB2BUA() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"cfb2bua-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("cfb2bua-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFileCallForwarding(), tomcatBasePath, 5070);
		deployCallForwardingApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFileCallForwarding(), getTomcatBackupHomePath(), 5071);
		deployCallForwardingApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		sender.setOkToByeReceived(false);
		receiver.setByeReceived(false);
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		toUser = "receiver2";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		sender.setOkToByeReceived(false);
		receiver.setByeReceived(false);
		secondTomcatServer.stopTomcat();
	}
	
	public void testBasicFailoverCallForwardingB2BUACalleeSendsBye() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"cfb2bua-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("cfb2bua-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, true);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFileCallForwarding(), tomcatBasePath, 5070);
		deployCallForwardingApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFileCallForwarding(), getTomcatBackupHomePath(), 5071);
		deployCallForwardingApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());
		receiver.setOkToByeReceived(false);
		sender.setByeReceived(false);
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		toUser = "receiver2";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());
		receiver.setOkToByeReceived(false);
		sender.setByeReceived(false);
		secondTomcatServer.stopTomcat();
	}
	
	public void testBasicFailoverCancelCallForwardingB2BUA() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"cfb2bua-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("cfb2bua-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		tomcat = setupAndStartTomcat(serverName, getDarConfigurationFileCallForwarding(), tomcatBasePath, 5070);
		deployCallForwardingApplication(tomcat);
		//starts the second server
		secondTomcatServer = setupAndStartTomcat(SECOND_SERVER_NAME, getDarConfigurationFileCallForwarding(), getTomcatBackupHomePath(), 5071);
		deployCallForwardingApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
		tomcat.stopTomcat();
		Thread.sleep(TIMEOUT);
		toUser = "receiver2";
		toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
		secondTomcatServer.stopTomcat();
	}
	
	public void testFailoverNoNodeStarted() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"failover-sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isErrorResponseReceived());
	}

	/**
	 * @throws IOException
	 * @throws Exception
	 */
	private SipEmbedded setupAndStartTomcat(String serverName, String darConfigurationFile, String specificTomcatBasePath, int sipConnectorPort) throws IOException, Exception {
		SipEmbedded tomcatServer = new SipEmbedded(serverName, SIP_SERVICE_CLASS_NAME);
		tomcatServer.setLoggingFilePath(  
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + tomcatServer.getLoggingFilePath());
		tomcatServer.setDarConfigurationFilePath(darConfigurationFile);		
		tomcatServer.initTomcat(specificTomcatBasePath);						
		tomcatServer.addSipConnector(serverName, sipIpAddress, sipConnectorPort, ListeningPoint.UDP);
		((SipStandardBalancerNodeService)tomcatServer.getSipService()).setBalancers(balancerAddress.getHostAddress());
		tomcatServer.startTomcat();
		return tomcatServer;
	}
	
	private String getTomcatBackupHomePath() throws IOException {
		//Reading properties
		Properties properties = new Properties();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"org/mobicents/servlet/sip/testsuite/testsuite.properties");		
		try{
			properties.load(inputStream);
		} catch (NullPointerException e) {
			inputStream = getClass().getResourceAsStream(
				"org/mobicents/servlet/sip/testsuite/testsuite.properties");
			properties.load(inputStream);
		}
		
		// First try to use the env variables - useful for shell scripting
		String tomcatBackupBasePath = System.getenv("CATALINA_BACKUP_HOME");	
		
		// Otherwise use the properties
		if(tomcatBackupBasePath == null || tomcatBackupBasePath.length() <= 0) 
			tomcatBackupBasePath = properties.getProperty("tomcat.backup.home");
		logger.info("Tomcat Backup base Path is : " + tomcatBackupBasePath);
		return tomcatBackupBasePath;
	}

	private void startSipBalancer() throws Exception {
		prepareRegister();		
		reg.startRegistry(2000);
		RouterImpl.setRegister(reg);
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "SipBalancerForwarder");
		properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/sipbalancerforwarderdebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/sipbalancerforwarder.xml");
		properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
		properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		properties.setProperty("gov.nist.javax.sip.CANCEL_CLIENT_TRANSACTION_CHECKED", "false");
		
		properties.setProperty("host", "127.0.0.1");
		properties.setProperty("internalPort", "" + BALANCER_INTERNAL_PORT);
		properties.setProperty("externalPort", "" + BALANCER_EXTERNAL_PORT);
		fwd=new SIPBalancerForwarder(properties,reg);
		fwd.start();
	}
	
	private NodeRegisterImpl prepareRegister() throws Exception {
		reg=new NodeRegisterImpl(balancerAddress);
		return reg;
	}
	
	private void undoRegister(NodeRegisterImpl reg) throws Exception {
		reg.stopRegistry();
	}


	@Override
	public void tearDown() throws Exception {
		logger.info("Stopping BasicFailoverTest");
		Thread.sleep(5000);
		if(fwd != null) {
			fwd.stop();
			fwd = null;
		}
		undoRegister(reg);
		reg =null;
		if(senderProtocolObjects != null) {
			senderProtocolObjects.destroy();	
		}		
		if(receiverProtocolObjects != null) {
			receiverProtocolObjects.destroy();
		}
		if(tomcat != null) {
			tomcat.stopTomcat();
		}
		if(secondTomcatServer != null) {
			secondTomcatServer.stopTomcat();
		}
//		super.tearDown();
	}
}
