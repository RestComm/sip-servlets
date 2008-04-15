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
package org.mobicents.servlet.sip.startup.loading;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.deploy.SecurityCollection;

public class SipSecurityCollection extends SecurityCollection {
	public List<String> servletNames = new ArrayList<String>();
	public List<String> sipMethods = new ArrayList<String>();
		
	
	/**
	 * 
	 */
	public SipSecurityCollection() {
		super();	
	}
	
	/**
	 * 
	 * @param name
	 * @param description
	 */
	public SipSecurityCollection(String name, String description) {
		super(name, description);
	}
	/**
	 * 
	 * @param name
	 */
	public SipSecurityCollection(String name) {
		super(name);
	}
	
	/**
	 * 
	 * @param servletName
	 */
	public void addServletName(String servletName) {
		if(servletName == null) {
			return;
		}
		servletNames.add(servletName);
	}
	
	/**
	 * 
	 * @param servletName
	 */
	public void removeServletName(String servletName) {
		if(servletName == null) {
			return;
		}
		servletNames.remove(servletName);
	}
	
	public boolean findServletName(String servletName) {
		for(String servletNameFromList:servletNames) {
			if(servletNameFromList.equals(servletName))
				return true;
		}
		return false;
	}
	
	public String[] findServletNames() {
		String[] ret = null;
		servletNames.toArray(ret);
		return ret;
	}
	
	/**
	 * 
	 * @param sipMethod
	 */
	public void addSipMethod(String sipMethod) {
		if(sipMethod == null) {
			return;
		}
		sipMethods.add(sipMethod);
	}
	
	/**
	 * 
	 * @param sipMethod
	 */
	public void removeSipMethod(String sipMethod) {
		if(sipMethod == null) {
			return;
		}
		sipMethods.remove(sipMethod);
	}
}
