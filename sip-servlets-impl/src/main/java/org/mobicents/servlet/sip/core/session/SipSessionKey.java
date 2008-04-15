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
 * @author Jean Deruelle
 *
 */
public class SipSessionKey implements Serializable {
	String fromAddress;
	String fromTag;
	String toAddress;
	String callId; 
	String applicationName;
	/**
	 * @param fromAddress
	 * @param fromTag
	 * @param toAddress
	 * @param callId
	 * @param applicationName
	 */
	protected SipSessionKey(String fromAddress, String fromTag, String toAddress,
			String callId, String applicationName) {
		super();
		this.fromAddress = fromAddress;
		this.fromTag = fromTag;
		this.toAddress = toAddress;
		this.callId = callId;
		this.applicationName = applicationName;
	}
	/**
	 * @return the fromAddress
	 */
	public String getFromAddress() {
		return fromAddress;
	}
	/**
	 * @return the fromTag
	 */
	public String getFromTag() {
		return fromTag;
	}
	/**
	 * @return the toAddress
	 */
	public String getToAddress() {
		return toAddress;
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
		result = prime * result
				+ ((fromAddress == null) ? 0 : fromAddress.hashCode());
		result = prime * result + ((fromTag == null) ? 0 : fromTag.hashCode());
		result = prime * result
				+ ((toAddress == null) ? 0 : toAddress.hashCode());
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
		final SipSessionKey other = (SipSessionKey) obj;
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
		if (fromAddress == null) {
			if (other.fromAddress != null)
				return false;
		} else if (!fromAddress.equals(other.fromAddress))
			return false;
		if (fromTag == null) {
			if (other.fromTag != null)
				return false;
		} else if (!fromTag.equals(other.fromTag))
			return false;
		if (toAddress == null) {
			if (other.toAddress != null)
				return false;
		} else if (!toAddress.equals(other.toAddress))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer value = new StringBuffer();
		value = value.append("(");
		value = value.append(fromAddress);
		value = value.append(",");
		value = value.append(fromTag);
		value = value.append(",");
		value = value.append(toAddress);
		value = value.append(",");
		value = value.append(callId);
		value = value.append(",");
		value = value.append(applicationName);
		value = value.append(")");
		return value.toString();
	}
}
