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
package org.mobicents.servlet.sip.message;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ListeningPoint;
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
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * Implementation of the B2BUA helper class.
 * 
 * 
 * @author mranga
 * @author Jean Deruelle
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
	//FIXME this is not a one to one mapping - B2BUA can link to more than one other sip session
	private Map<MobicentsSipSession, MobicentsSipSession> sessionMap = new ConcurrentHashMap<MobicentsSipSession, MobicentsSipSession>();
	//Map to handle responses to original request and cancel on original request
	private Map<String, SipServletRequest> originalRequestMap = new ConcurrentHashMap<String, SipServletRequest>();

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
			boolean linked, Map<String, List<String>> headerMap) {

		try {
			SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			Request newRequest = (Request) origRequestImpl.message.clone();
			//content should be copied too, so commented out
//		 	newRequest.removeContent();				
			//removing the via header from original request
			newRequest.removeHeader(ViaHeader.NAME);	
			
			((FromHeader) newRequest.getHeader(FromHeader.NAME))
					.removeParameter("tag");
			((ToHeader) newRequest.getHeader(ToHeader.NAME))
					.removeParameter("tag");
			// Remove the route header ( will point to us ).
			newRequest.removeHeader(RouteHeader.NAME);
			String tag = Integer.toString((int) (Math.random()*1000));
			((FromHeader) newRequest.getHeader(FromHeader.NAME)).setParameter("tag", tag);
			
			// Remove the record route headers. This is a new call leg.
			newRequest.removeHeader(RecordRouteHeader.NAME);
			
			//For non-REGISTER requests, the Contact header field is not copied 
			//but is populated by the container as usual
			if(!Request.REGISTER.equalsIgnoreCase(origRequest.getMethod())) {
				newRequest.removeHeader(ContactHeader.NAME);
			}

			List<String> contactHeaderSet = retrieveContactHeaders(headerMap,
					newRequest);			
			MobicentsSipSession originalSession = origRequestImpl.getSipSession();
			MobicentsSipApplicationSession appSession = originalSession
					.getSipApplicationSession();				
			
			SipSessionKey key = SessionManagerUtil.getSipSessionKey(originalSession.getKey().getApplicationName(), newRequest, false);
			MobicentsSipSession session = ((SipManager)appSession.getSipContext().getManager()).getSipSession(key, true, sipFactoryImpl, appSession);			
			session.setHandler(originalSession.getHandler());
						
			SipServletRequestImpl newSipServletRequest = new SipServletRequestImpl(
					newRequest,
					sipFactoryImpl,					
					session, 
					null, 
					null, 
					JainSipUtils.dialogCreatingMethods.contains(newRequest.getMethod()));			
			//JSR 289 Section 15.1.6
			newSipServletRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);			
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			for (String contactHeaderValue : contactHeaderSet) {
				newSipServletRequest.addHeader(ContactHeader.NAME, contactHeaderValue);
			}
			
			originalRequestMap.put(originalSession.getId(), origRequest);
			originalRequestMap.put(session.getId(), newSipServletRequest);
			
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
			SipServletRequest origRequest, Map<String, List<String>> headerMap) {
		try {
			SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			MobicentsSipSession sessionImpl = (MobicentsSipSession) session;

			Dialog dialog = sessionImpl.getSessionCreatingDialog();
			
			Request newRequest = dialog.createRequest(origRequest.getMethod());
									
			MobicentsSipSession originalSession = origRequestImpl.getSipSession();
			if(logger.isInfoEnabled()) {
				logger.info(origRequest.getSession());				
				logger.info(session);
			}
			
			//For non-REGISTER requests, the Contact header field is not copied 
			//but is populated by the container as usual
			if(!Request.REGISTER.equalsIgnoreCase(origRequest.getMethod())) {
				newRequest.removeHeader(ContactHeader.NAME);
			}
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			//They will be added later after the sip servlet request has been created
			List<String> contactHeaderSet = retrieveContactHeaders(headerMap,
					newRequest);
			
			//we already have a dialog since it is a subsequent request			
			SipServletRequestImpl newSipServletRequest = new SipServletRequestImpl(
					newRequest,
					sipFactoryImpl,
					sessionImpl, 
					sessionImpl.getSessionCreatingTransaction(), 
					dialog, 
					JainSipUtils.dialogCreatingMethods.contains(newRequest.getMethod()));
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
			
			originalRequestMap.put(originalSession.getId(), origRequest);
			originalRequestMap.put(session.getId(), newSipServletRequest);
			
			sessionMap.put(originalSession, sessionImpl);
			sessionMap.put(sessionImpl, originalSession);

			return newSipServletRequest;
		} catch (Exception ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg ecnountered while creatigng b2bua", ex);
		}
	}

	/**
	 * @param headerMap
	 * @param newRequest
	 * @return
	 * @throws ParseException
	 */
	private static List<String> retrieveContactHeaders(
			Map<String, List<String>> headerMap, Request newRequest)
			throws ParseException {
		List<String> contactHeaderList = new ArrayList<String>();
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
					contactHeaderList = headerMap.get(headerName);
				}
			}
		}
		return contactHeaderList;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createResponseToOriginalRequest(javax.servlet.sip.SipSession, int, java.lang.String)
	 */
	public SipServletResponse createResponseToOriginalRequest(
			SipSession session, int status, String reasonPhrase) {

		if (session == null) {
			throw new NullPointerException("Null arg");
		}

		MobicentsSipSession sipSession = (MobicentsSipSession) session;

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
			if (reasonPhrase != null) {
				response.setReasonPhrase(reasonPhrase);
			}
						
			ListIterator<RecordRouteHeader> recordRouteHeaders = request.getHeaders(RecordRouteHeader.NAME);			
			while (recordRouteHeaders.hasNext()) {
				RecordRouteHeader recordRouteHeader = recordRouteHeaders.next();				
				response.addHeader(recordRouteHeader);
			}			
			
			if(status ==  Response.OK) {
				String transport = null;
				ViaHeader viaHeader = ((ViaHeader) request.getHeader(ViaHeader.NAME));
				if(viaHeader != null) {
					transport = viaHeader.getTransport();
				}
				if(transport == null || transport.length() <1) {
					transport = JainSipUtils.findTransport(request);
				}
					
				ContactHeader contactHeader = 
					JainSipUtils.createContactHeader(
							sipFactoryImpl.getSipNetworkInterfaceManager(), transport, null);
				response.addHeader(contactHeader);
			}
			
			SipServletResponseImpl newSipServletResponse = new SipServletResponseImpl(
					response, sipFactoryImpl, st, sipSession,
					sipSession.getSessionCreatingDialog());
			newSipServletResponse.setOriginalRequest(sipServletRequest);
			return newSipServletResponse;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("bad input argument", ex);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getLinkedSession(javax.servlet.sip.SipSession)
	 */
	public SipSession getLinkedSession(SipSession session) {
		if ( session == null) { 
			throw new NullPointerException("the argument is null");
		}
		if(!session.isValid()) {
			throw new IllegalArgumentException("the session is invalid");
		}
		return this.sessionMap.get((MobicentsSipSession) session );
		
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
		MobicentsSipSession sipSessionImpl = (MobicentsSipSession) session;
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
		if ( session1 == null) {
			throw new NullPointerException("First argument is null");
		}
		if ( session2 == null) {
			throw new NullPointerException("Second argument is null");
		}
		
		if(!session1.isValid() || !session2.isValid() || 
				State.TERMINATED.equals(((MobicentsSipSession)session1).getState()) ||
				State.TERMINATED.equals(((MobicentsSipSession)session2).getState()) ||
				!session1.getApplicationSession().equals(session2.getApplicationSession()) ||
				sessionMap.get(session1) != null ||
				sessionMap.get(session2) != null) {
			throw new IllegalArgumentException("either of the specified sessions has been terminated " +
					"or the sessions do not belong to the same application session or " +
					"one or both the sessions are already linked with some other session(s)");
		}
		this.sessionMap.put((MobicentsSipSession)session1, (MobicentsSipSession)session2);
		this.sessionMap.put((MobicentsSipSession) session2, (MobicentsSipSession) session1);

	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#unlinkSipSessions(javax.servlet.sip.SipSession)
	 */
	public void unlinkSipSessions(SipSession session) {		
		if ( session == null) { 
			throw new NullPointerException("the argument is null");
		}
		if(!session.isValid() || 
				State.TERMINATED.equals(((MobicentsSipSession)session).getState()) ||
				sessionMap.get(session) == null) {
			throw new IllegalArgumentException("the session is not currently linked to another session or it has been terminated");
		}
		MobicentsSipSession key = (MobicentsSipSession) session;
		MobicentsSipSession value  = this.sessionMap.get(key);
		if (value != null) {
			this.sessionMap.remove(value);
		}
		this.sessionMap.remove(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest) {
		try {
			SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			Request newRequest = (Request) origRequestImpl.message.clone();
			//removing the via header from original request
			newRequest.removeHeader(ViaHeader.NAME);	
			
			//assign a new from tag
			((FromHeader) newRequest.getHeader(FromHeader.NAME))
					.removeParameter("tag");
			String tag = Integer.toString((int) (Math.random()*1000));
			((FromHeader) newRequest.getHeader(FromHeader.NAME)).setParameter("tag", tag);
			//remove the to tag
			((ToHeader) newRequest.getHeader(ToHeader.NAME))
					.removeParameter("tag");
			// Remove the route header ( will point to us ).
			newRequest.removeHeader(RouteHeader.NAME);
			
			// Remove the record route headers. This is a new call leg.
			newRequest.removeHeader(RecordRouteHeader.NAME);
			
			//For non-REGISTER requests, the Contact header field is not copied 
			//but is populated by the container as usual
			if(!Request.REGISTER.equalsIgnoreCase(origRequest.getMethod())) {
				newRequest.removeHeader(ContactHeader.NAME);
			}
			//Creating new call id
			ExtendedListeningPoint extendedListeningPoint = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(ListeningPoint.UDP, false);
			CallIdHeader callIdHeader = SipFactories.headerFactory.createCallIdHeader(extendedListeningPoint.getSipProvider().getNewCallId().getCallId());
			newRequest.setHeader(callIdHeader);
			
			MobicentsSipSession originalSession = origRequestImpl.getSipSession();
			MobicentsSipApplicationSession originalAppSession = originalSession
					.getSipApplicationSession();				
			
			SipSessionKey key = SessionManagerUtil.getSipSessionKey(originalSession.getKey().getApplicationName(), newRequest, false);
			MobicentsSipSession session = ((SipManager)originalAppSession.getSipContext().getManager()).getSipSession(key, true, sipFactoryImpl, originalAppSession);			
			session.setHandler(originalSession.getHandler());
			
			SipServletRequestImpl newSipServletRequest = new SipServletRequestImpl(
					newRequest,
					sipFactoryImpl,					
					session, 
					null, 
					null, 
					JainSipUtils.dialogCreatingMethods.contains(newRequest.getMethod()));			
			//JSR 289 Section 15.1.6
			newSipServletRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);			
			
			sessionMap.put(originalSession, session);
			sessionMap.put(session, originalSession);				

			originalRequestMap.put(originalSession.getId(), origRequest);
			originalRequestMap.put(session.getId(), newSipServletRequest);
			
			return newSipServletRequest;
		} catch (Exception ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg ecnountered while creatigng b2bua", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SipServletRequest createCancel(SipSession session) {
		SipServletRequest sipServletRequest = originalRequestMap.get(session.getId());
		return sipServletRequest.createCancel();
	}

}
