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

import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.util.Iterator;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.ListeningPoint;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
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
			((SIPMessage)clonedRequest).setApplicationData(null);

			// The target is null when proxying subsequent requests (the Route header is already there)
			if(destination != null)
			{				
				if(logger.isDebugEnabled()){
					logger.debug("request URI on the request to proxy : " + destination);
				}
				//this way everything is copied even the port but might not work for TelURI...
				clonedRequest.setRequestURI(((URIImpl)destination).getURI());
				
//				// Add route header
//				javax.sip.address.SipURI routeUri = SipFactories.addressFactory.createSipURI(
//						params.destination.getUser(), params.destination.getHost());
//				routeUri.setPort(params.destination.getPort());
//				routeUri.setLrParam();
//				javax.sip.address.Address address = SipFactories.addressFactory.createAddress(params.destination.getUser(),
//						routeUri);
//				RouteHeader rheader = SipFactories.headerFactory.createRouteHeader(address);
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
			}

			// Decrease max forwards if available
			MaxForwardsHeader mf = (MaxForwardsHeader) clonedRequest
				.getHeader(MaxForwardsHeader.NAME);
			if (mf == null) {
				mf = SipFactories.headerFactory.createMaxForwardsHeader(70);
				clonedRequest.addHeader(mf);
			} else {
				mf.setMaxForwards(mf.getMaxForwards() - 1);
			}

			if (method.equals(Request.CANCEL)) {				
				// Cancel is hop by hop so remove all other via headers.
				clonedRequest.removeHeader(ViaHeader.NAME);				
			} 
			final SipApplicationSessionKey sipAppKey = originalRequest.getSipSession().getSipApplicationSession().getKey();
			final String appName = sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(sipAppKey.getApplicationName());
			final SipServletRequestImpl proxyBranchRequest = (SipServletRequestImpl) proxyBranch.getRequest();
			//Add via header
			ViaHeader viaHeader = proxyBranch.viaHeader;
			if(viaHeader == null) {
				if(proxy.getOutboundInterface() == null) {
					String branchId = null;

					if(Request.ACK.equals(method) && proxyBranchRequest != null && proxyBranchRequest.getTransaction() != null) {
						branchId = proxyBranchRequest.getTransaction().getBranchId();
						logger.debug("reusing original branch id " + branchId);
					}
					// Issue 					
					viaHeader = JainSipUtils.createViaHeader(
							sipFactoryImpl.getSipNetworkInterfaceManager(), clonedRequest, branchId, null);
				} else { 
					//If outbound interface is specified use it
					String outboundTransport = proxy.getOutboundInterface().getTransportParam();
					if(outboundTransport == null) {
						if(proxy.getOutboundInterface().isSecure()) {
							outboundTransport =  ListeningPoint.TCP;
						} else {
							outboundTransport =  ListeningPoint.UDP;
						}
					}
					String branchId = null;

					if(Request.ACK.equals(method) && proxyBranchRequest != null && proxyBranchRequest.getTransaction() != null) {
						branchId = proxyBranchRequest.getTransaction().getBranchId();
						logger.debug("reusing original branch id " + branchId);
					}

					viaHeader = SipFactories.headerFactory.createViaHeader(
							proxy.getOutboundInterface().getHost(),
							proxy.getOutboundInterface().getPort(),
							outboundTransport,
							branchId);
				}
				proxyBranch.viaHeader = viaHeader;
			} else {
				String branchId = null;
				viaHeader = (ViaHeader) viaHeader.clone();
				if(Request.ACK.equals(method) && proxyBranchRequest != null && proxyBranchRequest.getTransaction() != null) {
					branchId = proxyBranchRequest.getTransaction().getBranchId();
					logger.debug("reusing original branch id " + branchId);
				} else {
					branchId = JainSipUtils.createBranch(sipAppKey.getId(), appName);
				}
				viaHeader.setBranch(branchId);
			}

			clonedRequest.addHeader(viaHeader);				
			
			
			//Add route-record header, if enabled and if needed (if not null)
			if(routeRecord != null && !Request.REGISTER.equalsIgnoreCase(method)) {
				javax.sip.address.SipURI rrURI = null;
				if(proxy.getOutboundInterface() == null) {
					rrURI = JainSipUtils.createRecordRouteURI(sipFactoryImpl.getSipNetworkInterfaceManager(), clonedRequest);
				} else {
					rrURI = ((SipURIImpl) proxy.getOutboundInterface()).getSipURI();
				}
				
				if(originalRequest.getTransport() != null) rrURI.setTransportParam(originalRequest.getTransport());
				
				final Iterator<String> paramNames = routeRecord.getParameterNames();
				
				// Copy the parameters set by the user
				while(paramNames.hasNext()) {
					String paramName = paramNames.next();
					rrURI.setParameter(paramName,
							routeRecord.getParameter(paramName));
				}
								
				rrURI.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME,
						appName);
				rrURI.setParameter(MessageDispatcher.RR_PARAM_PROXY_APP,
						"true");				
				rrURI.setParameter(MessageDispatcher.APP_ID, sipAppKey.getId());
				rrURI.setLrParam();
				
				final Address rraddress = SipFactories.addressFactory
					.createAddress(null, rrURI);
				final RecordRouteHeader recordRouteHeader = SipFactories.headerFactory
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
					pathURI.setParameter(paramName,
							path.getParameter(paramName));
				}
				
				final Address pathAddress = SipFactories.addressFactory
					.createAddress(null, pathURI);
				
				// Here I need to reference the header factory impl class because can't create path header otherwise
				final PathHeader pathHeader = ((HeaderFactoryExt)SipFactories.headerFactory)
					.createPathHeader(pathAddress);
				
				clonedRequest.addFirst(pathHeader);
			}
			
			return clonedRequest;
		} catch (Exception e) {
			throw new RuntimeException("Problem while creating the proxied request for message " + originalRequest.getMessage(), e);
		}
	}
	
	public static SipServletResponseImpl createProxiedResponse(SipServletResponseImpl sipServetResponse, ProxyBranchImpl proxyBranch)
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
			newServletResponseImpl = new SipServletResponseImpl(clonedResponse,		
					sipFactoryImpl,
					originalRequest.getTransaction(),
					originalRequest.getSipSession(),
					sipServetResponse.getDialog(),
					false,
					sipServetResponse.isRetransmission());
		} else {
			// retransmission case
			newServletResponseImpl = new SipServletResponseImpl(clonedResponse,		
					sipFactoryImpl,
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
