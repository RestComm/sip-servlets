/*
 * TeleStax, Open Source Cloud Communications  
 * Copyright 2012 and individual contributors by the @authors tag. 
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
package org.mobicents.servlet.sip.core.message;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class OutboundProxy {

	private String host = null;
	private int port = 5060;
	
	public OutboundProxy(String outboundProxy) {
		int separatorIndex = outboundProxy.indexOf(":");
		if(separatorIndex > 0) {
			host = outboundProxy.substring(0, separatorIndex);
			port = Integer.parseInt(outboundProxy.substring(separatorIndex + 1));
		} else {
			host = outboundProxy;
		}
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {		
		return host + ":" + port;
	}
}
