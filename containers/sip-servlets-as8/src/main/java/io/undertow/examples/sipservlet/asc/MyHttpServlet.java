package io.undertow.examples.sipservlet.asc;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;

public class MyHttpServlet extends HttpServlet{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 5928696560573860647L;
	
	private static final String ASCHOST = "sip:127.0.0.1:15080";
	private static final String ASHOST = "sip:127.0.0.1:5080";
	
	@Resource
	private SipFactory sipFactory;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
	      // Set response content type
	      response.setContentType("text/html");

	      
    	  StringBuffer sb = new StringBuffer();
    	  sb.append(""
    	  + "<html>"
    	  + "<head>"
    	  + "<title>Simple Mobicents Sip Servlet Test Application</title>"
    	  + "</head>"
    	  + "<body bgcolor=white>"
    	  + "<p><font size='6'>Simple Mobicents Sip Servlet Test Application</font></p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=1'>Initiate OPTIONS</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=2'>Initiate SUBSCRIBE - NOTIFY</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=3'>Initiate INVITE - SLESS</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=4'>Initiate INVITE - SFULL</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=5'>Initiate INVITE - CANCEL</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=6'>Initiate Concurrency Test</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=7'>Initiate Concurrency Test in MDB</a>"
    	  + "<p>&nbsp;</p>"
    	  + "<a href='/my-sipapp-asc/MyHttpServlet?test=8'>Initiate T1 Timer Test</a>"
    	  + "<p>&nbsp;</p>");

    	  String closeHtml="</body></html>";
    	  String testName="";
    	  Integer testNum=null;
    	  try{
    		  testNum = Integer.parseInt(request.getParameter("test"));
    	  }catch(NumberFormatException nfe){
    		  testNum = null;
    	  }
	      
    	  if(sipFactory!=null && testNum!=null){
	    	  switch(testNum)
	    	  {
	    	  case 1:	
	    		  initiateOptions();
	    		  testName="OPTIONS";
	    		  break;
	    	  case 2:
		    	  initiateSubscribe();
		    	  testName="SUBSCRIBE - NOTIFY";
		    	  break;
	    	  case 3: 
	    		  initiateInvite();
	    		  testName="INVITE - SLESS";
	    		  break;
	    	  case 4:
	    		  initiateInviteSfull();
	    		  testName="INVITE - SFULL";
	    		  break;
	    	  case 5:
	    		  initiateInviteCancel();
	    		  testName="INVITE - CANCEL";
	    		  break;
	    	  case 6:
	    		  initiateConcurrencyTest();
	    		  testName="Concurrency";
	    		  break;
	    	  case 7: 
	    		  initiateConcurrencyTestMDB();
	    		  testName="Concurrency MDB";
	    		  break;
	    	  case 8: 
	        	  initiateRetransmitTimerTest();
	    		  testName="T1 Timer";
	    		  break;
	    	  default:
		    	  sb.append("Test NOT initiated, 'test' req parameter was null or invalid!");
		    	  sb.append(closeHtml);

		    	  PrintWriter out = response.getWriter();
			      out.println(sb.toString());
			      return;
	    	  }	    		  
	    	  
	    	  sb.append("Test "+testName+" initiated using SipFactory:"+sipFactory+". Check server logs for the result.");
	    	  sb.append(closeHtml);
	    	  PrintWriter out = response.getWriter();
		      out.println(sb.toString());

	      }else{
	    	  sb.append("Test NOT initiated, SipFactory or 'test' req parameter was null or invalid!");
	    	  sb.append(closeHtml);

	    	  PrintWriter out = response.getWriter();
		      out.println(sb.toString());
	    	  
	      }
	      
	      
	  }
	  //ASC: 15080 -- initiator
	  //AS: 5080
	  private void initiateOptions() throws ServletException, IOException{
    	  SipApplicationSession session = sipFactory.createApplicationSession();

		  SipServletRequest req = sipFactory.createRequest(session, "OPTIONS", ASCHOST, ASHOST);
    	  
    	  req.send();
	  }

	  //ASC: 5080
	  //AS: 15080 -- initiator
	  private void initiateSubscribe() throws ServletException, IOException{
    	  SipApplicationSession session = sipFactory.createApplicationSession();

		  SipServletRequest req = sipFactory.createRequest(session, "SUBSCRIBE", "sip:user@as.com", "sip:+36305251004@HLRDFNR");
		  req.pushRoute(sipFactory.createAddress(ASHOST));
		  req.addHeader("Event", "presence");
		  req.addHeader("Accept", "application/map-phase3+ber");
		  req.addHeader("Expires", "0");
		  req.send();
	  }

	  //ASC: 15080 -- initiator
	  //AS: 5080	  
	  private void initiateInvite() throws ServletException, IOException{
		  SipApplicationSession session = sipFactory.createApplicationSession();

		  SipServletRequest req = sipFactory.createRequest(session, "INVITE", "sip:anonymous@anonimous.invalid", "tel:06305251004;noa=unknown;npi=1;phone-context=local");
		  req.addHeader("Supported", "100rel");
		  
		  
		  req.pushRoute(sipFactory.createAddress(ASHOST));

		  Object content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		  		+ "<Cap4>"
		  		+ "  <initialDP>"
		  		+ "    <serviceKey>28049</serviceKey>"
		  		+ "    <calledPartyNumber>82906003 25150004</calledPartyNumber>"
		  		+ "    <callingPartyNumber>83170372 731302</callingPartyNumber>"
		  		+ "    <callingPartysCategory>0A</callingPartysCategory>"
		  		+ "    <locationNumber>84936303 00000080 2501</locationNumber>"
		  		+ "    <bearerCapability>"
		  		+ "      <bearerCap>8090A3</bearerCap>"
		  		+ "    </bearerCapability>"
		  		+ "    <eventTypeBCSM><collectedInfo/></eventTypeBCSM>"
		  		+ "    <iMSI>12361010 956501F1</iMSI>"
		  		+ "    <locationInformation>"
		  		+ "      <ageOfLocationInformation>0</ageOfLocationInformation>"
		  		+ "      <vlr-number>91630349 9889F0</vlr-number>"
		  		+ "      <cellGlobalIdOrServiceAreaIdOrLAI>"
		  		+ "        <cellGlobalIdOrServiceAreaIdFixedLength>12F60300 050004</cellGlobalIdOrServiceAreaIdFixedLength>"
		  		+ "      </cellGlobalIdOrServiceAreaIdOrLAI>"
		  		+ "    </locationInformation>"
		  		+ "    <ext-basicServiceCode>"
		  		+ "      <ext-Teleservice>11</ext-Teleservice>"
		  		+ "    </ext-basicServiceCode>"
		  		+ "    <callReferenceNumber>00713000 78</callReferenceNumber>"
		  		+ "    <mscAddress>91630349 9889F0</mscAddress>"
		  		+ "    <calledPartyBCDNumber>81600325 1500F4</calledPartyBCDNumber>"
		  		+ "    <timeAndTimezone>02417071 41503000</timeAndTimezone>"
		  		+ "    <initialDPArgExtension>"
		  		+ "      <ms-Classmark2>5319A1</ms-Classmark2>"
		  		+ "      <iMEI>53651600 52141481</iMEI>"
		  		+ "      <supportedCamelPhases>1111</supportedCamelPhases>"
		  		+ "      <offeredCamel4Functionalities>11111101 11110011 1</offeredCamel4Functionalities>"
		  		+ "    </initialDPArgExtension>"
		  		+ "  </initialDP>"
		  		+ "</Cap4>";
		  
		  
		  req.setContent(content, "application/cap-phase4+xml");
		  
    	  req.send();
		  
	}

	//ASC: 15080 -- initiator
	//AS: 5080	  
	private void initiateInviteSfull() throws ServletException, IOException{
		SipApplicationSession session = sipFactory.createApplicationSession();

		//SipFactory -> Address/URI l�trehoz�s ( �name� <uri>  ||  <uri>  , from SipURI )	
		//SipServletRequest req = sipFactory.createRequest(session, "INVITE", sipFactory.createAddress("Anonymous <sip:anonymous@anonimous.invalid>"), sipFactory.createAddress("tel:06304443803;noa=unknown;npi=1;phone-context=local") );
		SipServletRequest req = sipFactory.createRequest(session, "INVITE", sipFactory.createSipURI("anonymous", "anonymous.invalid"), sipFactory.createURI("tel:06304443803") );

		req.addHeader("Supported", "100rel");
		  
		req.addHeader("SfullTest", "true");
		  
		req.pushRoute(sipFactory.createAddress(ASHOST));
		//req.pushRoute(sipFactory.createAddress(ASCHOST));

		//o	Address.toString()		  o	Address.getURI().setParameter(�a�,�b�);		  o	// OCCAS + l�p�s: Address.setURI(uri)		  o	Address.toString() l�tszik-e ;a=b ?
		Address to = req.getTo();
		to.getURI().setParameter("a", "b");
		System.out.println("to address toString():"+to.toString());
		  
		  
		String CRLF = "\r\n";
		String boundary = "--frontier\r\n";
		  
		String content = CRLF + boundary
				+ "Content-Type: application/cap-phase4+xml"+CRLF
				+ CRLF
				+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		  		+ "<Cap4>\n"
		  		+ "  <initialDP>\n"
		  		+ "    <serviceKey>28049</serviceKey>\n"
		  		+ "    <calledPartyNumber>82906003 25150004</calledPartyNumber>\n"
		  		+ "    <callingPartyNumber>83170372 731302</callingPartyNumber>\n"
		  		+ "    <callingPartysCategory>0A</callingPartysCategory>\n"
		  		+ "    <locationNumber>84936303 00000080 2501</locationNumber>\n"
		  		+ "    <bearerCapability>\n"
		  		+ "      <bearerCap>8090A3</bearerCap>\n"
		  		+ "    </bearerCapability>\n"
		  		+ "    <eventTypeBCSM><collectedInfo/></eventTypeBCSM>\n"
		  		+ "    <iMSI>12361010 956501F1</iMSI>\n"
		  		+ "    <locationInformation>\n"
		  		+ "      <ageOfLocationInformation>0</ageOfLocationInformation>\n"
		  		+ "      <vlr-number>91630349 9889F0</vlr-number>\n"
		  		+ "      <cellGlobalIdOrServiceAreaIdOrLAI>\n"
		  		+ "        <cellGlobalIdOrServiceAreaIdFixedLength>12F60300 050004</cellGlobalIdOrServiceAreaIdFixedLength>\n"
		  		+ "      </cellGlobalIdOrServiceAreaIdOrLAI>\n"
		  		+ "    </locationInformation>\n"
		  		+ "    <ext-basicServiceCode>\n"
		  		+ "      <ext-Teleservice>11</ext-Teleservice>\n"
		  		+ "    </ext-basicServiceCode>\n"
		  		+ "    <callReferenceNumber>00713000 78</callReferenceNumber>\n"
		  		+ "    <mscAddress>91630349 9889F0</mscAddress>\n"
		  		+ "    <calledPartyBCDNumber>81600325 1500F4</calledPartyBCDNumber>\n"
		  		+ "    <timeAndTimezone>02417071 41503000</timeAndTimezone>\n"
		  		+ "    <initialDPArgExtension>\n"
		  		+ "      <ms-Classmark2>5319A1</ms-Classmark2>\n"
		  		+ "      <iMEI>53651600 52141481</iMEI>\n"
		  		+ "      <supportedCamelPhases>1111</supportedCamelPhases>\n"
		  		+ "      <offeredCamel4Functionalities>11111101 11110011 1</offeredCamel4Functionalities>\n"
		  		+ "    </initialDPArgExtension>\n"
		  		+ "  </initialDP>\n"
		  		+ "</Cap4>"+CRLF
		  		+ CRLF
		  		+ boundary
		  		+ "Content-Type: application/sdp\r\n"
		  		+ CRLF
		  		+ "v=0"+CRLF
		  		+ "o=OCSB"+CRLF
		  		+ "s=ocsb 1 1 IN IP4 0.0.0.0"+CRLF
		  		+ "i=L1"+CRLF
		  		+ CRLF
		  		+"--frontier--"+CRLF;
		  
		byte[] begin = content.getBytes();
		  
		//calltester SRTestContentType alapj�n param�terkezel�se tesztel�s:
		String contentType = "multipart/mixed \t \r\n\t \t  ; other=-.!%*_+`'~A ;boundary=\"frontier\";boundary2=\"1234;\\\"1234\";third=\"quoted string\"";

		req.setContent(begin, contentType);
		req.send();
	}

	//ASC: 15080 -- initiator
	//AS: 5080	  
	private void initiateInviteCancel() throws ServletException, IOException{
		SipApplicationSession session = sipFactory.createApplicationSession();

		SipServletRequest req = sipFactory.createRequest(session, "INVITE", "sip:anonymous@anonimous.invalid", "tel:06305251004;noa=unknown;npi=1;phone-context=local");
		req.addHeader("Supported", "100rel");
		  
		req.addHeader("CancelTest", "true");

		req.pushRoute(sipFactory.createAddress(ASHOST));

		Object content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<Cap4>"
		  		+ "  <initialDP>"
		  		+ "    <serviceKey>28049</serviceKey>"
		  		+ "    <calledPartyNumber>82906003 25150004</calledPartyNumber>"
		  		+ "    <callingPartyNumber>83170372 731302</callingPartyNumber>"
		  		+ "    <callingPartysCategory>0A</callingPartysCategory>"
		  		+ "    <locationNumber>84936303 00000080 2501</locationNumber>"
		  		+ "    <bearerCapability>"
		  		+ "      <bearerCap>8090A3</bearerCap>"
		  		+ "    </bearerCapability>"
		  		+ "    <eventTypeBCSM><collectedInfo/></eventTypeBCSM>"
		  		+ "    <iMSI>12361010 956501F1</iMSI>"
		  		+ "    <locationInformation>"
		  		+ "      <ageOfLocationInformation>0</ageOfLocationInformation>"
		  		+ "      <vlr-number>91630349 9889F0</vlr-number>"
		  		+ "      <cellGlobalIdOrServiceAreaIdOrLAI>"
		  		+ "        <cellGlobalIdOrServiceAreaIdFixedLength>12F60300 050004</cellGlobalIdOrServiceAreaIdFixedLength>"
		  		+ "      </cellGlobalIdOrServiceAreaIdOrLAI>"
		  		+ "    </locationInformation>"
		  		+ "    <ext-basicServiceCode>"
		  		+ "      <ext-Teleservice>11</ext-Teleservice>"
		  		+ "    </ext-basicServiceCode>"
		  		+ "    <callReferenceNumber>00713000 78</callReferenceNumber>"
		  		+ "    <mscAddress>91630349 9889F0</mscAddress>"
		  		+ "    <calledPartyBCDNumber>81600325 1500F4</calledPartyBCDNumber>"
		  		+ "    <timeAndTimezone>02417071 41503000</timeAndTimezone>"
		  		+ "    <initialDPArgExtension>"
		  		+ "      <ms-Classmark2>5319A1</ms-Classmark2>"
		  		+ "      <iMEI>53651600 52141481</iMEI>"
		  		+ "      <supportedCamelPhases>1111</supportedCamelPhases>"
		  		+ "      <offeredCamel4Functionalities>11111101 11110011 1</offeredCamel4Functionalities>"
		  		+ "    </initialDPArgExtension>"
		  		+ "  </initialDP>"
		  		+ "</Cap4>";
		  
		  
		req.setContent(content, "application/cap-phase4+xml");
		  
    	req.send();
	}

	//ASC: 15080 -- initiator
	//AS: 5080
	private void initiateConcurrencyTest() throws ServletException, IOException{
    	SipApplicationSession session = sipFactory.createApplicationSession();

		SipServletRequest req1 = sipFactory.createRequest(session, "INVITE", ASCHOST, ASHOST);
		req1.addHeader("ConcurrencyTest", "true");
    	req1.send();

		SipServletRequest req2 = sipFactory.createRequest(session, "INVITE", ASCHOST, ASHOST);
    	req2.addHeader("ConcurrencyTest", "true");
    	req2.send();
	}

	//ASC: 15080 -- initiator
	//AS: 5080
	private void initiateConcurrencyTestMDB() throws ServletException, IOException{
		SipApplicationSession session = sipFactory.createApplicationSession();

		SipServletRequest req1 = sipFactory.createRequest(session, "INVITE", ASCHOST, ASHOST);
		req1.addHeader("ConcurrencyTestMDB", "true");
		req1.send();

		SipServletRequest req2 = sipFactory.createRequest(session, "INVITE", ASCHOST, ASHOST);
		req2.addHeader("ConcurrencyTestMDB", "true");
		req2.send();
	}  

	  
	//ASC: 15080 -- initiator
	//AS: 5080
	private void initiateRetransmitTimerTest() throws ServletException, IOException{
		SipApplicationSession session = sipFactory.createApplicationSession();
		
		//"rossz" c�mre k�ldj�k, hogy retransmit legyen az elmaradt valasz miatt
		SipServletRequest req1 = sipFactory.createRequest(session, "INVITE", ASCHOST, "sip:127.0.0.1:50800");
		req1.addHeader("RetransmitTimerTest", "true");
		req1.send();
	}  

	  
	  public void destroy()
	  {
	      // do nothing.
	  }
	
}
