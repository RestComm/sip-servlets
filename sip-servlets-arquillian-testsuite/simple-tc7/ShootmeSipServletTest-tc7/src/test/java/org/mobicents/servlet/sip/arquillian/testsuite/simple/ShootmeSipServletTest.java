/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.testsuite.simple;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.AllowHeader;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipTransaction;
import org.jboss.arquillian.container.mobicents.api.annotations.GetDeployableContainer;
import org.jboss.arquillian.container.mss.extension.ContainerManagerTool;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.mss.extension.authentication.DigestServerAuthenticationMethod;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.arquillian.testsuite.simple.SimpleSipServlet;

/**
 * @author gvagenas@gmail.com 
 * 
 */
@RunWith(Arquillian.class)
public class ShootmeSipServletTest extends SipTestCase 
{
	private final Logger logger = Logger.getLogger(ShootmeSipServletTest.class.getName());

	@GetDeployableContainer
	private ContainerManagerTool containerManager;

	@ArquillianResource
	private Deployer deployer;

	private SipStack receiver;
	private SipCall sipCallReceiver;
	private SipPhone sipPhoneReceiver;
	private String receiverPort = "5058";
	private String receiverURI = "sip:receiver@sip-servlets.com";

	private SipStack sender;
	private SipCall sipCallSender;
	private SipPhone sipPhoneSender;
	private String senderPort = "5080";
	private String senderURI = "sip:sender@sip-servlets.com";

	private int proxyPort = 5070;

	private static final int TIMEOUT = 10000;	
	private static final int TIMEOUT_CSEQ_INCREASE = 100000;
	private static final int DIALOG_TIMEOUT = 40000;

	public final static String[] ALLOW_HEADERS = new String[] {"INVITE","ACK","CANCEL","OPTIONS","BYE","SUBSCRIBE","NOTIFY","REFER"};

	private static SipStackTool receiverSipStackTool;
	private static SipStackTool senderSipStackTool;
	private String testArchive = "simple";
	private Boolean isDeployed = false;

	long cseqInt = 0;
	Semaphore semaphore = new Semaphore(1);

	@BeforeClass
	public static void beforeClass(){
		receiverSipStackTool = new SipStackTool("ShootmeSipServletTest_receiver");
		senderSipStackTool = new SipStackTool("ShootmeSipServletTest_sender");
	}

	@Before
	public void setUp() throws Exception
	{
		//Create the sipCall and start listening for messages
		receiver = receiverSipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", receiverPort, "127.0.0.1:5070");
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();
		sipCallReceiver.listenForIncomingCall();

		sender = senderSipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", senderPort, "127.0.0.1:5070");
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();
		sipCallSender.listenForIncomingCall();
	}

	@After
	public void tearDown() throws Exception
	{
		//Log last Error, Exception, and returnCode
		logger.info("Error Message: "+sipCallSender.getErrorMessage());
		logger.info("Exception Message: "+sipCallSender.getException());
		logger.info("Return Code: "+sipCallSender.getReturnCode());

		logger.info("About to un-deploy the application");
		if (isDeployed)
			deployer.undeploy(testArchive);
		if(sipCallSender != null)	sipCallSender.disposeNoBye();
		if(sipPhoneSender != null) sipPhoneSender.dispose();
		if(sender != null) sender.dispose();

		if(sipCallReceiver != null) sipCallReceiver.disposeNoBye();
		if(sipPhoneReceiver != null) sipPhoneReceiver.dispose();
		if(receiver != null) receiver.dispose();

	}

	@Deployment(name="simple", managed=false, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootmesipservlet.war");
		webArchive.addClasses(SimpleSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}

	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||

	@Test
	public void testShootme() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;

		sipCallSender.initiateOutgoingCall(receiverURI, null);

		Thread.sleep(TIMEOUT);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.sendInviteOkAck());
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(TIMEOUT);
		// test non regression for Issue 1687 : Contact Header is present in SIP Message where it shouldn't
		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());
		assertNull(response.getMessage().getHeader(ContactHeader.NAME));
	}

	/*
	 * Non regression test for Issue 2115 http://code.google.com/p/mobicents/issues/detail?id=2115
	 * MSS unable to handle GenericURI URIs
	 */
	@Test //@Ignore //SipUnit doesn't support URN
	public void testShootmeGenericRURI() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;

		URI toAddress = sender.getAddressFactory().createURI("urn:service:sos");
		sipCallSender.initiateOutgoingCall(toAddress.toString(), null);

		logger.info("Last exception: "+sipCallSender.getErrorMessage());

		Thread.sleep(TIMEOUT);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.sendInviteOkAck());
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(TIMEOUT);
		// test non regression for Issue 1687 : Contact Header is present in SIP Message where it shouldn't
		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());
		assertNull(response.getMessage().getHeader(ContactHeader.NAME));
	}

	/*
	 * Non regression test for Issue 2522 http://code.google.com/p/mobicents/issues/detail?id=2522
	 * Cloned URI is not modifiable
	 */
	@Test
	public void testShootmeCloneURI() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;

		//Override default options for sender
		senderURI = "sip:cloneURI@sip-servlet.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.initiateOutgoingCall(receiverURI, null);

		Thread.sleep(TIMEOUT);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.sendInviteOkAck());
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(TIMEOUT);
		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());
	}

	@Test
	public void testShootmeSendByeOnExpire() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replaceToHeaders = new ArrayList<Header>();
		replaceToHeaders.add(toHeader);

		//Add custom Header
		Header addHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> addHeaders = new ArrayList<Header>();
		addHeaders.add(addHeader);

		//Override default options for sender
		senderURI = "sip:byeOnExpire@sip-servlet.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, addHeaders, replaceToHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(5000));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		//Send inDialog Request
		SipTransaction siptrans = sipCallSender.sendReinvite(null, null, "no_body", null, null);

		assertTrue(sipCallSender.waitReinviteResponse(siptrans, TIMEOUT));
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitReinviteResponse(siptrans, TIMEOUT));
		assertEquals(Response.RINGING, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitReinviteResponse(siptrans, TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.sendReinviteOkAck(siptrans));

		assertTrue(sipCallSender.listenForDisconnect());

		//A BYE request is being sent on context destroy (org.mobicents.servlet.sip.arquillian.testsuite.SimpleSipServlet.destroy())
		//So first undeploy the application and then waitForDisconnect()
		deployer.undeploy(testArchive);
		isDeployed = false;

		assertTrue(sipCallSender.waitForDisconnect(70000));
		assertTrue(sipCallSender.respondToDisconnect());

		logger.info("Last error: "+sipCallSender.getErrorMessage());
		logger.info("last received response: "+sipCallSender.getLastReceivedResponse().getMessage().toString());
	}

	@Test
	public void testShootmeExceptionOnExpirationCount() throws InterruptedException, SipException, ParseException, InvalidArgumentException 
	{	
		new File("expirationFailure.tmp").delete();
		assertFalse(new File("expirationFailure.tmp").exists());

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:exceptionOnExpire@sip-servlet.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		sipCallSender.listenForIncomingCall();

		assertTrue(sipCallSender.waitForIncomingCall(TIMEOUT));

		//Replace ContactHeader
		Address contactAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhoneSender.getParent().getHeaderFactory().createContactHeader(contactAddr);
		replacedHeaders.add(contactHeader);

		assertTrue(sipCallSender.sendIncomingCallResponse(Response.TRYING,"Trying", -1));
		assertTrue(sipCallSender.sendIncomingCallResponse(Response.TRYING,"Trying", -1, null,replacedHeaders, null));	
		assertTrue(sipCallSender.sendIncomingCallResponse(Response.RINGING,"RINGING",-1,null,replacedHeaders,null));
		assertTrue(sipCallSender.sendIncomingCallResponse(Response.OK, "OK", -1,null,replacedHeaders,null));

		sipCallSender.listenForDisconnect();

		assertTrue(sipCallSender.waitForDisconnect(TIMEOUT));
		assertTrue(sipCallSender.respondToDisconnect(200,"ΟΚ",null,replacedHeaders,null));

		Thread.sleep(TIMEOUT);

		assertFalse(new File("expirationFailure.tmp").exists());
	}

	@Test
	public void testShootme491() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		int numberOf491s = 0;

		containerManager.getSipStandardService().setDialogPendingRequestChecking(true);

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:exceptionOnExpire@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(1000);

		sipCallSender.listenForIncomingCall();

		sipCallSender.waitForIncomingCall(TIMEOUT);

		//Replace ContactHeader
		Address contactAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("Shootme <sip:27.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhoneSender.getParent().getHeaderFactory().createContactHeader(contactAddr);
		replacedHeaders.clear();
		replacedHeaders.add(contactHeader);

		Thread.sleep(1000);
		assertTrue(sipCallSender.sendIncomingCallResponse(Response.TRYING,"Trying", -1, null,replacedHeaders, null));	

		//		Send inDialog Request
		SipTransaction siptrans1 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans2 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);


		assertTrue(sipCallSender.waitReinviteResponse(siptrans1, TIMEOUT));
		ResponseEvent resp491 = sipCallSender.getLastReceivedResponse().getResponseEvent();

		logger.info("resp491: "+resp491.getResponse().toString());

		if (resp491.getResponse().getStatusCode() == Response.REQUEST_PENDING)
			++numberOf491s;

		sipCallSender.waitReinviteResponse(siptrans2, TIMEOUT);
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());

		sipCallSender.sendIncomingCallResponse(Response.RINGING, "RINGING", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);

		Thread.sleep(6500);

		assertTrue(numberOf491s > 0);
	}

	@Test
	public void testShootme491withRetrans() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		
		int numberOf491s = 0;
		
		containerManager.getSipStandardService().setDialogPendingRequestChecking(true);

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:exceptionOnExpire@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(1000);

		sipCallSender.listenForIncomingCall();

		sipCallSender.waitForIncomingCall(TIMEOUT);

		//Replace ContactHeader
		Address contactAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("Shootme <sip:27.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhoneSender.getParent().getHeaderFactory().createContactHeader(contactAddr);
		replacedHeaders.clear();
		replacedHeaders.add(contactHeader);

		Thread.sleep(1000);
		assertTrue(sipCallSender.sendIncomingCallResponse(Response.TRYING,"Trying", -1, null,replacedHeaders, null));	

		SipTransaction siptrans1 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans2 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans3 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans4 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans5 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans6 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans7 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans8 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans9 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);
		SipTransaction siptrans10 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);

		assertTrue(sipCallSender.waitReinviteResponse(siptrans1, TIMEOUT*2));
		if(sipCallSender.getLastReceivedResponse().getStatusCode() == 491)
			++numberOf491s;
		assertTrue(sipCallSender.waitReinviteResponse(siptrans1, TIMEOUT*2));
		if(sipCallSender.getLastReceivedResponse().getStatusCode() == 491)
			++numberOf491s;
		assertTrue(sipCallSender.waitReinviteResponse(siptrans1, TIMEOUT*2));
		if(sipCallSender.getLastReceivedResponse().getStatusCode() == 491)
			++numberOf491s;

		sipCallSender.sendIncomingCallResponse(Response.RINGING, "RINGING", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);
		sipCallSender.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT);

		logger.info("numberOf491s: "+String.valueOf(numberOf491s));

		int current = numberOf491s;
		assertTrue(numberOf491s > 2);

		siptrans1 = sipCallSender.sendReinvite("sip:127.0.0.1:5080;transport=udp", null, "no_body", null, null);

		sipCallSender.waitReinviteResponse(siptrans1, TIMEOUT);
		if(sipCallSender.getLastReceivedResponse().getStatusCode() == 491)
			++numberOf491s;

		logger.info("numberOf491s: "+String.valueOf(numberOf491s));
		assertEquals(current, numberOf491s);

		semaphore.release();
		Thread.sleep(6500);
	}

	@Test
	public void testShootmeSendBye() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:SSsendBye@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		sipCallSender.listenForDisconnect();

		assertTrue(sipCallSender.waitForDisconnect(TIMEOUT*2));
		assertTrue(sipCallSender.respondToDisconnect(200,"ΟΚ",null,replacedHeaders,null));

		Thread.sleep(TIMEOUT*4);
	}

	// Issue 1042 : Trying to simulate the 2 Invites arriving at the same time
	@Test
	public void testShootmeRetransTest() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);
		Request req = sipCallSender.getLastTransaction().getRequest();
		sender.getSipProvider().sendRequest(req);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(TIMEOUT);
		assertTrue(sipCallSender.disconnect());

		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());	
	}

	@Test
	public void testShootmeParameterable() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader1 = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		Header extraHeader2 = sender.getHeaderFactory().createHeader("additionalParameterableHeader", "none");
		Header extraHeader3 = sender.getHeaderFactory().createHeader("nonParameterableHeader", "none");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader1);
		additionalHeaders.add(extraHeader2);
		additionalHeaders.add(extraHeader3);

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(TIMEOUT);
		assertTrue(sipCallSender.disconnect());

		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());		
	}

	/**
	 * Non Regression Test for Issue http://code.google.com/p/mobicents/issues/detail?id=2201
	 * javax.servlet.sip.ServletParseException: Impossible to parse the following header Remote-Party-ID as an address.
	 */
	@Test
	public void testShootmeRemotePartyID() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader1 = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		Header extraHeader2 = sender.getHeaderFactory().createHeader("Remote-Party-ID", "\"KATE SMITH\"<sip:4162375543@47.135.223.88;user=phone>; party=calling; privacy=off; screen=yes");
		Header extraHeader3 = sender.getHeaderFactory().createHeader("Remote-Party-ID2", "sip:4162375543@47.135.223.88;user=phone; party=calling; privacy=off; screen=yes");
		Header extraHeader4 = sender.getHeaderFactory().createHeader("Remote-Party-ID3", "<sip:4162375543@47.135.223.88;user=phone>; party=calling; privacy=off; screen=yes");
		Header extraHeader5 = sender.getHeaderFactory().createHeader("Remote-Party-ID4", "\"KATE SMITH\"<sip:4162375543@47.135.223.88>; party=calling; privacy=off; screen=yes");
		Header extraHeader6 = sender.getHeaderFactory().createHeader("Remote-Party-ID5", "<sip:4162375543@47.135.223.88>; party=calling; privacy=off; screen=yes");		
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader1);
		additionalHeaders.add(extraHeader2);
		additionalHeaders.add(extraHeader3);
		additionalHeaders.add(extraHeader4);
		additionalHeaders.add(extraHeader5);
		additionalHeaders.add(extraHeader6);

		//Override default options for sender
		senderURI = "sip:RemotePartyId@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(TIMEOUT);
		assertTrue(sipCallSender.disconnect());

		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());		
	}

	@Test
	public void testShootmeRegister() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;

		/*
		 * SipUnit way
		 */
		SipURI requestURI = sender.getAddressFactory().createSipURI("sender","127.0.0.1:5070;transport=udp");
		assertTrue(sipPhoneSender.register(requestURI, "no_user", "no_password", "sip:sender@127.0.0.1:5080;transport=udp;lr", TIMEOUT, TIMEOUT));

		/*
		 * Alternative, create the request using jain sip
		 */

		//		//Add custom Header
		//		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		//		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		//		additionalHeaders.add(extraHeader);
		//		
		//		SipURI requestURI = sender.getAddressFactory().createSipURI("sender","127.0.0.1:5070;transport=udp");
		//		SipURI proxyUri = sender.getAddressFactory().createSipURI(null,"127.0.0.1:5070;lr;transport=udp");
		//		
		//		// build the INVITE Request message
		//        AddressFactory addr_factory = sipPhoneSender.getParent().getAddressFactory();
		//        HeaderFactory hdr_factory = sipPhoneSender.getParent().getHeaderFactory();
		//
		//        CallIdHeader callIdHeader = sipPhoneSender.getParent().getSipProvider().getNewCallId();
		//        CSeqHeader cSeqHeader = hdr_factory.createCSeqHeader(1, Request.REGISTER);
		//        FromHeader fromHeader = hdr_factory.createFromHeader(sipPhoneSender.getAddress(), sipPhoneSender.generateNewTag());
		//        
		//        Address contactAddr = sender.getAddressFactory().createAddress("sip:sender@127.0.0.1:5080;transport=udp;lr");
		//        ContactHeader contactHeader = sender.getHeaderFactory().createContactHeader(contactAddr);
		//        
		////        Contact: <sip:sender@127.0.0.1:5080;transport=udp;lr>
		//
		//        Address to_address = addr_factory.createAddress(addr_factory.createURI("sip:sender@sip-servlets.com"));
		//        ToHeader toHeader = hdr_factory.createToHeader(to_address, null);
		//
		//        MaxForwardsHeader maxForwards = hdr_factory.createMaxForwardsHeader(5);
		//        ArrayList<ViaHeader> viaHeaders = sipPhoneSender.getViaHeaders();
		//        
		//		Request request = sender.getMessageFactory().createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
		//				fromHeader, toHeader, viaHeaders, maxForwards);
		//		
		//		Address address = addr_factory.createAddress(proxyUri);
		//		RouteHeader routeHeader = hdr_factory.createRouteHeader(address);
		//		
		//		request.addHeader(extraHeader);
		//		request.addHeader(contactHeader);
		//		request.addHeader(routeHeader);
		//        
		//         // send the Request message
		//        SipTransaction trans = sipPhoneSender.sendRequestWithTransaction(request, false, null);
		//        assertNotNull(sipPhoneSender.format(), trans);
		//        
		//        Thread.sleep(TIMEOUT);
	}

	/**
	 * non regression test for Issue 1104 http://code.google.com/p/mobicents/issues/detail?id=1104
	 * Cannot find the corresponding sip session to this subsequent request
	 * 
	 * In conflict with Issue 1401 http://code.google.com/p/mobicents/issues/detail?id=1401
	 * ACK request sent by sip client after receiving 488/reINVITE is passed to sip application
	 * Forbidden by SIP Spec "11.2.2 Receiving ACK" :
	 * "Applications are not notified of incoming ACKs for non-2xx final responses to INVITE."
	 * 
	 */
	@Test
	public void testShootmeErrorResponse() throws Exception {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testErrorResponse@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		sipCallSender.waitOutgoingCallResponse(TIMEOUT);
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());
		sipCallSender.waitOutgoingCallResponse(TIMEOUT);
		assertEquals(Response.BUSY_HERE, sipCallSender.getLastReceivedResponse().getStatusCode());

		//Override default options for sender
		senderURI = "sip:receiver@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.listenForMessage();

		assertTrue(sipCallSender.waitForMessage(TIMEOUT));
		assertTrue(!sipCallSender.getAllReceivedMessagesContent().contains("ackReceived"));
		assertTrue(sipCallSender.sendMessageResponse(200, "OK", -1));

		assertTrue(sipCallSender.waitForMessage(DIALOG_TIMEOUT));
		List<String> allMessagesContent = sipCallSender.getAllReceivedMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}

	@Test
	public void testShootmeRegisterNoContact() throws Exception {
		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testRegisterNoContact@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		//We need to change the contact of the receiver
		//Override default options for sender
		receiverURI = "sip:you@sip-servlets.com";
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testRegisterNoContact@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		//Receiver check for Register requests
		sipPhoneReceiver.listenRequestMessage();

		RequestEvent requestEvent = sipPhoneReceiver.waitRequest(TIMEOUT);
		assertEquals(Request.REGISTER, requestEvent.getRequest().getMethod());
		sipPhoneReceiver.sendReply(requestEvent, 200, "OK", null, null, -1);
		assertNull(requestEvent.getRequest().getHeader(ContactHeader.NAME));

		Thread.sleep(TIMEOUT);
		assertTrue(sipCallSender.disconnect());

		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());
	}

	@Test
	public void testShootmeRegisterCSeqIncrease() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testRegisterCSeq@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		//We need to change the contact of the receiver
		//Override default options for sender
		receiverURI = "sip:you@sip-servlets.com";
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testRegisterCSeq@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		//Receiver check for Register requests
		sipPhoneReceiver.listenRequestMessage();

		//Start a new thread for receiver request processing
		new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					semaphore.acquire();

					RequestEvent receiverRequestEvent = null;
					int i = 0;

					while (i<4){
						receiverRequestEvent = sipPhoneReceiver.waitRequest(TIMEOUT);
						assertEquals(Request.REGISTER, receiverRequestEvent.getRequest().getMethod());
						sipPhoneReceiver.sendReply(receiverRequestEvent, 200, "OK", null, null, -1);
						Thread.sleep(15000);
						i++;
					}	
					cseqInt = ((CSeqHeader)receiverRequestEvent.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber();
					semaphore.release();
					logger.info("Semaphore released");
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}).start();

		Thread.sleep(TIMEOUT);
		assertTrue(sipCallSender.disconnect());

		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());

		semaphore.acquire();
		logger.info("Semaphone acquired");
		Thread.sleep(1000);
		assertTrue(cseqInt==4);
		semaphore.release();
	}

	@Test
	public void testShootmeRegisterCSeqIncreaseAuth() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testRegisterCSeq@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		//We need to change the contact of the receiver
		//Override default options for sender
		receiverURI = "sip:you@sip-servlets.com";
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testRegisterCSeq@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		//Receiver check for Register requests
		sipPhoneReceiver.listenRequestMessage();

		//Start a new thread for receiver request processing
		new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					semaphore.acquire();
					logger.info("Semaphone acquired");
					RequestEvent receiverRequestEvent = null;
					int i = 0;

					receiverRequestEvent = sipPhoneReceiver.waitRequest(TIMEOUT);
					assertEquals(Request.REGISTER, receiverRequestEvent.getRequest().getMethod());
					DigestServerAuthenticationMethod dsam = new DigestServerAuthenticationMethod();
					dsam.initialize();

					Response responseauth = receiver.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED,receiverRequestEvent.getRequest());

					ProxyAuthenticateHeader proxyAuthenticate = 
							receiver.getHeaderFactory().createProxyAuthenticateHeader(dsam.getScheme());
					proxyAuthenticate.setParameter("realm",dsam.getRealm(null));
					proxyAuthenticate.setParameter("nonce",dsam.generateNonce());
					proxyAuthenticate.setParameter("opaque","");

					proxyAuthenticate.setParameter("algorithm",dsam.getAlgorithm());
					responseauth.setHeader(proxyAuthenticate);
					ToHeader toHeader = (ToHeader) responseauth.getHeader(ToHeader.NAME);
					if (toHeader.getTag() == null) {
						toHeader.setTag(Integer.toString(new Random().nextInt(10000000)));
					}

					sipPhoneReceiver.sendReply(receiverRequestEvent, responseauth);


					while (i<3){
						receiverRequestEvent = sipPhoneReceiver.waitRequest(TIMEOUT);
						assertEquals(Request.REGISTER, receiverRequestEvent.getRequest().getMethod());
						sipPhoneReceiver.sendReply(receiverRequestEvent, 200, "OK", null, null, -1);
						Thread.sleep(15000);
						i++;
					}	
					cseqInt = ((CSeqHeader)receiverRequestEvent.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber();
					semaphore.release();
					logger.info("Semaphore released");
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}).start();

		Thread.sleep(TIMEOUT);
		assertTrue(sipCallSender.disconnect());

		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());

		semaphore.acquire();
		logger.info("Semaphone acquired");
		Thread.sleep(1000);
		assertTrue(cseqInt==4);
		semaphore.release();
		logger.info("Semaphore released");
	}

	@Test
	public void testShootmeRegisterAuthSavedSession() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testRegisterSavedSession@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		//We need to change the contact of the receiver
		//Override default options for sender
		receiverURI = "sip:you@sip-servlets.com";
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testRegisterSavedSession@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		//Receiver check for Register requests
		sipPhoneReceiver.listenRequestMessage();

		//Start a new thread for receiver request processing
		new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					semaphore.acquire();
					logger.info("Semaphore acquired");
					RequestEvent receiverRequestEvent = null;
					int i = 0;

					receiverRequestEvent = sipPhoneReceiver.waitRequest(TIMEOUT*2);
					assertEquals(Request.REGISTER, receiverRequestEvent.getRequest().getMethod());
					DigestServerAuthenticationMethod dsam = new DigestServerAuthenticationMethod();
					dsam.initialize();

					Response responseauth = receiver.getMessageFactory().createResponse(Response.PROXY_AUTHENTICATION_REQUIRED,receiverRequestEvent.getRequest());

					ProxyAuthenticateHeader proxyAuthenticate = 
							receiver.getHeaderFactory().createProxyAuthenticateHeader(dsam.getScheme());
					proxyAuthenticate.setParameter("realm",dsam.getRealm(null));
					proxyAuthenticate.setParameter("nonce",dsam.generateNonce());
					proxyAuthenticate.setParameter("opaque","");

					proxyAuthenticate.setParameter("algorithm",dsam.getAlgorithm());
					responseauth.setHeader(proxyAuthenticate);
					ToHeader toHeader = (ToHeader) responseauth.getHeader(ToHeader.NAME);
					if (toHeader.getTag() == null) {
						toHeader.setTag(Integer.toString(new Random().nextInt(10000000)));
					}

					sipPhoneReceiver.sendReply(receiverRequestEvent, responseauth);


					while (i<3){
						receiverRequestEvent = sipPhoneReceiver.waitRequest(TIMEOUT);
						assertEquals(Request.REGISTER, receiverRequestEvent.getRequest().getMethod());
						sipPhoneReceiver.sendReply(receiverRequestEvent, 200, "OK", null, null, -1);
						Thread.sleep(15000);
						i++;
					}	
					cseqInt = ((CSeqHeader)receiverRequestEvent.getRequest().getHeader(CSeqHeader.NAME)).getSeqNumber();
					semaphore.release();
					logger.info("Semaphore released");
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}).start();

		Thread.sleep(1000);
		semaphore.acquire();
		logger.info("Semaphore acquired");
		Thread.sleep(1000);
		assertTrue(cseqInt==4);
		semaphore.release();
		logger.info("Semaphore released");
	}

	@Test
	public void testShootmeCancel() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:cancel@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.RINGING, sipCallSender.getLastReceivedResponse().getStatusCode());

		SipTransaction sipTrans1 = sipCallSender.sendCancel();
		assertTrue(sipCallSender.waitForCancelResponse(sipTrans1, TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.REQUEST_TERMINATED, sipCallSender.getLastReceivedResponse().getStatusCode());

		//Override default options for sender
		senderURI = "sip:receiver@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.listenForMessage();

		assertTrue(sipCallSender.waitForMessage(TIMEOUT));
		assertTrue(sipCallSender.sendMessageResponse(200, "OK", -1));
		List<String> allMessagesContent = sipCallSender.getAllReceivedMessagesContent();
		assertTrue(allMessagesContent.contains("cancelReceived"));		
	}

	@Test
	public void testShootmeMultipleValueHeaders() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:TestAllowHeader@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:TestAllowHeader@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		SipResponse finalResponse = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.METHOD_NOT_ALLOWED, finalResponse.getStatusCode());

		//Issue 1164 non regression test
		ListIterator<AllowHeader> allowHeaders = (ListIterator<AllowHeader>) finalResponse.getMessage().getHeaders(AllowHeader.NAME);
		assertNotNull(allowHeaders);

		List<String> allowHeadersList = new ArrayList<String>();
		while (allowHeaders.hasNext()) {
			allowHeadersList.add(allowHeaders.next().getMethod());
		}
		assertTrue(Arrays.equals(ALLOW_HEADERS, (String[])allowHeadersList.toArray(new String[allowHeadersList.size()])));
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=676
	@Test
	public void testShootmeToTag() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:TestToTag@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:TestAllowHeader@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=695
	@Test
	public void testSubscriberURI() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testSubscriberUri@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testSubscriberUri@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=1010
	@Test
	public void testFlagParameter() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testFlagParameter@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testFlagParameter@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=1021
	@Test
	public void testSessionRetrieval() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testSessionRetrieval@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:testSessionRetrieval@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());		
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=1061
	// Also test http://code.google.com/p/mobicents/issues/detail?id=1681
	//TODO: SipUnit count retransimitions
	@Test
	public void testNoAckReceived() throws Exception {

		int nbRetrans = 0;

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:noAckReceived@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:noAckReceived@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));

		//Override default options for sender
		senderURI = "sip:receiver@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.listenForMessage();

		// test http://code.google.com/p/mobicents/issues/detail?id=1681
		// Make sure we get the 10 retrans for 200 to INVITE when no ACK is sent
		// corresponding to Timer G				
		assertTrue(sipCallSender.waitForMessage(DIALOG_TIMEOUT+TIMEOUT));
		assertTrue(sipCallSender.sendMessageResponse(200, "OK", -1));
		List<String> allMessagesContent = sipCallSender.getAllReceivedMessagesContent();		

		assertEquals(1,allMessagesContent.size());
		assertEquals("noAckReceived", allMessagesContent.get(0));

		nbRetrans = sender.getRetransmissions();
		logger.info("nbRetrans: "+nbRetrans);
		assertTrue(nbRetrans >= 9);
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=2427
	@Test
	public void testShootmeSerializationDeserialization() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:serialization@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());		
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=2361
	@Test
	public void testShootmeSystemHeaderModification() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:systemHeaderModification@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());		
	}

	// test for http://code.google.com/p/mobicents/issues/detail?id=2578
	@Test
	public void testShootmeAuthenticationInfoHeader() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;	

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		SipURI requestURI = sender.getAddressFactory().createSipURI("receiver","127.0.0.1:5070;transport=udp");
		SipURI proxyUri = sender.getAddressFactory().createSipURI(null,"127.0.0.1:5070;lr;transport=udp");

		// build the INVITE Request message
		AddressFactory addr_factory = sipPhoneSender.getParent().getAddressFactory();
		HeaderFactory hdr_factory = sipPhoneSender.getParent().getHeaderFactory();

		CallIdHeader callIdHeader = sipPhoneSender.getParent().getSipProvider().getNewCallId();
		CSeqHeader cSeqHeader = hdr_factory.createCSeqHeader(1, Request.REGISTER);
		Address from_address = addr_factory.createAddress("sip:authenticationInfoHeader@sip-servlets.com");
		FromHeader fromHeader = hdr_factory.createFromHeader(from_address, sipPhoneSender.generateNewTag());

		Address contactAddr = addr_factory.createAddress("sip:authenticationInfoHeader@127.0.0.1:5080;transport=udp;lr");
		ContactHeader contactHeader = hdr_factory.createContactHeader(contactAddr);

		Address to_address = addr_factory.createAddress(addr_factory.createURI("sip:receiver@sip-servlets.com"));
		ToHeader toHeader = hdr_factory.createToHeader(to_address, null);

		MaxForwardsHeader maxForwards = hdr_factory.createMaxForwardsHeader(5);
		ArrayList<ViaHeader> viaHeaders = sipPhoneSender.getViaHeaders();

		Request request = sender.getMessageFactory().createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
				fromHeader, toHeader, viaHeaders, maxForwards);

		Address address = addr_factory.createAddress(proxyUri);
		RouteHeader routeHeader = hdr_factory.createRouteHeader(address);

		request.addHeader(extraHeader);
		request.addHeader(contactHeader);
		request.addHeader(routeHeader);

		// send the Request message
		SipTransaction trans = sipPhoneSender.sendRequestWithTransaction(request, false, null);
		assertNotNull(sipPhoneSender.format(), trans);

		javax.sip.ResponseEvent respEvent = (ResponseEvent) sipPhoneSender.waitResponse(trans, TIMEOUT);
		assertEquals(Response.UNAUTHORIZED, respEvent.getResponse().getStatusCode());



		AuthenticationInfoHeader authenticationInfoHeader = (AuthenticationInfoHeader) respEvent.getResponse().getHeader(AuthenticationInfoHeader.NAME);
		assertEquals("Authentication-Info: NTLM rspauth=\"01000000000000005CD422F0C750C7C6\",srand=\"0B9D33A2\",snum=\"1\",opaque=\"BCDC0C9D\",qop=\"auth\",targetname=\"server.contoso.com\",realm=\"SIP Communications Service\"",
				authenticationInfoHeader.toString().trim());

	}

	@Test
	public void testShootmeServerHeader() throws Exception {


		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-127.0.0.1-5070");
		sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
				"off");
		sipStackProperties.setProperty(
				"gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE",
				"64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",
				"true");
		sipStackProperties.setProperty("org.mobicents.servlet.sip.SERVER_HEADER",
				"MobicentsSipServletsServer");

		logger.info("Restart the container and pass new SipStack properties");
		containerManager.restartContainer(sipStackProperties);

		Thread.sleep(TIMEOUT);

		//Get the list of SipConnectors
		List<SipConnector> sipConnectors = containerManager.getSipConnectors();

		//Remove the existing SipConnectors from the container
		logger.info("Stop the SipConnectors");
		for (SipConnector sipConnector : sipConnectors) {
			containerManager.removeSipConnector(sipConnector);
		}

		Thread.sleep(TIMEOUT);

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		logger.info("Add a new SipConnector");
		containerManager.addSipConnector("127.0.0.1", 5070, ListeningPoint.UDP);

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		SipResponse resp = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, resp.getStatusCode());

		ServerHeader serverHeader = (ServerHeader) resp.getMessage().getHeader(ServerHeader.NAME);
		assertNotNull(serverHeader);
	}

}
