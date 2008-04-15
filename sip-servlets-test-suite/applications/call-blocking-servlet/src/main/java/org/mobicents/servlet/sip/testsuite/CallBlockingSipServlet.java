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
		blockedUris.add("sip:blocked-sender@127.0.0.1");
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n"
				+ request.toString());
		String fromUri = request.getFrom().getURI().toString();
		logger.info(fromUri);
		
		if(blockedUris.contains(fromUri)) {
			logger.info(fromUri + " has been blocked !");
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
			sipServletResponse.send();		
		} else {
			logger.info(fromUri + " has not been blocked.");
		}
	}

	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Call Blocking doesn't handle BYE requests");
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