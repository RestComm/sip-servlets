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
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
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

	private static Log logger = LogFactory.getLog(BasicFailoverTest.class);
	
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;
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
		autoDeployOnStartup = false;
		startTomcatOnStartup = false;
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
	
	@Override
	public void setUp() throws Exception {
		serverName = "SIP-Servlet-First-Tomcat-Server";
		serviceFullClassName = SIP_SERVICE_CLASS_NAME;
		super.setUp();
		balancerAddress=InetAddress.getByAddress(new byte[]{127,0,0,1});				
	}
	
	
	public void testBasicFailover() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"failover-sender", "gov.nist", TRANSPORT, AUTODIALOG);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		((SipStandardBalancerNodeService)tomcat.getSipService()).setBalancers(balancerAddress.getHostAddress());
		tomcat.startTomcat();
		deployApplication(tomcat);		
		//starts the second server
		secondTomcatServer = new SipEmbedded(SECOND_SERVER_NAME, SIP_SERVICE_CLASS_NAME);
		secondTomcatServer.setLoggingFilePath(  
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + secondTomcatServer.getLoggingFilePath());
		secondTomcatServer.setDarConfigurationFilePath(getDarConfigurationFile());
		getTomcatBackupHomePath();
		secondTomcatServer.initTomcat(getTomcatBackupHomePath());						
		secondTomcatServer.addSipConnector(SECOND_SERVER_NAME, sipIpAddress, 5071, ListeningPoint.UDP);
		((SipStandardBalancerNodeService)secondTomcatServer.getSipService()).setBalancers(balancerAddress.getHostAddress());
		secondTomcatServer.startTomcat();
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
		secondTomcatServer.startTomcat();
	}
	
	public void testBasicFailoverSpeedDialLocationService() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"sdls-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("sdls-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		((SipStandardBalancerNodeService)tomcat.getSipService()).setBalancers(balancerAddress.getHostAddress());
		tomcat.setDarConfigurationFilePath(getLocationServiceDarConfigurationFile());
		tomcat.initTomcat(tomcatBasePath);
		tomcat.startTomcat();
		deployLocationServiceApplication(tomcat);
		deploySpeedDialApplication(tomcat);
		//starts the second server
		secondTomcatServer = new SipEmbedded(SECOND_SERVER_NAME, SIP_SERVICE_CLASS_NAME);
		secondTomcatServer.setLoggingFilePath(  
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + secondTomcatServer.getLoggingFilePath());
		secondTomcatServer.setDarConfigurationFilePath(getLocationServiceDarConfigurationFile());
		getTomcatBackupHomePath();
		secondTomcatServer.initTomcat(getTomcatBackupHomePath());						
		secondTomcatServer.addSipConnector(SECOND_SERVER_NAME, sipIpAddress, 5071, ListeningPoint.UDP);
		((SipStandardBalancerNodeService)secondTomcatServer.getSipService()).setBalancers(balancerAddress.getHostAddress());
		secondTomcatServer.startTomcat();
		deployLocationServiceApplication(secondTomcatServer);
		deploySpeedDialApplication(secondTomcatServer);
		//first test
		Thread.sleep(TIMEOUT);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "1";
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
		toUser = "6";
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
	
	public void testBasicFailoverCallForwardingB2BUA() throws Exception {
		senderProtocolObjects =new ProtocolObjects(
				"cfb2bua-failover-sender", "gov.nist", TRANSPORT, AUTODIALOG);
		sender = new TestSipListener(5080, BALANCER_EXTERNAL_PORT, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();	
		receiverProtocolObjects = new ProtocolObjects("cfb2bua-failover-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG);			
		receiver = new TestSipListener(5090, 5060, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		//start the sip balancer
		startSipBalancer();				
		//starts the first server
		((SipStandardBalancerNodeService)tomcat.getSipService()).setBalancers(balancerAddress.getHostAddress());
		tomcat.setDarConfigurationFilePath(getDarConfigurationFileCallForwarding());
		tomcat.initTomcat(tomcatBasePath);
		tomcat.startTomcat();
		deployCallForwardingApplication(tomcat);
		//starts the second server
		secondTomcatServer = new SipEmbedded(SECOND_SERVER_NAME, SIP_SERVICE_CLASS_NAME);
		secondTomcatServer.setLoggingFilePath(  
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + secondTomcatServer.getLoggingFilePath());
		secondTomcatServer.setDarConfigurationFilePath(getDarConfigurationFileCallForwarding());
		getTomcatBackupHomePath();
		secondTomcatServer.initTomcat(getTomcatBackupHomePath());						
		secondTomcatServer.addSipConnector(SECOND_SERVER_NAME, sipIpAddress, 5071, ListeningPoint.UDP);
		((SipStandardBalancerNodeService)secondTomcatServer.getSipService()).setBalancers(balancerAddress.getHostAddress());
		secondTomcatServer.startTomcat();
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
		reg.startServer();
		RouterImpl.setRegister(reg);
		fwd=new SIPBalancerForwarder(balancerAddress.getHostAddress(),BALANCER_INTERNAL_PORT,BALANCER_EXTERNAL_PORT,reg);
		fwd.start();
	}
	
	private NodeRegisterImpl prepareRegister() throws Exception {
		reg=new NodeRegisterImpl(balancerAddress);
		
		MBeanServer server=ManagementFactory.getPlatformMBeanServer();
		ObjectName on=new ObjectName("mobicents:name=Balancer,type=sip");
		
		if(server.isRegistered(on)) {
			server.unregisterMBean(on);
		}
		server.registerMBean(reg, on);
		
		return reg;
	}
	
	private void undoRegister(NodeRegisterImpl reg) throws Exception {
		MBeanServer server=ManagementFactory.getPlatformMBeanServer();
		ObjectName on = new ObjectName("mobicents:name=Balancer,type=sip");
		server.unregisterMBean(on);
		reg.stopServer();
	}


	@Override
	public void tearDown() throws Exception {
		logger.info("Stopping BasicFailoverTest");
		Thread.sleep(1000);
		fwd.stop();
		undoRegister(reg);
		senderProtocolObjects.destroy();
		if(receiverProtocolObjects != null) {
			receiverProtocolObjects.destroy();
		}
	}
}
