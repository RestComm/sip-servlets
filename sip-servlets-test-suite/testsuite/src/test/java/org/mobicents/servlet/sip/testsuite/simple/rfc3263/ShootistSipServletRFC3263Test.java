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
package org.mobicents.servlet.sip.testsuite.simple.rfc3263;

import gov.nist.javax.sip.stack.HopImpl;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.Hop;
import javax.sip.header.ContactHeader;
import javax.sip.header.RouteHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSLookupPerformer;
import org.mobicents.ext.javax.sip.dns.DefaultDNSLookupPerformer;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;
import org.mobicents.servlet.sip.testsuite.simple.ShootistSipServletTest;
import org.xbill.DNS.DClass;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;

public class ShootistSipServletRFC3263Test extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootistSipServletRFC3263Test.class);
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 80000;
    private static final int DIALOG_TIMEOUT = 40000;



    public ShootistSipServletRFC3263Test(String name) {
        super(name);
        startTomcatOnStartup = false;
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat.deployContext(
                projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp",
                "sip-test-context", "sip-test"));
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("javax.net.ssl.keyStore", ShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.trustStore", ShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        containerPort = NetworkPortAssigner.retrieveNextPort();        
        super.setUp();
    }

    /*
	 * Making sure the procedures of retrying the next hop of RFC 3263 are working
     */
    public void testShootist() throws Exception {
        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(receiverProtocolObjects);
        
      
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        ProtocolObjects badReceiverProtocolObjects = new ProtocolObjects(
                "bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiverProtocolObjects);
        int badReceiverPort = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver = new TestSipListener(badReceiverPort, containerPort, badReceiverProtocolObjects, false); 
        SipProvider badReceiverProvider = badReceiver.createProvider();
        badReceiverProvider.addSipListener(badReceiver);
        badReceiverProtocolObjects.start();
        badReceiver.setDropRequest(true);

        tomcat.startTomcat();

        

        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort)); 
        String host = "mobicents.org";        
        ctxAtts.put("host", host);          
        mockDNSLookup(host, "udp", myPort);        
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getInviteRequest());
        RouteHeader routeHeader = (RouteHeader) receiver.getInviteRequest().getHeader(RouteHeader.NAME);
        assertNull(routeHeader);
        assertFalse(badReceiver.getByeReceived());
        assertTrue(receiver.getByeReceived());
    }

    /*
	 * Non Regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=3140
     */
    public void testShootistTLSwithTLSConnector() throws Exception {
        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", ListeningPoint.TLS, AUTODIALOG, null, null, null);
        testResources.add(receiverProtocolObjects);
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false); 
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        ProtocolObjects badReceiverProtocolObjects = new ProtocolObjects(
                "bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiverProtocolObjects);
        int badReceiverPort = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver = new TestSipListener(badReceiverPort, containerPort, badReceiverProtocolObjects, false); 
        SipProvider badReceiverProvider = badReceiver.createProvider();
        badReceiverProvider.addSipListener(badReceiver);
        badReceiverProtocolObjects.start();
        badReceiver.setDropRequest(true);
        String host = "mobicents.org";

        tomcat.startTomcat();
        tomcat.removeConnector(sipConnector);
        tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TLS);

        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));       
        ctxAtts.put("host", host);
        ctxAtts.put("method", "REGISTER");
        ctxAtts.put("secureRURI", "true");         
        mockDNSLookup(host, "tls", myPort);         
        deployShootist(ctxAtts, null);

        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getRegisterReceived());
        RouteHeader routeHeader = (RouteHeader) receiver.getRegisterReceived().getHeader(RouteHeader.NAME);
        assertNull(routeHeader);
        assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));
    }

    /*
	 * Non Regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=3140
     */
    public void testShootistTLSwithTCPConnector() throws Exception {
        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", ListeningPoint.TCP, AUTODIALOG, null, null, null);
        testResources.add(receiverProtocolObjects);
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false); 
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        ProtocolObjects badReceiverProtocolObjects = new ProtocolObjects(
                "bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiverProtocolObjects);
        int badReceiverPort = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver = new TestSipListener(badReceiverPort, containerPort, badReceiverProtocolObjects, false); 
        SipProvider badReceiverProvider = badReceiver.createProvider();
        badReceiverProvider.addSipListener(badReceiver);
        badReceiverProtocolObjects.start();
        badReceiver.setDropRequest(true);
        String host = "mobicents.org";

        tomcat.startTomcat();
        tomcat.removeConnector(sipConnector);
        tomcat.addSipConnector(serverName, sipIpAddress, containerPort, ListeningPoint.TCP);

        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));       
        ctxAtts.put("host", host);
        ctxAtts.put("method", "REGISTER");
        ctxAtts.put("secureRURI", "true");         
        mockDNSLookup(host, "tcp", myPort);         
        deployShootist(ctxAtts, null);

        Thread.sleep(TIMEOUT);
        RouteHeader routeHeader = (RouteHeader) receiver.getRegisterReceived().getHeader(RouteHeader.NAME);
        assertNull(routeHeader);
        assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));
    }

    /*
     * Non Regression test for Issue https://code.google.com/p/sipservlets/issues/detail?id=250
     */
    public void testShootistTCP() throws Exception {
        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", ListeningPoint.TCP, AUTODIALOG, null, null, null);
        testResources.add(receiverProtocolObjects);
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        ProtocolObjects badReceiverProtocolObjects = new ProtocolObjects(
                "bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiverProtocolObjects);
        int badReceiverPort = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver = new TestSipListener(badReceiverPort, containerPort, badReceiverProtocolObjects, false); 
        SipProvider badReceiverProvider = badReceiver.createProvider();
        badReceiverProvider.addSipListener(badReceiver);
        badReceiverProtocolObjects.start();
        badReceiver.setDropRequest(true);
        String host = "mobicents.org";

        tomcat.startTomcat();
        tomcat.removeConnector(sipConnector);
        tomcat.addSipConnector(serverName, sipIpAddress, containerPort, ListeningPoint.TCP);



        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));       
        ctxAtts.put("host", host);
        ctxAtts.put("method", "REGISTER");
        ctxAtts.put("route", "sip:mobicents.org;transport=tcp");        
        mockDNSLookupTCP(host, myPort, new int[]{badReceiverPort,badReceiverPort});         
        deployShootist(ctxAtts, null);

        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getRegisterReceived());
        RouteHeader routeHeader = (RouteHeader) receiver.getRegisterReceived().getHeader(RouteHeader.NAME);
        assertNull(routeHeader);
        assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));
    }

    /*
	 * Non Regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2346
     */
    public void testShootistRouteCheck() throws Exception {
        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(receiverProtocolObjects);
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        String host = "mobicents.org";

        tomcat.startTomcat();

        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));       
        ctxAtts.put("host", host);
        ctxAtts.put("route", "sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" +myPort+ ";lr");       
        deployShootist(ctxAtts, null);

        Thread.sleep(TIMEOUT / 4);
        assertTrue(receiver.isInviteReceived());
        ListIterator<RouteHeader> routeHeaders = receiver.getInviteRequest().getHeaders(RouteHeader.NAME);
        assertNotNull(routeHeaders);
        RouteHeader routeHeader = routeHeaders.next();
        assertFalse(routeHeaders.hasNext());
        assertEquals("sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" +myPort+ ";lr", routeHeader.getAddress().getURI().toString());
        assertTrue(receiver.getByeReceived());
    }

    private void mockDNSLookup(String host, String transport, int hopsPort) throws TextParseException {
        DNSLookupPerformer dnsLookupPerformer = mock(DefaultDNSLookupPerformer.class);
        //mocking the DNS Lookups to match our test cases
        tomcat.getSipService().getSipApplicationDispatcher().getDNSServerLocator().setDnsLookupPerformer(dnsLookupPerformer);

        Set<String> supportedTransports = new HashSet<String>();
        supportedTransports.add(TRANSPORT.toUpperCase());
        supportedTransports.add(ListeningPoint.TCP.toUpperCase());
        supportedTransports.add(ListeningPoint.TLS.toUpperCase());

        Queue<Hop> hops = new ConcurrentLinkedQueue();
        hops = new ConcurrentLinkedQueue();
        //dont use "localhost" or DNS will not work (wouldnt be external)
        hops.add(new HopImpl("127.0.0.1", hopsPort, transport));
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", hopsPort, transport)).thenReturn(hops);
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", 5082, transport)).thenReturn(null);
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", 5081, transport)).thenReturn(null);

        List<NAPTRRecord> mockedNAPTRRecords = new LinkedList<NAPTRRecord>();
        // mocking the name because localhost is not absolute and localhost. cannot be resolved 
        Name name = mock(Name.class);
        when(name.isAbsolute()).thenReturn(true);
        when(name.toString()).thenReturn("localhost");
        mockedNAPTRRecords.add(new NAPTRRecord(new Name(host + "."), DClass.IN, 1000, 0, 0, "s", "SIP+D2U", "", new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + ".")));
        when(dnsLookupPerformer.performNAPTRLookup(host, false, supportedTransports)).thenReturn(mockedNAPTRRecords);
        List<Record> mockedSRVRecords = new LinkedList<Record>();
        mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, hopsPort, name));
        mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 0, 0, 5081, name));
        when(dnsLookupPerformer.performSRVLookup("_sip._" + TRANSPORT.toLowerCase() + "." + host)).thenReturn(mockedSRVRecords);
        List<Record> mockedSRVTCPRecords = new LinkedList<Record>();
        mockedSRVTCPRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, hopsPort, name));
        mockedSRVTCPRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 0, 0, 5081, name));
//		mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5081, name));
        when(dnsLookupPerformer.performSRVLookup("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host)).thenReturn(mockedSRVTCPRecords);

        List<Record> mockedSRVTLSRecords = new LinkedList<Record>();
        mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, hopsPort, name));
        mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 0, 0, 5081, name));
//		mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, 5081, name));
        when(dnsLookupPerformer.performSRVLookup("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host)).thenReturn(mockedSRVTLSRecords);
    }

    private void mockDNSLookupTCP(String host, int receiverPort, int[] badReceiverPort) throws TextParseException {
        DNSLookupPerformer dnsLookupPerformer = mock(DefaultDNSLookupPerformer.class);
        //mocking the DNS Lookups to match our test cases
        tomcat.getSipService().getSipApplicationDispatcher().getDNSServerLocator().setDnsLookupPerformer(dnsLookupPerformer);

        Set<String> supportedTransports = new HashSet<String>();
        supportedTransports.add(TRANSPORT.toUpperCase());
        supportedTransports.add(ListeningPoint.TCP.toUpperCase());

        List<NAPTRRecord> mockedNAPTRRecords = new LinkedList<NAPTRRecord>();
        // mocking the name because localhost is not absolute and localhost. cannot be resolved 
        Name name = mock(Name.class);
        when(name.isAbsolute()).thenReturn(true);
        when(name.toString()).thenReturn("localhost");
        mockedNAPTRRecords.add(new NAPTRRecord(new Name(host + "."), DClass.IN, 1000, 0, 0, "s", "SIP+D2T", "", new Name("_sip._" + ListeningPoint.TCP.toLowerCase() + "." + host + ".")));
        when(dnsLookupPerformer.performNAPTRLookup(host, false, supportedTransports)).thenReturn(mockedNAPTRRecords);

        Queue<Hop> hops = new ConcurrentLinkedQueue();
        hops = new ConcurrentLinkedQueue();
        //dont use "localhost" or DNS will not work (wouldnt be external)
        hops.add(new HopImpl("127.0.0.1", receiverPort, "tcp"));
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", badReceiverPort[0], "tcp")).thenReturn(null);
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", badReceiverPort[1], "tcp")).thenReturn(null);
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", receiverPort, "tcp")).thenReturn(hops);

        List<Record> mockedSRVTCPRecords = new LinkedList<Record>();
        mockedSRVTCPRecords.add(new SRVRecord(new Name("_sip._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 0, 0, badReceiverPort[0], name));
        mockedSRVTCPRecords.add(new SRVRecord(new Name("_sip._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 2, 0, receiverPort, name));
        mockedSRVTCPRecords.add(new SRVRecord(new Name("_sip._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, badReceiverPort[1], name));
        when(dnsLookupPerformer.performSRVLookup("_sip._" + ListeningPoint.TCP.toLowerCase() + "." + host)).thenReturn(mockedSRVTCPRecords);
    }

    private void mockDNSLookup4Records(String host, int receiverPort, int[] badReceiverPorts) throws TextParseException {
        DNSLookupPerformer dnsLookupPerformer = mock(DefaultDNSLookupPerformer.class);
        //mocking the DNS Lookups to match our test cases
        tomcat.getSipService().getSipApplicationDispatcher().getDNSServerLocator().setDnsLookupPerformer(dnsLookupPerformer);

        Set<String> supportedTransports = new HashSet<String>();
        supportedTransports.add(TRANSPORT.toUpperCase());
        supportedTransports.add(ListeningPoint.TCP.toUpperCase());
        supportedTransports.add(ListeningPoint.TLS.toUpperCase());

        Queue<Hop> hops = new ConcurrentLinkedQueue();
        hops = new ConcurrentLinkedQueue();
        //dont use "localhost" or DNS will not work (wouldnt be external)
        hops.add(new HopImpl("127.0.0.1", receiverPort, TRANSPORT.toLowerCase()));
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", receiverPort, TRANSPORT.toLowerCase())).thenReturn(hops);
        hops = new ConcurrentLinkedQueue();
        //dont use "localhost" or DNS will not work (wouldnt be external)
        hops.add(new HopImpl("127.0.0.1", badReceiverPorts[0], TRANSPORT.toLowerCase()));
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", badReceiverPorts[0], TRANSPORT.toLowerCase())).thenReturn(hops);
        hops = new ConcurrentLinkedQueue();
        //dont use "localhost" or DNS will not work (wouldnt be external)
        hops.add(new HopImpl("127.0.0.1", badReceiverPorts[1], TRANSPORT.toLowerCase()));
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", badReceiverPorts[1], TRANSPORT.toLowerCase())).thenReturn(hops);
        hops = new ConcurrentLinkedQueue();
        //dont use "localhost" or DNS will not work (wouldnt be external)
        hops.add(new HopImpl("127.0.0.1", badReceiverPorts[2], TRANSPORT.toLowerCase()));
        when(dnsLookupPerformer.locateHopsForNonNumericAddressWithPort("localhost", badReceiverPorts[2], TRANSPORT.toLowerCase())).thenReturn(hops);

        List<NAPTRRecord> mockedNAPTRRecords = new LinkedList<NAPTRRecord>();
        // mocking the name because localhost is not absolute and localhost. cannot be resolved 
        Name name = mock(Name.class);
        when(name.isAbsolute()).thenReturn(true);
        when(name.toString()).thenReturn("localhost");
        mockedNAPTRRecords.add(new NAPTRRecord(new Name(host + "."), DClass.IN, 1000, 0, 0, "s", "SIP+D2U", "", new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + ".")));
        when(dnsLookupPerformer.performNAPTRLookup(host, false, supportedTransports)).thenReturn(mockedNAPTRRecords);
        List<Record> mockedSRVRecords = new LinkedList<Record>();
        mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 3, 0, receiverPort, name));
        mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 0, 0, badReceiverPorts[0], name));
        mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, badReceiverPorts[1], name));
        mockedSRVRecords.add(new SRVRecord(new Name("_sip._" + TRANSPORT.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 2, 0, badReceiverPorts[2], name));
        when(dnsLookupPerformer.performSRVLookup("_sip._" + TRANSPORT.toLowerCase() + "." + host)).thenReturn(mockedSRVRecords);
        List<Record> mockedSRVTCPRecords = new LinkedList<Record>();
        mockedSRVTCPRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, badReceiverPorts[0], name));
        when(dnsLookupPerformer.performSRVLookup("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host)).thenReturn(mockedSRVTCPRecords);

        List<Record> mockedSRVTLSRecords = new LinkedList<Record>();
        mockedSRVTLSRecords.add(new SRVRecord(new Name("_sips._" + ListeningPoint.TCP.toLowerCase() + "." + host + "."), DClass.IN, 1000L, 1, 0, badReceiverPorts[0], name));
        when(dnsLookupPerformer.performSRVLookup("_sips._" + ListeningPoint.TLS.toLowerCase() + "." + host)).thenReturn(mockedSRVTLSRecords);
    }

    /*
	 * Making sure the procedures of retrying the next hop of RFC 3263 are working
	 * and that the same hop is used for CANCEL
     */
    public void testShootistCancel() throws Exception {
        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(receiverProtocolObjects);
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false); 
        receiver.setWaitForCancel(true);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);

        ProtocolObjects badReceiverProtocolObjects = new ProtocolObjects(
                "bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiverProtocolObjects);
        int badReceiverPort = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver = new TestSipListener(badReceiverPort, containerPort, badReceiverProtocolObjects, false); 
        SipProvider badReceiverProvider = badReceiver.createProvider();
        badReceiverProvider.addSipListener(badReceiver);
        badReceiverProtocolObjects.start();
        badReceiver.setDropRequest(true);

        String host = "mobicents.org";


        receiverProtocolObjects.start();
        tomcat.startTomcat();

        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort)); 
        ctxAtts.put("host", host);  
        ctxAtts.put("cancelOn1xx", "true");        
        mockDNSLookup(host, "udp", myPort);        
        deployShootist(ctxAtts, null);

        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        assertFalse(badReceiver.getByeReceived());
        assertFalse(badReceiver.isCancelReceived());
        RouteHeader routeHeader = (RouteHeader) receiver.getInviteRequest().getHeader(RouteHeader.NAME);
        assertNull(routeHeader);
        assertTrue(receiver.isCancelReceived());
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertTrue(allMessagesContent.size() >= 2);
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
    }

    /*
	 * Making sure the procedures of retrying the next hop of RFC 3263 are working
	 * and that the ACK to an error response uses the same hop
	 * 
	 * Updated for https://code.google.com/p/sipservlets/issues/detail?id=249
     */
    public void testShootistErrorResponse() throws Exception {
        Map<String, String> additionalProps = new HashMap<String, String>();
        additionalProps.put(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");

        ProtocolObjects receiverProtocolObjects = new ProtocolObjects(
                "receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null, additionalProps);
        testResources.add(receiverProtocolObjects);
        int myPort = NetworkPortAssigner.retrieveNextPort();                
        TestSipListener receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false); 
        receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
        receiver.setFinalResponseToSend(Response.SERVER_INTERNAL_ERROR);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        ProtocolObjects badReceiverProtocolObjects = new ProtocolObjects(
                "bad-receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null, additionalProps);
        testResources.add(badReceiverProtocolObjects);
        int badReceiverPort = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver = new TestSipListener(badReceiverPort, containerPort, badReceiverProtocolObjects, false); 
        SipProvider badReceiverProvider = badReceiver.createProvider();
        badReceiverProvider.addSipListener(badReceiver);
        badReceiverProtocolObjects.start();
        badReceiver.setDropRequest(true);

        ProtocolObjects badReceiver2ProtocolObjects = new ProtocolObjects(
                "bad-receiver2", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiver2ProtocolObjects);
        int badReceiverPort2 = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver2 = new TestSipListener(badReceiverPort2, containerPort, badReceiver2ProtocolObjects, false); 
        SipProvider badReceiver2Provider = badReceiver2.createProvider();
        badReceiver2Provider.addSipListener(badReceiver2);
        badReceiver2ProtocolObjects.start();
        badReceiver2.setDropRequest(true);

        ProtocolObjects badReceiver3ProtocolObjects = new ProtocolObjects(
                "bad-receiver3", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        testResources.add(badReceiver3ProtocolObjects);
        int badReceiverPort3 = NetworkPortAssigner.retrieveNextPort();                     
        TestSipListener badReceiver3 = new TestSipListener(badReceiverPort3, containerPort, badReceiver3ProtocolObjects, false); 
        SipProvider badReceiver3Provider = badReceiver3.createProvider();
        badReceiver3Provider.addSipListener(badReceiver3);
        badReceiver3ProtocolObjects.start();
        badReceiver3.setDropRequest(true);

        String host = "mobicents.org";

        tomcat.startTomcat();

        

        Map<String,String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort)); 
        ctxAtts.put("host", host);  
        ctxAtts.put("testErrorResponse", "true");       
        mockDNSLookup4Records(host, myPort, new int[]{badReceiverPort,badReceiverPort2,badReceiverPort3});       
        deployShootist(ctxAtts, null);

        Thread.sleep(DIALOG_TIMEOUT * 3 + TIMEOUT);
        assertTrue(badReceiver.isInviteReceived());
        assertTrue(badReceiver2.isInviteReceived());
        //assertTrue(badReceiver3.isInviteReceived());
        assertFalse(badReceiver.isAckReceived());
        assertFalse(badReceiver2.isAckReceived());
        //assertFalse(badReceiver3.isAckReceived());
        //assertTrue(receiver.isAckReceived());
        assertTrue(receiver.isInviteReceived());
        RouteHeader routeHeader = (RouteHeader) receiver.getInviteRequest().getHeader(RouteHeader.NAME);
        assertNull(routeHeader);
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(2, allMessagesContent.size());
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
    }

    @Override
    protected void tearDown() throws Exception {
        logger.info("Test completed");
        super.tearDown();
    }
}
