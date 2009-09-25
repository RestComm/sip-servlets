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
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.SipSession.State;

import org.apache.log4j.Logger;

/**
 * This example shows a simple User agent that can any accept call and reply to BYE or initiate BYE 
 * depending on the sender.
 * 
 * @author Jean Deruelle
 *
 */
public class DistributableSimpleSipServlet 
		extends SipServlet 
		implements TimerListener {
	private static final long serialVersionUID = 1L;
	private static final String RECEIVED = "Received";

	private static Logger logger = Logger.getLogger(DistributableSimpleSipServlet.class);
	
	private static final String CALLEE_SEND_BYE = "YouSendBye";
	//60 sec
	private static final int DEFAULT_BYE_DELAY = 180000;
	
	/** Creates a new instance of SimpleProxyServlet */
	public DistributableSimpleSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the distributable simple sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: Got request:\n"
				+ request.getMethod());
		}
		if(request.isInitial() && request.getHeader("CSeq").indexOf("2") != -1) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Reinvite not recognized as subsequent");
			sipServletResponse.send();
			return;
		}
		request.getSession().setAttribute("INVITE", RECEIVED);
		request.getSession().setAttribute("activationListener", new SipSessionActivationListenerAttribute());
		request.getApplicationSession().setAttribute("INVITE", RECEIVED);
		request.getSession().setAttribute("activationListener", new SipApplicationSessionActivationListenerAttribute());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		if(CALLEE_SEND_BYE.equalsIgnoreCase(((SipURI)request.getTo().getURI()).getUser())) {
			TimerService timer = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
			String byeDelayStr = getServletContext().getInitParameter("bye.delay");
			int byeDelay = DEFAULT_BYE_DELAY;
			try{
				Integer.parseInt(byeDelayStr);
			} catch (NumberFormatException e) {
				logger.error("Impossible to parse the bye delay : " + byeDelayStr, e);
			}
			timer.createTimer(request.getApplicationSession(), byeDelay, false, request.getSession().getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: Got BYE request:\n" + request);
		}
		String sipSessionInviteAttribute  = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute  = (String) request.getApplicationSession().getAttribute("INVITE");
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Simple Servlet: attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
		}
		if(sipSessionInviteAttribute != null  && sipApplicationSessionInviteAttribute != null 
				&& RECEIVED.equalsIgnoreCase(sipSessionInviteAttribute) 
				&& RECEIVED.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			request.getSession().setAttribute("BYE", RECEIVED);
			request.getApplicationSession().setAttribute(" BYE", RECEIVED);
			SipServletResponse sipServletResponse = request.createResponse(200);
			sipServletResponse.send();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: Got response:\n" + response);
		}
		if(SipServletResponse.SC_OK == response.getStatus() && "BYE".equalsIgnoreCase(response.getMethod())) {
			SipApplicationSession sipApplicationSession = response.getApplicationSession(false);
			if(sipApplicationSession != null && sipApplicationSession.isValid()) {
				sipApplicationSession.invalidate();
			}			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
	 */
	public void timeout(ServletTimer servletTimer) {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: timer expired\n");
		}
		SipSession sipSession = servletTimer.getApplicationSession().getSipSession((String)servletTimer.getInfo());
		if(!State.TERMINATED.equals(sipSession.getState())) {
			try {
				sipSession.createRequest("BYE").send();
			} catch (IOException e) {
				logger.error("An unexpected exception occured while sending the BYE", e);
			}				
		}
	}
}