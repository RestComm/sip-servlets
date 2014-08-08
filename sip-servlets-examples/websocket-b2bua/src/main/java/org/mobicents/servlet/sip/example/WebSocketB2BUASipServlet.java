/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;

import org.apache.log4j.Logger;

/**
 * This example shows a simple websocket app
 * 
 * @author jean.deruelle@telestax.com
 * @author Vladimir Ralev
 *
 */
public class WebSocketB2BUASipServlet extends SipServlet implements TimerListener {
	private static Logger logger = Logger.getLogger(WebSocketB2BUASipServlet.class);
	private static final long serialVersionUID = 1L;
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	@Resource
	SipFactory sipFactory;
	
	HashMap<SipSession, SipSession> sessions= new HashMap<SipSession, SipSession>();
	
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
		request.getSession().setAttribute("lastRequest", request);
		if(logger.isInfoEnabled()) {
			logger.info("Simple Servlet: Got request:\n"
					+ request.getMethod());
		}
		
		SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(),
				"INVITE", request.getFrom().getURI(), request.getTo().getURI());
		String user = ((SipURI) request.getTo().getURI()).getUser();
		Address calleeAddress = registeredUsersToIp.get(user);
		if(calleeAddress == null) {
			request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
			return;
		}
		outRequest.setRequestURI(calleeAddress.getURI());
		if(request.getContent() != null) {
			outRequest.setContent(request.getContent(), request.getContentType());
		}
		outRequest.send();
		sessions.put(request.getSession(), outRequest.getSession());
		sessions.put(outRequest.getSession(), request.getSession());
	}
	
	protected void doAck(SipServletRequest request) throws ServletException,
	IOException {
		SipServletResponse response = (SipServletResponse) sessions.get(request.getSession()).getAttribute("lastResponse");
		response.createAck().send();
		SipApplicationSession sipApplicationSession = request.getApplicationSession();
		// Defaulting the sip application session to 1h
		sipApplicationSession.setExpires(60);
	}

	HashMap<String, Address> registeredUsersToIp = new HashMap<String, Address>();
	
	protected void doRegister(SipServletRequest request) throws ServletException,
	IOException {

		Address addr = request.getAddressHeader("Contact");		
		String user = ((SipURI) request.getFrom().getURI()).getUser();
		registeredUsersToIp.put(user, addr);
		if(logger.isInfoEnabled()) {
			logger.info("Address registered " + addr);
		}
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		request.getSession().setAttribute("lastRequest", request);
		if(logger.isInfoEnabled()) {
			logger.info("Got BYE request:\n" + request);
		}
		sessions.get(request.getSession()).createRequest("BYE").send();
	}
	
	@Override
	protected void doMessage(SipServletRequest request) throws ServletException,
			IOException {
		request.getSession().setAttribute("lastRequest", request);
		if(logger.isInfoEnabled()) {
			logger.info("Got MESSAGE request:\n" + request);
		}
		SipSession sipSession = sessions.get(request.getSession());
		SipServletRequest message = null;
		if(sipSession == null) {
			SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(),
					"MESSAGE", request.getFrom().getURI(), request.getTo().getURI());
			String user = ((SipURI) request.getTo().getURI()).getUser();
			Address calleeAddress = registeredUsersToIp.get(user);
			if(calleeAddress == null) {
				request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
				return;
			}
			outRequest.setRequestURI(calleeAddress.getURI());
			message = outRequest;
			sessions.put(request.getSession(), outRequest.getSession());
			sessions.put(outRequest.getSession(), request.getSession());
		} else {
			 message = sipSession.createRequest("MESSAGE");
		}
		if(request.getContent() != null) {
			String contentType = request.getContentType();
			if(contentType == null || contentType.isEmpty()) {
				contentType = CONTENT_TYPE;
			}
			message.setContent(request.getContent(), contentType);
		}
		if(logger.isInfoEnabled()) {
			logger.info("Seding MESSAGE request:\n" + message);
		}
		message.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {
		if(logger.isInfoEnabled()) {
			logger.info("SimpleProxyServlet: Got response:\n" + response);
		}
		response.getSession().setAttribute("lastResponse", response);
		SipServletRequest request = (SipServletRequest) sessions.get(response.getSession()).getAttribute("lastRequest");
		SipServletResponse resp = request.createResponse(response.getStatus());
		if(response.getContent() != null) {
			resp.setContent(response.getContent(), response.getContentType());
		}
		resp.send();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
	 */
	public void timeout(ServletTimer servletTimer) {
		SipSession sipSession = servletTimer.getApplicationSession().getSipSession((String)servletTimer.getInfo());
		if(!State.TERMINATED.equals(sipSession.getState())) {
			try {
				sipSession.createRequest("BYE").send();
			} catch (IOException e) {
				logger.error("An unexpected exception occured while sending the BYE", e);
			}				
		}
	}
}