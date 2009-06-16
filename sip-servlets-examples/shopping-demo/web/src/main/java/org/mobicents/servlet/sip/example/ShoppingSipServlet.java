/**
 * 
 */
package org.mobicents.servlet.sip.example;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
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
import org.jboss.mobicents.seam.listeners.MediaResourceListener;
import org.jboss.mobicents.seam.util.DTMFUtils;
import org.jboss.mobicents.seam.util.TTSUtils;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionState;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ShoppingSipServlet 
	extends SipServlet  
	implements TimerListener {
	
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
		if (status == SipServletResponse.SC_SESSION_PROGRESS && "INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
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
			//send ack
			SipServletRequest ackRequest = sipServletResponse.createAck();			
			ackRequest.send();
			//creates the connection
			Object sdpObj = sipServletResponse.getContent();
			byte[] sdpBytes = (byte[]) sdpObj;
			String sdp = new String(sdpBytes);		
			String pathToAudioDirectory = (String)getServletContext().getAttribute("audioFilePath");
			sipServletResponse.getSession().setAttribute("audioFilePath", pathToAudioDirectory);
			MsConnection connection = (MsConnection)sipServletResponse.getSession().getAttribute("connection");
			if(!connection.getState().equals(MsConnectionState.OPEN)) {
				logger.info("Creating the end to end media connection");
				sipServletResponse.getSession().setAttribute("playAnnouncement", Boolean.TRUE);
				connection.modify("$", sdp);
				
			} else {
				logger.info("Not Creating the end to end media connection, connection already opened");
				MediaConnectionListener.playAnnouncement(connection, (MsLink)sipServletResponse.getSession().getAttribute("link"), sipServletResponse.getSession(), pathToAudioDirectory);
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
				logger.info("Sending the challenge request " + challengeRequest);
				
				MsConnection connection =  (MsConnection) 
					sipSession.getAttribute("connection");
				if(connection != null) {
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
			if(attributes.get("adminApproval") != null) {
				logger.info("preparing speech for admin approval");
				customerContact = (String) attributes.get("adminContactAddress");
				//TTS file creation		
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(customerName);
				stringBuffer.append(" has placed an order of $");
				stringBuffer.append(amount);
				stringBuffer.append(". Press 1 to approve and 2 to reject.");				
				
				TTSUtils.buildAudio(stringBuffer.toString(), "adminspeech.wav");
			}			
			Address toAddress = sipFactory.createAddress(customerPhone);									
			logger.info("preparing to call : "+ toAddress);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(customerContact);			
			sipServletRequest.setRequestURI(requestURI);
							
			//copying the attributes in the sip session since the media listener depends on it
			Iterator<Entry<String,Object>> attrIterator = attributes.entrySet().iterator();
			while (attrIterator.hasNext()) {
				Map.Entry<java.lang.String, java.lang.Object> entry = (Map.Entry<java.lang.String, java.lang.Object>) attrIterator
						.next();
				sipServletRequest.getSession().setAttribute(entry.getKey(), entry.getValue());
			}
			
			//Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection(MediaConnectionListener.PR_JNDI_NAME);
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
//			provider.addConnectionListener(listener);
			connection.addConnectionListener(listener);
			sipServletRequest.getSession().setAttribute("connection", connection);
			connection.modify("$", null);
			logger.info("waiting to get the SDP from Media Server before sending the INVITE to " + callerAddress + "@" + callerDomain);
		} catch (Exception e) {
			logger.error("An unexpected exception occured while creating the request for delivery date", e);
		}
	}
}
