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
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class MainHandlerSipServlet
		extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(MainHandlerSipServlet.class);
	
	private SipFactory sipFactory;	
	private static String TEST_BAD_HANDLER_USERNAME = "badHandler";
	
	/** Creates a new instance of MainHandlerSipServlet */
	public MainHandlerSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the main handler test sip servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.MainHandlerApplication/SipFactory");
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
		if(TEST_BAD_HANDLER_USERNAME.equalsIgnoreCase(((SipURI)request.getFrom().getURI()).getUser())) {
			request.getSession().setHandler(TEST_BAD_HANDLER_USERNAME);
		} else {
			request.getSession().setHandler("SecondaryHandlerSipServlet");
		}
		SipServletResponse ringingResponse = request.createResponse(SipServletResponse.SC_RINGING);
		ringingResponse.send();				
		SipServletResponse okResponse = request.createResponse(SipServletResponse.SC_OK);
		okResponse.send();		
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse response)
			throws ServletException, IOException {
		Boolean responseReceived = (Boolean) response.getSession().getAttribute("mainHandlerResponseReceived");
		if(responseReceived == null) {
			responseReceived = true;
			response.getSession().setAttribute("mainHandlerResponseReceived", responseReceived);
			MessageSenderUtil.sendMessageHandlerOK(response.getSession(), "MainHandler : response OK");
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
}
