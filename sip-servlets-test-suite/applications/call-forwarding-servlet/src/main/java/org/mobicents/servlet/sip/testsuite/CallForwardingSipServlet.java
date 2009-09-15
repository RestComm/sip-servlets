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

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;


public class CallForwardingSipServlet extends SipServlet implements SipErrorListener,
		Servlet {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(CallForwardingSipServlet.class);
	
	
	/** Creates a new instance of CallForwardingSipServlet */
	public CallForwardingSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding sip servlet has been started");
		super.init(servletConfig);
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got ACK: "
				+ request.getMethod());
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request: "
				+ request.getMethod());
		if(request.getFrom().getURI().toString().indexOf("sip:forward-sender@sip-servlets.com") != -1) {
			SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_MOVED_TEMPORARILY);
			SipURI sipUri= sipFactory.createSipURI("forward-receiver", "127.0.0.1:5090");		
			sipServletResponse.addHeader("Contact", sipUri.toString());		
			sipServletResponse.send();
		}
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