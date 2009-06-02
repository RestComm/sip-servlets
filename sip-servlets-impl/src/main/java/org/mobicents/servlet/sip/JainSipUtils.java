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
package org.mobicents.servlet.sip;


import gov.nist.javax.sip.header.extensions.ReferredByHeader;
import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.SIPMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sip.ListeningPoint;
import javax.sip.address.SipURI;
import javax.sip.header.AcceptEncodingHeader;
import javax.sip.header.AcceptHeader;
import javax.sip.header.AlertInfoHeader;
import javax.sip.header.AllowEventsHeader;
import javax.sip.header.AllowHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ErrorInfoHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.RAckHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.ReplyToHeader;
import javax.sip.header.RetryAfterHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubjectHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.message.SipFactoryImpl.NamesComparator;

/**
 * 
 * Various helpful utilities to map jain sip abstractions. 
 * 
 * @author mranga
 * @author Jean Deruelle
 */
public class JainSipUtils {
	
	/**
     * The maximum int value that could correspond to a port nubmer.
     */
    public static final int    MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port nubmer bindable
     * by the SIP Communicator.
     */
    public static final int    MIN_PORT_NUMBER = 1024;
    
	
    private static transient Logger logger = Logger.getLogger(JainSipUtils.class);

	public static String GLOBAL_IPADDRESS = "0.0.0.0";
	
	public static final TreeSet<String> dialogCreatingMethods = new TreeSet<String>(
			new NamesComparator());
	
	public static final TreeSet<String> dialogTerminatingMethods = new TreeSet<String>(
			new NamesComparator());

	static {		
		dialogCreatingMethods.add(Request.INVITE);
		dialogCreatingMethods.add(Request.SUBSCRIBE);
		dialogCreatingMethods.add(Request.REFER);
		dialogTerminatingMethods.add(Request.CANCEL);
		dialogTerminatingMethods.add(Request.BYE);
	}
	
	public static final String INITIAL_REMOTE_ADDR_HEADER_NAME = "MSS_Initial_Remote_Addr";
	public static final String INITIAL_REMOTE_PORT_HEADER_NAME = "MSS_Initial_Remote_Port";
	public static final String INITIAL_REMOTE_TRANSPORT_HEADER_NAME = "MSS_Initial_Remote_Transport";
	
	/**
	 * List of headers that ARE system at all times
	 */
	public static final Set<String> systemHeaders = new HashSet<String>();
	static {

		systemHeaders.add(FromHeader.NAME);
		systemHeaders.add(ToHeader.NAME);
		systemHeaders.add(CallIdHeader.NAME);
		systemHeaders.add(CSeqHeader.NAME);
		systemHeaders.add(ViaHeader.NAME);
		systemHeaders.add(RouteHeader.NAME);
		systemHeaders.add(RecordRouteHeader.NAME);
		systemHeaders.add(PathHeader.NAME);
		// This is system in messages other than REGISTER!!! ContactHeader.NAME
		// Contact is a system header field in messages other than REGISTER
		// requests and responses, 3xx and 485 responses, and 200/OPTIONS
		// responses. Additionally, for containers implementing the reliable
		// provisional responses extension, RAck and RSeq are considered system
		// headers also.
		systemHeaders.add(RSeqHeader.NAME);
		systemHeaders.add(RAckHeader.NAME);
		//custom header used by Mobicents Sip Servlets and not allowed to be overriden by apps
		systemHeaders.add(INITIAL_REMOTE_ADDR_HEADER_NAME);
		systemHeaders.add(INITIAL_REMOTE_PORT_HEADER_NAME);
		systemHeaders.add(INITIAL_REMOTE_TRANSPORT_HEADER_NAME);
	}

	public static final Set<String> addressHeadersNames = new HashSet<String>();

	static {

		// Section 4.1 The baseline SIP specification defines the following set of header
		// fields that conform to this grammar: From, To, Contact, Route,
		// Record-Route, Reply-To, Alert-Info, Call-Info, and Error-Info
		// The SipServletMessage interface defines a set of methods which operate 
		// on any address header field (see section 5.4.1 Parameterable and Address Header Fields ). 
		// This includes the RFC 3261 defined header fields listed above as well as extension headers 
		// such as Refer-To [refer] and P-Asserted-Identity [privacy]. 

		addressHeadersNames.add(FromHeader.NAME);
		addressHeadersNames.add(ToHeader.NAME);
		addressHeadersNames.add(ContactHeader.NAME);
		addressHeadersNames.add(RouteHeader.NAME);
		addressHeadersNames.add(RecordRouteHeader.NAME);
		addressHeadersNames.add(ReplyToHeader.NAME);
		addressHeadersNames.add(AlertInfoHeader.NAME);
		addressHeadersNames.add(CallInfoHeader.NAME);
		addressHeadersNames.add(ErrorInfoHeader.NAME);
		addressHeadersNames.add(ReferToHeader.NAME);
		addressHeadersNames.add(PAssertedIdentityHeader.NAME);
			
	}

	public static final Set<String> parameterableHeadersNames = new HashSet<String>();

	static {

		// All of the Address header fields are Parameterable, including Contact, From, To, Route, Record-Route, and Reply-To. 
		// In addition, the header fields Accept, Accept-Encoding, Alert-Info, 
		// Call-Info, Content-Disposition, Content-Type, Error-Info, Retry-After and Via are also Parameterable. 
		parameterableHeadersNames.add(FromHeader.NAME);
		parameterableHeadersNames.add(ToHeader.NAME);
		parameterableHeadersNames.add(ContactHeader.NAME);
		parameterableHeadersNames.add(RouteHeader.NAME);
		parameterableHeadersNames.add(RecordRouteHeader.NAME);
		parameterableHeadersNames.add(ReplyToHeader.NAME);
		parameterableHeadersNames.add(AcceptHeader.NAME);
		parameterableHeadersNames.add(AcceptEncodingHeader.NAME);
		parameterableHeadersNames.add(AlertInfoHeader.NAME);
		parameterableHeadersNames.add(CallInfoHeader.NAME);
		parameterableHeadersNames.add(ContentDispositionHeader.NAME);
		parameterableHeadersNames.add(ContentTypeHeader.NAME);
		parameterableHeadersNames.add(ErrorInfoHeader.NAME);
		parameterableHeadersNames.add(RetryAfterHeader.NAME);
		parameterableHeadersNames.add(ViaHeader.NAME);
			
	}

	
	public static final Map<String, String> headerCompact2FullNamesMappings = new HashMap<String, String>();

	{ // http://www.iana.org/assignments/sip-parameters
		// Header Name compact Reference
		// ----------------- ------- ---------
		// Call-ID i [RFC3261]
		// From f [RFC3261]
		// To t [RFC3261]
		// Via v [RFC3261]
		// =========== NON SYSTEM HEADERS ========
		// Contact m [RFC3261] <-- Possibly system header
		// Accept-Contact a [RFC3841]
		// Allow-Events u [RFC3265]
		// Content-Encoding e [RFC3261]
		// Content-Length l [RFC3261]
		// Content-Type c [RFC3261]
		// Event o [RFC3265]
		// Identity y [RFC4474]
		// Identity-Info n [RFC4474]
		// Refer-To r [RFC3515]
		// Referred-By b [RFC3892]
		// Reject-Contact j [RFC3841]
		// Request-Disposition d [RFC3841]
		// Session-Expires x [RFC4028]
		// Subject s [RFC3261]
		// Supported k [RFC3261]

		headerCompact2FullNamesMappings.put("i", CallIdHeader.NAME);
		headerCompact2FullNamesMappings.put("f", FromHeader.NAME);
		headerCompact2FullNamesMappings.put("t", ToHeader.NAME);
		headerCompact2FullNamesMappings.put("v", ViaHeader.NAME);
		headerCompact2FullNamesMappings.put("m", ContactHeader.NAME);
		// headerCompact2FullNamesMappings.put("a",); // Where is this header?
		headerCompact2FullNamesMappings.put("u", AllowEventsHeader.NAME);
		headerCompact2FullNamesMappings.put("e", ContentEncodingHeader.NAME);
		headerCompact2FullNamesMappings.put("l", ContentLengthHeader.NAME);
		headerCompact2FullNamesMappings.put("c", ContentTypeHeader.NAME);
		headerCompact2FullNamesMappings.put("o", EventHeader.NAME);
		// headerCompact2FullNamesMappings.put("y", IdentityHeader); // Where is
		// sthis header?
		// headerCompact2FullNamesMappings.put("n",IdentityInfoHeader );
		headerCompact2FullNamesMappings.put("r", ReferToHeader.NAME);
		 headerCompact2FullNamesMappings.put("b", ReferredByHeader.NAME);
		// headerCompact2FullNamesMappings.put("j", RejectContactHeader);
		headerCompact2FullNamesMappings.put("d", ContentDispositionHeader.NAME);
		 headerCompact2FullNamesMappings.put("x", SessionExpiresHeader.NAME);
		headerCompact2FullNamesMappings.put("s", SubjectHeader.NAME);
		headerCompact2FullNamesMappings.put("k", SupportedHeader.NAME);
	}

	public static final Map<String, String> headerFull2CompactNamesMappings = new HashMap<String, String>();

	static { // http://www.iana.org/assignments/sip-parameters
		// Header Name compact Reference
		// ----------------- ------- ---------
		// Call-ID i [RFC3261]
		// From f [RFC3261]
		// To t [RFC3261]
		// Via v [RFC3261]
		// =========== NON SYSTEM HEADERS ========
		// Contact m [RFC3261] <-- Possibly system header
		// Accept-Contact a [RFC3841]
		// Allow-Events u [RFC3265]
		// Content-Encoding e [RFC3261]
		// Content-Length l [RFC3261]
		// Content-Type c [RFC3261]
		// Event o [RFC3265]
		// Identity y [RFC4474]
		// Identity-Info n [RFC4474]
		// Refer-To r [RFC3515]
		// Referred-By b [RFC3892]
		// Reject-Contact j [RFC3841]
		// Request-Disposition d [RFC3841]
		// Session-Expires x [RFC4028]
		// Subject s [RFC3261]
		// Supported k [RFC3261]

		headerFull2CompactNamesMappings.put(CallIdHeader.NAME, "i");
		headerFull2CompactNamesMappings.put(FromHeader.NAME, "f");
		headerFull2CompactNamesMappings.put(ToHeader.NAME, "t");
		headerFull2CompactNamesMappings.put(ViaHeader.NAME, "v");
		headerFull2CompactNamesMappings.put(ContactHeader.NAME, "m");
		// headerFull2CompactNamesMappings.put(,"a"); // Where is this header?
		headerFull2CompactNamesMappings.put(AllowEventsHeader.NAME, "u");
		headerFull2CompactNamesMappings.put(ContentEncodingHeader.NAME, "e");
		headerFull2CompactNamesMappings.put(ContentLengthHeader.NAME, "l");
		headerFull2CompactNamesMappings.put(ContentTypeHeader.NAME, "c");
		headerFull2CompactNamesMappings.put(EventHeader.NAME, "o");
		// headerCompact2FullNamesMappings.put(IdentityHeader,"y"); // Where is
		// sthis header?
		// headerCompact2FullNamesMappings.put(IdentityInfoHeader ,"n");
		headerFull2CompactNamesMappings.put(ReferToHeader.NAME, "r");
		// headerCompact2FullNamesMappings.put(ReferedByHeader,"b");
		// headerCompact2FullNamesMappings.put(RejectContactHeader,"j");
		headerFull2CompactNamesMappings.put(ContentDispositionHeader.NAME, "d");
		// headerCompact2FullNamesMappings.put(SessionExpiresHeader,"x");
		headerFull2CompactNamesMappings.put(SubjectHeader.NAME, "s");
		headerFull2CompactNamesMappings.put(SupportedHeader.NAME, "k");
	}
	
	public static final Set<String> ianaAllowedContentTypes = new HashSet<String>();	

	static {

		// All of the Address header fields are Parameterable, including Contact, From, To, Route, Record-Route, and Reply-To. 
		// In addition, the header fields Accept, Accept-Encoding, Alert-Info, 
		// Call-Info, Content-Disposition, Content-Type, Error-Info, Retry-After and Via are also Parameterable. 
		ianaAllowedContentTypes.add("application");
		ianaAllowedContentTypes.add("audio");
		ianaAllowedContentTypes.add("example");
		ianaAllowedContentTypes.add("image");
		ianaAllowedContentTypes.add("message");
		ianaAllowedContentTypes.add("model");
		ianaAllowedContentTypes.add("multipart");
		ianaAllowedContentTypes.add("text");
		ianaAllowedContentTypes.add("video");
			
	}

	// we don't have any other choice as to maintain a static list of multi value headers
	// because checking for , for the values as a delimiter won't work for WWW-Authenticate header which is not a multivalue header
	// but contains multiple , 
	public static final Set<String> multiValueHeaders = new HashSet<String>();	

	static {
		multiValueHeaders.add(AllowHeader.NAME);
	}

	private static final String[] allowedAddressSchemes = {"sip","sips","http","https","tel","tels","mailto"};
	
	public static final int MAX_FORWARD_HEADER_VALUE = 70;

	// never instantiate a utility class : Enforce noninstantiability with private constructor
    private JainSipUtils() {
    	throw new AssertionError();    	
    }
	
	// RFC 1918 address spaces
	public static int getAddressOutboundness(String address) {
		if(address.startsWith("127.0")) return 0;
		if(address.startsWith("192.168")) return 1;
		if(address.startsWith("10.")) return 2;
		if(address.startsWith("172.16") || address.startsWith("172.17") || address.startsWith("172.18") 
				|| address.startsWith("172.19") || address.startsWith("172.20") || address.startsWith("172.21") 
				|| address.startsWith("172.22") || address.startsWith("172.23") || address.startsWith("172.24")
				|| address.startsWith("172.25") || address.startsWith("172.26") || address.startsWith("172.27")
				|| address.startsWith("172.28") || address.startsWith("172.29") || address.startsWith("172.30")
				|| address.startsWith("172.31")) return 3;
		if(address.indexOf(".")>0) return 4; // match IPv4 addresses heuristically
		return -1; // matches IPv6 or something malformed;
	}
	public static String getMostOutboundAddress(List<String> addresses) {
		// getIpAddresses() returns [0:0:0:0:0:0:0:1, 127.0.0.1, 2001:0:d5c7:a2ca:3065:1735:3f57:fe71, fe80:0:0:0:3065:1735:3f57:fe71%15, 192.168.1.142, fe80:0:0:0:0:5efe:c0a8:18e%21]
		// IPv6 addresses are not good for default value
		String bestAddr = "127.0.0.1"; // The default is something completely fails
		int bestAddrOutboundness = -2;
		for(String address:addresses)  {
			int addrOutboundness = getAddressOutboundness(address);
			if(addrOutboundness>bestAddrOutboundness) {
				bestAddr = address;
				bestAddrOutboundness = addrOutboundness;
			}
		}
		return bestAddr;
	}
	
	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @param branch
	 * @return
	 */
	public static ViaHeader createViaHeader(
			SipNetworkInterfaceManager sipNetworkInterfaceManager, Request request, String branch) {
		String transport = findTransport(request);
		ExtendedListeningPoint listeningPoint = 
			sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		boolean usePublicAddress = findUsePublicAddress(
				sipNetworkInterfaceManager, request, listeningPoint);
		return listeningPoint.createViaHeader(branch, usePublicAddress);		
    }
	 
	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @return
	 */
	public static ContactHeader createContactHeader(SipNetworkInterfaceManager sipNetworkInterfaceManager, Request request, String displayName) {
		String transport = findTransport(request);
		ExtendedListeningPoint listeningPoint = 
			sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		boolean usePublicAddress = findUsePublicAddress(
				sipNetworkInterfaceManager, request, listeningPoint);
		return listeningPoint.createContactHeader(displayName, usePublicAddress);
	}

	/**
	 * 
	 * @param sipProviders
	 * @param transport
	 * @return
	 */
	public static javax.sip.address.SipURI createRecordRouteURI(SipNetworkInterfaceManager sipNetworkInterfaceManager, Message message) {
		String transport = findTransport(message);		
		ExtendedListeningPoint listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		boolean usePublicAddress = findUsePublicAddress(
				sipNetworkInterfaceManager, message, listeningPoint);
		return listeningPoint.createRecordRouteURI(usePublicAddress);
	}

	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param request
	 * @param listeningPoint
	 * @return
	 */
	public static boolean findUsePublicAddress(
			SipNetworkInterfaceManager sipNetworkInterfaceManager,
			Message message, ExtendedListeningPoint listeningPoint) {
		boolean usePublicAddress = false;
		if(listeningPoint.isUseStaticAddress()) {
			usePublicAddress = true;
		} else if(listeningPoint.getGlobalIpAddress() != null) {
			usePublicAddress = sipNetworkInterfaceManager.findUsePublicAddress(message);
		}
		return usePublicAddress;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String findTransport(Message message) {		
		if(message == null) {
			return ListeningPoint.UDP;
		}
		// check if the transport is present in the message application data for maximizing perf
		String transport = (String)((SIPMessage)message).getApplicationData();
		if(transport != null) {			
			return transport;
		}
		//if the request uri doesn't have any param, the request can still be on TCP so we check the topmost via header
		ViaHeader topmostViaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
		if(topmostViaHeader != null) {
			String viaTransport = topmostViaHeader.getTransport();
			if(viaTransport != null && viaTransport.length() > 0) {
				transport = viaTransport;
			}
		}
		if(transport == null && message instanceof Request) {
			transport = ListeningPoint.UDP;
			Request request = (Request)message;

			if(request.getRequestURI() instanceof SipURI) {
				String transportParam = ((javax.sip.address.SipURI) request
					.getRequestURI()).getTransportParam();
				
				if (transportParam != null
						&& transportParam.equalsIgnoreCase(ListeningPoint.TLS)) {
					transport = ListeningPoint.TLS;
				}
				//Fix by Filip Olsson for Issue 112
				else if ((transportParam != null
					&& transportParam.equalsIgnoreCase(ListeningPoint.TCP)) || 
					request.getContentLength().getContentLength() > 4096) {
					transport = ListeningPoint.TCP;
				}
			}
		}
		// storing the transport is present in the message application data for maximizing perf 
		// in speeding up the retrieval later on
		((SIPMessage)message).setApplicationData(transport);
		return transport;
	}
	
	public static boolean checkScheme(String address) {
		for(String scheme:allowedAddressSchemes) {
			int start = address.indexOf("<");
			if(start >= 0) {
				int end = address.indexOf(">");
				address = address.substring(start + 1, end);
			}
				
			if(scheme.equalsIgnoreCase(address.substring(0, scheme.length())))
				return true;
		}
		return false;
	}
}
