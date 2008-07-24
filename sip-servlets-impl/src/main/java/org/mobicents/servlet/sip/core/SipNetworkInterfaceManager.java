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
package org.mobicents.servlet.sip.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.sip.SipURI;
import javax.sip.ListeningPoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.utils.Inet6Util;

/**
 * This class will be a placeholder for all sip network interfaces mapped to a sip
 * standard service. It should allow one to query for local and external address 
 * (discovered by STUN) of a specific interface. <br/>
 * 
 * It will also allow various queries against its network interfaces to discover
 * the right one to use. Those queries could be cached in order to improve performance 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class SipNetworkInterfaceManager {
	private static Log logger = LogFactory.getLog(SipNetworkInterfaceManager.class);
	
	/**
     * The maximum int value that could correspond to a port nubmer.
     */
    public static final int MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port nubmer bindable
     * by the SIP Communicator.
     */
    public static final int MIN_PORT_NUMBER = 1024;    
    
	List<ExtendedListeningPoint> extendedListeningPointList = null;
	List<SipURI> outboundInterfaces = null;
	
	//those maps are present to improve the performance of finding a listening point either from a transport
	// or from a triplet ipaddress, port and transport
	Map<String, List<ExtendedListeningPoint>> transportMappingCacheMap = null;	
	Map<String, ExtendedListeningPoint> extendedListeningPointsCacheMap = null;
	
	/**
	 * Default Constructor
	 */
	public SipNetworkInterfaceManager() {
		extendedListeningPointList = new CopyOnWriteArrayList<ExtendedListeningPoint>();
		outboundInterfaces = new CopyOnWriteArrayList<SipURI>();
		
		// creating and populating the transport cache map with transports
		transportMappingCacheMap = new HashMap<String, List<ExtendedListeningPoint>>();
		transportMappingCacheMap.put(ListeningPoint.TCP.toLowerCase(), new CopyOnWriteArrayList<ExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPoint.UDP.toLowerCase(), new CopyOnWriteArrayList<ExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPoint.SCTP.toLowerCase(), new CopyOnWriteArrayList<ExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPoint.TLS.toLowerCase(), new CopyOnWriteArrayList<ExtendedListeningPoint>());
		// creating the ipaddress/port/transport cache map
		extendedListeningPointsCacheMap = new ConcurrentHashMap<String, ExtendedListeningPoint>();
	}
	
	/**
	 * Retrieve the listening points for this manager
	 * @return the listening points for this manager
	 */
	public Iterator<ExtendedListeningPoint> getExtendedListeningPoints() {
		return extendedListeningPointList.iterator();
	}
	
	/**
	 * 
	 * @param extendedListeningPoint
	 */
	public void addExtendedListeningPoint(ExtendedListeningPoint extendedListeningPoint) {
		extendedListeningPointList.add(extendedListeningPoint);
		computeOutboundInterfaces();
		// Adding to the transport cache map
		List<ExtendedListeningPoint> extendedListeningPoints = 
			transportMappingCacheMap.get(extendedListeningPoint.getTransport().toLowerCase());
	    extendedListeningPoints.add(extendedListeningPoint);
	    // Adding private ipaddress to the triplet cache map
	    for(String ipAddress : extendedListeningPoint.getIpAddresses()) {
	    	extendedListeningPointsCacheMap.put(ipAddress + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
	    }
	    // Adding public address if any to the triplet cache map
	    if(extendedListeningPoint.getGlobalIpAddress() != null) {
	    	extendedListeningPointsCacheMap.put(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
	    	extendedListeningPointsCacheMap.put(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getGlobalPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
	    }
	}
	
	/**
	 * 
	 * @param extendedListeningPoint
	 */
	public void removeExtendedListeningPoint(ExtendedListeningPoint extendedListeningPoint) {
		extendedListeningPointList.add(extendedListeningPoint);
		computeOutboundInterfaces();
		// removing from the transport cache map
		List<ExtendedListeningPoint> extendedListeningPoints = 
			transportMappingCacheMap.get(extendedListeningPoint.getTransport().toLowerCase());
	    extendedListeningPoints.remove(extendedListeningPoint);
	    // Removing private ipaddress from the triplet cache map for listening point
	    for(String ipAddress : extendedListeningPoint.getIpAddresses()) {
	    	extendedListeningPointsCacheMap.remove(ipAddress + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
	    }
	    // Removing public address if any from the triplet cache map
	    if(extendedListeningPoint.getGlobalIpAddress() != null) {
	    	extendedListeningPointsCacheMap.remove(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
	    	extendedListeningPointsCacheMap.remove(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getGlobalPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
	    }
	}
	
	/**
	 * Retrieve the first matching listening point corresponding to the transport.
	 * @param transport the transport
	 * @param strict if true, it will search only for this transport otherwise it will look for any transport if no
	 * valid listening point could be found for the transport in parameter
	 * @return Retrieve the first matching listening point corresponding to the transport.
	 * If none has been found, null is returned if strict is true.
	 * If none has been found, an exception is thrown is strict is false and no listening point could be found.
	 */
	public ExtendedListeningPoint findMatchingListeningPoint(String transport, boolean strict) {		
		if(transport == null) {
			transport = ListeningPoint.UDP;
		}
		List<ExtendedListeningPoint> extendedListeningPoints = transportMappingCacheMap.get(transport.toLowerCase());
		if(extendedListeningPoints.size() > 0) {
			return extendedListeningPoints.get(0);
		}		
		if(strict) {
			return null;
		} else {
			if(extendedListeningPointList.size() > 0) {
				return extendedListeningPointList.get(0);
			} else {
				throw new RuntimeException("no valid sip connectors could be found to create the sip application session !!!");
			} 
		}
	}
	
	/**
	 * Retrieve the first matching listening Point corresponding to the 
	 * ipAddress port and transport given in parameter.
	 *
	 * @param ipAddress the ip address
	 * @param port the port
	 * @param transport the transport
	 * @return Retrieve the first matching listening point corresponding to the ipAddress port and transport.
	 * If none has been found, null is returned.
	 */
	public ExtendedListeningPoint findMatchingListeningPoint(String ipAddress, int port, String transport) {
		boolean isNumericalIPAddress = false;
		InetAddress[] inetAddresses = new InetAddress[0];
		if(Inet6Util.isValidIP6Address(ipAddress) 
				|| Inet6Util.isValidIPV4Address(ipAddress)) {
			isNumericalIPAddress = true;
		} else {
			try {
				inetAddresses = InetAddress.getAllByName(ipAddress);
			} catch (UnknownHostException e) {
				//not important it can mean that the ipAddress provided is not a hostname
				// but an ip address not found in the searched listening points above				
			}
		}
		if(transport == null) {
			transport = ListeningPoint.UDP;
		}
		int portChecked = checkPortRange(port, transport);
		
		if(isNumericalIPAddress) {
			return extendedListeningPointsCacheMap.get(ipAddress + "/" + portChecked + ":" + transport.toLowerCase());
		} else {
			for (InetAddress inetAddress : inetAddresses) {
				ExtendedListeningPoint extendedListeningPoint = extendedListeningPointsCacheMap.get(inetAddress.getHostAddress() + "/" + portChecked + ":" + transport.toLowerCase());
				if(extendedListeningPoint != null) {
					return extendedListeningPoint;
				}
			}
		}
						
		return null; 
	}
	
	/**
	 * Checks if the port is in the UDP-TCP port numbers (0-65355) range 
	 * otherwise defaulting to 5060 if UDP, TCP or SCTP or to 5061 if TLS
	 * @param port port to check
	 * @return the smae port number if in range, otherwise 5060 if UDP, TCP or SCTP or to 5061 if TLS
	 */
	public static int checkPortRange(int port, String transport) {
		if(port < MIN_PORT_NUMBER && port > MAX_PORT_NUMBER) {		
			if(transport.equalsIgnoreCase(ListeningPoint.TLS)) {
				return ListeningPoint.PORT_5061;
			} else {
				return ListeningPoint.PORT_5060;
			}
		}
		else {
			return port;
		}
	}
	
	/**
	 * Returns An immutable instance of the java.util.List interface containing 
	 * the SipURI representation of IP addresses which are used by the container to send out the messages.
	 * @return immutable List containing the SipURI representation of IP addresses 
	 */
	public List<SipURI> getOutboundInterfaces() {
		return Collections.unmodifiableList(outboundInterfaces);
	}
	
	/**
	 * Compute all the outbound interfaces for this manager 
	 */
	protected void computeOutboundInterfaces() {
		if(logger.isDebugEnabled()) {
			logger.debug("Outbound Interface List : ");
		}
		List<SipURI> newlyComputedOutboundInterfaces = new CopyOnWriteArrayList<SipURI>();
		Iterator<ExtendedListeningPoint> it = getExtendedListeningPoints();
		while (it.hasNext()) {
			ExtendedListeningPoint extendedListeningPoint = it
					.next();
									
			for(String ipAddress : extendedListeningPoint.getIpAddresses()) {
				try {
					javax.sip.address.SipURI jainSipURI = SipFactories.addressFactory.createSipURI(
							null, ipAddress);
					jainSipURI.setPort(extendedListeningPoint.getPort());
					jainSipURI.setTransportParam(extendedListeningPoint.getTransport());
					SipURI sipURI = new SipURIImpl(jainSipURI);
					newlyComputedOutboundInterfaces.add(sipURI);
					if(logger.isDebugEnabled()) {
						logger.debug("Outbound Interface : " + jainSipURI);
					}
				} catch (ParseException e) {
					logger.error("cannot add the following listening point " +
							ipAddress + ":" +
							extendedListeningPoint.getPort() + ";transport=" +
							extendedListeningPoint.getTransport() + " to the outbound interfaces", e);
				}
			}
		}					
		synchronized (outboundInterfaces) {
			outboundInterfaces  = newlyComputedOutboundInterfaces;
		}
	}
}
