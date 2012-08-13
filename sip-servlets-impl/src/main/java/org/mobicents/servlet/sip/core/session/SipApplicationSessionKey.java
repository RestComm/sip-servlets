/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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

package org.mobicents.servlet.sip.core.session;

import java.io.Serializable;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.GenericUtils;

/**
 * <p>
 * Class representing the key (which will also be its id) for a sip application session.<br/>
 * It is composed of a random UUID and the application Name.
 * </p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public final class SipApplicationSessionKey implements Serializable, MobicentsSipApplicationSessionKey {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SipApplicationSessionKey.class
			.getCanonicalName());
	private final String uuid;
	private final String appGeneratedKey;
	private final String applicationName;
	private String toString;
	
	/**
	 * @param id
	 * @param applicationName
	 */
	public SipApplicationSessionKey(String id, String applicationName, String appGeneratedKey) {
		super();
		this.appGeneratedKey = appGeneratedKey;
		this.applicationName = applicationName;
		// "While processing the initial request after selecting the application, the 
		// container MUST look for this annotated static method within the application. 
		// If found, the container MUST call the method to get the key and generate an 
		// application-session-id by appending some unique identifier
		if(appGeneratedKey != null) {
			// http://code.google.com/p/sipservlets/issues/detail?id=146 : @SipApplicationSessionKey usage can break replication
			// Hash the appGeneratedKey to make sure it always resolve to the same uuid and reset the uuid with it.			
			uuid = GenericUtils.hashString(appGeneratedKey);
			if(logger.isDebugEnabled()) {
				logger.debug("uuid for appGeneratedKey " + appGeneratedKey + " set to " + uuid);
			}
			toString = appGeneratedKey + SessionManagerUtil.SESSION_KEY_SEPARATOR + uuid + SessionManagerUtil.SESSION_KEY_SEPARATOR + applicationName;
		} else {
			if(id == null) {
				// Issue 1551 : SipApplicationSessionKey is not unique
				this.uuid = "" + UUID.randomUUID();
			} else {
				this.uuid = id;
			}					
			toString = uuid + SessionManagerUtil.SESSION_KEY_SEPARATOR + applicationName;
		}
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
//	public void setAppGeneratedKey(String appGeneratedKey) {
//		
//	}
	
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
