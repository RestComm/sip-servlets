/*
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

/**
 * This example shows Proxying on 2 different location.
 * Base on the request URI it forwards it to 2 different hard coded locations if
 * it is in its forwarding list 
 * @author Jean Deruelle
 *
 */
public class DistributableLocationServiceSipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DistributableLocationServiceSipServlet.class);
	private static final String CONTACT_HEADER = "Contact";
	private static final String RECEIVED = "Received";
	Map<String, List<URI>> registeredUsers = null;
	
	/** Creates a new instance of SpeedDialSipServlet */
	public DistributableLocationServiceSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the distributable location service sip servlet has been started");
		super.init(servletConfig);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		registeredUsers = new HashMap<String, List<URI>>();
		List<URI> uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:5090"));
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:6090"));
		registeredUsers.put("sip:receiver@sip-servlets.com", uriList);
		List<URI> perfUriList  = new ArrayList<URI>();
		perfUriList.add(sipFactory.createURI("sip:perf-receiver@127.0.0.1:5090"));		
		registeredUsers.put("sip:perf-receiver@sip-servlets.com", perfUriList);
		ArrayList<URI> failOverUriList  = new ArrayList<URI>();
		failOverUriList.add(sipFactory.createURI("sip:receiver-failover@127.0.0.1:5090"));
		registeredUsers.put("sip:receiver-failover@sip-servlets.com", failOverUriList);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());		
		
		if(request.isInitial()) {
			List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
			if(contactAddresses != null && contactAddresses.size() > 0) {
				request.getSession().setAttribute("INVITE", RECEIVED);
				request.getApplicationSession().setAttribute("INVITE", RECEIVED);
				Proxy proxy = request.getProxy();
				proxy.setRecordRoute(true);
				proxy.setParallel(true);
				proxy.setSupervised(true);
				proxy.proxyTo(contactAddresses);		
			} else {
				logger.info(request.getRequestURI().toString() + " is not currently registered");
				SipServletResponse sipServletResponse = 
					request.createResponse(SipServletResponse.SC_MOVED_PERMANENTLY, "Moved Permanently");
				sipServletResponse.send();
			}
		}
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Received register request: " + req.getTo());
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		HashMap<String, String> users = (HashMap<String, String>) getServletContext().getAttribute("registeredUsersMap");
		if(users == null) users = new HashMap<String, String>();
		getServletContext().setAttribute("registeredUsersMap", users);
		
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
					" registered with an Expire time of " + expires);
		}				
						
		resp.send();
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		String sipSessionInviteAttribute  = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute  = (String) request.getApplicationSession().getAttribute("INVITE");
		if(logger.isInfoEnabled()) {			
			logger.info("Distributable Location Service : attributes previously set in sip session INVITE : "+ sipSessionInviteAttribute);
			logger.info("Distributable Location Service : attributes previously set in sip application session INVITE : "+ sipApplicationSessionInviteAttribute);
		}
		if(sipSessionInviteAttribute == null  || sipApplicationSessionInviteAttribute == null 
				|| !RECEIVED.equalsIgnoreCase(sipSessionInviteAttribute) 
				|| !RECEIVED.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			throw new IllegalArgumentException("State not replicated...");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		logger.info("SimpleProxyServlet: Got response:\n" + response);
//		if(SipServletResponse.SC_OK == response.getStatus() && "BYE".equalsIgnoreCase(response.getMethod())) {
//			SipSession sipSession = response.getSession(false);
//			if(sipSession != null) {
//				SipApplicationSession sipApplicationSession = sipSession.getApplicationSession();
//				sipSession.invalidate();
//				sipApplicationSession.invalidate();
//			}			
//		}
	}
	
}
