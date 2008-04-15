package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CallForwardingSipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(CallForwardingSipServlet.class);
	
	
	/** Creates a new instance of CallForwardingSipServlet */
	public CallForwardingSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding sip servlet has been started");
		super.init(servletConfig);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());
		List<SipURI> outboundInterfaces = (List<SipURI>) getServletContext().getAttribute(OUTBOUND_INTERFACES);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		SipURI sipURI = sipFactory.createSipURI("forward-receiver", "127.0.0.1:5090");		
		Proxy proxy = request.getProxy();
		proxy.setOutboundInterface(outboundInterfaces.get(0));
		proxy.setRecordRoute(true);
		proxy.getRecordRouteURI().setParameter("testparamname", "TESTVALUE");		
		proxy.proxyTo(sipURI);		
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