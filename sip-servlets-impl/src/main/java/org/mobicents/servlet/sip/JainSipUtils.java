package org.mobicents.servlet.sip;


import java.text.ParseException;
import java.util.Iterator;
import java.util.Set;

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

/**
 * 
 * Various helpful utilities to map jain sip abstractions.
 * 
 * @author mranga
 */
public class JainSipUtils {
	
	private static Log logger = LogFactory.getLog(JainSipUtils.class);

	/**
	 * @deprecated
	 */
	public static ViaHeader createViaHeader(SipProvider sipProvider, String transport) {
		ListeningPoint lp = sipProvider.getListeningPoint(transport);
	 	String host = lp.getIPAddress();
	 	int port = lp.getPort();
	 		 	
        try {
            ViaHeader via = SipFactories.headerFactory.createViaHeader(host, port, transport, null);
          
            return via;
        } catch (Exception ex) {
        	logger.fatal ("Unexpected error",ex);
            throw new RuntimeException("Unexpected exception when creating via header ", ex);
        }
    }
	/**
	 * @deprecated
	 */ 
	public static ContactHeader createContactForProvider(SipProvider provider, String transport) {
		try {
			String ipAddress = provider.getListeningPoint(transport).getIPAddress();
			int port = provider.getListeningPoint(transport).getPort();
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
	/**
	 * @deprecated
	 */
	public static javax.sip.address.SipURI createRecordRouteURI(SipProvider provider) {
		ListeningPoint listeningPoint = provider.getListeningPoint(ListeningPoint.UDP);
		if(listeningPoint == null){
			throw new RuntimeException("no connector have been found for transport "+ ListeningPoint.UDP);
		}
		try {			
			SipURI sipUri = SipFactories.addressFactory.createSipURI(
					null, listeningPoint.getIPAddress());
			sipUri.setPort(listeningPoint.getPort());
			sipUri.setTransportParam(listeningPoint.getTransport());
			// Do we want to add an ID here?
			return sipUri;
		} catch ( Exception ex) {
			logger.fatal("Unexpected error", ex);
			throw new RuntimeException("Container error", ex);
		}
	
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
            ViaHeader via = SipFactories.headerFactory.createViaHeader(host, port, transport, null);
          
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
		//FIXME defaulting to udp but an exceptions should be thrown instead 
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
