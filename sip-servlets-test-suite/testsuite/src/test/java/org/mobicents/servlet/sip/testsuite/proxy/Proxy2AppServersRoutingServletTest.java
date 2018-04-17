/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.servlet.sip.testsuite.proxy;

import java.io.File;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ViaHeader;
import static junit.framework.Assert.assertTrue;
import org.apache.catalina.Realm;
import org.apache.catalina.deploy.ApplicationParameter;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class Proxy2AppServersRoutingServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(Proxy2AppServersRoutingServletTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;

    private int closestTomcatPort;

    SipEmbedded closestTomcat;

    TestSipListener sender;
    int senderPort;
    TestSipListener receiver;
    int receiverPort;

    ProtocolObjects senderProtocolObjects;
    ProtocolObjects recieverProtocolObjects;

    public Proxy2AppServersRoutingServletTest(String name) {
        super(name);
        autoDeployOnStartup = false;
        startTomcatOnStartup = false;
        System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", "org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider");
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat
                .deployContext(
                        projectHome
                        + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                        "sip-test-context", "sip-test"));
    }
    
        protected SipStandardContext deployOnSecondApplication(SipEmbedded second, String docBase, String name, Map<String, String> params,
                ConcurrencyControlMode concurrencyControlMode, Integer sessionTimeout, Realm realm) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(docBase);
		context.setName(name);
		context.setPath("/" + name);
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		if(concurrencyControlMode != null) {
			context.setConcurrencyControlMode(concurrencyControlMode);
		}
                if (sessionTimeout != null) {
                    context.setSipApplicationSessionTimeout(sessionTimeout);
                }
                if (realm != null) {
                    context.setRealm(realm);
                }
                if (params != null) {
                    for (Map.Entry<String, String> param : params.entrySet()) {
                            ApplicationParameter applicationParameter = new ApplicationParameter();
                            applicationParameter.setName(param.getKey());
                            applicationParameter.setValue(param.getValue());
                            context.addApplicationParameter(applicationParameter);
                    }
                }
                ctx = context;
		assertTrue(second.deployContext(context));
		return context;
	}       

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/proxy/proxy-dar.properties";
    }

    protected String getDarConfigurationFileForSecondServer() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/proxy/proxy-dar.properties";
    }

    protected String getDarConfigurationFileUASSecondServer() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
    }

    @Override
    public void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();
    }

    public void setupPhones() throws Exception {

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
        SipProvider senderProvider = sender.createProvider();
        senderProvider.addSipListener(sender);
        senderProtocolObjects.start();

        recieverProtocolObjects = new ProtocolObjects(
                "reciever", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        receiverPort = NetworkPortAssigner.retrieveNextPort();
        receiver = new TestSipListener(receiverPort, containerPort, recieverProtocolObjects, false);
        SipProvider registerRecieverProvider = receiver.createProvider();
        registerRecieverProvider.addSipListener(receiver);
        recieverProtocolObjects.start();

        closestTomcatPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(senderPort));
        params.put("receiverPort", String.valueOf(receiverPort));
        params.put("downstreamContainerPort", String.valueOf(closestTomcatPort));
        deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                params, null);
    }


    /* 
	 * https://code.google.com/p/sipservlets/issues/detail?id=259
	 * This test starts 2 sip servlets container 
	 * one on 5070 that has the proxy app to ROUTE to the other server on 5069
	 * one on port 5069 that has the proxy application to route to UAS.
	 * 
	 * Tests for number of Via Header in ACK to be correct 3 instead of 2
     */
    public void test2Proxies() throws Exception {
        //start the most remote server first
        tomcat.startTomcat();
        setupPhones();
        //create and start the closest server
        //starting tomcat
        closestTomcat = new SipEmbedded("SIP-Servlet-Second-Tomcat-Server", serviceFullClassName);
        closestTomcat.setLoggingFilePath(
                projectHome + File.separatorChar + "sip-servlets-test-suite"
                + File.separatorChar + "testsuite"
                + File.separatorChar + "src"
                + File.separatorChar + "test"
                + File.separatorChar + "resources" + File.separatorChar);
        logger.info("Log4j path is : " + closestTomcat.getLoggingFilePath());
        closestTomcat.setDarConfigurationFilePath(getDarConfigurationFileForSecondServer());
        closestTomcat.initTomcat(tomcatBasePath, null);

        closestTomcat.addSipConnector("SIP-Servlet-Second-Tomcat-Server", sipIpAddress, closestTomcatPort, ListeningPoint.UDP);
        closestTomcat.startTomcat();
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(senderPort));
        params.put("receiverPort", String.valueOf(receiverPort));
        deployOnSecondApplication(closestTomcat,
                projectHome
                + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                "secondProxy",
                params,
                null,null,null);
        Thread.sleep(TIMEOUT);

        String fromName = "test-unique-location-urn-uri1-push-route-app-server";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        String r = "requestUri";
        String ra = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5057";
        SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
                r, ra);
        sender.setRecordRoutingProxyTesting(true);
        receiver.setRecordRoutingProxyTesting(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        assertTrue(receiver.isAckReceived());
        logger.info("ack received " + receiver.getAckRequest());
        assertNotNull(receiver.getAckRequest());
        ListIterator<ViaHeader> viaHeaders = (ListIterator<ViaHeader>) receiver.getAckRequest().getHeaders(ViaHeader.NAME);
        int count = 0;
        while (viaHeaders.hasNext()) {
            count++;
            viaHeaders.next();
        }
        assertEquals(3, count);
    }

    /* 
	 * https://code.google.com/p/sipservlets/issues/detail?id=237
	 * This test starts 2 sip servlets container 
	 * one on 5070 that has the proxy app to ROUTE to the other server on 5069
	 * one on port 5069 that has the UAS application to send back an error
	 * 
	 * Tests for number of Via Header in ACK to be correct 3 instead of 2
     */
    public void testProxyUASError() throws Exception {
        //start the most remote server first
        tomcat.startTomcat();
        setupPhones();
        //create and start the closest server
        //starting tomcat
        closestTomcat = new SipEmbedded("SIP-Servlet-Second-Tomcat-Server", serviceFullClassName);
        closestTomcat.setLoggingFilePath(
                projectHome + File.separatorChar + "sip-servlets-test-suite"
                + File.separatorChar + "testsuite"
                + File.separatorChar + "src"
                + File.separatorChar + "test"
                + File.separatorChar + "resources" + File.separatorChar);
        logger.info("Log4j path is : " + closestTomcat.getLoggingFilePath());
        closestTomcat.setDarConfigurationFilePath(getDarConfigurationFileUASSecondServer());
        closestTomcat.initTomcat(tomcatBasePath, null);
        closestTomcat.addSipConnector("SIP-Servlet-Second-Tomcat-Server", sipIpAddress, closestTomcatPort, ListeningPoint.UDP);
        closestTomcat.startTomcat();
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(senderPort));
        deployOnSecondApplication(closestTomcat,
                projectHome
                + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
                "secondUAS",
                params,
                null,null,null);        
        Thread.sleep(TIMEOUT);

        String fromName = "test-unique-location-urn-uri1-push-route-app-server-error-response";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        String r = "requestUri";
        String ra = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5057";
        SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
                r, ra);
        sender.setRecordRoutingProxyTesting(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isFinalResponseReceived());
    }

    @Override
    public void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        recieverProtocolObjects.destroy();
        closestTomcat.stopTomcat();
        logger.info("Test completed");
        super.tearDown();
    }

}
