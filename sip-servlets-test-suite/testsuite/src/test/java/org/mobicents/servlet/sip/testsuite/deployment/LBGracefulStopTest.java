/*
 * TeleStax, Open Source Cloud Communications.
 * Copyright 2012 and individual contributors by the @authors tag. 
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

package org.mobicents.servlet.sip.testsuite.deployment;

import java.io.File;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.Parameters;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;
import org.mobicents.tools.sip.balancer.BalancerRunner;
/**
 * Test for http://code.google.com/p/sipservlets/issues/detail?id=195
 * Support for Graceful Shutdown of SIP Applications and Overall Server
 * @author jean.deruelle@gmail.com
 *
 */
public class LBGracefulStopTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(LBGracefulStopTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	

	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;	
	
	public LBGracefulStopTest(String name) {
		super(name);
		System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", DefaultApplicationRouterProvider.class.getName());
		createTomcatOnStartup = false;
		initTomcatOnStartup = false;
		autoDeployOnStartup = false;
		startTomcatOnStartup = false;
	}
	
	public SipContext deployShootme(SipEmbedded sipEmbedded, String webContextName) {
		if(webContextName == null) {
			webContextName = "sip-test";
		}
		SipStandardContext context = new SipStandardContext();
		context
				.setDocBase(projectHome
						+ "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath(webContextName);
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("testContextApplicationParameter");
		applicationParameter.setValue("OK");
		context.addApplicationParameter(applicationParameter);
		MemoryRealm realm = new MemoryRealm();
		realm
				.setPathname(projectHome
						+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
						+ "org/mobicents/servlet/sip/testsuite/security/tomcat-users.xml");
		context.setRealm(realm);
		assertTrue(sipEmbedded.deployContext(context));
		return context;
	}
	
	
	public SipContext deployConvergedApp(SipEmbedded sipEmbedded) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome +  "/sip-servlets-test-suite/applications/click-to-call-servlet/src/main/sipapp");
		context.setName("click2call-context");
		context.setPath("/click2call");		
		context.addLifecycleListener(new SipContextConfig());
		((StandardContext)context).setSessionTimeout(1);
		context.setSipApplicationSessionTimeout(1);
		SipStandardManager manager = new SipStandardManager();
		context.setManager(manager);
		assertTrue(sipEmbedded.deployContext(context));
		return context;
	}
	
	@Override
	protected void deployApplication() {
		// TODO Auto-generated method stub
		
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
	
	/**
	 * Make sure that the graceful stop guarantees the LB is accepting subsequent requests and routing them
	 * to the same node and routing new requests to the node not shutting down
	 * Ensure that when all nodes have been grecefully shutted down, the LB is sending back
	 * no nodes available
	 */
	public void testLBContainerGracefulShutdown() throws Exception {
		BalancerRunner balancerRunner = startLoadBalancer();
		
		SipEmbedded node1 = createTomcatNode("node1");	
		node1.initTomcat(TRANSPORT, null);
		node1.addSipConnector("node1", sipIpAddress, 5070, listeningPointTransport);		
		node1.startTomcat();	
		
		SipEmbedded node2 = createTomcatNode("node2");	
		node2.initTomcat(TRANSPORT, null);
		node2.addSipConnector("node2", sipIpAddress, 5069, listeningPointTransport);
		node2.startTomcat();	
		
		SipContext sipContext = deployShootme(node1, null);
		SipContext sipContext2 = deployShootme(node2, null);

		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5060", null, null);
		
		sender = new TestSipListener(5080, 5060, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		Thread.sleep(TIMEOUT/2);
		
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRecordRoutingProxyTesting(true);
		sender.setTimeToWaitBeforeBye(TIMEOUT*3/2);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(1000);
		// shutting down node gracefully
		node2.getSipService().stopGracefully(-1);		
		Thread.sleep(TIMEOUT);
		assertEquals("5069", ((Parameters)((RecordRouteHeader)sender.getFinalResponse().getHeader(RecordRouteHeader.NAME)).getAddress().getURI()).getParameter("node_port"));
		assertTrue(sender.isAckSent());
		Thread.sleep(TIMEOUT); 		
		// ensure the subsequent requests are responded too
		assertTrue(sender.getOkToByeReceived());
		sender.setOkToByeReceived(false);
		Thread.sleep(30000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertEquals(200, sender.getFinalResponseStatus());
		// ensure new initial requests are not handled anymore by the same node
		assertEquals("5070", ((Parameters)((RecordRouteHeader)sender.getFinalResponse().getHeader(RecordRouteHeader.NAME)).getAddress().getURI()).getParameter("node_port"));		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());	
		sender.setOkToByeReceived(false);
		sender.setFinalResponse(null);
		// make sure the Graceful Stop was effective on node2
		assertEquals(0, sipContext2.getSipApplicationDispatcher().findInstalledSipApplications().length);
		assertEquals(1, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);

		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		// ensure new initial requests are routed to the only available node
		assertEquals(200, sender.getFinalResponseStatus());
		assertEquals("5070", ((Parameters)((RecordRouteHeader)sender.getFinalResponse().getHeader(RecordRouteHeader.NAME)).getAddress().getURI()).getParameter("node_port"));
		// shutting down node1
		node1.getSipService().stopGracefully(-1);
		Thread.sleep(TIMEOUT);
		// ensure subsequent requests are still handled
		sender.setOkToByeReceived(false);
		sender.setFinalResponse(null);	
		Thread.sleep(35000);
		// sending last invite with no nodes available
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		// ensure the LB returns 500 no sip nodes available
		assertEquals(500, sender.getFinalResponseStatus());
		
		assertEquals(0, sipContext2.getSipApplicationDispatcher().findInstalledSipApplications().length);
		assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
		
		node1.stopTomcat();
		node2.stopTomcat();
		balancerRunner.stop();
		senderProtocolObjects.destroy();	
	}
	
	/**
	 * Make sure that the graceful stop guarantees the LB is accepting subsequent requests and routing them
	 * to the same node and routing new requests to the node not shutting down
	 * Ensure that when the application is restarted the LB starts routing requests back 
	 * to the node whose application was gracefully shutting down
	 */
	public void testLBContextGracefulShutdownAndReload() throws Exception {
		BalancerRunner balancerRunner = startLoadBalancer();
		
		SipEmbedded node1 = createTomcatNode("node1");	
		node1.initTomcat(TRANSPORT, null);
		node1.addSipConnector("node1", sipIpAddress, 5070, listeningPointTransport);		
		node1.startTomcat();	
		
		SipEmbedded node2 = createTomcatNode("node2");	
		node2.initTomcat(TRANSPORT, null);
		node2.addSipConnector("node2", sipIpAddress, 5069, listeningPointTransport);
		node2.startTomcat();	
		
		SipContext sipContext = deployShootme(node1, null);
		SipContext sipContext2 = deployShootme(node2, null);

		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5060", null, null);
		
		sender = new TestSipListener(5080, 5060, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		Thread.sleep(TIMEOUT/2);
		
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRecordRoutingProxyTesting(true);
		sender.setTimeToWaitBeforeBye(TIMEOUT*3/2);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(1000);
		// shutting down node gracefully
		node2.getSipService().stopGracefully(-1);		
		Thread.sleep(TIMEOUT);
		assertEquals("5069", ((Parameters)((RecordRouteHeader)sender.getFinalResponse().getHeader(RecordRouteHeader.NAME)).getAddress().getURI()).getParameter("node_port"));
		assertTrue(sender.isAckSent());
		Thread.sleep(TIMEOUT); 		
		// ensure the subsequent requests are responded too
		assertTrue(sender.getOkToByeReceived());
		sender.setOkToByeReceived(false);
		Thread.sleep(30000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertEquals(200, sender.getFinalResponseStatus());
		// ensure new initial requests are not handled anymore by the same node
		assertEquals("5070", ((Parameters)((RecordRouteHeader)sender.getFinalResponse().getHeader(RecordRouteHeader.NAME)).getAddress().getURI()).getParameter("node_port"));		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());	
		sender.setOkToByeReceived(false);
		sender.setFinalResponse(null);
		// make sure the Graceful Stop was effective on node2
		assertEquals(0, sipContext2.getSipApplicationDispatcher().findInstalledSipApplications().length);
		assertEquals(1, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);

		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		// ensure new initial requests are routed to the only available node
		assertEquals(200, sender.getFinalResponseStatus());
		assertEquals("5070", ((Parameters)((RecordRouteHeader)sender.getFinalResponse().getHeader(RecordRouteHeader.NAME)).getAddress().getURI()).getParameter("node_port"));
		// shutting down node1
		node1.getSipService().stopGracefully(-1);
		Thread.sleep(TIMEOUT);
		// ensure subsequent requests are still handled
		sender.setOkToByeReceived(false);
		sender.setFinalResponse(null);	
		Thread.sleep(35000);
		// sending last invite with no nodes available
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		// ensure the LB returns 500 no sip nodes available
		assertEquals(500, sender.getFinalResponseStatus());
		
		assertEquals(0, sipContext2.getSipApplicationDispatcher().findInstalledSipApplications().length);
		assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
		
		node1.stopTomcat();
		node2.stopTomcat();
		balancerRunner.stop();
		senderProtocolObjects.destroy();	
	}

	private SipEmbedded createTomcatNode(String serverName) {
		SipEmbedded sipEmbedded = new SipEmbedded(serverName, serviceFullClassName);
		sipEmbedded.setLoggingFilePath(				
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + sipEmbedded.getLoggingFilePath());
		String darConfigurationFile = getDarConfigurationFile();
		sipEmbedded.setDarConfigurationFilePath(darConfigurationFile);
		sipEmbedded.setHA(true);
		
		return sipEmbedded;
	}

	private BalancerRunner startLoadBalancer() throws UnknownHostException, RemoteException {
		BalancerRunner balancerRunner = new BalancerRunner();
		/*LoadBalancerConfiguration lbConfig = new LoadBalancerConfiguration();
		lbConfig.getSipConfiguration().getInternalLegConfiguration().setUdpPort(5065);
		lbConfig.getSipConfiguration().getExternalLegConfiguration().setUdpPort(5060);*/
                Properties properties = new Properties();
		balancerRunner.start(properties);	
		return balancerRunner;	
	}
	
	@Override
	protected Properties getSipStackProperties() {
		
		return super.getSipStackProperties();
	}
}
