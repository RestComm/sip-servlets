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
package org.mobicents.servlet.sip.example;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

/**
 * This Sip Servlet acts as as a UAC registering to callwithus
 * @author Jean Deruelle
 *
 */
public class UACRegisterSipServlet extends SipServlet implements SipServletListener {
	private static final long serialVersionUID = 1L;
	private static transient final Logger logger = Logger.getLogger(UACRegisterSipServlet.class);		

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the UAC Register sip servlet has been started");
		super.init(servletConfig);		
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse response)
			throws ServletException, IOException {		
			
		logger.info("Got response: " + response);
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		if(response.getStatus() == SipServletResponse.SC_UNAUTHORIZED || 
				response.getStatus() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
			// Avoid re-sending if the auth repeatedly fails.
			if(!"true".equals(getServletContext().getAttribute("FirstResponseRecieved")))
			{
				getServletContext().setAttribute("FirstResponseRecieved", "true");
				AuthInfo authInfo = sipFactory.createAuthInfo();
				authInfo.addAuthInfo(
						response.getStatus(), 
						response.getChallengeRealms().next(), 
						getServletContext().getInitParameter("user.name"), 
						getServletContext().getInitParameter("password"));
				
				SipServletRequest challengeRequest = response.getSession().createRequest(
						response.getRequest().getMethod());
				
				challengeRequest.addAuthHeader(response, authInfo);
				logger.info("Sending the challenge request " + challengeRequest);
				challengeRequest.send();				
			}
		} else {					
			super.doErrorResponse(response);
		}
		
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		
		logger.info("GOT SUCESS RESPONSE HURRAH ! : " + resp);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		ServletContext servletContext = ce.getServletContext();
		SipFactory sipFactory = (SipFactory) servletContext.getAttribute(SIP_FACTORY);
		String userName = servletContext.getInitParameter("user.name");
		String domainName = servletContext.getInitParameter("domain.name");		
		
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		// for REGISTER the from and the to headers are one and the same
		SipURI fromToURI = sipFactory.createSipURI(userName, domainName);				
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, "REGISTER", fromToURI, fromToURI);
		sipServletRequest.setHeader("Expires", "3600");		
		sipServletRequest.setHeader("User-Agent", "MobicentsSipServlets");
		SipURI requestURI = sipFactory.createSipURI(null, domainName);
		try {
			Parameterable parameterable = sipServletRequest.getParameterableHeader("Contact");
			parameterable.setParameter("expires", "0");
		} catch (ServletParseException e1) {
			logger.error("Impossible to set the expires on the contact header",e1);
		}
		try {			
			sipServletRequest.setRequestURI(requestURI);				
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error("An unexpected exception occured while sending the REGISTER request",e);
		}
	}
	
}