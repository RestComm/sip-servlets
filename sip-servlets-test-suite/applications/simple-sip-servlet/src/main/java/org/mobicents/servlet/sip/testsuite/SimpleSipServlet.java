/*
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
package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.SipSession.State;
import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.listener.SipConnectorListener;

public class SimpleSipServlet extends SipServlet implements SipErrorListener, TimerListener, SipConnectorListener {
	private static transient Logger logger = Logger.getLogger(SimpleSipServlet.class);
	private static final long serialVersionUID = 1L;
	private static final String TEST_PRACK = "prack";
	private static final String TEST_ERROR_RESPONSE = "testErrorResponse";
	private static final String TEST_REGISTER_C_SEQ = "testRegisterCSeq";
	private static final String TEST_REGISTER_NO_CONTACT = "testRegisterNoContact";
	private static final String TEST_REGISTER_SAVED_SESSION = "testRegisterSavedSession";
	private static final String TEST_SUBSCRIBER_URI = "testSubscriberUri";
	private static final String TEST_FLAG_PARAM = "testFlagParameter";
	private static final String TEST_EXTERNAL_ROUTING = "testExternalRouting";
	private static final String TEST_EXTERNAL_ROUTING_NO_INFO = "testExternalRoutingNoInfo";
	private static final String TEST_NON_EXISTING_HEADER = "TestNonExistingHeader";
	private static final String TEST_NON_EXISTING_HEADER_LIST = "TestNonExistingHeaderList";
	private static final String TEST_ALLOW_HEADER = "TestAllowHeader";
	private static final String TEST_TO_TAG = "TestToTag";
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final String CANCEL_RECEIVED = "cancelReceived";
	private static final String SUBSCRIBER_URI = "sip:testSubscriberUri@sip-servlets.com";		
	private static final String TEST_REINVITE_USERNAME = "reinvite";
	private static final String TEST_IS_SEND_REINVITE_USERNAME = "isendreinvite";
	private static final String TEST_IS_SEND_REINVITE_PRACK = "prackisendreinvite";
	private static final String TEST_IS_SIP_SERVLET_SEND_BYE = "SSsendBye";
	private static final String TEST_CANCEL_USERNAME = "cancel";
	private static final String TEST_BYE_ON_DESTROY = "testByeOnDestroy";
	@Resource
	SipFactory sipFactory;
	@Resource
	TimerService timerService;
	SipSession registerSipSession;
	SipSession inviteSipSession;
		
	@Override
	protected void doBranchResponse(SipServletResponse resp)
			throws ServletException, IOException {
		resp.getApplicationSession().setAttribute("doBranchResponse", "true");
		super.doBranchResponse(resp);
	}

	
	
	/** Creates a new instance of SimpleProxyServlet */
	public SimpleSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the simple sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("from : " + request.getFrom());
		logger.info("Got request: "
				+ request.getMethod());		
		if(request.getParameterableHeader("additionalParameterableHeader") != null) {
			request.getParameterableHeader("additionalParameterableHeader").setParameter("dsfds", "value");
			boolean error = false;
			try {
				request.getParameterableHeader("nonParameterableHeader").setParameter("dsfds", "value");
			} catch (ServletParseException e) {
				error = true;
			}
			if(error) {
				// Success only if nonParameterable headers thow excpetion when treated as parametereable
				request.createResponse(200).send();
			}
			return;
		}
		
		request.createResponse(SipServletResponse.SC_TRYING).send();
		
		String fromString = request.getFrom().toString();
		if(fromString.contains(TEST_BYE_ON_DESTROY)) {
			inviteSipSession = request.getSession();
		}
		if(fromString.contains(TEST_ERROR_RESPONSE)) {			
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_BUSY_HERE);
			sipServletResponse.send();
			return;
		}
		
		if(fromString.contains(TEST_EXTERNAL_ROUTING_NO_INFO)) {			
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			timerService.createTimer(request.getApplicationSession(), 1000, false, (Serializable)sipServletResponse);
			return;
		}
		
		if(fromString.contains(TEST_EXTERNAL_ROUTING)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.send();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			timerService.createTimer(request.getApplicationSession(), 1000, false, (Serializable)sipServletResponse);
			return;
		}
		
		if(fromString.contains(TEST_ALLOW_HEADER)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_METHOD_NOT_ALLOWED);
			sipServletResponse.setHeader("Allow", "INVITE, ACK, CANCEL, OPTIONS, BYE");
			// Calling setHeader 2 times for Issue 1164 non regression test
			sipServletResponse.setHeader("Allow", "INVITE, ACK, CANCEL, OPTIONS, BYE");
			sipServletResponse.addHeader("Allow", "SUBSCRIBE, NOTIFY");
			sipServletResponse.addHeader("Allow", "REFER");
			sipServletResponse.send();
			return;
		}
		
		logger.info("Subscriber URI received : " + request.getSubscriberURI());
		if(fromString.contains(TEST_SUBSCRIBER_URI) && !SUBSCRIBER_URI.equalsIgnoreCase(request.getSubscriberURI().toString())) {			
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
			sipServletResponse.send();
			return;
		}	
		
		if(fromString.contains(TEST_TO_TAG)) {
			//checking if the to tag is not
			Address fromAddress = request.getFrom();
			Address toAddress   = sipFactory.createAddress(request.getRequestURI(), request.getTo().getDisplayName());
	        SipServletRequest newRequest = sipFactory.createRequest(request.getApplicationSession(), "INVITE", fromAddress, toAddress);
	        if(newRequest.getTo().getParameter("tag") != null) {
				logger.error("the ToTag should be empty, sending 500 response");
				SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
				sipServletResponse.send();
				return;
			}
		}				
		
		request.getAddressHeader(TEST_NON_EXISTING_HEADER);
		request.getHeader(TEST_NON_EXISTING_HEADER);		
		request.getHeaders(TEST_NON_EXISTING_HEADER);
		request.setHeader(TEST_NON_EXISTING_HEADER,"true");
		request.removeHeader(TEST_NON_EXISTING_HEADER);
		request.addHeader(TEST_NON_EXISTING_HEADER,"true");		
		request.addHeader(TEST_NON_EXISTING_HEADER_LIST,"true,false,maybe");
		request.getParameterableHeader("Reply-To");
		request.getParameterableHeaders("Reply-To");
		// Test register cseq issue http://groups.google.com/group/mobicents-public/browse_thread/thread/70f472ca111baccf
		if(fromString.contains(TEST_REGISTER_C_SEQ)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.send();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
			
			sendRegister(null, false);			
			return;
		}
		if(fromString.contains(TEST_REGISTER_NO_CONTACT)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.send();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
			
			sendRegister(null, true);			
			return;
		}
		if(fromString.contains(TEST_REGISTER_SAVED_SESSION)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.send();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
			
			sendRegister();
			return;
		}
		if(fromString.contains(TEST_PRACK)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.sendReliably();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.getSession().setAttribute("okResponse", sipServletResponse);
			
			return;
		}
		if(!TEST_CANCEL_USERNAME.equalsIgnoreCase(((SipURI)request.getFrom().getURI()).getUser())) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			if(sipServletResponse.getParameterableHeader("Contact") == null) {
				logger.error("the Contact Header on a non 3xx or 485 response should be set");
				sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
				sipServletResponse.send();
			}
			sipServletResponse.send();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			if(fromString.contains(TEST_FLAG_PARAM)) {
				try {
					sipServletResponse.setHeader("Contact", "sip:127.0.0.1:5070");
					logger.error("an IllegalArgumentException should be thrown when trying to set the Contact Header on a 2xx response");
					sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
					sipServletResponse.send();
				} catch (IllegalArgumentException e) {
					logger.info("Contact Header is not set-able for the 2XX response to an INVITE");
				}				
				Parameterable contact = sipServletResponse.getParameterableHeader("Contact");
				contact.setParameter("flagparam", "");
				String contactStringified = contact.toString().trim();
				logger.info("Contact Header with flag param " + contactStringified);
				if(contactStringified.endsWith("flagparam=")) {
					logger.error("the flagParam should not contains the equals followed by empty, it is a flag so no equals sign should be present, sending 500 response");
					sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
					sipServletResponse.send();
					return;
				}
				contact.setParameter("flagparam", null);
				contactStringified = contact.toString().trim();
				logger.info("Contact Header with flag param " + contactStringified);
				if(contactStringified.endsWith("flagparam")) {
					logger.error("the flagParam should have been removed when setting its value to null, sending 500 response");
					sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
					sipServletResponse.send();
					return;
				}
				contact = sipFactory.createParameterable("sip:user@127.0.0.1:5080;flagparam");
				contactStringified = contact.toString().trim();
				logger.info("Contact Header with flag param " + contactStringified);
				if(contactStringified.endsWith("flagparam=")) {
					logger.error("the flagParam should not contains the equals followed by empty, it is a flag so no equals sign should be present, sending 500 response");
					sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
					sipServletResponse.send();
					return;
				}
			}
			sipServletResponse.getAddressHeader(TEST_NON_EXISTING_HEADER);
			sipServletResponse.getHeader(TEST_NON_EXISTING_HEADER);		
			sipServletResponse.getHeaders(TEST_NON_EXISTING_HEADER);
			sipServletResponse.setHeader(TEST_NON_EXISTING_HEADER,"true");
			sipServletResponse.removeHeader(TEST_NON_EXISTING_HEADER);
			sipServletResponse.addHeader(TEST_NON_EXISTING_HEADER,"true");		
			sipServletResponse.addHeader(TEST_NON_EXISTING_HEADER_LIST,"true,false,maybe");
			sipServletResponse.getParameterableHeader("Reply-To");
			
			sipServletResponse.send();
		} else {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);			
			sipServletResponse.send();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				logger.error("unexpected exception while waiting ", e);
			}
//			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
//			sipServletResponse.send();
		}
	}
	
	@Override
	protected void doPrack(SipServletRequest req) throws ServletException,
			IOException {
		req.createResponse(SipServletResponse.SC_OK).send();		
		SipServletResponse okResponseToInvite = (SipServletResponse) req.getSession().getAttribute("okResponse");
		okResponseToInvite.send();
	}
	
	@Override
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		if(req.getFrom().getURI() instanceof SipURI) {
			if(TEST_REINVITE_USERNAME.equalsIgnoreCase(((SipURI)req.getFrom().getURI()).getUser())) {
				SipServletRequest reInvite = req.getSession(false).createRequest("INVITE");
				
				if(reInvite.getHeader("Contact").contains("0.0.0.0")) {
					logger.error("Reinvite doesn't add correct address. We must not see 0.0.0.0 here");
					return;
				}
				if(req.getSession(false) == reInvite.getSession(false)) {
					reInvite.send();
				} else {
					logger.error("the newly created subsequent request doesn't have " +
							"the same session instance as the one it has been created from");
				}
			} else if(TEST_IS_SEND_REINVITE_USERNAME.equalsIgnoreCase(((SipURI)req.getFrom().getURI()).getUser())
					|| TEST_IS_SEND_REINVITE_PRACK.equalsIgnoreCase(((SipURI)req.getFrom().getURI()).getUser())) {
				Integer nbOfAcks = (Integer) req.getSession().getAttribute("nbAcks");
				if(nbOfAcks == null) {
					nbOfAcks = Integer.valueOf(1);
				} else {
					nbOfAcks = Integer.valueOf(nbOfAcks.intValue() + 1);
				}
				req.getSession().setAttribute("nbAcks", nbOfAcks);
			} else if(TEST_IS_SIP_SERVLET_SEND_BYE.equalsIgnoreCase(((SipURI)req.getFrom().getURI()).getUser())) {				
				timerService.createTimer(req.getApplicationSession(), 10000, false, (Serializable)req.getSession());
			}
		}
		String fromString = req.getFrom().toString();
		if(fromString.contains(TEST_ERROR_RESPONSE)) {			
			sendMessage(req.getApplicationSession(), sipFactory, "ackReceived", null);
			return;
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		// This is for the REGISTER CSeq test
		final String fromString = resp.getFrom().toString();
		if(resp.getMethod().equalsIgnoreCase("REGISTER") && !fromString.contains(TEST_REGISTER_NO_CONTACT)) {
			int cseq = Integer.parseInt(resp.getRequest().getHeader("CSeq").substring(0,1));
			if(cseq < 4) {
				try {
					// Put some delay as per http://groups.google.com/group/mobicents-public/browse_thread/thread/70f472ca111baccf
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					logger.error("Unexpected exception", e);
				}
				// Check if the session remains in INITIAL state, if not, the test will fail for missing registers
				if(resp.getSession().getState().equals(State.INITIAL)) {					
					if(fromString.contains(TEST_REGISTER_SAVED_SESSION)) {
//						registerSipSession = resp.getSession();
						sendRegister();
					} else {
						sendRegister(resp.getSession(), false);
					}					
				}
			}
			return;
		}
		if(!"BYE".equalsIgnoreCase(resp.getMethod()) && !"MESSAGE".equalsIgnoreCase(resp.getMethod())) {
			resp.createAck().send();
			resp.getSession(false).createRequest("BYE").send();
		}
	}

	@Override
	protected void doErrorResponse(SipServletResponse response)
			throws ServletException, IOException {
			
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		if(response.getStatus() == SipServletResponse.SC_UNAUTHORIZED || 
				response.getStatus() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
		
			// Avoid re-sending if the auth repeatedly fails.
			if(!"true".equals(getServletContext().getAttribute("FirstResponseRecieved")))
			{				
				getServletContext().setAttribute("FirstResponseRecieved", "true");
				AuthInfo authInfo = sipFactory.createAuthInfo();
				authInfo.addAuthInfo(response.getStatus(), "sip-servlets-realm", "user", "pass");
				SipServletRequest challengeRequest = null;
				String fromString = response.getFrom().toString();
				if(fromString.contains(TEST_REGISTER_SAVED_SESSION)) {
//					registerSipSession = response.getSession();
					challengeRequest = registerSipSession.createRequest(
							response.getMethod());
				} else {
					challengeRequest = response.getSession().createRequest(
							response.getMethod());
				}					
				
				challengeRequest.addAuthHeader(response, authInfo);
				challengeRequest.send();
			}
		}
		
		logger.info("Got response: " + response);	
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		int statusCode = SipServletResponse.SC_OK;
		
		logger.info("Got BYE request: " + request);
		if(TEST_IS_SEND_REINVITE_USERNAME.equalsIgnoreCase(((SipURI)request.getFrom().getURI()).getUser())) {
			Integer nbOfAcks = (Integer) request.getSession().getAttribute("nbAcks");
			if(nbOfAcks == null || nbOfAcks.intValue() != 2) {
				logger.error("Number of ACK seen " + nbOfAcks + " sending Error Response");
				statusCode = SipServletResponse.SC_DECLINE;
			}
		} 
		SipServletResponse sipServletResponse = request.createResponse(statusCode);
	
		
		// Force fail by not sending OK if the doBranchResponse is called. In non-proxy app
		// this would be wrong.
		if(!"true".equals(request.getApplicationSession().getAttribute("doBranchResponse")))
			sipServletResponse.send();
	}	
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		
		logger.info("Got CANCEL request: " + request);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		try {
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipFactory.createApplicationSession(), 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(CANCEL_RECEIVED.length());
			sipServletRequest.setContent(CANCEL_RECEIVED, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		Address contact = req.getAddressHeader("Contact");
		contact.setExpires(3600);
		logger.info("REGISTER Contact Address.toString = " + contact.toString());
		int response = SipServletResponse.SC_OK;
		if(!"<sip:sender@127.0.0.1:5080;transport=udp;lr>;expires=3600".equals(contact.toString())) {
			response = SipServletResponse.SC_SERVER_INTERNAL_ERROR;
		}
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}
	
	@Override
	protected void doInfo(SipServletRequest req) throws ServletException,
			IOException {
		String content = (String) req.getContent();
		req.getSession().setAttribute("mutable", content);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int response = SipServletResponse.SC_OK;
		if(!content.equals(req.getSession().getAttribute("mutable")))
			response = SipServletResponse.SC_SERVER_INTERNAL_ERROR;
		
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}
	
	// SipErrorListener methods

	/**
	 * {@inheritDoc}
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");
		sendMessage(ee.getRequest().getApplicationSession(), sipFactory, "noAckReceived", null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
	}

	public void timeout(ServletTimer timer) {
		Serializable info = timer.getInfo();
		if(info instanceof SipServletResponse) {
			SipServletResponse sipServletResponse = (SipServletResponse)timer.getInfo();
			try {
				sipServletResponse.send();
			} catch (IOException e) {
				logger.error("Unexpected exception while sending the OK", e);
			}
		} else {
			try {
				((SipSession)info).createRequest("BYE").send();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void sendRegister() throws ServletParseException, IOException {
		SipServletRequest register = null;
		if (registerSipSession != null) {
			logger.info("saved session instance : " + registerSipSession);			
			logger.info("session attribute is : " + registerSipSession.getAttribute("attribute"));
			register = registerSipSession.createRequest("REGISTER");			
		} else {
			SipApplicationSession app = sipFactory.createApplicationSession();
			register = sipFactory.createRequest(app, "REGISTER", "sip:testRegisterSavedSession@simple-servlet.com", "sip:you@localhost:5058");
			Parameterable contact = sipFactory.createParameterable("sip:john@127.0.0.1:6090;expires=900");
			register.addParameterableHeader("Contact", contact, true);
			registerSipSession = register.getSession();
			logger.info("saved session instance : " + registerSipSession);
			registerSipSession.setAttribute("attribute", "value");
		}
		register.setHeader("Expires", "3600");
		register.setHeader("test", "test");
		register.send();
	}
	
	private void sendRegister(SipSession sipSession, boolean removeContact) throws ServletParseException, IOException {
		SipServletRequest register = null;
		if (sipSession != null) {			
			register = sipSession.createRequest("REGISTER");
		} else {
			SipApplicationSession app = sipFactory.createApplicationSession();
			register = sipFactory.createRequest(app, "REGISTER", "sip:testRegisterCSeq@simple-servlet.com", "sip:you@localhost:5058");
			Parameterable contact = sipFactory.createParameterable("sip:john@127.0.0.1:6090;expires=900");
			register.addParameterableHeader("Contact", contact, true);			
		}
		register.setHeader("Expires", "3600");
		register.setHeader("test", "test");
		if(removeContact) {
			register.removeHeader("Contact");
		}
		register.send();
	}	

	public void sipConnectorAdded(SipConnector connector) {
		logger.info(connector + " added" );
		if(connector.getTransport().equalsIgnoreCase(ListeningPoint.TCP) && connector.getPort() == 5072) {
			// Issue 1161
			sendMessage(sipFactory.createApplicationSession(), sipFactory, "sipConnectorAdded", ListeningPoint.UDP);
			sendMessage(sipFactory.createApplicationSession(), sipFactory, "sipConnectorAdded", connector.getTransport());			
		} else if(connector.getPort() != 5072) {
			sendMessage(sipFactory.createApplicationSession(), sipFactory, "sipConnectorAdded", connector.getTransport());
		}
	}

	public void sipConnectorRemoved(SipConnector connector) {
		logger.info(connector + " removed" );
		sendMessage(sipFactory.createApplicationSession(), sipFactory, "sipConnectorRemoved", connector.getTransport());
	}

	/**
	 * @param sipApplicationSession
	 * @param storedFactory
	 */
	private void sendMessage(SipApplicationSession sipApplicationSession,
			SipFactory storedFactory, String content, String transport) {
		try {
			SipServletRequest sipServletRequest = storedFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			sipServletRequest.addHeader("Ext", "Test 1, 2 ,3");
			SipURI sipUri = storedFactory.createSipURI("receiver", "127.0.0.1:5080");
			if(transport != null) {
				if(transport.equalsIgnoreCase(ListeningPoint.TCP)) {
					sipUri = storedFactory.createSipURI("receiver", "127.0.0.1:5081");
				}
				sipUri.setTransportParam(transport);
			}
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(content.length());
			sipServletRequest.setContent(content, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
	
	@Override
	public void destroy() {
		if(inviteSipSession != null) {
			try {
				inviteSipSession.createRequest("BYE").send();
			} catch (IOException e) {
				logger.error("Exception occured while sending the request",e);			
			}
		}
		super.destroy();
	}
}