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
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;


public class ShootmeSipServletAuth extends SipServlet implements SipErrorListener,
		Servlet {
	private static final long serialVersionUID = 1L;
	@Override
	protected void doBranchResponse(SipServletResponse resp)
			throws ServletException, IOException {
		resp.getApplicationSession().setAttribute("doBranchResponse", "true");
		super.doBranchResponse(resp);
	}

	private static transient Logger logger = Logger.getLogger(ShootmeSipServletAuth.class);
	private static String TEST_REINVITE_USERNAME = "reinvite";
	private static String TEST_CANCEL_USERNAME = "cancel";
	
	/** Creates a new instance of ShootmeSipServletAuth */
	public ShootmeSipServletAuth() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the simple sip servlet has been started");
		super.init(servletConfig);		
		if(!servletConfig.getServletContext().getInitParameter("testContextApplicationParameter").equals("OK")) {
			throw new ServletException("Cannot read the Context ApplicationParameter");
		} else {
			logger.info("testContextApplicationParameter : " + servletConfig.getServletContext().getInitParameter("testContextApplicationParameter"));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("from : " + request.getFrom());
		logger.info("Got request: "
				+ request.getMethod());
		if(!TEST_CANCEL_USERNAME.equalsIgnoreCase(((SipURI)request.getFrom().getURI()).getUser())) {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.send();
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
		} else {
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
			sipServletResponse.send();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();
		}
	}
	
	@Override
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		if(req.getFrom().getURI() instanceof SipURI) {
			if(TEST_REINVITE_USERNAME.equalsIgnoreCase(((SipURI)req.getFrom().getURI()).getUser())) {
				SipServletRequest reInvite = req.getSession(false).createRequest("INVITE");
				if(req.getSession(false) == reInvite.getSession(false)) {
					reInvite.send();
				} else {
					logger.error("the newly created subsequent request doesn't have " +
							"the same session instance as the one it has been created from");
				}
			}
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
		if(!"BYE".equalsIgnoreCase(resp.getMethod())) {
			resp.createAck().send();
			resp.getSession(false).createRequest("BYE").send();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request: " + request);
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		
		// Force fail by not sending OK if the doBranchResponse is called. In non-proxy app
		// this would be wrong.
		if(!"true".equals(request.getApplicationSession().getAttribute("doBranchResponse")))
			sipServletResponse.send();
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