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
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

/**
 * This Sip Servlet shows how a B2BUA can be used to forward a request on another call leg.
 * Based on the from address if it is in its forwarding list it forwards the INVITE to
 * the corresponding hard coded location.  
 * @author Jean Deruelle
 *
 */
public class CallForwardingB2BUASipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(CallForwardingB2BUASipServlet.class);	
	Map<String, String[]> forwardingUris = null;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding B2BUA sip servlet has been started");
		super.init(servletConfig);
		forwardingUris = new ConcurrentHashMap<String, String[]>();
		forwardingUris.put("sip:receiver@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {	
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got INVITE: " + request.toString());
			logger.info(request.getFrom().getURI().toString());
		}
		String[] forwardingUri = forwardingUris.get(request.getTo().getURI().toString());
		if(forwardingUri != null && forwardingUri.length > 0) {
			B2buaHelper helper = request.getB2buaHelper();						
			
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
			
			forkedRequest.send();
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("INVITE has not been forwarded.");
			}
		}
	}	
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got BYE: " + request.toString());
		}
		//we send the OK directly to the first call leg
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		
		//we forward the BYE
		SipSession session = request.getSession();
		B2buaHelper helper = request.getB2buaHelper();
		SipSession linkedSession = helper.getLinkedSession(session);		
		SipServletRequest forkedRequest = linkedSession.createRequest("BYE");
		if(logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + forkedRequest);
		}
		forkedRequest.send();	
		if(session != null && session.isValid()) {
			session.invalidate();
		}	
		return;
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
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {		
		if(logger.isInfoEnabled()) {
			logger.info("Got CANCEL: " + request.toString());
		}
		SipSession session = request.getSession();	
		B2buaHelper helper = request.getB2buaHelper();
		SipSession linkedSession = helper.getLinkedSession(session);
		SipServletRequest originalRequest = (SipServletRequest)linkedSession.getAttribute("originalRequest");
		SipServletRequest  cancelRequest = helper.getLinkedSipServletRequest(originalRequest).createCancel();
		if(logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + cancelRequest);
		}
		cancelRequest.send();
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.toString());
		}		
		if(sipServletResponse.getMethod().indexOf("BYE") != -1) {
			SipSession sipSession = sipServletResponse.getSession(false);
			if(sipSession != null && sipSession.isValid()) {
				sipSession.invalidate();
			}
			SipApplicationSession sipApplicationSession = sipServletResponse.getApplicationSession(false);
			if(sipApplicationSession != null && sipApplicationSession.isValid()) {
				sipApplicationSession.invalidate();
			}	
			return;
		} 
		
		if(sipServletResponse.getMethod().indexOf("INVITE") != -1) {
			//	if this is a response to an INVITE we ack it and forward the OK 
			SipServletRequest ackRequest = sipServletResponse.createAck();
			if(logger.isInfoEnabled()) {
				logger.info("Sending " +  ackRequest);
			}
			ackRequest.send();
			//create and sends OK for the first call leg							
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			if(logger.isInfoEnabled()) {
				logger.info("Sending OK on 1st call leg" +  responseToOriginalRequest);
			}
			responseToOriginalRequest.setContentLength(sipServletResponse.getContentLength());
			if(sipServletResponse.getContent() != null && sipServletResponse.getContentType() != null)
				responseToOriginalRequest.setContent(sipServletResponse.getContent(), sipServletResponse.getContentType());
			responseToOriginalRequest.send();
		}		
		if(sipServletResponse.getMethod().indexOf("UPDATE") != -1) {
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
		// we don't forward the timeout nor the Request Terminated due to CANCEL
		if(sipServletResponse.getStatus() != 408 && sipServletResponse.getStatus() != 487) {
			//create and sends the error response for the first call leg
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
			if(logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
			}
			responseToOriginalRequest.send();		
		}
	}
	
	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
		SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
		if(logger.isInfoEnabled()) {
			logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
		}
		responseToOriginalRequest.send();
	}
}
