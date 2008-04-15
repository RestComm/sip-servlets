package org.mobicents.servlet.sip;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

public class SipFactories {
	private static Log logger = LogFactory.getLog(SipFactories.class.getCanonicalName());

	private static boolean initialized;

	public static AddressFactory addressFactory;

	public static HeaderFactory headerFactory;

	public static SipFactory sipFactory;

	public static MessageFactory messageFactory;

	public static void initialize(String pathName) {
		if (!initialized) {
			try {
				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName(pathName);
				addressFactory = sipFactory.createAddressFactory();
				headerFactory = sipFactory.createHeaderFactory();
				messageFactory = sipFactory.createMessageFactory();
				initialized = true;
			} catch (Exception ex) {
				logger.error("Could not instantiate factories -- exitting", ex);
				throw new RuntimeException("Cannot instantiate factories ", ex);
			}
		}
	}
}
