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

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

public class DistributableShootistSipServlet 
		extends SipServlet 
		implements SipServletListener, TimerListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DistributableShootistSipServlet.class);
		
	private static final String SENT = "Sent";
	@Resource TimerService timerService;
	
	/** Creates a new instance of ShootistSipServlet */
	public DistributableShootistSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the shootist has been started");
		super.init(servletConfig);
	}		
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			SipServletRequest ackRequest = sipServletResponse.createAck();
			ackRequest.send();			
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		String sipSessionInviteAttribute  = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute  = (String) request.getApplicationSession().getAttribute("INVITE");
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Shootist Servlet: attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Shootist Servlet: attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
		}
		if(sipSessionInviteAttribute == null  || sipApplicationSessionInviteAttribute == null 
				|| !SENT.equalsIgnoreCase(sipSessionInviteAttribute) 
				|| !SENT.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			throw new IllegalArgumentException("State not replicated...");
		}
		request.createResponse(SipServletResponse.SC_OK).send();
	}
	
	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		String sendOnInit = ce.getServletContext().getInitParameter("send-on-init");
		String method = ce.getServletContext().getInitParameter("method");
		logger.info("Send on Init : "+ sendOnInit);
		if(sendOnInit == null || "true".equals(sendOnInit)) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			if(method.equalsIgnoreCase("INVITE")) {								
				sendMessage(sipFactory.createApplicationSession(), method);				
			} else {
				timerService.createTimer(sipApplicationSession, 10000, 20000, true, true, method);
			}
		}
	}

	public void timeout(ServletTimer servletTimer) {
		String method = (String) servletTimer.getInfo();
		if(servletTimer.getApplicationSession().getAttribute(method) == null) {
			sendMessage(servletTimer.getApplicationSession(), method);
		} else {
			if(servletTimer.getApplicationSession().getSessions().hasNext()) {
				SipSession sipSession =(SipSession) servletTimer.getApplicationSession().getSessions().next();
				try {
					sipSession.createRequest(method).send();
				} catch (IOException e) {
					logger.error(e);
				}
			} else {
				logger.error("Coldn't create a " + method + " request. No sip session available in sip application session " + servletTimer.getApplicationSession().getId());
			}
		}
	}

	private void sendMessage(SipApplicationSession sipApplicationSession, String method) {
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		URI fromURI = sipFactory.createSipURI("BigGuy", "here.com");
		URI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, method, fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("LittleGuy", "127.0.0.1:5090");
		sipServletRequest.setRequestURI(requestURI);
		
		sipServletRequest.getSession().setAttribute(method, SENT);
		sipServletRequest.getApplicationSession().setAttribute(method, SENT);
		
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}	
	}
	
	
}