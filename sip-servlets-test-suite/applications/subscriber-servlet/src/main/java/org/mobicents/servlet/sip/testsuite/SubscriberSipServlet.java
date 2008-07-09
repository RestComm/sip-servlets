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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SubscriberSipServlet 
		extends SipServlet 
		implements SipServletListener {

	private static Log logger = LogFactory.getLog(SubscriberSipServlet.class);
	
	
	/** Creates a new instance of SubscriberSipServlet */
	public SubscriberSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the Subscriber sip servlet has been started");
		super.init(servletConfig);
	}		
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
	}

	@Override
	protected void doNotify(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got notify : "
				+ request.getMethod());	
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		if("Active".equalsIgnoreCase(request.getHeader("Subscription-State"))) {
			SipServletRequest subscriberRequest = request.getSession().createRequest("SUBSCRIBE");
			SipURI requestURI = ((SipFactory)getServletContext().getAttribute(SIP_FACTORY)).createSipURI("LittleGuy", "127.0.0.1:5080");
			subscriberRequest.setRequestURI(requestURI);
			subscriberRequest.setHeader("Expires", "0");
			subscriberRequest.setHeader("Event", "reg");
			try {			
				subscriberRequest.send();
			} catch (IOException e) {
				logger.error(e);
			}	
		}
	}
	
	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		SipURI fromURI = sipFactory.createSipURI("BigGuy", "here.com");			
		SipURI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, "SUBSCRIBE", fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
		sipServletRequest.setRequestURI(requestURI);
		sipServletRequest.setHeader("Expires", "200");
		sipServletRequest.setHeader("Event", "reg");
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}		
	}
}