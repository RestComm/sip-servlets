package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CallForwardingB2BUASipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(CallForwardingB2BUASipServlet.class);
	B2buaHelper helper = null;
	
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
		logger.info(request.getFrom().getURI().toString());
		if(request.getFrom().getURI().toString().indexOf("sip:forward-sender@sip-servlets.com") != -1) {
			helper = request.getB2buaHelper();
			
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
					SIP_FACTORY);
			
			Map<String, Set<String>> headers=new HashMap<String, Set<String>>();
			Set<String> toHeaderSet = new HashSet<String>();
			toHeaderSet.add("sip:forward-receiver@sip-servlets.com");
			headers.put("To", toHeaderSet);
			
			SipServletRequest forkedRequest = helper.createRequest(request, true,
					headers);
			SipURI sipUri = (SipURI) sipFactory.createURI("sip:forward-receiver@127.0.0.1:5090");		
			forkedRequest.setRequestURI(sipUri);						
			
			logger.info("forkedRequest = " + forkedRequest);
			
			forkedRequest.send();
		}
	}	
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got BYE: "
				+ request.getMethod());
		//we send the OK directly to the first call leg
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		
		//we forward the BYE
//		SipSession linkedSession = helper.getLinkedSession(request.getSession());		
//		SipServletRequest forkedRequest = helper.createRequest(
//				linkedSession,
//				request, 
//				null);
//		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
//				SIP_FACTORY);
//		SipURI sipUri = (SipURI) sipFactory.createURI("sip:127.0.0.1:5090");				
//		forkedRequest.setRequestURI(sipUri);
		SipSession session = request.getSession();		
		SipSession linkedSession = helper.getLinkedSession(session);
		SipServletRequest forkedRequest = linkedSession.createRequest("BYE");
		
		logger.info("forkedRequest = " + forkedRequest);
		
		forkedRequest.send();
	}	
	
	/**
	 * {@inheritDoc}
	 */
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());		
		
		String cSeqValue = sipServletResponse.getHeader("CSeq");
		//if this is a response to an INVITE we ack it and forward the OK 
		if(cSeqValue.indexOf("INVITE") != -1) {
			SipServletRequest ackRequest = sipServletResponse.createAck();
			ackRequest.send();
			//create and sends OK for the first call leg
			SipSession originalSession =   
			    helper.getLinkedSession(sipServletResponse.getSession());					
			SipServletResponse responseToOriginalRequest = 
				helper.createResponseToOriginalRequest(originalSession, sipServletResponse.getStatus(), "OK");
			responseToOriginalRequest.send();
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