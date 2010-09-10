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
package org.mobicents.servlet.sip.testsuite.b2bua;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletParseException;
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
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.URI;
import javax.servlet.sip.SipSession.State;
import javax.sip.header.ContactHeader;

import org.apache.log4j.Logger;


public class CallForwardingB2BUASipServlet extends SipServlet implements SipErrorListener, SipApplicationSessionListener {
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
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:factory-sender@sip-servlets.com", 
				new String[]{"sip:factory-receiver@sip-servlets.com", "sip:factory-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:forward-pending-sender@sip-servlets.com", 
				new String[]{"sip:forward-pending-receiver@sip-servlets.com", "sip:forward-pending-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:blocked-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:forward-sender@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:blocked-sender@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:forward-tcp-sender@sip-servlets.com", 
			new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090;transport=tcp"});
		forwardingUris.put("sip:forward-udp-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5080"});
		forwardingUris.put("sip:forward-udp-sender-tcp-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5080"});
		forwardingUris.put("sip:forward-sender@127.0.0.1:5090", 
				new String[]{"sip:forward-receiver@127.0.0.1:5090", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:composition@127.0.0.1:5090", 
				new String[]{"sip:forward-composition@127.0.0.1:5070", "sip:forward-composition@127.0.0.1:5070"});
		forwardingUris.put("sip:composition@sip-servlets.com", 
				new String[]{"sip:forward-composition@127.0.0.1:5070", "sip:forward-composition@127.0.0.1:5070"});
		forwardingUris.put("sip:sender@sip-servlets.com", 
				new String[]{"sip:fromB2BUA@sip-servlets.com", "sip:fromB2BUA@127.0.0.1:5090"});
		forwardingUris.put("sip:samesipsession@sip-servlets.com", 
				new String[]{"sip:forward-samesipsession@127.0.0.1:5070", "sip:forward-samesipsession@127.0.0.1:5070"});
		forwardingUris.put("sip:cancel-samesipsession@sip-servlets.com", 
				new String[]{"sip:cancel-forward-samesipsession@127.0.0.1:5070", "sip:cancel-forward-samesipsession@127.0.0.1:5070"});
		forwardingUris.put("sip:error-samesipsession@sip-servlets.com", 
				new String[]{"sip:error-forward-samesipsession@127.0.0.1:5070", "sip:error-forward-samesipsession@127.0.0.1:5070"});
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding B2BUA sip servlet has been started");
		super.init(servletConfig);		
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got : " + request.toString());
		SipSession session = request.getSession();
		if(request.getTo().toString().contains("b2bua@sip-servlet")) {
			session.createRequest("MESSAGE").send();
		}
		if(request.getFrom().getURI().toString().contains("pending") || 
				request.getFrom().getURI().toString().contains("factory") ||
				request.getApplicationSession().getAttribute(TEST_USE_LINKED_REQUEST) != null) {
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
		// Issue 1484 : http://code.google.com/p/mobicents/issues/detail?id=1484
		// check the state of the session it shouldn't be Terminated
		request.getSession().getAttribute("Foo");
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
				if(!contactAddress.toString().equals("<sip:127.0.0.1:5056>;+sip.instance=\"<urn:uuid:some-xxxx>\"")) {
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
				if(((SipURI)request.getTo().getURI()).getUser().contains("factory")) {
					forwardInviteUsingSipFactory(request, forwardingUri);
				} else {
					forwardInviteUsingB2BUAHelper(request, forwardingUri);
				}
			} else {
				logger.info("INVITE has not been forwarded.");
			}
		} else {
			// deals with reinvite requests
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession origSession = b2buaHelper.getLinkedSession(request.getSession());
			origSession.setAttribute("originalRequest", request);
			b2buaHelper.createRequest(origSession, request, null).send();
		}
	}	
	
	private void forwardInviteUsingB2BUAHelper(SipServletRequest request,
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
				contactHeaderList.add("\"callforwardingB2BUA\" <sip:test@127.0.0.1:5070;q=0.1;lr;transport=udp;test>;test");
				headers.put("Contact", contactHeaderList);
			} else {
				List<String> contactHeaderList = new ArrayList<String>();
				contactHeaderList.add("\"callforwardingB2BUA\" <sip:test@127.0.0.1:5070;q=0.1;lr;transport=tcp;test>;test");
				headers.put("Contact", contactHeaderList);
			}
		}
		
		List<String> extensionsHeaderList = new ArrayList<String>();
		extensionsHeaderList.add("extension-header-value1");
		extensionsHeaderList.add("extension-header-value2");
		headers.put("extension-header", extensionsHeaderList);
		
		SipServletRequest forkedRequest = helper.createRequest(request, true,
				headers);
		SipURI sipUri = (SipURI) sipFactory.createURI(forwardingUri[1]);
		forkedRequest.setRequestURI(sipUri);						
		
		logger.info("forkedRequest = " + forkedRequest);
		forkedRequest.getSession().setAttribute("originalRequest", request);
		
		forkedRequest.send();
	}

	private void forwardInviteUsingSipFactory(SipServletRequest origReq, String[] forwardingUri) throws ServletParseException, UnsupportedEncodingException, IOException {
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
		B2buaHelper b2buaHelper = origReq.getB2buaHelper();
        URI from = origReq.getTo().getURI();
        URI to= sipFactory.createURI(forwardingUri[1]);
        SipApplicationSession appSession = origReq.getApplicationSession();
        SipServletRequest newRequest = sipFactory.createRequest(appSession, "INVITE", from, to);
        newRequest.setContent(origReq.getContent(), origReq.getContentType());
        newRequest.setHeader("Allow", "INVITE, ACK, CANCEL, BYE, REFER, UPDATE, INFO");
        b2buaHelper.linkSipSessions(origReq.getSession(), newRequest.getSession()); //Linking sessions!
        origReq.getSession().setAttribute("originalRequest", origReq);
        newRequest.send();        
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got BYE: " + request.toString());		
		if(request.getSession().getAttribute(ACT_AS_UAS) == null) {
			//we forward the BYE
			SipSession session = request.getSession();		
			SipSession linkedSession = request.getB2buaHelper().getLinkedSession(session);
			Map<String, List<String>> headers=new HashMap<String, List<String>>();
			
			List<String> userAgentHeaderList = new ArrayList<String>();
			userAgentHeaderList.add("CallForwardingB2BUASipServlet");
			headers.put("User-Agent", userAgentHeaderList);
			
			List<String> contactHeaderList = new ArrayList<String>();
			contactHeaderList.add("\"callforwardingB2BUA\" <sip:127.0.0.1:5070;transport=tcp>");
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
			SipServletRequest originalRequest = (SipServletRequest)linkedSession.getAttribute("originalRequest");
			if(originalRequest != null) {
				SipServletRequest  cancelRequest = b2buaHelper.getLinkedSipServletRequest(originalRequest).createCancel();				
				logger.info("forkedRequest = " + cancelRequest);			
				cancelRequest.send();
			} else {
				logger.info("no invite to cancel, it hasn't been forwarded");
			}
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
		
		B2buaHelper b2buaHelper = sipServletResponse.getRequest().getB2buaHelper();
		
		SipSession originalSession =   
		    sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
		
		if(sipServletResponse.getApplicationSession().getAttribute(TEST_USE_LINKED_REQUEST) != null) {
			logger.info("using linkedRequest to send the response: ");
			SipServletRequest otherReq = b2buaHelper.getLinkedSipServletRequest(sipServletResponse.getRequest());
			SipServletResponse other = otherReq.createResponse(sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
			other.send();
		} else {
		
			String cSeqValue = sipServletResponse.getHeader("CSeq");
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
			//if this is a response to an INVITE we ack it and forward the OK 
			if(originalSession!= null && cSeqValue.indexOf("INVITE") != -1) {			
				//create and sends OK for the first call leg							
				SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
				boolean sendAck = true;
				if(sipServletResponse.getFrom().getURI().toString().contains("pending") || sipServletResponse.getFrom().getURI().toString().contains("factory")) {
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
					if(!originalRequest.isCommitted()) {
						SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
						logger.info("Sending OK on 1st call leg" +  responseToOriginalRequest);			
						responseToOriginalRequest.setContentLength(sipServletResponse.getContentLength());
						//responseToOriginalRequest.setContent(sipServletResponse.getContent(), sipServletResponse.getContentType());
						responseToOriginalRequest.send();
					}
				} else {
				    SipSession peerSession = sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
				    try {
				    	SipServletResponse responseToOriginalRequest = sipServletResponse.getRequest().getB2buaHelper().createResponseToOriginalRequest(peerSession, sipServletResponse.getStatus(), sipServletResponse.getReasonPhrase());
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
//				SipServletRequest req1 = helper.getLinkedSipServletRequest(req2);
				SipServletRequest challengeRequest = helper.createRequest(sipServletResponse.getSession(), req2,
				null);
				challengeRequest.addAuthHeader(sipServletResponse, authInfo);
				challengeRequest.send();
			}
		} else {
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
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
		if(!originalRequest.isCommitted()) {
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
		Integer nbTimesCalled = (Integer) ev.getApplicationSession().getAttribute(TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED);
		if(nbTimesCalled != null) {
			ev.getApplicationSession().setAttribute(TEST_SIP_APP_SESSION_READY_TO_BE_INVALIDATED, Integer.valueOf(nbTimesCalled.intValue() + 1));
			logger.info("sipApplicationSessionReadyToBeInvalidated");
			if(nbTimesCalled.intValue() > 1) {
				sendMessage(ev.getApplicationSession(), (SipFactory) getServletContext().getAttribute(SIP_FACTORY), "sipApplicationSessionReadyToBeInvalidated called multiple times");
			}
		} else {
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
			SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5080");
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

}