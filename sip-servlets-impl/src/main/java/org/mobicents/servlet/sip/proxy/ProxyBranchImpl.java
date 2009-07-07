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
package org.mobicents.servlet.sip.proxy;

import gov.nist.javax.sip.message.SIPMessage;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.sip.ClientTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.GenericUtils;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;

/**
 * @author root
 *
 */
public class ProxyBranchImpl implements ProxyBranch, Serializable {
	private static transient Logger logger = Logger.getLogger(ProxyBranchImpl.class);
	private ProxyImpl proxy;
	private transient SipServletRequestImpl originalRequest;
	private transient SipServletRequestImpl prackOriginalRequest;
	private transient SipServletRequestImpl outgoingRequest;
	private transient SipServletResponseImpl lastResponse;
	private transient URI targetURI;
	private transient SipURI outboundInterface;
	private transient SipURI recordRouteURI;
	private boolean recordRoutingEnabled;
	private boolean recurse;
	private transient SipURI pathURI;
	private boolean started;
	private boolean timedOut;
	private int proxyBranchTimeout;
	private transient ProxyBranchTimerTask proxyTimeoutTask;
	private boolean canceled;
	private boolean isAddToPath;
	private transient List<ProxyBranch> recursedBranches;
	private boolean waitingForPrack;
	
	private static transient Timer timer = new Timer();
	
	public ProxyBranchImpl(URI uri, ProxyImpl proxy)
	{
		this.targetURI = uri;
		this.proxy = proxy;
		isAddToPath = proxy.getAddToPath();
		this.originalRequest = (SipServletRequestImpl) proxy.getOriginalRequest();
		this.recordRouteURI = proxy.recordRouteURI;
		this.pathURI = proxy.pathURI;
		this.outboundInterface = proxy.getOutboundInterface();
		if(recordRouteURI != null) {
			this.recordRouteURI = (SipURI)((SipURIImpl)recordRouteURI).clone();			
		}
		this.proxyBranchTimeout = proxy.getProxyTimeout();
		this.canceled = false;
		this.recursedBranches = new ArrayList<ProxyBranch>();
		
		// Here we create a clone which is available through getRequest(), the user can add
		// custom headers and push routes here. Later when we actually proxy the request we
		// will clone this request (with it's custome headers and routes), but we will override
		// the modified RR and Path parameters (as defined in the spec).
		Request cloned = (Request)originalRequest.getMessage().clone();
		((SIPMessage)cloned).setApplicationData(null);
		this.outgoingRequest = new SipServletRequestImpl(
				cloned,
				proxy.getSipFactoryImpl(),
				this.originalRequest.getSipSession(),
				null, null, false);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#cancel()
	 */
	public void cancel() {		
		cancel(null, null, null);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#cancel(java.lang.String[], int[], java.lang.String[])
	 */
	public void cancel(String[] protocol, int[] reasonCode, String[] reasonText) {
		if(proxy.getAckReceived()) throw new IllegalStateException("There has been an ACK received on this branch. Can not cancel.");
		try {			
			cancelTimer();
			if(this.isStarted() && !canceled && !timedOut &&
				outgoingRequest.getMethod().equalsIgnoreCase(Request.INVITE)) {
					SipServletRequest cancelRequest = outgoingRequest.createCancel();
					//Adding reason headers if needed
					if(protocol != null && reasonCode != null && reasonText != null
						&& protocol.length == reasonCode.length && reasonCode.length == reasonText.length) {
						for (int i = 0; i < protocol.length; i++) {
							((SipServletRequestImpl)cancelRequest).
								addHeaderInternal("Reason", 
										protocol[i] + ";cause=" + reasonCode[i] + ";text=\"" + reasonText[i] + "\"",
										false);
						}
					}
					cancelRequest.send();
				canceled = true;
			}
			if(!this.isStarted() &&
					outgoingRequest.getMethod().equalsIgnoreCase(Request.INVITE)) {
				canceled = true;	
			}
		}
		catch(Exception e) {
			throw new IllegalStateException("Failed canceling proxy branch", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#getProxy()
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#getProxyBranchTimeout()
	 */
	public int getProxyBranchTimeout() {
		return proxyBranchTimeout;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#getRecordRouteURI()
	 */
	public SipURI getRecordRouteURI() {
		if(this.getRecordRoute()) {
			if(this.recordRouteURI == null) 
				this.recordRouteURI = proxy.getSipFactoryImpl().createSipURI("proxy", "localhost");
			return this.recordRouteURI;
		}
		
		else throw new IllegalStateException("Record Route not enabled for this ProxyBranch. You must call proxyBranch.setRecordRoute(true) before getting an URI.");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#getRecursedProxyBranches()
	 */
	public List<ProxyBranch> getRecursedProxyBranches() {
		return recursedBranches;
	}
	
	public void addRecursedBranch(ProxyBranchImpl branch) {
		recursedBranches.add(branch);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#getRequest()
	 */
	public SipServletRequest getRequest() {
		return outgoingRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#getResponse()
	 */
	public SipServletResponse getResponse() {
		return lastResponse;
	}
	
	public void setResponse(SipServletResponse response) {
		lastResponse = (SipServletResponseImpl) response;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#isStarted()
	 */
	public boolean isStarted() {
		return started;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#setProxyBranchTimeout(int)
	 */
	public void setProxyBranchTimeout(int seconds) {
		if(seconds<=0) 
			throw new IllegalArgumentException("Negative or zero timeout not allowed");
		
		this.proxyBranchTimeout = seconds;
		if(this.started) updateTimer();
	}
	
	/**
	 * After the branch is initialized, this method proxies the initial request to the
	 * specified destination. Subsequent requests are proxied through proxySubsequentRequest
	 */
	public void start()	{
		if(started) {
			throw new IllegalStateException("Proxy branch alredy started!");
		}
		if(canceled) {
			throw new IllegalStateException("Proxy branch was cancelled, you must create a new branch!");
		}
		if(timedOut) {
			throw new IllegalStateException("Proxy brnach has timed out!");
		}
		if(proxy.getAckReceived()) {
			throw new IllegalStateException("An ACK request has been received on this proxy. Can not start new branches.");
		}
		
		// Initialize these here for efficiency.
		updateTimer();
		
		SipURI recordRoute = null;
		
		// If the proxy is not adding record-route header, set it to null and it
		// will be ignored in the Proxying
		if(proxy.getRecordRoute() || this.getRecordRoute()) {
			if(recordRouteURI == null) {
				recordRouteURI = proxy.getSipFactoryImpl().createSipURI("proxy", "localhost");
			}
			recordRoute = recordRouteURI;
		}
						
		Request cloned = proxy.getProxyUtils().createProxiedRequest(
				outgoingRequest,
				this,
				new ProxyParams(this.targetURI,
				this.outboundInterface,
				recordRoute, 
				this.pathURI));
		//tells the application dispatcher to stop routing the original request
		//since it has been proxied
		originalRequest.setRoutingState(RoutingState.PROXIED);
		
		forwardRequest(cloned, false);					
		started = true;
	}
	
	/**
	 * Forward the request to the specified destination. The method is used internally.
	 * @param request
	 * @param subsequent Set to false if the the method is initial
	 */
	private void forwardRequest(Request request, boolean subsequent) {

		if(logger.isDebugEnabled()) {
			logger.debug("creating cloned Request for proxybranch " + request);
		}
		SipServletRequestImpl clonedRequest = new SipServletRequestImpl(
				request,
				proxy.getSipFactoryImpl(),
				null,
				null, null, false);
		
		if(subsequent) {
			clonedRequest.setRoutingState(RoutingState.SUBSEQUENT);
		}
		
		this.outgoingRequest = clonedRequest;
		
		// Initialize the sip session for the new request if initial
		clonedRequest.setCurrentApplicationName(originalRequest.getCurrentApplicationName());
		if(clonedRequest.getCurrentApplicationName() == null && subsequent) {
			clonedRequest.setCurrentApplicationName(originalRequest.getSipSession().getSipApplicationSession().getApplicationName());
		}
		clonedRequest.setSipSession(originalRequest.getSipSession());
		MobicentsSipSession newSession = (MobicentsSipSession) clonedRequest.getSession(true);
		try {
			newSession.setHandler(((MobicentsSipSession)this.originalRequest.getSession()).getHandler());
		} catch (ServletException e) {
			logger.error("could not set the session handler while forwarding the request", e);
			throw new RuntimeException(e);
		}
		
		// Use the original dialog in the new session
		newSession.setSessionCreatingDialog(originalRequest.getSipSession().getSessionCreatingDialog());
		
		// And set a reference to the proxy		
		newSession.setProxy(proxy);		
				
		//JSR 289 Section 15.1.6
		if(!subsequent) {
			// Subsequent requests can't have a routing directive?
			clonedRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, originalRequest);
		}
		clonedRequest.getTransactionApplicationData().setProxyBranch(this);			
		clonedRequest.send();
	}
	
	/**
	 * A callback. Here we receive all responses from the proxied requests we have sent.
	 * 
	 * @param response
	 */
	public void onResponse(SipServletResponseImpl response)
	{
		// We have already sent TRYING, don't send another one
		if(response.getStatus() == 100)
			return;
		
		// Send informational responses back immediately
		if((response.getStatus() > 100 && response.getStatus() < 200) || (response.getStatus() == 200 && Request.PRACK.equals(response.getMethod())))
		{
			// Deterimine if the response is reliable. We just look at RSeq, because
			// every such response is required to have it.
			if(response.getHeader("RSeq") != null) {
				this.setWaitingForPrack(true); // this branch is expecting a PRACK now
			}
			
			SipServletResponse proxiedResponse = 
				proxy.getProxyUtils().createProxiedResponse(response, this);
			
			if(proxiedResponse == null) 
				return; // this response was addressed to this proxy
			
			try {
				proxiedResponse.send();
			} catch (IOException e) {
				logger.error("A problem occured while proxying a response", e);
			}
			
			return;
		}
		
		// Non-provisional responses must also cancel the timer, otherwise it will timeout
		// and return multiple responses for a single transaction.
		cancelTimer();
		
		if(response.getStatus() >= 600) // Cancel all 10.2.4
			this.proxy.cancelAllExcept(this, null, null, null, false);
		
		// FYI: ACK is sent automatically by jsip when needed
		
		boolean recursed = false;
		if(response.getStatus() >= 300 && response.getStatus()<400 && recurse) {
			String contact = response.getHeader("Contact");
			if(contact != null) {
				//javax.sip.address.SipURI uri = SipFactories.addressFactory.createAddress(contact);
				try {
					int start = contact.indexOf('<');
					int end = contact.indexOf('>');
					contact = contact.substring(start + 1, end);
					URI uri = proxy.getSipFactoryImpl().createURI(contact);
					ArrayList<SipURI> list = new ArrayList<SipURI>();
					list.add((SipURI)uri);
					List<ProxyBranch> pblist = proxy.createProxyBranches(list);
					ProxyBranchImpl pbi = (ProxyBranchImpl)pblist.get(0);
					this.addRecursedBranch(pbi);
					pbi.start();
					recursed = true;
				} catch (ServletParseException e) {
					throw new RuntimeException("Can not parse contact header", e);
				}
			}
		}
		if(response.getStatus() >= 200 && !recursed)
		{
			if(outgoingRequest != null && outgoingRequest.isInitial()) {
				this.proxy.onFinalResponse(this);
			} else {
				this.proxy.sendFinalResponse(response, this);
			}
		}
		
	}

	/**
	 * Has the branch timed out?
	 * 
	 * @return
	 */
	public boolean isTimedOut() {
		return timedOut;
	}
	
	/**
	 * Call this method when a subsequent request must be proxied through the branch.
	 * 
	 * @param request
	 */
	public void proxySubsequentRequest(SipServletRequestImpl request) {		
		
		// A re-INVITE needs special handling without goind through the dialog-stateful methods
		if(request.getMethod().equalsIgnoreCase("INVITE")) {
			if(logger.isDebugEnabled()) {
				logger.debug("Proxying reinvite request " + request);
			}
			// reset the ack received flag for reINVITE
			request.getSipSession().setAckReceived(false);
			proxyDialogStateless(request);
			return;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Proxying subsequent request " + request);
		}
		
		// Update the last proxied request
		request.setRoutingState(RoutingState.PROXIED);
		proxy.setOriginalRequest(request);
		this.originalRequest = request;
		
		// No proxy params, sine the target is already in the Route headers
		ProxyParams params = new ProxyParams(null, null, null, null);
		Request clonedRequest = 
			proxy.getProxyUtils().createProxiedRequest(request, this, params);

//      There is no need for that, it makes application composition fail (The subsequent request is not dispatched to the next application since the route header is removed)
//		RouteHeader routeHeader = (RouteHeader) clonedRequest.getHeader(RouteHeader.NAME);
//		if(routeHeader != null) {
//			if(!((SipApplicationDispatcherImpl)proxy.getSipFactoryImpl().getSipApplicationDispatcher()).isRouteExternal(routeHeader)) {
//				clonedRequest.removeFirst(RouteHeader.NAME);	
//			}
//		}		
	
		String transport = JainSipUtils.findTransport(clonedRequest);
		SipProvider sipProvider = proxy.getSipFactoryImpl().getSipNetworkInterfaceManager().findMatchingListeningPoint(
				transport, false).getSipProvider();
		
		try {
			// Reset the proxy supervised state to default Chapter 6.2.1 - page down list bullet number 6
			proxy.setSupervised(true);
			if(clonedRequest.getMethod().equalsIgnoreCase(Request.ACK) ) { //|| clonedRequest.getMethod().equalsIgnoreCase(Request.PRACK)) {
				sipProvider.sendRequest(clonedRequest);
			}
			else {				
				forwardRequest(clonedRequest, true);
			}
			
		} catch (SipException e) {
			logger.error("A problem occured while proxying a subsequent request", e);
		}
	}
	
	/**
	 * This method proxies requests without updating JSIP dialog state. PRACK and re-INVITE
	 * requests require this kind of handling because:
	 * 1. PRACK occurs before a dialog has been established (and also produces OKs before
	 *  the final response)
	 * 2. re-INVITE when sent with the dialog method resets the internal JSIP CSeq counter
	 *  to 1 every time you need it, which causes issues like 
	 *  http://groups.google.com/group/mobicents-public/browse_thread/thread/1a22ccdc4c481f47
	 * 
	 * @param request
	 */
	public void proxyDialogStateless(SipServletRequestImpl request) {
		if(logger.isDebugEnabled()) {
			logger.debug("Proxying request dialog-statelessly" + request);
		}
		// Update the last proxied request
		request.setRoutingState(RoutingState.PROXIED);
		this.prackOriginalRequest = request;
		
		URI targetURI = this.targetURI;
		
		// Determine the direction of the request. Either it's from the dialog initiator (the caller)
		// or from the callee
		if(!request.getFrom().toString().equals(proxy.getCallerFromHeader())) {
			// If it's from the callee we should send it in the other direction
			targetURI = proxy.getPreviousNode();
		}
		
		ProxyParams params = new ProxyParams(targetURI, null, null, null);
		Request clonedRequest = 
			proxy.getProxyUtils().createProxiedRequest(request, this, params);

		ViaHeader viaHeader = (ViaHeader) clonedRequest.getHeader(ViaHeader.NAME);
		try {
			viaHeader.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME,
					proxy.getSipFactoryImpl().getSipApplicationDispatcher().getHashFromApplicationName(request.getSipSession().getKey().getApplicationName()));
			viaHeader.setParameter(MessageDispatcher.APP_ID,
					request.getSipSession().getSipApplicationSession().getKey().getId());
		} catch (ParseException pe) {
			logger.error("A problem occured while proxying a request in a dialog-stateless transaction", pe);
		}
		
		RouteHeader routeHeader = (RouteHeader) clonedRequest.getHeader(RouteHeader.NAME);
		if(routeHeader != null) {
			if(!((SipApplicationDispatcherImpl)proxy.getSipFactoryImpl().getSipApplicationDispatcher()).isRouteExternal(routeHeader)) {
				clonedRequest.removeFirst(RouteHeader.NAME);	
			}
		}	
	
		String transport = JainSipUtils.findTransport(clonedRequest);
		SipProvider sipProvider = proxy.getSipFactoryImpl().getSipNetworkInterfaceManager().findMatchingListeningPoint(
				transport, false).getSipProvider();
		
		if(logger.isDebugEnabled()) {
			logger.debug("Getting new Client Tx for request " + clonedRequest);
		}
		
		try {
			ClientTransaction ctx = sipProvider
				.getNewClientTransaction(clonedRequest);			
			
			TransactionApplicationData appData = (TransactionApplicationData) request.getTransactionApplicationData();
			appData.setProxyBranch(this);
			ctx.setApplicationData(appData);
			
			ctx.sendRequest();
		} catch (SipException e) {
			logger.error("A problem occured while proxying a request in a dialog-stateless transaction", e);
		}
	}
	
	/**
	 * This callback is called when the remote side has been idle too long while
	 * establishing the dialog.
	 *
	 */
	public void onTimeout()
	{
		this.cancel();
		this.timedOut = true;
		// Just do a timeout response
		proxy.onBranchTimeOut(this);
	}
	
	/**
	 * Restart the timer. Call this method when some activity shows the remote
	 * party is still online.
	 *
	 */
	void updateTimer() {
		if(proxyTimeoutTask != null) {
			proxyTimeoutTask.cancel();
		}
		proxyTimeoutTask = new ProxyBranchTimerTask(this);
		if(logger.isDebugEnabled()) {
			logger.debug("Proxy Branch Timeout set to " + proxyBranchTimeout);
		}
		if(proxyBranchTimeout != 0)
			timer.schedule(proxyTimeoutTask, proxyBranchTimeout * 1000);
	}
	
	/**
	 * Stop the C Timer.
	 */
	public void cancelTimer()
	{
		if(proxyTimeoutTask != null)
		{
			proxyTimeoutTask.cancel();
			proxyTimeoutTask = null;
		}
	}

	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getAddToPath() {
		return isAddToPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public SipURI getPathURI() {
		if(!isAddToPath) {
			throw new IllegalStateException("addToPath is not enabled!");
		}
		return this.pathURI;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getRecordRoute() {
		return recordRoutingEnabled;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getRecurse() {
		return recurse;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAddToPath(boolean isAddToPath) {
		if(started) {
			throw new IllegalStateException("Cannot set a record route on an already started proxy");
		}
		if(this.pathURI == null) {
			this.pathURI = new SipURIImpl ( JainSipUtils.createRecordRouteURI( proxy.getSipFactoryImpl().getSipNetworkInterfaceManager(), null));
		}		
		this.isAddToPath = isAddToPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutboundInterface(InetAddress inetAddress) {
		//TODO check against our defined outbound interfaces
		checkSessionValidity();
		String address = inetAddress.getHostAddress();
		outboundInterface = proxy.getSipFactoryImpl().createSipURI(null, address);		
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutboundInterface(InetSocketAddress inetSocketAddress) {
		//TODO check against our defined outbound interfaces		
		checkSessionValidity();
		String address = inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
		outboundInterface = proxy.getSipFactoryImpl().createSipURI(null, address);		
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRecordRoute(boolean isRecordRoute) {
		recordRoutingEnabled = isRecordRoute;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRecurse(boolean isRecurse) {
		recurse = isRecurse;
	}	
	
	private void checkSessionValidity() {
		if(this.originalRequest.getApplicationSession().isValid() 
		&& this.originalRequest.getSession().isValid())
			return;
		throw new IllegalStateException("Invalid session.");
	}

	/**
	 * @param prackOriginalRequest the prackOriginalRequest to set
	 */
	public void setPrackOriginalRequest(SipServletRequestImpl prackOriginalRequest) {
		this.prackOriginalRequest = prackOriginalRequest;
	}

	/**
	 * @return the prackOriginalRequest
	 */
	public SipServletRequestImpl getPrackOriginalRequest() {
		return prackOriginalRequest;
	}

	public boolean isWaitingForPrack() {
		return waitingForPrack;
	}

	public void setWaitingForPrack(boolean waitingForPrack) {
		this.waitingForPrack = waitingForPrack;
	}

}
