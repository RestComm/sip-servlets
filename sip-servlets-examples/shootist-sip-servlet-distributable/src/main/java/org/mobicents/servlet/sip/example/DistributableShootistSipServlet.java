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
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

public class DistributableShootistSipServlet 
		extends SipServlet 
		implements SipServletListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DistributableShootistSipServlet.class);
		
	private static final String SENT = "Sent";
	
	/** Creates a new instance of ShootistSipServlet */
	public DistributableShootistSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the shootist has been started");
		super.init(servletConfig);
	}		
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			SipServletRequest ackRequest = sipServletResponse.createAck();
			ackRequest.send();			
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		String sipSessionInviteAttribute  = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute  = (String) request.getApplicationSession().getAttribute("INVITE");
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Shootist Servlet: attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Shootist Servlet: attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
		}
		if(sipSessionInviteAttribute == null  || sipApplicationSessionInviteAttribute == null 
				|| !SENT.equalsIgnoreCase(sipSessionInviteAttribute) 
				|| !SENT.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			throw new IllegalArgumentException("State not replicated...");
		}
		request.createResponse(SipServletResponse.SC_OK).send();
	}
	
	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		String sendOnInit = ce.getServletContext().getInitParameter("send-on-init");
		logger.info("Send on Init : "+ sendOnInit);
		if(sendOnInit == null || "true".equals(sendOnInit)) {
			SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			
			URI fromURI = sipFactory.createSipURI("BigGuy", "here.com");
			URI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromURI, toURI);
			SipURI requestURI = sipFactory.createSipURI("LittleGuy", "127.0.0.1:5090");
			sipServletRequest.setRequestURI(requestURI);
			
			sipServletRequest.getSession().setAttribute("INVITE", SENT);
			sipServletRequest.getApplicationSession().setAttribute("INVITE", SENT);
			
			try {			
				sipServletRequest.send();
			} catch (IOException e) {
				logger.error(e);
			}	
		}
	}
}