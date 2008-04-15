/**
 * 
 */
package org.mobicents.servlet.sip.core.session;

/**
 * @author Jean Deruelle
 *
 */
public class SipApplicationSessionKey {
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
