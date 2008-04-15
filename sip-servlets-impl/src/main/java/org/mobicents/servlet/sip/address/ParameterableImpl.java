/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	public ParameterableImpl(Map params) {
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
