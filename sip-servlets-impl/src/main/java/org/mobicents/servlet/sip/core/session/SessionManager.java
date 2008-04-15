package org.mobicents.servlet.sip.core.session;

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;

public class SessionManager {
	private Map<String, SipApplicationSessionImpl> appSessions = new HashMap<String, SipApplicationSessionImpl>();

	private Map<String, SipSessionImpl> sipSessions = new HashMap<String, SipSessionImpl>();

	private Object lock = new Object();

	private static transient Log logger = LogFactory
			.getLog(SessionManager.class);

	/**
	 * Associates requests to sessions. It uses
	 * (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID) intially as a session id until the
	 * to-tag becomes available then changes the session key to
	 * (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID,TO-TAG) expecting all further
	 * requests to have a to-tag.
	 * 
	 * In the future we must handle multiple responses to the same request more
	 * gracefully.
	 * 
	 * @param requestEvent
	 *            a JAIN SIP request event
	 * @return
	 */
	public SipSessionImpl getRequestSession(RequestEvent requestEvent,
			Transaction transaction) {
		
		/*
		 * Vlad -- you can remove this if no longer relevant.
		 * 
		 */
		Request request = requestEvent.getRequest();
		SipSessionImpl session = null;
		SipProvider provider = (SipProvider) requestEvent.getSource();
		try {
			String initialSessionId = getInitialSessionId(request); // without
																	// to-tag
			
			 //with  to-tag  (null if  there is  no  to-tag) 
			String ackSessionId = getAcknoledgedSessionId(request); 
		
		
			if (ackSessionId == null) // if the we still dont have a dialog
										// (to-tag)
			{
				if (sipSessions.get(initialSessionId) != null) {
					// See if we have previous initial session (happens when
					// handling subsequent REGSITERs).
					session = sipSessions.get(initialSessionId);
					logger.info("Found initial session " + initialSessionId);
				} else {
					session = new SipSessionImpl(provider, null);
					sipSessions.put(initialSessionId, session);
					logger.info("Created initial session " + initialSessionId);
				}
			} else {
				// If ack-ed session exists (dialog) delete the initial session
				// (the one without to-tag)
				if (sipSessions.get(ackSessionId) == null
						&& sipSessions.get(initialSessionId) != null) {
					synchronized (lock) {
						sipSessions.put(ackSessionId, sipSessions
								.get(initialSessionId));
						sipSessions.remove(initialSessionId); 
					}
					logger
							.info("Got rid of the initial session, subsequent requests will have the to tag.");
				}
				session = sipSessions.get(ackSessionId);

				logger.info("Found acknoledged session " + ackSessionId);
			}
		} catch (Exception ex) {
			logger.info(ex.getMessage());
			throw new RuntimeException("Error associating session!", ex);
		}
		return session;

	}

	// Gives (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID)
	public static String getInitialSessionId(Message request) {
		String sessionId = ((FromHeader) request.getHeader(FromHeader.NAME))
				.getAddress().getURI().toString()
				+ ((FromHeader) request.getHeader(FromHeader.NAME))
						.getParameter("tag")
				+ ((ToHeader) request.getHeader(ToHeader.NAME)).getAddress()
						.getURI().toString()
				+ ((CallIdHeader) request.getHeader(CallIdHeader.NAME))
						.getCallId();

		return sessionId;
	}

	// Gives (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID,TO-TAG)
	public static String getAcknoledgedSessionId(Message request) {

		if (((ToHeader) request.getHeader(ToHeader.NAME)).getTag() == null)
			return null;

		String sessionId = getInitialSessionId(request)
				+ ((ToHeader) request.getHeader(ToHeader.NAME)).getTag();

		return sessionId;
	}

	public void addApplicationSession(String id,
			SipApplicationSessionImpl appSession) {
		if (appSessions.get(id) != null)
			throw new IllegalArgumentException(
					"Application session already exists for " + id);
		appSessions.put(id, appSession);
	}

	public SipApplicationSessionImpl getApplicationSession(String id) {
		return appSessions.get(id);
	}

	public static boolean isDialogCreatingRequest(RequestEvent requestEvent) {
		return (requestEvent.getRequest().getMethod().equals(Request.INVITE) || requestEvent
				.getRequest().getMethod().equals(Request.SUBSCRIBE));
	}
}
