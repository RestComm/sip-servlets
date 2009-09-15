/**
 * 
 */
package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.CallManager;
import org.mobicents.servlet.sip.example.util.DTMFUtils;

/**
 * Sip Servlet handling responses to call initiated due to actions made on the web shopping demo
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class JSleeSipsInteropServlet 
	extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static Log logger = LogFactory.getLog(JSleeSipsInteropServlet.class);
	private static final String CONTACT_HEADER = "Contact";
	
	/** Creates a new instance of ShoppingSipServlet */
	public JSleeSipsInteropServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the jslee sips interop sip servlet has been started");
		super.init(servletConfig);
	}		
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Got Invite = " + req);
		try {
			InitialContext ctx = new InitialContext();
			CallManager callManagerRef = (CallManager) ctx.lookup("jslee-sips/CallManagerBean/local");
			//saving the reference to the EJB ref for latter use to avoid doing jndi lookup calls
			req.getSession().setAttribute("callManagerRef", callManagerRef);
			logger.info("Calling out call manager");
			callManagerRef.initMediaConnection(req);
		} catch (NamingException e) {
			logger.error("An exception occured while retrieving the EJB OrderApproval",e);
		}		
	}
	
	@Override
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		CallManager callManagerRef = (CallManager) req.getSession().getAttribute("callManagerRef");
		callManagerRef.playAnnouncement(req, CallManager.ANNOUNCEMENT_TYPE_OPENING);
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
		
		logger.info("DTMF session in started state, parsing message content");
		if(messageContent != null && messageContent.length() > 0 && signalIndex != -1) {
			String signal = messageContent.substring("Signal=".length(),"Signal=".length()+1).trim();			
			logger.info("Signal received " + signal );																
			
			if(DTMFUtils.updateBoothNumber(request.getSession(), signal)) {
				CallManager callManagerRef = (CallManager) request.getSession().getAttribute("callManagerRef");
				callManagerRef.playAnnouncement(request, CallManager.ANNOUNCEMENT_TYPE_CONFIRMATION);
			}
		}		
	}	
	
	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {				
		CallManager callManagerRef = (CallManager) req.getSession().getAttribute("callManagerRef");
		callManagerRef.endCall(req, true);
		req.createResponse(SipServletResponse.SC_OK).send();
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
			logger.info("User " + fromURI + 
					" registered this contact address " + address + 
					" with an Expire time of " + expires);
		}							
		
		resp.send();
	}
}
