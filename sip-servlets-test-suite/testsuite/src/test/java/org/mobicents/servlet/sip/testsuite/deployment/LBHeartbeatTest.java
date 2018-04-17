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

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.log4j.Logger;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingService;
import org.mobicents.ha.javax.sip.MultiNetworkLoadBalancerHeartBeatingServiceImpl;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.tools.sip.balancer.BalancerRunner;
/**
 * 
 * @author kostyantyn.nosach@telestax.com
 *
 */
public class LBHeartbeatTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(LBHeartbeatTest.class);
	
	
	public static final String DEFAULT_SIP_PATH_NAME = "gov.nist";
	public static final String PASS_INVITE_NON_2XX_ACK_TO_LISTENER = "gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER";
	public static final String TCP_POST_PARSING_THREAD_POOL_SIZE = "gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE";
	public static final String AUTOMATIC_DIALOG_SUPPORT_STACK_PROP = "javax.sip.AUTOMATIC_DIALOG_SUPPORT";	
	public static final String LOOSE_DIALOG_VALIDATION = "gov.nist.javax.sip.LOOSE_DIALOG_VALIDATION";
	public static final String SERVER_LOG_STACK_PROP = "gov.nist.javax.sip.SERVER_LOG";
	public static final String DEBUG_LOG_STACK_PROP = "gov.nist.javax.sip.DEBUG_LOG";	
	public static final String SERVER_HEADER = "org.mobicents.servlet.sip.SERVER_HEADER";
	public static final String USER_AGENT_HEADER = "org.mobicents.servlet.sip.USER_AGENT_HEADER";

	private static final String TRANSPORT = "udp";
	private static final int TIMEOUT = 10000;
	private int lbRmiPort = 2000;
	
	public LBHeartbeatTest(String name) {
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
	 *1.Start Tomcat  server
	 *2.Wait for server to start completely(thread.sleep)
	 *3.AssertFalse(clusteredStack.hbService.isStarted())
	 *4.Deploy any app
	 *5.AssertTrue(clusteredStack.hbService.isStarted())
	 *https://github.com/RestComm/sip-servlets/issues/261
	 */
	public void testLBHeartbeating() throws Exception 
	{
		BalancerRunner balancerRunner = startLoadBalancer();
		Thread.sleep(TIMEOUT/5);
		SipEmbedded node1 = createTomcatNode("node1");
		node1.initTomcat(TRANSPORT, getProperties());
		node1.startTomcat();
		node1.addSipConnectorForLB("node1", sipIpAddress, 5070, listeningPointTransport, System.getProperty("org.mobicents.testsuite.testhostaddr") , lbRmiPort);
		Thread.sleep(TIMEOUT);
		assertFalse(((ClusteredSipStack)(node1.getSipService().getSipStack())).getLoadBalancerHeartBeatingService().isHeartbeatStarted());
		SipContext sipContext = deployShootme(node1, null);
		Thread.sleep(TIMEOUT);
		assertTrue(((ClusteredSipStack)(node1.getSipService().getSipStack())).getLoadBalancerHeartBeatingService().isHeartbeatStarted());
		node1.stopTomcat();
		balancerRunner.stop();
	}

	private SipEmbedded createTomcatNode(String serverName) 
	{
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

	private Properties getProperties() {
		Properties prop = new Properties();
		prop.setProperty(LoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME, MultiNetworkLoadBalancerHeartBeatingServiceImpl.class.getCanonicalName());
		prop.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "true");
		prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		prop.setProperty(DEBUG_LOG_STACK_PROP, "." + "/" + "mss-jsip-" + getName() + "-debug.txt");
		prop.setProperty(SERVER_LOG_STACK_PROP, "." + "/" + "mss-jsip-" + getName() + "-messages.xml");
		prop.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
		prop.setProperty(AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");
		prop.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		prop.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
		prop.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		prop.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0");
		prop.setProperty(LOOSE_DIALOG_VALIDATION, "true");
		prop.setProperty(PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
		prop.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
		prop.setProperty("gov.nist.javax.sip.AGGRESSIVE_CLEANUP", "true");
		prop.setProperty(LoadBalancerHeartBeatingService.AUTO_START_HEARTBEAT, "false");
		if (prop.get(TCP_POST_PARSING_THREAD_POOL_SIZE) == null)
			prop.setProperty(TCP_POST_PARSING_THREAD_POOL_SIZE, "30");

		return prop;
	}
	
	@Override
	protected Properties getSipStackProperties() {
		
		return super.getSipStackProperties();
	}
}

