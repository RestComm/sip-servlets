package org.mobicents.servlet.sip.arquillian.testsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

@RunWith(Arquillian.class)
public class SimpleProxyTest {

    private static Logger logger = Logger.getLogger(SimpleProxyTest.class);

    //SipUnit related components
    private SipStack bobStack;
    private SipCall bobCall;
    private SipPhone bobPhone;
    private static SipStackTool sipStackTool1;

    private SipStack aliceStack;
    private SipCall aliceCall;
    private SipPhone alicePhone;
    private static SipStackTool sipStackTool2;

    private static String toUri = "sip:alice@127.0.0.1:5092";

    @BeforeClass
    public static void beforeClass(){
        sipStackTool1 = new SipStackTool("mySipUnitStackTool1");
        sipStackTool2 = new SipStackTool("mySipUnitStackTool2");
    }

    @Before
    public void setUp() throws Exception
    {	
        logger.info("Setting up SipUnit");
        //Create the sipCall and start listening for messages

        //.SipStackTool.initializeSipStack(String myTransport, String myHost, String myPort, String outboundProxy)
        bobStack = sipStackTool1.initializeSipStack(SipStack.PROTOCOL_TCP, "127.0.0.1", "5091", "127.0.0.1:5070");
        //SipStack.createSipPhone(String proxyHost, String proxyProto, int proxyPort, String myURI)
        bobPhone = bobStack.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, 5070, "sip:bob@127.0.0.1:5091");
        bobCall = bobPhone.createSipCall();
        bobCall.listenForIncomingCall();

        //.SipStackTool.initializeSipStack(String myTransport, String myHost, String myPort, String outboundProxy)
        aliceStack = sipStackTool2.initializeSipStack(SipStack.PROTOCOL_TCP, "127.0.0.1", "5092", "127.0.0.1:5070");
        //SipStack.createSipPhone(String proxyHost, String proxyProto, int proxyPort, String myURI)
        alicePhone = aliceStack.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, 5070, "sip:alice@127.0.0.1:5092");
        aliceCall = alicePhone.createSipCall();
        aliceCall.listenForIncomingCall();
    }

    @After
    public void tearDown() throws Exception
    {
        logger.info("Tear down SipUnit");
        if(bobCall != null)	bobCall.disposeNoBye();
        if(bobPhone != null) bobPhone.dispose();
        if(bobStack != null) bobStack.dispose();

        if(aliceCall != null) aliceCall.disposeNoBye();
        if(alicePhone != null) alicePhone.dispose();
        if(aliceStack != null) aliceStack.dispose();
    }

    /*
     * Define the test archive here.
     * Pay attention to the properties of the Deployment annotation
     * --name: the arquillian Deployer, you can deploy/undeploy this archive by using the name here
     * --managed: if this is FALSE then the framework WILL NOT manage the lifecycle of this archive, the developer is responsible to deploy/undeploy
     * --testable: as-client mode (https://docs.jboss.org/author/display/ARQ/Test+run+modes) 
     */
    @Deployment(name="simple", managed=true, testable=false)
    public static WebArchive createTestArchive()
    {
        //Create a test archive named: simplesipservlet.war
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "simpleproxy.war");
        //Include the SimpleSipServlet.class from /src/main/java
        webArchive.addClasses(ProxySipServlet.class);
        //Include as WEB-INF resource sip.xml the in-container-sip.xml from src/test/resources
        webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

        return webArchive;
    }

    /*
     *****************************
     *Tests 
     *****************************
     **/

    @Test
    public void testInitiateCall() throws InterruptedException {

        //Initiate a new call
        assertTrue(bobCall.initiateOutgoingCall(toUri, null));

        assertTrue(aliceCall.waitForIncomingCall(5000));
        assertTrue(aliceCall.sendIncomingCallResponse(180, "Alice-Ringing", 600));
        assertTrue(aliceCall.sendIncomingCallResponse(200, "Alice-OK", 600));
        
        //Wait for answer
        assertTrue(bobCall.waitForAnswer(5000));
        assertEquals(SipServletResponse.SC_OK, bobCall.getLastReceivedResponse().getStatusCode());

        //Send ACK to 200 OK
        assertTrue(bobCall.sendInviteOkAck());
//        bobCall.listenForDisconnect();
        
        Thread.sleep(3000);
        
        logger.info("---### Will now disconnect Bob");
        
        bobCall.disposeNoBye();
        bobPhone.dispose();
        bobStack.dispose();
        bobCall = null;
        bobPhone = null;
        bobStack = null;
        
        Thread.sleep(1000);
        
        logger.info("---### Will now send BYE from Alice");
        
        aliceCall.disconnect();
        
//        assertTrue(bobCall.waitForDisconnect(5000));
//        assertTrue(bobCall.respondToDisconnect());
        Thread.sleep(4000);
        assertEquals(SipServletResponse.SC_REQUEST_TIMEOUT,aliceCall.getLastReceivedResponse().getStatusCode());
        
//        //Disconnect call and wait for 200 ok to BYE
//        assertTrue(bobCall.disconnect());
//        assertEquals(SipServletResponse.SC_OK,bobCall.getLastReceivedResponse().getStatusCode());


    }

}
