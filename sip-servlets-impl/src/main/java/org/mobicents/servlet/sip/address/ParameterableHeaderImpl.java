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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.header.Header;
import javax.sip.header.Parameters;

/**
 * This class is impl of Parameterable object returned
 * 
 * @author baranowb
 * 
 */
public class ParameterableHeaderImpl extends ParameterableImpl {
	private static final long serialVersionUID = 1L;
	protected String value = null;
	
	public ParameterableHeaderImpl() {
		super();
	}

	public ParameterableHeaderImpl(Header header, String value, Map<String, String> params, boolean isNotModifiable) {
		// General form of parametrable header
		super(header, params, !isNotModifiable);
		this.value = value;
	}

	@Override
	public Object clone() {
		ParameterableHeaderImpl cloned = new ParameterableHeaderImpl();
		cloned.parameters = cloneParameters(super.parameters);
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
		if(!isModifiable) {
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
	
	@Override
	public String toString() {
		String retVal = new String(value);
		if(retVal.trim().startsWith("<") && !retVal.trim().endsWith(">")) {
			retVal = retVal.concat(">");
		}
		if(!super.toString().trim().equals("")) {
			retVal = retVal.concat(";" + super.toString());
		}
		return retVal;
	}

	public static final Map<String, String> cloneParameters(Map<String, String> parametersToClone) {
		Map<String, String> params = new ConcurrentHashMap<String, String>();
		for(Entry<String, String> param : parametersToClone.entrySet()) {			
			params.put(param.getKey(), param.getValue());
		}
		return params;
	}
}
