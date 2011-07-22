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

package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipServletRequestExt;

public class ShootistSipServletAuth 
		extends SipServlet 
		implements SipServletListener, SipSessionListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ShootistSipServletAuth.class);
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	/** Creates a new instance of ShootistSipServletAuth */
	public ShootistSipServletAuth() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the shootist has been started");
		super.init(servletConfig);
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
				String fromString = response.getFrom().getURI().toString();
				
				getServletContext().setAttribute("FirstResponseRecieved", "true");
				AuthInfo authInfo = sipFactory.createAuthInfo();
				authInfo.addAuthInfo(response.getStatus(), "sip-servlets-realm", "user", "pass");
				SipServletRequest challengeRequest = response.getSession().createRequest(
						response.getRequest().getMethod());
				boolean cacheCredentials = false;
				if(fromString.contains("cache-credentials")) {
					cacheCredentials = true;
				}
				logger.info("cache Credentials : " + cacheCredentials);	
				((SipServletRequestExt)challengeRequest).addAuthHeader(response, authInfo, cacheCredentials);
				challengeRequest.send();
				
				if(fromString.contains("cancelChallenge")) {
					if(fromString.contains("Before1xx")) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {				
							logger.error("unexpected exception", e);
						}
						challengeRequest.createCancel().send();
					} else {
						response.getSession().setAttribute("cancelChallenge", challengeRequest);
					}
				}
			}			
		}
		
		logger.info("Got response: " + response);	
		
	}
	
	@Override
	protected void doProvisionalResponse(SipServletResponse resp)
			throws ServletException, IOException {
		SipServletRequest servletRequest = (SipServletRequest) resp.getSession().getAttribute("cancelChallenge");
		if(servletRequest != null && resp.getStatus() > 100) {
			servletRequest.createCancel().send();
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		int status = sipServletResponse.getStatus();
		String fromString = sipServletResponse.getFrom().getURI().toString();
		if (status == SipServletResponse.SC_OK && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			SipServletRequest ackRequest = sipServletResponse.createAck();
			ackRequest.send();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {				
				logger.error("unexpected exception", e);
			}
			if(!fromString.contains("reinvite")) {
				SipServletRequest sipServletRequest = sipServletResponse.getSession().createRequest("BYE");
				sipServletRequest.send();
			}
		}
		if("REGISTER".equalsIgnoreCase(sipServletResponse.getMethod())) {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!sipServletResponse.getHeader("CSeq").contains((String)sipServletResponse.getApplicationSession().getAttribute("nbSubsequentReq"))) {
				getServletContext().setAttribute("FirstResponseRecieved", "false");
				SipServletRequest register = sipServletResponse.getSession().createRequest(sipServletResponse.getMethod());
				register.send();
			}
		}
	}
	
	@Override
	protected void doInfo(SipServletRequest req) throws ServletException,
			IOException {
		req.createResponse(200).send();
		SipServletRequest reInvite = req.getSession().createRequest("INVITE");
		reInvite.send();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {				
			logger.error("unexpected exception", e);
		}
		
		reInvite.createCancel().send();
	}

	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
		String method = ce.getServletContext().getInitParameter("METHOD");
		if(method == null) {
			method = "INVITE";
		}
		
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		String from = ce.getServletContext().getInitParameter("from");
		if(from == null) {
			from = "BigGuy";
		}
		
		String numberOfSubsequentRequests = ce.getServletContext().getInitParameter("nbSubsequentReq");
		if(numberOfSubsequentRequests == null) {
			numberOfSubsequentRequests = "10";
		}
		SipURI fromURI = sipFactory.createSipURI(from, "here.com");			
		SipURI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, method, fromURI, toURI);
		sipApplicationSession.setAttribute("nbSubsequentReq", numberOfSubsequentRequests);
		SipURI requestURI = sipFactory.createSipURI("LittleGuy", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
		sipServletRequest.setRequestURI(requestURI);
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}		
	}

	public void sessionCreated(SipSessionEvent se) {
		final SipSession sipSession = se.getSession();		
		Integer nbSessionCreated = (Integer) sipSession.getAttribute("nbSessionCreated");
		if(nbSessionCreated == null) {
			sipSession.setAttribute("nbSessionCreated", Integer.valueOf(1));
		} else {
			sipSession.setAttribute("nbSessionCreated", Integer.valueOf(nbSessionCreated.intValue() + 1));
		}
		logger.info("number of sip sessions created " + sipSession.getAttribute("nbSessionCreated") + " session " + sipSession);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		if(nbSessionCreated != null && nbSessionCreated > 1) {
			sendMessage(sipSession.getApplicationSession(), sipFactory, "" + nbSessionCreated, null);
		}
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
			SipURI sipUri = storedFactory.createSipURI("receiver", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
			if(transport != null) {
				if(transport.equalsIgnoreCase(ListeningPoint.TCP)) {
					sipUri = storedFactory.createSipURI("receiver", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5081");
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

	public void sessionDestroyed(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}
}