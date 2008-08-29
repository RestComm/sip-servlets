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
package org.mobicents.servlet.sip.address;

import gov.nist.core.NameValueList;

import java.util.Map;

import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;

/**
 * This class is impl of Parameterable object returned
 * 
 * @author baranowb
 * 
 */
public class ParameterableHeaderImpl extends ParameterableImpl {

	protected String value = null;
	//do not copy it during the clone that would be wrong
	protected boolean isNotModifiable = false;
	
	public ParameterableHeaderImpl() {
		super();
	}

	public ParameterableHeaderImpl(Header header, String value, Map<String, String> params, boolean isNotModifiable) {
		// General form of parametrable header
		super(header, params);
		this.value = value;
		this.isNotModifiable = isNotModifiable;
	}

	@Override
	public Object clone() {
		ParameterableHeaderImpl cloned = new ParameterableHeaderImpl();
		cloned.parameters = (NameValueList) super.parameters.clone();
		cloned.value = new String(this.value);
		cloned.header = (Parameters)((Header)super.header).clone();
		return cloned;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		if(value == null) {
			throw new NullPointerException("value is null ! ");
		}
		if(isNotModifiable) {
			throw new IllegalStateException("it is forbidden for an application to set the From Header");
		}
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ParameterableHeaderImpl other = (ParameterableHeaderImpl) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
