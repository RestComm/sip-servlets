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

package org.mobicents.servlet.sip;


import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.header.extensions.ReferredByHeader;
import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PAssociatedURIHeader;
import gov.nist.javax.sip.header.ims.PMediaAuthorizationHeader;
import gov.nist.javax.sip.header.ims.PVisitedNetworkIDHeader;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.header.ims.PrivacyHeader;
import gov.nist.javax.sip.header.ims.SecurityClientHeader;
import gov.nist.javax.sip.header.ims.SecurityServerHeader;
import gov.nist.javax.sip.header.ims.SecurityVerifyHeader;
import gov.nist.javax.sip.header.ims.ServiceRouteHeader;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.SIPClientTransaction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.Transaction;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.AcceptEncodingHeader;
import javax.sip.header.AcceptHeader;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.AlertInfoHeader;
import javax.sip.header.AllowEventsHeader;
import javax.sip.header.AllowHeader;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.DateHeader;
import javax.sip.header.ErrorInfoHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.InReplyToHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.MimeVersionHeader;
import javax.sip.header.MinExpiresHeader;
import javax.sip.header.OrganizationHeader;
import javax.sip.header.PriorityHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ProxyRequireHeader;
import javax.sip.header.RAckHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.ReplyToHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RetryAfterHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.SubjectHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.TimeStampHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UnsupportedHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.header.WarningHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl.NamesComparator;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * 
 * Various helpful utilities to map jain sip abstractions. 
 * 
 * @author mranga
 * @author Jean Deruelle
 */
public final class JainSipUtils {
	private static final Logger logger = Logger.getLogger(JainSipUtils.class);
	
	/**
     * The maximum int value that could correspond to a port nubmer.
     */
    public static final int    MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port nubmer bindable
     * by the SIP Communicator.
     */
    public static final int    MIN_PORT_NUMBER = 1024;
    
	
//    private static final transient Logger logger = Logger.getLogger(JainSipUtils.class);

	public static final String GLOBAL_IPADDRESS = "0.0.0.0";
	
	// methods where a contact header is mandatory
	public static final Set<String> CONTACT_HEADER_METHODS = new TreeSet<String>(
			new NamesComparator());
	
	static {		
		CONTACT_HEADER_METHODS.add(Request.INVITE);
		// Issue 1412 http://code.google.com/p/mobicents/issues/detail?id=1412 
		// Contact header is added to REGISTER request by container but javadoc says
		// "The container is responsible for assigning the request appropriate Call-ID and CSeq 
		// headers, as well as Contact header if the method is not REGISTER." so commenting out
//		CONTACT_HEADER_METHODS.add(Request.REGISTER);
		CONTACT_HEADER_METHODS.add(Request.SUBSCRIBE);
		CONTACT_HEADER_METHODS.add(Request.NOTIFY);
		CONTACT_HEADER_METHODS.add(Request.REFER);
		CONTACT_HEADER_METHODS.add(Request.UPDATE);
	}
	
	public static final Set<String> DIALOG_CREATING_METHODS = new TreeSet<String>(
			new NamesComparator());
	
	public static final Set<String> DIALOG_TERMINATING_METHODS = new TreeSet<String>(
			new NamesComparator());

	static {		
		DIALOG_CREATING_METHODS.add(Request.INVITE);
		DIALOG_CREATING_METHODS.add(Request.SUBSCRIBE);
		DIALOG_CREATING_METHODS.add(Request.REFER);
		DIALOG_TERMINATING_METHODS.add(Request.CANCEL);
		DIALOG_TERMINATING_METHODS.add(Request.BYE);
	}
	
	public static final String INITIAL_REMOTE_ADDR_HEADER_NAME = "MSS_Initial_Remote_Addr";
	public static final String INITIAL_REMOTE_PORT_HEADER_NAME = "MSS_Initial_Remote_Port";
	public static final String INITIAL_REMOTE_TRANSPORT_HEADER_NAME = "MSS_Initial_Remote_Transport";
	
	/**
	 * List of headers that ARE system at all times
	 */
	public static final Set<String> SYSTEM_HEADERS = new HashSet<String>();
	static {

		// From and To are not system header in requests except for their tag 
//		SYSTEM_HEADERS.add(FromHeader.NAME);
//		SYSTEM_HEADERS.add(ToHeader.NAME);
		SYSTEM_HEADERS.add(CallIdHeader.NAME);
		SYSTEM_HEADERS.add(CSeqHeader.NAME);
		SYSTEM_HEADERS.add(ViaHeader.NAME);
		SYSTEM_HEADERS.add(RouteHeader.NAME);
		SYSTEM_HEADERS.add(RecordRouteHeader.NAME);
		SYSTEM_HEADERS.add(PathHeader.NAME);
		// This is system in messages other than REGISTER!!! ContactHeader.NAME
		// Contact is a system header field in messages other than REGISTER
		// requests and responses, 3xx and 485 responses, and 200/OPTIONS
		// responses. Additionally, for containers implementing the reliable
		// provisional responses extension, RAck and RSeq are considered system
		// headers also.
		SYSTEM_HEADERS.add(RSeqHeader.NAME);
		SYSTEM_HEADERS.add(RAckHeader.NAME);
		//custom header used by Mobicents Sip Servlets and not allowed to be overriden by apps
		SYSTEM_HEADERS.add(INITIAL_REMOTE_ADDR_HEADER_NAME);
		SYSTEM_HEADERS.add(INITIAL_REMOTE_PORT_HEADER_NAME);
		SYSTEM_HEADERS.add(INITIAL_REMOTE_TRANSPORT_HEADER_NAME);
	}

	public static final Set<String> ADDRESS_HEADER_NAMES = new HashSet<String>();

	static {

		// Section 4.1 The baseline SIP specification defines the following set of header
		// fields that conform to this grammar: From, To, Contact, Route,
		// Record-Route, Reply-To, Alert-Info, Call-Info, and Error-Info
		// The SipServletMessage interface defines a set of methods which operate 
		// on any address header field (see section 5.4.1 Parameterable and Address Header Fields ). 
		// This includes the RFC 3261 defined header fields listed above as well as extension headers 
		// such as Refer-To [refer] and P-Asserted-Identity [privacy]. 

		ADDRESS_HEADER_NAMES.add(FromHeader.NAME);
		ADDRESS_HEADER_NAMES.add(ToHeader.NAME);
		ADDRESS_HEADER_NAMES.add(ContactHeader.NAME);
		ADDRESS_HEADER_NAMES.add(RouteHeader.NAME);
		ADDRESS_HEADER_NAMES.add(RecordRouteHeader.NAME);
		ADDRESS_HEADER_NAMES.add(ReplyToHeader.NAME);
		ADDRESS_HEADER_NAMES.add(AlertInfoHeader.NAME);
		ADDRESS_HEADER_NAMES.add(CallInfoHeader.NAME);
		ADDRESS_HEADER_NAMES.add(ErrorInfoHeader.NAME);
		ADDRESS_HEADER_NAMES.add(ReferToHeader.NAME);
		ADDRESS_HEADER_NAMES.add(PAssertedIdentityHeader.NAME);
			
	}

	public static final Set<String> PARAMETERABLE_HEADER_NAMES = new HashSet<String>();

	static {

		// All of the Address header fields are Parameterable, including Contact, From, To, Route, Record-Route, and Reply-To. 
		// In addition, the header fields Accept, Accept-Encoding, Alert-Info, 
		// Call-Info, Content-Disposition, Content-Type, Error-Info, Retry-After and Via are also Parameterable. 
		PARAMETERABLE_HEADER_NAMES.add(FromHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ToHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ContactHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(RouteHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(RecordRouteHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ReplyToHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(AcceptHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(AcceptEncodingHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(AlertInfoHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(CallInfoHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ContentDispositionHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ContentTypeHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ErrorInfoHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(RetryAfterHeader.NAME);
		PARAMETERABLE_HEADER_NAMES.add(ViaHeader.NAME);
		
	}

	
	public static final Map<String, String> HEADER_COMPACT_2_FULL_NAMES_MAPPINGS = new HashMap<String, String>();

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

		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("i", CallIdHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("f", FromHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("t", ToHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("v", ViaHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("m", ContactHeader.NAME);
		// headerCompact2FullNamesMappings.put("a",); // Where is this header?
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("u", AllowEventsHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("e", ContentEncodingHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("l", ContentLengthHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("c", ContentTypeHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("o", EventHeader.NAME);
		// headerCompact2FullNamesMappings.put("y", IdentityHeader); // Where is
		// sthis header?
		// headerCompact2FullNamesMappings.put("n",IdentityInfoHeader );
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("r", ReferToHeader.NAME);
		 HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("b", ReferredByHeader.NAME);
		// headerCompact2FullNamesMappings.put("j", RejectContactHeader);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("d", ContentDispositionHeader.NAME);
		 HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("x", SessionExpiresHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("s", SubjectHeader.NAME);
		HEADER_COMPACT_2_FULL_NAMES_MAPPINGS.put("k", SupportedHeader.NAME);
	}

	public static final Map<String, String> HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS = new HashMap<String, String>();

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

		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(CallIdHeader.NAME, "i");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(FromHeader.NAME, "f");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ToHeader.NAME, "t");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ViaHeader.NAME, "v");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ContactHeader.NAME, "m");
		// headerFull2CompactNamesMappings.put(,"a"); // Where is this header?
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(AllowEventsHeader.NAME, "u");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ContentEncodingHeader.NAME, "e");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ContentLengthHeader.NAME, "l");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ContentTypeHeader.NAME, "c");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(EventHeader.NAME, "o");
		// headerCompact2FullNamesMappings.put(IdentityHeader,"y"); // Where is
		// sthis header?
		// headerCompact2FullNamesMappings.put(IdentityInfoHeader ,"n");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ReferToHeader.NAME, "r");
		// headerCompact2FullNamesMappings.put(ReferedByHeader,"b");
		// headerCompact2FullNamesMappings.put(RejectContactHeader,"j");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(ContentDispositionHeader.NAME, "d");
		// headerCompact2FullNamesMappings.put(SessionExpiresHeader,"x");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(SubjectHeader.NAME, "s");
		HEADER_FULL_TO_COMPACT_NAMES_MAPPINGS.put(SupportedHeader.NAME, "k");
	}
	
	public static final Set<String> IANA_ALLOWED_CONTENT_TYPES = new HashSet<String>();	

	static {

		// All of the Address header fields are Parameterable, including Contact, From, To, Route, Record-Route, and Reply-To. 
		// In addition, the header fields Accept, Accept-Encoding, Alert-Info, 
		// Call-Info, Content-Disposition, Content-Type, Error-Info, Retry-After and Via are also Parameterable. 
		IANA_ALLOWED_CONTENT_TYPES.add("application");
		IANA_ALLOWED_CONTENT_TYPES.add("audio");
		IANA_ALLOWED_CONTENT_TYPES.add("example");
		IANA_ALLOWED_CONTENT_TYPES.add("image");
		IANA_ALLOWED_CONTENT_TYPES.add("message");
		IANA_ALLOWED_CONTENT_TYPES.add("model");
		IANA_ALLOWED_CONTENT_TYPES.add("multipart");
		IANA_ALLOWED_CONTENT_TYPES.add("text");
		IANA_ALLOWED_CONTENT_TYPES.add("video");
			
	}

	// we don't have any other choice as to maintain a static list of multi value headers
	// because checking for , for the values as a delimiter won't work for WWW-Authenticate header which is not a multivalue header
	// but contains multiple , 
	public static final Set<String> SINGLETON_HEADER_NAMES = new HashSet<String>();
	static {
		SINGLETON_HEADER_NAMES.add(FromHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ToHeader.NAME);
		SINGLETON_HEADER_NAMES.add(CSeqHeader.NAME);
		SINGLETON_HEADER_NAMES.add(CallIdHeader.NAME);
		SINGLETON_HEADER_NAMES.add(MaxForwardsHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ContentLengthHeader.NAME);		
		SINGLETON_HEADER_NAMES.add(ContentDispositionHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ContentTypeHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ContentLengthHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ContentTypeHeader.NAME);
		SINGLETON_HEADER_NAMES.add(DateHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ContentTypeHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ExpiresHeader.NAME);
		SINGLETON_HEADER_NAMES.add(MinExpiresHeader.NAME);
		SINGLETON_HEADER_NAMES.add(MimeVersionHeader.NAME);
		SINGLETON_HEADER_NAMES.add(MinExpiresHeader.NAME);
		SINGLETON_HEADER_NAMES.add(OrganizationHeader.NAME);
		SINGLETON_HEADER_NAMES.add(PriorityHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ReplyToHeader.NAME);
		SINGLETON_HEADER_NAMES.add(RetryAfterHeader.NAME);
		SINGLETON_HEADER_NAMES.add(PriorityHeader.NAME);
		SINGLETON_HEADER_NAMES.add(ServerHeader.NAME);
		SINGLETON_HEADER_NAMES.add(SubjectHeader.NAME);
		SINGLETON_HEADER_NAMES.add(TimeStampHeader.NAME);
		SINGLETON_HEADER_NAMES.add(UserAgentHeader.NAME);
		SINGLETON_HEADER_NAMES.add(WWWAuthenticateHeader.NAME);
		// Fix by andrew miller 
		// Issue 1547 : Can't add a Proxy-Authorization using SipServletMessage.addHeader
		// those headers are singleton headers not list headers
		SINGLETON_HEADER_NAMES.add(ProxyAuthenticateHeader.NAME);		
		SINGLETON_HEADER_NAMES.add(ProxyAuthorizationHeader.NAME);
		// Issue 2798 : Can't add a Authorization using SipServletMessage.addHeader
		// those headers are singleton headers not list headers
		SINGLETON_HEADER_NAMES.add(AuthorizationHeader.NAME);
		// Same thing for Issue 2578 
		SINGLETON_HEADER_NAMES.add(AuthenticationInfoHeader.NAME);
	}	
	
	// we don't have any other choice as to maintain a static list of multi value headers
	// because checking for , for the values as a delimiter won't work for WWW-Authenticate header which is not a multivalue header
	// but contains multiple , 
	public static final Set<String> LIST_HEADER_NAMES = new HashSet<String>();
	static {
		LIST_HEADER_NAMES.add(AcceptEncodingHeader.NAME);
		LIST_HEADER_NAMES.add(AcceptLanguageHeader.NAME);
		LIST_HEADER_NAMES.add(AcceptHeader.NAME);
		LIST_HEADER_NAMES.add(AlertInfoHeader.NAME);
		LIST_HEADER_NAMES.add(AllowEventsHeader.NAME);
		LIST_HEADER_NAMES.add(AllowHeader.NAME);				
		LIST_HEADER_NAMES.add(CallInfoHeader.NAME);
		LIST_HEADER_NAMES.add(ContactHeader.NAME);
		LIST_HEADER_NAMES.add(ContentEncodingHeader.NAME);
		LIST_HEADER_NAMES.add(ContentLanguageHeader.NAME);
		LIST_HEADER_NAMES.add(ErrorInfoHeader.NAME);
		LIST_HEADER_NAMES.add(InReplyToHeader.NAME);
		LIST_HEADER_NAMES.add(ProxyRequireHeader.NAME);
		LIST_HEADER_NAMES.add(ReasonHeader.NAME);
		LIST_HEADER_NAMES.add(RecordRouteHeader.NAME);
		LIST_HEADER_NAMES.add(RequireHeader.NAME);
		LIST_HEADER_NAMES.add(RouteHeader.NAME);
		LIST_HEADER_NAMES.add(SupportedHeader.NAME);
		LIST_HEADER_NAMES.add(UnsupportedHeader.NAME);
		LIST_HEADER_NAMES.add(ViaHeader.NAME);
		LIST_HEADER_NAMES.add(WarningHeader.NAME);
		LIST_HEADER_NAMES.add(PAssertedIdentityHeader.NAME);
		LIST_HEADER_NAMES.add(PAssociatedURIHeader.NAME);
		LIST_HEADER_NAMES.add(PathHeader.NAME);
		LIST_HEADER_NAMES.add(PMediaAuthorizationHeader.NAME);
		LIST_HEADER_NAMES.add(PrivacyHeader.NAME);
		LIST_HEADER_NAMES.add(PVisitedNetworkIDHeader.NAME);
		LIST_HEADER_NAMES.add(SecurityClientHeader.NAME);
		LIST_HEADER_NAMES.add(SecurityServerHeader.NAME);
		LIST_HEADER_NAMES.add(SecurityVerifyHeader.NAME);
		LIST_HEADER_NAMES.add(ServiceRouteHeader.NAME);
	}	

	private static final String[] ALLOWED_ADDRESS_SCHEMES = {"sip","sips","tel","tels"};
	
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
			SipNetworkInterfaceManager sipNetworkInterfaceManager, Request request, String branch, String outboundInterface) {		
		MobicentsExtendedListeningPoint listeningPoint = null;
		if(outboundInterface == null) {
			String transport = findTransport(request);
			listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		} else {
			javax.sip.address.SipURI outboundInterfaceURI = null;			
			try {
				outboundInterfaceURI = (javax.sip.address.SipURI) SipFactoryImpl.addressFactory.createURI(outboundInterface);
			} catch (ParseException e) {
				throw new IllegalArgumentException("couldn't parse the outbound interface " + outboundInterface, e);
			}
			listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(outboundInterfaceURI, false);
		}
		boolean usePublicAddress = findUsePublicAddress(
				sipNetworkInterfaceManager, request, listeningPoint);
		return listeningPoint.createViaHeader(branch, usePublicAddress);		
    }
	
	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @param branch
	 * @return
	 */
	public static String createBranch(String appSessionId, String appname) {
		return createBranch(appSessionId, appname, Long.toString(System.nanoTime()));
    }
	
	public static String createBranch(String appSessionId, String appname, String random) {
		return MessageDispatcher.BRANCH_MAGIC_COOKIE + appSessionId + "_" + appname + "_" + random;		
    }
	 
	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @return
	 */
	public static ContactHeader createContactHeader(SipNetworkInterfaceManager sipNetworkInterfaceManager, Request request, String displayName, String userName, String outboundInterface) {		
		MobicentsExtendedListeningPoint listeningPoint = null;
		if(outboundInterface == null) {
			String transport = findTransport(request);
			listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		} else {
			javax.sip.address.SipURI outboundInterfaceURI = null;			
			try {
				outboundInterfaceURI = (javax.sip.address.SipURI) SipFactoryImpl.addressFactory.createURI(outboundInterface);
			} catch (ParseException e) {
				throw new IllegalArgumentException("couldn't parse the outbound interface " + outboundInterface, e);
			}			
			listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(outboundInterfaceURI, false);
		}
		boolean usePublicAddress = findUsePublicAddress(
				sipNetworkInterfaceManager, request, listeningPoint);
		ContactHeader ch = listeningPoint.createContactHeader(displayName, userName, usePublicAddress);
		if(StaticServiceHolder.sipStandardService.isMd5ContactUserPart()) {
			CallIdHeader callId = (CallIdHeader)request.getHeader(CallIdHeader.NAME);
			String username = getHash(callId.getCallId().getBytes());
			SipURI uri = (SipURI)ch.getAddress().getURI();
			try {
				uri.setUser(username);
				ch.getAddress().setDisplayName(null);
			} catch (ParseException e) {
				throw new IllegalStateException("Can't create contact header user part with MD5", e);
			}
		}
		
		return ch;
	}
	
	private static ThreadLocal<MessageDigest> localDigest = new ThreadLocal<MessageDigest>();
	private static String getHash(byte[] b) {
		MessageDigest md = localDigest.get();
		if(md == null) {
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			localDigest.set(md);
		}
		md.reset();
		md.update(b);
		
	    byte[] hash = md.digest();
	    StringBuilder sb = new StringBuilder();
	    for(int i=0; i<hash.length; i++) {
	      sb.append(Integer.toHexString( (hash[i]>> 4) & 0x0F ) );
	      sb.append(Integer.toHexString( hash[i] & 0x0F ) );
	    }
	    String rv = sb.toString();
	    return rv;
	}

	/**
	 * 
	 * @param sipProviders
	 * @param transport
	 * @return
	 */
	public static javax.sip.address.SipURI createRecordRouteURI(SipNetworkInterfaceManager sipNetworkInterfaceManager, Message message) {
		String transport = findTransport(message);		
		return createRecordRouteURI(sipNetworkInterfaceManager, message, transport);
	}
	
	public static javax.sip.address.SipURI createRecordRouteURI(SipNetworkInterfaceManager sipNetworkInterfaceManager, Message message, String transport) {	
		MobicentsExtendedListeningPoint listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
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
			Message message, MobicentsExtendedListeningPoint listeningPoint) {
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
			
			Request request = (Request)message;

			RouteHeader route = (RouteHeader) request.getHeader(RouteHeader.NAME);
			if(route != null) {
				transport = ListeningPoint.UDP;
				URI uri = route.getAddress().getURI();
				if(uri instanceof SipURI) {
					SipURI sipURI = (SipURI) uri;
					if(sipURI.isSecure()) {
						transport = ListeningPoint.TLS;
					} else {
						String transportParam = sipURI.getTransportParam();

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
			}
		}

		if(transport == null && message instanceof Request) {
			transport = ListeningPoint.UDP;
			Request request = (Request)message;
			URI ruri = request.getRequestURI();
			if(ruri instanceof SipURI) {
				SipURI sruri = ((javax.sip.address.SipURI) ruri);
				if(sruri.isSecure()) {
					transport = ListeningPoint.TLS;
				} else {
					String transportParam = sruri.getTransportParam();

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
		}
		// storing the transport is present in the message application data for maximizing perf 
		// in speeding up the retrieval later on
		((SIPMessage)message).setApplicationData(transport);
		return transport;
	}
	
	public static boolean checkScheme(String address) {
		String tmpAddress = address;
		for(String scheme:ALLOWED_ADDRESS_SCHEMES) {
			int start = tmpAddress.indexOf("<");
			if(start >= 0) {
				int end = tmpAddress.indexOf(">");
				tmpAddress = tmpAddress.substring(start + 1, end);
			}
				
			if(scheme.equalsIgnoreCase(tmpAddress.substring(0, scheme.length())))
				return true;
		}
		return false;
	}

	public static void terminateTransaction(Transaction transaction) {
		// Issue 2130 (http://code.google.com/p/mobicents/issues/detail?id=2130) : Memory leak in Sip stack when INFO message is used 
		// fail before the ctx is created to avoid mem leaks
		if(transaction != null && transaction instanceof SIPClientTransaction) {
			if(logger.isDebugEnabled()) {
				logger.debug("terminating transaction " + transaction + " with transaction id "+ transaction.getBranchId());
			}
			try {
				transaction.terminate();
			} catch (ObjectInUseException e) {
				logger.error("Couldn't terminate the transaction " + transaction + " with transaction id "+ transaction.getBranchId());
			}
		}
	}
	
	public static void setTransactionTimers(TransactionExt transaction, SipApplicationDispatcher sipApplicationDispatcher) {
		transaction.setRetransmitTimer(sipApplicationDispatcher.getBaseTimerInterval());
	    ((TransactionExt)transaction).setTimerT2(sipApplicationDispatcher.getT2Interval());
	    ((TransactionExt)transaction).setTimerT4(sipApplicationDispatcher.getT4Interval());
	    ((TransactionExt)transaction).setTimerD(sipApplicationDispatcher.getTimerDInterval());
	}
	
	public static void optimizeRouteHeaderAddressForInternalRoutingrequest(SipConnector sipConnector, Request request, MobicentsSipSession session,  SipFactoryImpl sipFactoryImpl, String transport) {
		RouteHeader rh = (RouteHeader) request.getHeader(RouteHeader.NAME);
		javax.sip.address.URI uri = null;
		if(rh != null) {
			uri = rh.getAddress().getURI();
		} else {
			uri = request.getRequestURI();
		}
		if(uri.isSipURI()) {
			javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) uri;
			optimizeUriForInternalRoutingRequest(sipConnector, sipUri, session, sipFactoryImpl, transport);
		}

	}

	public static void optimizeUriForInternalRoutingRequest(SipConnector sipConnector, javax.sip.address.SipURI sipUri, MobicentsSipSession session,  SipFactoryImpl sipFactoryImpl, String transport) {
		SipNetworkInterfaceManager sipNetworkInterfaceManager = sipFactoryImpl.getSipNetworkInterfaceManager();

		try {
			boolean isExternal = sipFactoryImpl.getSipApplicationDispatcher().isExternal(sipUri.getHost(), sipUri.getPort(), transport);
			if(!isExternal) {
				if(logger.isDebugEnabled()) {
					logger.debug("The request is going internally due to sipUri = " + sipUri);
				}
				// http://code.google.com/p/sipservlets/issues/detail?id=92
				if (!sipConnector
						.isReplaceStaticServerAddressForInternalRoutingRequest()) {
					// config that turns off optimization if uri is the static
					// server address, this allows the static server address to
					// be a hop in the request outgoing path
					if (sipConnector.getStaticServerAddress() != null
							&& (sipConnector.getStaticServerAddress().equals(
									sipUri.getHost()) || sipConnector
									.getStaticServerAddress().equals(
											sipUri.getHost() + ":"
													+ sipUri.getPort()))) {
						if(logger.isDebugEnabled()) {
							logger.debug("Avoiding URI optimization due to connector configuration and URI points to the static server address.");
						}
						return;
					}
				}	
				MobicentsExtendedListeningPoint lp = null;
				if(session.getOutboundInterface() != null) {
					javax.sip.address.SipURI outboundInterfaceURI = (javax.sip.address.SipURI) SipFactoryImpl.addressFactory.createURI(session.getOutboundInterface());
					lp = sipNetworkInterfaceManager.findMatchingListeningPoint(outboundInterfaceURI, false);
				} else {
					lp = sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
				}
				sipUri.setHost(lp.getHost(false));
				sipUri.setPort(lp.getPort());
				sipUri.setTransportParam(lp.getTransport());
			}
		} catch (ParseException e) {
			logger.error("AR optimization error", e);
		}
	}

	public static void optimizeViaHeaderAddressForStaticAddress(SipConnector sipConnector, Request request, SipFactoryImpl sipFactoryImpl, String transport) throws ParseException, InvalidArgumentException {
		javax.sip.address.URI uri = request.getRequestURI();

		ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);

		RouteHeader route = (RouteHeader) request.getHeader(RouteHeader.NAME);
		if(route != null) {
			uri = route.getAddress().getURI();
		}
		if(uri.isSipURI()) {
			javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) uri;
			String host = sipUri.getHost();
			int port = sipUri.getPort();
			if(sipFactoryImpl.getSipApplicationDispatcher().isExternal(host, port, transport)) {
				viaHeader.setHost(sipConnector.getStaticServerAddress());
				viaHeader.setPort(sipConnector.getStaticServerPort());
			}
		}
	}
}
