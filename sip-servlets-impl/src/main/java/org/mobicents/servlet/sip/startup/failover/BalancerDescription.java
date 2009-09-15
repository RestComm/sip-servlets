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
package org.mobicents.servlet.sip.startup.failover;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class BalancerDescription implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private InetAddress address;
	private int sipPort;
	/**
	 * @param address
	 * @param sipPort
	 * @param hostName
	 */
	public BalancerDescription(InetAddress address, int sipPort) {
		super();
		this.address = address;
		this.sipPort = sipPort;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	/**
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}
	/**
	 * @param sipPort the sipPort to set
	 */
	public void setSipPort(int sipPort) {
		this.sipPort = sipPort;
	}
	/**
	 * @return the sipPort
	 */
	public int getSipPort() {
		return sipPort;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + sipPort;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BalancerDescription other = (BalancerDescription) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (sipPort != other.sipPort)
			return false;
		return true;
	}
	
	
	
}
