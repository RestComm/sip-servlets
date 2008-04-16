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


import java.text.ParseException;
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
	//TODO since listening points don't change often, use a cache system
	//or hashmap with correct keys to improve performance
	
	/**
     * The maximum int value that could correspond to a port nubmer.
     */
    public static final int    MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port nubmer bindable
     * by the SIP Communicator.
     */
    public static final int    MIN_PORT_NUMBER = 1024;
    
	
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

	public static final int MAX_FORWARD_HEADER_VALUE = 70;
	
	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @param branch
	 * @return
	 */
	public static ViaHeader createViaHeader(
			SipNetworkInterfaceManager sipNetworkInterfaceManager, String transport, String branch) {
		
		ExtendedListeningPoint listeningPoint = 
			sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		// Making use of the global ip address discovered by STUN if it is present
		String host = null;
		int port = -1;
		String globalIpAddress = listeningPoint.getGlobalIpAddress();
		if(globalIpAddress != null) {
			host = globalIpAddress;
			port = listeningPoint.getGlobalPort();
		} else {
			host = listeningPoint.getIpAddresses().get(0);
			port = listeningPoint.getPort();
		}
	 		 	
        try {
            ViaHeader via = SipFactories.headerFactory.createViaHeader(host, port, transport, branch);
          
            return via;
        } catch (ParseException ex) {
        	logger.error ("Unexpected error while creating a via header",ex);
            throw new IllegalArgumentException("Unexpected exception when creating via header ", ex);
        } catch (InvalidArgumentException e) {
        	logger.error ("Unexpected error while creating a via header",e);
            throw new IllegalArgumentException("Unexpected exception when creating via header ", e);
		}
    }
	 
	/**
	 * 
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @return
	 */
	public static ContactHeader createContactHeader(SipNetworkInterfaceManager sipNetworkInterfaceManager, String transport, String displayName) {
		
		ExtendedListeningPoint listeningPoint = 
			sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);						
		
		// Making use of the global ip address discovered by STUN if it is present
		String host = null;
		int port = -1;
		String globalIpAddress = listeningPoint.getGlobalIpAddress();
		if(globalIpAddress != null) {
			host = globalIpAddress;
			port = listeningPoint.getGlobalPort();
		} else {
			host = listeningPoint.getIpAddresses().get(0);
			port = listeningPoint.getPort();
		}
		
		try {
			javax.sip.address.SipURI sipURI = SipFactories.addressFactory.createSipURI(null, host);
			sipURI.setHost(host);
			sipURI.setPort(port);			
			sipURI.setTransportParam(transport);
			javax.sip.address.Address contactAddress = SipFactories.addressFactory.createAddress(sipURI);
			if(displayName != null && displayName.length() > 0) {
				contactAddress.setDisplayName(displayName);
			}
			ContactHeader contact = SipFactories.headerFactory.createContactHeader(contactAddress);
		
			return contact;
		} catch (ParseException ex) {
        	logger.error ("Unexpected error while creating a sip URI",ex);
            throw new IllegalArgumentException("Unexpected exception when creating a sip URI", ex);
        }
	}

	/**
	 * 
	 * @param sipProviders
	 * @param transport
	 * @return
	 */
	public static javax.sip.address.SipURI createRecordRouteURI(SipNetworkInterfaceManager sipNetworkInterfaceManager, String transport) {		
		ExtendedListeningPoint listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);							
		try {
			SipURI sipUri = SipFactories.addressFactory.createSipURI(null, listeningPoint.getIpAddresses().get(0));
			sipUri.setPort(listeningPoint.getPort());
			sipUri.setTransportParam(listeningPoint.getTransport());
			// Do we want to add an ID here?
			return sipUri;
		} catch (ParseException ex) {
        	logger.error ("Unexpected error while creating a record route URI",ex);
            throw new IllegalArgumentException("Unexpected exception when creating a record route URI", ex);
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
