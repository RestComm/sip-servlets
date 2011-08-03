/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.UAMode;

import org.apache.log4j.Logger;

/**
 * This Sip Servlet shows how a B2BUA can be used to forward a request on another call leg.
 * Based on the from address if it is in its forwarding list it forwards the INVITE to
 * the corresponding hard coded location.  
 * @author Jean Deruelle
 *
 */
public class DistributableCallForwardingB2BUASipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DistributableCallForwardingB2BUASipServlet.class);
	B2buaHelper helper = null;
	private static final String RECEIVED = "Received";
	Map<String, String[]> forwardingUris = null;
	
	/** Creates a new instance of CallForwardingB2BUASipServlet */
	public DistributableCallForwardingB2BUASipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding B2BUA sip servlet has been started");
		super.init(servletConfig);
		forwardingUris = new HashMap<String, String[]>();
		forwardingUris.put("sip:receiver@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver@127.0.0.1:5080", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		
		forwardingUris.put("sip:receiver-fwd-ack@sip-servlets.com", 
				new String[]{"sip:forward-receiver-fwd-ack@sip-servlets.com", "sip:forward-receiver-fwd-ack@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver-fwd-ack@127.0.0.1", 
				new String[]{"sip:forward-receiver-fwd-ack@sip-servlets.com", "sip:forward-receiver-fwd-ack@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver-fwd-ack@127.0.0.1:5080", 
				new String[]{"sip:forward-receiver-fwd-ack@sip-servlets.com", "sip:forward-receiver-fwd-ack@127.0.0.1:5090"});
		
		forwardingUris.put("sip:receiver-linked@sip-servlets.com", 
				new String[]{"sip:forward-receiver-linked@sip-servlets.com", "sip:forward-receiver-linked@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver-linked@127.0.0.1", 
				new String[]{"sip:forward-receiver-linked@sip-servlets.com", "sip:forward-receiver-linked@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver-linked@127.0.0.1:5080", 
				new String[]{"sip:forward-receiver-linked@sip-servlets.com", "sip:forward-receiver-linked@127.0.0.1:5090"});
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {		
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
		if(request.getTo().getURI().toString().contains("fwd-ack")) {
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
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got INVITE: " + request.toString());
			logger.info(request.getFrom().getURI().toString());
			logger.info("OUTBOUND INTERFACES  " + getServletContext().getAttribute("javax.servlet.sip.outboundInterfaces"));
		}
		
		if(request.isInitial()) {
			String[] forwardingUri = forwardingUris.get(request.getTo().getURI().toString());
			if(forwardingUri != null && forwardingUri.length > 0) {
				helper = request.getB2buaHelper();						
				request.getSession().setAttribute("INVITE", RECEIVED);
				request.getApplicationSession().setAttribute("INVITE", RECEIVED);			
				
				SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
						SIP_FACTORY);
				
				Map<String, List<String>> headers=new HashMap<String, List<String>>();
				List<String> toHeaderSet = new ArrayList<String>();
				toHeaderSet.add(forwardingUri[0]);
				headers.put("To", toHeaderSet);
				
				SipServletRequest forkedRequest = helper.createRequest(request, true,
						headers);
				SipURI sipUri = (SipURI) sipFactory.createURI(forwardingUri[1]);		
				forkedRequest.setRequestURI(sipUri);						
				if(logger.isInfoEnabled()) {
					logger.info("forkedRequest = " + forkedRequest);
				}
				forkedRequest.getSession().setAttribute("originalRequest", request);
				forkedRequest.getSession().setAttribute("INVITE", RECEIVED);
				
				forkedRequest.send();
			} else {
				if(logger.isInfoEnabled()) {
					logger.info("INVITE has not been forwarded.");
				}
			}
		} else {
			// deals with reinvite requests
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession origSession = b2buaHelper.getLinkedSession(request.getSession());
			origSession.setAttribute("originalRequest", request);
			b2buaHelper.createRequest(origSession, request, null).send();
		}
	}	
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got BYE: " + request.toString());
		}
		//we forward the BYE
		B2buaHelper byeHelper = request.getB2buaHelper();
		SipSession linkedSipSession = byeHelper.getLinkedSession(request.getSession());
		String linkedSipSessionInviteAttribute  = (String) linkedSipSession.getAttribute("INVITE");
		String sipSessionInviteAttribute  = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute  = (String) request.getApplicationSession().getAttribute("INVITE");		
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Simple Servlet: attributes previously set in linked sip session INVITE : "+ linkedSipSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
		}
		
		//we send the OK directly to the first call leg if the attributes have been correctly replicated
		if(sipSessionInviteAttribute == null  || sipApplicationSessionInviteAttribute == null || linkedSipSessionInviteAttribute == null
				|| !RECEIVED.equalsIgnoreCase(sipSessionInviteAttribute)
				|| !RECEIVED.equalsIgnoreCase(linkedSipSessionInviteAttribute) 
				|| !RECEIVED.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
			sipServletResponse.send();
			return;
		}
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		
		SipSession session = request.getSession();		
		SipSession linkedSession = byeHelper.getLinkedSession(session);		
		SipServletRequest forkedRequest = linkedSession.createRequest("BYE");
		if(logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + forkedRequest);
		}
		forkedRequest.send();	
		helper = byeHelper;
	}	
	
	@Override
    protected void doUpdate(SipServletRequest request) throws ServletException,
            IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
        B2buaHelper helper = request.getB2buaHelper();
        SipSession peerSession = helper.getLinkedSession(request.getSession());
        SipServletRequest update = helper.createRequest(peerSession, request, null);
        update.send();
    }
	
	@Override
    protected void doInfo(SipServletRequest request) throws ServletException,
            IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
        B2buaHelper helper = request.getB2buaHelper();
        SipSession peerSession = helper.getLinkedSession(request.getSession());
        SipServletRequest info = helper.createRequest(peerSession, request, null);
        info.send();
    }
    
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got CANCEL: " + request.toString());		
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.toString());
		}
		
//		SipSession originalSession =   
//		    helper.getLinkedSession(sipServletResponse.getSession());		
		//if this is a response to an INVITE we ack it and forward the OK 
		if("INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			if(!sipServletResponse.getTo().getURI().toString().contains("fwd-ack")) {
				SipServletRequest ackRequest = sipServletResponse.createAck();
				if(logger.isInfoEnabled()) {
					logger.info("Sending " +  ackRequest);
				}
				ackRequest.send();
			}
			SipServletResponse responseToOriginalRequest = null;
			//create and sends OK for the first call leg	
			if(sipServletResponse.getTo().getURI().toString().contains("linked")) {
				B2buaHelper b2buaHelper = sipServletResponse.getRequest().getB2buaHelper();
				SipServletRequest originalRequest = b2buaHelper.getLinkedSipServletRequest(sipServletResponse.getRequest());			
				responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
				if(logger.isInfoEnabled()) {
					logger.info("Sending OK on the 1st call leg with linked " + responseToOriginalRequest.toString());
				}				
			} else {
				SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
				responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
				if(logger.isInfoEnabled()) {
					logger.info("Sending OK on 1st call leg" +  responseToOriginalRequest);
				}
			}
			responseToOriginalRequest.setContentLength(sipServletResponse.getContentLength());
			if(sipServletResponse.getContent() != null && sipServletResponse.getContentType() != null)
				responseToOriginalRequest.setContent(sipServletResponse.getContent(), sipServletResponse.getContentType());
			responseToOriginalRequest.send();
		} 
		if(sipServletResponse.getMethod().indexOf("UPDATE") != -1 || sipServletResponse.getMethod().indexOf("INFO") != -1) {
			B2buaHelper helper = sipServletResponse.getRequest().getB2buaHelper();
			SipServletRequest orgReq = helper.getLinkedSipServletRequest(sipServletResponse.getRequest());
			SipServletResponse res2 = orgReq.createResponse(sipServletResponse.getStatus());
			res2.send();
		}	
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getReasonPhrase());
		}
						
		//create and sends the error response for the first call leg
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
		if(logger.isInfoEnabled()) {
			logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
		}
		responseToOriginalRequest.send();		
	}
	
	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		SipServletResponse responseToOriginalRequest = null;
		if(sipServletResponse.getTo().getURI().toString().contains("linked")) {
			B2buaHelper b2buaHelper = sipServletResponse.getRequest().getB2buaHelper();
			SipServletRequest originalRequest = b2buaHelper.getLinkedSipServletRequest(sipServletResponse.getRequest());			
			responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			if(logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg with linked " + responseToOriginalRequest.toString());
			}
			
		} else {
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
			responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			if(logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
			}
		}
		responseToOriginalRequest.send();
	}
}
