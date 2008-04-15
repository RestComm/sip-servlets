package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
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
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ListenersSipServlet
		extends SipServlet 
		implements SipErrorListener, SipServletListener, 
		SipSessionListener, SipSessionActivationListener, SipSessionAttributeListener, SipSessionBindingListener,
		SipApplicationSessionListener, SipApplicationSessionActivationListener, SipApplicationSessionAttributeListener, SipApplicationSessionBindingListener {

	private static Log logger = LogFactory.getLog(ListenersSipServlet.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	private static final String OK = "OK";
	private static final String KO = "KO";

	private static final String SIP_SESSION_CREATED = "sipSessionCreated";
	private static final String NO_ACK_RECEIVED = "noAckReceived";
	private static final String NO_PRACK_RECEIVED = "noAckReceived";
	private static final String SIP_SERVLET_INITIALIZED = "sipServletInitialized";
	private static final String SIP_SESSION_DESTROYED = "sipSessionDestroyed";

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
			sipFactory = (SipFactory) envCtx.lookup("sip/SipFactory");
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
//		SipServletResponse ringingResponse = request.createResponse(SipServletResponse.SC_RINGING);
//		ringingResponse.send();
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
			if(SIP_SESSION_CREATED.equals(message)) {
				SipServletRequest responseMessage = request.getSession().createRequest("MESSAGE");
//				SipServletRequest responseMessage = sipFactory.createRequest(
//						request.getApplicationSession(),
//						"MESSAGE", 
//						request.getTo().toString(), 
//						request.getFrom().toString());
//				SipURI requestURI = sipFactory.createSipURI("sender", "127.0.0.1:5080");
//				responseMessage.setRequestURI(requestURI);
				responseMessage.setContentLength(2);
				if(request.getSession().getAttribute(SIP_SESSION_CREATED) != null) {
					responseMessage.setContent(OK, CONTENT_TYPE);
				} else {
					responseMessage.setContent(KO, CONTENT_TYPE);
				}
				responseMessage.send();
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("the encoding is not supported", e);
		} catch (IOException e) {
			logger.error("an IO exception occured", e);
		} 
//		catch (ServletParseException e) {
//			logger.error("a parse exception occured", e);
//		}
		
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
		se.getSession().setAttribute(SIP_SESSION_DESTROYED, OK);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionDidActivate(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionWillPassivate(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionAttributeListener#attributeAdded(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void attributeAdded(SipSessionBindingEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionAttributeListener#attributeRemoved(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void attributeRemoved(SipSessionBindingEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionAttributeListener#attributeReplaced(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void attributeReplaced(SipSessionBindingEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionBindingListener#valueBound(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void valueBound(SipSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionBindingListener#valueUnbound(javax.servlet.sip.SipSessionBindingEvent)
	 */
	public void valueUnbound(SipSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionListener#sessionCreated(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionCreated(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionListener#sessionDestroyed(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionListener#sessionExpired(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionExpired(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionDidActivate(SipApplicationSessionEvent se) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionWillPassivate(SipApplicationSessionEvent se) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionAttributeListener#attributeAdded(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void attributeAdded(SipApplicationSessionBindingEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionAttributeListener#attributeRemoved(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void attributeRemoved(SipApplicationSessionBindingEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionAttributeListener#attributeReplaced(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void attributeReplaced(SipApplicationSessionBindingEvent ev) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionBindingListener#valueBound(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void valueBound(SipApplicationSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionBindingListener#valueUnbound(javax.servlet.sip.SipApplicationSessionBindingEvent)
	 */
	public void valueUnbound(SipApplicationSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}
}
