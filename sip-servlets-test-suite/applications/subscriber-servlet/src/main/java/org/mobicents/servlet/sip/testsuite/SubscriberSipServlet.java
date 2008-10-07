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

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SubscriberSipServlet 
		extends SipServlet 
		implements SipServletListener, TimerListener, SipSessionListener {

	private static Log logger = LogFactory.getLog(SubscriberSipServlet.class);
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final String SIP_SESSION_READY_TO_BE_INVALIDATED = "sipSessionReadyToBeInvalidated";
	
	@Resource
	SipFactory sipFactory;
	
	/** Creates a new instance of SubscriberSipServlet */
	public SubscriberSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the Subscriber sip servlet has been started");
		super.init(servletConfig);
	}		
	
	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("from : " + request.getFrom());
		logger.info("Got request: "
				+ request.getMethod());
	
		//If we receive an INVITE, we cancel the timer to avoid acting as UAC and sending REFER
		ServletTimer servletTimer = (ServletTimer)getServletContext().getAttribute("servletTimer");
		servletTimer.cancel();
		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		request.getSession().setAttribute("inviteReceived", "true");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
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
		SipServletRequest subscribe = request.getSession().createRequest("SUBSCRIBE");
		subscribe.setHeader("Expires", "200");
		subscribe.setHeader("Event", "reg; id=1");
		try {			
			subscribe.send();
		} catch (IOException e) {
			logger.error(e);
		}				
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
			subscriberRequest.setHeader("Event", request.getHeader("Event"));
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
		TimerService timerService = (TimerService)ce.getServletContext().getAttribute(TIMER_SERVICE);
		sipApplicationSession.setAttribute("sipFactory", sipFactory);
		ServletTimer servletTimer = timerService.createTimer(sipApplicationSession, 2000, false, null);
		ce.getServletContext().setAttribute("servletTimer", servletTimer);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
	 */
	public void timeout(ServletTimer timer) {
		SipFactory sipFactory = (SipFactory) timer.getApplicationSession().getAttribute("sipFactory");		
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		SipURI fromURI = sipFactory.createSipURI("BigGuy", "here.com");			
		SipURI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, "SUBSCRIBE", fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
		sipServletRequest.setRequestURI(requestURI);
		sipServletRequest.setHeader("Expires", "200");
		sipServletRequest.setHeader("Event", "reg; id=2");
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}		
		sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, "SUBSCRIBE", fromURI, toURI);		
		sipServletRequest.setRequestURI(requestURI);
		sipServletRequest.setHeader("Expires", "200");
		sipServletRequest.setHeader("Event", "reg; id=1");
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}		
	}

	public void sessionCreated(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		logger.info("sip session expired " +  se.getSession());
		
		try {
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipFactory.createApplicationSession(), 
					"MESSAGE", 
					se.getSession().getLocalParty(), 
					se.getSession().getRemoteParty());
			SipURI sipUri=sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(SIP_SESSION_READY_TO_BE_INVALIDATED.length());
			sipServletRequest.setContent(SIP_SESSION_READY_TO_BE_INVALIDATED, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
}