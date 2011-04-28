/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.mobicents.servlet.sip.alerting;

import java.io.IOException;
import java.util.HashMap;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
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
import org.mobicents.servlet.sip.alerting.util.DTMFListener;
import org.mobicents.servlet.sip.alerting.util.DTMFUtils;
import org.mobicents.servlet.sip.alerting.util.MMSUtil;
import org.mobicents.servlet.sip.alerting.util.MediaConnectionListener;

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
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		int status = sipServletResponse.getStatus();
		Boolean isHelpCall = (Boolean) sipServletResponse.getSession().getAttribute("HelpCall");
		if (status == SipServletResponse.SC_SESSION_PROGRESS && 
				"INVITE".equalsIgnoreCase(sipServletResponse.getMethod()) && 
				(isHelpCall == null || Boolean.FALSE.equals(isHelpCall))) {
			//creates the connection
			Object sdpObj = sipServletResponse.getContent();
			byte[] sdpBytes = (byte[]) sdpObj;
			String sdp = new String(sdpBytes);
			logger.info("Creating the end to end media connection");
			sipServletResponse.getSession().setAttribute("playAnnouncement", Boolean.FALSE);
			sipServletResponse.getSession().setAttribute("audioFilePath", (String)getServletContext().getAttribute("audioFilePath"));
			NetworkConnection connection = (NetworkConnection)sipServletResponse.getSession().getAttribute("connection");
			try {
				connection.getSdpPortManager().processSdpAnswer(sdpBytes);
			} catch (SdpPortManagerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			NetworkConnection connection = (NetworkConnection)sipServletResponse.getSession().getAttribute("connection");
			try {
				connection.getSdpPortManager().processSdpAnswer(sdpBytes);
				Thread.sleep(400);
			} catch (SdpPortManagerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MsControlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MediaGroup mg =(MediaGroup)sipServletResponse.getSession().getAttribute("mediaGroup");
			if(mg != null) {
				logger.info("Playing Alerting TTS");
				MediaConnectionListener.playAnnouncement(mg, sipServletResponse.getSession());
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
				
				NetworkConnection connection =  (NetworkConnection) 
					sipSession.getAttribute("connection");
				if(connection != null) {
					try {
						SdpPortManager sdpManag = connection.getSdpPortManager();
						sdpManag.generateSdpOffer();
		
		
						byte[] sdpOffer = null;
						int numTimes = 0;
						while(sdpOffer == null && numTimes<10) {
							sdpOffer = sdpManag.getMediaServerSessionDescription();
							Thread.sleep(500);
							numTimes++;
						}
						challengeRequest.setContentLength(sdpOffer.length);
						challengeRequest.setContent(sdpOffer, "application/sdp");						
					} catch (IOException e) {
						logger.error("An unexpected exception occured while sending the request", e);
					} catch (Exception e) {
						logger.error("An unexpected exception occured while creating the request", e);
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
		NetworkConnection connection = (NetworkConnection)request.getSession().getAttribute("connection");
		MediaGroup mg =(MediaGroup)request.getSession().getAttribute("mediaGroup");
		MediaSession session =(MediaSession)request.getSession().getAttribute("mediaSession");
	
		session.release();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SipServletResponse ok = request
				.createResponse(SipServletResponse.SC_OK);
		ok.send();
		
		SipSession linkedSession = (SipSession) request.getSession()
				.getAttribute("LinkedSession");
		if (linkedSession != null) {
			SipServletRequest bye = linkedSession.createRequest("BYE");
			logger.info("Sending bye to " + linkedSession.getRemoteParty());
			bye.send();
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