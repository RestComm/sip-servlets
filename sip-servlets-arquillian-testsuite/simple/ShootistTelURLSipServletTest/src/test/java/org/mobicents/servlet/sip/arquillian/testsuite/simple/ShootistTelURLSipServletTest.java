/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.testsuite.simple;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sip.address.Address;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mobicents.api.annotations.ContextParam;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
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
public class ShootistTelURLSipServletTest 
{
	@ArquillianResource
	private Deployer deployer;

	private SipStack receiver;

	private SipCall sipCall;
	private SipPhone sipPhone;

	private final int TIMEOUT = 10000;	

	private final Logger logger = Logger.getLogger(ShootistTelURLSipServletTest.class.getName());

	private static SipStackTool sipStackTool;
	private String testArchive = "simple";

	@BeforeClass
	public static void beforeClass(){
		sipStackTool = new SipStackTool("ShootistTelURLSipServletTest");
	}

	@Before
	public void setUp() throws Exception
	{
		//Create the sipCall and start listening for messages
		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
	}

	@After
	public void tearDown() throws Exception
	{
		logger.info("About to un-deploy the application");
		deployer.undeploy(testArchive);
		if(sipCall != null)	sipCall.disposeNoBye();
		if(sipPhone != null) sipPhone.dispose();
		if(receiver != null) receiver.dispose();
	}

	@Deployment(name="simple", managed=false, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootistsipservlet.war");
		webArchive.addClasses(ShootistSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}

	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||



	@Test @ContextParam(name = "urlType", value = "tel")
	public void testShootistTelURL() throws Exception {

		Address contactAddr = receiver.getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = receiver.getHeaderFactory().createContactHeader(contactAddr);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(contactHeader);

		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", -1));
		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", -1, null, replacedHeaders, null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",-1, null, replacedHeaders, null));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", -1, null, replacedHeaders, null));

		assertTrue(sipCall.waitForDisconnect(TIMEOUT));
		assertTrue(sipCall.respondToDisconnect());	
	}

	@Test @ContextParam(name = "urlType", value = "telAsSip")
	public void testShootistTelURLAsSIP() throws Exception {

		Address contactAddr = receiver.getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = receiver.getHeaderFactory().createContactHeader(contactAddr);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(contactHeader);

		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", -1));
		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", -1, null, replacedHeaders, null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",-1, null, replacedHeaders, null));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", -1, null, replacedHeaders, null));

		assertTrue(sipCall.waitForDisconnect(TIMEOUT));
		assertTrue(sipCall.respondToDisconnect());				
	}
}
