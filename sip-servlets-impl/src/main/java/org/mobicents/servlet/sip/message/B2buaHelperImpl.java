package org.mobicents.servlet.sip.message;

import gov.nist.core.Host;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.UAMode;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.session.*;

/**
 * Implementation of the B2BUA helper class.
 * 
 * @author mranga
 */

public class B2buaHelperImpl implements B2buaHelper {

	private SipServletRequestImpl sipServletRequest;

	private ConcurrentHashMap<SipSessionImpl, SipSessionImpl> sessionMap = new ConcurrentHashMap<SipSessionImpl, SipSessionImpl>();

	private SipProvider provider;

	private static Log logger = LogFactory.getLog(B2buaHelperImpl.class);

	public B2buaHelperImpl(SipServletRequestImpl sipServletRequest) {
		this.sipServletRequest = sipServletRequest;
		this.provider = sipServletRequest.provider;
	}

	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean linked, Map<String, Set<String>> headerMap)
			throws IllegalArgumentException {

		try {
			SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			Request newRequest = (Request) origRequestImpl.message.clone();
			newRequest.removeContent();
			// SipSessionImpl ( Dialog dialog, Transaction transaction,
			// SipApplicationSessionImpl sipApp)

			ViaHeader viaHeader = (ViaHeader) newRequest
					.getHeader(ViaHeader.NAME);
			newRequest.removeHeader(ViaHeader.NAME);
			ViaHeader newViaHeader = JainSipUtils.createViaHeader(
					this.provider, viaHeader.getTransport());
			newRequest.setHeader(newViaHeader);
			((FromHeader) newRequest.getHeader(FromHeader.NAME))
					.removeParameter("tag");
			((ToHeader) newRequest.getHeader(ToHeader.NAME))
					.removeParameter("tag");

			for (String headerName : headerMap.keySet()) {
				for (String value : headerMap.get(headerName)) {
					Header header = SipFactories.headerFactory.createHeader(
							headerName, value);
					newRequest.addHeader(header);
				}
			}
			
			SipSessionImpl originalSession = (SipSessionImpl) origRequestImpl.getDialog()
					.getApplicationData();
			SipApplicationSessionImpl appSession = originalSession
					.getSipApplicationSession();

			SipSessionImpl session = new SipSessionImpl(provider, appSession);

			SipServletRequestImpl retVal = new SipServletRequestImpl(
					newRequest,
					provider,
					session, null, null, true);

			if (linked) {
				sessionMap.put(originalSession, session);
				sessionMap.put(session, originalSession);
				origRequestImpl.setLinkedRequest(retVal);
				retVal.setLinkedRequest(origRequestImpl);
			}

			return retVal;
		} catch (Exception ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg ecnountered while creatigng b2bua", ex);
		}
	}

	public SipServletRequest createRequest(SipSession session,
			SipServletRequest origRequest, Map<String, Set<String>> headerMap)
			throws IllegalArgumentException {
		try {
			SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			SipSessionImpl sessionImpl = (SipSessionImpl) session;

			Dialog dialog = sessionImpl.getSessionCreatingDialog();

			Request newRequest = dialog.createRequest(((Request) origRequest)
					.getMethod());
			// SipSessionImpl ( Dialog dialog, Transaction transaction,
			// SipApplicationSessionImpl sipApp)

			for (String headerName : headerMap.keySet()) {
				for (String value : headerMap.get(headerName)) {
					Header header = SipFactories.headerFactory.createHeader(
							headerName, value);
					newRequest.addHeader(header);
				}
			}
			
			SipSessionImpl originalSession = (SipSessionImpl) origRequestImpl.getDialog()
					.getApplicationData();

			SipServletRequest retVal = new SipServletRequestImpl(newRequest,provider,
					session, null, null, true);

			sessionMap.put(originalSession, sessionImpl);
			sessionMap.put(sessionImpl, originalSession);

			return retVal;
		} catch (Exception ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg ecnountered while creatigng b2bua", ex);
		}
	}

	public SipServletResponse createResponseToOriginalRequest(
			SipSession session, int status, String reasonPhrase) {

		if (session == null)
			throw new NullPointerException("Null arg");

		SipSessionImpl sipSession = (SipSessionImpl) session;

		Transaction trans = sipSession.getSessionCreatingTransaction();

		if (!(trans instanceof ServerTransaction)) {
			throw new IllegalStateException(
					"Was expecting to find a server transaction ");
		}
		ServerTransaction st = (ServerTransaction) trans;

		Request request = st.getRequest();
		try {
			Response response = SipFactories.messageFactory.createResponse(
					status, request);
			if (reasonPhrase != null)
				response.setReasonPhrase(reasonPhrase);

			SipServletResponseImpl retval = new SipServletResponseImpl(
					response, sipSession.getProvider(), st, sipSession,
					sipSession.getSessionCreatingDialog());
			return retval;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("bad input argument", ex);
		}
	}

	public SipSession getLinkedSession(SipSession session) {
		return this.sessionMap.get((SipSessionImpl) session );
		
	}

	public SipServletRequest getLinkedSipServletRequest(SipServletRequest req) {
		return ((SipServletRequestImpl)req).getLinkedRequest();
	}

	public List<SipServletMessage> getPendingMessages(SipSession session,
			UAMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public void linkSipSessions(SipSession session1, SipSession session2) {
		// TODO Auto-generated method stub

	}

	public void unlinkSipSessions(SipSession session) {
		// TODO Auto-generated method stub

	}

}
