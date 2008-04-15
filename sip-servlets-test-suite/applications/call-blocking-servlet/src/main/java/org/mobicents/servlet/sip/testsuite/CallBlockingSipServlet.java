package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CallBlockingSipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(CallBlockingSipServlet.class);
	List<String> blockedUris = null;
	
	/** Creates a new instance of CallBlockingSipServlet */
	public CallBlockingSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call blocking sip servlet has been started");
		super.init(servletConfig);
		blockedUris = new ArrayList<String>();
		blockedUris.add("sip:blocked-sender@sip-servlets.com");
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n"
				+ request.getMethod());
		logger.info(request.getFrom().getURI().toString());
		
		if(blockedUris.contains(request.getFrom().getURI().toString())) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
			sipServletResponse.send();		
		}		
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