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

import javax.annotation.Resource;
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

import org.apache.log4j.Logger;


public class ListenersSipServlet
		extends SipServlet 
		implements SipErrorListener, SipServletListener, 
		SipSessionListener, SipSessionActivationListener, SipSessionBindingListener,
		SipApplicationSessionListener, SipApplicationSessionActivationListener, SipApplicationSessionBindingListener,
		SipSessionAttributeListener, SipApplicationSessionAttributeListener, TimerListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ListenersSipServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	private static final String OK = "OK";
	private static final String KO = "KO";
	
	private static final String ATTRIBUTE = "attribute";
	private static final String VALUE = "value";
	private static final String NEW_VALUE = "new_value";
	
	private static final String NO_ACK_RECEIVED = "noAckReceived";
	private static final String NO_PRACK_RECEIVED = "noPrackReceived";
	private static final String SIP_SERVLET_INITIALIZED = "sipServletInitialized";
	private static final String SIP_SESSION_CREATED = "sipSessionCreated";
	private static final String SIP_SESSION_DESTROYED = "sipSessionDestroyed";
	private static final String SIP_APP_SESSION_VALUE_UNBOUND = "sipAppSessionValueUnbound";
	private static final String SIP_APP_SESSION_VALUE_BOUND = "sipAppSessionValueBound";
	private static final String SIP_APP_SESSION_ATTRIBUTE_REPLACED = "sipAppSessionAttributeReplaced";
	private static final String SIP_APP_SESSION_ATTRIBUTE_REMOVED = "sipAppSessionAttributeRemoved";
	private static final String SIP_APP_SESSION_ATTRIBUTE_ADDED = "sipAppSessionAttributeAdded";
	private static final String SIP_APP_SESSION_PASSIVATED = "sipAppSessionPassivated";
	private static final String SIP_APP_SESSION_ACTIVATED = "sipAppSessionActivated";
	private static final String SIP_APP_SESSION_EXPIRED = "sipAppSessionExpired";
	private static final String SIP_APP_SESSION_DESTROYED = "sipAppSessionDestroyed";
	private static final String SIP_APP_SESSION_CREATED = "sipAppSessionCreated";
	private static final String SIP_SESSION_VALUE_UNBOUND = "sipSessionValueUnbound";
	private static final String SIP_SESSION_VALUE_BOUND = "sipSessionValueBound";
	private static final String SIP_SESSION_ATTRIBUTE_REPLACED = "sipSessionAttributeReplaced";
	private static final String SIP_SESSION_ATTRIBUTE_REMOVED = "sipSessionAttributeRemoved";
	private static final String SIP_SESSION_ATTRIBUTE_ADDED = "sipSessionAttributeAdded";
	private static final String SIP_SESSION_PASSIVATED = "sipSessionPassivated";
	private static final String SIP_SESSION_ACTIVATED = "sipSessionActivated";	

	@Resource
	private SipFactory sipFactory;	
	
	
	/** Creates a new instance of ListenersSipServlet */
	public ListenersSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the listeners test sip servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.ListenersApplication/SipFactory");
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
		SipServletResponse ringingResponse = request.createResponse(SipServletResponse.SC_RINGING);
		ringingResponse.send();
		SipServletResponse okResponse = request.createResponse(SipServletResponse.SC_OK);
		okResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request: " + request);
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		//create a timer to invalidate the sessions
		request.getSession().setAttribute("sipFactory", sipFactory);
//		request.getSession().getApplicationSession().setAttribute("sipFactory", sipFactory);
		TimerService timerService = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
		timerService.createTimer(request.getApplicationSession(), 5000, false, null);		
	}	

	@Override
	protected void doMessage(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		processMessage(request);
	}
	
	/**
	 * 
	 * @param request
	 */
	private void processMessage(SipServletRequest request) {
		try {
			String message = (String)request.getContent();
			if(message != null && message.length() > 0) {
				SipServletRequest responseMessage = request.getSession().createRequest("MESSAGE");
				responseMessage.setContentLength(2);
				responseMessage.setContent(KO, CONTENT_TYPE);
				if (hasListenerBeenCalled(request, message)) {
					responseMessage.setContent(OK, CONTENT_TYPE);
				}
				responseMessage.send();
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("the encoding is not supported", e);
		} catch (IOException e) {
			logger.error("an IO exception occured", e);
		} 		
	}

	/**
	 * Check if a particular listener has been called
	 * @param request the request the message has been received on 
	 * @param message the message containing the listener to check
	 * @return true if the listener has been called, flase otherwise
	 */
	private boolean hasListenerBeenCalled(SipServletRequest request, String message) {
		if(SIP_SESSION_CREATED.equals(message) 
				&& request.getSession().getAttribute(message) != null) {
			return true;
		} else if (SIP_SESSION_DESTROYED.equals(message)) {
			request.getSession().invalidate();
			if(request.getSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}
		} if(SIP_APP_SESSION_CREATED.equals(message) 
				&& request.getApplicationSession().getAttribute(message) != null) {
			return true;
		} else if (SIP_APP_SESSION_DESTROYED.equals(message)) {
			request.getApplicationSession().invalidate();
			if(request.getApplicationSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}
		} else if (SIP_SESSION_ATTRIBUTE_ADDED.equals(message) || 
				SIP_SESSION_VALUE_BOUND.equals(message)) {
			request.getSession().setAttribute(ATTRIBUTE, VALUE);
			if(request.getSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}
		}else if (SIP_SESSION_ATTRIBUTE_REMOVED.equals(message) ||
				SIP_SESSION_VALUE_UNBOUND.equals(message)) {
			request.getSession().setAttribute(ATTRIBUTE, VALUE);
			request.getSession().removeAttribute(ATTRIBUTE);
			if(request.getSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}
		} else if (SIP_SESSION_ATTRIBUTE_REPLACED.equals(message)) {
			request.getSession().setAttribute(ATTRIBUTE, VALUE);
			request.getSession().setAttribute(ATTRIBUTE, NEW_VALUE);
			if(request.getSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}			
		} else if (SIP_APP_SESSION_ATTRIBUTE_ADDED.equals(message) || 
				SIP_APP_SESSION_VALUE_BOUND.equals(message)) {
			request.getApplicationSession().setAttribute(ATTRIBUTE, VALUE);
			if(request.getApplicationSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}
		}else if (SIP_APP_SESSION_ATTRIBUTE_REMOVED.equals(message) ||
				SIP_APP_SESSION_VALUE_UNBOUND.equals(message)) {
			request.getApplicationSession().setAttribute(ATTRIBUTE, VALUE);
			request.getApplicationSession().removeAttribute(ATTRIBUTE);
			if(request.getApplicationSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}
		} else if (SIP_APP_SESSION_ATTRIBUTE_REPLACED.equals(message)) {
			request.getApplicationSession().setAttribute(ATTRIBUTE, VALUE);
			request.getApplicationSession().setAttribute(ATTRIBUTE, NEW_VALUE);
			if(request.getApplicationSession().getAttribute(message) != null) {
				return true;
			} else {
				return false;
			}			
		} else if (SIP_SERVLET_INITIALIZED.equals(message)
				&& getServletContext().getAttribute(message) != null) {
			return true;
		}
		return false;
	}

	// Listeners methods	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipErrorListener#noAckReceived(javax.servlet.sip.SipErrorEvent)
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");
		ee.getRequest().getSession().setAttribute(NO_ACK_RECEIVED, OK);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipErrorListener#noPrackReceived(javax.servlet.sip.SipErrorEvent)
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
		ee.getRequest().getSession().setAttribute(NO_PRACK_RECEIVED, OK);	
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		logger.info("servlet initialized ");
		ce.getServletContext().setAttribute(SIP_SERVLET_INITIALIZED, OK);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionListener#sessionCreated(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionCreated(SipSessionEvent se) {
		logger.info("sip session created " +  se.getSession());
		se.getSession().setAttribute(SIP_SESSION_CREATED, OK);		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionListener#sessionDestroyed(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionDestroyed(SipSessionEvent se) {
		logger.info("sip session destroyed " +  se.getSession());
//		SipFactory storedFactory = (SipFactory)se.getSession().getApplicationSession().getAttribute("sipFactory");
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		try {
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(SIP_SESSION_DESTROYED.length());
			sipServletRequest.setContent(SIP_SESSION_DESTROYED, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionDidActivate(SipSessionEvent se) {
		logger.info("sip session activated " +  se.getSession());
		se.getSession().setAttribute(SIP_SESSION_ACTIVATED, OK);			
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionWillPassivate(SipSessionEvent se) {
		logger.info("sip session passivated " +  se.getSession());
		se.getSession().setAttribute(SIP_SESSION_PASSIVATED, OK);
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionAttributeListener#attributeAdded(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void attributeAdded(SipSessionBindingEvent ev) {
		logger.info("sip session attribute added " +  ev.getName());
		if(!SIP_SESSION_ATTRIBUTE_ADDED.equals(ev.getName()) && 
				!SIP_SESSION_VALUE_BOUND.equals(ev.getName()) && 
				!SIP_SESSION_ATTRIBUTE_REPLACED.equals(ev.getName())) { 
			ev.getSession().setAttribute(SIP_SESSION_ATTRIBUTE_ADDED, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionAttributeListener#attributeRemoved(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void attributeRemoved(SipSessionBindingEvent ev) {
		logger.info("sip session attribute removed " +  ev.getName());
		if(!SIP_SESSION_ATTRIBUTE_REMOVED.equals(ev.getName()) && 
				!SIP_SESSION_VALUE_UNBOUND.equals(ev.getName()) && 
				!SIP_SESSION_ATTRIBUTE_REPLACED.equals(ev.getName())) { 
			ev.getSession().setAttribute(SIP_SESSION_ATTRIBUTE_REMOVED, OK);
		}		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionAttributeListener#attributeReplaced(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void attributeReplaced(SipSessionBindingEvent ev) {
		logger.info("sip session attribute removed " +  ev.getName());
		if(!SIP_SESSION_ATTRIBUTE_REPLACED.equals(ev.getName())) {
			ev.getSession().setAttribute(SIP_SESSION_ATTRIBUTE_REPLACED, OK);
		}		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionBindingListener#valueBound(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void valueBound(SipSessionBindingEvent event) {
		logger.info("sip session attribute bound " +  event.getName());
		if(!SIP_SESSION_VALUE_BOUND.equals(event.getName()) && 
				!SIP_SESSION_ATTRIBUTE_ADDED.equals(event.getName()) && 
				!SIP_SESSION_ATTRIBUTE_REPLACED.equals(event.getName())) { 
			event.getSession().setAttribute(SIP_SESSION_VALUE_BOUND, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionBindingListener#valueUnbound(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void valueUnbound(SipSessionBindingEvent event) {
		logger.info("sip session attribute unbound " +  event.getName());
		if(!SIP_SESSION_VALUE_UNBOUND.equals(event.getName()) && 
				!SIP_SESSION_ATTRIBUTE_REMOVED.equals(event.getName()) && 
				!SIP_SESSION_ATTRIBUTE_REPLACED.equals(event.getName())) { 
			event.getSession().setAttribute(SIP_SESSION_VALUE_UNBOUND, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionListener#sessionCreated(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionCreated(SipApplicationSessionEvent ev) {
		logger.info("sip application session created " +  ev.getApplicationSession());
		ev.getApplicationSession().setAttribute(SIP_APP_SESSION_CREATED, OK);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionListener#sessionDestroyed(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		logger.info("sip application session destroyed " +  ev.getApplicationSession());
//		SipFactory storedFactory = (SipFactory)ev.getApplicationSession().getAttribute("sipFactory");
		if(sipFactory != null) {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			try {
				SipServletRequest sipServletRequest = sipFactory .createRequest(
						sipApplicationSession, 
						"MESSAGE", 
						"sip:sender@sip-servlets.com", 
						"sip:receiver@sip-servlets.com");
				SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5080");
				sipServletRequest.setRequestURI(sipUri);
				sipServletRequest.setContentLength(SIP_APP_SESSION_DESTROYED.length());
				sipServletRequest.setContent(SIP_APP_SESSION_DESTROYED, CONTENT_TYPE);
				sipServletRequest.send();
			} catch (ServletParseException e) {
				logger.error("Exception occured while parsing the addresses",e);
			} catch (IOException e) {
				logger.error("Exception occured while sending the request",e);			
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionListener#sessionExpired(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionExpired(SipApplicationSessionEvent ev) {
		logger.info("sip application session expired " +  ev.getApplicationSession());
		ev.getApplicationSession().setAttribute(SIP_APP_SESSION_EXPIRED, OK);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionDidActivate(SipApplicationSessionEvent se) {
		logger.info("sip application session activated " +  se.getApplicationSession());
		se.getApplicationSession().setAttribute(SIP_APP_SESSION_ACTIVATED, OK);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionWillPassivate(SipApplicationSessionEvent se) {
		logger.info("sip application session passivated " +  se.getApplicationSession());
		se.getApplicationSession().setAttribute(SIP_APP_SESSION_PASSIVATED, OK);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionAttributeListener#attributeAdded(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void attributeAdded(SipApplicationSessionBindingEvent ev) {
		logger.info("sip application session attribute added " +  ev.getName());
		if(!SIP_APP_SESSION_ATTRIBUTE_ADDED.equals(ev.getName()) && 
				!SIP_APP_SESSION_VALUE_BOUND.equals(ev.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_REPLACED.equals(ev.getName())) { 
			ev.getApplicationSession().setAttribute(SIP_APP_SESSION_ATTRIBUTE_ADDED, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionAttributeListener#attributeRemoved(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void attributeRemoved(SipApplicationSessionBindingEvent ev) {
		logger.info("sip application session attribute removed " +  ev.getName());		
		if(!SIP_APP_SESSION_ATTRIBUTE_REMOVED.equals(ev.getName()) && 
				!SIP_APP_SESSION_VALUE_UNBOUND.equals(ev.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_REPLACED.equals(ev.getName())) { 
			ev.getApplicationSession().setAttribute(SIP_APP_SESSION_ATTRIBUTE_REMOVED, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionAttributeListener#attributeReplaced(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void attributeReplaced(SipApplicationSessionBindingEvent ev) {
		logger.info("sip application session attribute replaced " +  ev.getName());
		if(!SIP_APP_SESSION_ATTRIBUTE_REMOVED.equals(ev.getName()) && 
				!SIP_APP_SESSION_VALUE_UNBOUND.equals(ev.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_REPLACED.equals(ev.getName())) {
			ev.getApplicationSession().setAttribute(SIP_APP_SESSION_ATTRIBUTE_REPLACED, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionBindingListener#valueBound(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void valueBound(SipApplicationSessionBindingEvent event) {
		logger.info("sip application session value bound " +  event.getName());
		if(!SIP_APP_SESSION_VALUE_BOUND.equals(event.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_ADDED.equals(event.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_REPLACED.equals(event.getName())) { 
			event.getApplicationSession().setAttribute(SIP_APP_SESSION_VALUE_BOUND, OK);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionBindingListener#valueUnbound(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void valueUnbound(SipApplicationSessionBindingEvent event) {
		logger.info("sip application session value unbound " +  event.getName());
		if(!SIP_APP_SESSION_VALUE_UNBOUND.equals(event.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_REMOVED.equals(event.getName()) && 
				!SIP_APP_SESSION_ATTRIBUTE_REPLACED.equals(event.getName())) { 
			event.getApplicationSession().setAttribute(SIP_APP_SESSION_VALUE_UNBOUND, OK);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
	 */
	public void timeout(ServletTimer timer) {
		SipApplicationSession sipApplicationSession = timer.getApplicationSession();
		Iterator<SipSession> sipSessions = (Iterator<SipSession>)
			sipApplicationSession.getSessions("SIP");
		int nbSipSessions = 0;
		while (sipSessions.hasNext()) {
			sipSessions.next();
			nbSipSessions++;
		}
		logger.info("Number of sip sessions contained in the sip application " +
				"session to invalidate " + nbSipSessions);
		sipSessions = (Iterator<SipSession>)
			sipApplicationSession.getSessions("SIP");		
		while (sipSessions.hasNext()) {
			SipSession sipSession = (SipSession) sipSessions.next();
			sipSession.invalidate();
		}		
		sipApplicationSession.invalidate();		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}
}
