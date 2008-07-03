/**
 * 
 */
package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

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
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.listeners.DTMFListener;
import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
import org.jboss.mobicents.seam.util.DTMFUtils;
import org.jboss.mobicents.seam.util.TTSUtils;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.impl.MsPeerFactory;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ShoppingSipServlet 
	extends SipServlet  
	implements TimerListener {
	
	private static Log logger = LogFactory.getLog(ShoppingSipServlet.class);
	private static final String CONTACT_HEADER = "Contact";
	
	/** Creates a new instance of ShoppingSipServlet */
	public ShoppingSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the shopping sip servlet has been started");
		super.init(servletConfig);
	}		
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			//send ack
			SipServletRequest ackRequest = sipServletResponse.createAck();			
			ackRequest.send();
			//creates the connection
			Object sdpObj = sipServletResponse.getContent();
			byte[] sdpBytes = (byte[]) sdpObj;
			String sdp = new String(sdpBytes);
			sipServletResponse.getSession().getApplicationSession().setAttribute("audioFilePath", (String)getServletContext().getAttribute("audioFilePath"));
			MsConnection connection = (MsConnection)sipServletResponse.getSession().getApplicationSession().getAttribute("connection");
			connection.modify("$", sdp);			
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
			if(!"true".equals(response.getApplicationSession().getAttribute("FirstResponseRecieved")))
			{
				SipApplicationSession sipApplicationSession = response.getApplicationSession();
				sipApplicationSession.setAttribute("FirstResponseRecieved", "true");
				AuthInfo authInfo = sipFactory.createAuthInfo();
				authInfo.addAuthInfo(
						response.getStatus(), 
						response.getChallengeRealms().next(), 
						getServletContext().getInitParameter("caller.sip"), 
						getServletContext().getInitParameter("caller.password"));
				
				SipServletRequest challengeRequest = response.getSession().createRequest(
						response.getRequest().getMethod());
				
				challengeRequest.addAuthHeader(response, authInfo);
				logger.info("Sending the challenge request " + challengeRequest);
				
				MsConnection connection =  (MsConnection) 
					sipApplicationSession.getAttribute("connection");
				String sdp = connection.getLocalDescriptor();
				try {
					challengeRequest.setContentLength(sdp.length());
					challengeRequest.setContent(sdp.getBytes(), "application/sdp");
					logger.info("sending challenge request " + challengeRequest);
					challengeRequest.send();
				} catch (IOException e) {
					logger.error("An unexpected exception occured while sending the request", e);
				}
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
				String pathToAudioDirectory = (String)getServletContext().getAttribute("audioFilePath");
				logger.info("Signal received " + signal );													
				
				if(request.getSession().getApplicationSession().getAttribute("orderApproval") != null) {
					if(request.getSession().getApplicationSession().getAttribute("adminApproval") != null) {
						logger.info("customer approval in progress.");
						DTMFUtils.adminApproval(request.getSession(), signal, pathToAudioDirectory);
					} else {
						logger.info("customer approval in progress.");
						DTMFUtils.orderApproval(request.getSession(), signal, pathToAudioDirectory);
					}
				} else if(request.getSession().getApplicationSession().getAttribute("deliveryDate") != null) {
					DTMFUtils.updateDeliveryDate(request.getSession(), signal);
				} 
			}
		} else {
			logger.info("DTMF session in stopped state, not parsing message content");
		}
	}	
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got bye " + request);		
		SipServletResponse ok = request
				.createResponse(SipServletResponse.SC_OK);
		ok.send();
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
		SipFactory sipFactory = (SipFactory) sipApplicationSession.getAttribute("sipFactory");
		try {			
			String callerAddress = (String)sipApplicationSession.getAttribute("caller");
			String callerDomain = (String)sipApplicationSession.getAttribute("callerDomain");
			SipURI fromURI = sipFactory.createSipURI(callerAddress, callerDomain);
			Address fromAddress = sipFactory.createAddress(fromURI);
			String customerName = (String) sipApplicationSession.getAttribute("customerName");
			BigDecimal amount = (BigDecimal) sipApplicationSession.getAttribute("amountOrder");
			String customerPhone = (String) sipApplicationSession.getAttribute("customerPhone");
			String customerContact = (String) sipApplicationSession.getAttribute("customerContact");
			if(sipApplicationSession.getAttribute("adminApproval") != null) {
				customerContact = (String) sipApplicationSession.getAttribute("adminContactAddress");
				//TTS file creation		
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(customerName);
				stringBuffer.append(" has placed an order of $");
				stringBuffer.append(amount);
				stringBuffer.append(". Press 1 to approve and 2 to reject.");				
				
				TTSUtils.buildAudio(stringBuffer.toString(), "adminspeech.wav");
				Thread.sleep(300);
			}			
			Address toAddress = sipFactory.createAddress(customerPhone);									
			logger.info("preparing to call : "+ toAddress);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(customerContact);			
			sipServletRequest.setRequestURI(requestURI);
														
			//Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer();
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection("media/trunk/IVR/1");
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
			connection.addConnectionListener(listener);
			sipApplicationSession.setAttribute("connection", connection);
			connection.modify("$", null);
		} catch (Exception e) {
			logger.error("An unexpected exception occured while creating the request for delivery date", e);
		}
	}
}
