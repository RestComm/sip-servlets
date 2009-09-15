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
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
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
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SubscriberSipServlet 
		extends SipServlet 
		implements SipServletListener, TimerListener, SipSessionListener {
	private static final long serialVersionUID = 1L;
	private static final String TEST_SAME_CONTAINER_USER_NAME = "sameContainerUserName";
	private static transient Logger logger = Logger.getLogger(SubscriberSipServlet.class);
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
		request.getSession().setAttribute("byeReceived", "true");
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
		if(sipServletResponse.getSession().getAttribute(TEST_SAME_CONTAINER_USER_NAME) != null && "INVITE".equals(sipServletResponse.getMethod())) {
			sipServletResponse.createAck().send();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
			}
			SipServletRequest subscribe = sipServletResponse.getSession().createRequest("SUBSCRIBE");
			subscribe.setHeader("Expires", "200");
			subscribe.setHeader("Event", "reg; id=1");
			try {			
				subscribe.send();
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	@Override
	protected void doNotify(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got notify : "
				+ request.getMethod());			
		String state = request.getHeader("Subscription-State");
		logger.info("state " + state);
		logger.info("session id " + request.getSession().getId());
		logger.info("sameContainerUserName attribute in session " + request.getSession().getAttribute(TEST_SAME_CONTAINER_USER_NAME));
		logger.info("byeReceived attribute in session " + request.getSession().getAttribute("byeReceived"));
						
		if("Active".equalsIgnoreCase(state)) {
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
		if(state != null && state.indexOf("Terminated") != -1 && request.getSession().getAttribute(TEST_SAME_CONTAINER_USER_NAME) != null) {
			if(request.getSession().getAttribute("byeReceived") == null) {
				request.getSession().createRequest("BYE").send();
				SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
				SipURI fromURI = sipFactory.createSipURI("receiver", "sip-servlets.com");
				SipURI requestURI = sipFactory.createSipURI("receiver", "127.0.0.1:5080");
				SipServletRequest sipServletRequest = sipFactory.createRequest(sipApplicationSession, "MESSAGE", fromURI, request.getFrom().getURI());
				String messageContent = "dialogCompleted";
				sipServletRequest.setContentLength(messageContent.length());
				sipServletRequest.setContent(messageContent, CONTENT_TYPE);
				sipServletRequest.setRequestURI(requestURI);		
				sipServletRequest.send();		
			}
		}
		if(getServletContext().getInitParameter("no200OKToNotify") == null) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
		} else {
			logger.info("no sending 200 to Initial Notify as specified by the test configuration");			
		}
	}
	
	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		if(ce.getServletContext().getInitParameter("requestURI") != null) {
			SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			
			URI fromURI = sipFactory.createSipURI("BigGuy", "here.com");
			URI toURI =  sipFactory.createSipURI("LittleGuy", "there.com");
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromURI, toURI);
			SipURI requestURI = sipFactory.createSipURI(TEST_SAME_CONTAINER_USER_NAME, ce.getServletContext().getInitParameter("requestURI"));			
			sipServletRequest.setRequestURI(requestURI);
			sipServletRequest.getSession().setAttribute(TEST_SAME_CONTAINER_USER_NAME, Boolean.TRUE);
			logger.info("session id " + sipServletRequest.getSession().getId());
			try {			
				sipServletRequest.send();
			} catch (IOException e) {
				logger.error("Unexpected exception while sending the INVITE request",e);
			}					
		} else {
			SipFactory sipFactory = (SipFactory)ce.getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			TimerService timerService = (TimerService)ce.getServletContext().getAttribute(TIMER_SERVICE);
			sipApplicationSession.setAttribute("sipFactory", sipFactory);
			ServletTimer servletTimer = timerService.createTimer(sipApplicationSession, 2000, false, null);
			ce.getServletContext().setAttribute("servletTimer", servletTimer);
		}		
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