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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ProxyExt;

/**
 * This example shows Proxying on 2 different location.
 * Base on the request URI it forwards it to 2 different hard coded locations if
 * it is in its forwarding list 
 * @author Jean Deruelle
 *
 */
public class DistributableLocationServiceSipServlet extends SipServlet implements SipApplicationSessionListener, TimerListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DistributableLocationServiceSipServlet.class);
	private static String TEST_TERMINATION = "test_termination";
	private static final String CONTACT_HEADER = "Contact";
	private static final String RECEIVED = "Received";
	Map<String, List<URI>> registeredUsers = null;	
	
	/** Creates a new instance of SpeedDialSipServlet */
	public DistributableLocationServiceSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the distributable location service sip servlet has been started");
		super.init(servletConfig);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		registeredUsers = new HashMap<String, List<URI>>();
		List<URI> uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:5090"));
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:6090"));
		registeredUsers.put("sip:receiver@sip-servlets.com", uriList);
		List<URI> perfUriList  = new ArrayList<URI>();
		perfUriList.add(sipFactory.createURI("sip:perf-receiver@127.0.0.1:5090"));		
		registeredUsers.put("sip:perf-receiver@sip-servlets.com", perfUriList);
		ArrayList<URI> failOverUriList  = new ArrayList<URI>();
		failOverUriList.add(sipFactory.createURI("sip:receiver-failover@127.0.0.1:5090"));
		registeredUsers.put("sip:receiver-failover@sip-servlets.com", failOverUriList);
		registeredUsers.put("sip:receiver-failover@127.0.0.1:5090", failOverUriList);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got request:\n" + request.toString());
		}
		
		if(request.isInitial()) {
			List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
			if(contactAddresses != null && contactAddresses.size() > 0) {
				request.getSession().setAttribute("INVITE", RECEIVED);
				request.getApplicationSession().setAttribute("INVITE", RECEIVED);
				Proxy proxy = request.getProxy();
				proxy.setRecordRoute(true);
				proxy.setParallel(true);
				proxy.setSupervised(true);
				SipURI fromURI = ((SipURI)request.getFrom().getURI());	
				if(fromURI.getUser().contains(TEST_TERMINATION)) {		
					((ProxyExt)proxy).storeTerminationInformation(true);
					logger.info("testing termination");
				}
				proxy.proxyTo(contactAddresses);		
			} else {
				if(logger.isInfoEnabled()) {
					logger.info(request.getRequestURI().toString() + " is not currently registered");
				}
				SipServletResponse sipServletResponse = 
					request.createResponse(SipServletResponse.SC_MOVED_PERMANENTLY, "Moved Permanently");
				sipServletResponse.send();
			}
		}
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Received register request: " + req.getTo());
		}
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		HashMap<String, String> users = (HashMap<String, String>) getServletContext().getAttribute("registeredUsersMap");
		if(users == null) users = new HashMap<String, String>();
		getServletContext().setAttribute("registeredUsersMap", users);
		
		Address address = req.getAddressHeader(CONTACT_HEADER);
		String fromURI = req.getFrom().getURI().toString();
		
		int expires = address.getExpires();
		if(expires < 0) {
			expires = req.getExpires();
		}
		if(expires == 0) {
			users.remove(fromURI);
			if(logger.isInfoEnabled()) {
				logger.info("User " + fromURI + " unregistered");
			}
		} else {
			resp.setAddressHeader(CONTACT_HEADER, address);
			users.put(fromURI, address.getURI().toString());
			if(logger.isInfoEnabled()) {
				logger.info("User " + fromURI + 
					" registered with an Expire time of " + expires);
			}
		}				
						
		resp.send();
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		String sipSessionInviteAttribute  = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute  = (String) request.getApplicationSession().getAttribute("INVITE");
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Location Service : attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Location Service : attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
		}
		if(sipSessionInviteAttribute == null  || sipApplicationSessionInviteAttribute == null 
				|| !RECEIVED.equalsIgnoreCase(sipSessionInviteAttribute) 
				|| !RECEIVED.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			throw new IllegalArgumentException("State not replicated...");
		}
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		String from = request.getFrom().getURI().toString();
		Object timerSet = request.getSession().getAttribute("timerSet");
		if(from.contains(TEST_TERMINATION) && timerSet == null) {
			long duration = 30000;
			if(logger.isInfoEnabled()) {			
				logger.info("Starting timer for termination in " + duration + " ms ");
			}
			((TimerService)getServletContext().getAttribute(TIMER_SERVICE)).createTimer(request.getApplicationSession(), duration, false, (Serializable) request);
			request.getSession().setAttribute("timerSet", "true");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		if(logger.isInfoEnabled()) {
			logger.info("SimpleProxyServlet: Got response:\n" + response);
		}
//		if(SipServletResponse.SC_OK == response.getStatus() && "BYE".equalsIgnoreCase(response.getMethod())) {
//			SipSession sipSession = response.getSession(false);
//			if(sipSession != null) {
//				SipApplicationSession sipApplicationSession = sipSession.getApplicationSession();
//				sipSession.invalidate();
//				sipApplicationSession.invalidate();
//			}			
//		}
	}
	
	public void sessionCreated(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("LSS CREATED " + arg0.getApplicationSession());
		}
	}

	public void sessionDestroyed(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("LSS DESTROYED " + arg0.getApplicationSession());
			if(!new File("lssdestryed.flag").exists()) {
				try {
					new File("lssdestryed.flag").createNewFile();
				} catch (IOException e) {
					logger.error("Error flagging", e);
				}
			}
		}
	}

	public void sessionExpired(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("LSS EXPIRED " + arg0.getApplicationSession());
		}
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent arg0) {
		if(logger.isInfoEnabled()) {
			logger.info("LSS READY " + arg0.getApplicationSession());
		}
	}
	
	public void timeout(ServletTimer timer) {
		SipServletRequest request = (SipServletRequest)timer.getInfo();
		try {
			((ProxyExt)request.getProxy()).terminateSession(request.getSession(), 500, "Testing termination", 500, "Testing Termination");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TooManyHopsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
