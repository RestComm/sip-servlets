package org.mobicents.servlet.sip.core;

import gov.nist.core.net.AddressResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sip.ListeningPoint;
import javax.sip.address.Hop;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.utils.Inet6Util;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * The Address resolver to resolve proxy domain to a hop to the outbound proxy server 
 * by doing SRV lookup of the host of the Hop as mandated by rfc3263. <br/>
 * 
 * some of the rfc3263 can hardly be implemented and NAPTR query can hardly be done 
 * since the stack populate port and transport automatically.
 * 
 * @author M. Ranganathan
 * @author J. Deruelle
 *
 */
public class DNSAddressResolver implements AddressResolver {
	private static Logger logger = Logger.getLogger(DNSAddressResolver.class);
	//the sip factory implementation to be able
	SipApplicationDispatcher sipApplicationDispatcher;
	
	/**
	 * @param sipApplicationDispatcherImpl
	 */
	public DNSAddressResolver(
			SipApplicationDispatcher sipApplicationDispatcher) {		
		this.sipApplicationDispatcher = sipApplicationDispatcher;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nist.core.net.AddressResolver#resolveAddress(javax.sip.address.Hop)
	 */
	public Hop resolveAddress(Hop hop) {
		String hopHost = hop.getHost();
		int hopPort = hop.getPort();
		String hopTransport = hop.getTransport();	
		
		if(logger.isDebugEnabled()) {
			logger.debug("Resolving " + hopHost + " transport " + hopTransport);
		}
		// As per rfc3263 Section 4.2 
		// If TARGET is a numeric IP address, the client uses that address.  If
		// the URI also contains a port, it uses that port.  If no port is
		// specified, it uses the default port for the particular transport
		// protocol.numeric IP address, no DNS lookup to be done
		if(Inet6Util.isValidIP6Address(hopHost) 
				|| Inet6Util.isValidIPV4Address(hopHost)) {
			if(logger.isDebugEnabled()) {
				logger.debug("host " + hopHost + " is a numeric IP address, " +
						"no DNS SRV lookup to be done, using the hop given in param");
			}
			return hop;
		} 
		
		// if the host belong to the container, it tries to resolve the ip address		
		if(sipApplicationDispatcher.findHostNames().contains(hopHost)) {
			try {
				InetAddress ipAddress = InetAddress.getByName(hopHost);
				return new HopImpl(ipAddress.getHostAddress(), hopPort, hopTransport);
			} catch (UnknownHostException e) {
				logger.warn(hopHost + " belonging to the container cannoit be resolved");
			}			
		}
				
		// As per rfc3263 Section 4.2
		// If the TARGET was not a numeric IP address, and no port was present
		// in the URI, the client performs an SRV query
		return resolveHostByDnsSrvLookup(hop);
		
	}
	
	/**
	 * Resolve the Host by doing a SRV lookup on it 
	 * @param host the host
	 * @param port the port 
	 * @param transport the transport
	 * @return
	 */
	protected Hop resolveHostByDnsSrvLookup(Hop hop) {
		String host = hop.getHost();
		String transport = hop.getTransport();

		Record[] records = null;
		try {
			records = new Lookup("_sip._" + transport
					+ "." + host, Type.SRV).run();
		} catch (TextParseException e) {
			logger.error("Impossible to parse the parameters for dns lookup", e);
		}

		if (records == null || records.length == 0) {
			// SRV lookup failed, use the outbound proxy directly.
			if(logger.isDebugEnabled()) {
				logger
						.debug("SRV lookup for host:transport " +
							""+ host + "/" + transport + " returned nothing " +
							"-- we are going to just use the domain name directly");
			}
			return hop;
		} else {			
			SRVRecord record = (SRVRecord) records[0];
			int recordPort = record.getPort();						
			String resolvedName = record.getTarget().toString();
			try {
				String hostAddress= InetAddress.getByName(resolvedName).getHostAddress();
				if(logger.isDebugEnabled()) {
					logger.debug("Did a successful DNS SRV lookup for host:transport " +
							""+ host + "/" + transport +
							" , Host Name = " + resolvedName +
							" , Host IP Address = " + hostAddress + 
							", Host Port = " + recordPort);
				}
				return new HopImpl(hostAddress, recordPort, transport);
			} catch (UnknownHostException e) {
				logger.error("Impossible to get the host address of the resolved name, " +
						"we are going to just use the domain name directly" + resolvedName, e);
				return hop;
			}
		}
				
	}
}
