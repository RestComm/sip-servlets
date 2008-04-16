/**
 * 
 */
package org.mobicents.servlet.sip.example;

import java.io.File;
import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.actions.OrderManager;
import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
import org.jboss.mobicents.seam.listeners.MediaResourceListener;
import org.jboss.mobicents.seam.util.TTSUtils;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.MsSignalGenerator;
import org.mobicents.mscontrol.signal.Announcement;
import org.mobicents.mscontrol.signal.Basic;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author Jean Deruelle
 *
 */
public class ShoppingSipServlet 
	extends SipServlet  
	implements TimerListener {
	
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	
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
			sipServletResponse.getSession().getApplicationSession().setAttribute("audio.files.path", (String)getServletContext().getAttribute("audio.files.path"));
			MsConnection connection = (MsConnection)sipServletResponse.getSession().getApplicationSession().getAttribute("connection");
			connection.modify("$", sdp);			
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
		if(DTMF_SESSION_STARTED == (Integer) request.getSession().getAttribute("DTMFSession")) {
			logger.info("DTMF session in started state, parsing message content");
			if(messageContent != null && messageContent.length() > 0 && signalIndex != -1) {
				String signal = messageContent.substring("Signal=".length(),"Signal=".length()+1).trim();
				logger.info("Signal received " + signal );													
				
				if(request.getSession().getApplicationSession().getAttribute("orderApproval") != null) {
					if(request.getSession().getApplicationSession().getAttribute("adminApproval") != null) {
						logger.info("customer approval in progress.");
						adminApproval(request, signal);
					} else {
						logger.info("customer approval in progress.");
						orderApproval(request, signal);
					}
				} else if(request.getSession().getApplicationSession().getAttribute("deliveryDate") != null) {
					updateDeliveryDate(request, signal);
				} 
			}
		} else {
			logger.info("DTMF session in stopped state, not parsing message content");
		}
	}

	private void adminApproval(SipServletRequest request, String signal) {
		String pathToAudioDirectory = (String)getServletContext().getAttribute("audio.files.path");		
		
		if("1".equalsIgnoreCase(signal)) {
			// Order Approved
			logger.info("Order approved !");
			String audioFile = pathToAudioDirectory + "OrderApproved.wav";					
			
			playFileInResponseToDTMFInfo(request.getSession(), audioFile);
//			try {
//				InitialContext ctx = new InitialContext();
//				OrderApproval orderApproval = (OrderApproval) ctx.lookup("shopping-demo/OrderApprovalAction/remote");
//				orderApproval.fireOrderApprovedEvent();
//			} catch (NamingException e) {
//				logger.error("An exception occured while retrieving the EJB OrderApproval",e);
//			}					
		} else if("2".equalsIgnoreCase(signal)) {
			// Order Rejected
			logger.info("Order rejected !");
			String audioFile = pathToAudioDirectory + "OrderCancelled.wav";					
			
			playFileInResponseToDTMFInfo(request.getSession(), audioFile);
//			try {
//				InitialContext ctx = new InitialContext();
//				OrderApproval orderApproval = (OrderApproval) ctx.lookup("shopping-demo/OrderApprovalAction/remote");
//				orderApproval.fireOrderRejectedEvent();
//			} catch (NamingException e) {
//				logger.error("An exception occured while retrieving the EJB OrderApproval",e);
//			}
		}
	}

	private void orderApproval(SipServletRequest request, String signal) {
		String pathToAudioDirectory = (String)getServletContext().getAttribute("audio.files.path");
		long orderId = (Long) request.getSession().getApplicationSession().getAttribute("orderId");
		
		if("1".equalsIgnoreCase(signal)) {
			// Order Confirmed
			logger.info("Order confirmed !");
			String audioFile = pathToAudioDirectory + "OrderApproved.wav";					
			
			playFileInResponseToDTMFInfo(request.getSession(), audioFile);
			try {
				InitialContext ctx = new InitialContext();
				OrderManager orderManager = (OrderManager) ctx.lookup("shopping-demo/OrderManagerBean/remote");
				orderManager.confirmOrder(orderId);
			} catch (NamingException e) {
				logger.error("An exception occured while retrieving the EJB OrderManager",e);
			}					
		} else if("2".equalsIgnoreCase(signal)) {
			// Order cancelled
			logger.info("Order cancelled !");
			String audioFile = pathToAudioDirectory + "OrderCancelled.wav";					
			
			playFileInResponseToDTMFInfo(request.getSession(), audioFile);
			try {
				InitialContext ctx = new InitialContext();
				OrderManager orderManager = (OrderManager) ctx.lookup("shopping-demo/OrderManagerBean/remote");
				orderManager.cancelOrder(orderId);
			} catch (NamingException e) {
				logger.error("An exception occured while retrieving the EJB OrderManager",e);
			}
		}
	}

	private void updateDeliveryDate(SipServletRequest request, String signal) {
		int cause = Integer.parseInt(signal);

		boolean success = false;

		synchronized(request.getSession()) {
			String dateAndTime = (String) request.getSession().getAttribute("dateAndTime");
			if(dateAndTime == null) {
				dateAndTime = "";
			}
	
			switch (cause) {
			case Basic.CAUSE_DIGIT_0:
				dateAndTime = dateAndTime + "0";
				break;
			case Basic.CAUSE_DIGIT_1:
				dateAndTime = dateAndTime + "1";
				break;
			case Basic.CAUSE_DIGIT_2:
				dateAndTime = dateAndTime + "2";
				break;
			case Basic.CAUSE_DIGIT_3:
				dateAndTime = dateAndTime + "3";
				break;
			case Basic.CAUSE_DIGIT_4:
				dateAndTime = dateAndTime + "4";
				break;
			case Basic.CAUSE_DIGIT_5:
				dateAndTime = dateAndTime + "5";
				break;
			case Basic.CAUSE_DIGIT_6:
				dateAndTime = dateAndTime + "6";
				break;
			case Basic.CAUSE_DIGIT_7:
				dateAndTime = dateAndTime + "7";
				break;
			case Basic.CAUSE_DIGIT_8:
				dateAndTime = dateAndTime + "8";
				break;
			case Basic.CAUSE_DIGIT_9:
				dateAndTime = dateAndTime + "9";
				break;
			default:
				break;
			}
	
			// TODO: Add logic to check if date and time is valid. We assume that
			// user is well educated and will always punch right date and time
	
			if (dateAndTime.length() == 10) {			
				
				char[] c = dateAndTime.toCharArray();
	
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("You have selected delivery date to be ");
	
				String date = "" + c[0] + c[1];
				int iDate = (new Integer(date)).intValue();
				stringBuffer.append(iDate);
	
				String month = "" + c[2] + c[3];
				int iMonth = (new Integer(month)).intValue();
	
				String year = "" + c[4] + c[5];
				int iYear = (new Integer(year)).intValue();
	
				String hour = "" + c[6] + c[7];
				int iHour = (new Integer(hour)).intValue();
	
				String min = "" + c[8] + c[9];
				int iMin = (new Integer(min)).intValue();
	
				switch (iMonth) {
				case 1:
					month = "January";
					break;
				case 2:
					month = "February";
					break;
				case 3:
					month = "March";
					break;
				case 4:
					month = "April";
					break;
				case 5:
					month = "May";
					break;
				case 6:
					month = "June";
					break;
				case 7:
					month = "July";
					break;
				case 8:
					month = "August";
					break;
				case 9:
					month = "September";
					break;
				case 10:
					month = "October";
					break;
				case 11:
					month = "November";
					break;
				case 12:
					month = "December";
					break;
				default:
					break;
				}
				stringBuffer.append(" of ");
				stringBuffer.append(month);
				stringBuffer.append(" ");
				stringBuffer.append(2000 + iYear);
				stringBuffer.append(" at ");
				stringBuffer.append(iHour);
				stringBuffer.append(" hour and ");
				stringBuffer.append(iMin);
				stringBuffer.append(" minute. Thank you. Bye.");
	
				java.sql.Timestamp timeStamp = new java.sql.Timestamp(
						(iYear + 100), iMonth - 1, iDate, iHour, iMin, 0, 0);
				
				try {
					InitialContext ctx = new InitialContext();
					OrderManager orderManager = (OrderManager) ctx.lookup("shopping-demo/OrderManagerBean/remote");
					orderManager.setDeliveryDate(request.getSession().getApplicationSession().getAttribute("orderId"), timeStamp);
				} catch (NamingException e) {
					logger.error("An exception occured while retrieving the EJB OrderManager",e);
				}				
				logger.info(stringBuffer.toString());
				try {
					TTSUtils.buildAudio(stringBuffer.toString(), "deliveryDate.wav");
					MsConnection connection = (MsConnection) request.getSession().getApplicationSession().getAttribute("connection");
					String endpoint = connection.getEndpoint();
					MsSignalGenerator generator = connection.getSession().getProvider().getSignalGenerator(endpoint);
					java.io.File speech = new File("deliveryDate.wav");
					logger.info("Playing delivery date summary : " + "file://" + speech.getAbsolutePath());
					MediaResourceListener mediaResourceListener = new MediaResourceListener(request.getSession(), connection);
					generator.addResourceListener(mediaResourceListener);
					generator.apply(Announcement.PLAY, new String[]{"file://" + speech.getAbsolutePath()});
					logger.info("delivery Date summary played. waiting for DTMF ");
				} catch (Exception e) {
					logger.error("An unexpected exception occured while generating the deliveryDate tts file");
				}							
			} else {
				request.getSession().setAttribute("dateAndTime", dateAndTime);
			}
		}	
	}
	
	/**
	 * Make the media server play a file given in parameter
	 * and add a listener so that when the media server is done playing the call is tear down 
	 * @param session the sip session used to tear down the call
	 * @param audioFile the file to play
	 */
	private void playFileInResponseToDTMFInfo(SipSession session,
			String audioFile) {
		MsConnection connection = (MsConnection)session.getApplicationSession().getAttribute("connection");
		String endpoint = connection.getEndpoint();
		MsSignalGenerator generator = connection.getSession().getProvider().getSignalGenerator(endpoint);		
		MediaResourceListener mediaResourceListener = new MediaResourceListener(session, connection);
		generator.addResourceListener(mediaResourceListener);
		generator.apply(Announcement.PLAY, new String[] { audioFile });
		session.setAttribute("DTMFSession", DTMF_SESSION_STOPPED);
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
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);				
		Address address = req.getAddressHeader(CONTACT_HEADER);
		String fromURI = req.getFrom().getURI().toString();
		if(address.getExpires() == 0) {			
			logger.info("User " + fromURI + " unregistered");
		} else {
			resp.setAddressHeader(CONTACT_HEADER, address);			
			logger.info("User " + fromURI + 
					" registered with an Expire time of " + address.getExpires());
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
			Address fromAddress = sipFactory.createAddress("sip:admin@sip-servlets.com");
			String customerPhone = (String) sipApplicationSession.getAttribute("customerPhone");
			if(sipApplicationSession.getAttribute("adminApproval") != null) {
				customerPhone = (String) sipApplicationSession.getAttribute("adminAddress");
			}			
			Address toAddress = sipFactory.createAddress(customerPhone);									
			logger.info("preparing to call : "+ toAddress);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(customerPhone);			
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
