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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;


public class CallForwardingB2BUASipServlet extends SipServlet implements SipErrorListener {
	private static final long serialVersionUID = 1L;
	private static final String ACT_AS_UAS = "actAsUas";
	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUASipServlet.class);
	private static Map<String, String[]> forwardingUris = null;
	
	static {
		forwardingUris = new HashMap<String, String[]>();
		forwardingUris.put("sip:forward-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:blocked-sender@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:forward-sender@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:blocked-sender@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:forward-tcp-sender@sip-servlets.com", 
			new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090;transport=tcp"});
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
		
		logger.info("Got INVITE: " + request.toString());
		logger.info(request.getFrom().getURI().toString());
		String[] forwardingUri = forwardingUris.get(request.getFrom().getURI().toString());
		if(((SipURI)request.getTo().getURI()).getUser().contains("forward-samesipsession")) {
			request.getSession().setAttribute(ACT_AS_UAS, Boolean.TRUE);
			if(request.getSession().getAttribute("originalRequest") == null) {
				if(((SipURI)request.getTo().getURI()).getUser().contains("cancel")) {
					request.createResponse(SipServletResponse.SC_RINGING).send();
				} else if(((SipURI)request.getTo().getURI()).getUser().contains("error")) {
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "expected error").send();
				} else {
					request.createResponse(SipServletResponse.SC_OK).send();
				}
			} else {
				request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "same sip session").send();
			}
			return ;
		}
		
		
		if(forwardingUri != null && forwardingUri.length > 0) {
			B2buaHelper helper = request.getB2buaHelper();						
			
			helper.createResponseToOriginalRequest(request.getSession(), SipServletResponse.SC_TRYING, "").send();
			
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
					SIP_FACTORY);
			
			Map<String, List<String>> headers=new HashMap<String, List<String>>();
			List<String> toHeaderList = new ArrayList<String>();
			toHeaderList.add(forwardingUri[0]);
			headers.put("To", toHeaderList);
			
			SipServletRequest forkedRequest = helper.createRequest(request, true,
					headers);
			SipURI sipUri = (SipURI) sipFactory.createURI(forwardingUri[1]);
			forkedRequest.setRequestURI(sipUri);						
			
			logger.info("forkedRequest = " + forkedRequest);
			forkedRequest.getSession().setAttribute("originalRequest", request);
			
			forkedRequest.send();
		} else {
			logger.info("INVITE has not been forwarded.");
		}
	}	
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got BYE: " + request.toString());
		//we send the OK directly to the first call leg
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		if(request.getSession().getAttribute(ACT_AS_UAS) == null) {
			//we forward the BYE
			SipSession session = request.getSession();		
			SipSession linkedSession = request.getB2buaHelper().getLinkedSession(session);		
			SipServletRequest forkedRequest = linkedSession.createRequest("BYE");			
			logger.info("forkedRequest = " + forkedRequest);			
			forkedRequest.send();
		}
	}	
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		if(!((SipURI)request.getTo().getURI()).getUser().equals("cancel-forward-samesipsession")) {
			logger.info("Got CANCEL: " + request.toString());
			SipSession session = request.getSession();
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession linkedSession = b2buaHelper.getLinkedSession(session);
			SipServletRequest originalRequest = (SipServletRequest)linkedSession.getAttribute("originalRequest");
			SipServletRequest  cancelRequest = b2buaHelper.getLinkedSipServletRequest(originalRequest).createCancel();				
			logger.info("forkedRequest = " + cancelRequest);			
			cancelRequest.send();
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.toString());
		
		SipSession originalSession =   
		    sipServletResponse.getRequest().getB2buaHelper().getLinkedSession(sipServletResponse.getSession());
		String cSeqValue = sipServletResponse.getHeader("CSeq");
		//if this is a response to an INVITE we ack it and forward the OK 
		if(originalSession!= null && cSeqValue.indexOf("INVITE") != -1) {
			SipServletRequest ackRequest = sipServletResponse.createAck();
			logger.info("Sending " +  ackRequest);
			ackRequest.send();
			//create and sends OK for the first call leg							
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			logger.info("Sending OK on 1st call leg" +  responseToOriginalRequest);
			responseToOriginalRequest.setContentLength(sipServletResponse.getContentLength());
			//responseToOriginalRequest.setContent(sipServletResponse.getContent(), sipServletResponse.getContentType());
			responseToOriginalRequest.send();
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
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
			responseToOriginalRequest.send();
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

}