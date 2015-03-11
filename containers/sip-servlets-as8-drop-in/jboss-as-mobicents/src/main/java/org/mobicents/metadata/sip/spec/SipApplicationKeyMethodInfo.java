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
package org.mobicents.metadata.sip.spec;

/**
 * Data to fetch the SipApplicationKey method in the proper class loader context.
 * No need to store the args and return type, they are always the same (String, SipServletRequest)
 *
 * @author josemrecio@gmail.com
 */
public class SipApplicationKeyMethodInfo {
	
	final String className;
	final String methodName;
	
	public SipApplicationKeyMethodInfo(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}
	
	public String toString() {
		return new StringBuilder(className).append("-").append(methodName).toString();
	}
}