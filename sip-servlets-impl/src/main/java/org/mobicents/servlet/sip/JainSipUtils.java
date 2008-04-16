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


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.message.SipFactoryImpl.NamesComparator;

/**
 * 
 * Various helpful utilities to map jain sip abstractions. 
 * 
 * @author mranga
 * @author Jean Deruelle
 */
public class JainSipUtils {
	//TODO since listening points don't change often, use a cache system
	//or hashmap with correct keys to improve performance
	
	private static Log logger = LogFactory.getLog(JainSipUtils.class);

	public static String GLOBAL_IPADDRESS = "0.0.0.0";
	
	public static final TreeSet<String> dialogCreatingMethods = new TreeSet<String>(
			new NamesComparator());
	
	public static final TreeSet<String> dialogTerminatingMethods = new TreeSet<String>(
			new NamesComparator());

	static {		
		dialogCreatingMethods.add(Request.INVITE);
		dialogCreatingMethods.add(Request.SUBSCRIBE);
		dialogTerminatingMethods.add(Request.CANCEL);
		dialogTerminatingMethods.add(Request.BYE);
	}

	
	public static ViaHeader createViaHeader(Set<SipProvider> sipProviders, String transport, String branch) {
		Iterator<SipProvider> it = sipProviders.iterator();
		ListeningPoint listeningPoint = null;
		while (it.hasNext() && listeningPoint == null) {
			SipProvider sipProvider = it.next();
			listeningPoint = sipProvider.getListeningPoint(transport);
		}		
		if(listeningPoint == null) {
			throw new RuntimeException("no connector have been found for transport "+ transport);
		}
	 	String host = listeningPoint.getIPAddress();
	 	int port = listeningPoint.getPort();
	 		 	
        try {
            ViaHeader via = SipFactories.headerFactory.createViaHeader(host, port, transport, branch);
          
            return via;
        } catch (Exception ex) {
        	logger.fatal ("Unexpected error",ex);
            throw new RuntimeException("Unexpected exception when creating via header ", ex);
        }
    }
	 
	public static ContactHeader createContactForProvider(Set<SipProvider> sipProviders, String transport) {
		Iterator<SipProvider> it = sipProviders.iterator();
		ListeningPoint listeningPoint = null;
		while (it.hasNext() && listeningPoint == null) {
			SipProvider sipProvider = it.next();
			listeningPoint = sipProvider.getListeningPoint(transport);
		}		
		if(listeningPoint == null) {
			throw new RuntimeException("no connector have been found for transport "+ transport);
		}
		
		String ipAddress = listeningPoint.getIPAddress();
		int port = listeningPoint.getPort();
		try {
			javax.sip.address.SipURI sipURI = SipFactories.addressFactory.createSipURI(null, ipAddress);
			sipURI.setHost(ipAddress);
			sipURI.setPort(port);
			sipURI.setTransportParam(transport);
			ContactHeader contact = SipFactories.headerFactory.createContactHeader(SipFactories.addressFactory.createAddress(sipURI));
		
			return contact;
		} catch (Exception ex) {
			logger.fatal ("Unexpected error",ex);
			throw new RuntimeException ("Unexpected error",ex);
		}
	}

	public static javax.sip.address.SipURI createRecordRouteURI(Set<SipProvider> sipProviders, String transport) {
		//FIXME defaulting to udp but an exception should be thrown instead 
		if(transport == null) {
			transport = ListeningPoint.UDP;
		}
		Iterator<SipProvider> it = sipProviders.iterator();
		ListeningPoint listeningPoint = null;
		while (it.hasNext() && listeningPoint == null) {
			SipProvider sipProvider = it.next();
			listeningPoint = sipProvider.getListeningPoint(transport);
		}		
		if(listeningPoint == null) {
			throw new RuntimeException("no connector have been found for transport "+ transport);
		}
		try {
			SipURI sipUri = SipFactories.addressFactory.createSipURI(null, listeningPoint.getIPAddress());
			sipUri.setPort(listeningPoint.getPort());
			sipUri.setTransportParam(listeningPoint.getTransport());
			// Do we want to add an ID here?
			return sipUri;
		} catch ( Exception ex) {
			logger.fatal("Unexpected error", ex);
			throw new RuntimeException("Container error", ex);
		}	
	}
	
	/**
	 * Retrieve the first matching sip Provider from the set of sip provider corresponding to the transport.
	 * @param sipProviders the set of sip providers to look into
	 * @param transport the transport
	 * @return Retrieve the first matching sip Provider from the set of sip provider corresponding to the transport.
	 * If none has been found, null is returned.
	 */
	public static SipProvider findMatchingSipProvider(Set<SipProvider> sipProviders, String transport) {		
		Iterator<SipProvider> it = sipProviders.iterator();		 
		while (it.hasNext()) {
			SipProvider sipProvider = it.next();
			ListeningPoint listeningPoint = sipProvider.getListeningPoint(transport);
			if(listeningPoint != null) {
				return sipProvider;
			}
		}					
		return null; 
	}
	/**
	 * Retrieve the first matching sip Provider from the set of sip provider corresponding to the 
	 * ipAddress port and transport given in parameter.
	 * @param sipProviders the set of sip providers to look into
	 * @param ipAddress the ip address
	 * @param port the port
	 * @param transport the transport
	 * @return Retrieve the first matching sip Provider from the set of sip provider corresponding to the transport.
	 * If none has been found, null is returned.
	 */
	public static ListeningPoint findMatchingListeningPoint(
			Set<SipProvider> sipProviders, String ipAddress, int port, String transport) {		
		Iterator<SipProvider> it = sipProviders.iterator();		 
		while (it.hasNext()) {
			SipProvider sipProvider = it.next();
			ListeningPoint listeningPoint = sipProvider.getListeningPoint(transport);
			
			if(isListeningPointMatching(ipAddress, port, listeningPoint)) {
				return listeningPoint;
			}
		}					
		return null; 
	}

	/**
	 * Check if the ipAddress and port is matching the listening point
	 * @param ipAddress ipAddress to check the listening point against
	 * @param port port to check the listening point against
	 * @param listeningPoint the listening point to check if it matches the ipaddress and port 
	 * @return true if the ipAddress and port is matching the listening point
	 */
	public static boolean isListeningPointMatching(String ipAddress, int port,
			ListeningPoint listeningPoint) {
		int portChecked = checkPortRange(port);
		List<String> listeningPointAddresses = new ArrayList<String>();
		listeningPointAddresses.add(listeningPoint.getIPAddress());
		//if the listening point is bound to 0.0.0.0 then adding all corresponding IP address
		if(GLOBAL_IPADDRESS.equals(listeningPoint.getIPAddress())) {
			try {
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				while(networkInterfaces.hasMoreElements()) {
					NetworkInterface networkInterface = networkInterfaces.nextElement();
					Enumeration<InetAddress> bindings = networkInterface.getInetAddresses();
					while(bindings.hasMoreElements()) {
						InetAddress addr = bindings.nextElement();
						String networkInterfaceIpAddress = addr.getHostAddress();
						listeningPointAddresses.add(networkInterfaceIpAddress);
					}
				}
			} catch (SocketException e) {
				logger.warn("Unable to enumerate local interfaces. Binding to 0.0.0.0 may not work.",e);
			}				
		}
		//resolving by ipaddress an port
		if(listeningPointAddresses.contains(ipAddress) &&					
				listeningPoint.getPort() == portChecked) {
			return true;
		}
		//resolving by hostname
		try {
			InetAddress[] inetAddresses = InetAddress.getAllByName(ipAddress);				
			for (int i = 0; i < inetAddresses.length; i++) {
				if(listeningPointAddresses.contains(inetAddresses[i].getHostAddress())
						&& listeningPoint.getPort() == portChecked) {
					return true;
				}
			}
		} catch (UnknownHostException e) {
			//not important it can mean that the ipAddress provided is not a hostname
			// but an ip address not found in the searched listening points above				
		}
		return false;
	}		

	/**
	 * Retrieve the first matching sip Provider from the set of sip provider corresponding to the 
	 * ipAddress and port given in parameter.
	 * @param sipProviders the set of sip providers to look into
	 * @param ipAddress the ip address
	 * @param port the port
	 * @return Retrieve the first matching sip Provider from the set of sip provider corresponding to the transport.
	 * If none has been found, null is returned.
	 */
	public static ListeningPoint findMatchingListeningPoint(
			Set<SipProvider> sipProviders, String ipAddress, int port) {		
		Iterator<SipProvider> it = sipProviders.iterator();		 
		while (it.hasNext()) {
			SipProvider sipProvider = it.next();
			ListeningPoint[] listeningPoints = sipProvider.getListeningPoints();
			for (ListeningPoint listeningPoint : listeningPoints) {
				if(isListeningPointMatching(ipAddress, port, listeningPoint)) {
					return listeningPoint;
				}
			}			
		}					
		return null; 
	}
	
	/**
	 * Checks if the port is in the UDP-TCP port numbers (0-65355) range 
	 * otherwise defaulting to 5060
	 * @param port port to check
	 * @return the smae port number if in range, 5060 otherwise
	 */
	public static int checkPortRange(int port) {
		if(port < 0 && port > 65355) {		
			return 5060;
		}
		else {
			return port;
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String findTransport(Request request) {
		String transport = ListeningPoint.UDP;

		String transportParam = ((javax.sip.address.SipURI) request
				.getRequestURI()).getTransportParam();

		if (transportParam != null
				&& transportParam.equalsIgnoreCase(ListeningPoint.TLS)) {
			transport = ListeningPoint.TLS;
		} else if (request.getContentLength().getContentLength() > 4096) {
			transport = ListeningPoint.TCP;
		}
		return transport;
	}
	/**
	 * 
	 * @param serverInternalError
	 * @param transaction
	 * @param request
	 * @param sipProvider
	 * @throws ParseException
	 * @throws SipException
	 * @throws InvalidArgumentException
	 */
	public static void sendErrorResponse(int serverInternalError,
			ServerTransaction transaction, Request request,
			SipProvider sipProvider) {
		try{
			Response response=SipFactories.messageFactory.createResponse
	        	(Response.SERVER_INTERNAL_ERROR,request);			
	        if (transaction!=null) {
	        	transaction.sendResponse(response);
	        } else { 
	        	sipProvider.sendResponse(response);
	        }
		} catch (Exception e) {
			logger.error("Problem while sending the error response to the following request "
					+ request.toString(), e);
		}
	}
}
