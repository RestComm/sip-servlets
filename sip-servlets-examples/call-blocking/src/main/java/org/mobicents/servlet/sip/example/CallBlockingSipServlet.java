package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This examle shows Call Blocking Capabilities. It makes the sip servlet acts as
 * a UAS.
 * Based on the from header it blocks the call if it founds the address in its hard coded black list.
 * 
 * @author Jean Deruelle
 *
 */
public class CallBlockingSipServlet extends SipServlet {

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
}