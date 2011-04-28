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

package javax.servlet.sip.ar;

import java.io.Serializable;

/**
 * The SipApplicationRouterInfo class encapsulates the different pieces of
 * information that the application router returns to the container when the
 * container calls the SipApplicationRouter.getNextApplication() method.
 * 
 * @since 1.1
 */
public class SipApplicationRouterInfo {
	private String nextApplicationName = null;

	private SipApplicationRoutingRegion routingRegion = null;
	
	private String subscriberURI = null;

	private String[] routes = null;

	private SipRouteModifier mod = null;

	private Serializable stateInfo = null;

	/**
	 * Creates a SipApplicationRouterInfo object containing the information
	 * necessary for the conatiner to perform its routing decision.
	 * 
	 * @param nextApplicationName
	 *            The name of the application that the application router
	 *            selects to service this request. If no further application is
	 *            needed in the current region, this is set to null.
	 * @param subscriberURI
	 *            The URI that the application is selected to serve
	 * @param route
	 *            The route header that could either be an external or internal
	 *            route, the internal route replaces the route popped by the
	 *            container
	 * @param mod
	 *            An enum modifier which qualifies the route returned and the
	 *            router behavior
	 * @param stateInfo
	 *            Arbitrary state information of the application router that it
	 *            wishes the container to store on its behalf
	 */
	public SipApplicationRouterInfo(
			java.lang.String nextApplicationName,
			SipApplicationRoutingRegion routingRegion,
			java.lang.String subscriberURI, 
			java.lang.String[] routes,
			javax.servlet.sip.ar.SipRouteModifier mod,
			java.io.Serializable stateInfo) {
		this.nextApplicationName = nextApplicationName;
		this.routingRegion = routingRegion;
		this.subscriberURI = subscriberURI;
		this.routes = routes;
		this.mod = mod;
		this.stateInfo = stateInfo;
	}

	public java.lang.String getNextApplicationName() {
		return nextApplicationName;
	}

	/**
	 * An array of SIP routes of the same type (internal or external). 
	 * If the top (first) is external, they are to be used by the container to route the request to the external entities. 
	 * The container pushes the external routes onto the request by iterating over 
	 * the array starting with the last element until the top (first) element, inclusive.
	 * If the top (first) route is internal, it indicates the route which led the request to the container. 
	 * This internal route is not used for any routing purposes but to let application router 
	 * potentially modify the route popped by the container. 
	 * Only the first internal route from the array is used for this purpose, the rest (if any) are ignored by the container.
	 * @return  The SIP route headers which could be internal or external. An empty array is returned when no routes are present. 
	 */
	public java.lang.String[] getRoutes() {
		return routes;
	}

	public javax.servlet.sip.ar.SipRouteModifier getRouteModifier() {
		return mod;
	}

	public javax.servlet.sip.ar.SipApplicationRoutingRegion getRoutingRegion() {
		return routingRegion;
	}

	public java.io.Serializable getStateInfo() {
		return stateInfo;
	}

	public java.lang.String getSubscriberURI() {
		return subscriberURI;
	}

}
