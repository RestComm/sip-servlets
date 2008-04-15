/**
 * 
 */
package org.mobicents.servlet.sip.proxy;

import java.io.IOException;
import java.util.List;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.sip.ClientTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;

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
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.ProxyBranch#cancel()
	 */
	public void cancel() {
		try
		{
			outgoingRequest.createCancel().send();
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
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub

	}
	
	public void start()
	{
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

		try {
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
				trying.send();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void forwardRequestStateless(Request request)
	{
		String transport = JainSipUtils.findTransport(request);
		SipProvider sipProvider = JainSipUtils.findMatchingSipProvider(
				sipFactoryImpl.getSipProviders(), transport);
		try {
			sipProvider.sendRequest(request);
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void forwardRequest(Request request, boolean subsequent) throws TransactionUnavailableException
	{
		String transport = JainSipUtils.findTransport(request);
		SipProvider sipProvider = JainSipUtils.findMatchingSipProvider(
				sipFactoryImpl.getSipProviders(), transport);
		ClientTransaction tx = sipProvider.getNewClientTransaction(request);
		SipServletRequestImpl clonedRequest = new	SipServletRequestImpl(
				request,
				sipFactoryImpl,
				null,
				tx, null, false);
		
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
		
		//ret.getTransactionApplicationData().setProxyBranch(proxyBranch);
		tx.setApplicationData(clonedRequest.getTransactionApplicationData());
		
		clonedRequest.send();
	}
	
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
				proxyUtils.createProxiedResponse(response);
			
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
		
		// Bad final responses should send ACK
		if(response.getStatus() >= 300)
		{
			// TODO: Send ACK to the callee
			// ACTUALLY: This ACK is sent by the stack
		}
		

		
		if(response.getStatus() >= 200)
		{
			this.proxy.onFinalResponse(this);
		}
		
	}

	public boolean isTimedOut() {
		return timedOut;
	}
	
	public void proxyInDialogRequest(SipServletRequestImpl request)
	{
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

}
