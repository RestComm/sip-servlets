/*
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
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.annotation.SipApplicationKey;

import org.apache.log4j.Logger;


public class ChatRoomSipServlet
		extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ChatRoomSipServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
//	private static final String OK = "OK";
//	private static final String KO = "KO";
//	
//	private static final String ATTRIBUTE = "attribute";
//	private static final String VALUE = "value";
//	private static final String NEW_VALUE = "new_value";
	
	private SipFactory sipFactory;	
	
	private static boolean staticField = false;
	
	/** Creates a new instance of ListenersSipServlet */
	public ChatRoomSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the listeners test sip servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.ChatRoomApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
		staticField = true;
	}		
	
	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Static Field value " + staticField);
		if(staticField) {
			logger.info("Got request: "
					+ request.getMethod());
			SipServletResponse ringingResponse = request.createResponse(SipServletResponse.SC_RINGING);
			ringingResponse.send();
			Integer numberOfParticipants = (Integer) request.getApplicationSession().getAttribute("numberOfParticipants");
			if(numberOfParticipants == null) {
				numberOfParticipants = 0;			
			}
			numberOfParticipants++;
			request.getApplicationSession().setAttribute("numberOfParticipants", numberOfParticipants);
			SipServletResponse okResponse = request.createResponse(SipServletResponse.SC_OK);
			okResponse.send();
		} else {
			SipServletResponse response = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
			response.send();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request: " + request);
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}	

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());		
		sendNumberOfParticipants(request);
	}
	
	/**
	 * 
	 * @param request
	 */
	private void sendNumberOfParticipants(SipServletRequest request) {
		try {
			SipServletRequest responseMessage = request.getSession().createRequest("MESSAGE");
			Integer numberOfParticipants = (Integer) request.getApplicationSession().getAttribute("numberOfParticipants");
			responseMessage.setContentLength(numberOfParticipants.toString().length());
			responseMessage.setContent(numberOfParticipants.toString(), CONTENT_TYPE);
			responseMessage.send();
		} catch (UnsupportedEncodingException e) {
			logger.error("the encoding is not supported", e);
		} catch (IOException e) {
			logger.error("an IO exception occured", e);
		} 
	}
	
	@SipApplicationKey
	public static String generateChatroomApplicationKey(SipServletRequest request) {
		logger.info("Static Field value " + staticField);
		if(request.getSubscriberURI() == null) {
			throw new IllegalStateException("the subscriber URI should not be null");
		}
		if(staticField) {
			if("modifyRequest".equalsIgnoreCase(((SipURI)request.getFrom().getURI()).getUser())) {
				request.setExpires(1000);
			}
			return request.getRequestURI().toString();
		} else {
			throw new IllegalStateException("the static field should have been initialized");
		}
	}
}
