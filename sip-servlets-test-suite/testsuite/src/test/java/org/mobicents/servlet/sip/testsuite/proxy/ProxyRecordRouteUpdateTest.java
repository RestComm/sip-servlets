package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyRecordRouteUpdateTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ProxyRecordRouteUpdateTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	
	private static final int TIMEOUT = 20000;

	public ProxyRecordRouteUpdateTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();				
	}

	//https://code.google.com/p/mobicents/issues/detail?id=2264
	//Call flow replicated the RFC3311 example but within the context of a record-route proxy
	//No PRACK involved, UPDATE is used to refresh the session-timer
	public void testProxySendUpdateBeforeFinalResponse() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-update";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitBeforeFinalResponse(15000);
		
		sender.setSendUpdateOn180(true);
		receiver.setSendUpdateAfterUpdate(true);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT*2);		
		assertTrue(sender.isSendUpdate());
		assertTrue(receiver.isUpdateReceived());
		assertFalse(receiver.isSendUpdateAfterUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isUpdateReceived());
		assertTrue(receiver.isSendUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	//https://code.google.com/p/mobicents/issues/detail?id=2264
	//Only the receiver sends UPDATE
	public void testProxyReceiverSendUpdateBeforeFinalResponse() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-update";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitBeforeFinalResponse(15000);
		
		receiver.setSendUpdateAfterProvisionalResponses(true);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT*2);		
		assertTrue(receiver.isSendUpdate());
		assertTrue(sender.isUpdateReceived());
		assertFalse(receiver.isSendUpdateAfterUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	//https://code.google.com/p/mobicents/issues/detail?id=2264
	//Call flow replicated the RFC3311 example but within the context of a record-route proxy
	//PRACK involved
	public void testProxySendUpdateBeforeFinalResponseWithPRACK() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-udpate";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitForCancel(true);
		
		sender.setSendUpdateAfterPrack(true);
		receiver.setSendUpdateAfterUpdate(true);
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);	
		Thread.sleep(TIMEOUT*2);		
		assertTrue(sender.isSendUpdate());
		assertTrue(receiver.isUpdateReceived());
//		assertFalse(receiver.isSendUpdateAfterUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isUpdateReceived());
		assertTrue(receiver.isSendUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());	
	}
	
	public void setupPhones(String transport) throws Exception {
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", transport, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", transport, AUTODIALOG, null, "3", "true");
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