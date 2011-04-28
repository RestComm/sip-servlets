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

package org.mobicents.servlet.sip.core;

import gov.nist.core.net.AddressResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.ListeningPoint;
import javax.sip.address.Hop;

import org.apache.log4j.Logger;
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
@Deprecated
public class DNSAddressResolver implements AddressResolver {
	private static final Logger logger = Logger.getLogger(DNSAddressResolver.class);
	//the sip factory implementation to be able
	SipApplicationDispatcher sipApplicationDispatcher;
	
	static ConcurrentHashMap<String, Map<String, String>> cachedLookup = new ConcurrentHashMap<String, Map<String, String>>();
	
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
	public static Hop resolveHostByDnsSrvLookup(Hop hop) {
		String host = hop.getHost();
		String transport = hop.getTransport();
		if(transport==null) {
			transport = ListeningPoint.UDP;
		}
		transport = transport.toLowerCase();

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
			Map<String, String> cachedEntry = foundCachedEntry(host, transport, (Record[]) records);
			if(cachedEntry == null) {
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
					Map<String, String> entry = new HashMap<String, String>();
					entry.put("hostName", resolvedName);
					entry.put("hostAddress", hostAddress);
					entry.put("hostPort", ""+recordPort);
					cachedLookup.putIfAbsent(host + transport, entry);
					return new HopImpl(hostAddress, recordPort, transport);
				} catch (UnknownHostException e) {
					logger.error("Impossible to get the host address of the resolved name, " +
							"we are going to just use the domain name directly" + resolvedName, e);
					return hop;
				}
			} else {
				String entryResolvedName = cachedEntry.get("hostName");
				String hostAddress = cachedEntry.get("hostAddress");
				String hostPort = cachedEntry.get("hostPort");
				if(logger.isDebugEnabled()) {
					logger.debug("Reusing a previous DNS SRV lookup for host:transport " +
							""+ host + "/" + transport +
							" , Host Name = " + entryResolvedName +
							" , Host IP Address = " + hostAddress + 
							", Host Port = " + hostPort);
				}
				return new HopImpl(hostAddress, Integer.parseInt(hostPort), transport);
			}
		}
				
	}

	public static Map<String, String> foundCachedEntry(String host, String transport, Record[] records) {
		Map<String, String> entry = cachedLookup.get(host+transport);
		if(entry == null) {
			return null;
		}
		String entryResolvedName = entry.get("hostName");
		String hostAddress = entry.get("hostAddress");
		String hostPort = entry.get("hostPort");
		for (Record record : records) {
			if(record instanceof SRVRecord) {
				SRVRecord srvRecord = (SRVRecord) record;
				String resolvedName = srvRecord.getTarget().toString();
				String resolvedHostAddress;
				try {
					resolvedHostAddress = InetAddress.getByName(resolvedName).getHostAddress();
					int recordPort = srvRecord.getPort();
					if(entryResolvedName.equalsIgnoreCase(resolvedName) 
							&& hostAddress.equalsIgnoreCase(resolvedHostAddress)
							&& hostPort.equalsIgnoreCase("" + recordPort)) {
						return entry;
					}
				} catch (UnknownHostException e) {
					logger.warn("Couldn't resolve address " + resolvedName);
				}				
			}
			
		}
		return null;
	}
}
