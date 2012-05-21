/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

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
		implements TimerListener, SipApplicationSessionListener {
	private static final String SIP_APP_SESSION_ACTIVATION_LISTENER = "sipAppSessionActivationListener";
	private static final String SIP_SESSION_ACTIVATION_LISTENER = "sipSessionActivationListener";
	private static final long serialVersionUID = 1L;
	private static final String RECEIVED = "Received";

	private static final Logger logger = Logger.getLogger(DistributableSimpleSipServlet.class);
	
	private static final String CALLEE_SEND_BYE = "yousendbye";	
	private static final String SAS_TIMER_SEND_BYE = "sastimersendbye";
	private static final String CANCEL_SERVLET_TIMER = "cancelservlettimer";
	private static final String INJECTED_TIMER = "injectedtimer";
	
	private static final String NO_ATTRIBUTES = "NoAttributes";
	private static final String REMOVE_ATTRIBUTES = "RemoveAttributes";
	
	//60 sec
	private static final int DEFAULT_BYE_DELAY = 60000;
	private int byeDelay = DEFAULT_BYE_DELAY;
	
	ServletTimer servletTimer = null;
	@Resource TimerService injectedTimerService;
	
	/** Creates a new instance of SimpleProxyServlet */
	public DistributableSimpleSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the distributable simple sip servlet has been started");
		super.init(servletConfig);
		String byeDelayStr = getServletContext().getInitParameter("bye.delay");		
		try{
			byeDelay = Integer.parseInt(byeDelayStr);
		} catch (NumberFormatException e) {
			logger.error("Impossible to parse the bye delay : " + byeDelayStr, e);
		}
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
		if(!(((SipURI)request.getFrom().getURI()).getUser()).contains(NO_ATTRIBUTES)) {
			if(request.isInitial()) { 
				request.getSession().setAttribute("INVITE", RECEIVED);
				request.getSession().setAttribute(SIP_SESSION_ACTIVATION_LISTENER, new SipSessionActivationListenerAttribute());
				request.getApplicationSession().setAttribute("INVITE", RECEIVED);
				request.getApplicationSession().setAttribute(SIP_APP_SESSION_ACTIVATION_LISTENER, new SipApplicationSessionActivationListenerAttribute());
				if(((SipURI)request.getTo().getURI()).getUser().contains("reinvite")) {
					if(logger.isInfoEnabled()) {			
						logger.info("Distributable Simple Servlet: setting isReINVITE");
					}
					request.getSession().setAttribute("ISREINVITE", RECEIVED);
				}				
			} else {
				if((((SipURI)request.getTo().getURI()).getUser()).contains(REMOVE_ATTRIBUTES)) {
					if(logger.isInfoEnabled()) {			
						logger.info("Distributable Simple Servlet: removing isReINVITE");
					}
					request.getSession().removeAttribute("ISREINVITE");
				} else {
					request.getSession().setAttribute("REINVITE", RECEIVED);			
					request.getApplicationSession().setAttribute("REINVITE", RECEIVED);
				}
			}
		}
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		if((((SipURI)request.getTo().getURI()).getUser()).contains(CALLEE_SEND_BYE)) {
			TimerService timer = null;
			if(!(((SipURI)request.getFrom().getURI()).getUser()).contains(INJECTED_TIMER)) {
				if(logger.isInfoEnabled()) {			
					logger.info("Distributable Simple Servlet: using injected timer");
				}
				timer = injectedTimerService;
			} else {
				if(logger.isInfoEnabled()) {			
					logger.info("Distributable Simple Servlet: using servlet context timer");
				}
				timer = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
			}
			SessionIdPlaceHolder sessionIdPlaceHolder = new SessionIdPlaceHolder();
			sessionIdPlaceHolder.setId(request.getSession().getId());
			timer.createTimer(request.getApplicationSession(), byeDelay, false, sessionIdPlaceHolder);
		} else if ((((SipURI)request.getTo().getURI()).getUser()).contains(SAS_TIMER_SEND_BYE)) {
			request.getSession().getApplicationSession().setExpires(1);
		} else if ((((SipURI)request.getTo().getURI()).getUser()).contains(CANCEL_SERVLET_TIMER)) {
			TimerService timer = null;
			if(!(((SipURI)request.getFrom().getURI()).getUser()).contains(INJECTED_TIMER)) {
				if(logger.isInfoEnabled()) {			
					logger.info("Distributable Simple Servlet: using injected timer");
				}
				timer = injectedTimerService;
			} else {
				if(logger.isInfoEnabled()) {			
					logger.info("Distributable Simple Servlet: using servlet context timer");
				}
				timer = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
			}			
			SessionIdPlaceHolder sessionIdPlaceHolder = new SessionIdPlaceHolder();
			sessionIdPlaceHolder.setId(request.getSession().getId());
			servletTimer = timer.createTimer(request.getApplicationSession(), byeDelay, false, sessionIdPlaceHolder);
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
		String sipSessionReInviteAttribute  = (String) request.getSession().getAttribute("REINVITE");
		String sipApplicationSessionReInviteAttribute  = (String) request.getApplicationSession().getAttribute("REINVITE");
		Object sipSessionActivationListener = request.getSession().getAttribute(SIP_SESSION_ACTIVATION_LISTENER);
		Object sipApplicationSessionActivationListener = request.getApplicationSession().getAttribute(SIP_APP_SESSION_ACTIVATION_LISTENER);
		Object sipSessionActivated = request.getSession().getAttribute(SipSessionActivationListenerAttribute.SIP_SESSION_ACTIVATED);
		Object sipApplicationSessionActivated = request.getApplicationSession().getAttribute(SipApplicationSessionActivationListenerAttribute.SIP_APPLICATION_SESSION_ACTIVATED);
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Simple Servlet: attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip session REINVITE : "+ sipSessionReInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session REINVITE : "+ sipApplicationSessionReInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip session sipSessionActivationListener : "+ sipSessionActivationListener);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session sipApplciationSessionActivationListener : "+ sipApplicationSessionActivationListener);
			logger.info("Distributable Simple Servlet: attributes previously set in sip session sipSessionActivated : "+ sipSessionActivated);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session sipApplicationSessionActivated : "+ sipApplicationSessionActivated);
		}
		if(!(((SipURI)request.getFrom().getURI()).getUser()).contains(NO_ATTRIBUTES)) {
			if(sipSessionInviteAttribute != null  && sipApplicationSessionInviteAttribute != null 				
					&& RECEIVED.equalsIgnoreCase(sipSessionInviteAttribute) 
					&& RECEIVED.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {	
				
				if((((SipURI)request.getTo().getURI()).getUser()).contains(REMOVE_ATTRIBUTES)) {
					if(logger.isInfoEnabled()) {			
						logger.info("Distributable Simple Servlet: checking if isReINVITE present in removeattributes case");
					}
					if(request.getSession().getAttribute("ISREINVITE") !=null) {
						SipServletResponse sipServletResponse = request.createResponse(500, "isReinvite attribute should have been removed");
						sipServletResponse.send();
						return;
					}
				}
				if((((SipURI)request.getFrom().getURI()).getUser()).contains("test-activation")) {
					if(sipApplicationSessionActivationListener != null && sipApplicationSessionActivated == null) {
						SipServletResponse sipServletResponse = request.createResponse(500, "sipApplicationSessionActivationListener not called");
						sipServletResponse.send();
						return;
					}
					if(sipSessionActivationListener != null && sipSessionActivated == null) {
						SipServletResponse sipServletResponse = request.createResponse(500, "sipSessionActivationListener not called");
						sipServletResponse.send();
						return;
					}				
				}
				if(request.getSession().getAttribute("ISREINVITE") !=null && sipSessionReInviteAttribute != null  && sipApplicationSessionReInviteAttribute != null 				
						&& RECEIVED.equalsIgnoreCase(sipSessionReInviteAttribute) 
						&& RECEIVED.equalsIgnoreCase(sipApplicationSessionReInviteAttribute)) {
	//				request.getSession().setAttribute("BYE", RECEIVED);
	//				request.getApplicationSession().setAttribute(" BYE", RECEIVED);
					SipServletResponse sipServletResponse = request.createResponse(200);
					sipServletResponse.send();
					return;
				}
	//			request.getSession().setAttribute("BYE", RECEIVED);
	//			request.getApplicationSession().setAttribute(" BYE", RECEIVED);
				SipServletResponse sipServletResponse = request.createResponse(200);
				sipServletResponse.send();
				return;
			}
			SipServletResponse sipServletResponse = request.createResponse(500);
			sipServletResponse.send();
		} else {
			SipServletResponse sipServletResponse = request.createResponse(200);
			sipServletResponse.send();
		}
	}
	
	@Override
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		if ((((SipURI)req.getTo().getURI()).getUser()).contains(CANCEL_SERVLET_TIMER)) {						
			servletTimer.cancel();
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
		SipSession sipSession = servletTimer.getApplicationSession().getSipSession(((SessionIdPlaceHolder)servletTimer.getInfo()).getId());
		if(sipSession != null && !State.TERMINATED.equals(sipSession.getState())) {
			try {
				sipSession.createRequest("BYE").send();
			} catch (IOException e) {
				logger.error("An unexpected exception occured while sending the BYE", e);
			}				
		}
	}

	public void sessionCreated(SipApplicationSessionEvent event) {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: sip app session " + event.getApplicationSession().getId() + " created");
		}
	}

	public void sessionDestroyed(SipApplicationSessionEvent event) {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: sip app session " + event.getApplicationSession().getId() + " destroyed");
		}
	}

	public void sessionExpired(SipApplicationSessionEvent event) {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: sip app session " + event.getApplicationSession().getId() + " expired");
		}		
		if(event.getApplicationSession().isValid()) {
			Iterator<SipSession> sipSessionsIt = (Iterator<SipSession>) event.getApplicationSession().getSessions("SIP");
			if(sipSessionsIt.hasNext()) {
				SipSession sipSession = sipSessionsIt.next();
				if(sipSession != null && sipSession.isValid() && !State.TERMINATED.equals(sipSession.getState())) {
					try {
						sipSession.createRequest("BYE").send();
					} catch (IOException e) {
						logger.error("An unexpected exception occured while sending the BYE", e);
					}				
				}
			}
		}
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent event) {
		if(logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: sip app session " + event.getApplicationSession().getId() + " ready to invalidate");
		}		
	}
}