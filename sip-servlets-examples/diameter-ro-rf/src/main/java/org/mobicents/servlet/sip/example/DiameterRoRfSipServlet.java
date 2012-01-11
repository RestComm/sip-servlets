/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.SipSession.State;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.example.diameter.rorf.RoClient;
import org.mobicents.servlet.sip.example.diameter.rorf.RoClientImpl;
import org.mobicents.servlet.sip.example.diameter.rorf.RoClientListener;

/**
 * This Sip Servlet integration with Mobicents Diameter Ro/Rf.
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * 
 */
public class DiameterRoRfSipServlet extends SipServlet implements
		RoClientListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger
			.getLogger(DiameterRoRfSipServlet.class);

	private final static int IDLE = 0;
	private final static int SENT_INITIAL_RESERVATION = 1;
	private final static int GRANTED_INITIAL_RESERVATION = 2;
	private final static int STARTED_CALL_CHARGING = 3;

	private int CURRENT_STATE = IDLE;

	SipServletRequest inviteRequest = null;
	private RoClient roClient = null;
	private static final String CONTACT_HEADER = "Contact";

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger
				.info("Mobicents Diameter Ro/Rf SIP Servlets Example has been started");
		super.init(servletConfig);

		roClient = new RoClientImpl(this);
	}

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got INVITE: " + request.toString());
			logger.info(request.getFrom().getURI().toString());
		}

		try {
			this.inviteRequest = request;
			roClient
					.reserveInitialUnits(
							request.getFrom().getURI().toString(), request
									.getCallId());
			this.CURRENT_STATE = SENT_INITIAL_RESERVATION;
		} catch (Exception e) {
			logger.error("Failed to start charging process. Aborting call.", e);
			request
					.createResponse(
							SipServletResponse.SC_TEMPORARILY_UNAVAILABLE)
					.send();
		}
	}

	private void doInviteChargingAllowed(SipServletRequest request)
			throws ServletException, IOException {

		B2buaHelper helper = request.getB2buaHelper();
		HashMap<String, String> users = (HashMap<String, String>) getServletContext()
				.getAttribute("registeredUsersMap");
		if (users != null) {

			String contact = users.get(request.getRequestURI().toString());
			if (contact != null) {
				SipFactory sipFactory = (SipFactory) getServletContext()
						.getAttribute(SIP_FACTORY);

				Map<String, List<String>> headers = new HashMap<String, List<String>>();
				List<String> toHeaderSet = new ArrayList<String>();
				toHeaderSet.add(request.getTo().toString());
				headers.put("To", toHeaderSet);

				SipServletRequest forkedRequest = helper.createRequest(request,
						true, headers);

				SipURI sipUri = (SipURI) sipFactory.createURI(contact);
				forkedRequest.setRequestURI(sipUri);
				if (logger.isInfoEnabled()) {
					logger.info("forkedRequest = " + forkedRequest);
				}
				forkedRequest.getSession().setAttribute("originalRequest",
						request);

				forkedRequest.send();
			} else {
				request.createResponse(404).send();
			}
		} else {
			request.createResponse(404).send();
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got BYE: " + request.toString());
		}
		// we send the OK directly to the first call leg
		SipServletResponse sipServletResponse = request
				.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();

		// We stop the charging procedures
		try {
			roClient.stopCharging(this.inviteRequest.getFrom().getURI().toString(),
					this.inviteRequest.getCallId());
		} catch (Exception e) {
			logger.error("Failed to stop charging process.", e);
		}

		// we forward the BYE
		// SipSession session = request.getSession();
		// B2buaHelper helper = request.getB2buaHelper();
		// SipSession linkedSession = helper.getLinkedSession(session);
		// SipServletRequest forkedRequest = linkedSession.createRequest("BYE");
		// if(logger.isInfoEnabled()) {
		// logger.info("forkedRequest = " + forkedRequest);
		// }
		// forkedRequest.send();
		// if(session != null && session.isValid()) {
		// session.invalidate();
		// }

		return;
	}

	@Override
	protected void doUpdate(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got UPDATE: " + request.toString());
		}
		B2buaHelper helper = request.getB2buaHelper();
		SipSession peerSession = helper.getLinkedSession(request.getSession());
		SipServletRequest update = helper.createRequest(peerSession, request,
				null);
		update.send();
	}

	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got CANCEL: " + request.toString());
		}
		SipSession session = request.getSession();
		B2buaHelper helper = request.getB2buaHelper();
		SipSession linkedSession = helper.getLinkedSession(session);
		SipServletRequest originalRequest = (SipServletRequest) linkedSession
				.getAttribute("originalRequest");
		SipServletRequest cancelRequest = helper.getLinkedSipServletRequest(
				originalRequest).createCancel();
		if (logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + cancelRequest);
		}
		cancelRequest.send();

		// Now let's stop charging
		try {
			roClient.stopCharging(this.inviteRequest.getFrom().getURI().toString(),
					this.inviteRequest.getCallId());
		} catch (Exception e) {
			logger.error("Failed to terminate charging session on CANCEL.", e);
		}
	}

	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.toString());
		}
		if (sipServletResponse.getMethod().indexOf("BYE") != -1) {
			SipSession sipSession = sipServletResponse.getSession(false);
			if (sipSession != null && sipSession.isValid()) {
				sipSession.invalidate();
			}
			SipApplicationSession sipApplicationSession = sipServletResponse
					.getApplicationSession(false);
			if (sipApplicationSession != null
					&& sipApplicationSession.isValid()) {
				sipApplicationSession.invalidate();
			}
			return;
		}

		if (sipServletResponse.getMethod().indexOf("INVITE") != -1) {
			try {
				if (CURRENT_STATE == GRANTED_INITIAL_RESERVATION) {
					roClient.startCharging(
							this.inviteRequest.getFrom().getURI().toString(),
							this.inviteRequest.getCallId());
					CURRENT_STATE = STARTED_CALL_CHARGING;
				} else {
					logger
							.warn("Received OK to INVITE on invalid state for Ro FSM. Not performing further charging and aborting call.");
				}
			} catch (Exception e) {
				logger.error(
						"Failed to start charging process. Aborting call.", e);
				// TODO: Abort call
			}

			// if this is a response to an INVITE we ack it and forward the OK
			SipServletRequest ackRequest = sipServletResponse.createAck();
			if (logger.isInfoEnabled()) {
				logger.info("Sending " + ackRequest);
			}
			ackRequest.send();
			// create and sends OK for the first call leg
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse
					.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest
					.createResponse(sipServletResponse.getStatus());
			if (logger.isInfoEnabled()) {
				logger.info("Sending OK on 1st call leg"
						+ responseToOriginalRequest);
			}
			responseToOriginalRequest.setContentLength(sipServletResponse
					.getContentLength());
			if (sipServletResponse.getContent() != null
					&& sipServletResponse.getContentType() != null)
				responseToOriginalRequest.setContent(sipServletResponse
						.getContent(), sipServletResponse.getContentType());
			responseToOriginalRequest.send();
		}
		if (sipServletResponse.getMethod().indexOf("UPDATE") != -1) {
			B2buaHelper helper = sipServletResponse.getRequest()
					.getB2buaHelper();
			SipServletRequest orgReq = helper
					.getLinkedSipServletRequest(sipServletResponse.getRequest());
			SipServletResponse res2 = orgReq.createResponse(sipServletResponse
					.getStatus());
			res2.send();
		}
	}

	@Override
	protected void doErrorResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.getStatus() + " "
					+ sipServletResponse.getReasonPhrase());
		}
		// we don't forward the timeout
		if (sipServletResponse.getStatus() != 408) {
			// create and sends the error response for the first call leg
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse
					.getSession().getAttribute("originalRequest");
			SipServletResponse responseToOriginalRequest = originalRequest
					.createResponse(sipServletResponse.getStatus());
			if (logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg "
						+ responseToOriginalRequest.toString());
			}
			responseToOriginalRequest.send();

			// Now let's stop charging
			try {
				roClient.stopCharging(this.inviteRequest.getFrom().getURI().toString(),
						this.inviteRequest.getCallId());
			} catch (Exception e) {
				logger
						.error(
								"Failed to terminate charging session on ERROR RESPONSE.",
								e);
			}
		}
	}

	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse
				.getSession().getAttribute("originalRequest");
		SipServletResponse responseToOriginalRequest = originalRequest
				.createResponse(sipServletResponse.getStatus());
		if (logger.isInfoEnabled()) {
			logger.info("Sending on the first call leg "
					+ responseToOriginalRequest.toString());
		}
		responseToOriginalRequest.send();
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Received register request: " + req.getTo());
		}
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		HashMap<String, String> users = (HashMap<String, String>) getServletContext()
				.getAttribute("registeredUsersMap");
		if (users == null)
			users = new HashMap<String, String>();
		getServletContext().setAttribute("registeredUsersMap", users);

		Address address = req.getAddressHeader(CONTACT_HEADER);
		String fromURI = req.getFrom().getURI().toString();

		int expires = address.getExpires();
		if (expires < 0) {
			expires = req.getExpires();
		}
		if (expires == 0) {
			users.remove(fromURI);
			if (logger.isInfoEnabled()) {
				logger.info("User " + fromURI + " unregistered");
			}
		} else {
			resp.setAddressHeader(CONTACT_HEADER, address);
			users.put(fromURI, address.getURI().toString());
			if (logger.isInfoEnabled()) {
				logger.info("User " + fromURI
						+ " registered with an Expire time of " + expires);
			}
		}

		resp.send();
	}

	// Ro Client Listener Implementation

	public void creditGranted(long amount, boolean finalUnits) throws Exception {
		if (CURRENT_STATE == SENT_INITIAL_RESERVATION) {
			CURRENT_STATE = GRANTED_INITIAL_RESERVATION;
			if (logger.isInfoEnabled()) {
				logger.info("credit granted : ");
			}
			doInviteChargingAllowed(this.inviteRequest);
		}
	}

	public void creditDenied(int failureCode) throws Exception {
		if (CURRENT_STATE == SENT_INITIAL_RESERVATION) {
			CURRENT_STATE = IDLE;
			if (logger.isInfoEnabled()) {
				logger.info("credit denied: ");
			}
			inviteRequest
					.createResponse(SipServletResponse.SC_PAYMENT_REQUIRED)
					.send();
		}
	}

	public void creditTerminated() throws Exception {
		if (CURRENT_STATE == STARTED_CALL_CHARGING) {
			if (logger.isInfoEnabled()) {
				logger.info("credit terminated : ");
			}
			Iterator<SipSession> sessions = (Iterator<SipSession>) this.inviteRequest
					.getApplicationSession().getSessions("SIP");
			while (sessions.hasNext()) {
				SipSession session = sessions.next();
				if (session.getState() != State.TERMINATED) {
					SipServletRequest bye= session.createRequest("BYE");
					HashMap<String, String> users = (HashMap<String, String>) getServletContext()
					.getAttribute("registeredUsersMap");
					if (users != null) {

						String contact = users.get(bye.getFrom().toString());
						if (contact != null) {
							SipFactory sipFactory = (SipFactory) getServletContext()
								.getAttribute(SIP_FACTORY);

							SipURI sipUri = (SipURI) sipFactory.createURI(contact);
							bye.setRequestURI(sipUri);
						}
					}
					if (logger.isInfoEnabled()) {
						logger.info("sending byeRequest = " + bye);
					}
					bye.send();
				}
			}
			CURRENT_STATE = IDLE;
		}
	}

}
