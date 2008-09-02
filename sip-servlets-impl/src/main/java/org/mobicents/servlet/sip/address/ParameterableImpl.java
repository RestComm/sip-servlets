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

import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.sip.Parameterable;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the parameterable interface.
 * 
 * @author mranga
 *
 */

public abstract class ParameterableImpl implements Parameterable ,Cloneable{
	protected NameValueList parameters = new NameValueList();
	
	protected Parameters header = null;
	
	protected static final Log logger= LogFactory.getLog(ParameterableImpl.class.getCanonicalName());
	protected transient boolean isModifiable = true;
	
	protected ParameterableImpl() {
		this.parameters = new NameValueList();	
	}
	
	/**
	 * Create parametrable instance.
	 * @param value - initial value of parametrable value
	 * @param parameters - parameter map - it can be null;
	 */
	public ParameterableImpl(Header header, Map<String, String> params, boolean isModifiable) {
		this.isModifiable = isModifiable;
		if(header instanceof Parameters) {
			this.header = (Parameters) header;
			if(params!=null) {			 
				Iterator<Map.Entry<String, String>> entries=params.entrySet().iterator(); 
				while (entries.hasNext()) {
					Map.Entry<String, String> e=entries.next();
					parameters.put(e.getKey(), new NameValue(e.getKey(),e.getValue()));
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return this.parameters.get(name) != null ?
				RFC2396UrlDecoder.decode(this.parameters.get(name).getValue()): null	;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameterNames()
	 */
	public Iterator<String> getParameterNames() {
			return this.parameters.keySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#removeParameter(java.lang.String)
	 */
	public void removeParameter(String name) {
		if(name == null) {
			throw new NullPointerException("parameter name is null ! ");
		}
		if(!isModifiable) {
			throw new IllegalStateException("it is forbidden to modify the parameters");
		}
		this.parameters.remove(name);
		if(header != null) {
			header.removeParameter(name);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		if(name == null) {
			throw new NullPointerException("parameter name is null ! ");
		}
		if(value == null) {
			throw new NullPointerException("parameter value is null ! ");
		}
		if(!isModifiable) {
			throw new IllegalStateException("it is forbidden to modify the parameters");
		}
		this.parameters.put(name,new NameValue(name,value));
		if(header != null) {
			try {
				header.setParameter(name, value);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Problem setting parameter",e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameters()
	 */
	public Set<Entry<String, String>> getParameters() {
		HashSet<Entry<String,String>> retval = new HashSet<Entry<String,String>> ();
		for(NameValue nameValue : this.parameters.values()) {
			nameValue.setValue(RFC2396UrlDecoder.decode(nameValue.getValue()));
			retval.add(nameValue);
		}
		return retval;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameters()
	 */
	public NameValueList getInternalParameters() {
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.parameters.toString();
	}
	
	public void setParameters(NameValueList parameters) {
		this.parameters = parameters;
		if(header != null) {
			Iterator<NameValue> it = parameters.iterator();
			while (it.hasNext()) {
				NameValue nameValue = (NameValue) it.next();			
				try {
					header.setParameter(nameValue.getName(), nameValue.getValue());
				} catch (ParseException e) {
					throw new IllegalArgumentException("Problem setting parameter",e);
				}
			}
		}
	}
	
	public abstract Object clone();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
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
		ParameterableImpl other = (ParameterableImpl) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}
	
	
	
}
