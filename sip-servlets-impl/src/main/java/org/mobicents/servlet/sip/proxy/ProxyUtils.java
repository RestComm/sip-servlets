/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.util.Iterator;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.ListeningPoint;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.address.Address;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxyBranch;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;
/**
 * TODO: Use outbound interface from ProxyParams.outboundInterface when adding local
 * listening point addresses.
 *
 */
public class ProxyUtils {
	private static final Logger logger = Logger.getLogger(ProxyUtils.class);
	
	public static Request createProxiedRequest(SipServletRequestImpl originalRequest, ProxyBranchImpl proxyBranch, URI destination, SipURI outboundInterface, SipURI routeRecord, SipURI path)
	{
		try {
			final Request clonedRequest = (Request) originalRequest.getMessage().clone();
			final String method = clonedRequest.getMethod();
			final ProxyImpl proxy = (ProxyImpl) proxyBranch.getProxy(); 
			final SipFactoryImpl sipFactoryImpl = proxy.getSipFactoryImpl();
			((MessageExt)clonedRequest).setApplicationData(null);


			String outboundTransport = null;

			RouteHeader rHeader = (RouteHeader) clonedRequest.getHeader(RouteHeader.NAME);
			if(rHeader != null) {
				String nextApp = ((javax.sip.address.SipURI)rHeader.getAddress().getURI()).getParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME);
				if(nextApp != null) {
					final MobicentsSipApplicationSessionKey sipAppKey = originalRequest.getSipSession().getSipApplicationSession().getKey();
					final String thisApp = sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(sipAppKey.getApplicationName());
					
					if(nextApp.equals(thisApp)) {
						clonedRequest.removeHeader(RouteHeader.NAME);
					}
					rHeader = (RouteHeader) clonedRequest.getHeader(RouteHeader.NAME);
				}
				if(rHeader != null) {
					outboundTransport = ((javax.sip.address.SipURI)rHeader.getAddress().getURI()).getTransportParam();
					if(outboundTransport == null) {
						outboundTransport = ListeningPoint.UDP;
					}
				}
				
			}
			if(outboundTransport == null) {
				// Fix for Issue 2783 : Proxying to non-UDP transport is broken
				if(clonedRequest.getRequestURI().isSipURI()) {
					outboundTransport = ((javax.sip.address.SipURI)clonedRequest.getRequestURI()).getTransportParam();
					if(destination != null && destination.isSipURI()) {
						String destinationTransport = ((SipURI)destination).getTransportParam();
						if(outboundTransport == null) {
							outboundTransport = destinationTransport;
						} else if(!outboundTransport.equalsIgnoreCase(destinationTransport)) {
							outboundTransport = destinationTransport;
						}
					}
				}
			}
			
			if(outboundTransport == null) outboundTransport = ListeningPoint.UDP;

			((MessageExt)clonedRequest).setApplicationData(outboundTransport);
			
			Via via = ((Via)clonedRequest.getHeader(Via.NAME));
			String inboundTransport = via.getTransport();
			if(inboundTransport == null) inboundTransport = ListeningPoint.UDP;
			
			if(logger.isDebugEnabled()) {
				logger.debug("Outbound transport = " + outboundTransport + " inbountTransport = " + inboundTransport);
			}

			/*
			 * The outbound transport has nothing to do with outbound interface
			if(proxy.getOutboundInterface() != null) {
				outboundTransport = proxy.getOutboundInterface().getTransportParam();
				if(outboundTransport == null) {
					if(proxy.getOutboundInterface().isSecure()) {
						outboundTransport =  ListeningPoint.TLS;
					} else {
						outboundTransport =  ListeningPoint.UDP;
					}
				}
			}*/
			
			// The target is null when proxying subsequent requests (the Route header is already there)
			if(destination != null)
			{				
				if(logger.isDebugEnabled()){
					logger.debug("request URI on the request to proxy : " + destination);
				}
				// only set the request URI if the request has no Route headers
				// see RFC3261 16.12
				// see http://code.google.com/p/mobicents/issues/detail?id=1847
				Header route = clonedRequest.getHeader("Route");
				if(route == null || 
						// it was decided that initial requests
						// should have their RURI changed to pass TCK testGetAddToPath001
						originalRequest.isInitial()) 
				{
					if(logger.isDebugEnabled()){
						logger.debug("setting request uri as cloned request has no Route headers: " + destination);
					}
					//this way everything is copied even the port but might not work for TelURI...
					clonedRequest.setRequestURI(((URIImpl)destination).getURI());
				}
				else
				{
					if(logger.isDebugEnabled()){
						logger.debug("NOT setting request uri as cloned request has at least one Route header: " + route);
					}

				}
				
//				// Add route header
//				javax.sip.address.SipURI routeUri = SipFactoryImpl.addressFactory.createSipURI(
//						params.destination.getUser(), params.destination.getHost());
//				routeUri.setPort(params.destination.getPort());
//				routeUri.setLrParam();
//				javax.sip.address.Address address = SipFactoryImpl.addressFactory.createAddress(params.destination.getUser(),
//						routeUri);
//				RouteHeader rheader = SipFactoryImpl.headerFactory.createRouteHeader(address);
//				
//				clonedRequest.setHeader(rheader);
			}
			else
			{
				// CANCELs are hop-by-hop, so here must remove any existing Via
				// headers,
				// Record-Route headers. We insert Via header below so we will
				// get response.
				if (method.equals(Request.CANCEL)) {
					clonedRequest.removeHeader(ViaHeader.NAME);
					clonedRequest.removeHeader(RecordRouteHeader.NAME);
				}
				
				SipConnector sipConnector = StaticServiceHolder.sipStandardService.findSipConnector(outboundTransport);
				if(sipConnector != null && sipConnector.isUseStaticAddress()) {
					
					// This is needed because otherwise we have the IP LB address here. If there is no route header
					// this means the request will go to the IP LB. For outbound requests we must bypass the IP LB.
					// http://code.google.com/p/mobicents/issues/detail?id=2210
					//clonedRequest.setRequestURI(((URIImpl)(proxyBranch).getTargetURI()).getURI());
					javax.sip.address.URI uri = clonedRequest.getRequestURI();
					if(uri.isSipURI()) {
						javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) uri;
						JainSipUtils.optimizeUriForInternalRoutingRequest(sipConnector, sipUri, originalRequest.getSipSession(), sipFactoryImpl, outboundTransport);
					}
					if(logger.isDebugEnabled()){
						logger.debug("setting request uri as cloned request has no Route headers and is using static address: " + destination);
					}
				}
			}

			// Decrease max forwards if available
			MaxForwardsHeader mf = (MaxForwardsHeader) clonedRequest
				.getHeader(MaxForwardsHeader.NAME);
			if (mf == null) {
				mf = SipFactoryImpl.headerFactory.createMaxForwardsHeader(70);
				clonedRequest.addHeader(mf);
			} else {
				mf.setMaxForwards(mf.getMaxForwards() - 1);
			}

			if (method.equals(Request.CANCEL)) {				
				// Cancel is hop by hop so remove all other via headers.
				clonedRequest.removeHeader(ViaHeader.NAME);				
			} 
			final MobicentsSipApplicationSessionKey sipAppKey = originalRequest.getSipSession().getSipApplicationSession().getKey();
			final String appName = sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(sipAppKey.getApplicationName());
			final SipServletRequestImpl proxyBranchMatchingRequest = (SipServletRequestImpl) proxyBranch.getMatchingRequest(originalRequest);
			
			//Add via header
			if(proxy.getOutboundInterface() == null) {
				String branchId = null;
				
				// http://code.google.com/p/mobicents/issues/detail?id=2359
				// ivan dubrov : TERMINATED state checking to avoid reusing the branchid for ACK to 200 
				if(Request.ACK.equals(method) && proxyBranchMatchingRequest != null && proxyBranchMatchingRequest.getTransaction() != null
						&& proxyBranchMatchingRequest.getTransaction().getState() != TransactionState.TERMINATED) {
					branchId = proxyBranchMatchingRequest.getTransaction().getBranchId();
					logger.debug("reusing original branch id " + branchId);
				} else {
					branchId = JainSipUtils.createBranch(sipAppKey.getId(),  appName);
				}
				// Issue 					
				proxyBranch.viaHeader = JainSipUtils.createViaHeader(
						sipFactoryImpl.getSipNetworkInterfaceManager(), clonedRequest, branchId, null);
			} else { 
				//If outbound interface is specified use it
				String branchId = null;

				// make sure to adjust for TLS
				if(proxy.getOutboundInterface().isSecure()) {
					outboundTransport =  ListeningPoint.TLS;
				}

				// http://code.google.com/p/mobicents/issues/detail?id=2359
				// ivan dubrov : TERMINATED state checking to avoid reusing the branchid for ACK to 200
				if(Request.ACK.equals(method) && proxyBranchMatchingRequest != null && proxyBranchMatchingRequest.getTransaction() != null
						&& proxyBranchMatchingRequest.getTransaction().getState() != TransactionState.TERMINATED) {
					branchId = proxyBranchMatchingRequest.getTransaction().getBranchId();
					logger.debug("reusing original branch id " + branchId);
				} else {
					branchId = JainSipUtils.createBranch(sipAppKey.getId(),  appName);
				}

				proxyBranch.viaHeader = SipFactoryImpl.headerFactory.createViaHeader(
						proxy.getOutboundInterface().getHost(),
						proxy.getOutboundInterface().getPort(),
						outboundTransport,
						branchId);
			}

			clonedRequest.addHeader(proxyBranch.viaHeader);		
			
			// Correction for misbehaving clients and unit testing
			if(clonedRequest.getHeader(RouteHeader.NAME) == null) {
				if(clonedRequest.getRequestURI().isSipURI()) {
					javax.sip.address.SipURI sipURI = ((javax.sip.address.SipURI)clonedRequest.getRequestURI());
					String transportFromURI = sipURI.getTransportParam();
					if(transportFromURI == null) transportFromURI = ListeningPoint.UDP;
					if(!transportFromURI.equalsIgnoreCase(outboundTransport)) 
						sipURI.setTransportParam(outboundTransport);
				}
			}
			
			
			//Add route-record header, if enabled and if needed (if not null)
			if(routeRecord != null && !Request.REGISTER.equalsIgnoreCase(method)) {
				if(!inboundTransport.equalsIgnoreCase(outboundTransport)) {
					javax.sip.address.SipURI inboundRURI = JainSipUtils.createRecordRouteURI(sipFactoryImpl.getSipNetworkInterfaceManager(), clonedRequest, inboundTransport);
					if(originalRequest.getTransport() != null) inboundRURI.setTransportParam(originalRequest.getTransport());
					final Iterator<String> paramNames = routeRecord.getParameterNames();
					// Copy the parameters set by the user
					while(paramNames.hasNext()) {
						String paramName = paramNames.next();
						if(!paramName.equalsIgnoreCase("transport")) {
							inboundRURI.setParameter(paramName,
									routeRecord.getParameter(paramName));
						}
					}

					inboundRURI.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME,
							appName);
					inboundRURI.setParameter(MessageDispatcher.RR_PARAM_PROXY_APP,
					"true");				
					inboundRURI.setParameter(MessageDispatcher.APP_ID, sipAppKey.getId());
					inboundRURI.setLrParam();

					final Address rraddress = SipFactoryImpl.addressFactory
					.createAddress(null, inboundRURI);
					final RecordRouteHeader recordRouteHeader = SipFactoryImpl.headerFactory
					.createRecordRouteHeader(rraddress);

					clonedRequest.addFirst(recordRouteHeader);
				}
				javax.sip.address.SipURI rrURI = null;
				if(proxy.getOutboundInterface() == null) {
					rrURI = JainSipUtils.createRecordRouteURI(sipFactoryImpl.getSipNetworkInterfaceManager(), clonedRequest, outboundTransport);
				} else {
					rrURI = ((SipURIImpl) proxy.getOutboundInterface()).getSipURI();
					rrURI.setTransportParam(outboundTransport);
				}
				
				final Iterator<String> paramNames = routeRecord.getParameterNames();
				
				// Copy the parameters set by the user
				while(paramNames.hasNext()) {
					String paramName = paramNames.next();
					if(!paramName.equalsIgnoreCase("transport")) {
						rrURI.setParameter(paramName,
								routeRecord.getParameter(paramName));
					}
				}
								
				rrURI.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME,
						appName);
				rrURI.setParameter(MessageDispatcher.RR_PARAM_PROXY_APP,
						"true");				
				rrURI.setParameter(MessageDispatcher.APP_ID, sipAppKey.getId());
				rrURI.setLrParam();
				
				final Address rraddress = SipFactoryImpl.addressFactory
					.createAddress(null, rrURI);
				final RecordRouteHeader recordRouteHeader = SipFactoryImpl.headerFactory
					.createRecordRouteHeader(rraddress);
				
				clonedRequest.addFirst(recordRouteHeader);
			}
			
			// Add path header
			if(path != null && Request.REGISTER.equalsIgnoreCase(method))
			{
				final javax.sip.address.SipURI pathURI = JainSipUtils.createRecordRouteURI(sipFactoryImpl.getSipNetworkInterfaceManager(), clonedRequest);

				final Iterator<String> paramNames = path.getParameterNames();				
				// Copy the parameters set by the user
				while(paramNames.hasNext()) {
					String paramName = paramNames.next();
					if(paramName.equals("lr")) {
						pathURI.setParameter(paramName, null);
					} else {
						pathURI.setParameter(paramName,
								path.getParameter(paramName));
					}
				}
				
				final Address pathAddress = SipFactoryImpl.addressFactory
					.createAddress(null, pathURI);
				
				// Here I need to reference the header factory impl class because can't create path header otherwise
				final PathHeader pathHeader = ((HeaderFactoryExt)SipFactoryImpl.headerFactory)
					.createPathHeader(pathAddress);
				
				clonedRequest.addFirst(pathHeader);
			}
			
			((SIPMessage)clonedRequest).setApplicationData(outboundTransport);
			
			return clonedRequest;
		} catch (Exception e) {
			throw new RuntimeException("Problem while creating the proxied request for message " + originalRequest.getMessage(), e);
		}
	}
	
	public static SipServletResponseImpl createProxiedResponse(MobicentsSipServletResponse sipServetResponse, MobicentsProxyBranch proxyBranch)
	{
		final Response response = (Response)sipServetResponse.getMessage();
		final Response clonedResponse = (Response)  response.clone();
		((MessageExt)clonedResponse).setApplicationData(null);
		final Transaction transaction = sipServetResponse.getTransaction();
		final int status = response.getStatusCode();
		// 1. Update timer C for provisional non retransmission responses
		if(transaction != null && ((SIPTransaction)transaction).getMethod().equals(Request.INVITE)) {
			if(Response.TRYING == status) {
				proxyBranch.cancel1xxTimer();
			}
			if(Response.TRYING < status && status < Response.OK) {
				proxyBranch.updateTimer(true);
			} else if(status >= Response.OK) {
				//remove it if response is final
				proxyBranch.cancel1xxTimer();
				proxyBranch.cancelTimer();
			}
		}
			
		// 2. Remove topmost via
		final Iterator<ViaHeader> viaHeaderIt = clonedResponse.getHeaders(ViaHeader.NAME);
		viaHeaderIt.next();
		viaHeaderIt.remove();
		if (!viaHeaderIt.hasNext()) {
			return null; // response was meant for this proxy
		}
		final ProxyImpl proxy = (ProxyImpl) proxyBranch.getProxy(); 
		final SipFactoryImpl sipFactoryImpl = proxy.getSipFactoryImpl();
		
		SipServletRequestImpl originalRequest =
			(SipServletRequestImpl) proxy.getOriginalRequest();
		
		if(Request.PRACK.equals(sipServetResponse.getMethod())) {
			originalRequest =
				(SipServletRequestImpl) proxyBranch.getPrackOriginalRequest();
		}
		
		SipServletResponseImpl newServletResponseImpl = null;
		
		if(transaction != null && originalRequest != null) {
			// non retransmission case
			newServletResponseImpl = (SipServletResponseImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletResponse(
					clonedResponse,		
					originalRequest.getTransaction(),
					originalRequest.getSipSession(),
					sipServetResponse.getDialog(),
					false,
					sipServetResponse.isRetransmission());
		} else {
			// retransmission case
			newServletResponseImpl = (SipServletResponseImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletResponse(
					clonedResponse,		
					null,
					sipServetResponse.getSipSession(),
					sipServetResponse.getDialog(),
					false,
					sipServetResponse.isRetransmission());
		}
		newServletResponseImpl.setOriginalRequest(originalRequest);
		newServletResponseImpl.setProxiedResponse(true);
		return newServletResponseImpl;
	}
	
	public static String toHexString(byte[] b) {
		int pos = 0;
		char[] c = new char[b.length * 2];

		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}

		return new String(c);
	}
	
	private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
