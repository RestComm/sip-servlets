package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProxySipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(ProxySipServlet.class);
	
	
	/** Creates a new instance of SimpleProxyServlet */
	public ProxySipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the proxy sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n"
				+ request.getMethod());		
		//This is a proxying sample.
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		URI uri = sipFactory.createAddress("sip:receiver@127.0.0.1:5059").getURI();
		Proxy proxy = request.getProxy();
		proxy.setOutboundInterface((SipURI)sipFactory.createAddress("sip:proxy@127.0.0.1:5070").getURI());
		proxy.setRecordRoute(true);
		proxy.getRecordRouteURI().setParameter("testparamname", "TESTVALUE");
		proxy.proxyTo(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request:\n" + request);
		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		logger.info("Got response:\n" + response);
		super.doResponse(response);
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