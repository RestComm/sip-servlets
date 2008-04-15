package org.mobicents.servlet.sip.message;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.SipSession.State;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.session.SessionManager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;

/**
 * Implementation of the B2BUA helper class.
 * 
 * @author mranga
 * @author jean.deruelle
 */

public class B2buaHelperImpl implements B2buaHelper {
	private static Log logger = LogFactory.getLog(B2buaHelperImpl.class);
	
	protected static final HashSet<String> singletonHeadersNames = new HashSet<String>();
	static {
		singletonHeadersNames.add(FromHeader.NAME);
		singletonHeadersNames.add(ToHeader.NAME);
		singletonHeadersNames.add(CSeqHeader.NAME);
		singletonHeadersNames.add(CallIdHeader.NAME);
		singletonHeadersNames.add(MaxForwardsHeader.NAME);
		singletonHeadersNames.add(ContentLengthHeader.NAME);		
		singletonHeadersNames.add(ContentDispositionHeader.NAME);
		singletonHeadersNames.add(ContentTypeHeader.NAME);
		//TODO are there any other singleton headers ?
	}	
	
	//FIXME @jean.deruelle session map is never cleaned up => could lead to memory leak
	//shall we have a thread scanning for invalid sessions and removing them accordingly ?
	private ConcurrentHashMap<SipSessionImpl, SipSessionImpl> sessionMap = new ConcurrentHashMap<SipSessionImpl, SipSessionImpl>();

	private SipFactoryImpl sipFactoryImpl;

	private SipServletRequestImpl sipServletRequest;		
	
	/**
	 * 
	 * @param sipFactoryImpl
	 */
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
			
			//For non-REGISTER requests, the Contact header field is not copied 
			//but is populated by the container as usual
			newRequest.removeHeader(ContactHeader.NAME);

			//but If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			//They will be added later after the sip servlet request has been created
			Set<String> contactHeaderSet = new HashSet<String>();
			if(headerMap != null) {
				for (String headerName : headerMap.keySet()) {
					if(!headerName.equalsIgnoreCase(ContactHeader.NAME)) {
						for (String value : headerMap.get(headerName)) {							
							Header header = SipFactories.headerFactory.createHeader(
									headerName, value);
							if(! singletonHeadersNames.contains(header.getName())) {
								newRequest.addHeader(header);
							} else {
								newRequest.setHeader(header);
							}
						}
					} else {
						contactHeaderSet = headerMap.get(headerName);
					}
				}
			}			
			SipSessionImpl originalSession = origRequestImpl.getSipSession();
			SipApplicationSessionImpl appSession = originalSession
					.getSipApplicationSession();				
			
			String key = SessionManager.getSipSessionKey(origRequestImpl.getCurrentApplicationName(), newRequest);
			SipSessionImpl session = sipFactoryImpl.getSessionManager().getSipSession(key, true, sipFactoryImpl);			
			session.setSipApplicationSession(appSession);
			session.setHandler(originalSession.getHandler());
			appSession.setSipContext(session.getSipApplicationSession().getSipContext());
			
			SipServletRequestImpl newSipServletRequest = new SipServletRequestImpl(
					newRequest,
					sipFactoryImpl,
					session, null, null, true);			
			//JSR 289 Section 15.1.6
			newSipServletRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);
			
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			for (String contactHeaderValue : contactHeaderSet) {
				newSipServletRequest.addHeader(ContactHeader.NAME, contactHeaderValue);
			}
			
			origRequestImpl.setLinkedRequest(newSipServletRequest);
			newSipServletRequest.setLinkedRequest(origRequestImpl);
			if (linked) {
				sessionMap.put(originalSession, session);
				sessionMap.put(session, originalSession);				
			}

			return newSipServletRequest;
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
			
			Request newRequest = dialog.createRequest(((SipServletRequest) origRequest)
					.getMethod());
									
			SipSessionImpl originalSession = origRequestImpl.getSipSession();

			logger.info(origRequest.getSession());				
			logger.info(session);
			
			//For non-REGISTER requests, the Contact header field is not copied 
			//but is populated by the container as usual
			newRequest.removeHeader(ContactHeader.NAME);
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			//They will be added later after the sip servlet request has been created
			Set<String> contactHeaderSet = new HashSet<String>();
			if(headerMap != null) {
				for (String headerName : headerMap.keySet()) {
					if(!headerName.equalsIgnoreCase(ContactHeader.NAME)) {
						for (String value : headerMap.get(headerName)) {							
							Header header = SipFactories.headerFactory.createHeader(
									headerName, value);
							if(! singletonHeadersNames.contains(header.getName())) {
								newRequest.addHeader(header);
							} else {
								newRequest.setHeader(header);
							}
						}
					} else {
						contactHeaderSet = headerMap.get(headerName);
					}
				}
			}
			
			//we already have a dialog since it is a subsequent request			
			SipServletRequestImpl newSipServletRequest = new SipServletRequestImpl(newRequest,sipFactoryImpl,
					session, sessionImpl.getSessionCreatingTransaction(), dialog, false);
			//JSR 289 Section 15.1.6
			newSipServletRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			for (String contactHeaderValue : contactHeaderSet) {
				newSipServletRequest.addHeader(ContactHeader.NAME, contactHeaderValue);
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("newRequest = " + newRequest);
			}			
			
			origRequestImpl.setLinkedRequest(newSipServletRequest);
			newSipServletRequest.setLinkedRequest(origRequestImpl);			
			sessionMap.put(originalSession, sessionImpl);
			sessionMap.put(sessionImpl, originalSession);

			return newSipServletRequest;
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
			
			if(status ==  Response.OK) {
				ContactHeader contactHeader = 
					JainSipUtils.createContactForProvider(sipFactoryImpl.getSipProviders(), JainSipUtils.findTransport(request));
				response.addHeader(contactHeader);
			}
			
			SipServletResponseImpl retval = new SipServletResponseImpl(
					response, sipFactoryImpl, st, sipSession,
					sipSession.getSessionCreatingDialog(), sipServletRequest);			
			return retval;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("bad input argument", ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getLinkedSession(javax.servlet.sip.SipSession)
	 */
	public SipSession getLinkedSession(SipSession session) {
		if ( session == null )throw new NullPointerException("the argument is null");
		if(!session.isValid() || 
				State.TERMINATED.equals(((SipSessionImpl)session).getState())) {
			throw new IllegalArgumentException("the session is invalid");
		}
		return this.sessionMap.get((SipSessionImpl) session );
		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getLinkedSipServletRequest(javax.servlet.sip.SipServletRequest)
	 */
	public SipServletRequest getLinkedSipServletRequest(SipServletRequest req) {
		return ((SipServletRequestImpl)req).getLinkedRequest();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getPendingMessages(javax.servlet.sip.SipSession, javax.servlet.sip.UAMode)
	 */
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
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#linkSipSessions(javax.servlet.sip.SipSession, javax.servlet.sip.SipSession)
	 */
	public void linkSipSessions(SipSession session1, SipSession session2) {
		if ( session1 == null )throw new NullPointerException("First argument is null");
		if ( session2 == null )throw new NullPointerException("Second argument is null");
		
		if(!session1.isValid() || !session2.isValid() || 
				State.TERMINATED.equals(((SipSessionImpl)session1).getState()) ||
				State.TERMINATED.equals(((SipSessionImpl)session2).getState()) ||
				!session1.getApplicationSession().equals(session2.getApplicationSession()) ||
				sessionMap.get(session1) != null ||
				sessionMap.get(session2) != null) {
			throw new IllegalArgumentException("either of the specified sessions has been terminated " +
					"or the sessions do not belong to the same application session or " +
					"one or both the sessions are already linked with some other session(s)");
		}
		this.sessionMap.put((SipSessionImpl)session1, (SipSessionImpl)session2);
		this.sessionMap.put((SipSessionImpl) session2, (SipSessionImpl) session1);

	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#unlinkSipSessions(javax.servlet.sip.SipSession)
	 */
	public void unlinkSipSessions(SipSession session) {		
		if ( session == null )throw new NullPointerException("the argument is null");
		if(!session.isValid() || 
				State.TERMINATED.equals(((SipSessionImpl)session).getState()) ||
				sessionMap.get(session) == null) {
			throw new IllegalArgumentException("the session is not currently linked to another session or it has been terminated");
		}
		SipSessionImpl key = (SipSessionImpl) session;
		SipSessionImpl value  = this.sessionMap.get(key);
		if ( value != null) this.sessionMap.remove(value);
		this.sessionMap.remove(key);
	}

}
