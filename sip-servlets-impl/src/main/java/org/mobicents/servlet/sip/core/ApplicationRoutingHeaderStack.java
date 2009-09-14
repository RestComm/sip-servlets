package org.mobicents.servlet.sip.core;

import java.util.ArrayList;

/**
 * This class manipulates a string that contains the AR stack for cases when the container
 * behaves as UAC. The string looks like this:
 * appname1!handler1/appname2!handler2/appname3!handler3/.../...
 * 
 * @author vralev
 *
 */
public class ApplicationRoutingHeaderStack {
	
	public ApplicationRoutingHeaderStack(String text) {
		if(text == null) {
			return;
		}
		String[] txtNodes = text.split("/");
		for(String node:txtNodes) {
			String[] params = node.split("!");
			ApplicationRouterNode arNode = new ApplicationRouterNode(params[0], params[1]);
			this.nodes.add(arNode);
		}
	}
	
	public static class ApplicationRouterNode {
		public ApplicationRouterNode(String app, String handler) {
			this.application = app;
			this.handler = handler;
		}
		public String application;
		public String handler;
	}
	
	private ArrayList<ApplicationRouterNode> nodes = new ArrayList<ApplicationRouterNode>();
	
	public void addNode(ApplicationRouterNode node) {
		this.nodes.add(node);
	}
	
	public void removeLast() {
		if(this.nodes.size()<=0) return;
		this.nodes.remove(0);//this.nodes.size() - 1);
	}
	
	public ApplicationRouterNode getLast() {
		return this.nodes.get(0);//this.nodes.size()-1);
	}
	
	public String toString() {
		StringBuffer text = new StringBuffer();
		for(int q=0; q<this.nodes.size(); q++) {
			text.append(this.nodes.get(q).application).append("!").append(this.nodes.get(q).handler).append("/");
		}
		if(text.length()>0) {
			text.append(text.substring(0, text.length() - 1));
		}
		return text.toString();
	}

}
