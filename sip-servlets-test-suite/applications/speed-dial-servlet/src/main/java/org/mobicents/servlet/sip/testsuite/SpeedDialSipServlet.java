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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.ListeningPoint;

import org.apache.log4j.Logger;


public class SpeedDialSipServlet extends SipServlet implements SipErrorListener, SipApplicationSessionListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SpeedDialSipServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";	
	private static final String REMOTE_TRANSPORT = "udp";
	private static final int REMOTE_PORT = 5080;
	private static final String REMOTE_LOCALHOST_ADDR = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	
	private static final String INITIAL_REMOTE_TRANSPORT = "udp";
	private static final int INITIAL_REMOTE_PORT = 5090;
	private static final String INITIAL_REMOTE_LOCALHOST_ADDR = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	
	private static final String LOCAL_TRANSPORT = "udp";
	private static final int LOCAL_PORT = 5070;
	private static final String LOCAL_LOCALHOST_ADDR = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	
	private static final String TEST_USER_REMOTE = "remote";
	
	private static boolean isRecordRoute = true;
	
	Map<String, String> dialNumberToSipUriMapping = null;
	
	@Resource
	SipFactory sipFactory;

	/** Creates a new instance of SpeedDialSipServlet */
	public SpeedDialSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the speed dial sip servlet has been started");
		super.init(servletConfig);
		dialNumberToSipUriMapping = new HashMap<String, String>();
		dialNumberToSipUriMapping.put("1", "sip:receiver@sip-servlets.com");
		dialNumberToSipUriMapping.put("2", "sip:mranga@sip-servlets.com");
		dialNumberToSipUriMapping.put("3", "sip:vlad@sip-servlets.com");
		dialNumberToSipUriMapping.put("4", "sip:bartek@sip-servlets.com");
		dialNumberToSipUriMapping.put("5", "sip:jeand@sip-servlets.com");
		dialNumberToSipUriMapping.put("6", "sip:receiver-failover@sip-servlets.com");
		dialNumberToSipUriMapping.put("b2bua", "sip:fromProxy@sip-servlets.com");
		dialNumberToSipUriMapping.put("9", "sip:receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090");
		dialNumberToSipUriMapping.put("test-callResponseBacks", "sip:receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5090,sip:receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5091");
		dialNumberToSipUriMapping.put("forward-pending-sender", "sip:forward-pending-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
		dialNumberToSipUriMapping.put("factory-sender", "sip:factory-sender@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");		
		String initParam = servletConfig.getServletContext().getInitParameter("record_route");
		if(initParam != null && initParam.equals("false")) {
			isRecordRoute = false;
		}
		logger.info("Speed dial sip servlet is record routing ?" + isRecordRoute);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());
		logger.info(request.getRequestURI().toString());
		if(request.isInitial()) {
			if(request.isInitial()) {
				String user = ((SipURI)request.getFrom().getURI()).getUser();
				if(user.contains(TEST_USER_REMOTE)) {
					String transport ="udp";
					if(user.contains("tcp")) {
						transport="tcp";
					}
					if(!checkRequestAddressPortTransport(request, transport)) {
						return ;
					}
				}
				
				String dialNumber = ((SipURI)request.getRequestURI()).getUser();
				String mappedUri = dialNumberToSipUriMapping.get(dialNumber);	
				if(mappedUri != null) {
					SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
					Proxy proxy = request.getProxy();
					proxy.setProxyTimeout(120);
					proxy.setRecordRoute(isRecordRoute);
					proxy.setParallel(false);
					proxy.setSupervised(true);
					logger.info("proxying to " + mappedUri);
					if(mappedUri.contains(",")) {
						List<URI> uris = new ArrayList<URI>();
						StringTokenizer stringTokenizer = new StringTokenizer(mappedUri, ",");
						while (stringTokenizer.hasMoreTokens()) {
							String uri = stringTokenizer.nextToken();
							uris.add(sipFactory.createURI(uri));
						}
						proxy.proxyTo(uris);
					} else {
						proxy.proxyTo(sipFactory.createURI(mappedUri));
					}
				} else {
					SipServletResponse sipServletResponse = 
						request.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE, "No mapping for " + dialNumber);
					sipServletResponse.send();			
				}
			}						
		}
	}
	
	private boolean checkRequestAddressPortTransport(SipServletRequest request, String transport) throws IOException {
		boolean isCorrect = false;
		if(request.getRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && request.getTransport().equalsIgnoreCase(transport)) {				
			if(transport.equalsIgnoreCase("tcp")) {
				logger.info("remote tcp information is correct");
				isCorrect =true;
			} else {
				if(request.getRemotePort() == REMOTE_PORT){
					logger.info("remote udp information is correct");
				}
				isCorrect =true;
			}			
		}
		if(request.getRemoteAddr() == null) {
			isCorrect = false;
		}
		if(!isCorrect){
			logger.error("remote information is incorrect");
			logger.error("remote addr " + request.getRemoteAddr());
			logger.error("remote port " + request.getRemotePort());
			logger.error("remote transport " + request.getTransport());
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Incorrect remote information");
			sipServletResponse.send();
			return false;
		}
		isCorrect = false;
		if(request.getInitialRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && request.getInitialTransport().equalsIgnoreCase(transport)) {				
			if(transport.equalsIgnoreCase("tcp")) {
				logger.info("initial remote tcp information is correct");
				isCorrect =true;
			} else {
				if(request.getInitialRemotePort() == REMOTE_PORT){
					logger.info("initial remote udp information is correct");
				}
				isCorrect =true;
			}			
		}
		if(request.getInitialRemoteAddr() == null) {
			isCorrect = false;
		}
		if(!isCorrect){
			logger.error("initial remote information is incorrect");
			logger.error("initial remote addr " + request.getInitialRemoteAddr());
			logger.error("initial remote port " + request.getInitialRemotePort());
			logger.error("initial remote transport " + request.getInitialTransport());
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Incorrect initial remote information");
			sipServletResponse.send();
			return false;
		}
		isCorrect = false;
		if(request.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && request.getTransport().equalsIgnoreCase(transport)) {				
			if(transport.equalsIgnoreCase("tcp")) {
				logger.info("local tcp information is correct");
				isCorrect =true;
			} else {
				if(request.getLocalPort() == LOCAL_PORT){
					logger.info("local udp information is correct");
				}
				isCorrect =true;
			}			
		}	
		if(request.getLocalAddr() == null) {
			isCorrect = false;
		}
		if(!isCorrect){
			logger.error("local information is incorrect");
			logger.error("local addr " + request.getLocalAddr());
			logger.error("local port " + request.getLocalPort());
			logger.error("local transport " + request.getTransport());
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Incorrect local information");
			sipServletResponse.send();
			return false;
		}
		return true;
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got Error response " + resp);
		if(resp.getTo().toString().contains("test-callResponseBacks")) {
			resp.getApplicationSession().setAttribute("errorResponseReceivedAtAppLevel", "true");
		} 
		if(resp.getApplicationSession().getAttribute("errorResponseReceivedAtAppLevel") != null) {
			throw new IllegalStateException("Error Response Received at Application Level but we should only see the best final response");
		}
	}
	
	@Override
	protected void doBranchResponse(SipServletResponse resp)
			throws ServletException, IOException {	
		logger.info("Got Branch response " + resp);
		if(resp.getTo().toString().contains("test-callResponseBacks")) {
			if(resp.getStatus() >= 400) {
				resp.getApplicationSession().setAttribute("doBranchErrorResponseReceivedAtAppLevel", "true");
			}
			if(resp.getStatus() >= 200) {
				resp.getApplicationSession().setAttribute("doBranchSuccessResponseReceivedAtAppLevel", "true");
			}
		} 
	}
	
	protected void doSuccessResponse(SipServletResponse resp)
		throws ServletException, IOException {
		logger.info("Got Success response " + resp);
		if(resp.getApplicationSession().getAttribute("errorResponseReceivedAtAppLevel") != null) {
			throw new IllegalStateException("Error Response Received at Application Level but we should only see the best final response");
		}
		if(resp.getTo().toString().contains("test-callResponseBacks") && !resp.isBranchResponse()) {
			resp.getApplicationSession().setAttribute("doSuccessResponseReceivedAtAppLevel", "true");
		}
		if(((SipURI)resp.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			if(resp.getRemoteAddr().equals(LOCAL_LOCALHOST_ADDR) && resp.getRemotePort() == LOCAL_PORT && resp.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("remote information is correct");
			} else {
				if(resp.getRemotePort()!=5090){ // 5090 is the remote client
					logger.error("remote information is incorrect");
					logger.error("remote addr " + resp.getRemoteAddr());
					logger.error("remote port " + resp.getRemotePort());
					logger.error("remote transport " + resp.getTransport());
					throw new IllegalArgumentException("remote information is incorrect");
				}
			}
			if(resp.getInitialRemoteAddr().equals(INITIAL_REMOTE_LOCALHOST_ADDR) && resp.getInitialRemotePort() == INITIAL_REMOTE_PORT && resp.getInitialTransport().equalsIgnoreCase(INITIAL_REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + resp.getInitialRemoteAddr());
				logger.error("Initial remote port " + resp.getInitialRemotePort());
				logger.error("Initial remote transport " + resp.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(resp.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && resp.getLocalPort() == LOCAL_PORT && resp.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("local information is correct");
			} else {
				logger.error("local information is incorrect");
				logger.error("local addr " + resp.getLocalAddr());
				logger.error("local port " + resp.getLocalPort());
				logger.error("local transport " + resp.getTransport());
				throw new IllegalArgumentException("local information is incorrect");
			}
		}
	}
	
	@Override
	protected void doCancel(SipServletRequest req) throws ServletException,
			IOException {
		logger.error("CANCEL seen at proxy " + req);
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

	public void sessionCreated(SipApplicationSessionEvent ev) {
		
	}

	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		
	}

	public void sessionExpired(SipApplicationSessionEvent ev) {
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		if(ev.getApplicationSession().getAttribute("doSuccessResponseReceivedAtAppLevel") != null && 
			ev.getApplicationSession().getAttribute("doBranchErrorResponseReceivedAtAppLevel") != null &&
			ev.getApplicationSession().getAttribute("doBranchSuccessResponseReceivedAtAppLevel") != null) {
			sendMessage(sipFactory.createApplicationSession(), sipFactory, "allResponsesReceivedCorrectlyOnEachCallBack", null);
		}
	}
}
