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
 *
 * This file incorporates work covered by the following copyright contributed under the GNU LGPL : Copyright 2007-2011 Red Hat.
 */

package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;

import org.apache.log4j.Logger;

@SipListener
public class LocationServiceSipServlet extends SipServlet implements SipServletListener, TimerListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(LocationServiceSipServlet.class);
	
	private static final String LOCAL_TRANSPORT = "udp";
	private static final String LOCAL_LOCALHOST_ADDR = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	
	private static final String REMOTE_TRANSPORT = "udp";
	private static final String REMOTE_LOCALHOST_ADDR = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	
	private static final String INITIAL_REMOTE_TRANSPORT = "udp";
	private static final String INITIAL_REMOTE_LOCALHOST_ADDR = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";
	
	private static final String TEST_USER_REMOTE = "remote";
	
	Map<String, List<URI>> registeredUsers = null;
	
	@Resource
	SipFactory sipFactory;
	@Resource
	TimerService timerService;
	ServletTimer timerTask;
        static ServletContext ctx;         
	
	/** Creates a new instance of SpeedDialSipServlet */
	public LocationServiceSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the location service sip servlet has been started");
		super.init(servletConfig);
                ctx = servletConfig.getServletContext();                 
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		registeredUsers = new HashMap<String, List<URI>>();
		List<URI> uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx)));
		uriList.add(sipFactory.createURI("sip:receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":6090"));
		registeredUsers.put("sip:receiver@sip-servlets.com", uriList);
		// https://code.google.com/p/sipservlets/issues/detail?id=273
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver-prack@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx)));
		registeredUsers.put("sip:receiver-prack@sip-servlets.com", uriList);
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver-prack@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx)));
		registeredUsers.put("sip:receiver-prack@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx), uriList);
		
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:cancel-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx)));
		registeredUsers.put("sip:cancel-receiver@sip-servlets.com", uriList);
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:cancel-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx)));
		uriList.add(sipFactory.createURI("sip:cancel-receiver2@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getReceiver2Port(ctx)));		
		registeredUsers.put("sip:cancel-receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx), uriList);
		
		
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver-failover@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx)));
		registeredUsers.put("sip:receiver-failover@sip-servlets.com", uriList);
		registeredUsers.put("sip:receiver-failover@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getTestPort(ctx), uriList);
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getServletContainerPort(ctx)));
		registeredUsers.put("sip:proxy-b2bua@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + getServletContainerPort(ctx), uriList);
		
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());		
		if(!request.getRequestURI().toString().contains("act-as-uac") && timerTask != null) {
			timerTask.cancel();
		}
		if(((SipURI)request.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			if(request.getRemoteAddr().equals(LOCAL_LOCALHOST_ADDR) && request.getRemotePort() == getServletContainerPort(ctx) && request.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {
				logger.info("remote information is correct");
			} else {
				logger.error("remote information is incorrect");
				logger.error("remote addr " + request.getRemoteAddr());
				logger.error("remote port " + request.getRemotePort());
				logger.error("remote transport " + request.getTransport());
				SipServletResponse sipServletResponse = 
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Incorrect remote information");
				sipServletResponse.send();
				return;
			}
			if(request.getInitialRemoteAddr().equals(INITIAL_REMOTE_LOCALHOST_ADDR) && 
                                request.getInitialRemotePort() == getServletContainerPort(ctx) && 
                                request.getInitialTransport().equalsIgnoreCase(INITIAL_REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + request.getInitialRemoteAddr());
				logger.error("Initial remote port " + request.getInitialRemotePort());
				logger.error("Initial remote transport " + request.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(request.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && request.getLocalPort() == getServletContainerPort(ctx) && request.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("local information is correct");
			} else {
				logger.error("local information is incorrect");
				logger.error("local addr " + request.getLocalAddr());
				logger.error("local port " + request.getLocalPort());
				logger.error("local transport " + request.getTransport());
				throw new IllegalArgumentException("local information is incorrect");
			}
		}
		
		String count = null;
		if(request.getHeader("X-Count") != null) {
			count = request.getHeader("X-Count");
		}
		if(request.isInitial()) {
			if(count != null && count.equalsIgnoreCase("0")) {
				Proxy proxy = request.getProxy();
				proxy.setRecordRoute(true);
				proxy.setParallel(true);
				proxy.setSupervised(true);
				ArrayList<URI> uris = new ArrayList<URI>();
				uris.add(sipFactory.createURI("sip:receiver@sip-servlets.com"));
				List<ProxyBranch> proxyBranches = proxy.createProxyBranches(uris);
				for(ProxyBranch proxyBranch : proxyBranches) {
					proxyBranch.getRequest().setHeader("X-Count", "1");
				} 
				logger.info("proxying to " + request.getRequestURI());
				proxy.startProxy();
				return;
			}
			List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
			if(contactAddresses != null && contactAddresses.size() > 0) {			
				Proxy proxy = request.getProxy();
				proxy.setProxyTimeout(3);
				proxy.setRecordRoute(true);
				proxy.setParallel(true);
				proxy.setSupervised(true);			
				for (URI uri : contactAddresses) {
					logger.info("proxying to " + uri);
				}
				proxy.proxyTo(contactAddresses);		
			} else {
				logger.info(request.getRequestURI().toString() + " is not currently registered");
				SipServletResponse sipServletResponse = 
					request.createResponse(SipServletResponse.SC_MOVED_PERMANENTLY, "Moved Permanently");
				sipServletResponse.send();
			}
		}
	}
	
	@Override
	protected void doPrack(SipServletRequest req) throws ServletException,
			IOException {
		req.addHeader("X-Seen", "" + UUID.randomUUID());
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got response " + resp);
	}
	
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got response " + resp);
		if(((SipURI)resp.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			String npecause = resp.getTransport();
			npecause = resp.getInitialTransport();
			if(resp.getRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && resp.getRemotePort() == getTestPort(ctx) && resp.getTransport().equalsIgnoreCase(REMOTE_TRANSPORT)) {
				logger.info("remote information is correct");
			} else {
				logger.error("remote information is incorrect");
				logger.error("remote addr " + resp.getRemoteAddr());
				logger.error("remote port " + resp.getRemotePort());
				logger.error("remote transport " + resp.getTransport());
				throw new IllegalArgumentException("remote information is incorrect");
			}
			if(resp.getInitialRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && resp.getInitialRemotePort() == getTestPort(ctx) && resp.getInitialTransport().equalsIgnoreCase(REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + resp.getInitialRemoteAddr());
				logger.error("Initial remote port " + resp.getInitialRemotePort());
				logger.error("Initial remote transport " + resp.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(resp.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && resp.getLocalPort() == getServletContainerPort(ctx) && resp.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
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
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Received register request: " + req.getTo());
		
		//Storing the registration 
		Address toAddress = req.getTo();
		ListIterator<Address> contactAddresses = req.getAddressHeaders("Contact");
		List<URI> contactUris = new ArrayList<URI>();
		while (contactAddresses.hasNext()) {
			Address contactAddress = contactAddresses.next();
			contactUris.add(contactAddress.getURI());
		}
		//FIXME handle the expires to add or remove the user
		registeredUsers.put(toAddress.toString(), contactUris);
		//answering OK to REGISTER
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}
	
	@Override
	protected void doCancel(SipServletRequest req) throws ServletException,
			IOException {
		logger.error("CANCEL seen at proxy " + req);
	}

	@Override
	public void servletInitialized(SipServletContextEvent ce) {
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		timerTask = timerService.createTimer(sipApplicationSession, 2000, false, null);
	}

	@Override
	public void timeout(ServletTimer timer) {
		try {
			SipServletRequest newRequest = sipFactory.createRequest(
					timer.getApplicationSession(), 
					"INVITE", 
					"sip:uac@" + System.getProperty("org.mobicents.testsuite.testhostaddr"), 
					"sip:receiver@sip-servlets.com");
			newRequest.addHeader("X-Count", "0");
			newRequest.send();
		} catch (Exception e) {
			logger.error("an error occured sending the UAC INVITE ", e);
		}
	}
	
        
        public static Integer getTestPort(ServletContext ctx) {
            String tPort = ctx.getInitParameter("testPort");
            logger.info("TestPort at:" + tPort);
            if (tPort != null) {
                return Integer.valueOf(tPort);
            } else {
                return 5090;
            }
        }
        
        public static Integer getSenderPort(ServletContext ctx) {
            String tPort = ctx.getInitParameter("senderPort");
            logger.info("SenderPort at:" + tPort);
            if (tPort != null) {
                return Integer.valueOf(tPort);
            } else {
                return 5080;
            }
        }  
        
        public static Integer getReceiver2Port(ServletContext ctx) {
            String tPort = ctx.getInitParameter("receiver2Port");
            logger.info("Receiver2Port at:" + tPort);
            if (tPort != null) {
                return Integer.valueOf(tPort);
            } else {
                return 5091;
            }
        }        
        
        public static Integer getServletContainerPort(ServletContext ctx) {
            String cPort = ctx.getInitParameter("servletContainerPort");
            logger.info("TestPort at:" + cPort);            
            if (cPort != null) {
                return Integer.valueOf(cPort);
            } else {
                return 5070;
            }            
        }     
	
}
