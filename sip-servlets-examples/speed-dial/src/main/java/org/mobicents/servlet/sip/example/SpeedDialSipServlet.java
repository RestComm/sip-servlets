package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This example shows Speed Dial capabilities.
 * User can type a number on their sip phone and this will be translated to their favorite
 * hard coded address and then proxied to it.
 * @author Jean Deruelle
 *
 */
public class SpeedDialSipServlet extends SipServlet {

	private static Log logger = LogFactory.getLog(SpeedDialSipServlet.class);
	Map<String, String> dialNumberToSipUriMapping = null;
	
	/** Creates a new instance of SpeedDialSipServlet */
	public SpeedDialSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the speed dial sip servlet has been started");
		super.init(servletConfig);
		dialNumberToSipUriMapping = new HashMap<String, String>();
		dialNumberToSipUriMapping.put("1", "sip:receiver@sip-servlets.com"); 
		dialNumberToSipUriMapping.put("2", "sip:mranga@sip-servlets.com"); // The Master Blaster
		dialNumberToSipUriMapping.put("3", "sip:vlad@sip-servlets.com");
		dialNumberToSipUriMapping.put("4", "sip:bartek@sip-servlets.com");
		dialNumberToSipUriMapping.put("5", "sip:jeand@sip-servlets.com");
		dialNumberToSipUriMapping.put("9", "sip:receiver@127.0.0.1:5090"); // special case to be able to run the app standalone
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());
		logger.info(request.getRequestURI().toString());
		
		String dialNumber = ((SipURI)request.getRequestURI()).getUser();
		String mappedUri = dialNumberToSipUriMapping.get(dialNumber);	
		if(mappedUri != null) {
			SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
			Proxy proxy = request.getProxy();
			proxy.setRecordRoute(false);
			proxy.setParallel(false);
			proxy.setSupervised(false);
			logger.info("proxying to " + mappedUri);
			proxy.proxyTo(sipFactory.createURI(mappedUri));				
		} else {
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE, "No mapping for " + dialNumber);
			sipServletResponse.send();			
		}		
	}
}
