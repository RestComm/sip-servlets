/**
 * 
 */
package org.mobicents.servlet.sip.proxy;

import java.io.IOException;
import java.util.List;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.sip.SipProvider;

import org.mobicents.servlet.sip.address.SipURIImpl;
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
	private boolean started;
	private SipProvider provider;
	
	private ProxyUtils proxyUtils;
	
	public ProxyBranchImpl(SipURI uri, ProxyImpl proxy, SipProvider provider, SipURI recordRouteURI)
	{
		this.targetURI = uri;
		this.proxy = proxy;
		this.originalRequest = (SipServletRequestImpl) proxy.getOriginalRequest();
		this.recordRouteURI = proxy.getRecordRouteURI();
		this.outboundInterface = proxy.getOutboundInterface();
		this.provider = provider;
		if(recordRouteURI != null)
			this.recordRouteURI = (SipURI)((SipURIImpl)recordRouteURI).clone();
		this.proxyUtils = new ProxyUtils(provider, this);
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
		
		SipServletRequestImpl cloned = (SipServletRequestImpl) 
			this.proxyUtils.createProxiedRequest(originalRequest,
					this,
					new ProxyParams(this.targetURI, this.outboundInterface,
							recordRoute));

		try {
			cloned.send();
			started = true;
			// Send provisional TRYING
			SipServletResponse trying =
				originalRequest.createResponse(100);
			trying.send();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onResponse(SipServletResponse response)
	{
		// We have already sent TRYING, don't send another one
		if(response.getStatus() == 100)
			return;
		
		if(response.getStatus() >= 300 && response.getStatus() < 400
				&& getProxy().getRecurse())
		{
			// Recurse
		}
		SipServletResponse responseToOriginal = originalRequest.createResponse(
				response.getStatus(),
				response.getReasonPhrase());

		try {
			responseToOriginal.send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
