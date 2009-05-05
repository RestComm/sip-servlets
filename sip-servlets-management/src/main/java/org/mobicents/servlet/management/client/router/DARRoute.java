package org.mobicents.servlet.management.client.router;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DARRoute implements IsSerializable{
	private String request; // INVITE SUBSCRIBE etc
	private DARRouteNode[] nodes;
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {		
		this.request = request;				
	}
	public DARRouteNode[] getNodes() {
		return nodes;
	}
	public void setNodes(DARRouteNode[] nodes) {
		this.nodes = nodes;
	}
}
