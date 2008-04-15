/**
 * 
 */
package org.mobicents.servlet.sip.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.SipProvider;

import org.mobicents.servlet.sip.message.SipServletRequestImpl;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author root
 *
 */
public class ProxyImpl implements Proxy {

	private SipServletRequestImpl originalRequest;
	private boolean recurse;
	private int proxyTimeout;
	private int seqSearchTimeout;
	private boolean supervised; 
	private boolean recordRoutingEnabled;
	private boolean parallel;
	private boolean addToPath;
	private SipURI pathURI;
	private SipURI recordRouteURI;
	private SipURI outboundInterface;
	private SipProvider provider;
	
	private Map<URI, ProxyBranch> proxyBranches;
	
	public ProxyImpl(SipServletRequestImpl request, SipProvider provider)
	{
		this.originalRequest = request;
		this.provider = provider;
		proxyBranches = new HashMap<URI, ProxyBranch> ();
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#cancel()
	 */
	public void cancel() {
		for(ProxyBranch pb : proxyBranches.values())
		{
			pb.cancel();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#createProxyBranches(java.util.List)
	 */
	public List<ProxyBranch> createProxyBranches(List<? extends URI> targets) {
		ArrayList<ProxyBranch> list = new ArrayList<ProxyBranch>();
		for(URI target: targets)
		{
			ProxyBranchImpl branch = new ProxyBranchImpl((SipURI)target, this, provider);
			list.add(branch);
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getAddToPath()
	 */
	public boolean getAddToPath() {
		return addToPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getOriginalRequest()
	 */
	public SipServletRequest getOriginalRequest() {
		return originalRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getParallel()
	 */
	public boolean getParallel() {
		return this.parallel;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getPathURI()
	 */
	public SipURI getPathURI() {
		return this.pathURI;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getProxyBranch(javax.servlet.sip.URI)
	 */
	public ProxyBranch getProxyBranch(URI uri) {
		return this.proxyBranches.get(uri);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getProxyBranches()
	 */
	public List<ProxyBranch> getProxyBranches() {
		return new ArrayList<ProxyBranch>(this.proxyBranches.values());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getProxyTimeout()
	 */
	public int getProxyTimeout() {
		return this.proxyTimeout;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getRecordRoute()
	 */
	public boolean getRecordRoute() {
		return this.recordRoutingEnabled;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getRecordRouteURI()
	 */
	public SipURI getRecordRouteURI() {
		return this.recordRouteURI;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getRecurse()
	 */
	public boolean getRecurse() {
		return this.recurse;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getSequentialSearchTimeout()
	 */
	public int getSequentialSearchTimeout() {
		return this.seqSearchTimeout;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getStateful()
	 */
	public boolean getStateful() {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#getSupervised()
	 */
	public boolean getSupervised() {
		return this.supervised;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#proxyTo(java.util.List)
	 */
	public void proxyTo(List<? extends URI> uris) {
		for (URI uri : uris)
		{
			ProxyBranchImpl pbi = new ProxyBranchImpl((SipURI) uri, this, provider);
			this.proxyBranches.put(uri, pbi);
			pbi.start();
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#proxyTo(javax.servlet.sip.URI)
	 */
	public void proxyTo(URI uri) {
		
		ProxyBranchImpl pbi = new ProxyBranchImpl((SipURI) uri, this, provider);
		this.proxyBranches.put(uri, pbi);
		pbi.start();

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setAddToPath(boolean)
	 */
	public void setAddToPath(boolean p) {
		addToPath = p;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setOutboundInterface(javax.servlet.sip.SipURI)
	 */
	public void setOutboundInterface(SipURI uri) {
		outboundInterface = uri;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setParallel(boolean)
	 */
	public void setParallel(boolean parallel) {
		this.parallel = parallel;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setProxyTimeout(int)
	 */
	public void setProxyTimeout(int seconds) {
		proxyTimeout = seconds;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setRecordRoute(boolean)
	 */
	public void setRecordRoute(boolean rr) {
		this.recordRoutingEnabled = rr;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setRecurse(boolean)
	 */
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setSequentialSearchTimeout(int)
	 */
	public void setSequentialSearchTimeout(int seconds) {
		seqSearchTimeout = seconds;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setStateful(boolean)
	 */
	public void setStateful(boolean stateful) {
		//NOTHING

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setSupervised(boolean)
	 */
	public void setSupervised(boolean supervised) {
		this.supervised = supervised;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#startProxy()
	 */
	public void startProxy() {
		for (ProxyBranch pb : this.proxyBranches.values())
		{
			((ProxyBranchImpl)pb).start();
		}

	}

	public SipURI getOutboundInterface() {
		return outboundInterface;
	}

}
