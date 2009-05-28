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
import java.io.Serializable;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
import javax.servlet.sip.TelURL;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

public class ShootistSipServlet 
		extends SipServlet 
		implements SipServletListener,TimerListener {

	private static transient Logger logger = Logger.getLogger(ShootistSipServlet.class);	
	@Resource
	TimerService timerService;
	
	/** Creates a new instance of ShootistSipServlet */
	public ShootistSipServlet() {
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
			
			if(sipServletResponse.getRequest().isInitial() && !(sipServletResponse.getFrom().getURI() instanceof TelURL) && !(sipServletResponse.getTo().getURI() instanceof TelURL) &&
					(((SipURI)sipServletResponse.getFrom().getURI()).getUser().equals("reinvite") || ((SipURI)sipServletResponse.getTo().getURI()).getUser().equals("reinvite"))) {
				SipServletRequest request=sipServletResponse.getSession().createRequest("INVITE");				
				request.send();
			}  else {
				SipServletRequest sipServletRequest = sipServletResponse.getSession().createRequest("BYE");
				ServletTimer timer = timerService.createTimer(sipServletResponse.getApplicationSession(), 2000, false, (Serializable)sipServletRequest);
				sipServletResponse.getApplicationSession().setAttribute("timer", timer);
			}
		}
	}

	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {
		ServletTimer timer = (ServletTimer) req.getApplicationSession().getAttribute("timer");
		timer.cancel();
		req.createResponse(SipServletResponse.SC_OK).send();
	}
	
	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		
		String userName = ce.getServletContext().getInitParameter("username");
		if(userName == null || userName.length() < 1) {
			userName = "BigGuy";
		}
		URI fromURI = sipFactory.createSipURI(userName, "here.com");
		URI toURI = null;
		if(ce.getServletContext().getInitParameter("urlType") != null && ce.getServletContext().getInitParameter("urlType").equalsIgnoreCase("tel")) {
			try {
				toURI = sipFactory.createURI("tel:+358-555-1234567");
			} catch (ServletParseException e) {
				logger.error("Impossible to create the tel URL", e);
			}
		} else {
			toURI = sipFactory.createSipURI("LittleGuy", "there.com");
		}
		String toTag = ce.getServletContext().getInitParameter("toTag");
		if(toTag != null) {
			toURI.setParameter("tag", toTag);
		}
		String toParam = ce.getServletContext().getInitParameter("toParam");
		if(toParam != null) {
			toURI.setParameter("toParam", toParam);
		}
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, "INVITE", fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
		if(ce.getServletContext().getInitParameter("encodeRequestURI") != null) {
			sipApplicationSession.encodeURI(requestURI);
		}
		sipServletRequest.setRequestURI(requestURI);
		if(sipServletRequest.getTo().getParameter("tag") != null) {
			logger.error("the ToTag should be empty, not sending the request");
			return;
		}
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error("Unexpected exception while sending the INVITE request",e);
		}		
	}

	public void timeout(ServletTimer timer) {
		SipServletRequest sipServletRequest = (SipServletRequest) timer.getInfo();
		try {
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error("Unexpected exception while sending the BYE request",e);
		}
	}
}