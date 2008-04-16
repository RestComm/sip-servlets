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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipURI;
import javax.sip.ListeningPoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;

/**
 * This class will be a placeholder for all sip network interfaces mapped to a sip
 * standard service. It should allow one to query for local and external address 
 * (discovered by STUN) of a specific interface. <br/>
 * 
 * It will also allow various queries against its network interfaces to discover
 * the right one to use. Those queries could be cached in order to improve performance 
 * 
 * TODO : cache queries to improve performance
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class SipNetworkInterfaceManager {
	private static Log logger = LogFactory.getLog(SipNetworkInterfaceManager.class);
	
	/**
     * The maximum int value that could correspond to a port nubmer.
     */
    public static final int    MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port nubmer bindable
     * by the SIP Communicator.
     */
    public static final int    MIN_PORT_NUMBER = 1024;
    
	
	List<ExtendedListeningPoint> extendedListeningPointList = null;
	List<SipURI> outboundInterfaces = null;
	
	/**
	 * Default Constructor
	 */
	public SipNetworkInterfaceManager() {
		extendedListeningPointList = new ArrayList<ExtendedListeningPoint>();
		outboundInterfaces = new ArrayList<SipURI>();
	}
	
	/**
	 * Retrieve the listening points for this manager
	 * @return the listening points for this manager
	 */
	public Iterator<ExtendedListeningPoint> getExtendedListeningPoints() {
		synchronized (extendedListeningPointList) {
			return Collections.unmodifiableList(extendedListeningPointList).iterator();
		}
	}
	
	/**
	 * 
	 * @param extendedListeningPoint
	 */
	public void addExtendedListeningPoint(ExtendedListeningPoint extendedListeningPoint) {
		synchronized (extendedListeningPointList) {
			extendedListeningPointList.add(extendedListeningPoint);
			computeOutboundInterfaces();
		}		
	}
	
	/**
	 * 
	 * @param extendedListeningPoint
	 */
	public void removeExtendedListeningPoint(ExtendedListeningPoint extendedListeningPoint) {
		synchronized (extendedListeningPointList) {
			extendedListeningPointList.add(extendedListeningPoint);
			computeOutboundInterfaces();
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
		Iterator<ExtendedListeningPoint> it = extendedListeningPointList.iterator();		 
		while (it.hasNext()) {
			ExtendedListeningPoint extendedListeningPoint = it.next();
			if(transport != null && transport.equalsIgnoreCase(extendedListeningPoint.getTransport())) {
				return extendedListeningPoint;
			}			
		}
		if(strict) {
			return null;
		} else {
			it = getExtendedListeningPoints();
			if(it.hasNext()) {
				return it.next();
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
		Iterator<ExtendedListeningPoint> it = extendedListeningPointList.iterator();		 
		while (it.hasNext()) {
			ExtendedListeningPoint extendedListeningPoint = it.next();						
			if(isListeningPointMatching(ipAddress, port, transport, extendedListeningPoint)) {
				return extendedListeningPoint;
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
	public static boolean isListeningPointMatching(String ipAddress, int port, String transport, 
			ExtendedListeningPoint listeningPoint) {
		int portChecked = checkPortRange(port, transport);
		
		List<String> listeningPointAddresses = listeningPoint.getIpAddresses();
		//resolving by ipaddress an port
		if((listeningPointAddresses.contains(ipAddress) &&					
				listeningPoint.getPort() == portChecked) ||
				(listeningPoint.getGlobalIpAddress() != null && listeningPoint.getGlobalIpAddress().equalsIgnoreCase(ipAddress) &&					
				listeningPoint.getGlobalPort() == portChecked)){
			return true;
		}
		//resolving by hostname
		try {
			InetAddress[] inetAddresses = InetAddress.getAllByName(ipAddress);				
			for (InetAddress inetAddress : inetAddresses) {
				if((listeningPointAddresses.contains(inetAddress.getHostAddress())
						&& listeningPoint.getPort() == portChecked) ||
						(listeningPoint.getGlobalIpAddress() != null && listeningPoint.getGlobalIpAddress().equalsIgnoreCase(inetAddress.getHostAddress()) &&					
						listeningPoint.getGlobalPort() == portChecked)) {
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
	 * Checks if the port is in the UDP-TCP port numbers (0-65355) range 
	 * otherwise defaulting to 5060
	 * @param port port to check
	 * @return the smae port number if in range, 5060 otherwise
	 */
	public static int checkPortRange(int port, String transport) {
		if(port < MIN_PORT_NUMBER && port > MAX_PORT_NUMBER) {		
			if(transport.equalsIgnoreCase(ListeningPoint.TCP)) {
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
		
		List<SipURI> newlyComputedOutboundInterfaces = new ArrayList<SipURI>();
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
