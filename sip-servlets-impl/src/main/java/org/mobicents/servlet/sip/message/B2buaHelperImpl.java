package org.mobicents.servlet.sip.message;

import java.text.ParseException;
import java.util.ArrayList;
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
import javax.sip.ServerTransaction;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;

/**
 * Implementation of the B2BUA helper class.
 * 
 * @author mranga
 */

public class B2buaHelperImpl implements B2buaHelper {

	private SipServletRequestImpl sipServletRequest;

	private ConcurrentHashMap<SipSessionImpl, SipSessionImpl> sessionMap = new ConcurrentHashMap<SipSessionImpl, SipSessionImpl>();

	private SipFactoryImpl sipFactoryImpl;

	private static Log logger = LogFactory.getLog(B2buaHelperImpl.class);

	public B2buaHelperImpl(SipServletRequestImpl sipServletRequest) {
		this.sipServletRequest = sipServletRequest;
		this.sipFactoryImpl = sipServletRequest.sipFactoryImpl; 
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createRequest(javax.servlet.sip.SipServletRequest, boolean, java.util.Map)
	 */
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
					sipFactoryImpl.getSipProviders(), viaHeader.getTransport(), null);
			newRequest.setHeader(newViaHeader);
			((FromHeader) newRequest.getHeader(FromHeader.NAME))
					.removeParameter("tag");
			((ToHeader) newRequest.getHeader(ToHeader.NAME))
					.removeParameter("tag");
			// Remove the route header ( will point to us ).
			newRequest.removeHeader(RouteHeader.NAME);
			String tag = Integer.toString((int) (Math.random()*1000));
			((FromHeader) newRequest.getHeader(FromHeader.NAME)).setParameter("tag", tag);
			
			if(headerMap != null) {
				for (String headerName : headerMap.keySet()) {
					for (String value : headerMap.get(headerName)) {
						Header header = SipFactories.headerFactory.createHeader(
								headerName, value);
						newRequest.addHeader(header);
					}
				}
			}
			TransactionApplicationData transactionApplicationData = 
				(TransactionApplicationData) origRequestImpl.getDialog().getApplicationData();
			
			SipSessionImpl originalSession = transactionApplicationData.getSipSession();
			SipApplicationSessionImpl appSession = originalSession
					.getSipApplicationSession();

			SipSessionImpl session = new SipSessionImpl(sipFactoryImpl, appSession);
			session.setHandler(originalSession.getHandler());
			appSession.setSipContext(((SipApplicationSessionImpl)session.getApplicationSession()).getSipContext());
			
			SipServletRequestImpl retVal = new SipServletRequestImpl(
					newRequest,
					sipFactoryImpl,
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createRequest(javax.servlet.sip.SipSession, javax.servlet.sip.SipServletRequest, java.util.Map)
	 */
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

			logger.debug("newRequest = " + newRequest);
			SipServletRequest retVal = new SipServletRequestImpl(newRequest,sipFactoryImpl,
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createResponseToOriginalRequest(javax.servlet.sip.SipSession, int, java.lang.String)
	 */
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
					response, sipFactoryImpl, st, sipSession,
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
		SipSessionImpl sipSessionImpl = (SipSessionImpl) session;
		List<SipServletMessage> retval = new ArrayList<SipServletMessage> ();
		if ( mode.equals(UAMode.UAC)) {
			for ( Transaction transaction: sipSessionImpl.getOngoingTransactions()) {
				if ( transaction instanceof ClientTransaction && transaction.getState() != TransactionState.TERMINATED) {
					TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
					retval.add(tad.getSipServletMessage());
				}
			}
			
		} else {
			for ( Transaction transaction: sipSessionImpl.getOngoingTransactions()) {
				if ( transaction instanceof ServerTransaction && transaction.getState() != TransactionState.TERMINATED) {
					TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
					retval.add(tad.getSipServletMessage());
				}
			}
		}
		return retval;
	}

	public void linkSipSessions(SipSession session1, SipSession session2) {
		this.sessionMap.put((SipSessionImpl)session1, (SipSessionImpl)session2);
		this.sessionMap.put((SipSessionImpl) session2, (SipSessionImpl) session1);

	}

	public void unlinkSipSessions(SipSession session) {
		if ( session == null )throw new NullPointerException("Null arg");
		SipSessionImpl key = (SipSessionImpl) session;
		SipSessionImpl value  = this.sessionMap.get(key);
		if ( value != null) this.sessionMap.remove(value);
		this.sessionMap.remove(key);

	}

}
