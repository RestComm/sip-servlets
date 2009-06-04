package org.mobicents.servlet.sip.core;

import java.util.Random;

import org.apache.log4j.Logger;

/**
 * This class manipulates strings representing the AR stack for cases when the container
 * behaves as UAC. When the container acts as UAC the AR must be stored in the from-tag
 * of the outgoing request. The string looks like this:
 * uniqueValue_appname1 and an optional part _appGeneratedApplicationSessionId
 * 
 * @author vralev
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ApplicationRoutingHeaderComposer {
	private static transient Logger logger = Logger.getLogger(ApplicationRoutingHeaderComposer.class
			.getCanonicalName());
	
	private static Random random = new Random();
	private static final String TOKEN_SEPARATOR = "_";
	
	private static String reduceRandomValue(String str, int maxChars) {
		int len = str.length();
		if(len > maxChars) {
			return str.substring(len-maxChars, len);
		} else {
			return str;
		}
	}
	private static String randomString() {
		long randValue = Math.abs(random.nextInt(1211111) ^ System.nanoTime());
		return reduceRandomValue(String.valueOf(randValue), 8);
	}
	
	private String uniqueValue;
	
	private SipApplicationDispatcher sipApplicationDispatcher = null;

	private String applicationName = null;
	
	private String appGeneratedApplicationSessionId = null;	

	public ApplicationRoutingHeaderComposer(SipApplicationDispatcher sipApplicationDispatcher) {
		this(sipApplicationDispatcher, null);
	}
	
	public ApplicationRoutingHeaderComposer(SipApplicationDispatcher sipApplicationDispatcher, String text) {
		this.sipApplicationDispatcher = sipApplicationDispatcher;
		
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
		String hashedAppName = tokens[1];
		String appName = sipApplicationDispatcher.getApplicationNameFromHash(hashedAppName);
		if(appName == null) 
			throw new NullPointerException("The hash doesn't correspond to any app name: " + hashedAppName);
		applicationName = appName;
		if(tokens.length > 2) {
			appGeneratedApplicationSessionId = tokens[2];
		}
	}
	
	public void setApplicationName(String applicationName) {
		this.applicationName = sipApplicationDispatcher.getHashFromApplicationName(applicationName);
	}
	
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * @return the appGeneratedApplicationSessionId
	 */
	public String getAppGeneratedApplicationSessionId() {
		return appGeneratedApplicationSessionId;
	}

	/**
	 * @param appGeneratedApplicationSessionId the appGeneratedApplicationSessionId to set
	 */
	public void setAppGeneratedApplicationSessionId(
			String appGeneratedApplicationSessionId) {
		this.appGeneratedApplicationSessionId = appGeneratedApplicationSessionId;
	}
	
	public String toString() {
		String text = uniqueValue + TOKEN_SEPARATOR;
		text += this.applicationName;
		if(appGeneratedApplicationSessionId != null && appGeneratedApplicationSessionId.length() > 0) {
			text = text + TOKEN_SEPARATOR + appGeneratedApplicationSessionId; 
		}
		if(logger.isDebugEnabled()) {
			logger.debug("tag will be equal to " + text);
		}
		return text;
	}

}
