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
package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.SipApplicationSession.Protocol;

import org.apache.log4j.Logger;

public class Click2DialSipServlet extends SipServlet implements SipApplicationSessionListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(Click2DialSipServlet.class);
	private static final String SIP_APP_SESSION_DESTROYED = "sipAppSessionDestroyed";
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	@Resource
	private SipFactory sipFactory;	
	
	public Click2DialSipServlet() {
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		super.init(servletConfig);
		logger.info("the click to dial servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			SipFactory sipFactory2 = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.Click2DialApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory2);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
		logger.info("Sip Factory ref from Annotations: " + sipFactory);
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
		
		if (resp.getStatus() == SipServletResponse.SC_OK && resp.getMethod().equals("INVITE")) {
			if(resp.getFrom().toString().contains("sipAppTest")) {
				resp.createAck().send();
			} else {
				SipSession session = resp.getSession();
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
					if (contentType != null && contentType.trim().equals("application/sdp")) {
						invite.setContent(resp.getContent(), "application/sdp");
					}
	
					session.setAttribute("LinkedSession", invite.getSession());
					invite.getSession().setAttribute("LinkedSession", session);
	
					SipServletRequest ack = resp.createAck();
					invite.getSession().setAttribute("FirstPartyAck", ack);
	
					invite.send();
	
					session.setAttribute("InviteSent", Boolean.TRUE);
				} else {
					String cSeqValue = resp.getHeader("CSeq");
					if(cSeqValue.indexOf("INVITE") != -1) {				
						logger.info("Got OK from second party -- sending ACK");
		
						SipServletRequest secondPartyAck = resp.createAck();
						SipServletRequest firstPartyAck = (SipServletRequest) resp
								.getSession().getAttribute("FirstPartyAck");
		
						if (resp.getContentType() != null && resp.getContentType().equals("application/sdp")) {
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
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got bye");
		SipSession session = request.getSession();		
		SipServletResponse ok = request
				.createResponse(SipServletResponse.SC_OK);
		ok.send();
		if(!request.getTo().toString().contains("sipAppTest")) {			
			SipSession linkedSession = (SipSession) session
					.getAttribute("LinkedSession");
			if (linkedSession != null) {
				SipServletRequest bye = linkedSession.createRequest("BYE");
				logger.info("Sending bye to " + linkedSession.getRemoteParty());
				bye.send();
			}
		} else {
			if(session.isValid() && session.isReadyToInvalidate()) {
				String httpSessionId = (String) session.getAttribute("invalidateHttpSession"); 
				if(httpSessionId != null) {
					HttpSession httpSession = (HttpSession) session.getApplicationSession().getSession(httpSessionId, Protocol.HTTP);
					httpSession.invalidate();
				}
				session.invalidate();
			}
		}
	}


	protected void doRegister(SipServletRequest req) 
	throws ServletException, IOException 
	{
		logger.info("Received register request: " + req.getTo());
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}

	public void sessionCreated(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipApplicationSessionEvent ev) {
		logger.info("sip application session destroyed " +  ev.getApplicationSession());
//		SipFactory storedFactory = (SipFactory)ev.getApplicationSession().getAttribute("sipFactory");
		if(sipFactory != null) {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			try {
				SipServletRequest sipServletRequest = sipFactory .createRequest(
						sipApplicationSession, 
						"MESSAGE", 
						"sip:sender@sip-servlets.com", 
						"sip:receiver@sip-servlets.com");
				SipURI sipUri=sipFactory.createSipURI("receiver", "127.0.0.1:5057");
				sipServletRequest.setRequestURI(sipUri);
				sipServletRequest.setContentLength(SIP_APP_SESSION_DESTROYED.length());
				sipServletRequest.setContent(SIP_APP_SESSION_DESTROYED, CONTENT_TYPE);
				sipServletRequest.send();
			} catch (ServletParseException e) {
				logger.error("Exception occured while parsing the addresses",e);
			} catch (IOException e) {
				logger.error("Exception occured while sending the request",e);			
			}
		}
	}

	public void sessionExpired(SipApplicationSessionEvent ev) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
		
	}

}