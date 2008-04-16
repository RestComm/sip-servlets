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
import javax.servlet.sip.SipSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			SipServletRequest ackRequest = sipServletResponse.createAck();
			ackRequest.send();			
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
