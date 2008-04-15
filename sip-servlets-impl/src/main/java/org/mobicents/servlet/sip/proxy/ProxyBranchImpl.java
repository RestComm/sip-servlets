/**
 * 
 */
package org.mobicents.servlet.sip.proxy;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.core.RoutingState;
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
	private SipURI pathURI;
	private boolean started;
	private SipFactoryImpl sipFactoryImpl;
	private ProxyUtils proxyUtils;
	private boolean timedOut;
	private int proxyBranchTimeout;
	private Timer proxyBranchTimer;
	private ProxyBranchTimerTask proxyTimeoutTask;
	private boolean canceled;
	
	public ProxyBranchImpl(SipURI uri, ProxyImpl proxy, SipFactoryImpl sipFactoryImpl, SipURI recordRouteURI)
	{
		this.targetURI = uri;
		this.proxy = proxy;
		this.originalRequest = (SipServletRequestImpl) proxy.getOriginalRequest();
		this.recordRouteURI = proxy.getRecordRouteURI();
		this.pathURI = proxy.getPathURI();
		this.outboundInterface = proxy.getOutboundInterface();
		this.sipFactoryImpl = sipFactoryImpl;
		if(recordRouteURI != null)
			this.recordRouteURI = (SipURI)((SipURIImpl)recordRouteURI).clone();
		this.proxyUtils = proxy.getProxyUtils();
		this.proxyBranchTimeout = proxy.getProxyTimeout();
		this.canceled = false;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#cancel()
	 */
	public void cancel() {
		try
		{
			if(this.isStarted() && !canceled && !timedOut &&
				outgoingRequest.getMethod().equalsIgnoreCase(Request.INVITE)) {
					outgoingRequest.createCancel().send();
					canceled = true;
			}
		}
		catch(Exception e)
		{
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
		// TODO Auto-generated method stub
		return null;
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
	 * @see javax.servlet.sip.ProxyBranch#setOutboundInterface(javax.servlet.sip.SipURI)
	 */
	public void setOutboundInterface(SipURI uri) {
		outboundInterface = uri;

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
	public void start()
	{
		if(started)
			throw new IllegalStateException("Proxy branch alredy started!");
		if(canceled)
			throw new IllegalStateException("Proxy branch was cancelled, you must create a new branch!");
		if(timedOut)
			throw new IllegalStateException("Proxy brnach has timed out!");
		
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

		forwardRequest(cloned, false);			
		//tells the application dispatcher to stop routing the original request
		//since it has been proxied
		originalRequest.setRoutingState(RoutingState.PROXIED);
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
	private void forwardRequest(Request request, boolean subsequent) 
	{

		if(logger.isDebugEnabled()) {
			logger.debug("creating cloned Request for proxybranch " + request);
		}
		SipServletRequestImpl clonedRequest = new	SipServletRequestImpl(
				request,
				sipFactoryImpl,
				null,
				null, null, false);
		
		this.outgoingRequest = clonedRequest;
		
		// Initialize the sip session for the new request if initial
		clonedRequest.setCurrentApplicationName(originalRequest.getCurrentApplicationName());
		SipSessionImpl newSession = (SipSessionImpl) clonedRequest.getSession(true);
		
		// Use the original dialog in the new session
		newSession.setSessionCreatingDialog(originalRequest.getSipSession().getSessionCreatingDialog());
		
		// And set a reference to the proxy branch
		newSession.setProxyBranch(this);
				
		//JSR 289 Section 15.1.6
		if(!subsequent) // Subsequent requests can't have a routing directive?
			clonedRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, originalRequest);
		
		clonedRequest.send();
		}
	
	/**
	 * A callback. Here we receive all responses from the proxied requests we have sent.
	 * 
	 * @param response
	 */
	public void onResponse(SipServletResponseImpl response)
	{
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return;
		}
		
		if(response.getStatus() >= 600) // Cancel all 10.2.4
			this.proxy.cancelAllExcept(this);
		
		// FYI: ACK is sent automatically by jsip when needed
		
		if(response.getStatus() >= 200)
		{
			if(response.getRequest() != null && response.getRequest().isInitial()) {
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
	public void proxySubsequentRequest(SipServletRequestImpl request)
	{
		// Update the last proxied request
		request.setRoutingState(RoutingState.PROXIED);
		proxy.setOriginalRequest(request);
		
		// No proxy params, sine the target is already in the Route headers
		ProxyParams params = new ProxyParams(null, null, null, null);
		Request clonedRequest = 
			proxyUtils.createProxiedRequest(request, this, params);

		clonedRequest.removeFirst(RouteHeader.NAME);
	
		String transport = JainSipUtils.findTransport(clonedRequest);
		SipProvider sipProvider = JainSipUtils.findMatchingSipProvider(
				sipFactoryImpl.getSipProviders(), transport);
		
		try {
			if(clonedRequest.getMethod().equalsIgnoreCase(Request.ACK)) {
				sipProvider.sendRequest(clonedRequest);
			}
			else {
				forwardRequest(clonedRequest, true);
			}
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

}
