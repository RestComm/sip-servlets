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
package org.mobicents.servlet.sip.core.session;

import java.io.Serializable;

/**
 * <p>
 * Class representing the key (which will also be its id) for a sip application session.<br/>
 * It is composed of a random UUID and the application Name.
 * </p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class SipApplicationSessionKey implements Serializable {

	private static final long serialVersionUID = 1L;
	String uuid;
	String appGeneratedKey;
	String applicationName;
	private String toString;
	
	/**
	 * @param id
	 * @param applicationName
	 */
	public SipApplicationSessionKey(String id, String applicationName) {
		super();
		if(id == null) {
			this.uuid = "" + System.nanoTime();
		} else {
			this.uuid = id;
		}		
		this.applicationName = applicationName;
		toString = "(" + uuid + "," + applicationName +	")";
	}
	/**
	 * @return the Id
	 */
	public String getId() {
		return uuid;
	}
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @return the applicationName
	 */
	public String getAppGeneratedKey() {
		return appGeneratedKey;
	}
	public void setAppGeneratedKey(String appGeneratedKey) {
		this.appGeneratedKey = appGeneratedKey;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((applicationName == null) ? 0 : applicationName.hashCode());		
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());	
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
		final SipApplicationSessionKey other = (SipApplicationSessionKey) obj;
		if (applicationName == null) {
			if (other.applicationName != null)
				return false;
		} else if (!applicationName.equals(other.applicationName))
			return false;		
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}		
	
	@Override
	public String toString() {
		return toString;
	}
}
