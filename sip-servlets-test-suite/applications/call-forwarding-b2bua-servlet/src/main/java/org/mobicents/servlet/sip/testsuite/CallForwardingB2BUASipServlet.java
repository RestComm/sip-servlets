package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.sip.header.ToHeader;

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

		logger.info("Got INVITE: "
				+ request.getMethod());		
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
		B2buaHelper helper = request.getB2buaHelper();
		SipServletRequest forkedRequest = helper.createRequest(request, true,
				null);
		SipURI sipUri = (SipURI) sipFactory.createURI("sip:forward-receiver@127.0.0.1:5090");		
		request.setHeader("To", "sip:forward-receiver@sip-servlets.com");
		
		logger.info("forkedRequest = " + forkedRequest);
		
		forkedRequest.setRequestURI(sipUri);
		forkedRequest.send();
	}
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got BYE: "
				+ request.getMethod());
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);
		B2buaHelper helper = request.getB2buaHelper();
		SipServletRequest forkedRequest = helper.createRequest(request, true,
				null);
		SipURI sipUri = (SipURI) sipFactory.createURI("sip:forward-receiver@127.0.0.1:5090");		
		request.setHeader("To", "sip:forward-receiver@sip-servlets.com");
		
		logger.info("forkedRequest = " + forkedRequest);
		
		forkedRequest.setRequestURI(sipUri);
		forkedRequest.send();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());		
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK) {
			String cSeqValue = sipServletResponse.getHeader("CSeq");
			//if this is a BYE we don't need to ack it 
			if(cSeqValue.indexOf("BYE") == -1) {
				SipServletRequest ackRequest = sipServletResponse.createAck();
				ackRequest.send();			
			}
			//create and sends OK for the first call leg
			SipServletRequest originalRequest = sipServletResponse.getRequest();			
			SipServletResponse responseToOriginalRequest = 
				originalRequest.getB2buaHelper().createResponseToOriginalRequest(originalRequest.getSession(), SipServletResponse.SC_OK, "OK");
			responseToOriginalRequest.send();
		} else {
			super.doResponse(sipServletResponse);
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