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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

/**
 * @author root
 *
 */
public class ProxyImpl implements Proxy {
	private static Log logger = LogFactory.getLog(ProxyImpl.class);
	
	private SipServletRequestImpl originalRequest;
	private SipServletResponseImpl bestResponse;
	private ProxyBranchImpl bestBranch;
	private boolean recurse = true;
	private int proxyTimeout;
	private int seqSearchTimeout;
	private boolean supervised = true; 
	private boolean recordRoutingEnabled;
	private boolean parallel = true;
	private boolean addToPath;
	private SipURI pathURI;
	private SipURI recordRouteURI;
	private SipURI outboundInterface;
	private SipFactoryImpl sipFactoryImpl;
	private boolean isNoCancel;
	
	private ProxyUtils proxyUtils;
	
	private Map<URI, ProxyBranch> proxyBranches;
	private boolean started; 
	private boolean ackReceived = false;
	
	public ProxyImpl(SipServletRequestImpl request, SipFactoryImpl sipFactoryImpl)
	{
		this.originalRequest = request;
		this.sipFactoryImpl = sipFactoryImpl;
		this.proxyBranches = new LinkedHashMap<URI, ProxyBranch> ();
		this.proxyUtils = new ProxyUtils(sipFactoryImpl, this);
		this.proxyTimeout = 180; // 180 secs default
		this.outboundInterface = ((MobicentsSipSession)request.getSession()).getOutboundInterface();
		
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#cancel()
	 */
	public void cancel() {
		cancelAllExcept(null, null, null, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#cancel(java.lang.String[], int[], java.lang.String[])
	 */
	public void cancel(String[] protocol, int[] reasonCode, String[] reasonText) {
		cancelAllExcept(null, protocol, reasonCode, reasonText);
	}

	public void cancelAllExcept(ProxyBranch except, String[] protocol, int[] reasonCode, String[] reasonText) {
		for(ProxyBranch proxyBranch : proxyBranches.values()) {		
			if(!proxyBranch.equals(except)) {
				proxyBranch.cancel(protocol, reasonCode, reasonText);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#createProxyBranches(java.util.List)
	 */
	public List<ProxyBranch> createProxyBranches(List<? extends URI> targets) {
		ArrayList<ProxyBranch> list = new ArrayList<ProxyBranch>();
		for(URI target: targets)
		{
			if(target == null) throw new NullPointerException("URI can't be null");
			ProxyBranchImpl branch = new ProxyBranchImpl(target, this, sipFactoryImpl, this.recordRouteURI, this.pathURI);
			branch.setRecordRoute(recordRoutingEnabled);
			branch.setRecurse(recurse);
			list.add(branch);
			this.proxyBranches.put(target, branch);
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
		if(!this.addToPath) throw new IllegalStateException("You must setAddToPath(true) before getting URI");
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
		if(!this.recordRoutingEnabled) throw new IllegalStateException("You must setRecordRoute(true) before getting URI");
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
			if(uri == null) throw new NullPointerException("URI can't be null");
			ProxyBranchImpl branch = new ProxyBranchImpl((SipURI) uri, this, sipFactoryImpl, this.recordRouteURI, this.pathURI);
			branch.setRecordRoute(recordRoutingEnabled);
			branch.setRecurse(recurse);
			this.proxyBranches.put(uri, branch);
		}
		startProxy();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#proxyTo(javax.servlet.sip.URI)
	 */
	public void proxyTo(URI uri) {
		if(uri == null) throw new NullPointerException("URI can't be null");
		
		ProxyBranchImpl branch = new ProxyBranchImpl(uri, this, sipFactoryImpl, this.recordRouteURI, this.pathURI);
		branch.setRecordRoute(recordRoutingEnabled);
		branch.setRecurse(recurse);
		this.proxyBranches.put(uri, branch);
		startProxy();

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setAddToPath(boolean)
	 */
	public void setAddToPath(boolean p) {
		if(started) {
			throw new IllegalStateException("Cannot set a record route on an already started proxy");
		}
		if(this.pathURI == null) {
			this.pathURI = new SipURIImpl ( JainSipUtils.createRecordRouteURI( sipFactoryImpl.getSipNetworkInterfaceManager(), null));
		}		
		addToPath = p;

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
		if(seconds<=0) throw new IllegalArgumentException("Negative or zero timeout not allowed");
		
		proxyTimeout = seconds;
		for(ProxyBranch proxyBranch : proxyBranches.values()) {		
			proxyBranch.setProxyBranchTimeout(seconds);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#setRecordRoute(boolean)
	 */
	public void setRecordRoute(boolean rr) {
		if(started) {
			throw new IllegalStateException("Cannot set a record route on an already started proxy");
		}
		if(rr) {
			this.recordRouteURI = new SipURIImpl ( JainSipUtils.createRecordRouteURI( sipFactoryImpl.getSipNetworkInterfaceManager(), null));
			if(logger.isDebugEnabled()) {
				logger.debug("Record routing enabled for proxy, Record Route used will be : " + recordRouteURI.toString());
			}
		}		
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
		originalRequest.getSipSession().setSupervisedMode(supervised);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#startProxy()
	 */
	public void startProxy() {
		if(this.ackReceived) 
			throw new IllegalStateException("Can't start. ACK has been received.");
	
		
		started = true;
		if(this.parallel) {
			for (ProxyBranch pb : this.proxyBranches.values()) {
				if(!((ProxyBranchImpl)pb).isStarted())
					((ProxyBranchImpl)pb).start();
			}
		} else {
			startNextUntriedBranch();
		}		
	}
	
	public SipURI getOutboundInterface() {
		return outboundInterface;
	}
	
	public void onFinalResponse(ProxyBranchImpl branch) {
		//Get the final response
		SipServletResponseImpl response = (SipServletResponseImpl) branch.getResponse();
		
		// Cancel all others if 2xx or 6xx 10.2.4
		if(!isNoCancel) {
			if( (response.getStatus() >= 200 && response.getStatus() < 300) 
					|| (response.getStatus() >= 600 && response.getStatus() < 700) ) { 
				if(this.getParallel()) {
					cancelAllExcept(branch, null, null, null);
				}
			}
		}
		// Recurse if allowed
		if(response.getStatus() >= 300 && response.getStatus() < 400
				&& getRecurse())
		{
			// We may want to store these for "moved permanently" and others
			ListIterator<Header> headers = 
				response.getMessage().getHeaders(ContactHeader.NAME);
			while(headers.hasNext()) {
				ContactHeader contactHeader = (ContactHeader) headers.next();
				javax.sip.address.URI addressURI = contactHeader.getAddress().getURI();
				URI contactURI = null;
				if (addressURI instanceof javax.sip.address.SipURI) {
					contactURI = new SipURIImpl(
							(javax.sip.address.SipURI) addressURI);
				} else if (addressURI instanceof javax.sip.address.TelURL) {
					contactURI = new TelURLImpl(
							(javax.sip.address.TelURL) addressURI);

				}
				ProxyBranchImpl recurseBranch = new ProxyBranchImpl(contactURI, this, sipFactoryImpl, this.recordRouteURI, this.pathURI);
				recurseBranch.setRecordRoute(recordRoutingEnabled);
				recurseBranch.setRecurse(recurse);
				this.proxyBranches.put(contactURI, recurseBranch);
				branch.addRecursedBranch(branch);
				if(parallel) recurseBranch.start();
				// if not parallel, just adding it to the list is enough
			}
		}
		
		// Sort best do far		
		if(bestResponse == null || bestResponse.getStatus() > response.getStatus())
		{
			bestResponse = response;
			bestBranch = branch;
		}
		
		// Check if we are waiting for more response
		if(allResponsesHaveArrived())
		{
			bestResponse.getSipSession().setProxyBranch(branch);
			sendFinalResponse(bestResponse, bestBranch);
		}
		else
		{
			if(!parallel)
			{
				startNextUntriedBranch();
			}
		}

	}
	
	public void onBranchTimeOut(ProxyBranchImpl branch)
	{
		if(this.bestBranch == null) this.bestBranch = branch;
		if(allResponsesHaveArrived())
		{
			sendFinalResponse(bestResponse, bestBranch);
		}
		else
		{
			if(!parallel)
			{
				startNextUntriedBranch();
			}
		}
	}
	
	// In sequential proxying get some untried branch and start it, then wait for response and repeat
	public void startNextUntriedBranch()
	{
		if(this.parallel) 
			throw new IllegalStateException("This method is only for sequantial proxying");
		
		for(ProxyBranch pb: this.proxyBranches.values())
		{
			ProxyBranchImpl pbi = (ProxyBranchImpl) pb;
			SipServletResponse response = pb.getResponse();
			if(!pbi.isStarted())
			{
				pbi.start();
				return;
			}
		}
	}
	
	public boolean allResponsesHaveArrived()
	{
		for(ProxyBranch pb: this.proxyBranches.values())
		{
			ProxyBranchImpl pbi = (ProxyBranchImpl) pb;
			SipServletResponse response = pb.getResponse();
			
			// The unstarted branches still haven't got a chance to get response
			if(!pbi.isStarted()) { 
				return false;
			}
			
			if(pbi.isStarted() && !pbi.isTimedOut() && !pbi.isCanceled())
			{
				if(response == null || 						// if there is no response yet
					response.getStatus() < Response.OK) {	// or if the response if not final
					return false;							// then we should wait more
				}
			}
		}
		return true;
	}
	
	public void sendFinalResponse(SipServletResponseImpl response,
			ProxyBranchImpl proxyBranch)
	{
		// If we didn't get any response and only a timeout just return a timeout
		if(proxyBranch.isTimedOut()) {
			try {
				originalRequest.createResponse(Response.REQUEST_TIMEOUT).send();
				return;
			} catch (IOException e1) {
				throw new IllegalStateException("Faild to send a timeout response");
			}
		}
		
		//Otherwise proceed with proxying the response
		SipServletResponse proxiedResponse = 
			proxyUtils.createProxiedResponse(response, proxyBranch);
		
		if(proxiedResponse == null) {
			return; // this response was addressed to this proxy
		}

		try {
			proxiedResponse.send();
		} catch (IOException e) {
			logger.error("A problem occured while proxying the final response", e);
		}
	}
	
	ProxyUtils getProxyUtils() {
		return proxyUtils;
	}

	/**
	 * @return the bestResponse
	 */
	public SipServletResponseImpl getBestResponse() {
		return bestResponse;
	}
	
	void setOriginalRequest(SipServletRequestImpl originalRequest) {
		this.originalRequest = originalRequest;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getNoCancel() {
		return isNoCancel;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNoCancel(boolean isNoCancel) {
		this.isNoCancel = isNoCancel;
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
	
	public void setAckReceived(boolean received) {
		this.ackReceived = received;
	}
	
	public boolean getAckReceived() {
		return this.ackReceived;
	}
}
