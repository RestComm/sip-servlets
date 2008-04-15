/**
 * 
 */
package org.mobicents.servlet.sip.router;

import javax.servlet.sip.SipApplicationRoutingRegion;
import javax.servlet.sip.SipRouteModifier;

/**
 * This class contains one information parsed in the dar configuration file.
 * Example : ("OriginatingCallWaiting", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
 * @author Jean Deruelle
 *
 */
public class DefaultSipApplicationRouterInfo {
	private String applicationName;
	private String subscriberIdentity;
	private SipApplicationRoutingRegion routingRegion;
	private String route;
	private SipRouteModifier routeModifier;
	private int order;
			
	/**
	 * 
	 */
	public DefaultSipApplicationRouterInfo() {
		super();
	}
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
			SipApplicationRoutingRegion routingRegion, String route,
			SipRouteModifier routeModifier, int order) {
		super();
		this.applicationName = applicationName;
		this.subscriberIdentity = subscriberIdentity;
		this.routingRegion = routingRegion;
		this.route = route;
		this.routeModifier = routeModifier;
		this.order = order;
	}
	
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @param applicationName the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/**
	 * @return the subscriberIdentity
	 */
	public String getSubscriberIdentity() {
		return subscriberIdentity;
	}
	/**
	 * @param subscriberIdentity the subscriberIdentity to set
	 */
	public void setSubscriberIdentity(String subscriberIdentity) {
		this.subscriberIdentity = subscriberIdentity;
	}
	/**
	 * @return the routingRegionType
	 */
	public SipApplicationRoutingRegion getRoutingRegion() {
		return routingRegion;
	}
	/**
	 * @param routingRegionType the routingRegionType to set
	 */
	public void setRoutingRegionType(SipApplicationRoutingRegion routingRegion) {
		this.routingRegion = routingRegion;
	}
	/**
	 * @return the route
	 */
	public String getRoute() {
		return route;
	}
	/**
	 * @param route the route to set
	 */
	public void setRoute(String route) {
		this.route = route;
	}
	/**
	 * @return the routeModifier
	 */
	public SipRouteModifier getRouteModifier() {
		return routeModifier;
	}
	/**
	 * @param routeModifier the routeModifier to set
	 */
	public void setRouteModifier(SipRouteModifier routeModifier) {
		this.routeModifier = routeModifier;
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
}
