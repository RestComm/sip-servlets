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
package org.mobicents.servlet.sip.monitoring;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionState;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.servlet.sip.monitoring.util.DTMFListener;
import org.mobicents.servlet.sip.monitoring.util.DTMFUtils;
import org.mobicents.servlet.sip.monitoring.util.MediaLinkListener;

public class PhoneCallSipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(PhoneCallSipServlet.class);
	private static final String CONTACT_HEADER = "Contact";
	private static final String INVITE = "INVITE";
	
	public PhoneCallSipServlet() {
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		logger.info("the PhoneCallSipServlet has been started");	
	}
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Monitoring App don't handle INVITE yet. Here's the one we got :  " + req.toString());
	}
	
	@Override
	protected void doOptions(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Got :  " + req.toString());
		req.createResponse(SipServletResponse.SC_OK).send();
	}
	
	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		int status = sipServletResponse.getStatus();
		
		if (status == SipServletResponse.SC_SESSION_PROGRESS && 
				INVITE.equalsIgnoreCase(sipServletResponse.getMethod())) {
			//creates the connection
			Object sdpObj = sipServletResponse.getContent();
			byte[] sdpBytes = (byte[]) sdpObj;
			String sdp = new String(sdpBytes);
			logger.info("Creating the end to end media connection");
			sipServletResponse.getSession().setAttribute("playAnnouncement", Boolean.FALSE);
			sipServletResponse.getSession().setAttribute("audioFilePath", (String)getServletContext().getAttribute("audioFilePath"));
			MsConnection connection = (MsConnection)sipServletResponse.getSession().getAttribute("connection");
			connection.modify("$", sdp);			
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {			
			// send ack
			SipServletRequest ackRequest = sipServletResponse.createAck();			
			ackRequest.send();
			//creates the connection
			Object sdpObj = sipServletResponse.getContent();
			byte[] sdpBytes = (byte[]) sdpObj;
			String sdp = new String(sdpBytes);					
			MsConnection connection = (MsConnection)sipServletResponse.getSession().getAttribute("connection");
			if(!connection.getState().equals(MsConnectionState.OPEN)) {
				logger.info("Creating the end to end media connection");
				sipServletResponse.getSession().setAttribute("playAnnouncement", Boolean.TRUE);
				connection.modify("$", sdp);				
			} else {
				logger.info("Not Creating the end to end media connection, connection already opened");
				MediaLinkListener.playAnnouncement(
						connection, 
						(MsLink)sipServletResponse.getSession().getAttribute("link"), 
						sipServletResponse.getSession(), 
						(String)sipServletResponse.getSession().getAttribute("fileName"),
						(String)sipServletResponse.getSession().getAttribute("alertId"),
						(String)getServletContext().getAttribute("alert.feedback.url"));
			}						
		}
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse response)
			throws ServletException, IOException {		
			
		logger.info("Got response: " + response);
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		if(response.getStatus() == SipServletResponse.SC_UNAUTHORIZED || 
				response.getStatus() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
			// Avoid re-sending if the auth repeatedly fails.
			if(!"true".equals(response.getSession().getAttribute("FirstResponseRecieved")))
			{
				SipSession sipSession = response.getSession();
				sipSession.setAttribute("FirstResponseRecieved", "true");
				AuthInfo authInfo = sipFactory.createAuthInfo();
				authInfo.addAuthInfo(
						response.getStatus(), 
						response.getChallengeRealms().next(), 
						getServletContext().getInitParameter("caller.sip"), 
						getServletContext().getInitParameter("caller.password"));
				
				SipServletRequest challengeRequest = response.getSession().createRequest(
						response.getRequest().getMethod());
				
				challengeRequest.addAuthHeader(response, authInfo);
				
				MsConnection connection =  (MsConnection) 
					sipSession.getAttribute("connection");
				if(connection != null) {
					String sdp = connection.getLocalDescriptor();
					try {
						challengeRequest.setContentLength(sdp.length());
						challengeRequest.setContent(sdp.getBytes(), "application/sdp");						
					} catch (IOException e) {
						logger.error("An unexpected exception occured while sending the request", e);
					}
				}
				logger.info("sending challenge request " + challengeRequest);
				challengeRequest.send();
			}
		} else {					
			super.doErrorResponse(response);
		}
		
	}

	@Override
	protected void doInfo(SipServletRequest request) throws ServletException,
			IOException {
		//sending OK
		SipServletResponse ok = request.createResponse(SipServletResponse.SC_OK);
		ok.send();
		//Getting the message content
		String messageContent = new String( (byte[]) request.getContent());
		logger.info("got INFO request with following content " + messageContent);
		int signalIndex = messageContent.indexOf("Signal=");
		
		//Playing file only if the DTMF session has been started
		if(DTMFListener.DTMF_SESSION_STARTED == (Integer) request.getSession().getAttribute("DTMFSession")) {
			logger.info("DTMF session in started state, parsing message content");
			if(messageContent != null && messageContent.length() > 0 && signalIndex != -1) {
				String signal = messageContent.substring("Signal=".length(),"Signal=".length()+1).trim();				
				logger.info("Signal received " + signal );													
				
				DTMFUtils.answerBack((String)request.getSession().getAttribute("alertId"), signal, (String)getServletContext().getAttribute("alert.feedback.url"));
			}
		} else {
			logger.info("DTMF session in stopped state, not parsing message content");
		}
	}	
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got bye " + request);
		MsConnection connection = (MsConnection)request.getSession().getAttribute("connection");
		MsLink link = (MsLink)request.getSession().getAttribute("link");
		
		SipServletResponse ok = request
				.createResponse(SipServletResponse.SC_OK);
		ok.send();				
		
		if(connection != null) {
			connection.release();
		} else {
			logger.info("connection not created");
		}
		if(link != null) {
			link.release();	
		} else {
			logger.info("link not created");
		}
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