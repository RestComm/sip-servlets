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

package org.mobicents.servlet.sip.router;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;

/**
 * This class contains one information parsed in the dar configuration file.
 * Example : ("OriginatingCallWaiting", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
 * @author Jean Deruelle
 *
 */
public class DefaultSipApplicationRouterInfo extends SipApplicationRouterInfo {
//	private String applicationName;
//	private SipApplicationRoutingRegion routingRegion;
//	private String subscriberIdentity;	
//	private String[] routes;
//	private SipRouteModifier routeModifier;
	private int order;
	private Map<String, String> optionalParameters;
			
	
	/**
	 * @param applicationName
	 * @param subscriberIdentity
	 * @param routingRegionType
	 * @param route
	 * @param routeModifier
	 * @param order
	 */
	public DefaultSipApplicationRouterInfo(String applicationName,
			String subscriberIdentity,
			SipApplicationRoutingRegion routingRegion, String[] routes,
			SipRouteModifier routeModifier, int order, String optionalParameters) {
		super(applicationName, routingRegion, subscriberIdentity, routes, routeModifier, null);
//		this.applicationName = applicationName;
//		this.subscriberIdentity = subscriberIdentity;
//		this.routingRegion = routingRegion;
//		this.routes = routes;
//		this.routeModifier = routeModifier;
		this.order = order;
		try {
			this.optionalParameters = stringToMap(optionalParameters);
		} catch (ParseException e) {
			throw new RuntimeException("Error", e);
		}
		
	}
	
	public static Map<String, String> stringToMap(String str) throws ParseException {
		
		Map<String, String> map = new HashMap<String, String>();
		if(str == null) return map;
		String[] props = str.split(" ");
		for(String prop : props) {
			if(prop.equals("") || prop.equals(" ")) continue;
			
			int indexOfEq = prop.indexOf('=');
			if(indexOfEq == -1) {
				throw new RuntimeException("Expected '=' sign in the optional Parameters");
			}
			
			String key = prop.substring(0, indexOfEq);
			String value = prop.substring(indexOfEq + 1);
			map.put(key, value);
		}
		return map;
	}
	
	public static String mapToString(Map<String, String> map) {
		StringBuilder str = new StringBuilder("");
		Set<Entry<String, String>> entries = map.entrySet();
		for(Entry<String, String> entry : entries) {
			str.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
		}
		return str.toString();
	}
	
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return getNextApplicationName();
	}
	
	/**
	 * @return the subscriberIdentity
	 */
	public String getSubscriberIdentity() {
		return getSubscriberURI();
	}
	
	
	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setStateInfo(int stateInfo) {
		this.order = stateInfo;
	}
	
	/**
	 * 
	 * @return optional params as a string (command separated key=value pairs)
	 */
	public Map<String, String> getOptionalParameters() {
		return optionalParameters;
	}
	
	/**
	 * 
	 * @param optionalParameters
	 */
	public void setOptionalParameters(HashMap<String, String> optionalParameters) {
		this.optionalParameters = optionalParameters;
	}
}
