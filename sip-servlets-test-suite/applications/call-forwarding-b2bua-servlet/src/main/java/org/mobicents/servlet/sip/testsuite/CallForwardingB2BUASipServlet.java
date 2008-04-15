package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CallForwardingB2BUASipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(CallForwardingB2BUASipServlet.class);
	
	
	/** Creates a new instance of CallForwardingB2BUASipServlet */
	public CallForwardingB2BUASipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding B2BUA sip servlet has been started");
		super.init(servletConfig);
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got ACK: "
				+ request.getMethod());
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_MOVED_TEMPORARILY);
		SipURI sipUri= sipFactory.createSipURI("forward-receiver", "127.0.0.1:5090");		
		sipServletResponse.addHeader("Contact", sipUri.toString());		
		sipServletResponse.send();
	}
	
	// SipErrorListener methods
	/**
	 * {@inheritDoc}
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
	}

}