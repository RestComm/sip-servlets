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

/**
 * Route modifiers as returned by the Application Router, used to interpret the returned route from the router.
 * @since 1.1
 *
 */
public enum SipRouteModifier{
	/**
	 * Tells the container to push a route back to itself before pusing the external routes specified by SipApplicationRouterInfo.getRoutes().
	 */
	ROUTE_BACK,
	/**
	 * Indicates that SipApplicationRouterInfo.getRoute() does not contain any valid route.
	 */	
	NO_ROUTE,
	/**
	 * Indicates that the route returned by SipApplicationRouterInfo.getRoute() is a valid route.
	 */
	ROUTE;
}
