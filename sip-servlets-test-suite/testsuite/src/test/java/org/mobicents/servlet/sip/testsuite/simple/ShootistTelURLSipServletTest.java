/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
package org.mobicents.servlet.sip.testsuite.simple;

import java.util.HashMap;
import java.util.Map;

import javax.sip.SipProvider;
import javax.sip.header.ToHeader;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootistTelURLSipServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootistTelURLSipServletTest.class);
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener receiver;

    ProtocolObjects receiverProtocolObjects;

    private int receiverPort;

    public ShootistTelURLSipServletTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    protected void deployApplication() {
        // TODO Auto-generated method stub

    }

    public void deployApplication(Map<String, String> params) {
        SipStandardContext context = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp",
                "sip-test",
                params,
                null);
        assertTrue(context.getAvailable());
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();

        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        receiverPort = NetworkPortAssigner.retrieveNextPort();
        receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();
        senderProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
    }

    public void testShootistTelURL() throws Exception {
        //tomcat.startTomcat();
        
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(receiverPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("urlType", "tel");
        deployShootist(ctxAtts, null);
        
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    /*
	 * https://code.google.com/p/sipservlets/issues/detail?id=170
     */
    public void testShootistTelURLFlagParameter() throws Exception {
        //tomcat.startTomcat();
        
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(receiverPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("urlType", "tel");
        ctxAtts.put("flag", "ndpi");        
        deployShootist(ctxAtts, null);
        
        Thread.sleep(TIMEOUT);
        assertEquals("tel:+358-555-1234567;ndpi", ((ToHeader) receiver.getInviteRequest().getHeader(ToHeader.NAME)).getAddress().getURI().toString());
        assertTrue(receiver.getByeReceived());
    }

    public void testShootistTelURLAsSIP() throws Exception {
        //tomcat.startTomcat();
        
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(receiverPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("urlType", "telAsSip");       
        deployShootist(ctxAtts, null);
        
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    @Override
    protected void tearDown() throws Exception {
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }
}
