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

import gov.nist.javax.sip.ListeningPointExt;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.sip.SipURI;
import javax.sip.ListeningPoint;
import javax.sip.address.Hop;
import javax.sip.address.URI;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
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
public class SipNetworkInterfaceManagerImpl implements SipNetworkInterfaceManager {
	private static final Logger logger = Logger.getLogger(SipNetworkInterfaceManagerImpl.class);
	
	/**
     * The maximum int value that could correspond to a port number.
     */
    public static final int MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port number bindable
     * by the SIP Communicator.
     */
    public static final int MIN_PORT_NUMBER = 1024;    
    
	Set<MobicentsExtendedListeningPoint> extendedListeningPointList = null;
	List<SipURI> outboundInterfaces = null;
	//related to google code issue 563, will be used to check if a request is aimed at a local network or outside
	Set<String> outboundInterfacesIpAddresses = null;
	//use only to lookup the hosts for issue 563, need to find a better way and reduce coupling
	private SipApplicationDispatcherImpl sipApplicationDispatcher;
	
	//those maps are present to improve the performance of finding a listening point either from a transport
	// or from a triplet ipaddress, port and transport
	Map<String, Set<MobicentsExtendedListeningPoint>> transportMappingCacheMap = null;	
	Map<String, MobicentsExtendedListeningPoint> extendedListeningPointsCacheMap = null;
	
	/**
	 * Default Constructor
	 */
	public SipNetworkInterfaceManagerImpl(SipApplicationDispatcherImpl sipApplicationDispatcher) {
		this.sipApplicationDispatcher = sipApplicationDispatcher;
		extendedListeningPointList = new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>();
		outboundInterfaces = new CopyOnWriteArrayList<SipURI>();
		outboundInterfacesIpAddresses = new CopyOnWriteArraySet<String>();
		
		// creating and populating the transport cache map with transports
		transportMappingCacheMap = new HashMap<String, Set<MobicentsExtendedListeningPoint>>();
		transportMappingCacheMap.put(ListeningPoint.TCP.toLowerCase(), new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPoint.UDP.toLowerCase(), new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPoint.SCTP.toLowerCase(), new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPoint.TLS.toLowerCase(), new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPointExt.WS.toLowerCase(), new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>());
		transportMappingCacheMap.put(ListeningPointExt.WSS.toLowerCase(), new CopyOnWriteArraySet<MobicentsExtendedListeningPoint>());
		// creating the ipaddress/port/transport cache map
		extendedListeningPointsCacheMap = new ConcurrentHashMap<String, MobicentsExtendedListeningPoint>();
	}
	
	/**
	 * Retrieve the listening points for this manager
	 * @return the listening points for this manager
	 */
	public Iterator<MobicentsExtendedListeningPoint> getExtendedListeningPoints() {
		return extendedListeningPointList.iterator();
	}
	
    static final String LISTENING_POINT_ATT = "ListeningPoint";
        
    class AddPointAction implements DispatcherFSM.Action {

        
        @Override
        public void execute(DispatcherFSM.Context ctx) {
            MobicentsExtendedListeningPoint extendedListeningPoint = (MobicentsExtendedListeningPoint)ctx.lastEvent.data.get(LISTENING_POINT_ATT);
            
            extendedListeningPointList.add(extendedListeningPoint);
            computeOutboundInterfaces();
            // Adding to the transport cache map
            Set<MobicentsExtendedListeningPoint> extendedListeningPoints
                    = transportMappingCacheMap.get(extendedListeningPoint.getTransport().toLowerCase());
            boolean added = extendedListeningPoints.add(extendedListeningPoint);
            if (added) {
                if (sipApplicationDispatcher.getDNSServerLocator() != null) {
                    sipApplicationDispatcher.getDNSServerLocator().addSupportedTransport(extendedListeningPoint.getTransport());
                }
                // Adding private ipaddress to the triplet cache map
                for (String ipAddress : extendedListeningPoint.getIpAddresses()) {
                    extendedListeningPointsCacheMap.put(ipAddress + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
                }
                // Adding public address if any to the triplet cache map
                if (extendedListeningPoint.getGlobalIpAddress() != null) {
                    extendedListeningPointsCacheMap.put(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
                    extendedListeningPointsCacheMap.put(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getGlobalPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
                }
                // Adding local hostnames if any to the triplet cache map
                if (extendedListeningPoint.getSipConnector().getHostNames() != null) {
                    StringTokenizer tokenizer = new StringTokenizer(extendedListeningPoint.getSipConnector().getHostNames(), ",");
                    while (tokenizer.hasMoreTokens()) {
                        String localHostName = tokenizer.nextToken();
                        extendedListeningPointsCacheMap.put(localHostName + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase(), extendedListeningPoint);
                        if (sipApplicationDispatcher.getDNSServerLocator() != null) {
                            sipApplicationDispatcher.getDNSServerLocator().mapLocalHostNameToIP(localHostName, new CopyOnWriteArraySet<String>(extendedListeningPoint.getIpAddresses()));
                        }
                    }
                }
            }
        }

    }
        
        @Override
    public void addExtendedListeningPoint(MobicentsExtendedListeningPoint extendedListeningPoint) {
        DispatcherFSM.Event event = sipApplicationDispatcher.fsm.new Event(DispatcherFSM.EventType.ADD_CONNECTOR);
        event.data.put(LISTENING_POINT_ATT, extendedListeningPoint);
        this.sipApplicationDispatcher.fsm.fireEvent(event);
    }
	
    class RemovePointAction implements DispatcherFSM.Action {
        @Override
        public void execute(DispatcherFSM.Context ctx) {
            MobicentsExtendedListeningPoint extendedListeningPoint = (MobicentsExtendedListeningPoint)ctx.lastEvent.data.get(LISTENING_POINT_ATT);
            
            // notifying the applications before removing the listening point so that apps can still try to send a message on it
            if (extendedListeningPointList.contains(extendedListeningPoint)) {

                extendedListeningPointList.remove(extendedListeningPoint);
                computeOutboundInterfaces();
                // removing from the transport cache map
                Set<MobicentsExtendedListeningPoint> extendedListeningPoints
                        = transportMappingCacheMap.get(extendedListeningPoint.getTransport().toLowerCase());
                extendedListeningPoints.remove(extendedListeningPoint);
                if (sipApplicationDispatcher.getDNSServerLocator() != null) {
                    sipApplicationDispatcher.getDNSServerLocator().removeSupportedTransport(extendedListeningPoint.getTransport());
                }
                // Removing private ipaddress from the triplet cache map for listening point
                for (String ipAddress : extendedListeningPoint.getIpAddresses()) {
                    extendedListeningPointsCacheMap.remove(ipAddress + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
                }
                // Removing public address if any from the triplet cache map
                if (extendedListeningPoint.getGlobalIpAddress() != null) {
                    extendedListeningPointsCacheMap.remove(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
                    extendedListeningPointsCacheMap.remove(extendedListeningPoint.getGlobalIpAddress() + "/" + extendedListeningPoint.getGlobalPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
                }
                if (extendedListeningPoint.getSipConnector().getHostNames() != null) {
                    StringTokenizer tokenizer = new StringTokenizer(extendedListeningPoint.getSipConnector().getHostNames(), ",");
                    while (tokenizer.hasMoreTokens()) {
                        String localHostName = tokenizer.nextToken();
                        extendedListeningPointsCacheMap.remove(localHostName + "/" + extendedListeningPoint.getPort() + ":" + extendedListeningPoint.getTransport().toLowerCase());
                        if (sipApplicationDispatcher.getDNSServerLocator() != null) {
                            sipApplicationDispatcher.getDNSServerLocator().unmapLocalHostNameToIP(localHostName);
                        }
                    }
                }
            }
        }

    }
    
    public void removeExtendedListeningPoint(MobicentsExtendedListeningPoint extendedListeningPoint) {
        DispatcherFSM.Event event = sipApplicationDispatcher.fsm.new Event(DispatcherFSM.EventType.REMOVE_CONNECTOR);
        event.data.put(LISTENING_POINT_ATT, extendedListeningPoint);
        this.sipApplicationDispatcher.fsm.fireEvent(event);
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
	public MobicentsExtendedListeningPoint findMatchingListeningPoint(String transport, final boolean strict) {
		String tmpTransport = transport;
		if(tmpTransport == null) {
			tmpTransport = ListeningPoint.UDP;
		}
		Set<MobicentsExtendedListeningPoint> extendedListeningPoints = transportMappingCacheMap.get(tmpTransport.toLowerCase());
		if(extendedListeningPoints.size() > 0) {
			MobicentsExtendedListeningPoint extendedListeningPoint = extendedListeningPoints.iterator().next();
			if(logger.isTraceEnabled()) {
				logger.trace("Found first listening point " + extendedListeningPoint + " with transport " + transport);
			}
			return extendedListeningPoint;
		}		
		if(strict) {
			return null;
		} else {
			if(extendedListeningPointList.size() > 0) {
				MobicentsExtendedListeningPoint extendedListeningPoint = extendedListeningPointList.iterator().next();
				if(logger.isTraceEnabled()) {
					logger.trace("Found first listening point " + extendedListeningPoint + " with transport " + transport);
				}
				return extendedListeningPoint;
			} else {
				throw new RuntimeException("no valid sip connectors could be found to create the sip application session !!!");
			} 
		}
	}
	
	public MobicentsExtendedListeningPoint findMatchingListeningPoint(
			javax.sip.address.SipURI outboundInterface, boolean strict) {
		if(logger.isTraceEnabled()) {
			logger.trace("Trying to find listening point corresponding to outbound interface " + outboundInterface + " in strict manner " + strict);
		}
		String tmpTransport = outboundInterface.getTransportParam();		
		if(tmpTransport == null) {
			if(outboundInterface.isSecure()) {
				tmpTransport =  ListeningPoint.TCP;
			} else {
				tmpTransport =  ListeningPoint.UDP;
			}
		}
		Set<MobicentsExtendedListeningPoint> extendedListeningPoints = transportMappingCacheMap.get(tmpTransport.toLowerCase());
		if(extendedListeningPoints.size() > 0) {
			Iterator<MobicentsExtendedListeningPoint> extentdedLPiterator = extendedListeningPoints.iterator();
			while (extentdedLPiterator.hasNext()) {
				MobicentsExtendedListeningPoint extendedListeningPoint = extentdedLPiterator.next();
				if(logger.isTraceEnabled()) {
					logger.trace("Comparing listening point " + extendedListeningPoint + " with outbound ipaddress " + outboundInterface.getHost() + " and port " + outboundInterface.getPort());
				}
				// Fix for http://code.google.com/p/sipservlets/issues/detail?id=159 	Bad choice of connectors when multiple of the same transport are available
				if(extendedListeningPoint.getIpAddresses().contains(outboundInterface.getHost()) && extendedListeningPoint.getPort() == outboundInterface.getPort()) {
					if(logger.isTraceEnabled()) {
						logger.trace("Found listening point " + extendedListeningPoint);
					}
					return extendedListeningPoint;
				}
			}
		}		
		if(strict) {
			return null;
		} else {
			if(extendedListeningPointList.size() > 0) {
				Iterator<MobicentsExtendedListeningPoint> extentdedLPiterator = extendedListeningPoints.iterator();
				while (extentdedLPiterator.hasNext()) {
					MobicentsExtendedListeningPoint extendedListeningPoint = extentdedLPiterator.next();
					if(logger.isTraceEnabled()) {
						logger.trace("Comparing listening point " + extendedListeningPoint + " with outbound ipaddress " + outboundInterface.getHost() + " and port " + outboundInterface.getPort());
					}
					String outboundInterfaceHost = extractIPV6Brackets(outboundInterface.getHost());
					for (String ipAddress : extendedListeningPoint.getIpAddresses()) {
						// Fix for http://code.google.com/p/sipservlets/issues/detail?id=159 	Bad choice of connectors when multiple of the same transport are available
						// Ipv6 issue when scope-id is removed out of via header, but the sip-connector is created on ipv6 with scope-id
						if (ipAddress.contains(outboundInterfaceHost) && extendedListeningPoint.getPort() == outboundInterface.getPort()) {
							if(logger.isTraceEnabled()) {
								logger.trace("Found listening point " + extendedListeningPoint);
							}
							return extendedListeningPoint;
						}
					}
				}
				throw new RuntimeException("no valid sip connectors could be found to create the sip application session !!!");
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
	public MobicentsExtendedListeningPoint findMatchingListeningPoint(final String ipAddress, int port, String transport) {
		String tmpTransport = transport;
		int portChecked = checkPortRange(port, tmpTransport);
		if(tmpTransport == null) {
			tmpTransport = ListeningPoint.UDP;
		}	
		
		// we check first if a listening point can be found (we only do the host resolving if not found to have better perf )
		MobicentsExtendedListeningPoint listeningPoint = null;
		
		if (ipAddress.indexOf(':') != -1) {
			String ipAddressTmp = extractIPV6Brackets(ipAddress);
			// IPv6 here, Jain-Sip stack remove scope zone which is included in the key of listener object
			for(String key: extendedListeningPointsCacheMap.keySet()){
				if (logger.isDebugEnabled()) {
					logger.debug("Find listening point for IPv6: " + key + " compare: " + ipAddressTmp + " port " + portChecked + " transport " + tmpTransport.toLowerCase());
				}
				if (key.contains(ipAddressTmp) &&
						key.contains(String.valueOf(portChecked)+ ":" + tmpTransport.toLowerCase())) {
					listeningPoint = extendedListeningPointsCacheMap.get(key);
					break;
				}
			}
		}else {
			listeningPoint = extendedListeningPointsCacheMap.get(ipAddress + "/" + portChecked + ":" + tmpTransport.toLowerCase());
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Checked Listening Point " + ipAddress + "/" + portChecked + ":" + tmpTransport.toLowerCase() + " against existing listening points, found " + listeningPoint);
		}
		if(listeningPoint == null && !Inet6Util.isValidIP6Address(ipAddress) 
					&& !Inet6Util.isValidIPV4Address(ipAddress)) {
			// if no listening point has been found and the ipaddress is not a valid IP6 address nor a valid IPV4 address 
			// then we try to resolve it as a hostname
			Queue<Hop> hops = null;
			try {
				hops = sipApplicationDispatcher.getDNSServerLocator().getDnsLookupPerformer().locateHopsForNonNumericAddressWithPort(ipAddress, portChecked, tmpTransport.toLowerCase());
			} catch (Exception e) {
				// not important it can mean that the ipAddress provided is not a hostname
				// but an ip address not found in the searched listening points above				
			}
			if(hops != null) {
				for (Hop hop : hops) {
					if(logger.isDebugEnabled()) {
						logger.debug("Checking Hop " + hop.getHost() + "/" + portChecked + ":" + tmpTransport.toLowerCase() + " against existing listening points");
					}
					listeningPoint = extendedListeningPointsCacheMap.get(hop.getHost() + "/" + portChecked + ":" + tmpTransport.toLowerCase());
					if(listeningPoint != null) {
						if(logger.isDebugEnabled()) {
							logger.debug("Found listening point " + listeningPoint);
						}
						return listeningPoint;
					}
				}
			}
		}
		return listeningPoint;
	}		
	
	/**
	 * If the host is Ipv6, get the host without "[" and "]"
	 * If the host is Ipv4, get the host as normal.
	 * @param host
	 * @return host without square brackets
	 */
	private String extractIPV6Brackets(String host) {
		String ret = host;
		// IPv6 here, remove square brackets
		if (host.indexOf(':') != -1) {
			int startPoint = host.indexOf('[');
			int endPoint = host.indexOf(']');
			if (endPoint == -1) endPoint = host.length();
			if (startPoint != -1) {
				ret = host.substring(startPoint + 1, endPoint);
			} else {
				ret = host.substring(0, endPoint);
			}
		}
		return ret;
	}
	/**
	 * Checks if the port is in the UDP-TCP port numbers (0-65355) range 
	 * otherwise defaulting to 5060 if UDP, TCP or SCTP or to 5061 if TLS
	 * @param port port to check
	 * @return the smae port number if in range, otherwise 5060 if UDP, TCP or SCTP or to 5061 if TLS
	 */
	public static int checkPortRange(int port, String transport) {
		if(port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {		
			if((ListeningPoint.TLS).equalsIgnoreCase(transport)) {
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
		Set<String> newlyComputedOutboundInterfacesIpAddresses = new CopyOnWriteArraySet<String>();
		Iterator<MobicentsExtendedListeningPoint> it = getExtendedListeningPoints();
		while (it.hasNext()) {
			MobicentsExtendedListeningPoint extendedListeningPoint = it
					.next();
									
			for(String ipAddress : extendedListeningPoint.getIpAddresses()) {
				try {
					newlyComputedOutboundInterfacesIpAddresses.add(ipAddress);
					javax.sip.address.SipURI jainSipURI = SipFactoryImpl.addressFactory.createSipURI(
							null, ipAddress);
					jainSipURI.setPort(extendedListeningPoint.getPort());
					jainSipURI.setTransportParam(extendedListeningPoint.getTransport());
					SipURI sipURI = new SipURIImpl(jainSipURI, ModifiableRule.NotModifiable);
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

                outboundInterfaces  = newlyComputedOutboundInterfaces;		
                outboundInterfacesIpAddresses  = newlyComputedOutboundInterfacesIpAddresses;

		
	}
	
	public boolean findUsePublicAddress(Message message) {
		// TODO add a caching mechanism to increase perf
		boolean usePublicAddress = true;
		String host = null;
		int port = -1;
		String transport = JainSipUtils.findTransport(message);
		if(message instanceof Request) {
			// Get the first route header (it will be used before the request uri for request routing as per RFC3261)
			// or the request uri if it is null 
			Request request = (Request) message;
			RouteHeader routeHeader = (RouteHeader) request.getHeader(RouteHeader.NAME);
			URI uri = null;
			if(routeHeader != null) {
				uri = routeHeader.getAddress().getURI();
			} else {
				uri = request.getRequestURI();
			}
			if(uri instanceof javax.sip.address.SipURI) {
				javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) uri;
				host = sipUri.getHost();
				port = sipUri.getPort();
			}			
		} else {		
			// Get the first via header (it will be used for response routing as per RFC3261)
			ViaHeader topmostViaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
			if(topmostViaHeader != null) {
				host = topmostViaHeader.getHost();
				port = topmostViaHeader.getPort();
			}
		}
		// check if it the host is part of a private network as defined per RFC 1918 (see http://en.wikipedia.org/wiki/Private_network)
		if(host != null) {
			if(Inet6Util.isValidIP6Address(host)) {
				usePublicAddress = false;
				if(logger.isDebugEnabled()) {
					logger.debug("host " + host + " is a numeric IPV6 address, " +
							"no DNS SRV lookup to be done and no need for STUN");
				}
			} else if(Inet6Util.isValidIPV4Address(host)) {
				if(logger.isDebugEnabled()) {
					logger.debug("host " + host + " is a numeric IPV4 address, " +
							"no DNS SRV lookup to be done");
				}
				usePublicAddress = !isIPAddressPartOfPrivateNetwork(host);
			} else {
				logger.debug("host " + host + " is a hostname, " +
					"doing DNS A + AAAA lookup");
				//hostname  : DNS lookup to do
				Hop hop = new HopImpl(host, port, transport);
				Queue<Hop> hops = null;
				try {
					hops = sipApplicationDispatcher.getDNSServerLocator().getDnsLookupPerformer().locateHopsForNonNumericAddressWithPort(host, port, transport);
				} catch (Exception e) {
					// not important it can mean that the ipAddress provided is not a hostname
					// but an ip address not found in the searched listening points above				
				}
				if(hops != null && hops.size() > 0) {
					Hop lookedupHop = hops.peek();
					if(!hop.equals(lookedupHop)) {
						usePublicAddress = !isIPAddressPartOfPrivateNetwork(lookedupHop.getHost());
					}
				}
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("STUN enabled : use public adress " + usePublicAddress + " for following host " + host);
		}
		return usePublicAddress;
	}
	
	public static boolean isIPAddressPartOfPrivateNetwork(String ipAddress) {
		int addressOutboundness = JainSipUtils.getAddressOutboundness(ipAddress);
		// check if it part of a private network
		if(addressOutboundness < 4) {			
			if(logger.isDebugEnabled()) {
				logger.debug("host " + ipAddress + " is part of a private network we will not use the public address resolved by STUN");
			}
			return true;
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("host " + ipAddress + " is not part of a private network we will use the public address resolved by STUN");
			}
			return false;
		}
	}	
}
