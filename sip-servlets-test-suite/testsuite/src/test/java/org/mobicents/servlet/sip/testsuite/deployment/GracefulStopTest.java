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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Test for http://code.google.com/p/sipservlets/issues/detail?id=195 Support
 * for Graceful Shutdown of SIP Applications and Overall Server
 *
 * @author jean.deruelle@gmail.com
 *
 */
public class GracefulStopTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(GracefulStopTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;

    TestSipListener sender;
    ProtocolObjects senderProtocolObjects;

    public GracefulStopTest(String name) {
        super(name);
        autoDeployOnStartup = false;
        startTomcatOnStartup = false;
    }

    public SipContext deployShootme(String webContextName, Map<String,String> params) {
        if (webContextName == null) {
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
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                    ApplicationParameter applParameter = new ApplicationParameter();
                    applParameter.setName(param.getKey());
                    applParameter.setValue(param.getValue());
                    context.addApplicationParameter(applParameter);
            }
        }        
        MemoryRealm realm = new MemoryRealm();
        realm
                .setPathname(projectHome
                        + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                        + "org/mobicents/servlet/sip/testsuite/security/tomcat-users.xml");
        context.setRealm(realm);
        assertTrue(tomcat.deployContext(context));
        return context;
    }

    public SipContext deployConvergedApp(Map<String,String> params) {
        SipStandardContext context = new SipStandardContext();
        context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/click-to-call-servlet/src/main/sipapp");
        context.setName("click2call-context");
        context.setPath("/click2call");
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                    ApplicationParameter applicationParameter = new ApplicationParameter();
                    applicationParameter.setName(param.getKey());
                    applicationParameter.setValue(param.getValue());
                    context.addApplicationParameter(applicationParameter);
            }
        }        
        context.addLifecycleListener(new SipContextConfig());
        ((StandardContext) context).setSessionTimeout(1);
        context.setSipApplicationSessionTimeout(1);
        SipStandardManager manager = new SipStandardManager();
        context.setManager(manager);
        assertTrue(tomcat.deployContext(context));
        return context;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/deployment/blank dir/simple-sip-servlet-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();
    }

    /**
     * Make sure that the graceful stop guarantees the application is accepting
     * subsequent requests but not new requests and that when no sessions are
     * present the context is stopped automatically
     */
    public void testContextGracefulShutdownNegativeValue() throws Exception {
        tomcat.startTomcat();
        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();

        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("testPort", String.valueOf(myPort));
        params.put("servletContainerPort", String.valueOf(containerPort));        
        SipContext sipContext = deployShootme(null,params);        

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setTimeToWaitBeforeBye(TIMEOUT * 3 / 2);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(1000);
        sipContext.stopGracefully(-1);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        Thread.sleep(TIMEOUT);
        // ensure the subsequent requests are responded too
        assertTrue(sender.getOkToByeReceived());
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(30000);
        // ensure new initial requests are not handled anymore by the app
        assertEquals(404, sender.getFinalResponseStatus());
        // make sure the Graceful Stop was effective
        assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
        tomcat.stopTomcat();
        senderProtocolObjects.destroy();
    }

    /**
     * Make sure that the graceful stop guarantees the application is stopped
     * forcefully by using 0 as time to Wait
     */
    public void testContextGracefulShutdownZeroValue() throws Exception {
        tomcat.startTomcat();

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();

        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("testPort", String.valueOf(myPort));
        params.put("servletContainerPort", String.valueOf(containerPort));        
        SipContext sipContext = deployShootme(null,params);         

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setTimeToWaitBeforeBye(TIMEOUT * 3 / 2);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(1000);
        sipContext.stopGracefully(0);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        Thread.sleep(TIMEOUT);
        // ensure the subsequent requests are responded too
        assertEquals(500, sender.getFinalResponseStatus());
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(30000);
        // ensure new initial requests are not handled anymore by the app
        assertEquals(404, sender.getFinalResponseStatus());
        // make sure the Graceful Stop was effective
        assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
        tomcat.stopTomcat();
        senderProtocolObjects.destroy();
    }

    /**
     * Make sure that the graceful stop guarantees the application is stopped
     * forcefully after TIMEOUT and that the result is equivalent to zero value
     * in our case
     */
    public void testContextGracefulShutdownPositiveValue() throws Exception {
        tomcat.startTomcat();

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();

        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("testPort", String.valueOf(myPort));
        params.put("servletContainerPort", String.valueOf(containerPort));        
        SipContext sipContext = deployShootme(null,params);         

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setTimeToWaitBeforeBye(TIMEOUT * 3 / 2);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(1000);
        sipContext.stopGracefully(TIMEOUT);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        Thread.sleep(TIMEOUT);
        // ensure the subsequent requests are responded too
        assertEquals(500, sender.getFinalResponseStatus());
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(30000);
        // ensure new initial requests are not handled anymore by the app
        assertEquals(404, sender.getFinalResponseStatus());
        // make sure the Graceful Stop was effective
        assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
        tomcat.stopTomcat();
        senderProtocolObjects.destroy();
    }

    /**
     * Make sure that the graceful stop guarantees the application is accepting
     * subsequent requests but not new requests and that when no sessions are
     * present the context is stopped automatically
     */
    public void testServiceGracefulShutdownNegativeValue() throws Exception {
        tomcat.startTomcat();

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();

        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("testPort", String.valueOf(myPort));
        params.put("servletContainerPort", String.valueOf(containerPort));        
        SipContext sipContext = deployShootme(null,params);         

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setTimeToWaitBeforeBye(TIMEOUT * 3 / 2);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(1000);
        tomcat.getSipService().stopGracefully(-1);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        Thread.sleep(TIMEOUT);
        // ensure the subsequent requests are responded too
        assertTrue(sender.getOkToByeReceived());
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(30000);
        // ensure new initial requests are not handled anymore by the app
        assertEquals(404, sender.getFinalResponseStatus());
        // make sure the Graceful Stop was effective
        assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
        senderProtocolObjects.destroy();
    }

    /**
     * Make sure that the graceful stop guarantees the application is accepting
     * subsequent requests but not new requests and that when no sessions are
     * present the context is stopped automatically
     */
    public void testServiceGracefulShutdownNegativeValueJMX() throws Exception {
        tomcat.startTomcat();

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();

        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("testPort", String.valueOf(myPort));
        params.put("servletContainerPort", String.valueOf(containerPort));        
        SipContext sipContext = deployShootme(null,params);         

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setTimeToWaitBeforeBye(TIMEOUT * 3 / 2);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(1000);

//		tomcat.getSipService().stopGracefully(-1);
        MBeanServer mBeanServer = null;
        if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
            mBeanServer
                    = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
        }
        ObjectName objectName
                = tomcat.getSipService().getObjectName();
        mBeanServer.invoke(objectName, "stopGracefully", new Object[]{-1}, new String[]{long.class.getName()});

        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        Thread.sleep(TIMEOUT);
        // ensure the subsequent requests are responded too
        assertTrue(sender.getOkToByeReceived());
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(30000);
        // ensure new initial requests are not handled anymore by the app
        assertEquals(404, sender.getFinalResponseStatus());
        // make sure the Graceful Stop was effective
        assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
        senderProtocolObjects.destroy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void deployApplication() {

    }

    /**
     * Make sure that the graceful stop guarantees the application is accepting
     * subsequent HTTP requests but not new HTTP requests and that when no
     * sessions are present the context is stopped automatically
     */
    public void testConvergedContextGracefulShutdownNegativeValue() throws Exception {
        String CLICK2DIAL_URL = "http://" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + httpContainerPort + "/click2call/call";
        tomcat.startTomcat();

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();

        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("testPort", String.valueOf(myPort));
        params.put("servletContainerPort", String.valueOf(containerPort));        
        SipContext sipContext = deployConvergedApp(params);         

	String CLICK2DIAL_PARAMS = "?from=sip:from@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5056&to=sip:to@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + myPort+"&notification=false";
        
        logger.info("Trying to reach url : " + CLICK2DIAL_URL
                + CLICK2DIAL_PARAMS);

        URL url = new URL(CLICK2DIAL_URL + CLICK2DIAL_PARAMS);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        InputStream in = httpConnection.getInputStream();

        byte[] buffer = new byte[10000];
        int len = in.read(buffer);
        String httpResponse = "";
        for (int q = 0; q < len; q++) {
            httpResponse += (char) buffer[q];
        }
        logger.info("Received the follwing HTTP response: " + httpResponse);

        sipContext.stopGracefully(-1);

        Thread.sleep(40000);
        logger.info("Trying to access the context after gracefull stop");

        url = new URL(CLICK2DIAL_URL + CLICK2DIAL_PARAMS);
        try {
            in = url.openConnection().getInputStream();
            fail("new request should thrown a 404");
        } catch (FileNotFoundException e) {
            // expected
        }

        Thread.sleep(35000);

        assertEquals(0, ((StandardContext) sipContext).getManager().getActiveSessions());
        assertEquals(0, sipContext.getSipManager().getActiveSipApplicationSessions());
        tomcat.stopTomcat();
        senderProtocolObjects.destroy();
    }
}
