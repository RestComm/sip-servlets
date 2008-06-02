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
 * It is composed of the Call-Id and the application Name.
 * </p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class SipApplicationSessionKey implements Serializable {
	String callId; 
	String applicationName;
	/**
	 * @param callId
	 * @param applicationName
	 */
	protected SipApplicationSessionKey(String callId, String applicationName) {
		super();
		this.callId = callId;
		this.applicationName = applicationName;
	}
	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
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
		result = prime * result + ((callId == null) ? 0 : callId.hashCode());
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
		if (callId == null) {
			if (other.callId != null)
				return false;
		} else if (!callId.equals(other.callId))
			return false;
		return true;
	}		
	
	@Override
	public String toString() {
		StringBuffer value = new StringBuffer();
		value = value.append("(");
		value = value.append(callId);
		value = value.append(",");
		value = value.append(applicationName);
		value = value.append(")");
		return value.toString();
	}
}
