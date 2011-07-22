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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;


public class SessionStateUACSipServlet
		extends SipServlet 
		implements SipServletListener, SipApplicationSessionListener, SipSessionListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SessionStateUACSipServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final String SEND_1XX_4XX = "send1xx_4xx";
//	private static final String SEND_1XX_2XX = "send1xx_2xx";	
//	private static final String SEND_4XX = "send4xx";
//	private static final String SEND_2XX = "send2xx";
//	
//	private SipFactory sipFactory;	
	
	/** Creates a new instance of SessionStateUACSipServlet */
	public SessionStateUACSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the session state UAC test sip servlet has been started");		
	}		
	
	@Override
	protected void doResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse);
		if ("INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			sendMessage(sipFactory, sipServletResponse.getSession().getState().toString());		
		}
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		if(sipServletResponse.getStatus() == 408) {			
			try {
				Properties jndiProps = new Properties();			
				Context initCtx = new InitialContext(jndiProps);
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.SessionStateUACApplication/SipFactory");
				sendMessage(sipFactory, "408 received");
			} catch (NamingException e) {
				throw new ServletException("Uh oh -- JNDI problem !", e);			
			}
		}
		if(sipServletResponse.getStatus() == 200 && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			sipServletResponse.createAck().send();			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SipServletRequest refer =sipServletResponse.getSession().createRequest("REFER");
			refer.addHeader("Refer-To", "sip:refer-to@nist.gov");
			refer.send();
		}		
		if(sipServletResponse.getStatus() >= 200 && "REFER".equalsIgnoreCase(sipServletResponse.getMethod())) {
			sendMessage(sipFactory, sipServletResponse.getSession().getState().toString());		
		}
	}
	
	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {
		req.createResponse(200).send();
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		sendMessage(sipFactory, req.getSession().getState().toString());
	}

	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
		if(getServletContext().getInitParameter("testTimeout") != null) {
			sendMessage(sipFactory, "This request must timeout", "sip:timeout@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":4794");
		}else {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			SipURI fromURI = sipFactory.createSipURI("BigGuy", "here.com");			
			SipURI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromURI, toURI);
			SipURI requestURI = sipFactory.createSipURI("LittleGuy", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
			sipServletRequest.setRequestURI(requestURI);
			sipServletRequest.setContentLength(SEND_1XX_4XX.length());
			try {
				sipServletRequest.setContent(SEND_1XX_4XX, CONTENT_TYPE);
				sipServletRequest.send();			
			} catch (IOException e) {
				logger.error("An Io exception occured while trying to set the content or send the request", e);
			}		
			sendMessage(sipFactory, sipServletRequest.getSession().getState().toString());
		}
	}
	
	/**
	 * Utility method to send a message out of dialog
	 * @param messageContent messageContent
	 */
	public void sendMessage(SipFactory sipFactory, String messageContent) {
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri = sipFactory.createSipURI("receiver", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(messageContent.length());
			sipServletRequest.setContent(messageContent, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
	
	public void sendMessage(SipFactory sipFactory, String messageContent, String addr) {
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();			
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri = (SipURI) sipFactory.createURI(addr);
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(messageContent.length());
			sipServletRequest.setContent(messageContent, CONTENT_TYPE);
			sipServletRequest.send();
			sipApplicationSession.setExpires(1);
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}

	public void sessionCreated(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionExpired(SipApplicationSessionEvent ev) {
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		sendMessage(sipFactory, "sessionExpired");
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		if(getServletContext().getInitParameter("testTimeout") != null) {
			sendMessage(sipFactory, "sipAppSessionReadyToInvalidate");
		}
	}

	public void sessionCreated(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		if(getServletContext().getInitParameter("testTimeout") != null) {
			sendMessage(sipFactory, "sipSessionReadyToInvalidate");
		}
	}

}
