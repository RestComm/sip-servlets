package org.mobicents.servlet.management.client.router;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DARRouteNode implements IsSerializable{
	private String application;
	private String subscriber;
	private String routingRegion;
	private String routeModifier;
	private String order;
	private String route;
	private String direction;
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getSubscriber() {
		return subscriber;
	}
	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber;
	}
	public String getRoutingRegion() {
		return routingRegion;
	}
	public void setRoutingRegion(String routingRegion) {
		this.routingRegion = routingRegion;
	}
	public String getRouteModifier() {
		return routeModifier;
	}
	public void setRouteModifier(String routeModifier) {
		this.routeModifier = routeModifier;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getRoute() {
		return route;
	}
	public void setRoute(String route) {
		this.route = route;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
}
