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

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;


public class SameInstanceSipServlet 
	extends SipServlet 
	implements SipApplicationSessionListener, TimerListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SameInstanceSipServlet.class);
	private static String INSTANCES_DIFFERENT = "KO"; 
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final long DELAY = 2000;
	
	@Resource
	private SipFactory sipFactory;
	@Resource
	private TimerService timerService;
	
	private SipServlet instance;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the NoAppName sip servlet has been started");
		super.init(servletConfig);
    	System.out.println("servlet instance<init>:" + this);
    	instance = this;   
    	try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			SipFactory jndiSipFactory = (SipFactory) envCtx.lookup("sip/SameInstanceServletTestApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + jndiSipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);			
		}
		logger.info("Sip Factory ref from JNDI : " + sipFactory);
	}
	
	@Override
	public void doInvite(SipServletRequest req) throws ServletException, IOException {
		logger.info("servlet instance<doInvite>:" + this);
		if(this.equals(instance)) {			
			req.createResponse(SipServletResponse.SC_OK).send();
		} else {
			logger.error ("sip servlet instances are different ! ");
			req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
		}
		SipApplicationSession appSession = req.getApplicationSession();
		timerService.createTimer(appSession, DELAY, true, "test");
	}

	public void sessionCreated(SipApplicationSessionEvent event) {
		// TODO Auto-generated method stub
		logger.info("servlet instance<sessionCreated>:" + this);
		if(!this.equals(instance)) {
			logger.error ("sip servlet instances are different ! ");
			try {
				SipServletRequest sipServletRequest = sipFactory.createRequest(
						event.getApplicationSession(), 
						"MESSAGE", 
						"sip:sender@sip-servlets.com", 
						"sip:receiver@sip-servlets.com");
				SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5080");
				sipServletRequest.setRequestURI(sipUri);
				sipServletRequest.setContentLength(INSTANCES_DIFFERENT.length());
				sipServletRequest.setContent(INSTANCES_DIFFERENT, CONTENT_TYPE);
				sipServletRequest.send();
			} catch (ServletParseException e) {
				logger.error("Exception occured while parsing the addresses",e);
			} catch (IOException e) {
				logger.error("Exception occured while sending the request",e);			
			}

		}
	}

	public void sessionDestroyed(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub
		log("servlet instance<sessionDestroyed>:" + this);
	}

	public void sessionExpired(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void timeout(ServletTimer timer) {
		System.out.println("servlet instance<timeout>:" + this);
		if(!this.equals(instance)) {
			logger.error ("sip servlet instances are different ! ");
			try {
				SipServletRequest sipServletRequest = sipFactory.createRequest(
						timer.getApplicationSession(), 
						"MESSAGE", 
						"sip:sender@sip-servlets.com", 
						"sip:receiver@sip-servlets.com");
				SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5080");
				sipServletRequest.setRequestURI(sipUri);
				sipServletRequest.setContentLength(INSTANCES_DIFFERENT.length());
				sipServletRequest.setContent(INSTANCES_DIFFERENT, CONTENT_TYPE);
				sipServletRequest.send();
			} catch (ServletParseException e) {
				logger.error("Exception occured while parsing the addresses",e);
			} catch (IOException e) {
				logger.error("Exception occured while sending the request",e);			
			}

		}
		
	}

}