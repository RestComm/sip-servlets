package org.mobicents.servlet.sip.example;

import java.io.IOException;


import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleSipServlet extends SipServlet implements SipErrorListener,
		Servlet {
	private static Log logger = LogFactory.getLog(SimpleSipServlet.class);
	
	public SimpleSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println("the simple sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("SimpleProxyServlet: Got request:\n"
				+ request.getMethod());

		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		URI uri = sipFactory.createAddress("sip:aa@127.0.0.1:5050").getURI();
		Proxy proxy = request.getProxy();
		proxy.setOutboundInterface((SipURI)sipFactory.createAddress("sip:proxy@127.0.0.1:5070").getURI());
		proxy.proxyTo(uri);
	
	}
	
	@Override
    protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got OK");
		SipFactory sf = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		SipSession session = resp.getSession();

		if (resp.getStatus() == SipServletResponse.SC_OK) {

			Boolean inviteSent = (Boolean) session.getAttribute("InviteSent");
			if (inviteSent != null && inviteSent.booleanValue()) {
				return;
			}
			Address secondPartyAddress = (Address) resp.getSession()
					.getAttribute("SecondPartyAddress");
			if (secondPartyAddress != null) {

				SipServletRequest invite = sf.createRequest(resp
						.getApplicationSession(), "INVITE", session
						.getRemoteParty(), secondPartyAddress);

				logger.info("Found second party -- sending INVITE to "
						+ secondPartyAddress);

				String contentType = resp.getContentType();
				if (contentType.trim().equals("application/sdp")) {
					invite.setContent(resp.getContent(), "application/sdp");
				}

				session.setAttribute("LinkedSession", invite.getSession());
				invite.getSession().setAttribute("LinkedSession", session);

				SipServletRequest ack = resp.createAck();
				invite.getSession().setAttribute("FirstPartyAck", ack);

				invite.send();

				session.setAttribute("InviteSent", Boolean.TRUE);
			} else {
				logger.info("Got OK from second party -- sending ACK");

				SipServletRequest secondPartyAck = resp.createAck();
				SipServletRequest firstPartyAck = (SipServletRequest) resp
						.getSession().getAttribute("FirstPartyAck");

				if (resp.getContentType().equals("application/sdp")) {
					firstPartyAck.setContent(resp.getContent(),
							"application/sdp");
					secondPartyAck.setContent(resp.getContent(),
							"application/sdp");
				}

				firstPartyAck.send();
				secondPartyAck.send();
			}
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got bye");

		SipSession session = request.getSession();

		SipSession linkedSession = (SipSession) session
				.getAttribute("LinkedSession");
		if (linkedSession != null) {
			SipServletRequest bye = linkedSession.createRequest("BYE");

			logger.info("Sending bye to " + linkedSession.getRemoteParty());

			bye.send();
		}

		SipServletResponse ok = request
				.createResponse(SipServletResponse.SC_OK);
		ok.send();
	}
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		logger.info("SimpleProxyServlet: Got response:\n" + response);
		super.doResponse(response);
	}


	/**
	 * {@inheritDoc}
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.info("SimpleProxyServlet: Error: noAckReceived.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.info("SimpleProxyServlet: Error: noPrackReceived.");
	}
	
	protected void doRegister(SipServletRequest req) 
	throws ServletException, IOException 
	{
		logger.info("Received register request: " + req.getTo());
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}

}