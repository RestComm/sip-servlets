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
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;


public class ReplacesSenderSipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(ReplacesSenderSipServlet.class);
	Map<String, List<URI>> registeredUsers = null;
	
	@Resource
	private SipFactory sipFactory;
	
	/** Creates a new instance of ReplacesSenderSipServlet */
	public ReplacesSenderSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the join sip servlet has been started");
		super.init(servletConfig);
		SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
		registeredUsers = new HashMap<String, List<URI>>();
		List<URI> uriList  = new ArrayList<URI>();		
		uriList.add(sipFactory.createURI("sip:replaces@127.0.0.1:5090"));
		registeredUsers.put("sip:replaces@sip-servlets.com", uriList);
		registeredUsers.put("sip:replaces@127.0.0.1:5070;transport=udp", uriList);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doMessage(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got request: "
				+ request.getMethod());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
		
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
		SipURI fromURI = sipFactory.createSipURI("replacer", "sip-servlets.com");
		SipURI requestURI = sipFactory.createSipURI("replacer", "127.0.0.1:5090");
		SipServletRequest sipServletRequest = sipFactory.createRequest(sipApplicationSession, "INVITE", fromURI, request.getFrom().getURI());
		sipServletRequest.addHeader("Replaces", ((String)request.getContent()).substring("Replaces : ".length()));
		sipServletRequest.setRequestURI(requestURI);
		sipServletRequest.getSession().setAttribute("replacesInvite", Boolean.TRUE);
		sipServletRequest.send();
	}
	
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n" + request.toString());		
		
		List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
		if(contactAddresses != null && contactAddresses.size() > 0) {			
			Proxy proxy = request.getProxy();
			proxy.setProxyTimeout(3);
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
	
	@Override
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		if(!"BYE".equalsIgnoreCase(resp.getMethod()) && resp.getSession().getAttribute("replacesInvite") != null) {
			resp.createAck().send();
//			resp.getSession(false).createRequest("BYE").send();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		if(request.getSession().getAttribute("replacesInvite") != null) {
			logger.info("Got BYE request: " + request);
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
		}
	}	
}