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

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.actions.OrderManager;
import org.jboss.mobicents.seam.listeners.DTMFListener;
import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
import org.jboss.mobicents.seam.util.DTMFUtils;
import org.jboss.mobicents.seam.util.MMSUtil;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ShoppingSipServlet 
	extends SipServlet  
	implements TimerListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ShoppingSipServlet.class);
	private static final String CONTACT_HEADER = "Contact";
	
	@Resource
	SipFactory sipFactory;
	
	/** Creates a new instance of ShoppingSipServlet */
	public ShoppingSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the shopping sip servlet has been started");
		super.init(servletConfig);
		try {
			InitialContext ctx = new InitialContext();
			OrderManager orderManager = Util.getOrderManager();
			logger.info("Reference to OrderManagerBean " + orderManager);
		} catch (NamingException e) {
			logger.error("An exception occured while retrieving the EJB OrderManager",e);
		}	
	}		
	

	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
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

	protected void doHelpCallSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got OK");
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
			if (contentType.trim().equals("application/sdp")) {
				invite.setContent(resp.getContent(), "application/sdp");
			}

			session.setAttribute("LinkedSession", invite.getSession());
			invite.getSession().setAttribute("LinkedSession", session);

			SipServletRequest ack = resp.createAck();
			invite.getSession().setAttribute("FirstPartyAck", ack);
			invite.getSession().setAttribute("FirstPartyContent", resp.getContent());
			invite.getSession().setAttribute("HelpCall", Boolean.TRUE);
			
			invite.send();

			session.setAttribute("InviteSent", Boolean.TRUE);
		} else {
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
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			Boolean isHelpCall = (Boolean) sipServletResponse.getSession().getAttribute("HelpCall");
			if(isHelpCall != null && Boolean.TRUE.equals(isHelpCall)) {
				//Handle the help call use case
				doHelpCallSuccessResponse(sipServletResponse);
			} else {
				//send ack
				SipServletRequest ackRequest = sipServletResponse.createAck();			
				ackRequest.send();
				//creates the connection
				Object sdpObj = sipServletResponse.getContent();
				byte[] sdpBytes = (byte[]) sdpObj;	
				String pathToAudioDirectory = (String)getServletContext().getAttribute("audioFilePath");
				sipServletResponse.getSession().setAttribute("audioFilePath", pathToAudioDirectory);
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
					logger.info("Not Creating the end to end media connection, connection already opened");
					MediaConnectionListener.playAnnouncement(mg, sipServletResponse.getSession(), pathToAudioDirectory);
				}			
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
	
	@Override	
	protected void doRegister(SipServletRequest req) throws ServletException, IOException {
		logger.info("Received register request: " + req.getTo());
		Map<String, String> users = (Map<String, String>) getServletContext().getAttribute("registeredUsersMap");
			
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);				
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
			//updating the default contact address for the admin
			String adminAddress = (String) getServletContext().getAttribute("admin.sip");
			if(fromURI.equalsIgnoreCase(adminAddress)) {
				getServletContext().setAttribute("admin.sip.default.contact", address.getURI().toString());
			}
			logger.info("User " + fromURI + 
					" registered this contact address " + address + 
					" with an Expire time of " + expires);
		}							
		
		resp.send();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TimerListener#timeout(javax.servlet.sip.ServletTimer)
	 */
	public void timeout(ServletTimer timer) {
		SipApplicationSession sipApplicationSession = timer.getApplicationSession();
		Map<String, Object> attributes = (Map<String, Object>)timer.getInfo();
		try {			
			String callerAddress = (String)attributes.get("caller");
			String callerDomain = (String)attributes.get("callerDomain");
			SipURI fromURI = sipFactory.createSipURI(callerAddress, callerDomain);
			Address fromAddress = sipFactory.createAddress(fromURI);
			String customerName = (String) attributes.get("customerName");
			BigDecimal amount = (BigDecimal) attributes.get("amountOrder");
			String customerPhone = (String) attributes.get("customerPhone");
			String customerContact = (String) attributes.get("customerContact");
			Address toAddress = sipFactory.createAddress(customerPhone);									
			logger.info("preparing to call : "+ toAddress);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			if(attributes.get("adminApproval") != null) {
				logger.info("preparing speech for admin approval");
				customerContact = (String) attributes.get("adminContactAddress");
				//TTS file creation		
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(customerName);
				stringBuffer.append(" has placed an order of $");
				stringBuffer.append(amount);
				stringBuffer.append(". Press 1 to approve and 2 to reject.");				
				sipServletRequest.getSession().setAttribute("speechUri",
						java.net.URI.create("data:" + URLEncoder.encode("ts("+ stringBuffer +")", "UTF-8")));
				
			}			
			

			URI requestURI = sipFactory.createURI(customerContact);			
			sipServletRequest.setRequestURI(requestURI);
							
			//copying the attributes in the sip session since the media listener depends on it
			Iterator<Entry<String,Object>> attrIterator = attributes.entrySet().iterator();
			while (attrIterator.hasNext()) {
				Map.Entry<java.lang.String, java.lang.Object> entry = (Map.Entry<java.lang.String, java.lang.Object>) attrIterator
						.next();
				sipServletRequest.getSession().setAttribute(entry.getKey(), entry.getValue());
			}
			SipSession sipSession = sipServletRequest.getSession();
			
			if (sipSession.getAttribute("deliveryDate") != null) {			
				String announcementFile = MMSUtil.audioFilePath + "/OrderDeliveryDate.wav";
				sipServletRequest.getSession().setAttribute("speechUri",
				java.net.URI.create(announcementFile));
				logger.info("Preparing to play Delivery Date Announcement : " + announcementFile);
			} 
			
			//Media Server Control Creation
			MediaSession mediaSession = MMSUtil.getMsControl().createMediaSession();
			NetworkConnection conn = mediaSession
			.createNetworkConnection(NetworkConnection.BASIC);

			SdpPortManager sdpManag = conn.getSdpPortManager();
			
			sdpManag.generateSdpOffer();
			
			byte[] sdpOffer = null;
			int numTimes = 0;
			while(sdpOffer == null && numTimes<10) {
				sdpOffer = sdpManag.getMediaServerSessionDescription();
				Thread.sleep(500);
				numTimes++;
			}
			MediaGroup mg = mediaSession.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			sipServletRequest.getSession().setAttribute("mediaGroup", mg);
			sipServletRequest.getSession().setAttribute("mediaSession", mediaSession);
			sipServletRequest.setContentLength(sdpOffer.length);
			sipServletRequest.setContent(sdpOffer, "application/sdp");	
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
//			provider.addConnectionListener(listener);
			sipServletRequest.getSession().setAttribute("connection", conn);
			sipServletRequest.send();
			mg.getSignalDetector().addListener(new DTMFListener(mg, sipServletRequest.getSession(), MMSUtil.audioFilePath));
			conn.join(Direction.DUPLEX, mg);
			logger.info("waiting to get the SDP from Media Server before sending the INVITE to " + callerAddress + "@" + callerDomain);
		} catch (Exception e) {
			logger.error("An unexpected exception occured while creating the request for delivery date", e);
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
				String pathToAudioDirectory = (String)getServletContext().getAttribute("audioFilePath");
				logger.info("Signal received " + signal );													
				
				if(request.getSession().getAttribute("orderApproval") != null) {
					if(request.getSession().getAttribute("adminApproval") != null) {
						logger.info("customer approval in progress.");
						DTMFUtils.adminApproval(request.getSession(), signal, pathToAudioDirectory);
					} else {
						logger.info("customer approval in progress.");
						DTMFUtils.orderApproval(request.getSession(), signal, pathToAudioDirectory);
					}
				} else if(request.getSession().getAttribute("deliveryDate") != null) {
					DTMFUtils.updateDeliveryDate(request.getSession(), signal);
				} 
			}
		} else {
			logger.info("DTMF session in stopped state, not parsing message content");
		}
	}	
}
