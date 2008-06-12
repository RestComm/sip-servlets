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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Timer;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

/**
 * @author root
 *
 */
public class ProxyBranchImpl implements ProxyBranch {
	private static Log logger = LogFactory.getLog(ProxyBranchImpl.class);
	private ProxyImpl proxy;
	private SipServletRequestImpl originalRequest;
	private SipServletRequestImpl outgoingRequest;
	private SipServletResponseImpl lastResponse;
	private SipURI targetURI;
	private SipURI outboundInterface;
	private SipURI recordRouteURI;
	private boolean recordRoutingEnabled;
	private boolean recurse;
	private SipURI pathURI;
	private boolean started;
	private SipFactoryImpl sipFactoryImpl;
	private ProxyUtils proxyUtils;
	private boolean timedOut;
	private int proxyBranchTimeout;
	private Timer proxyBranchTimer;
	private ProxyBranchTimerTask proxyTimeoutTask;
	private boolean canceled;
	private boolean isAddToPath;
	private List<ProxyBranch> recursedBranches;
	
	
	public ProxyBranchImpl(SipURI uri, ProxyImpl proxy, SipFactoryImpl sipFactoryImpl, SipURI recordRouteURI)
	{
		this.targetURI = uri;
		this.proxy = proxy;
		this.originalRequest = (SipServletRequestImpl) proxy.getOriginalRequest();
		this.recordRouteURI = proxy.getRecordRouteURI();
		this.pathURI = proxy.getPathURI();
		this.outboundInterface = proxy.getOutboundInterface();
		this.sipFactoryImpl = sipFactoryImpl;
		if(recordRouteURI != null) {
			this.recordRouteURI = (SipURI)((SipURIImpl)recordRouteURI).clone();			
		}
		this.proxyUtils = proxy.getProxyUtils();
		this.proxyBranchTimeout = proxy.getProxyTimeout();
		this.canceled = false;
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
		try {			
			cancelTimer();
			if(this.isStarted() && !canceled && !timedOut &&
				outgoingRequest.getMethod().equalsIgnoreCase(Request.INVITE)) {
					SipServletRequest cancelRequest = outgoingRequest.createCancel();
					//Adding reason headers if needed
					if(protocol != null && reasonCode != null && reasonText != null
						&& protocol.length == reasonCode.length && reasonCode.length == reasonText.length) {
						for (int i = 0; i < protocol.length; i++) {
							cancelRequest.addHeader("Reason", protocol[i] + ";cause=" + reasonCode[i] + ";text=\"" + reasonText[i] + "\"");
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
			throw new RuntimeException("Failed canceling proxy branch", e);
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
		return this.recordRouteURI;
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
		this.proxyBranchTimeout = seconds;
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
		
		// Initialize these here for efficiency.
		updateTimer();
		
		SipURI recordRoute = null;
		
		// If the proxy is not adding record-route header, set it to null and it
		// will be ignored in the Proxying
		if(proxy.getRecordRoute())
			recordRoute = getRecordRouteURI();
						
		Request cloned = this.proxyUtils.createProxiedRequest(
				originalRequest,
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
		
		if(cloned.getMethod().equalsIgnoreCase("INVITE"))
		{
			// Send provisional TRYING. Chapter 10.2
			SipServletResponse trying =
				originalRequest.createResponse(100);
			try {
				trying.send();
			} catch (IOException e) { 
				logger.error("Cannot send the 100 Trying",e);
			}
		}
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
				sipFactoryImpl,
				null,
				null, null, false);
		
		if(subsequent) {
			clonedRequest.setRoutingState(RoutingState.SUBSEQUENT);
		}
		
		this.outgoingRequest = clonedRequest;
		
		// Initialize the sip session for the new request if initial
		clonedRequest.setCurrentApplicationName(originalRequest.getCurrentApplicationName());
		SipSessionImpl newSession = (SipSessionImpl) clonedRequest.getSession(true);
		
		// Use the original dialog in the new session
		newSession.setSessionCreatingDialog(originalRequest.getSipSession().getSessionCreatingDialog());
		
		// And set a reference to the proxy branch
		newSession.setProxyBranch(this);
				
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
		response.setIsBranchResponse(true);
		lastResponse = response;
		
		// We have already sent TRYING, don't send another one
		if(response.getStatus() == 100)
			return;
		
		// Send informational responses back immediately
		if(response.getStatus() > 100 && response.getStatus() < 200)
		{

			SipServletResponse proxiedResponse = 
				proxyUtils.createProxiedResponse(response, this);
			
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
			this.proxy.cancelAllExcept(this, null, null, null);
		
		// FYI: ACK is sent automatically by jsip when needed
		
		if(response.getStatus() >= 200)
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
		if(logger.isDebugEnabled()) {
			logger.debug("Proxying subsequent request " + request);
		}
		// Update the last proxied request
		request.setRoutingState(RoutingState.PROXIED);
		proxy.setOriginalRequest(request);
		
		// No proxy params, sine the target is already in the Route headers
		ProxyParams params = new ProxyParams(null, null, null, null);
		Request clonedRequest = 
			proxyUtils.createProxiedRequest(request, this, params);

		RouteHeader routeHeader = (RouteHeader) clonedRequest.getHeader(RouteHeader.NAME);
		if(routeHeader != null) {
			if(!((SipApplicationDispatcherImpl)sipFactoryImpl.getSipApplicationDispatcher()).isRouteExternal(routeHeader)) {
				clonedRequest.removeFirst(RouteHeader.NAME);	
			}
		}		
	
		String transport = JainSipUtils.findTransport(clonedRequest);
		SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(
				transport, false).getSipProvider();
		
		try {
			if(clonedRequest.getMethod().equalsIgnoreCase(Request.ACK)) {
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
	void updateTimer()
	{
		if(proxyBranchTimer != null) {
			proxyBranchTimer.cancel();
			proxyBranchTimer.purge();
		}
		proxyBranchTimer = new Timer();
		proxyTimeoutTask = new ProxyBranchTimerTask(this);
		if(this.getProxyBranchTimeout() != 0)
			proxyBranchTimer.schedule(proxyTimeoutTask, this.getProxyBranchTimeout() * 1000);
	}
	
	/**
	 * Stop the C Timer.
	 */
	public void cancelTimer()
	{
		if(proxyBranchTimer != null)
		{
			proxyBranchTimer.cancel();
			proxyBranchTimer.purge();
			proxyBranchTimer = null;
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
		if(isAddToPath) {
			throw new IllegalStateException("addToPath is not enabled!");
		}
		throw new UnsupportedOperationException("the path extension is not yet supported");
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
		this.isAddToPath = isAddToPath;
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutboundInterface(InetAddress inetAddress) {
		//TODO check against our defined outbound interfaces		
		String address = inetAddress.getHostAddress();
		outboundInterface = sipFactoryImpl.createSipURI(null, address);		
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutboundInterface(InetSocketAddress inetSocketAddress) {
		//TODO check against our defined outbound interfaces		
		String address = inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
		outboundInterface = sipFactoryImpl.createSipURI(null, address);		
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
}
