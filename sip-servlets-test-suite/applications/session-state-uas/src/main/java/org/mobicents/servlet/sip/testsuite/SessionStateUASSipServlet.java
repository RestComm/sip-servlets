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
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.SipSession.State;

import org.apache.log4j.Logger;


public class SessionStateUASSipServlet
		extends SipServlet {

	private static transient Logger logger = Logger.getLogger(SessionStateUASSipServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";		
	private static final String SEND_1XX_2XX = "send1xx_2xx";
	private static final String SEND_1XX_4XX = "send1xx_4xx";
	private static final String SEND_4XX = "send4xx";
	private static final String SEND_2XX = "send2xx";
	//TODO externalize in sip.xml and specify it from test if needed
	private static final int TIMEOUT = 10000;
	
	private SipFactory sipFactory;	
	
	
	/** Creates a new instance of SessionStateUASSipServlet */
	public SessionStateUASSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the session state UAS test sip servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.SessionStateUASApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
	}		
	
	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());		
		
		// send message precising the current session state
		sendMessage(request.getSession().getState().toString());
		
		String message = (String)request.getContent();
		//checking state machines for UAS mode,  sending session state after each response sent
		if(message != null && message.length() > 0) {			
			if(SEND_1XX_2XX.equals(message)) {	
				SipServletResponse ringingResponse = request.createResponse(SipServletResponse.SC_RINGING);
				ringingResponse.send();
				
				// send message precising the current session state
				sendMessage(request.getSession().getState().toString());
				
				SipServletResponse okResponse = request.createResponse(SipServletResponse.SC_OK);
				okResponse.send();
				
				// send message precising the current session state
				sendMessage(request.getSession().getState().toString());
			} else if(SEND_1XX_4XX.equals(message)) {
				request.getSession().setInvalidateWhenReady(false);
				
				SipServletResponse ringingResponse = request.createResponse(SipServletResponse.SC_RINGING);
				ringingResponse.send();
				
				// send message precising the current session state
				sendMessage(request.getSession().getState().toString());
				
				SipServletResponse forbiddenResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
				forbiddenResponse.send();
				
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					logger.error("unexpected exception while putting the thread to sleep", e);
				}
				if(request.getSession().isValid()) {
					logger.info("the session have not been invalidated by the container since the invalidateWhenReady flag is false");
					// send message precising the current session state
					sendMessage(request.getSession().getState().toString());
					request.getSession().invalidate();
				} else {
					logger.error("the session should not have been invalidated by the container since the invalidateWhenReady flag is false");
				}
			} else if(SEND_2XX.equals(message)) {
				SipServletResponse okResponse = request.createResponse(SipServletResponse.SC_OK);
				okResponse.send();
				
				// send message precising the current session state
				sendMessage(request.getSession().getState().toString());
			} else if(SEND_4XX.equals(message)) {
				SipServletResponse forbiddenResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
				forbiddenResponse.send();
				
				// send message precising the current session state
				if(request.getSession().isValid()) {
					sendMessage(request.getSession().getState().toString());
				} else {
					sendMessage(State.TERMINATED.toString());
				}
			}
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
		
		// send message precising the current session state
		sendMessage(request.getSession().getState().toString());
	}	

	/**
	 * Utility method to send a message out of dialog
	 * @param messageContent messageContent
	 */
	public void sendMessage(String messageContent) {
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri = sipFactory.createSipURI("receiver", "127.0.0.1:5080");
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

}
