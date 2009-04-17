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


import gov.nist.javax.sip.message.SIPMessage;

import java.util.List;
import java.util.TreeSet;

import javax.sip.ListeningPoint;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
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
		if(listeningPoint.getGlobalIpAddress() != null) {
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
