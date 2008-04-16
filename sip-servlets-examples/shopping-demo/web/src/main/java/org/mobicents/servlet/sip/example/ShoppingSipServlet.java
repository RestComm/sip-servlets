/**
 * 
 */
package org.mobicents.servlet.sip.example;

import java.io.IOException;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.actions.OrderManager;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsSignalGenerator;
import org.mobicents.mscontrol.signal.Announcement;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author Jean Deruelle
 *
 */
public class ShoppingSipServlet extends SipServlet {
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
				String pathToAudioDirectory = (String)getServletContext().getInitParameter("audio.files.path");			
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
		} else {
			logger.info("DTMF session in stopped state, not parsing message content");
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
}
