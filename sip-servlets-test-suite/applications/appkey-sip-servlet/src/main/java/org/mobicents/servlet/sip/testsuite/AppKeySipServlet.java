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

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

public class AppKeySipServlet 
		extends SipServlet 
		implements SipServletListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(AppKeySipServlet.class);
	@Resource
	SipFactory sipFactory;
	@Resource
	SipSessionsUtil sipSessionsUtil;
	
	public AppKeySipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the appkey sip servlet has been started");
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
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {				
//				logger.error("unexpected exception", e);
//			}
//			SipServletRequest sipServletRequest = sipServletResponse.getSession().createRequest("BYE");
//			sipServletRequest.send();
		}
		
		if (status == SipServletResponse.SC_OK && "REGISTER".equalsIgnoreCase(sipServletResponse.getMethod())) {
			URI fromURI = sipFactory.createSipURI("BigGuy", "here.com");
			URI toURI = sipFactory.createSipURI("LittleGuy", "there.com");
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipServletResponse.getApplicationSession(), "INVITE", fromURI, toURI);
			SipURI requestURI = sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(requestURI);
			String inviteCallId = sipServletRequest.getHeader("Call-ID");
			String registerCallId = (String) sipServletResponse.getApplicationSession().getAttribute("CallId");
			logger.info("REGISTER Call ID " + registerCallId);
			logger.info("INVITE Call ID " + inviteCallId);
			if(inviteCallId != null && registerCallId != null && !inviteCallId.equalsIgnoreCase(registerCallId)) {
				try {			
					sipServletRequest.send();
				} catch (IOException e) {
					logger.error(e);
				}		
			} else {
				throw new ServletException("Call Id for different requests within the same application session whose the key is app generated are the same!");
			}
		}
	}
	
	@Override
	protected void doBye(SipServletRequest req) throws ServletException,
			IOException {
		req.createResponse(SipServletResponse.SC_OK).send();
	}

	// SipServletListener methods
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletListener#servletInitialized(javax.servlet.sip.SipServletContextEvent)
	 */
	public void servletInitialized(SipServletContextEvent ce) {
		SipApplicationSession sipApplicationSession = sipSessionsUtil.getApplicationSessionByKey("appkeytest", true);
		
		URI fromURI = sipFactory.createSipURI("BigGuy", "here.com");
		URI toURI = sipFactory.createSipURI("BigGuy", "there.com");
		SipServletRequest sipServletRequest = 
			sipFactory.createRequest(sipApplicationSession, "REGISTER", fromURI, toURI);
		SipURI requestURI = sipFactory.createSipURI("BigGuy", "127.0.0.1:5080");
		sipServletRequest.setRequestURI(requestURI);
		//Storing the Call id for future comparison
		sipApplicationSession.setAttribute("CallId", sipServletRequest.getHeader("Call-ID"));
		try {			
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error(e);
		}		
	}
}