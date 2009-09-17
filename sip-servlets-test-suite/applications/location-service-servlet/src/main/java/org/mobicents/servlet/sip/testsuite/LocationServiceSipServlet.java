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
package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;


public class LocationServiceSipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(LocationServiceSipServlet.class);
	
	private static final String LOCAL_TRANSPORT = "udp";
	private static final int LOCAL_PORT = 5070;
	private static final String LOCAL_LOCALHOST_ADDR = "127.0.0.1";
	
	private static final String REMOTE_TRANSPORT = "udp";
	private static final int REMOTE_PORT = 5090;
	private static final String REMOTE_LOCALHOST_ADDR = "127.0.0.1";
	
	private static final String INITIAL_REMOTE_TRANSPORT = "udp";
	private static final int INITIAL_REMOTE_PORT = 5070;
	private static final String INITIAL_REMOTE_LOCALHOST_ADDR = "127.0.0.1";
	
	private static final String TEST_USER_REMOTE = "remote";
	
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
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver-failover@127.0.0.1:5090"));
		registeredUsers.put("sip:receiver-failover@sip-servlets.com", uriList);
		registeredUsers.put("sip:receiver-failover@127.0.0.1:5090", uriList);
		uriList  = new ArrayList<URI>();
		uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:5070"));
		registeredUsers.put("sip:proxy-b2bua@127.0.0.1:5070", uriList);
		
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());		
		
		if(((SipURI)request.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			if(request.getRemoteAddr().equals(LOCAL_LOCALHOST_ADDR) && request.getRemotePort() == LOCAL_PORT && request.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {
				logger.info("remote information is correct");
			} else {
				logger.error("remote information is incorrect");
				logger.error("remote addr " + request.getRemoteAddr());
				logger.error("remote port " + request.getRemotePort());
				logger.error("remote transport " + request.getTransport());
				SipServletResponse sipServletResponse = 
					request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Incorrect remote information");
				sipServletResponse.send();
				return;
			}
			if(request.getInitialRemoteAddr().equals(INITIAL_REMOTE_LOCALHOST_ADDR) && request.getInitialRemotePort() == INITIAL_REMOTE_PORT && request.getInitialTransport().equalsIgnoreCase(INITIAL_REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + request.getInitialRemoteAddr());
				logger.error("Initial remote port " + request.getInitialRemotePort());
				logger.error("Initial remote transport " + request.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(request.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && request.getLocalPort() == LOCAL_PORT && request.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("local information is correct");
			} else {
				logger.error("local information is incorrect");
				logger.error("local addr " + request.getLocalAddr());
				logger.error("local port " + request.getLocalPort());
				logger.error("local transport " + request.getTransport());
				throw new IllegalArgumentException("local information is incorrect");
			}
		}
		
		if(request.isInitial()) {
			List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
			if(contactAddresses != null && contactAddresses.size() > 0) {			
				Proxy proxy = request.getProxy();
				proxy.setProxyTimeout(3);
				proxy.setRecordRoute(true);
				proxy.setParallel(true);
				proxy.setSupervised(true);			
				for (URI uri : contactAddresses) {
					logger.info("proxying to " + uri);
				}
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
	protected void doErrorResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got response " + resp);
	}
	
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got response " + resp);
		if(((SipURI)resp.getFrom().getURI()).getUser().equalsIgnoreCase(TEST_USER_REMOTE)) {
			
			if(resp.getRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && resp.getRemotePort() == REMOTE_PORT && resp.getTransport().equalsIgnoreCase(REMOTE_TRANSPORT)) {
				logger.info("remote information is correct");
			} else {
				logger.error("remote information is incorrect");
				logger.error("remote addr " + resp.getRemoteAddr());
				logger.error("remote port " + resp.getRemotePort());
				logger.error("remote transport " + resp.getTransport());
				throw new IllegalArgumentException("remote information is incorrect");
			}
			if(resp.getInitialRemoteAddr().equals(REMOTE_LOCALHOST_ADDR) && resp.getInitialRemotePort() == REMOTE_PORT && resp.getInitialTransport().equalsIgnoreCase(REMOTE_TRANSPORT)) {			
				logger.info("Initial remote information is correct");
			} else {
				logger.error("Initial remote information is incorrect");
				logger.error("Initial remote addr " + resp.getInitialRemoteAddr());
				logger.error("Initial remote port " + resp.getInitialRemotePort());
				logger.error("Initial remote transport " + resp.getInitialTransport());
				throw new IllegalArgumentException("initial remote information is incorrect");
			}
			if(resp.getLocalAddr().equals(LOCAL_LOCALHOST_ADDR) && resp.getLocalPort() == LOCAL_PORT && resp.getTransport().equalsIgnoreCase(LOCAL_TRANSPORT)) {			
				logger.info("local information is correct");
			} else {
				logger.error("local information is incorrect");
				logger.error("local addr " + resp.getLocalAddr());
				logger.error("local port " + resp.getLocalPort());
				logger.error("local transport " + resp.getTransport());
				throw new IllegalArgumentException("local information is incorrect");
			}
		}
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Received register request: " + req.getTo());
		
		//Storing the registration 
		Address toAddress = req.getTo();
		ListIterator<Address> contactAddresses = req.getAddressHeaders("Contact");
		List<URI> contactUris = new ArrayList<URI>();
		while (contactAddresses.hasNext()) {
			Address contactAddress = contactAddresses.next();
			contactUris.add(contactAddress.getURI());
		}
		//FIXME handle the expires to add or remove the user
		registeredUsers.put(toAddress.toString(), contactUris);
		//answering OK to REGISTER
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		resp.send();
	}
	
	@Override
	protected void doCancel(SipServletRequest req) throws ServletException,
			IOException {
		logger.error("CANCEL seen at proxy " + req);
	}
}
