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
package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyREGISTERWithSTARContactTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ProxyREGISTERWithSTARContactTest.class);
    private static final boolean AUTODIALOG = true;
    TestSipListener sender;
    TestSipListener receiver;
    ProtocolObjects senderProtocolObjects;
    ProtocolObjects receiverProtocolObjects;

    private static final int TIMEOUT = 20000;

    public ProxyREGISTERWithSTARContactTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        senderProtocolObjects = new ProtocolObjects("proxy-sender",
                "gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
        receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
                "gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
        sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
        sender.setRecordRoutingProxyTesting(true);
        SipProvider senderProvider = sender.createProvider();

        receiver = new TestSipListener(5057, 5070, receiverProtocolObjects, false);
        receiver.setRecordRoutingProxyTesting(true);
        SipProvider receiverProvider = receiver.createProvider();

        receiverProvider.addSipListener(receiver);
        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        receiverProtocolObjects.start();
    }

    // Tests Issue 1779 https://telestax.desk.com/web/agent/case/1779

    public void testStarContactRegister() throws Exception {
        String fromName = "unique-location-starContactRegister";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toSipAddress = "sip-servlets.com";
        String toUser = "proxy-receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        //we want to test contact header set to *
        //we set expires to 0 just for compliance to actual scenario
        String[] headerNames = {"Contact", "Expires"};
        String[] headerValues = {"*", "0"};

        //it's key to enable setHeader!!, so Contact is properly initialize
        sender.sendSipRequest("REGISTER", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);
        Thread.sleep(TIMEOUT);
        assertEquals(200, sender.getFinalResponseStatus());

    }

    @Override
    public void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat
                .deployContext(
                        projectHome
                        + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                        "sip-test-context", "sip-test"));
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
    }
}
