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

package org.mobicents.servlet.sip;

import java.io.Serializable;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * Represents a Mobicents Sip Servlets SIP connector and its various attributes
 * 
 * @author jean.deruelle@gmail.com
 * 
 */
public class SipConnector implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(SipConnector.class);

	/**
	 * the sip stack signaling transport
	 */
	private String transport;

	/**
	 * the sip stack listening port
	 */
	private int port;
	/*
	 * IP address for this protocol handler.
	 */
	private String ipAddress;
	/*
	 * use Stun
	 */
	private boolean useStun;
	/*
	 * Stun Server Address
	 */
	private String stunServerAddress;
	/*
	 * Stun Server Port
	 */
	private int stunServerPort;

	/*
	 * These settings staticServerAddress, staticServerPort will override all
	 * stun settings and will put the server address in the Via/RR/Contact
	 * headers
	 */
	private String staticServerAddress;
	private int staticServerPort;
	private boolean useStaticAddress;
	private boolean replaceStaticServerAddressForInternalRoutingRequest = true;
	
	/**
	 * @return the Transport
	 */
	public String getTransport() {
		return transport;
	}

	/**
	 * @param transport
	 *            the transport to set
	 */
	public void setTransport(String transport) {
		this.transport = transport;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 * @throws Exception
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @return the stunServerAddress
	 */
	public String getStunServerAddress() {
		return stunServerAddress;
	}

	/**
	 * @param stunServerAddress
	 *            the stunServerAddress to set
	 */
	public void setStunServerAddress(String stunServerAddress) {
		this.stunServerAddress = stunServerAddress;
	}

	/**
	 * @return the stunServerPort
	 */
	public int getStunServerPort() {
		return stunServerPort;
	}

	/**
	 * @param stunServerPort
	 *            the stunServerPort to set
	 */
	public void setStunServerPort(int stunServerPort) {
		this.stunServerPort = stunServerPort;
	}

	/**
	 * @return the useStun
	 */
	public boolean isUseStun() {
		return useStun;
	}

	/**
	 * @param useStun
	 *            the useStun to set
	 */
	public void setUseStun(boolean useStun) {
		this.useStun = useStun;
	}

	public String getStaticServerAddress() {
		return staticServerAddress;
	}

	public void setStaticServerAddress(String staticServerAddress) {
		this.staticServerAddress = staticServerAddress;
	}

	public int getStaticServerPort() {
		return staticServerPort;
	}

	public void setStaticServerPort(int staticServerPort) {
		this.staticServerPort = staticServerPort;
	}

	public boolean isUseStaticAddress() {
		return useStaticAddress;
	}

	public void setUseStaticAddress(boolean useStaticAddress) {
		this.useStaticAddress = useStaticAddress;
	}
	
	public boolean isReplaceStaticServerAddressForInternalRoutingRequest() {
		return replaceStaticServerAddressForInternalRoutingRequest;
	}
	
	public void setReplaceStaticServerAddressForInternalRoutingRequest(boolean value) {
		this.replaceStaticServerAddressForInternalRoutingRequest = value;
	}
	
	/**
	 * Cleans up all cached TCP/TLS/SCTP sockets in case they are stalled. You should not call this method on
	 * UDP connector, it has no effect.
	 * 
	 * After closing the sockets when needed new sockets will be created for the same dialog or
	 * transaction as required by the SIP specification.
	 */
	public void closeAllSockets() {
		if(transport.equalsIgnoreCase("udp")) {
			logger.warn("Cannot close TCP sockets on UDP connector");
			return;
		}
		MBeanServer mbeanServer = getMBeanServer();
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("MBean Server = " + mbeanServer);
			}
			Set<ObjectName> queryNames = mbeanServer.queryNames(new ObjectName("*:type=GlobalRequestProcessor,*"), null);
			for(ObjectName objectName : queryNames) {
				if(objectName.getCanonicalName().toLowerCase().contains("sip-tcp") ||
						objectName.getCanonicalName().toLowerCase().contains("sip-tls")) {
					// TODO: we may want to check the IP address if it matches this.ipAddress
					if(logger.isInfoEnabled()) {
						logger.info("Invoking closeAllTcpSockets()");
					}
					mbeanServer.invoke(objectName, "closeAllTcpSockets", null, null);
				}
			}
		} catch (Exception e) {
			logger.error("Error closing the TCP sockets", e);
		}
	}

	private MBeanServer getMBeanServer() {
		return (MBeanServer) MBeanServerFactory.findMBeanServer(null).iterator().next();
	}
	
	/**
     * Send a heartbeat to the specified Ip address and port
     * via this listening point. This method can be used to send out a period
     * Double CR-LF for NAT keepalive as defined in RFC5626
     *
     * @since 1.7
     * 
     * @param ipAddress
     * @param port
     */
    public void sendHeartBeat(String ipAddress, int port) throws Exception {
    	MBeanServer mbeanServer = getMBeanServer();
			
		Set<ObjectName> queryNames = mbeanServer.queryNames(new ObjectName("*:type=Service,*"), null);
		for(ObjectName objectName : queryNames) {								
			mbeanServer.invoke(objectName, "sendHeartBeat", new Object[]{this.ipAddress, this.port, this.transport, ipAddress, port}, new String[] {String.class.getName() , "int", String.class.getName(), String.class.getName() , "int"});
		} 
			   
    }
    
    /**
     * Allow to reset the RFC5626 Section 4.4.1 keeplive on  a given TCP/TLS/SCTP connection
     * @since 1.7
     * 
     * @param ipAddress
     * @param port
     * @param timeout
     * @return
     * @throws Exception
     */
    public boolean setKeepAliveTimeout(String ipAddress, int port, long timeout) throws Exception {
    	MBeanServer mbeanServer = getMBeanServer();
		
		Set<ObjectName> queryNames = mbeanServer.queryNames(new ObjectName("*:type=Service,*"), null);
		boolean changed = false; 
		for(ObjectName objectName : queryNames) {								
			changed = (Boolean) mbeanServer.invoke(objectName, "setKeepAliveTimeout", new Object[]{this, ipAddress, port, timeout}, new String[] {SipConnector.class.getName() , String.class.getName(), "int" , "long"});
			if(changed) {
				return changed;
			}
		}		
		return false;
    }

    /**
     * Allow to close a given TCP/TLS/SCTP connection
     * @since 1.7
     * 
     * @param ipAddress
     * @param port
     * @return
     * @throws Exception
     */
    public boolean closeReliableConnection(String ipAddress, int port) throws Exception {
    	MBeanServer mbeanServer = getMBeanServer();
		
		Set<ObjectName> queryNames = mbeanServer.queryNames(new ObjectName("*:type=Service,*"), null);
		boolean changed = false; 
		for(ObjectName objectName : queryNames) {								
			changed = (Boolean) mbeanServer.invoke(objectName, "closeReliableConnection", new Object[]{this, ipAddress, port}, new String[] {SipConnector.class.getName() , String.class.getName(), "int"});
			if(changed) {
				return changed;
			}
		}
		return false;
    }


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ipAddress == null) ? 0 : ipAddress.hashCode());
		result = prime * result + port;
		result = prime * result
				+ ((transport == null) ? 0 : transport.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SipConnector other = (SipConnector) obj;
		if (ipAddress == null) {
			if (other.ipAddress != null) {
				return false;
			}
		} else if (!ipAddress.equals(other.ipAddress)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		if (transport == null) {
			if (other.transport != null) {
				return false;
			}
		} else if (!transport.equals(other.transport)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SipConnector [ipAddress=" + ipAddress + ", port=" + port				
				+ ", transport=" + transport + "]";
	}
}
