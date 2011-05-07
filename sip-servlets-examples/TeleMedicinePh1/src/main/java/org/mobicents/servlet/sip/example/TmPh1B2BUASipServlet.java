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
import java.util.concurrent.ConcurrentHashMap;

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

import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author Shay Matasaro
 * 
 */
public class TmPh1B2BUASipServlet extends SipServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger

	.getLogger(TmPh1B2BUASipServlet.class);
	private static final String CONTACT_HEADER = "Contact";

	Map<String, String> pfileUris = null;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call forwarding B2BUA sip servlet has been started");
		super.init(servletConfig);
		String serverAdress = System.getProperty("jboss.bind.address");
		String url = "http://" + serverAdress
				+ ":8080/call-forwarding-1.6.0-SNAPSHOT/";
		pfileUris = new ConcurrentHashMap<String, String>();
		pfileUris.put("david", url + "david.html");
		pfileUris.put("rogers", url + "rogers.html");
	}

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got INVITE: " + request.toString());
			logger.info(request.getFrom().getURI().toString());
		}
		if (!request.isInitial()) {
			B2buaHelper helper = request.getB2buaHelper();
			SipSession peerSession = helper.getLinkedSession(request
					.getSession());

			SipServletRequest forkedRequest = helper.createRequest(peerSession,
					request, null);
			forkedRequest.getSession().setAttribute("originalRequest", request);
			forkedRequest.send();

		} else {
			String destination = request.getTo().getURI().toString();
			destination = shortName(destination);
			if (destination.equalsIgnoreCase("doctor")||destination.equalsIgnoreCase("911")) {
				destination = "receiver";
				logger.info("forward alias found and substituted to "
						+ destination);
			} else {
				logger
						.info("invite not using an alias, keeping same destination "
								+ destination);
			}
			HashMap<String, String> users = (HashMap<String, String>) getServletContext()
					.getAttribute("registeredUsersMap");
			String address = null;
			if (users != null) {
				address = users.get(destination);
				logger.info("printing registered user map");
				for (String user : users.keySet()) {
					logger.info("User " + user + "   destination  "
							+ users.get(user));
				}
			} else {
				logger.warn("User registration DB  is empty");
				return;
			}

			logger.info("address is : " + address);
			if (address != null && address.length() > 0) {
				B2buaHelper helper = request.getB2buaHelper();

				SipFactory sipFactory = (SipFactory) getServletContext()
						.getAttribute(SIP_FACTORY);

				Map<String, List<String>> headers = new HashMap<String, List<String>>();
				List<String> toHeaderSet = new ArrayList<String>();
				toHeaderSet.add(address);
				headers.put("To", toHeaderSet);

				SipServletRequest forkedRequest = helper.createRequest(request,
						true, headers);
				if (address.indexOf("receiver@") > -1) {
					String patient = request.getFrom().getURI().toString();
					patient = shortName(patient);
					String pfileurl = pfileUris.get(patient);
					if (pfileurl==null) {
						logger.warn("Could not find patient name for doctor call : " + patient);
						logger.warn("Redirceting to defualt patient(david)");
						pfileurl = pfileUris.get("david");
						
					}
					
					forkedRequest.addHeader("pfile", pfileurl);
					
				}
				else
				{
					logger.warn("this is not a doctor call");
				}
				SipURI sipUri = (SipURI) sipFactory.createURI(address);
				forkedRequest.setRequestURI(sipUri);
				if (logger.isInfoEnabled()) {
					logger.info("forkedRequest = " + forkedRequest);
				}
				forkedRequest.getSession().setAttribute("originalRequest",
						request);

				forkedRequest.send();
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("INVITE has not been forwarded.");
				}
			}
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

		// we forward the BYE
		SipSession session = request.getSession();
		B2buaHelper helper = request.getB2buaHelper();
		SipSession linkedSession = helper.getLinkedSession(session);
		SipServletRequest forkedRequest = linkedSession.createRequest("BYE");
		if (logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + forkedRequest);
		}
		forkedRequest.send();
		if (session != null && session.isValid()) {
			session.invalidate();
		}
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

	@SuppressWarnings("unchecked")
	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		logger.info("Received register request:: " + req.getTo());
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = req.createResponse(response);
		HashMap<String, String> users = (HashMap<String, String>) getServletContext()
				.getAttribute("registeredUsersMap");
		if (users == null)
			users = new HashMap<String, String>();
		getServletContext().setAttribute("registeredUsersMap", users);

		Address address = req.getAddressHeader(CONTACT_HEADER);
		String fromURI = req.getFrom().getURI().toString();
		fromURI = shortName(fromURI);
		

		int expires = address.getExpires();
		if (expires < 0) {
			expires = req.getExpires();
		}
		if (expires == 0) {
			users.remove(fromURI);
			logger.info("User " + fromURI + " unregistered");
		} else {
			resp.setAddressHeader(CONTACT_HEADER, address);
			users.put(fromURI, address.getURI().toString());
			logger.info("User " + fromURI
					+ " registered with an Expire time of " + expires);
		}

		resp.send();
	}

	private String shortName(String fromURI) {
		fromURI = fromURI.replaceFirst("sip:", "");
		if (fromURI.indexOf("@")>0) {
			fromURI = fromURI.substring(0, fromURI.indexOf('@'));	
		}
		return fromURI;
	}
}