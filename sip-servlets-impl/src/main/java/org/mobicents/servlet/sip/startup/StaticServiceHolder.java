package org.mobicents.servlet.sip.startup;

import gov.nist.javax.sip.stack.SIPTransaction;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

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
	public static Method disableRetransmissionTimer; 
	private static final Logger logger = Logger.getLogger(StaticServiceHolder.class);
	static {
		try {
			disableRetransmissionTimer = SIPTransaction.class.getDeclaredMethod("disableRetransmissionTimer");
		} catch (Exception e) {
			logger.error("Error with reflection for disableRetransmissionTimer", e);
		}
		disableRetransmissionTimer.setAccessible(true);
	}
}
