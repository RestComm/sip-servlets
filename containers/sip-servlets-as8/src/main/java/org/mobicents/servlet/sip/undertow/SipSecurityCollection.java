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
package org.mobicents.servlet.sip.undertow;

import io.undertow.servlet.api.WebResourceCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Sip Security 
 * @author kakonyi.istvan@alerant.hu
 */
public class SipSecurityCollection extends WebResourceCollection {

	private static final long serialVersionUID = 1L;
	public List<String> servletNames = new ArrayList<String>();
	public List<String> sipMethods = new ArrayList<String>();

	private String name;
	private String description;

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
		this.name=name;
		this.description=description;
	}

	/**
	 * 
	 * @param name
	 */
	public SipSecurityCollection(String name) {
		this.name=name;
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

	public boolean findMethod(String method) {
		for(String declaredMethods:sipMethods) {
			if(method.equals(declaredMethods))
				return true;
		}
		return false;
	}

	public String[] findServletNames() {
		String[] ret = new String[servletNames.size()];
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}