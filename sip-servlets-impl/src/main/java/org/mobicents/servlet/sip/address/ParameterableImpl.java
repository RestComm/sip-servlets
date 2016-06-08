/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.address;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.Parameterable;
import javax.sip.header.Header;
import javax.sip.header.Parameters;
import javax.servlet.sip.SipSession;

import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;


/**
 * Implementation of the parameterable interface.
 * 
 * @author mranga
 * @author jean.deruelle@gmail.com
 *
 */

public abstract class ParameterableImpl implements Parameterable ,Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(ParameterableImpl.class.getCanonicalName());	
	private static final String PARAM_SEPARATOR = ";";
	private static final String PARAM_NAME_VALUE_SEPARATOR = "=";
	
	protected Map<String,String> parameters = new ConcurrentHashMap<String, String>();
	
	protected transient Parameters header = null;
	
	protected ModifiableRule isModifiable = ModifiableRule.Modifiable;
	
	protected SipSession sipSession;
	
	protected ParameterableImpl(SipSession sipSession) {
		this.parameters = new ConcurrentHashMap<String, String>();
		this.sipSession = sipSession;
	}
	
	/**
	 * Create parametrable instance.
	 * @param value - initial value of parametrable value
	 * @param parameters - parameter map - it can be null;
	 */
	public ParameterableImpl(Header header, Map<String, String> params, ModifiableRule isModifiable, SipSession sipSession) {
		this.isModifiable = isModifiable;
		this.sipSession = sipSession;
		if(header instanceof Parameters) {
			this.header = (Parameters) header;
		}
		if(params!=null) {			 
			Iterator<Map.Entry<String, String>> entries=params.entrySet().iterator(); 
			while (entries.hasNext()) {
				Map.Entry<String, String> e=entries.next();
				parameters.put(e.getKey(), e.getValue());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		if(name == null) {
			throw new NullPointerException("the parameter given in parameter is null !");
		}
		String value = this.parameters.get(name);
		if(value != null) {			
			return RFC2396UrlDecoder.decode(value);
		} else {
			if("lr".equals(name)) return "";// special case to pass Addressing spec test from 289 TCK
		}
		return null;
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
		if(isModifiable == ModifiableRule.NotModifiable) {
			throw new IllegalStateException("it is forbidden to modify the parameters");
		}
		if(name.equalsIgnoreCase("tag") && (isModifiable == ModifiableRule.From || isModifiable == ModifiableRule.To)) {
			throw new IllegalStateException("it is forbidden to remove the tag parameter on To or From Header");
		}
		if(name.equalsIgnoreCase("branch") && isModifiable == ModifiableRule.Via) {
			throw new IllegalStateException("it is forbidden to set the branch parameter on the Via Header");
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
		// Global Fix by alexvinn for Issue 1010 : Unable to set flag parameter to parameterable header 
		if(name == null) {
			throw new NullPointerException("parameter name is null ! ");
		}
		if(value == null) {
			removeParameter(name);
			return;
		}
		if(isModifiable == ModifiableRule.NotModifiable) {
			throw new IllegalStateException("it is forbidden to modify the parameters");
		}
		if(name.equalsIgnoreCase("tag") && (isModifiable == ModifiableRule.From || isModifiable == ModifiableRule.To)) {
			throw new IllegalStateException("it is forbidden to set the tag parameter on To or From Header");
		}
		if(name.equalsIgnoreCase("branch") && isModifiable == ModifiableRule.Via) {
			throw new IllegalStateException("it is forbidden to set the branch parameter on the Via Header");
		}
		//Fix from abondar for Issue 494 and angelo.marletta for Issue 502	  
		this.parameters.put(name.toLowerCase(), value);
		if(header != null) {
			try {
				if (isQuotableParameter(name, value)){
					header.setQuotedParameter(name, value);
				}
				else{
					header.setParameter(name, "".equals(value) ? null : value);
				}
			} catch (ParseException e) {
				throw new IllegalArgumentException("Problem setting parameter",e);
			}
		}
	}
	
	private static final String QUOTABLE_PARAMETER_NAME ="org.restcomm.servlets.sip.QUOTABLE_PARAMETER_NAME";
	/**
	 * Verify that the parameter is in the known list of parameters that its value need to be quoted
	 * 
	 * @return 
	 * true: if the Parameter name is present at servletContext and its value can be quoted, else return false
	 * otherwise
	 */
	protected boolean isQuotableParameter(String name, String value){
		if (sipSession != null && sipSession.getServletContext() != null){
			// get list of parameter names which are splited by comma.
			String quotableParamNames = sipSession.getServletContext().getInitParameter(QUOTABLE_PARAMETER_NAME);
			if (quotableParamNames != null){
				String[] paramNames = quotableParamNames.split(",");
				for (int i = 0; i < paramNames.length; i++){
					if (name.equalsIgnoreCase(paramNames[i])){
						if(isQuotableString(value)){
							return true;
						}
						break;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Verify that the string is quotable string base on RFC 3261
	 * 
	 * @return 
	 * true: if the Parameter value can be quoted base on condition which is stated in RFC 3261, else return false
	 * otherwise
	 */
	private final static char CR = 0x0D;
	private final static char LF = 0x0A;
	private final static char TAB = 0x09;
	private final static char VT = 0x0B;
	private final static char FF = 0x0C;
	private final static char SPACE = 0X20;
	private final static char DQUOTE = '"';
	private final static char BACKSLASH = '\\';
	
	protected boolean isQuotableString(String value){
		if (value.length() == 0){
			return true;
		}
		
		char c = value.charAt(0);
		for (int i = 0; i < value.length(); i++){
			
			if ((i - 1) >= 0){
				c = value.charAt(i - 1);
				if (c == BACKSLASH || c == CR || c == LF){
					// if previous is backslash, CR or LF, the current char is already check, skip this index.
					continue;
				}
			}
			
			c = value.charAt(i);
			
			if (c == BACKSLASH){
				if ((i + 1)>= value.length()){
					//Quotable String cannot be ended by an escape
					return false;
				}

				char c1 = value.charAt(i + 1);
				if(c1 == CR || c1 == LF){
					// CR, LF are not allowed to be escaped
					return false;
				}
			}
			else if (c == CR || c == LF){
				if ((i + 1) < value.length()){
					char c1 = value.charAt(i + 1);
					if (c1 == TAB||/***********************************/
						c1 == LF ||
						c1 == VT ||//  LINEAR WHITE SPACE
						c1 == FF ||
						c1 == CR ||
						c1 == SPACE){/************************************/
						return false;
					}
				}
			}
			else if (c == DQUOTE){
				// Double quotes must be escaped if they appear in a quotable
				// string
				return false;
			}
		}
		// the rest is quotable string
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameters()
	 */
	public Set<Entry<String, String>> getParameters() {
		Map<String,String> retval = new HashMap<String,String> ();
		for(Entry<String, String> nameValue : this.parameters.entrySet()) {
			retval.put(nameValue.getKey(), (RFC2396UrlDecoder.decode(nameValue.getValue())));
		}
		return retval.entrySet();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getParameters()
	 */
	public Map<String, String> getInternalParameters() {
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer retVal = new StringBuffer();
		boolean firstTime = true;
		for(java.util.Map.Entry<String, String> entry : parameters.entrySet()) {			
			if(!firstTime) {
				retVal.append(PARAM_SEPARATOR);				
			}
			firstTime = false;
			String value = entry.getValue();
			if(value != null && value.length() > 0) {
				retVal.append(entry.getKey()).append(PARAM_NAME_VALUE_SEPARATOR).append(value);
			} else {
				retVal.append(entry.getKey());
			}
		}
		return retVal.toString();
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		if(header != null) {			
			for(Entry<String, String> nameValue : this.parameters.entrySet()) {
				try {
					header.setParameter(nameValue.getKey(), nameValue.getValue());
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
