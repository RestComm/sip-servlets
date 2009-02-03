/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.ipbx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * This class correspond to the location of a sip phone for a given registration. 
 * Example, for a REGISTER with a From: sip:jean@mobicents.org header and a Contact: sip:jean@127.0.0.1:5060 headers
 * The registration is the URI of the FROM header and the RegistrationLocation is the URI of the Contact Header
 * 
 * @author jean.deruelle@gmail.com
 *
 */

@Entity
@Table(name="BINDINGS")
public class Binding {	
	private long id;
	private Integer version;
	
	private String contactAddress;
	private String callId;
	private int cSeq;
	private int expires;
	private Registration registration;
		      
	/**
	 * @param cseq the cseq to set
	 */
	public void setCSeq(int cSeq) {
		this.cSeq = cSeq;
	}
	/**
	 * @return the cseq
	 */
	public int getCSeq() {
		return cSeq;
	}
	/**
	 * @param callId the callId to set
	 */
	public void setCallId(String callId) {
		this.callId = callId;
	}
	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}
	/**
	 * @param contactAddress the contactAddress to set
	 */
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}
	/**
	 * @return the contactAddress
	 */
	public String getContactAddress() {
		return contactAddress;
	}
	/**
	 * @param expires the expires to set
	 */
	public void setExpires(int expires) {
		this.expires = expires;
	}
	/**
	 * @return the expires
	 */
	public int getExpires() {
		return expires;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	@Id @GeneratedValue
	@Column(name="BINDINGID")
	public long getId() {
		return id;
	}
	
	@Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

	/**
	 * @param registration the registration to set
	 */
	public void setRegistration(Registration registration) {
		this.registration = registration;
	}
	/**
	 * @return the registration
	 */
	@ManyToOne
	@JoinColumn(name="REGISTRATIONID")
	public Registration getRegistration() {
		return registration;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cSeq;
		result = prime * result + ((callId == null) ? 0 : callId.hashCode());
		result = prime * result
				+ ((contactAddress == null) ? 0 : contactAddress.hashCode());
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
		Binding other = (Binding) obj;
		if (cSeq != other.cSeq)
			return false;
		if (callId == null) {
			if (other.callId != null)
				return false;
		} else if (!callId.equals(other.callId))
			return false;
		if (contactAddress == null) {
			if (other.contactAddress != null)
				return false;
		} else if (!contactAddress.equals(other.contactAddress))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Binding [Address of record:");
		sb.append(registration.getUri());
		sb.append(", Contact Address :");
		sb.append(contactAddress);
		sb.append(", Expires :");
		sb.append(expires);
		sb.append(", call Id :");
		sb.append(callId);
		sb.append(", cseq :");
		sb.append(cSeq);		
		return sb.toString();
	}
	
}
