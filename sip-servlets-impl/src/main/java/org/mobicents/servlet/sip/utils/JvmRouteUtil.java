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

package org.mobicents.servlet.sip.utils;

/**
 * Cluster-enables app servers append the jvmRoute string to the jsessionid like "dKd86*gdygs8fd.node1",
 * where "dKd86*gdygs8fd" is the session id and "node1" is the JvmRoute. These utils just remove the 
 * jvmRoute. jvmROute is the ,machines id of the machine where the LB has landed the http session first.
 * 
 * @author vralev
 *
 */
public class JvmRouteUtil {
	public static String removeJvmRoute(String sid) {
		int dotIndex = sid.indexOf(".");
		if(dotIndex<0) return sid;
		return sid.substring(0, dotIndex);
	}
	
	public static String removeJvmRoute(Object sid) {
		return removeJvmRoute((String) sid);
	}
	
	public static String extractJvmRoute(String sid) {
		int dotIndex = sid.indexOf(".");
		if(dotIndex<0) return null;
		return sid.substring(dotIndex + 1);
	}
}
