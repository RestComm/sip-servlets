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
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;


public class CallBlockingSipServlet extends SipServlet implements SipErrorListener,
		Servlet {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(CallBlockingSipServlet.class);
	List<String> blockedUris = null;
	
	/** Creates a new instance of CallBlockingSipServlet */
	public CallBlockingSipServlet() {}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call blocking sip servlet has been started");
		super.init(servletConfig);
		blockedUris = new ArrayList<String>();
		blockedUris.add("sip:blocked-sender@sip-servlets.com");
		blockedUris.add("sip:blocked-sender@127.0.0.1");
	}
	
	public boolean isUriBlocked(String uri) {
		for(String blockedUri:blockedUris) {
			if(uri.indexOf(blockedUri)>=0) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n"
				+ request.toString());
		String fromUri = request.getFrom().getURI().toString();
		logger.info(fromUri);
		
		if(isUriBlocked(fromUri)) {
			logger.info(fromUri + " has been blocked !");
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
			sipServletResponse.send();		
		} else {
			logger.info(fromUri + " has not been blocked, we proxy the request");
			request.getProxy().proxyTo(request.getRequestURI());
		}
	}

	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Call Blocking doesn't handle BYE requests");
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