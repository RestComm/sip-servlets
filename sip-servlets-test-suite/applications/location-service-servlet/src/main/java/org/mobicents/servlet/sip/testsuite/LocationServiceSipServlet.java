package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LocationServiceSipServlet extends SipServlet implements SipErrorListener,
		Servlet {

	private static Log logger = LogFactory.getLog(LocationServiceSipServlet.class);
	Map<String, List<URI>> registeredUsers = null;
	
	/** Creates a new instance of SpeedDialSipServlet */
	public LocationServiceSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the locationb service sip servlet has been started");
		super.init(servletConfig);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		registeredUsers = new HashMap<String, List<URI>>();
		List<URI> uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:5090"));
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:6090"));
		registeredUsers.put("sip:receiver@sip-servlets.com", uriList);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());		
		
		List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
		if(contactAddresses != null && contactAddresses.size() > 0) {			
			Proxy proxy = request.getProxy();
			proxy.setRecordRoute(false);
			proxy.setParallel(true);
			proxy.setSupervised(false);
			proxy.proxyTo(contactAddresses);		
		} else {
			logger.info(request.getRequestURI().toString() + " is not currently registered");
			SipServletResponse sipServletResponse = 
				request.createResponse(SipServletResponse.SC_MOVED_PERMANENTLY, "Moved Permanently");
			sipServletResponse.send();
		}
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Received register request: " + req.getTo());
		
		//Storing the registration 
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		Address toAddress = req.getTo();
		ListIterator<String> contactAddresses = req.getHeaders("Contact");
		List<URI> contactUris = new ArrayList<URI>();
		while (contactAddresses.hasNext()) {
			String contactAddress = (String) contactAddresses.next();
			contactUris.add(sipFactory.createURI(contactAddress));
		}
		//FIXME handle the expires to add or remove the user
		registeredUsers.put(toAddress.toString(), contactUris);
		//answering OK to REGISTER
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}
	
	// SipErrorListener methods
	/**
	 * {@inheritDoc}
	 */
	public void noAckReceived(SipErrorEvent ee) {
		logger.error("noAckReceived.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void noPrackReceived(SipErrorEvent ee) {
		logger.error("noPrackReceived.");
	}

}
