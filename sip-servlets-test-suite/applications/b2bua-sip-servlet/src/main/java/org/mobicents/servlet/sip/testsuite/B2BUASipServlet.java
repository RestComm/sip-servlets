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

import javax.servlet.ServletException;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

public class B2BUASipServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;

	B2buaHelper helper = null;
	
	private static transient Logger logger = Logger.getLogger(B2BUASipServlet.class);

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got request:\n" + request);
		Map<String, List<String>> headers=new HashMap<String, List<String>>();
		List<String> toHeaderList = new ArrayList<String>();
		toHeaderList.add("sip:aa@sip-servlets.com");
		headers.put("To", toHeaderList);
		
		helper = request.getB2buaHelper();
		SipServletRequest forkedRequest = helper.createRequest(request, true,
				headers);
		
		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(
				SIP_FACTORY);				
		SipURI sipUri = (SipURI) sipFactory.createURI("sip:aa@127.0.0.1:5059");		
		forkedRequest.setRequestURI(sipUri);
		
		if (logger.isDebugEnabled()) {
			logger.debug("forkedRequest = " + forkedRequest);
		}
		forkedRequest.getSession().setAttribute("originalRequest", request);
		forkedRequest.send();

	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		logger.info("Got : " + sipServletResponse.getStatus() + " "
				+ sipServletResponse.getMethod());		
		int status = sipServletResponse.getStatus();
		if (status == SipServletResponse.SC_OK) {
			String cSeqValue = sipServletResponse.getHeader("CSeq");
			//if this is a response to an INVITE we ack it and forward the OK 
			if(cSeqValue.indexOf("INVITE") != -1) {
				SipServletRequest ackRequest = sipServletResponse.createAck();
				ackRequest.send();
				//create and sends OK for the first call leg
				SipSession originalSession =   
				    helper.getLinkedSession(sipServletResponse.getSession());					
				SipServletResponse responseToOriginalRequest = 
					helper.createResponseToOriginalRequest(originalSession, sipServletResponse.getStatus(), "OK");
				responseToOriginalRequest.send();
			}
		} else {
			super.doResponse(sipServletResponse);
		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got BYE: "
				+ request.getMethod());
		SipServletResponse response = request.createResponse(200);
		response.send();

		SipSession session = request.getSession();		
		SipSession linkedSession = helper.getLinkedSession(session);
		SipServletRequest newRequest = linkedSession.createRequest("BYE");
		logger.info(newRequest);
		newRequest.send();

	}
	
	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {		
		logger.info("Got CANCEL: " + request.toString());
		SipSession session = request.getSession();
		B2buaHelper b2buaHelper = request.getB2buaHelper();
		SipSession linkedSession = b2buaHelper.getLinkedSession(session);
		SipServletRequest originalRequest = (SipServletRequest)linkedSession.getAttribute("originalRequest");
		SipServletRequest  cancelRequest = b2buaHelper.getLinkedSipServletRequest(originalRequest).createCancel();				
		logger.info("forkedRequest = " + cancelRequest);			
		cancelRequest.send();
	}

}
