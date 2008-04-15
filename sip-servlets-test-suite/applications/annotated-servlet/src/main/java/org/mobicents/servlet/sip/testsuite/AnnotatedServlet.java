package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SipListener
@javax.servlet.sip.annotation.SipServlet
public class AnnotatedServlet extends SipServlet implements SipErrorListener,
		Servlet, SipServletListener {

	@Resource
	SipFactory sipFactory;
	
	private static Log logger = LogFactory.getLog(AnnotatedServlet.class);
	
	public AnnotatedServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the simple sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		
		// End the scenario by sending a message to the Tracker.
		try {
			SipApplicationSession appSession = 
        	sipFactory.createApplicationSession(); // Injected factory
        	SipServletRequest req = sipFactory.createRequest(appSession,
        		"INVITE", "sip:from@127.0.0.1:5070", "sip:to@127.0.0.1:5058");
        	req.send();
        } catch (Exception e) {
        	e.printStackTrace();
        }
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
	
	public void servletInitialized(SipServletContextEvent ce) {
		try {

			SipFactory sipFactory = (SipFactory) ce.getServletContext().getAttribute(SIP_FACTORY);
			SipApplicationSession appSession = 
        	sipFactory.createApplicationSession();
        	SipServletRequest req = sipFactory.createRequest(appSession,
        		"INVITE", "sip:from@127.0.0.1:5070", "sip:to@127.0.0.1:5070");
        	req.send();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse response)
			throws ServletException, IOException {
		logger.info("Got response: " + response);
		response.createAck().send();				
	}
	
	@SipApplicationKey
	public static String key(SipServletRequest request) {
		return "working";
	}
}