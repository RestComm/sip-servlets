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

import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.Rel100Exception;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * Implementation of the sip servlet response interface
 *
 */
public class SipServletResponseImpl extends SipServletMessageImpl implements
		SipServletResponse {
	
	private static final long serialVersionUID = 1L;

	public static final String REL100_OPTION_TAG = "100rel";

	private static final Logger logger = Logger.getLogger(SipServletResponseImpl.class);
	
	Response response;
	SipServletRequestImpl originalRequest;
	ProxyBranch proxyBranch;

	private boolean isProxiedResponse;
	private boolean isResponseForwardedUpstream;
	private boolean isAckGenerated;
	private boolean isPrackGenerated;
	//Added for TCK test SipServletResponseTest.testSend101
	private boolean hasBeenReceived;
	
	/**
	 * Constructor
	 * @param response
	 * @param sipFactoryImpl
	 * @param transaction
	 * @param session
	 * @param dialog
	 * @param originalRequest
	 */
	public SipServletResponseImpl (
			Response response, 
			SipFactoryImpl sipFactoryImpl, 
			Transaction transaction, 
			MobicentsSipSession session, 
			Dialog dialog,
			boolean hasBeenReceived) {
		
		super(response, sipFactoryImpl, transaction, session, dialog);
		this.response = response;	
		setProxiedResponse(false);
		isResponseForwardedUpstream = false;
		isAckGenerated = false;
		this.hasBeenReceived = hasBeenReceived;
	}
	
	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}
	
	@Override
	public boolean isSystemHeader(String headerName) {
		String hName = getFullHeaderName(headerName);

		/*
		 * Contact is a system header field in messages other than REGISTER
		 * requests and responses, 3xx and 485 responses, and 200/OPTIONS
		 * responses.
		 */

		// This doesnt contain contact!!!!
		boolean isSystemHeader = JainSipUtils.SYSTEM_HEADERS.contains(hName);

		if (isSystemHeader) {
			return isSystemHeader;
		}

		boolean isContactSystem = false;
		Response sipResponse = (Response) this.message;

		String method = ((CSeqHeader) sipResponse.getHeader(CSeqHeader.NAME))
				.getMethod();
		//Killer condition, see comment above for meaning
		if (method.equals(Request.REGISTER)
				|| (Response.MULTIPLE_CHOICES <= sipResponse.getStatusCode() && sipResponse.getStatusCode() < Response.BAD_REQUEST)
				|| sipResponse.getStatusCode() == Response.AMBIGUOUS
				|| (sipResponse.getStatusCode() == Response.OK && method.equals(Request.OPTIONS))) {
			isContactSystem = false;
		} else {
			isContactSystem = true;
		}

		if (isContactSystem && hName.equals(ContactHeader.NAME)) {
			isSystemHeader = true;
		} else {
			isSystemHeader = false;
		}

		return isSystemHeader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#createAck()
	 */
	@SuppressWarnings("unchecked")
	public SipServletRequest createAck() {
		if(!Request.INVITE.equals(((SIPTransaction)getTransaction()).getMethod()) || (response.getStatusCode() >= 100 && response.getStatusCode() < 200) || isAckGenerated) {
			throw new IllegalStateException("the transaction state is such that it doesn't allow an ACK to be sent now, e.g. the original request was not an INVITE, or this response is provisional only, or an ACK has already been generated");
		}
		final MobicentsSipSession session = getSipSession();
		Dialog dialog = session.getSessionCreatingDialog();
		CSeqHeader cSeqHeader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
		SipServletRequestImpl sipServletAckRequest = null; 
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("dialog to create the ack Request " + dialog);
			}
			Request ackRequest = dialog.createAck(cSeqHeader.getSeqNumber());
			if(logger.isInfoEnabled()) {
				logger.info("ackRequest just created " + ackRequest);
			}
			//Application Routing to avoid going through the same app that created the ack
			ListIterator<RouteHeader> routeHeaders = ackRequest.getHeaders(RouteHeader.NAME);
			ackRequest.removeHeader(RouteHeader.NAME);
			while (routeHeaders.hasNext()) {
				RouteHeader routeHeader = routeHeaders.next();
				String routeAppNameHashed = ((SipURI)routeHeader .getAddress().getURI()).
					getParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME);
				String routeAppName = null;
				if(routeAppNameHashed != null) {
					routeAppName = sipFactoryImpl.getSipApplicationDispatcher().getApplicationNameFromHash(routeAppNameHashed);
				}
				if(routeAppName == null || !routeAppName.equals(getSipSession().getKey().getApplicationName())) {
					ackRequest.addHeader(routeHeader);
				}
			}
			sipServletAckRequest = new SipServletRequestImpl(
					ackRequest,
					this.sipFactoryImpl, 
					this.getSipSession(), 
					this.getTransaction(), 
					dialog, 
					false); 
			isAckGenerated = true;
		} catch (InvalidArgumentException e) {
			logger.error("Impossible to create the ACK",e);
		} catch (SipException e) {
			logger.error("Impossible to create the ACK",e);
		}		
		return sipServletAckRequest;
	}

	public SipServletRequest createPrack() throws Rel100Exception {
		if((response.getStatusCode() == 100 && response.getStatusCode() >= 200) || isPrackGenerated) {
			throw new IllegalStateException("the transaction state is such that it doesn't allow a PRACK to be sent now, or this response is provisional only, or a PRACK has already been generated");
		}
		if(!Request.INVITE.equals(getTransaction().getRequest().getMethod())) {
			throw new Rel100Exception(Rel100Exception.NOT_INVITE);
		}		
		final MobicentsSipSession session = getSipSession();
		Dialog dialog = session.getSessionCreatingDialog();
		SipServletRequestImpl sipServletPrackRequest = null; 
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("dialog to create the prack Request " + dialog);
			}
			Request prackRequest = dialog.createPrack(response);
			if(logger.isInfoEnabled()) {
				logger.info("prackRequest just created " + prackRequest);
			}
			//Application Routing to avoid going through the same app that created the ack
			ListIterator<RouteHeader> routeHeaders = prackRequest.getHeaders(RouteHeader.NAME);
			prackRequest.removeHeader(RouteHeader.NAME);
			while (routeHeaders.hasNext()) {
				RouteHeader routeHeader = routeHeaders.next();
				String routeAppNameHashed = ((SipURI)routeHeader .getAddress().getURI()).
					getParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME);
				String routeAppName = null;
				if(routeAppNameHashed != null) {
					routeAppName = sipFactoryImpl.getSipApplicationDispatcher().getApplicationNameFromHash(routeAppNameHashed);
				}
				if(routeAppName == null || !routeAppName.equals(getSipSession().getKey().getApplicationName())) {
					prackRequest.addHeader(routeHeader);
				}
			}
			sipServletPrackRequest = new SipServletRequestImpl(
					prackRequest,
					this.sipFactoryImpl, 
					this.getSipSession(), 
					this.getTransaction(), 
					dialog, 
					false); 
			isPrackGenerated = true;
		} catch (SipException e) {
			logger.error("Impossible to create the ACK",e);
		}		
		return sipServletPrackRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// Always return null
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getProxy()
	 */
	public Proxy getProxy() {
		if(proxyBranch != null) {
			return proxyBranch.getProxy();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getReasonPhrase()
	 */
	public String getReasonPhrase() {
		return response.getReasonPhrase();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getRequest()
	 */
	public SipServletRequest getRequest() {		
		return originalRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getStatus()
	 */
	public int getStatus() {
		return response.getStatusCode();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		// Always returns null.
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#sendReliably()
	 */
	public void sendReliably() throws Rel100Exception {
		final int statusCode = getStatus();
		if(statusCode == 100 || statusCode >= 200) {
			throw new Rel100Exception(Rel100Exception.NOT_1XX);
		}
		if(!Request.INVITE.equals(originalRequest.getMethod())) {
			throw new Rel100Exception(Rel100Exception.NOT_INVITE);
		}
		if(!REL100_OPTION_TAG.equals(originalRequest.getHeader(RequireHeader.NAME)) && !REL100_OPTION_TAG.equals(originalRequest.getHeader(SupportedHeader.NAME))) {
			throw new Rel100Exception(Rel100Exception.NO_REQ_SUPPORT);
		}
		send(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#setStatus(int)
	 */
	public void setStatus(int statusCode) {
		try {
			response.setStatusCode(statusCode);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int statusCode, String reasonPhrase) {
		try {
			response.setStatusCode(statusCode);
			response.setReasonPhrase(reasonPhrase);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {		
		return 0;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// Do nothing
	}

	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void send()  {
		send(false);
	}

	public void send(boolean sendReliably) {
		if(isMessageSent) {
			throw new IllegalStateException("message already sent");
		}
		if(hasBeenReceived) {
			throw new IllegalStateException("this response was received from downstream");
		}
		try {	
			final int statusCode = response.getStatusCode();
			final MobicentsSipSession session = getSipSession();
			final MobicentsSipApplicationSession sipApplicationSession = session.getSipApplicationSession();
			final SipApplicationSessionKey sipAppSessionKey = sipApplicationSession.getKey();
			final ProxyImpl proxy = session.getProxy();
			// if this is a proxy response and the branch is record routing http://code.google.com/p/mobicents/issues/detail?id=747
			// we add a record route
			if(proxy != null && proxy.getFinalBranchForSubsequentRequests() == null &&
						proxyBranch != null && proxyBranch.getRecordRoute() && 
						statusCode > Response.TRYING && 
						statusCode <= Response.SESSION_NOT_ACCEPTABLE) {	
				//Issue 112 fix by folsson: use the viaheader transport				
				final javax.sip.address.SipURI sipURI = JainSipUtils.createRecordRouteURI(
						sipFactoryImpl.getSipNetworkInterfaceManager(), 
						response);
				sipURI.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME, sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(session.getKey().getApplicationName()));
				sipURI.setParameter(MessageDispatcher.APP_ID, sipAppSessionKey.getId());
				sipURI.setLrParam();				
				final javax.sip.address.Address recordRouteAddress = 
					SipFactories.addressFactory.createAddress(sipURI);
				final RecordRouteHeader recordRouteHeader = 
					SipFactories.headerFactory.createRecordRouteHeader(recordRouteAddress);
				response.addFirst(recordRouteHeader);
				if(logger.isDebugEnabled()) {
					logger.debug("Record Route added to the response "+ recordRouteHeader);
				}
			}
			final ServerTransaction transaction = (ServerTransaction) getTransaction();
			 
			// Update Session state
			session.updateStateOnResponse(this, false);						
			// RFC 3265 : If a 200-class response matches such a SUBSCRIBE request,
			// it creates a new subscription and a new dialog.
			// Issue 1481 http://code.google.com/p/mobicents/issues/detail?id=1481
			// proxy should not add or remove subscription since there is no dialog associated with it
			if(Request.SUBSCRIBE.equals(getMethod()) && statusCode >= 200 && statusCode <= 300 && session.getProxy() == null) {					
				session.addSubscription(this);
			}
			// RFC 3262 Section 3 UAS Behavior
			if(sendReliably) {
				final Header requireHeader = SipFactories.headerFactory.createRequireHeader(REL100_OPTION_TAG);
				response.addHeader(requireHeader);
				final Header rseqHeader = SipFactories.headerFactory.createRSeqHeader(getTransactionApplicationData().getRseqNumber().getAndIncrement());
				response.addHeader(rseqHeader);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("sending response "+ this.message);
			}
			if(originalRequest == null && proxy != null) {
				String txid = ((ViaHeader) message.getHeader(ViaHeader.NAME)).getBranch();
				TransactionApplicationData tad = (TransactionApplicationData) proxy.getTransactionMap().get(txid);
				if(logger.isDebugEnabled()) {
					logger.debug("Trying to recover lost transaction for proxy: " + txid);
				}
				if(tad != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("Recovering lost transaction from the proxy transaction map is succesful: " + txid);
					}
					originalRequest = (SipServletRequestImpl) tad.getSipServletMessage();
				}
			}
			if(logger.isDebugEnabled()) {
				logger.debug("original req method "+ originalRequest.getMethod());
				logger.debug("original req routing state "+ originalRequest.getRoutingState());
				if(transaction != null) {
					logger.debug("transaction dialog "+ transaction.getDialog());
				}
			}
			//if a response is sent for an initial request, it means that the application
			//acted as an endpoint so a dialog must be created but only for dialog creating method
			if(!Request.CANCEL.equals(originalRequest.getMethod())					
					&& (RoutingState.INITIAL.equals(originalRequest.getRoutingState()) 
							|| RoutingState.RELAYED.equals(originalRequest.getRoutingState())) 
					&& transaction.getDialog() == null 
					&& JainSipUtils.DIALOG_CREATING_METHODS.contains(getMethod())) {					
				final String transport = JainSipUtils.findTransport(transaction.getRequest());
				final SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(
						transport, false).getSipProvider();
				
				Dialog dialog = null;
				// Creates a dialog only for non trying responses 
				// and non error responses to cope with Issue 1104 : http://code.google.com/p/mobicents/issues/detail?id=1104 
				// there is no need to create a dialog in this case since it goes directly from NULL state to TERMINATED state
				// and this will avoid early invalidation of the session (ie, dialogterminatedevent sent by the stack 
				// invalidating the session before ACK has been received)
				if(statusCode != Response.TRYING && statusCode < 300) {
					dialog = sipProvider.getNewDialog(transaction);	
					((DialogExt)dialog).disableSequenceNumberValidation();
				}
				session.setSessionCreatingDialog(dialog);
				if(logger.isDebugEnabled()) {
					logger.debug("created following dialog since the application is acting as an endpoint " + dialog);
				}
			}
			final Dialog dialog  = transaction == null ? null:transaction.getDialog();
			//keeping track of application data and transaction in the dialog
			if(dialog != null) {
				if(dialog.getApplicationData() == null) {
					dialog.setApplicationData(
							originalRequest.getTransactionApplicationData());	
				}				
				((TransactionApplicationData)dialog.getApplicationData()).
					setTransaction(transaction);				
			}
			//specify that a final response has been sent for the request
			//so that the application dispatcher knows it has to stop
			//processing the request
//			if(response.getStatusCode() > Response.TRYING && 
//					response.getStatusCode() < Response.OK) {				
//				originalRequest.setRoutingState(RoutingState.INFORMATIONAL_RESPONSE_SENT);				
//			}
			if(statusCode >= Response.OK && 
					statusCode <= Response.SESSION_NOT_ACCEPTABLE) {				
				originalRequest.setRoutingState(RoutingState.FINAL_RESPONSE_SENT);				
			}
			if(originalRequest != null) {				
				originalRequest.setResponse(this);					
			}
			//updating the last accessed times 
			session.access();
			sipApplicationSession.access();
			if(transaction == null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Sending response statelessly " + message);
				}
				final String transport = JainSipUtils.findTransport(((SipServletRequestImpl)this.getRequest()).getMessage());
				final SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(
						transport, false).getSipProvider();
				sipProvider.sendResponse((Response)this.message);
			} else if(sendReliably) {
				if(logger.isDebugEnabled()) {
					logger.debug("Sending response reliably " + message);
				}
				dialog.sendReliableProvisionalResponse((Response)this.message);
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("Sending response " + message + " through tx " + transaction);
				}
				transaction.sendResponse( (Response)this.message );
				if(dialog != null) {
					// we need to set the dialog again because it's possible that when the dialog
					// was created it was in null state thus no dialog id so we need to reset it to trigger
					// replication of the dialog id since the dialog id is computed only after the response has been sent
					session.setSessionCreatingDialog(dialog);
				}
			}
			isMessageSent = true;
			if(isProxiedResponse) {
				isResponseForwardedUpstream = true;
			}			
		} catch (Exception e) {			
			throw new IllegalStateException("an exception occured when sending the response", e);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getChallengeRealms()
	 */
	public Iterator<String> getChallengeRealms() {
		List<String> realms = new ArrayList<String>();
		if(response.getStatusCode() == SipServletResponse.SC_UNAUTHORIZED) {
			WWWAuthenticateHeader authenticateHeader = (WWWAuthenticateHeader) 
				response.getHeader(WWWAuthenticateHeader.NAME);
			realms.add(authenticateHeader.getRealm());
		} else if (response.getStatusCode() == SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED) {
			ProxyAuthenticateHeader authenticateHeader = (ProxyAuthenticateHeader) 
				response.getHeader(ProxyAuthenticateHeader.NAME);
			realms.add(authenticateHeader.getRealm());
		}
		return realms.iterator();
	}


	/**
	 * {@inheritDoc}
	 */
	public ProxyBranch getProxyBranch() {
		return proxyBranch;
	}


	/**
	 * @param proxyBranch the proxyBranch to set
	 */
	public void setProxyBranch(ProxyBranch proxyBranch) {
		this.proxyBranch = proxyBranch;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isBranchResponse() {		
		return this.proxyBranch != null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#isCommitted()
	 */
	public boolean isCommitted() {
		//the message is an incoming non-reliable provisional response received by a servlet acting as a UAC
		if(getTransaction() instanceof ClientTransaction && getStatus() >= 101 && getStatus() <= 199 && getHeader("RSeq") == null) {
			if(this.proxyBranch == null) { // Make sure this is not a proxy. Proxies are allowed to modify headers.
				return true;
			} else {
				return false;
			}
		}
		//the message is an incoming reliable provisional response for which PRACK has already been generated. (Note that this scenario applies to containers that support the 100rel extension.)
		if(getTransaction() instanceof ClientTransaction && getStatus() >= 101 && getStatus() <= 199 && getHeader("RSeq") != null && TransactionState.TERMINATED.equals(getTransaction().getState())) {
			if(this.proxyBranch == null) { // Make sure this is not a proxy. Proxies are allowed to modify headers.
				return true;
			} else {
				return false;
			}
		}
		//the message is an incoming final response received by a servlet acting as a UAC for a Non INVITE transaction
		if(getTransaction() instanceof ClientTransaction && getStatus() >= 200 && getStatus() <= 999 && !Request.INVITE.equals(((SIPClientTransaction)getTransaction()).getMethod())) {
			if(this.proxyBranch == null) { // Make sure this is not a proxy. Proxies are allowed to modify headers.
				return true;
			} else {
				return false;
			}
		}
		//the message is a response which has been forwarded upstream
		if(isResponseForwardedUpstream) {
			return true;
		}
		//message is an incoming final response to an INVITE transaction and an ACK has been generated
		if(getTransaction() instanceof ClientTransaction && getStatus() >= 200 && getStatus() <= 999 && TransactionState.TERMINATED.equals(getTransaction().getState()) && isAckGenerated) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void checkMessageState() {
		if(isMessageSent || getTransaction() instanceof ClientTransaction) {
			throw new IllegalStateException("Message already sent or incoming message");
		}
	}

	public void setOriginalRequest(SipServletRequestImpl originalRequest) {
		this.originalRequest = originalRequest;
	}

	/**
	 * @param isProxiedResponse the isProxiedResponse to set
	 */
	public void setProxiedResponse(boolean isProxiedResponse) {
		this.isProxiedResponse = isProxiedResponse;
	}

	/**
	 * @return the isProxiedResponse
	 */
	public boolean isProxiedResponse() {
		return isProxiedResponse;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String enc) {		
		try {			
			this.message.setContentEncoding(SipFactories.headerFactory
					.createContentEncodingHeader(enc));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Encoding " + enc + " not valid", ex);
		}

	}
	
	@Override
	public void cleanUp() {		
//		super.cleanUp();
		originalRequest = null;
		proxyBranch = null;		
		response =null;
	}
}
