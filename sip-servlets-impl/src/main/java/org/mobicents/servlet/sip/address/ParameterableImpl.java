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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.sip.Parameterable;

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
	
	protected static final Log logger= LogFactory.getLog(ParameterableImpl.class.getCanonicalName());
	
	protected ParameterableImpl() {
		this.parameters = new NameValueList();	
	}
	
	/**
	 * Create parametrable instance.
	 * @param value - initial value of parametrable value
	 * @param parameters - parameter map - it can be null;
	 */
	public ParameterableImpl(Map<String, String> params) {
		 if(params!=null) {			 
			 Iterator<Map.Entry<String, String>> entries=params.entrySet().iterator(); 
			 while(entries.hasNext()) {
				 Map.Entry<String, String> e=entries.next();
				 parameters.put(e.getKey(), new NameValue(e.getKey(),e.getValue()));
			 }
		 }
	}
	
	public String getParameter(String name) {
		return this.parameters.get(name) != null ?
				this.parameters.get(name).getValue(): null	;
	}

	public Iterator<String> getParameterNames() {
			return this.parameters.keySet().iterator();
	}

	public void removeParameter(String name) {
			this.parameters.remove(name);
	}

	public void setParameter(String name, String value) {
		this.parameters.put(name,new NameValue(name,value));
	}

	public Set<Entry<String, String>> getParameters() {
		HashSet<Entry<String,String>> retval = new HashSet<Entry<String,String>> ();
		retval.addAll(this.parameters.values());
		return retval;
	}

	public String toString() {
		return this.parameters.toString();
	}

	public void setParameters(NameValueList parameters) {
		this.parameters = parameters;
	}
	
	public abstract Object clone();
}
