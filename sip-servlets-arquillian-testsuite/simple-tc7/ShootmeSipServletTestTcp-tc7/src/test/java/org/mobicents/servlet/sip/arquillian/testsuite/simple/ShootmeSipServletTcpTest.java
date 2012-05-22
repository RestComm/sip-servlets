/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.testsuite.simple;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTransaction;
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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */
@RunWith(Arquillian.class)
public class ShootmeSipServletTcpTest 
{
	private final Logger logger = Logger.getLogger(ShootmeSipServletTcpTest.class.getName());

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
		receiverSipStackTool = new SipStackTool("ShootmeSipServletTcpTest_receiver");
		senderSipStackTool = new SipStackTool("ShootmeSipServletTcpTest_sender");
	}

	@Before
	public void setUp() throws Exception
	{
		//Create the sipCall and start listening for messages
		receiver = receiverSipStackTool.initializeSipStack(SipStack.PROTOCOL_TCP, "127.0.0.1", receiverPort, "127.0.0.1:5070");
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();
		sipCallReceiver.listenForIncomingCall();

		sender = senderSipStackTool.initializeSipStack(SipStack.PROTOCOL_TCP, "127.0.0.1", senderPort, "127.0.0.1:5070");
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, proxyPort, senderURI);
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

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=tcp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());		
	}
	
	@Test
	public void testShootmeRegister() throws Exception {
		
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;
		
		SipURI requestURI = sender.getAddressFactory().createSipURI("sender","127.0.0.1:5070;transport=tcp");
		sipPhoneSender.register(requestURI, "no_user", "no_password", "sip:sender@127.0.0.1:5080;transport=tcp;lr", TIMEOUT, TIMEOUT);
		assertEquals(Response.SERVER_INTERNAL_ERROR ,sipPhoneSender.getReturnCode()); 
	}
	
	@Test
	public void testShootmeCancel() throws Exception {
	
		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:cancel@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		isDeployed = true;		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=tcp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.RINGING, sipCallSender.getLastReceivedResponse().getStatusCode());

		SipTransaction sipTrans1 = sipCallSender.sendCancel();
		assertNotNull(sipTrans1);
		
		assertTrue(sipCallSender.waitForCancelResponse(sipTrans1, TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.REQUEST_TERMINATED, sipCallSender.getLastReceivedResponse().getStatusCode());
		
		//Override default options for sender
		senderURI = "sip:receiver@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.listenForMessage();

		assertTrue(sipCallSender.waitForMessage(TIMEOUT));
		assertTrue(sipCallSender.sendMessageResponse(200, "OK", -1));
		List<String> allMessagesContent = sipCallSender.getAllReceivedMessagesContent();
		assertTrue(allMessagesContent.contains("cancelReceived"));	
		
	}
}
