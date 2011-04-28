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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;

import org.apache.log4j.Logger;

/**
 * This Sip Servlet shows how a B2BUA can be used to forward a request on another call leg.
 * Based on the from address if it is in its forwarding list it forwards the INVITE to
 * the corresponding hard coded location.  
 * @author Jean Deruelle
 *
 */
public class DistributableCallForwardingB2BUASipServlet extends SipServlet implements TimerListener, SipApplicationSessionListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DistributableCallForwardingB2BUASipServlet.class);
	
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
		forwardingUris.put("sip:receiver-tcp@sip-servlets.com", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver@127.0.0.1", 
				new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"});
		forwardingUris.put("sip:receiver@127.0.0.1:5080", 
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
		if(!request.isInitial()) {
			// REINVITE within the same session
			SipServletResponse resp = (SipServletResponse) request.getApplicationSession().getAttribute("resp");
			resp.getSession().getAttribute("justTestForNPE");
			resp.getApplicationSession().getAttribute("testForNPE");
			Composite composite = (Composite) request.getSession().getAttribute("timerRequest");
			Composite composite2 = (Composite) request.getSession().getAttribute("timerRequest2");
			if(composite2 == null) logger.info("timerRequest2=null, this is normal because it is from unmanaged timer.");
			SipServletRequest testMessageMethodRequest = composite.request;
			testMessageMethodRequest.getSession().createRequest("MESSAGE");
			
			
			SipSession linkedSipSession = (SipSession) request.getSession().getAttribute("linkedSession");
			SipServletRequest secondLegReinvite = (SipServletRequest) linkedSipSession.createRequest("INVITE");
			secondLegReinvite.getSession().getAttribute("fff");
			secondLegReinvite.send();
			SipServletRequest originalRequestLoadedFromSecondLeg = (SipServletRequest) linkedSipSession.getAttribute("originalRequest");
			request.getSession().getAttribute("INVITEREQUEST");
			originalRequestLoadedFromSecondLeg.getSession().getAttribute("fff");
			
			/// Test sessions are not null
			SipServletRequest forkedRequestLoadedFromFirstLeg = (SipServletRequest) request.getSession().getAttribute("forkedRequest");
			forkedRequestLoadedFromFirstLeg.getSession().getAttribute("fffff");
			SipServletRequest forkedRequestLoadedFromSecondLeg = (SipServletRequest) linkedSipSession.getAttribute("forkedRequest");
			forkedRequestLoadedFromSecondLeg.getSession().getAttribute("fffff");
			SipServletRequest repeatedLoadRequest = (SipServletRequest) request.getSession().getAttribute("forkedRequest");
			repeatedLoadRequest.getSession().getAttribute("fffff");
			logger.info("subsequent request");
			linkedSipSession.setAttribute("originalRequest", request);
			
			//forkedRequest.getSession().setAttribute("INVITE", RECEIVED);
			return;
		}

		TimerService timerService = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
		timerService.createTimer(request.getApplicationSession(), 400, false, (Serializable) request);
		
		String[] forwardingUri = forwardingUris.get(request.getTo().getURI().toString());
		if(forwardingUri == null) forwardingUri = new String[]{"sip:forward-receiver@sip-servlets.com", "sip:forward-receiver@127.0.0.1:5090"};
		if(forwardingUri != null && forwardingUri.length > 0) {					
			request.getSession().setAttribute("INVITE", RECEIVED);
			request.getSession().setAttribute("INVITEREQUEST", request);
			request.getApplicationSession().setAttribute("INVITE", RECEIVED);			
			
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
					SIP_FACTORY);
			
			Map<String, List<String>> headers=new HashMap<String, List<String>>();
			List<String> toHeaderSet = new ArrayList<String>();
			toHeaderSet.add(forwardingUri[0]);
			headers.put("To", toHeaderSet);
			
			SipServletRequest forkedRequest = sipFactory.createRequest (request.getApplicationSession(), 
                    "INVITE",
                    request.getFrom().getURI().toString(),
                    forwardingUri[0]);
			forkedRequest.getSession().setAttribute("linkedSession", request.getSession());

			request.getApplicationSession().setAttribute("S", forkedRequest.getSession());
			request.getSession().setAttribute("linkedSession", forkedRequest.getSession());
			
			logger.info("One session" + request.getSession() + " vs " + forkedRequest.getSession());
			SipURI sipUri = (SipURI) sipFactory.createURI(forwardingUri[1]);	
			if(request.getRequestURI().toString().contains("tcp")) sipUri.setTransportParam("tcp");
			forkedRequest.setRequestURI(sipUri);						
			if(logger.isInfoEnabled()) {
				logger.info("forkedRequest = " + forkedRequest);
			}
			forkedRequest.getSession().setAttribute("originalRequest", request);
			forkedRequest.getSession().setAttribute("forkedRequest", forkedRequest);
			request.getSession().setAttribute("forkedRequest", forkedRequest);
			forkedRequest.getSession().setAttribute("INVITE", RECEIVED);
			forkedRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, request);
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
		//we forward the BYE
		SipSession linkedSipSession = (SipSession) request.getSession().getAttribute("linkedSession");
		
		String linkedSipSessionInviteAttribute  = (String) linkedSipSession.getAttribute("INVITE");
		SipServletRequest r2 = (SipServletRequest) request.getSession().getAttribute("INVITEREQUEST");
		SipServletRequest r = (SipServletRequest) linkedSipSession.getAttribute("originalRequest");
		logger.info("Loaded r=" + r);
		logger.info("R session = " + r.getSession());
		logger.info("Loaded r2=" + r2);
		logger.info("R2 session = " + r2.getSession());
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
		SipSession linkedSession =(SipSession) linkedSipSession.getAttribute("linkedSession");	
		logger.info("Two session " + linkedSipSession + " vs " + linkedSession);
		SipServletRequest forkedRequest = linkedSipSession.createRequest("BYE");
		if(logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + forkedRequest);
		}
		forkedRequest.send();	
	}	
	
	@Override
    protected void doUpdate(SipServletRequest request) throws ServletException,
            IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got UPDATE: " + request.toString());
		}
        
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
			sipServletResponse.getApplicationSession().setAttribute("resp", sipServletResponse);
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
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
		SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
		if(logger.isInfoEnabled()) {
			logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
		}
		responseToOriginalRequest.send();
	}

	public void timeout(ServletTimer arg0) {
		final SipServletRequest request = (SipServletRequest) arg0.getInfo();
		request.getSession().setAttribute("timerRequest", new Composite(request, request.getSession()));
		
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				request.getSession().setAttribute("timerRequest2", new Composite(request, request.getSession()));
				logger.info("READY FOR FAILOVER!");
			}
		}, 1000);
	}
	
	// Test with composite
	public static class Composite implements Serializable
	{
		public Composite(SipServletRequest r, SipSession s) {
			this.request = r;
			this.session = s;
		}
		public SipServletRequest request;
		public SipSession session;
		String someString;
	}

	public void sessionCreated(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("CB2BUA CREATED " + arg0.getApplicationSession());
		}
	}

	public void sessionDestroyed(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("CB2BUA DESTROYED " + arg0.getApplicationSession());
			if(!new File("cb2buadestryed.flag").exists()) {
				try {
					new File("cb2buadestryed.flag").createNewFile();
				} catch (IOException e) {
					logger.error("Error flagging", e);
				}
			}
		}
	}

	public void sessionExpired(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("CB2BUA EXPIRED " + arg0.getApplicationSession());
		}
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("CB2BUA READY " + arg0.getApplicationSession());
		}
	}
}