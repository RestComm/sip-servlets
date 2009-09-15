/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;

public class SimpleSipServlet extends SipServlet implements SipErrorListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SimpleSipServlet.class);
	private static final String CONTACT_HEADER = "Contact";
	private SipFactory sipFactory;
	@Resource
	private SipFactory sipFactory2;
	
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
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.example.SimpleApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);			
		}
		logger.info("Sip Factory ref from annotation : " + sipFactory2);
		logger.info("Sip Factory ref from servlet context : " + getServletContext().getAttribute(SipServlet.SIP_FACTORY));
		logger.info("init param from servlet context : " + getServletContext().getInitParameter("foo"));
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
		logger.info("Got OK");
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
				invite.getSession().setAttribute("FirstPartyAck", ack);
				invite.getSession().setAttribute("FirstPartyContent", resp.getContent());
				
				Call call = (Call) session.getAttribute("call");
				
				// The call links the two sessions, add the new session to the call
				call.addSession(invite.getSession());
				invite.getSession().setAttribute("call", call);
				
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
	
					firstPartyAck.send();
					secondPartyAck.send();
				}
			}
		}
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse resp) throws ServletException,
			IOException {
		// If someone rejects it remove the call from the table
		CallStatusContainer calls = (CallStatusContainer) getServletContext().getAttribute("activeCalls");
		calls.removeCall(resp.getFrom().getURI().toString(), resp.getTo().getURI().toString());
		calls.removeCall(resp.getTo().getURI().toString(), resp.getFrom().getURI().toString());

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
		CallStatusContainer calls = (CallStatusContainer) getServletContext().getAttribute("activeCalls");
		calls.removeCall(request.getFrom().getURI().toString(), request.getTo().getURI().toString());
		calls.removeCall(request.getTo().getURI().toString(), request.getFrom().getURI().toString());
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
	
	protected void doRegister(SipServletRequest req) throws ServletException, IOException {
		logger.info("Received register request: " + req.getTo());
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		HashMap<String, String> users = (HashMap<String, String>) getServletContext().getAttribute("registeredUsersMap");
		if(users == null) users = new HashMap<String, String>();
		getServletContext().setAttribute("registeredUsersMap", users);
		
		Address address = req.getAddressHeader(CONTACT_HEADER);
		String fromURI = req.getFrom().getURI().toString();
		
		int expires = address.getExpires();
		if(expires < 0) {
			expires = req.getExpires();
		}
		if(expires == 0) {
			users.remove(fromURI);
			logger.info("User " + fromURI + " unregistered");
		} else {
			resp.setAddressHeader(CONTACT_HEADER, address);
			users.put(fromURI, address.getURI().toString());
			logger.info("User " + fromURI + 
					" registered with an Expire time of " + expires);
		}				
						
		resp.send();
	}

}