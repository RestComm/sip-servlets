package org.mobicents.servlet.sip.utils;

/**
 * Cluster-enables app servers append the jvmRoute string to the jsessionid like "dKd86*gdygs8fd.node1",
 * where "dKd86*gdygs8fd" is the session id and "node1" is the JvmRoute. These utils just remove the 
 * jvmRoute. jvmROute is the ,machines id of the machine where the LB has landed the http session first.
 * 
 * @author vralev
 *
 */
public class JvmRouteUtil {
	public static String removeJvmRoute(String sid) {
		int dotIndex = sid.indexOf(".");
		if(dotIndex<0) return sid;
		return sid.substring(0, dotIndex);
	}
	
	public static String removeJvmRoute(Object sid) {
		return removeJvmRoute((String) sid);
	}
	
	public static String extractJvmRoute(String sid) {
		int dotIndex = sid.indexOf(".");
		if(dotIndex<0) return null;
		return sid.substring(dotIndex + 1);
	}
}
