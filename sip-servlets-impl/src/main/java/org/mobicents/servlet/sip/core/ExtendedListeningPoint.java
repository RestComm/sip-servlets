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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ViaHeader;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * @author <A HREF="mailto:jean.deruellimport javax.sip.SipProvider;
e@gmail.com">Jean Deruelle</A>
 *
 */
public class ExtendedListeningPoint implements MobicentsExtendedListeningPoint {
	private static final Logger logger = Logger.getLogger(ExtendedListeningPoint.class);		
	
	// the listening point this class is extending
	private ListeningPointExt listeningPoint;
	// the sip provider attached to 
	private SipProvider sipProvider;
	private SipConnector sipConnector;
	private String globalIpAddress;
	String mostOutboundAddress = null;
	private int globalPort;
	private List<String> ipAddresses;
	private boolean isAnyLocalAddress;
	private boolean useStaticAddress;
	
//	String host = null;
	int port = -1;
	String transport;

	/**
	 * 
	 */
	public ExtendedListeningPoint(SipProvider sipProvider, ListeningPointExt listeningPoint, SipConnector sipConnector) {
		this.sipProvider = sipProvider;		
		this.listeningPoint = listeningPoint;
		this.sipConnector = sipConnector;
		this.globalIpAddress = null;
		this.globalPort = -1;
		ipAddresses = new ArrayList<String>();
		try {
			isAnyLocalAddress = InetAddress.getByName(listeningPoint.getIPAddress()).isAnyLocalAddress();						
		} catch (UnknownHostException e) {
			logger.warn("Unable to enumerate mapped interfaces. Binding to 0.0.0.0 may not work.");
			isAnyLocalAddress = false;			
		}	
		if(isAnyLocalAddress) {
			try{
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				while(networkInterfaces.hasMoreElements()) {
					NetworkInterface networkInterface = networkInterfaces.nextElement();
					Enumeration<InetAddress> bindings = networkInterface.getInetAddresses();
					while(bindings.hasMoreElements()) {
						InetAddress addr = bindings.nextElement();
						String networkInterfaceIpAddress = addr.getHostAddress();
						ipAddresses.add(networkInterfaceIpAddress);
					}
				}
			} catch (SocketException e) {
				logger.warn("Unable to enumerate network interfaces. Binding to 0.0.0.0 may not work.");
			}
		} else {
			ipAddresses.add(listeningPoint.getIPAddress());
		}		 
		
		mostOutboundAddress = JainSipUtils.getMostOutboundAddress(ipAddresses);
		transport = listeningPoint.getTransport();
		port = listeningPoint.getPort();
	}

	/**
	 *
	 */
	public String getIpAddress(boolean usePublicAddress) {
		// Making use of the global ip address discovered by STUN if it is present		
		if(usePublicAddress && globalIpAddress != null) {
			return globalIpAddress;
		} else {
			return mostOutboundAddress;
		}
		
	}
	/**
	 * Create a Contact Header based on the host, port and transport of this listening point 
	 * @param usePublicAddress if true, the host will be the global ip address found by STUN otherwise
	 *  it will be the local network interface ipaddress
	 * @param displayName the display name to use
	 * @return the Contact header 
	 */
	public ContactHeader createContactHeader(String displayName, String userName, boolean usePublicAddress) {
		return createContactHeader(displayName, userName, usePublicAddress, null);
	}
	
	/**
	 * Create a Contact Header based on the host, port and transport of this listening point 
	 * @param usePublicAddress if true, the host will be the global ip address found by STUN otherwise
	 *  it will be the local network interface ipaddress
	 * @param displayName the display name to use
	 * @param outboundInterface the outbound interface ip address to be used for the host part of the Contact header
	 * @return the Contact header 
	 */
	public ContactHeader createContactHeader(String displayName, String userName, boolean usePublicAddress, String outboundInterface) {
		try {
			// FIXME : the SIP URI can be cached to improve performance 
			String host = null;
			if(outboundInterface!=null){
				javax.sip.address.SipURI outboundInterfaceURI = (javax.sip.address.SipURI) SipFactoryImpl.addressFactory.createURI(outboundInterface);
				host = outboundInterfaceURI.getHost();
			} else {
				host = getIpAddress(usePublicAddress);
			}
			javax.sip.address.SipURI sipURI = SipFactoryImpl.addressFactory.createSipURI(userName, host);
			sipURI.setHost(host);
			sipURI.setPort(port);		
			// Issue 1150 : we assume that if the transport match the default protocol of the transport protocol used it is not added
			// See RFC 32661 Section 19.1.2 Character Escaping Requirements :
			// (2): The default transport is scheme dependent.  For sip:, it is UDP.  For sips:, it is TCP.
			if((!sipURI.isSecure() && !ListeningPoint.UDP.equalsIgnoreCase(transport)) || (sipURI.isSecure() && !ListeningPoint.TCP.equalsIgnoreCase(transport))) { 
				sipURI.setTransportParam(transport);
			}
			javax.sip.address.Address contactAddress = SipFactoryImpl.addressFactory.createAddress(sipURI);			
			ContactHeader contact = SipFactoryImpl.headerFactory.createContactHeader(contactAddress);
		
			if(displayName != null && displayName.length() > 0) {
				contactAddress.setDisplayName(displayName);
			}
			
			return contact;
		} catch (ParseException ex) {
        	logger.error ("Unexpected error while creating the contact header for the extended listening point",ex);
            throw new IllegalArgumentException("Unexpected exception when creating a sip URI", ex);
        }
	}

	/**
	 * Create a Via Header based on the host, port and transport of this listening point 
	 * @param usePublicAddress if true, the host will be the global ip address found by STUN otherwise
	 *  it will be the local network interface ipaddress
	 * @param branch the branch id to use
	 * @return the via header 
	 */
	public ViaHeader createViaHeader(String branch, boolean usePublicAddress) {
        try {
        	String host = getIpAddress(usePublicAddress);
            ViaHeader via = SipFactoryImpl.headerFactory.createViaHeader(host, port, transport, branch);
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
	 * Create a Record Route URI based on the host, port and transport of this listening point 
	 * @param usePublicAddress if true, the host will be the global ip address found by STUN otherwise
	 *  it will be the local network interface ipaddress
	 * @return the record route uri
	 */
	public javax.sip.address.SipURI createRecordRouteURI(boolean usePublicAddress) {		
		try {			
			String host = getIpAddress(usePublicAddress);
			SipURI sipUri = SipFactoryImpl.addressFactory.createSipURI(null, host);
			sipUri.setPort(port);
			sipUri.setTransportParam(transport);
			// Do we want to add an ID here?
			return sipUri;
		} catch (ParseException ex) {
        	logger.error ("Unexpected error while creating a record route URI",ex);
            throw new IllegalArgumentException("Unexpected exception when creating a record route URI", ex);
        }	
	}
	
	/**
	 * Retrieve the jain sip listening point being extended by this class
	 * @return the jain sip listening point being extended by this class
	 */
	public ListeningPointExt getListeningPoint() {
		return listeningPoint;
	}
	
	/**
	 * Retrieve the sip provider associated to the jain sip listening point being extended by this class
	 * @return the sip provider associated to the jain sip listening point being extended by this class
	 */
	public SipProvider getSipProvider() {
		return sipProvider;
	}
	
	/**
	 * Retrieve the global ip address of this listening point (found through STUN), null if no global
	 * ip address is asccoiated to it
	 * @return the global ip address of this listening point, null if no global
	 * ip address is asccoiated to it
	 */
	public String getGlobalIpAddress() {
		return globalIpAddress;
	}
	
	/**
	 * Retrieve the global port of this listening point (found through STUN), -1 if no global
	 * port is asccoiated to it
	 * @return the global port of this listening point, -1 if no global
	 * port is asccoiated to it
	 */
	public int getGlobalPort() {
		return globalPort;
	}
	
	/**
	 * Set the global ip address (found through STUN) of this extended listening point 
	 * @param globalIpAddress the global ip address of this listening point
	 */
	public void setGlobalIpAddress(String globalIpAddress) {
		this.globalIpAddress = globalIpAddress;		
	}
	
	/**
	 * Set the global port (found through STUN) of this extended listening point 
	 * @param globalPort the global port of this listening point
	 */
	public void setGlobalPort(int globalPort) {
		this.globalPort = globalPort;
	}
	
	/**
	 * If we are using a statically assigned IP address instead of our local address (for example when IP LB is used)
	 * @return
	 */
	public boolean isUseStaticAddress() {
		return useStaticAddress;
	}

	/**
	 * If we are using a statically assigned IP address instead of our local address (for example when IP LB is used)
	 * @param useStaticAddress
	 */
	public void setUseStaticAddress(boolean useStaticAddress) {
		this.useStaticAddress = useStaticAddress;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder = stringBuilder.append("ExtendedListeningPoint:")
				.append("ipAddress=")
				.append(listeningPoint.getIPAddress());						
		if(isAnyLocalAddress) {
			stringBuilder = stringBuilder.append(",maps to following ipaddress:");
			for(String mappedIpAddress: ipAddresses) {
				stringBuilder = stringBuilder.append("\n \t mapped ipaddress:");
				stringBuilder = stringBuilder.append(mappedIpAddress);
			}
			stringBuilder = stringBuilder.append("\n");
		}
		stringBuilder = stringBuilder.append(", port=")
		.append(listeningPoint.getPort())
		.append(", transport=")
		.append(transport)
		.append(", globalIpAddress=")
		.append(globalIpAddress)
		.append(", gloablPort=")
		.append(globalPort);
		return stringBuilder.toString();
	}

	/**
	 * Return the list of real ip address represented by this listening point.
	 * This can be many ip address because the listening point IP address could be 0.0.0.0
	 * @return the list of real ip address represented by this listening point.
	 */
	public List<String> getIpAddresses() {
		return ipAddresses;
	}
	
	/**
	 * Retrieve either the global ip address found by STUN or the local network interface one
	 * depending on the boolean value in parameter
	 * @param true means we want to retrieve the global ip address found by STUN 
	 * @return Retrieve either the global ip address found by STUN or the local network interface one
	 * depending on the boolean value in parameter
	 */
	public String getHost(boolean usePublicAddress){
		String host = getIpAddress(usePublicAddress);
		return host;
	}
	
	/**
	 * Retrieve the port associated with this listening point
	 * @return port associated with this listening point
	 */
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Retrieve the transport associated with this listening point
	 * @return the transport associated with this listening point
	 */
	public String getTransport() {
		return transport;
	}
	
	/**
	 * return true if the ip address maps to 0.0.0.0.
	 * If it's true call getIpAddresses ti get the real list of ip addresses it maps too
	 * @return true if the ip adress is a any local address
	 */
	public boolean isAnyLocalAddress() {
		return isAnyLocalAddress;
	}

	/**
	 * @param sipConnector the sipConnector to set
	 */
	public void setSipConnector(SipConnector sipConnector) {
		this.sipConnector = sipConnector;
	}

	/**
	 * @return the sipConnector
	 */
	public SipConnector getSipConnector() {
		return sipConnector;
	}
}
