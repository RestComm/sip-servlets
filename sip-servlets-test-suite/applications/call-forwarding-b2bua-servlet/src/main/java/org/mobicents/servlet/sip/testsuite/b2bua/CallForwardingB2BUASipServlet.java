/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.servlet.sip.testsuite.b2bua;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.URI;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipSessionExt;
import org.mobicents.servlet.sip.message.B2buaHelperImpl;


public class CallForwardingB2BUASipServlet extends SipServlet implements SipErrorListener, SipApplicationSessionListener, TimerListener {
	private static final String CHECK_CONTACT = "checkContact";
	private static final String TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED = "testSipAppSessionReadyToBeInvalidated";
	private static final String TEST_USE_LINKED_REQUEST = "useLinkedRequest";
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final long serialVersionUID = 1L;
	private static final String ACT_AS_UAS = "actAsUas";
	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUASipServlet.class);
	private static Map<String, String[]> forwardingUris = null;
	
	static {
		forwardingUris = new HashMap<String, String[]>();
		forwardingUris.put("sip:forward-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-sender-forking-pending@sip-servlets.com", 
				new String[]{"sip:forward-receiver-forking-pending@sip-servlets.com", "sip:forward-receiver-forking-pending@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
		forwardingUris.put("sip:forward-sender-factory-same-callID@sip-servlets.com", 
				new String[]{"sip:forward-receiver-factory-same-callID@sip-servlets.com", "sip:forward-receiver-factory-same-callID@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-sender-factory-same-callID-kill-original-session@sip-servlets.com", 
				new String[]{"sip:forward-receiver-factory-same-callID-kill-original-session@sip-servlets.com", "sip:forward-receiver-factory-same-callI-kill-original-sessionD@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-sender-408@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-sender-408-new-thread@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:factory-sender@sip-servlets.com", 
				new String[]{"sip:factory-receiver@sip-servlets.com", "sip:factory-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-pending-sender@sip-servlets.com", 
				new String[]{"sip:forward-pending-receiver@sip-servlets.com", "sip:forward-pending-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:blocked-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:blocked-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:forward-tcp-sender@sip-servlets.com", 
			new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090;transport=tcp"});
		forwardingUris.put("sip:forward-udp-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080"});
		forwardingUris.put("sip:forward-udp-sender-tcp-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080"});
		forwardingUris.put("sip:forward-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090", 
				new String[]{"sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090", "sip:forward-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:composition@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090", 
				new String[]{"sip:forward-composition@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:forward-composition@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
		forwardingUris.put("sip:composition@sip-servlets.com", 
				new String[]{"sip:forward-composition@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:forward-composition@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
		forwardingUris.put("sip:sender@sip-servlets.com", 
				new String[]{"sip:fromB2BUA@sip-servlets.com", "sip:fromB2BUA@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090"});
		forwardingUris.put("sip:samesipsession@sip-servlets.com", 
				new String[]{"sip:forward-samesipsession@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:forward-samesipsession@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
		forwardingUris.put("sip:cancel-samesipsession@sip-servlets.com", 
				new String[]{"sip:cancel-forward-samesipsession@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:cancel-forward-samesipsession@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
		forwardingUris.put("sip:error-samesipsession@sip-servlets.com", 
				new String[]{"sip:error-forward-samesipsession@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:error-forward-samesipsession@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
		forwardingUris.put("sip:forward-myself@sip-servlets.com", 
				new String[]{"sip:forward-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070", "sip:forward-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070"});
	}

	SipSession incomingSession;
	SipSession outgoingSession;
	int okReceived = 0;

	SipURI aliceContact;
	SipURI incomingInterface;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding B2BUA sip servlet has been started");
		super.init(servletConfig);		
	}
	
	@Override
	protected void doRequest(SipServletRequest request) throws ServletException,
			IOException {
		if (request.getFrom().getURI().toString().contains("2-connectors-port-issue-sender") || request.getFrom().getURI().toString().contains("2-connectors-port-issue-receiver")) {
			try {
			    if ("REGISTER".equals(request.getMethod())) {
			        if (request.getFrom().getURI().toString().contains("2-connectors-port-issue-sender")) {
			            // when alice registers, let's keep her contact using remote port information
			            String contact = request.getHeader("Contact");
			            SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
			    				SIP_FACTORY);
			            aliceContact = (SipURI) sipFactory.createURI(contact.substring(1, contact.length() - 1));
			            aliceContact.setPort(request.getRemotePort());
			            // Storing the incoming interface on which we received the message to be able to pick the correct interface
			            // When sending requests to this SIP UA.
			            String ipAddress = request.getLocalAddr();
			            int port = request.getLocalPort();
			            String transport = request.getTransport();
			            List<SipURI> outboundInterfaces = (List<SipURI>) getServletContext().getAttribute(OUTBOUND_INTERFACES);
			            for (SipURI uri : outboundInterfaces) {
					if(uri.getHost().equalsIgnoreCase(ipAddress) && uri.getPort() == port && uri.getTransportParam().equalsIgnoreCase(transport)) {
						incomingInterface = uri;
					}
				    }
			        } else {
			            // when any request from bob is received, let's try to send an INVITE to Alice
			            SipSession session = request.getSession();
			            // setting the outbound interface to make sure the container pick the correct connector
			       	    ((SipSessionExt)session).setOutboundInterface(incomingInterface);
			            SipServletRequest out = session.createRequest("INVITE");
			            out.setRequestURI(aliceContact);
			            out.send();
			        }
			        request.createResponse(SipServletResponse.SC_OK).send();
			    }
			} catch (Exception ex) {
			    log("Error processing request", ex);
			}
		}
		super.doRequest(request);
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got : " + request.toString());
		SipSession session = request.getSession();
		if(request.getTo().toString().contains("b2bua@sip-servlet")) {
			session.createRequest("MESSAGE").send();
		}
		if(new File("proxy-b2bua.case.flag").exists()) {
			try {
				Thread.sleep(200);
				SipServletRequest r = session.createRequest("INVITE");
				String rheaderBefore = r.getHeader("Route");
				r.send();
				String rheaderAfter = r.getHeader("Route");
				if(rheaderBefore == null || rheaderAfter == null || rheaderBefore.equals(rheaderAfter)) {
					// we expect the sent request to used update Route header without IP LB address due to optimization
					new File("proxy-b2bua.failure.flag").createNewFile();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(request.getFrom().getURI().toString().contains("pending") || 
				request.getFrom().getURI().toString().contains("factory")) {
			B2buaHelper helper = request.getB2buaHelper();
	        SipSession peerSession = helper.getLinkedSession(request.getSession());
			List<SipServletMessage> pendingMessages = helper.getPendingMessages(peerSession, UAMode.UAC);
	        SipServletResponse invitePendingResponse = null;
	        logger.info("pending messages : ");
	        for(SipServletMessage pendingMessage : pendingMessages) {
	        	logger.info("\t pending message : " + pendingMessage);
	        	if(((SipServletResponse)pendingMessage).getStatus() == 200) {
	        		invitePendingResponse = (SipServletResponse)pendingMessage;
	        		break;
	        	}
	        }
	        invitePendingResponse.createAck().send();
		}	
//		SipSession linkedSession = helper.getLinkedSession(session);
//		SipServletRequest forkedRequest = linkedSession.createRequest("ACK");
//		forkedRequest.setContentLength(request.getContentLength());
//		forkedRequest.setContent(request.getContent(), request.getContentType());
//		logger.info("forkedRequest = " + forkedRequest);
//		
//		forkedRequest.send();
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		request.getApplicationSession().setAttribute("double-callback-test", request.getHeader("DOUBLE")!=null);
			// Issue 1484 : http://code.google.com/p/mobicents/issues/detail?id=1484
		// check the state of the session it shouldn't be Terminated
		request.getSession().getAttribute("Foo");
		incomingSession = request.getSession();
		State state = request.getSession().getState();
		logger.info("session state : " + state);
		if(state == State.TERMINATED) {
			throw new IllegalStateException("It shouldn't be in TERMINATED state");
		}
		if(request.isInitial()) {
			logger.info("Got INVITE: " + request.toString());
			logger.info(request.getFrom().getURI().toString());
			if(request.getTo().getURI().toString().contains(TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED)) {
				request.getApplicationSession().setAttribute(TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED, Integer.valueOf(0));
			}
			if(request.getTo().getURI().toString().contains(TEST_USE_LINKED_REQUEST)) {
				request.getApplicationSession().setAttribute(TEST_USE_LINKED_REQUEST, Boolean.TRUE);
			}			
			if(request.getTo().getURI().toString().contains(CHECK_CONTACT)) {
				Address contactAddress = request.getAddressHeader("Contact");
				Set<Entry<String, String>> params = contactAddress.getParameters();
				Entry<String, String> entry = params.iterator().next();
				String paramValue = entry.getValue();
				logger.info("checkcontact address " + contactAddress.toString());			
				logger.info("checkcontact param value " + paramValue);
				// http://code.google.com/p/mobicents/issues/detail?id=1477
				// Address parameters become un-quoted
				if(!contactAddress.toString().equals("<sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5056>;+sip.instance=\"<urn:uuid:some-xxxx>\"")) {
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "bad contact address").send();
					return;
				}
				if(!paramValue.equals("\"<urn:uuid:some-xxxx>\"")) {
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "bad contact param value").send();
					return;
				}
			}
			String[] forwardingUri = forwardingUris.get(request.getFrom().getURI().toString());
			if(((SipURI)request.getTo().getURI()).getUser().contains("forward-samesipsession")) {
				request.getSession().setAttribute(ACT_AS_UAS, Boolean.TRUE);
				if(request.getSession().getAttribute("originalRequest") == null) {
					if(((SipURI)request.getTo().getURI()).getUser().contains("cancel")) {
						request.createResponse(SipServletResponse.SC_RINGING).send();
					} else if(((SipURI)request.getTo().getURI()).getUser().contains("error")) {
						request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "expected error").send();
					} else {
						request.createResponse(SipServletResponse.SC_RINGING).send();
						request.createResponse(SipServletResponse.SC_OK).send();
					}
				} else {
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "same sip session").send();
				}
				return ;
			}
			
			
			if(forwardingUri != null && forwardingUri.length > 0) {
				if(((SipURI)request.getFrom().getURI()).getUser().contains("factory")) {
					forwardInviteUsingSipFactory(request, forwardingUri);
				} else {
					forwardRequestUsingB2BUAHelper(request, forwardingUri);
				}
			} else {
				logger.info("INVITE has not been forwarded.");
			}
		} else {
			// deals with reinvite requests
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession origSession = b2buaHelper.getLinkedSession(request.getSession());
			origSession.setAttribute("originalRequest", request);
			Map<String, List<String>> headers=new HashMap<String, List<String>>();
			List<String> contactHeaderList = new ArrayList<String>();
			if(request.getHeader(ContactHeader.NAME).contains("transport=tcp")) {
				contactHeaderList.add("\"callforwardingB2BUA\" <sip:test@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070;q=0.1;lr;transport=tcp;test>;test");
			} else {
				contactHeaderList.add("\"callforwardingB2BUA\" <sip:test@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070;q=0.1;lr;transport=udp;test>;test");
			}
			headers.put("Contact", contactHeaderList);
			SipServletRequest forkedRequest = b2buaHelper.createRequest(origSession, request, headers);
			forkedRequest.getAddressHeader("From").setDisplayName("display name set correctly");			
			String method = request.getHeader("Method");
			if(method != null) {
				forkedRequest.getSession().setAttribute("method", method);
			}
			forkedRequest.send();
		}
	}	
	
	@Override
	protected void doRegister(SipServletRequest request) throws ServletException,
			IOException {
		String[] forwardingUri = forwardingUris.get(request.getFrom().getURI().toString());
		
		if(forwardingUri != null && forwardingUri.length > 0) {
			forwardRequestUsingB2BUAHelper(request, forwardingUri);
		} else {
			logger.info("REGISTER has not been forwarded.");
		}
	}
	
	private void forwardRequestUsingB2BUAHelper(SipServletRequest request,
			String[] forwardingUri) throws IOException, IllegalArgumentException, TooManyHopsException, ServletParseException {
		B2buaHelper helper = request.getB2buaHelper();						
		
		helper.createResponseToOriginalRequest(request.getSession(), SipServletResponse.SC_TRYING, "").send();
		
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
		
		Map<String, List<String>> headers=new HashMap<String, List<String>>();
		
		List<String> toHeaderList = new ArrayList<String>();
		toHeaderList.add(forwardingUri[0]);
		headers.put("To", toHeaderList);
		
		List<String> userAgentHeaderList = new ArrayList<String>();
		userAgentHeaderList.add("CallForwardingB2BUASipServlet");
		headers.put("User-Agent", userAgentHeaderList);
		
		if(((SipURI)request.getFrom().getURI()).getUser().contains("tcp-sender")) {
			if(request.getHeader(ContactHeader.NAME).contains("transport=tcp")) {
				List<String> contactHeaderList = new ArrayList<String>();
				contactHeaderList.add("\"callforwardingB2BUA\" <sip:test@192.168.15.15:5055;q=0.1;lr;transport=udp;test;method=INVITE;>;test");
				headers.put("Contact", contactHeaderList);
			} else {
				List<String> contactHeaderList = new ArrayList<String>();
				contactHeaderList.add("\"callforwardingB2BUA\" <sip:test@192.168.15.15:5055;q=0.1;lr;transport=tcp;test;method=INVITE;>;test");
				headers.put("Contact", contactHeaderList);
			}
		}
		
		List<String> extensionsHeaderList = new ArrayList<String>();
		extensionsHeaderList.add("extension-header-value1");
		extensionsHeaderList.add("extension-header-value2");
		headers.put("extension-header", extensionsHeaderList);
		
		SipServletRequest forkedRequest = helper.createRequest(request, true,
				headers);
		forkedRequest.getAddressHeader("From").setDisplayName("display name set correctly");
		SipURI sipUri = (SipURI) sipFactory.createURI(forwardingUri[1]);
		forkedRequest.setRequestURI(sipUri);						
		
		logger.info("forkedRequest = " + forkedRequest);
		outgoingSession = forkedRequest.getSession();
		forkedRequest.getSession().setAttribute("originalRequest", request);
		if(request.getFrom().getURI().toString().contains("myself")) {
			((SipURI)forkedRequest.getAddressHeader("From").getURI()).setUser("forward-sender");
		}
		
		forkedRequest.send();
	}

	private void forwardInviteUsingSipFactory(SipServletRequest origReq, String[] forwardingUri) throws ServletParseException, UnsupportedEncodingException, IOException {
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
		B2buaHelper b2buaHelper = origReq.getB2buaHelper();
        URI from = origReq.getTo().getURI();
        URI to= sipFactory.createURI(forwardingUri[1]);
        SipApplicationSession appSession = origReq.getApplicationSession();
        SipServletRequest newRequest = null;
        if(((SipURI)origReq.getFrom().getURI()).getUser().contains("same-callID")) {
        	if(((SipURI)origReq.getFrom().getURI()).getUser().contains("kill-original-session")) {
        		origReq.createResponse(SipServletResponse.SC_BUSY_HERE).send();
        		origReq.getSession().invalidate();        		
        	}
        	newRequest = sipFactory.createRequest(origReq, true);   
        	newRequest.setRequestURI(to);
        } else {
        	newRequest = sipFactory.createRequest(appSession, "INVITE", from, to);
        }
        if(origReq.getContent() != null) {
        	newRequest.setContent(origReq.getContent(), origReq.getContentType());
        }
        newRequest.setHeader("Allow", "INVITE, ACK, CANCEL, BYE, REFER, UPDATE, INFO");
        if(!((SipURI)origReq.getFrom().getURI()).getUser().contains("kill-original-session")) {
        	b2buaHelper.linkSipSessions(origReq.getSession(), newRequest.getSession()); //Linking sessions!        
        	origReq.getSession().setAttribute("originalRequest", origReq);
        }
        logger.info("forkedRequest = " + newRequest);        
        if(!((SipURI)origReq.getFrom().getURI()).getUser().contains("kill-original-session")) {
        	newRequest.getSession().setAttribute("originalRequest", origReq);
        	newRequest.getB2buaHelper().linkSipSessions(origReq.getSession(), newRequest.getSession()); //Linking sessions!
        }
        outgoingSession = newRequest.getSession();
        
        if(origReq.getFrom().getURI().toString().contains("myself")) {
			((SipURI)newRequest.getAddressHeader("From").getURI()).setUser("forward-sender");
		}
        
        newRequest.send();        
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got BYE: " + request.toString());		
		
		if(request.getFrom().toString().contains("408")) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
			return;
		}
		
		if(request.getSession().getAttribute(ACT_AS_UAS) == null) {
			if(getServletContext().getInitParameter("checkOriginalRequestMapSize") != null && ((B2buaHelperImpl)request.getB2buaHelper()).getOriginalRequestMap().size() != 0) {
				logger.error("originalRequestMap size is wrong " + ((B2buaHelperImpl)request.getB2buaHelper()).getOriginalRequestMap().size());
				return;
			}
			//we forward the BYE
			SipSession session = request.getSession();		
			SipSession linkedSession = request.getB2buaHelper().getLinkedSession(session);
			logger.info("Incoming side session state : " + request.getSession().getState());
			logger.info("outgoing side session state : " + linkedSession.getState());
			if(!linkedSession.getState().equals(State.CONFIRMED)) {
				request.createResponse(500, "outgoing session state should be CONFIRMED and it is " + linkedSession.getState()).send();
				return;
			}
			Map<String, List<String>> headers=new HashMap<String, List<String>>();
			
			List<String> userAgentHeaderList = new ArrayList<String>();
			userAgentHeaderList.add("CallForwardingB2BUASipServlet");
			headers.put("User-Agent", userAgentHeaderList);
			
			List<String> contactHeaderList = new ArrayList<String>();
			contactHeaderList.add("\"callforwardingB2BUA\" <sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070;transport=tcp>");
			headers.put("Contact", contactHeaderList);
			
			List<String> extensionsHeaderList = new ArrayList<String>();
			extensionsHeaderList.add("extension-header-value1");
			extensionsHeaderList.add("extension-header-value2");
			headers.put("extension-header", extensionsHeaderList);
			
			SipServletRequest forkedRequest = request.getB2buaHelper().createRequest(linkedSession, request, headers);
//			SipServletRequest forkedRequest = linkedSession.createRequest("BYE");			
			logger.info("forkedRequest = " + forkedRequest);			
			forkedRequest.send();
		}		
		//we send the OK to the first call leg after as sending the response may trigger session invalidation
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}	
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got CANCEL: " + request.toString());
		if(!((SipURI)request.getTo().getURI()).getUser().equals("cancel-forward-samesipsession")) {			
			SipSession session = request.getSession();
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession linkedSession = b2buaHelper.getLinkedSession(session);
//			SipServletRequest originalRequest = (SipServletRequest)linkedSession.getAttribute("originalRequest");
//			if(originalRequest != null) {
//				SipServletRequest  cancelRequest = b2buaHelper.getLinkedSipServletRequest(originalRequest).createCancel();				
			SipServletRequest  cancelRequest = b2buaHelper.createCancel(linkedSession);
				logger.info("forkedRequest = " + cancelRequest);			
				cancelRequest.send();
//			} else {
//				logger.info("no invite to cancel, it hasn't been forwarded");
//			}
		}
	}
	
	@Override
    protected void doUpdate(SipServletRequest request) throws ServletException,
            IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got UPDATE: " + request.toString());
		}
        B2buaHelper helper = request.getB2buaHelper();
        SipSession peerSession = helper.getLinkedSession(request.getSession());
        SipServletRequest update = helper.createRequest(peerSession, request, null);
        update.send();
    }
	
	@Override
    protected void doPrack(SipServletRequest req) throws ServletException,
            IOException {
        
        B2buaHelper helper = req.getB2buaHelper();
        SipSession peerSession = helper.getLinkedSession(req.getSession());
        List<SipServletMessage> pendingMessages = helper.getPendingMessages(peerSession, UAMode.UAC);
        SipServletResponse invitePendingResponse = null;
        logger.info("pending messages : ");
        for(SipServletMessage pendingMessage : pendingMessages) {
        	logger.info("\t pending message : " + pendingMessage);
        	if(((SipServletResponse)pendingMessage).getStatus() > 100) {
        		invitePendingResponse = (SipServletResponse)pendingMessage;
        		break;
        	}
        }
        SipServletResponse inviteResponse = (SipServletResponse) invitePendingResponse;
        SipServletRequest prack = inviteResponse.createPrack();
        String[] forwardingUri = forwardingUris.get(prack.getFrom().getURI().toString());
        SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
        SipURI sipUri = (SipURI) sipFactory.createURI(forwardingUri[1]);
		prack.setRequestURI(sipUri);						
        prack.send();
        
        if(logger.isTraceEnabled()) {
            logger.trace("[Send " + prack.getMethod() + " Request]:" + prack);
        }
    }
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.toString());

		if (sipServletResponse.getFrom().getURI().toString().contains("2-connectors-port-issue-sender") || sipServletResponse.getFrom().getURI().toString().contains("2-connectors-port-issue-receiver")) {
			// we send the ACK directly only in non PRACK scenario
			SipServletRequest ackRequest = sipServletResponse.createAck();
			logger.info("Sending " +  ackRequest);
			ackRequest.send();
			return;
		}
		String cSeqValue = sipServletResponse.getHeader("CSeq");
		B2buaHelper b2buaHelper = sipServletResponse.getRequest().getB2buaHelper();			
		
		SipSession originalSession =   
		    sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
		
		if(((SipURI)sipServletResponse.getFrom().getURI()).getUser().contains("same-callID-kill-original-session")) {
			sipServletResponse.getSession().setAttribute(ACT_AS_UAS, true);
			SipServletRequest ackRequest = sipServletResponse.createAck();
			logger.info("Sending " +  ackRequest);
			ackRequest.send();
		}
		
		if(sipServletResponse.getApplicationSession().getAttribute(TEST_USE_LINKED_REQUEST) != null) {
			if(getServletContext().getInitParameter("checkOriginalRequestMapSize") != null && ((B2buaHelperImpl)b2buaHelper).getOriginalRequestMap().size() != 2 && cSeqValue.indexOf("INVITE") != -1) {
				logger.error("originalRequestMap size is wrong " + ((B2buaHelperImpl)b2buaHelper).getOriginalRequestMap().size());
				return;
			}
			boolean sendAck = true;
			if(sipServletResponse.getFrom().getURI().toString().contains("pending") || sipServletResponse.getFrom().getURI().toString().contains("factory")
						|| sipServletResponse.getTo().getURI().toString().contains("pending") || sipServletResponse.getTo().getURI().toString().contains("factory")) {
				sendAck= false;
				if (logger.isDebugEnabled()) {
					logger.debug("testing pending messages so not sending ACK");
				}
			} 
			if(sendAck && cSeqValue.indexOf("INVITE") != -1) {
				// we send the ACK directly only in non PRACK scenario
				SipServletRequest ackRequest = sipServletResponse.createAck();
				logger.info("Sending " +  ackRequest);
				ackRequest.send();
			}
			logger.info("using linkedRequest to send the response: ");
			SipServletRequest otherReq = b2buaHelper.getLinkedSipServletRequest(sipServletResponse.getRequest());
			SipServletResponse other = otherReq.createResponse(sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
			other.send();
		} else {
					
			if ("PRACK".equals(sipServletResponse.getMethod()) || "UPDATE".equals(sipServletResponse.getMethod())) {            
	            List<SipServletMessage> pendingMessages = sipServletResponse.getRequest().getB2buaHelper().getPendingMessages(originalSession, UAMode.UAS);
	            SipServletRequest prackPendingMessage =null; 
	            logger.info("pending messages : ");
	            for(SipServletMessage pendingMessage : pendingMessages) {
	            	logger.info("\t pending message : " + pendingMessage);
	            	if(((SipServletRequest) pendingMessage).getMethod().equals("PRACK") || 
	            			((SipServletRequest) pendingMessage).getMethod().equals("UPDATE")) {
	            		prackPendingMessage = (SipServletRequest) pendingMessage;
	            		break;
	            	}
	            }
	            prackPendingMessage.createResponse(sipServletResponse.getStatus()).send();
	            return;
	        } 
			if(originalSession!= null && cSeqValue.indexOf("REGISTER") != -1) {			
				SipSession peerSession = sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
			    try {				    	
			    	SipServletResponse responseToOriginalRequest = sipServletResponse.getRequest().getB2buaHelper().createResponseToOriginalRequest(peerSession, sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
			    	logger.info("Created response " + responseToOriginalRequest + " to original request " +  sipServletResponse.getRequest());
			    	responseToOriginalRequest.send();
			    	return;
			    }catch (Exception e) {
			    	logger.error("exception happened while trying to forward the response to the other side", e);
			    	peerSession.createRequest("BYE").send();
			    	sipServletResponse.getSession().createRequest("BYE").send();
				}
			}
			//if this is a response to an INVITE we ack it and forward the OK 
			if(originalSession!= null && cSeqValue.indexOf("INVITE") != -1) {			
				//create and sends OK for the first call leg							
				SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
				boolean sendAck = true;
				String fromURI = sipServletResponse.getFrom().getURI().toString();
				String toURI = sipServletResponse.getTo().getURI().toString();
				if(fromURI.contains("pending") || fromURI.contains("factory")
						|| toURI.contains("pending") || toURI.contains("factory")) {
					sendAck= false;
					if (logger.isDebugEnabled()) {
						logger.debug("testing pending messages so not sending ACK");
					}
				} 
				if(originalRequest.getHeader("Require") == null) {
					if(sendAck) {
						// we send the ACK directly only in non PRACK scenario
						SipServletRequest ackRequest = sipServletResponse.createAck();
						logger.info("Sending " +  ackRequest);
						ackRequest.send();
					}
					if(!originalRequest.isCommitted() && !fromURI.contains("forking") && !toURI.contains("forking")) {
						SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
						logger.info("Sending OK on 1st call leg" +  responseToOriginalRequest);			
						responseToOriginalRequest.setContentLength(sipServletResponse.getContentLength());
						//responseToOriginalRequest.setContent(sipServletResponse.getContent(), sipServletResponse.getContentType());
						responseToOriginalRequest.send();
					} else {
						try {				    	
					    	SipServletResponse responseToOriginalRequest = sipServletResponse.getRequest().getB2buaHelper().createResponseToOriginalRequest(originalSession, sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
					    	logger.info("Forking case Created response " + responseToOriginalRequest + " to original request " +  sipServletResponse.getRequest());
					    	responseToOriginalRequest.send();
					    }catch (IllegalStateException e) {
					    	logger.warn("200 OK already forwarded to the other side from a different leg so acking and bye-ing this session");
					    	sipServletResponse.createAck().send();
					    	sipServletResponse.getSession().createRequest("BYE").send();
						}
					}
				} else {
				    SipSession peerSession = sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
				    try {				    	
				    	SipServletResponse responseToOriginalRequest = sipServletResponse.getRequest().getB2buaHelper().createResponseToOriginalRequest(peerSession, sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
				    	logger.info("Created response " + responseToOriginalRequest + " to original request " +  sipServletResponse.getRequest());
				    	responseToOriginalRequest.send();
				    }catch (Exception e) {
				    	logger.error("exception happened while trying to forward the response to the other side", e);
				    	peerSession.createRequest("BYE").send();
				    	sipServletResponse.getSession().createRequest("BYE").send();
					}
				    //
				    if(sendAck) {
					    SipServletRequest ackRequest = sipServletResponse.createAck();
						logger.info("Sending " +  ackRequest);
						ackRequest.send();
				    }
				}
			}	
		}
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getReasonPhrase());		
						
		//create and sends the error response for the first call leg
		if(sipServletResponse.getStatus() == SipServletResponse.SC_UNAUTHORIZED || 
				sipServletResponse.getStatus() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
		
			// Avoid re-sending if the auth repeatedly fails.
			if(!"true".equals(getServletContext().getAttribute("FirstResponseRecieved")))
			{
				getServletContext().setAttribute("FirstResponseRecieved", "true");
				SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
						SIP_FACTORY);
				AuthInfo authInfo = sipFactory.createAuthInfo();				
				authInfo.addAuthInfo(sipServletResponse.getStatus(), "sip-servlets-realm", "user", "pass");
				SipServletRequest req2 = sipServletResponse.getRequest();
				B2buaHelper helper = req2.getB2buaHelper();
				SipServletRequest req1 = helper.getLinkedSipServletRequest(req2);				
				SipServletRequest challengeRequest = helper.createRequest(sipServletResponse.getSession(), req2,
				null);
				challengeRequest.addAuthHeader(sipServletResponse, authInfo);
				req1.getSession().setAttribute("originalRequest", challengeRequest);
				challengeRequest.send();
			}
		} else {
			if(sipServletResponse.getStatus() == SipServletResponse.SC_REQUEST_TIMEOUT) {
				if(sipServletResponse.getFrom().toString().contains("new-thread")) {					
					((TimerService)getServletContext().getAttribute(TIMER_SERVICE)).createTimer(sipServletResponse.getApplicationSession(), 35000, false, (Serializable)sipServletResponse.getSession());
				} else {
					sendSubsequentRequestTo408(sipServletResponse.getSession(), "BYE");
				}
				return;
			}
			if(sipServletResponse.getStatus() != SipServletResponse.SC_REQUEST_TERMINATED) {
				SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
				SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
				logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
				responseToOriginalRequest.send();
			}			
		}
	}
	
	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.toString());
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
		String fromURI = sipServletResponse.getFrom().getURI().toString();
		String toURI = sipServletResponse.getTo().getURI().toString();
		
		if(!originalRequest.isCommitted() && !fromURI.contains("forking") && !toURI.contains("forking")) {
			SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			if(logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
			}
			if(sipServletResponse.getHeader("Require") != null) {
				try {
					responseToOriginalRequest.sendReliably();
				} catch(IllegalStateException e) {
					logger.error("could send the response reliably " + responseToOriginalRequest, e);
					SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
							SIP_FACTORY);
					sendMessage(sipFactory.createApplicationSession(), sipFactory, "KO");
				}
			} else {
				responseToOriginalRequest.send();
			}
		} else {
			SipSession originalSession =   
			    sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
			checkForkedSession(originalSession, sipServletResponse);
			try {				    	
		    	SipServletResponse responseToOriginalRequest = sipServletResponse.getRequest().getB2buaHelper().createResponseToOriginalRequest(originalSession, sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
		    	logger.info("Forking case Created response " + responseToOriginalRequest + " to original request " +  sipServletResponse.getRequest());
		    	responseToOriginalRequest.send();
		    } catch (IllegalStateException e) {
		    	logger.error("problem while trying to create response to original request ", e);
			}
		}
	}	
	
	// SipErrorListener methods
	/**
	 * {@inheritDoc}
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");		
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
	}

	public void sessionCreated(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionExpired(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		// Issue http://code.google.com/p/mobicents/issues/detail?id=2246
		String fromName = ev.getApplicationSession().getAttribute("double-callback-test").toString();
		if(fromName.contains("true")) {
			boolean doubleCallback = ev.getApplicationSession().getAttribute("double") != null;
			if(doubleCallback) {
				try {
					new File("b2buaSessionDoubleCallbackTest").createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				ev.getApplicationSession().setAttribute("double","");
			}
			try {
				Thread.sleep(1000*5);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("ev" + ev.getApplicationSession(), new RuntimeException());
		}
		Integer nbTimesCalled = (Integer) ev.getApplicationSession().getAttribute(TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED);
		if(nbTimesCalled != null) {
			ev.getApplicationSession().setAttribute(TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED, Integer.valueOf(nbTimesCalled.intValue() + 1));
			logger.info("sipApplicationSessionReadyToBeInvalidated");
			if(nbTimesCalled.intValue() > 1) {
				sendMessage(ev.getApplicationSession(), (SipFactory) getServletContext().getAttribute(SIP_FACTORY), "sipApplicationSessionReadyToBeInvalidated called multiple times");
			}
		} else {
//			incomingSession.getB
			sendMessage(ev.getApplicationSession(), (SipFactory) getServletContext().getAttribute(SIP_FACTORY), "sipApplicationSessionReadyToBeInvalidated");
		}
	}
	
	/**
	 * @param sipApplicationSession
	 * @param sipFactory
	 */
	private void sendMessage(SipApplicationSession sipApplicationSession,
			SipFactory sipFactory, String content) {
		try {
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri=sipFactory.createSipURI("receiver", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
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

	public void timeout(ServletTimer timer) {
		SipSession sipSession = (SipSession)timer.getInfo();
		sendSubsequentRequestTo408(sipSession, (String)sipSession.getAttribute("method"));
	}
	
	public void sendSubsequentRequestTo408(SipSession sipSession, String method) {
		try {
			sipSession.createRequest(method).send();		
			SipServletRequest originalRequest = (SipServletRequest) sipSession.getAttribute("originalRequest");
			originalRequest.createResponse(200).send();
		} catch (IOException e) {
			logger.error("Error while creating subsequent BYE", e);
		} catch (IllegalStateException e) {
			if(method.equals(Request.BYE)) {
				logger.error("Error while creating subsequent BYE", e);
			} else {
				SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
				sendMessage(sipFactory.createApplicationSession(), sipFactory, "IllegalStateException");
			}
		}
	}
	
	void checkForkedSession(SipSession originalSession, SipServletResponse sipServletResponse) {
		SipSession responseSession = sipServletResponse.getSession();
		okReceived++;
		logger.info("Number of OK Received is " + okReceived);
		logger.info("Original Session " + originalSession);
		logger.info("Incoming Session " + incomingSession);
		logger.info("Response Session " + responseSession);
		logger.info("Outgoing Session " + outgoingSession);
		// checks for Issue http://code.google.com/p/mobicents/issues/detail?id=2365
		// making sure the forked sessions are not equals to the non forked sessions
		if(okReceived == 1) {							
			if(!originalSession.equals(incomingSession)) {
				throw new IllegalStateException("original Session is not equals to the incoming session");
			}
			if(!responseSession.equals(outgoingSession)) {
				throw new IllegalStateException("response's Session is not equals to the outgoing session");
			}
		}
		if(okReceived == 2) {
			if(originalSession.equals(incomingSession)) {
				throw new IllegalStateException("forked original Session is equals to the incoming session");
			}
			if(responseSession.equals(outgoingSession)) {
				throw new IllegalStateException("response's forked Session is equals to the outgoing session");
			}
			SipSession originalForkedSession = sipServletResponse.getRequest().getApplicationSession().getSipSession(originalSession.getId());
			if(!originalSession.equals(originalForkedSession)) {
				throw new IllegalStateException("forked original Session retrieved through the application session is not equals to the original session");
			}							
		}		
	}
	
}
