package org.mobicents.servlet.sip.core;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class manipulates strings representing the AR stack for cases when the container
 * behaves as UAC. When the container acts as UAC the AR must be stored in the from-tag
 * of the outgoing request. The string looks like this:
 * uniqueValue!appname1!handler1!appname2!handler2!appname3!handler3!...!...
 * 
 * @author vralev
 *
 */
public class ApplicationRoutingHeaderComposer {
	
	private static Random random = new Random();
	private static final String TOKEN_SEPARATOR = "!";
	
	
	private static String randomString() {
		long randValue = random.nextLong() ^ System.currentTimeMillis();
		return String.valueOf(randValue);
	}
	
	private String uniqueValue;

	private ArrayList<ApplicationRouterNode> nodes = new ArrayList<ApplicationRouterNode>();
	
	public ApplicationRoutingHeaderComposer() {
		this(null);
	}
	
	public ApplicationRoutingHeaderComposer(String text) {
		if(text == null) {
			uniqueValue = randomString();
			return;
		}
		
		String[] txtNodes = text.split(TOKEN_SEPARATOR);
		
		// If there is no AR in the string, generate a uniqueValue for the tag
		// and it will be stored for later.
		if(txtNodes.length<=1) {
			uniqueValue = randomString();
			return;
		}
		
		// Otherwise extract the uniqueValue from the tag string, it's the first token.
		uniqueValue = txtNodes[0];
		for(int q = 1; q<txtNodes.length; q+=1) {
			ApplicationRouterNode arNode = new ApplicationRouterNode(txtNodes[q]);
			this.nodes.add(arNode);
		}
	}
	
	public static class ApplicationRouterNode {
		public ApplicationRouterNode(String app) {
			this.application = app;
		}
		public String application;
	}
	
	public void addNode(ApplicationRouterNode node) {
		this.nodes.add(node);
	}
	
	public void removeLast() {
		if(this.nodes.size()<=0) return;
		this.nodes.remove(0);
	}
	
	public ApplicationRouterNode getLast() {
		return this.nodes.get(0);
	}
	
	public String toString() {
		String text = uniqueValue + TOKEN_SEPARATOR;
		for(int q=0; q<this.nodes.size(); q++) {
			text += this.nodes.get(q).application + TOKEN_SEPARATOR;
		}
		if(text.length()>0)
			text = text.substring(0, text.length() - 1);
		return text;
	}

}
