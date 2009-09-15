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
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class NotifierSipServlet extends SipServlet implements SipSessionListener {
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(NotifierSipServlet.class);
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	private static final String SIP_SESSION_READY_TO_BE_INVALIDATED = "sipSessionReadyToBeInvalidated";
	
	@Resource
	SipFactory sipFactory;
	
	/** Creates a new instance of SimpleProxyServlet */
	public NotifierSipServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the notifier sip servlet has been started");
		super.init(servletConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("from : " + request.getFrom());
		logger.info("Got request: "
				+ request.getMethod());
	
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		request.getSession().setAttribute("inviteReceived", "true");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}
	
	@Override
	protected void doErrorResponse(SipServletResponse resp)
			throws ServletException, IOException {
		logger.info("Got response: "
				+ resp);
		resp.getSession().createRequest("BYE").send();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got BYE request: " + request);
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();
	}	
	
	/**
	 * {@inheritDoc}
	 */
	protected void doSubscribe(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got Subscribe: "
				+ request.getMethod());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.addHeader("Expires", request.getHeader("Expires"));
		sipServletResponse.addHeader("Event", request.getHeader("Event"));
		sipServletResponse.send();		
		// send notify		
		SipServletRequest notifyRequest = request.getSession().createRequest("NOTIFY");
		if(request.isInitial() || request.getSession().getAttribute("inviteReceived") != null) {
			request.getSession().removeAttribute("inviteReceived");
			notifyRequest.addHeader("Subscription-State", "pending");
			notifyRequest.addHeader("Event", "reg");
			notifyRequest.send();
			notifyRequest = request.getSession().createRequest("NOTIFY");
		}		
		if(request.getHeader("Expires").trim().equals("0")) {
			notifyRequest.addHeader("Subscription-State", "terminated");
		} else {			
			notifyRequest.addHeader("Subscription-State", "active");
		}
		notifyRequest.addHeader("Event", "reg");
		notifyRequest.send();
	}

	public void sessionCreated(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionDestroyed(SipSessionEvent se) {
		// TODO Auto-generated method stub
		
	}

	public void sessionReadyToInvalidate(SipSessionEvent se) {
		logger.info("sip session expired " +  se.getSession());
		
		try {
			SipServletRequest sipServletRequest = sipFactory.createRequest(
					sipFactory.createApplicationSession(), 
					"MESSAGE", 
					se.getSession().getLocalParty(), 
					se.getSession().getRemoteParty());
			SipURI sipUri=sipFactory.createSipURI("LittleGuy", "127.0.0.1:5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(SIP_SESSION_READY_TO_BE_INVALIDATED.length());
			sipServletRequest.setContent(SIP_SESSION_READY_TO_BE_INVALIDATED, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
}