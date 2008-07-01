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
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionActivationListener;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.annotation.SipApplicationKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SecondaryHandlerSipServlet
		extends SipServlet {

	private static Log logger = LogFactory.getLog(SecondaryHandlerSipServlet.class);
	
	private SipFactory sipFactory;	
	
	
	/** Creates a new instance of SecondaryHandlerSipServlet */
	public SecondaryHandlerSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the secondary handler test sip servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
	}		

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());		
		MessageSenderUtil.sendMessageHandlerOK(request.getSession(), "SecondaryHandler : Subsequent request received");
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse response)
			throws ServletException, IOException {
		Boolean responseReceived = (Boolean) response.getSession().getAttribute("secondaryHandlerResponseReceived");
		if(responseReceived == null) {
			responseReceived = true;
			response.getSession().setAttribute("secondaryHandlerResponseReceived", responseReceived);
			MessageSenderUtil.sendMessageHandlerOK(response.getSession(), "SecondaryHandler : response OK");
		} else {
			response.getSession().setHandler("MainHandlerSipServlet");
			MessageSenderUtil.sendMessageHandlerOK(response.getSession(), "SecondaryHandler : testing MainHandler");			
		}
	}
}
