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

import gov.nist.javax.sip.header.Via;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
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
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
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
public class ProxyImpl implements Proxy, Serializable {
	private static transient Logger logger = Logger.getLogger(ProxyImpl.class);
	
	private transient SipServletRequestImpl originalRequest;
	private transient SipServletResponseImpl bestResponse;
	private transient ProxyBranchImpl bestBranch;
	private boolean recurse = true;
	private int proxyTimeout;
	private int seqSearchTimeout;
	private boolean supervised = true; 
	private boolean recordRoutingEnabled;
	private boolean parallel = true;
	private boolean addToPath;
	protected transient SipURI pathURI;
	protected transient SipURI recordRouteURI;
	private transient SipURI outboundInterface;
	private transient SipFactoryImpl sipFactoryImpl;
	private boolean isNoCancel;
	// clustering : will be recreated when loaded from the cache
	private transient ProxyUtils proxyUtils;
	
	private transient Map<URI, ProxyBranch> proxyBranches;
	private boolean started; 
	private boolean ackReceived = false;
	private boolean tryingSent = false;
	// This branch is the final branch (set when the final response has been sent upstream by the proxy) 
	// that will be used for proxying subsequent requests
	private ProxyBranchImpl finalBranchForSubsequentRequests;
	
	// Keep the URI of the previous SIP entity that sent the original request to us (either another proxy or UA)
	private SipURI previousNode;
	
	// The From-header of the initiator of the request. Used to determine the direction of the request.
	// Caller -> Callee or Caller <- Callee
	private String callerFromHeader;

	public ProxyImpl(SipServletRequestImpl request, SipFactoryImpl sipFactoryImpl)
	{
		this.originalRequest = request;
		this.sipFactoryImpl = sipFactoryImpl;
		this.proxyBranches = new LinkedHashMap<URI, ProxyBranch> ();
		this.proxyUtils = new ProxyUtils(sipFactoryImpl, this);
		this.proxyTimeout = 180; // 180 secs default
		this.outboundInterface = ((MobicentsSipSession)request.getSession()).getOutboundInterface();
		this.callerFromHeader = request.getFrom().toString();
		this.previousNode = extractPreviousNodeFromRequest(request);
	}
	
	/*
	 * This method will find the address of the machine that is the previous dialog path node.
	 * If there are proxies before the current one that are adding Record-Route we should visit them,
	 * otherwise just send to the client directly. And we don't want to visit proxies that are not
	 * Record-Routing, because they are not in the dialog path.
	 */
	private SipURI extractPreviousNodeFromRequest(SipServletRequestImpl request) {
		SipURI uri = null;
		try {
			// First check for record route
			RecordRouteHeader rrh = (RecordRouteHeader) request.getMessage().getHeader(RecordRouteHeader.NAME);
			if(rrh != null) {
				javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) rrh.getAddress().getURI();
				uri = new SipURIImpl(sipUri);
			} else { 
				// If no record route is found then use the last via (the originating endpoint)
				ListIterator<ViaHeader> viaHeaders = request.getMessage().getHeaders(ViaHeader.NAME);
				ViaHeader lastVia = null;
				while(viaHeaders.hasNext()) {
					lastVia = viaHeaders.next();
				} 
				String uriString = ((Via)lastVia).getSentBy().toString();
				uri = sipFactoryImpl.createSipURI(null, uriString);
				if(lastVia.getTransport() != null) {
					uri.setTransportParam(lastVia.getTransport());
				} else {
					uri.setTransportParam("udp");
				}
			}
		} catch (Exception e) {
			// We shouldn't completely fail in this case because it is rare to visit this code
			logger.error("Failed parsing previous address ", e);
		}
		return uri;

	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#cancel()
	 */
	public void cancel() {
		cancelAllExcept(null, null, null, null, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#cancel(java.lang.String[], int[], java.lang.String[])
	 */
	public void cancel(String[] protocol, int[] reasonCode, String[] reasonText) {
		cancelAllExcept(null, protocol, reasonCode, reasonText, true);
	}

	public void cancelAllExcept(ProxyBranch except, String[] protocol, int[] reasonCode, String[] reasonText, boolean throwExceptionIfCannotCancel) {
		if(ackReceived) throw new IllegalStateException("There has been an ACK received on this branch. Can not cancel.");
		for(ProxyBranch proxyBranch : proxyBranches.values()) {		
			if(!proxyBranch.equals(except)) {
				try {
					proxyBranch.cancel(protocol, reasonCode, reasonText);
				} catch (IllegalStateException e) {
					// TODO: Instead of catching excpetions here just determine if the branch is cancellable
					if(throwExceptionIfCannotCancel) throw e;
				}
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
			if(!JainSipUtils.checkScheme(target.toString())) {
				throw new IllegalArgumentException("Scheme " + target.getScheme() + " is not supported");
			}
			ProxyBranchImpl branch = new ProxyBranchImpl(target, this);
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

	/**
	 * @return the finalBranchForSubsequentRequest
	 */
	public ProxyBranchImpl getFinalBranchForSubsequentRequests() {
		return finalBranchForSubsequentRequests;
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
			if(!JainSipUtils.checkScheme(uri.toString())) {
				throw new IllegalArgumentException("Scheme " + uri.getScheme() + " is not supported");
			}
			ProxyBranchImpl branch = new ProxyBranchImpl((SipURI) uri, this);
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
		if(!JainSipUtils.checkScheme(uri.toString())) {
			throw new IllegalArgumentException("Scheme " + uri.getScheme() + " is not supported");
		}
		ProxyBranchImpl branch = new ProxyBranchImpl(uri, this);
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
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.Proxy#startProxy()
	 */
	public void startProxy() {
		if(this.ackReceived) 
			throw new IllegalStateException("Can't start. ACK has been received.");
		if(!this.originalRequest.isInitial())
			throw new IllegalStateException("Applications should not attepmt to " +
					"proxy subsequent requests. Proxying the initial request is " +
					"sufficient to carry all subsequent requests through the same" +
					" path.");

		// Only send TRYING when the request is INVITE, needed by testProxyGen2xx form TCK (it sends MESSAGE)
		if(this.originalRequest.getMethod().equals(Request.INVITE) && !tryingSent) {
			// Send provisional TRYING. Chapter 10.2
			// We must send only one TRYING no matter how many branches we spawn later.
			// This is needed for tests like testProxyBranchRecurse
			tryingSent = true;
			logger.info("Sending 100 Trying to the source");
			SipServletResponse trying =
				originalRequest.createResponse(100);			
			try {
				trying.send();
			} catch (IOException e) { 
				logger.error("Cannot send the 100 Trying",e);
			}
		}
		
		
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
					cancelAllExcept(branch, null, null, null, false);
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
				ProxyBranchImpl recurseBranch = new ProxyBranchImpl(contactURI, this);
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
			//Assume 600 and 400 are equally bad, the better one is the one that came first (TCK doBranchBranchTest)
			if(bestResponse != null) {
				if(response.getStatus()<400) {
					bestResponse = response;
					bestBranch = branch;
				}
			} else {
				bestResponse = response;
				bestBranch = branch;
			}
		}
		
		// Check if we are waiting for more response
		if(allResponsesHaveArrived()) {
			finalBranchForSubsequentRequests = bestBranch;
			sendFinalResponse(bestResponse, bestBranch);
		} else if(!parallel) {
			startNextUntriedBranch();
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
			getProxyUtils().createProxiedResponse(response, proxyBranch);
		
		if(proxiedResponse == null) {
			return; // this response was addressed to this proxy
		}

		try {
			proxiedResponse.send();
			proxyBranches = new LinkedHashMap<URI, ProxyBranch> ();
			originalRequest = null;
			bestBranch = null;
			bestResponse = null;
		} catch (IOException e) {
			logger.error("A problem occured while proxying the final response", e);
		}
	}
	
	ProxyUtils getProxyUtils() {
		if(proxyUtils == null) {
			proxyUtils = new ProxyUtils(sipFactoryImpl, this);
		}
		return proxyUtils;
	}

	/**
	 * @return the bestResponse
	 */
	public SipServletResponseImpl getBestResponse() {
		return bestResponse;
	}
	
	public void setOriginalRequest(SipServletRequestImpl originalRequest) {
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
	 * @return the sipFactoryImpl
	 */
	public SipFactoryImpl getSipFactoryImpl() {
		return sipFactoryImpl;
	}

	/**
	 * @param sipFactoryImpl the sipFactoryImpl to set
	 */
	public void setSipFactoryImpl(SipFactoryImpl sipFactoryImpl) {
		this.sipFactoryImpl = sipFactoryImpl;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutboundInterface(InetAddress inetAddress) {
		String address = inetAddress.getHostAddress();
		List<SipURI> list = this.sipFactoryImpl.getSipNetworkInterfaceManager().getOutboundInterfaces();
		SipURI networkInterface = null;
		for(SipURI networkInterfaceURI:list) {
			if(networkInterfaceURI.toString().contains(address)) {
				networkInterface = networkInterfaceURI;
			}
		}
		
		if(networkInterface == null) throw new IllegalArgumentException("Network interface for " +
				inetAddress.getHostAddress() + " not found");
		
		outboundInterface = networkInterface;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOutboundInterface(InetSocketAddress inetSocketAddress) {
		String address = inetSocketAddress.getAddress().getHostAddress()
			+ ":" + inetSocketAddress.getPort();
		List<SipURI> list = this.sipFactoryImpl.getSipNetworkInterfaceManager().getOutboundInterfaces();
		SipURI networkInterface = null;
		for(SipURI networkInterfaceURI:list) {
			if(networkInterfaceURI.toString().contains(address)) {
				networkInterface = networkInterfaceURI;
			}
		}
		
		if(networkInterface == null) throw new IllegalArgumentException("Network interface for " +
				address + " not found");		
		
		outboundInterface = networkInterface;
	}	
	
	public void setAckReceived(boolean received) {
		this.ackReceived = received;
	}
	
	public boolean getAckReceived() {
		return this.ackReceived;
	}
	
	public SipURI getPreviousNode() {
		return previousNode;
	}

	public String getCallerFromHeader() {
		return callerFromHeader;
	}

	public void setCallerFromHeader(String initiatorFromHeader) {
		this.callerFromHeader = initiatorFromHeader;
	}

}
