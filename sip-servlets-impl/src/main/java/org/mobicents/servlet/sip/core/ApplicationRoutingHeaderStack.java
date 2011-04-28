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
