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

import java.util.Random;

import org.apache.log4j.Logger;

/**
 * This class manipulates strings representing the AR stack for cases when the container
 * behaves as UAC. When the container acts as UAC the AR must be stored in the from-tag
 * of the outgoing request.When the container acts as UAS/B2BUA the AR must be stored in the to-tag
 * of the outgoing request. The string looks like this:
 * uniqueValue_appname1_appGeneratedApplicationSessionId
 * 
 * @author vralev
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ApplicationRoutingHeaderComposer {
	private static final Logger logger = Logger.getLogger(ApplicationRoutingHeaderComposer.class
			.getCanonicalName());
	
	private static final Random RANDOM = new Random();
	private static final String TOKEN_SEPARATOR = "_";
	
	private final static String reduceRandomValue(String str, int maxChars) {
		int len = str.length();
		if(len > maxChars) {
			return str.substring(len-maxChars, len);
		} else {
			return str;
		}
	}
	public final static String randomString() {
		long randValue = Math.abs(RANDOM.nextInt(1211111) ^ System.nanoTime());
		return reduceRandomValue(String.valueOf(randValue), 8);
	}

	
	public final static String[] getAppNameAndSessionId(SipApplicationDispatcher sipApplicationDispatcher, String text) {
		String[] tuple = new String[2];
		if(text != null) {					
			final String[] tokens = text.split(TOKEN_SEPARATOR);
		
			// If there is no AR in the string, generate a uniqueValue for the tag
			// and it will be stored for later.
			if(tokens.length > 1) {				
				// Otherwise extract the uniqueValue from the tag string, it's the first token.
				final String hashedAppName = tokens[1];
				String appName = sipApplicationDispatcher.getApplicationNameFromHash(hashedAppName);
				if(appName == null) 
					throw new IllegalArgumentException("The hash doesn't correspond to any app name: " + hashedAppName);
				tuple[0] = appName;				
				tuple[1] = tokens[2];
			}
		}		
		return tuple;
	}		
	
	public final static String getHash(SipApplicationDispatcher sipApplicationDispatcher, String  applicationName,  String applicationId) {
		String text = randomString() + TOKEN_SEPARATOR;
		text += sipApplicationDispatcher.getHashFromApplicationName(applicationName);
		if(applicationId != null && applicationId.length() > 0) {
			text = text + TOKEN_SEPARATOR + applicationId; 
		}
		if(logger.isDebugEnabled()) {
			logger.debug("tag will be equal to " + text);
		}
		return text;
	}

}
