/**
 * 
 */
package org.mobicents.servlet.sip.example;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author Jean Deruelle
 *
 */
public class ShoppingSipServlet extends SipServlet {
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
//			try {
//				StringBuffer stringBuffer = new StringBuffer();
//				stringBuffer.append(sipServletResponse.getSession().getApplicationSession().getAttribute("customerName"));
//				stringBuffer.append(" has placed an order of $");
//				stringBuffer.append(sipServletResponse.getSession().getApplicationSession().getAttribute("amountOrder"));
//				stringBuffer.append(". Press 1 to approve and 2 to reject.");				
//				
//				buildAudio(stringBuffer.toString(), "speech.wav");
//				Thread.sleep(300);
//				MsPeer peer = MsPeerFactory.getPeer();
//				MsProvider provider = peer.getProvider();
//				MsSession session = provider.createSession();
//				MsConnection connection = session.createNetworkConnection("media/trunk/IVR/1");
//				MediaConnectionListener listener = new MediaConnectionListener();
//				listener.setInviteResponse(sipServletResponse);
//				connection.addConnectionListener(listener);
//				connection.modify("$", sdp);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}			
		}
	}
	
//	private void buildAudio(String text, String filename) throws Exception {
//		VoiceManager mgr = VoiceManager.getInstance();
//		Voice voice = mgr.getVoice("kevin16");
//		voice.allocate();
//		File speech = new File(filename);
//		SingleFileAudioPlayer player = new SingleFileAudioPlayer(getBasename(speech.getAbsolutePath()), getAudioType(filename));
//		voice.setAudioPlayer(player);
//		voice.startBatch();
//		boolean ok = voice.speak(text);
//		voice.endBatch();
//		player.close();
//		voice.deallocate();
//	}
//	
//	private static String getBasename(String path) {
//		int index = path.lastIndexOf(".");
//		if (index == -1) {
//			return path;
//		} else {
//			return path.substring(0, index);
//		}
//	}
//	
//	private static String getExtension(String path) {
//		int index = path.lastIndexOf(".");
//		if (index == -1) {
//			return null;
//		} else {
//			return path.substring(index + 1);
//		}
//	}
//	
//	private static AudioFileFormat.Type getAudioType(String file) {
//		AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
//		String extension = getExtension(file);
//
//		for (int i = 0; i < types.length; i++) {
//			if (types[i].getExtension().equals(extension)) {
//				return types[i];
//			}
//		}
//		return null;
//	}
	
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got bye " + request);		
		SipServletResponse ok = request
				.createResponse(SipServletResponse.SC_OK);
		ok.send();
	}
	
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
