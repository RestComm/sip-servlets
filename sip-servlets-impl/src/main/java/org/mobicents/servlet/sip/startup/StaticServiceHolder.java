package org.mobicents.servlet.sip.startup;

/**
 * There can be only one tomcat service per SAR classloader, so it can be a static safely.
 * No need for get/set, because it's set just once on init and read many times after that.
 * This here is used for deserialization of Session references where, we are at not particular app
 * and do not have any context.
 * 
 * @author vralev
 *
 */
public class StaticServiceHolder {
	public static SipStandardService sipStandardService;
}
