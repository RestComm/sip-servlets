package org.mobicents.servlet.sip;


import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;

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

	 public static ViaHeader createViaHeader(SipProvider sipProvider, String transport) {
		 	ListeningPoint lp = sipProvider.getListeningPoint(transport);
		 	String host = lp.getIPAddress();
		 	int port = lp.getPort();
		 
		 	
	        try {
	            ViaHeader via = SipFactories.headerFactory.createViaHeader(host, port, transport, null);
	           
	            return via;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            throw new RuntimeException("Unexpected exception when creating via header ", ex);
	        }
	    }
	 
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

	public static javax.sip.address.SipURI createRecordRouteURI(SipProvider provider) {
		try {
			SipURI sipUri = SipFactories.addressFactory.createSipURI(null, provider.getListeningPoint("udp").getIPAddress());
			// Do we want to add an ID here?
			return sipUri;
		} catch ( Exception ex) {
			logger.fatal("Unexpected error", ex);
			throw new RuntimeException("Container error", ex);
		}
	
	}
}
