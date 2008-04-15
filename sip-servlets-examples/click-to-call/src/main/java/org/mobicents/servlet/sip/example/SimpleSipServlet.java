package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.Properties;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleSipServlet extends SipServlet implements SipErrorListener,
		Servlet {
	private static Log logger = LogFactory.getLog(SimpleSipServlet.class);
	private SipFactory sipFactory;
	
	public SimpleSipServlet() {
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the simple sip servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
	}
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Click2Dial don't handle INVITE. Here's the one we got :  " + req.toString());
		
	}
	
	@Override
	protected void doOptions(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Got :  " + req.toString());
		req.createResponse(SipServletResponse.SC_OK).send();
	}
	
	@Override
    protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got " + resp.toString());
//		SipFactory sf = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		SipSession session = resp.getSession();

		if (resp.getStatus() == SipServletResponse.SC_OK) {

			Boolean inviteSent = (Boolean) session.getAttribute("InviteSent");
			if (inviteSent != null && inviteSent.booleanValue()) {
				return;
			}
			Address secondPartyAddress = (Address) resp.getSession()
					.getAttribute("SecondPartyAddress");
			if (secondPartyAddress != null) {

				SipServletRequest invite = sipFactory.createRequest(resp
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
//				contentType = resp.getContentType();
//				if (contentType.trim().equals("application/sdp")) {
//					ack.setContent(resp.getContent(), "application/sdp");
//				}
				invite.getSession().setAttribute("FirstPartyAck", ack);
				invite.getSession().setAttribute("FirstPartyContent", resp.getContent());
				
				invite.send();

				session.setAttribute("InviteSent", Boolean.TRUE);
			} else {
				String cSeqValue = resp.getHeader("CSeq");
				if(cSeqValue.indexOf("INVITE") != -1) {				
					logger.info("Got OK from second party -- sending ACK");
	
					SipServletRequest secondPartyAck = resp.createAck();
					SipServletRequest firstPartyAck = (SipServletRequest) resp
							.getSession().getAttribute("FirstPartyAck");
	
//					if (resp.getContentType() != null && resp.getContentType().equals("application/sdp")) {
						firstPartyAck.setContent(resp.getContent(),
								"application/sdp");
						secondPartyAck.setContent(resp.getSession().getAttribute("FirstPartyContent"),
								"application/sdp");
//					}
					logger.info("First ACK "+ firstPartyAck.toString());
					firstPartyAck.send();
					logger.info("Second ACK "+ secondPartyAck.toString());
					secondPartyAck.send();
				}
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