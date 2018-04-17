/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.mobicents.servlet.sip.testsuite.routing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import static junit.framework.Assert.assertTrue;
import org.apache.catalina.deploy.ApplicationParameter;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipStack;
import org.junit.Ignore;
import org.junit.Test;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipUnitServletTestCase;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test starts 2 sip servlets container one on 5069 that has the AR setup
 * to ROUTE to the other server on port 5070 that has the
 * LocationServiceSipServlet application installed.
 *
 * Then the test sends a REGISTER that goes through server 1 that routes it to
 * server 2 Location Service sends back OK and it is routed back to UAC => test
 * green
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class ExternalRoutingServletTest extends SipUnitServletTestCase {

    private static transient Logger logger = Logger.getLogger(ExternalRoutingServletTest.class);

    private static final int TIMEOUT = 5000;

    private SipStack sipStackSender;
    SipEmbedded closestTomcat;
    private int closestTomcatPort;

    public ExternalRoutingServletTest(String name) {
        super(name);
        autoDeployOnStartup = false;
        startTomcatOnStartup = false;
    }

    @Override
    public void deployApplication() {

    }

    public SipStandardContext deployApplication(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
                "location-service",
                params,
                null);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    protected SipStandardContext deployApplicationInClosest(SipEmbedded closestTomcat,
            String docBase, String name, Map<String, String> params, ConcurrencyControlMode concurrencyControlMode) {
        SipStandardContext context = new SipStandardContext();
        context.setDocBase(docBase);
        context.setName(name);
        context.setPath("/" + name);
        context.addLifecycleListener(new SipContextConfig());
        context.setManager(new SipStandardManager());
        if (concurrencyControlMode != null) {
            context.setConcurrencyControlMode(concurrencyControlMode);
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
        assertTrue(closestTomcat.deployContext(context));
        return context;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/routing/locationservice-dar.properties";
    }

    protected String getDarConfigurationFileForClosestServer(int port) throws Exception {
        String dar = "REGISTER: (\"org.mobicents.servlet.sip.testsuite.LocationServiceApplication\", \"DAR:From\", \"ORIGINATING\", \"sip:127.0.0.1:" + port + "\", \"ROUTE\", \"0\")";
        File darFile = new File("target/temp_dar.properties");
        FileWriter fWriter = new FileWriter(darFile);
        Writer output;
        output = new BufferedWriter(fWriter);
        output.append(dar);
        output.close();
        String filePath = darFile.toURL().toString();
        //TODO ugly fix for windows test
        if (filePath.startsWith("file:/C")) {
            filePath = "file:///" + darFile.getAbsolutePath();
        } else if (filePath.startsWith("file:/")) {
            //for linux make sure file protocol is as expected
            filePath = "file://" + darFile.getAbsolutePath();
        }
        return filePath;
    }

    @Override
    public void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();
        System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", "org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider");
    }

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    TestSipListener sender;
    int senderPort;
    ProtocolObjects senderProtocolObjects;

    public void testExternalRouting() throws Exception {
        //start the most remote server first
        tomcat.startTomcat();

        //create and start the closest server
        closestTomcatPort = NetworkPortAssigner.retrieveNextPort();
        //starting tomcat
        closestTomcat = new SipEmbedded("SIP-Servlet-Closest-Tomcat-Server", serviceFullClassName);
        closestTomcat.setLoggingFilePath(
                projectHome + File.separatorChar + "sip-servlets-test-suite"
                + File.separatorChar + "testsuite"
                + File.separatorChar + "src"
                + File.separatorChar + "test"
                + File.separatorChar + "resources"
                + File.separatorChar + "org"
                + File.separatorChar + "mobicents"
                + File.separatorChar + "servlet"
                + File.separatorChar + "sip"
                + File.separatorChar + "testsuite"
                + File.separatorChar + "routing"
                + File.separatorChar + "closest"
                + File.separatorChar
        );
        logger.info("Log4j path is : " + closestTomcat.getLoggingFilePath());
        closestTomcat.setDarConfigurationFilePath(getDarConfigurationFileForClosestServer(containerPort));
        closestTomcat.initTomcat(tomcatBasePath, null);
        closestTomcat.addSipConnector("SIP-Servlet-Closest-Tomcat-Server", sipIpAddress, closestTomcatPort, ListeningPoint.UDP);
        closestTomcat.startTomcat();
        Thread.sleep(TIMEOUT);

        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("senderPort", String.valueOf(senderPort));
        deployApplication(params);

        deployApplicationInClosest(closestTomcat,
                projectHome + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
                "location-service",
                params, null);

        int senderPort = NetworkPortAssigner.retrieveNextPort();
        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, closestTomcatPort, senderProtocolObjects, false);
        SipProvider senderProvider = sender.createProvider();
        senderProvider.addSipListener(sender);
        senderProtocolObjects.start();

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toSipAddress = "sip-servlets.com";
        String toUser = "receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.sendSipRequest("REGISTER", fromAddress, toAddress, null, null, false, null, null, true);
        Thread.sleep(TIMEOUT);
        assertEquals("REGISTER correctly answered", 200, sender.getFinalResponseStatus());
    }

    @Override
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        senderProtocolObjects.destroy();
        if (sipStackSender != null) {
            sipStackSender.dispose();
        }
        closestTomcat.stopTomcat();
        super.tearDown();
    }

}
