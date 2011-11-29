/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.mobicents.servlet.sip.rfc5626;

import gov.nist.javax.sip.header.ims.PathHeader;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.sip.SipException;
import javax.sip.address.Hop;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.core.HopImpl;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class RFC5626Helper {
	
	private static final Logger logger = Logger.getLogger(ProxyBranchImpl.class);
	
	private static final String ALGORITHM = "HmacSHA1";
	private static Mac mac = null;
	private static SecretKey secretKey = null; 
    	
	static{
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
			// 20 octet key * 8 bits	
			keyGenerator.init(20 * 8);
	        secretKey = keyGenerator.generateKey();
			mac = Mac.getInstance(ALGORITHM);
			mac.init(secretKey);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Couldn't find algorithm " + ALGORITHM, e);
		} catch (InvalidKeyException e) {
			logger.error("Invalid Key " + secretKey, e);
		}
	}
	
	public static void checkRequest(ProxyBranchImpl proxyBranch, Request request, SipServletRequestImpl originalRequest) throws IncorrectFlowIdentifierException {
		if(!((ProxyImpl)proxyBranch.getProxy()).getSipOutboundSupport()) {
			return;
		}
//		final Request request = (Request) sipServletRequestImpl.getMessage();
		final ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);		
		final ProxyImpl proxy = (ProxyImpl) proxyBranch.getProxy();		
		
		if(proxyBranch.isAddToPath() && request.getMethod().equalsIgnoreCase(Request.REGISTER)) {
			//RFC 5626 Section 5.1. Processing Register Requests			
			if(contactHeader != null && contactHeader.getParameter(MessageDispatcher.SIP_OUTBOUND_PARAM_REG_ID) != null) {
				int nbVias = 0;
				ListIterator<ViaHeader> vias = request.getHeaders(ViaHeader.NAME);
				while (vias.hasNext() && nbVias <= 2) {
					vias.next();
					nbVias++;
				}
				/* if the edge proxy is the first SIP node
				   after the UAC, the edge proxy either MUST store a "flow token"
				   (containing information about the flow from the previous hop) in its
				   Path URI */
				if(nbVias == 2) {
					PathHeader pathHeader = (PathHeader) request.getHeader(PathHeader.NAME);
					javax.sip.address.SipURI pathURI = (javax.sip.address.SipURI) pathHeader.getAddress().getURI();
					String flowToken = generateFlowToken(originalRequest);
					try {
						pathURI.setUser(flowToken);					
						pathURI.setParameter(MessageDispatcher.SIP_OUTBOUND_PARAM_OB, null);
					} catch (ParseException e) {
						logger.error("Impossible to add the ob parameter to the following path header " + pathHeader + " or flow token " + flowToken, e);
					}
				}
			}
		}else {
			//RFC 5626 Section 5.3. Forwarding Non-REGISTER Requests
			RouteHeader poppedRouteHeader = originalRequest.getPoppedRouteHeader();
			boolean poppedRouteHasObParam = false;
			boolean contactHasObParam = false;
			if(poppedRouteHeader != null && poppedRouteHeader.getParameter(MessageDispatcher.SIP_OUTBOUND_PARAM_OB) != null) {
				poppedRouteHasObParam = true;
			}
			if(contactHeader != null && contactHeader.getParameter(MessageDispatcher.SIP_OUTBOUND_PARAM_REG_ID) != null) {
				contactHasObParam = true;
			}
			javax.sip.address.SipURI poppedURI = (javax.sip.address.SipURI) poppedRouteHeader.getAddress().getURI();
			final String user = (poppedURI).getUser();
			Hop hop = null;
			if(user != null) {				
				hop = decodeFlowToken(user);				
				if(logger.isDebugEnabled()) {
					logger.debug("Hop " + hop + " poppedRouteHasObParam " + poppedRouteHasObParam + " contactHasObParam " + contactHasObParam
					+ " InitialRemoteAddr " + originalRequest.getInitialRemoteAddr() + " InitialRemotePort " + originalRequest.getInitialRemotePort());
				}
			}
			if(hop == null && !poppedRouteHasObParam && !contactHasObParam) {
				if(logger.isDebugEnabled()) {
					logger.debug("no RFC5626 flow idenitifer, returning");
				}
				return;
			} else if((hop != null && hop.getHost().equals(originalRequest.getInitialRemoteAddr()) && hop.getPort() == originalRequest.getInitialRemotePort()) || (hop == null && (contactHasObParam || poppedRouteHasObParam))) {
				if(logger.isDebugEnabled()) {
					logger.debug("RFC5626 Outgoing Case " + request);
				}			
				if(originalRequest.isInitial()) {
					//5.3.2 outgoing request case
					//proxy to add a Record-Route header field value that contains the flow-token
					addRecordRouteHeader(request, proxy.getSipFactoryImpl(), poppedURI);
				}
			} else if(hop != null) {
				//5.3.1 incoming request case
				 /*If the Route header value contains an "ob" URI parameter, the Route
				   header was probably copied from the Path header in a registration.
				   If the Route header value contains an "ob" URI parameter, and the
				   request is a new dialog-forming request, the proxy needs to adjust
				   the route set to ensure that subsequent requests in the dialog can be
				   delivered over a valid flow to the UA instance identified by the flow
				   token.*/
				if(logger.isDebugEnabled()) {
					logger.debug("RFC5626 Incoming Case " + request);
				}
				SipFactoryImpl sipFactoryImpl = proxy.getSipFactoryImpl();
				if(originalRequest.isInitial() && JainSipUtils.DIALOG_CREATING_METHODS.contains(originalRequest.getMethod())) {					
					addRecordRouteHeader(request, sipFactoryImpl, poppedURI);	
				} 
				try {
					javax.sip.address.SipURI routeURI = (javax.sip.address.SipURI) 
						sipFactoryImpl.getAddressFactory().createSipURI(null, hop.getHost());
					routeURI.setPort(hop.getPort());
					routeURI.setTransportParam(hop.getTransport());
					routeURI.setLrParam();
					RouteHeader routeHeader = sipFactoryImpl.getHeaderFactory().createRouteHeader(
							sipFactoryImpl.getAddressFactory().createAddress(routeURI));
					request.addHeader(routeHeader);
				} catch (ParseException e) {
					logger.error("Impossible to parse the following popped URI " + poppedURI, e);
				} 
			}
		}			
	}

	/**
	 * @param proxyBranch
	 * @param request
	 * @param proxy
	 * @param poppedURI
	 */
	private static void addRecordRouteHeader(
			Request request, SipFactoryImpl sipFactoryImpl,
			javax.sip.address.SipURI poppedURI) {
		try {
			SipURI recordRouteURI = (SipURI)((RecordRouteHeader)request.getHeader(RecordRouteHeader.NAME)).getAddress().getURI();
			javax.sip.address.SipURI newRecordRouteURI = (javax.sip.address.SipURI) 
				sipFactoryImpl.getAddressFactory().createURI(recordRouteURI.toString());
			newRecordRouteURI.removeParameter(MessageDispatcher.SIP_OUTBOUND_PARAM_OB);
			newRecordRouteURI.setUser(poppedURI.getUser());
			RecordRouteHeader recordRouteHeader = sipFactoryImpl.getHeaderFactory().createRecordRouteHeader(
					sipFactoryImpl.getAddressFactory().createAddress(newRecordRouteURI));
			// removes the header created when the request was cloned
			request.removeFirst(RecordRouteHeader.NAME);
			request.addFirst(recordRouteHeader);
		} catch (ParseException e) {
			logger.error("Impossible to parse the following popped URI " + poppedURI, e);
		} catch (SipException e) {
			logger.error("Impossible to add the following recordRouteHeader ", e);
		}
	}

	/* Excerpt from RFC 5626 Section 5.3.1
	   Proxies that used the example algorithm described in Section 5.2 to
	   form a flow token follow the procedures below to determine the
	   correct flow.  To decode the flow token, take the flow identifier in
	   the user portion of the URI and base64 decode it, then verify the
	   HMAC is correct by recomputing the HMAC and checking that it matches.
	   If the HMAC is not correct, the request has been tampered with.
	 */
	private static HopImpl decodeFlowToken(String user) throws IncorrectFlowIdentifierException {
		if(logger.isDebugEnabled()) {
			logger.debug("Decoding RFC 5626 Flow token " + user);
		}		
		byte[] byteConcat = Base64.decodeBase64(user.getBytes());
		if(logger.isDebugEnabled()) {
			logger.debug("Decoding RFC 5626 Flow token byteContact after base64 decoding is " + new String(byteConcat).trim());
		}
		String concatenation = new String(byteConcat);
		int indexOfSlash = concatenation.indexOf("/");
		String hmac = concatenation.substring(0, indexOfSlash);
		String arrayS = concatenation.substring(indexOfSlash + 1, concatenation.length());
		if(logger.isDebugEnabled()) {
			logger.debug("Decoding RFC 5626 Flow token recomputing hmac of array S " + arrayS);
		}
		byte[] recomputedHmac = mac.doFinal(arrayS.trim().getBytes());
		if(logger.isDebugEnabled()) {
			logger.debug("Decoding RFC 5626 Flow token recomputed Hmac" + recomputedHmac);
		}
		recomputedHmac = new String(recomputedHmac).trim().getBytes();
		if(!Arrays.equals(hmac.trim().getBytes(),recomputedHmac)) {
			throw new IncorrectFlowIdentifierException("hmac " + hmac + " is different from the recomputed hmac " + new String(recomputedHmac).trim());
		}
		StringTokenizer stringTokenizer = new StringTokenizer(arrayS, "_");
		String transport = stringTokenizer.nextToken();
		stringTokenizer.nextToken();
		stringTokenizer.nextToken();
		String initialRemoteAddress = stringTokenizer.nextToken();
		int initialRemotePort = Integer.parseInt(stringTokenizer.nextToken());
		return new HopImpl(initialRemoteAddress, initialRemotePort, transport);
	}

	/* 
	  Excerpt from RFC 5626 Section 5.2. Generating Flow Tokens
	  Example Algorithm: When the proxy boots, it selects a 20-octet
      crypto random key called K that only the edge proxy knows.  A byte
      array, called S, is formed that contains the following information
      about the flow the request was received on: an enumeration
      indicating the protocol, the local IP address and port, the remote
      IP address and port.  The HMAC of S is computed using the key K
      and the HMAC-SHA1-80 algorithm, as defined in [RFC2104].  The
      concatenation of the HMAC and S are base64 encoded, as defined in
      [RFC4648], and used as the flow identifier.  When using IPv4
      addresses, this will result in a 32-octet identifier.*/
	private static String generateFlowToken(SipServletRequestImpl request) {
		StringBuffer arrayS = new StringBuffer();
		arrayS.append(request.getTransport());
		arrayS.append("_");
		arrayS.append(request.getLocalAddr());
		arrayS.append("_");
		arrayS.append(request.getLocalPort());
		arrayS.append("_");
		arrayS.append(request.getInitialRemoteAddr());
		arrayS.append("_");
		arrayS.append(request.getInitialRemotePort());
		if(logger.isDebugEnabled()) {
			logger.debug("Generating RFC 5626 Flow token from " + arrayS);
		}
		byte[] byteArrayS = arrayS.toString().trim().getBytes();
		byte[] hmac = mac.doFinal(byteArrayS);
		if(logger.isDebugEnabled()) {
			logger.debug("Generating RFC 5626 Flow token hmac is " + new String(hmac).trim());
		}
		hmac = new String(hmac).trim().getBytes();
		byte[] concatDelimiter = "/".getBytes();
		byte[] byteConcat = new byte[hmac.length + concatDelimiter.length + byteArrayS.length];
		System.arraycopy(hmac, 0, byteConcat, 0, hmac.length);		
		System.arraycopy(concatDelimiter, 0, byteConcat, hmac.length, concatDelimiter.length);
		System.arraycopy(byteArrayS, 0, byteConcat, hmac.length + concatDelimiter.length, byteArrayS.length);
		if(logger.isDebugEnabled()) {
			logger.debug("Generating RFC 5626 Flow token byteContact before base64 encoding is " + new String(byteConcat).trim());
		}
		// does not result in a 32 octet id though...
		byte[] base64Encoded = Base64.encodeBase64(byteConcat);
		if(logger.isDebugEnabled()) {
			logger.debug("Generating RFC 5626 Flow token byteContact after base64 encoding is " + new String(base64Encoded).trim());
		}
		return new String(base64Encoded).trim();
	}
}
