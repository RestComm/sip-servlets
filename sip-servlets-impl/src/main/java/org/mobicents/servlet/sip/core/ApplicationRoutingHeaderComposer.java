package org.mobicents.servlet.sip.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.mobicents.servlet.sip.GenericUtils;

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
	private static final String TOKEN_SEPARATOR = "_";
	
	private static String randomString() {
		long randValue = Math.abs(random.nextLong() ^ System.currentTimeMillis());
		return String.valueOf(randValue);
	}
	
	private String uniqueValue;
	
	private Map<String, String> mdToAppName = null;

	private ArrayList<ApplicationRouterNode> nodes = new ArrayList<ApplicationRouterNode>();
	
	public ApplicationRoutingHeaderComposer(Map<String, String> hashMap) {
		this(hashMap, null);
	}
	
	public ApplicationRoutingHeaderComposer(Map<String, String> hashMap, String text) {
		this.mdToAppName = hashMap;
		
		if(text == null) {
			uniqueValue = randomString();
			return;
		}
		
		String[] tokens = text.split(TOKEN_SEPARATOR);
		
		// If there is no AR in the string, generate a uniqueValue for the tag
		// and it will be stored for later.
		if(tokens.length<=1) {
			uniqueValue = randomString();
			return;
		}
		
		// Otherwise extract the uniqueValue from the tag string, it's the first token.
		uniqueValue = tokens[0];
		for(int q = 1; q<tokens.length; q+=1) {
			String hashedAppName = tokens[q];
			String appName = mdToAppName.get(hashedAppName);
			if(appName == null) 
				throw new NullPointerException("The hash doesn't correspond to any app name: " + hashedAppName);
			ApplicationRouterNode arNode = new ApplicationRouterNode(appName);
			this.nodes.add(arNode);
		}
	}
	
	public static class ApplicationRouterNode {
		private String application;
		
		public ApplicationRouterNode(String app) {
			this.application = app;
		}
		
		public String toString() {
			return GenericUtils.hashString(this.application);
		}
		
		public String getApplication() {
			return this.application;
		}
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
			text += this.nodes.get(q).toString() + TOKEN_SEPARATOR;
		}
		if(text.length()>0)
			text = text.substring(0, text.length() - TOKEN_SEPARATOR.length());
		return text;
	}

}
