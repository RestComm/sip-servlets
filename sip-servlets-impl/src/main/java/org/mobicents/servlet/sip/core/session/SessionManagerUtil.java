/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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

package org.mobicents.servlet.sip.core.session;

import java.text.ParseException;
import java.util.StringTokenizer;

import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;

import org.apache.log4j.Logger;

/**
 * This class is used as a central place to get a session be it a sip session
 * or an sip application session. 
 * Here are the semantics of the key used for storing each kind of session :
 * <ul>
 * <li>sip session id = (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID,APPNAME)</li>
 * <li>sip app session id = (CALL-ID,APPNAME)<li>
 * </ul>
 * It is not possible to create a sip session or an application session directly,
 * this class should be used to get a session whatever its type. If the session 
 * already exsits it will be returned otherwise it will be created. 
 * One should be expected to remove the sessions from this manager through the
 * remove methods when the sessions are no longer used.
 */
public class SessionManagerUtil {
	private static final Logger logger = Logger.getLogger(SessionManagerUtil.class);
	
	public final static String TAG_PARAMETER_NAME = "tag";
	public final static String SESSION_KEY_SEPARATOR = ":";
	
	/**
	 * Computes the sip session key from the input parameters. The sip session
	 * key will be of the form (FROM-ADDR,FROM-TAG,TO-ADDR,CALL-ID,APPNAME)
	 * @param applicationName the name of the application that will be the fifth component of the key
	 * @param message the message to get the 4 components of the key from 
	 * @param inverted TODO
	 * @return the computed key 
	 * @throws NullPointerException if application name is null
	 */
	public static SipSessionKey getSipSessionKey(final String applicationSessionId, final String applicationName, final Message message, boolean inverted) {		
		if(applicationName == null) {
			throw new NullPointerException("the application name cannot be null for sip session key creation");
		}
		final ToHeader to = (ToHeader) message.getHeader(ToHeader.NAME);
		final FromHeader from = (FromHeader) message.getHeader(FromHeader.NAME); 
//		final URI toUri = to.getAddress().getURI();
//		final URI fromUri = from.getAddress().getURI();
//		String toUriString = null;
//		String fromURIString = null;
//		if(toUri.isSipURI()) {
//			toUriString = encode(((SipUri)toUri));
//		} else {
//			toUriString = toUri.toString();
//		}
//		if(fromUri.isSipURI()) {
//			fromURIString = encode(((SipUri)fromUri));
//		} else {
//			fromURIString = fromUri.toString();
//		}
		final String toTag = to.getTag();
		final String fromTag = 	from.getTag();

		
		if(inverted) {
			return new SipSessionKey(
					toTag,
					fromTag,
					((CallIdHeader) message.getHeader(CallIdHeader.NAME)).getCallId(),
					applicationSessionId,
					applicationName);
		} else {
			return new SipSessionKey(
				fromTag,
				toTag,
				((CallIdHeader) message.getHeader(CallIdHeader.NAME)).getCallId(),
				applicationSessionId,
				applicationName);
		}
	}	

	/**
	 * Computes the sip application session key from the input parameters. 
	 * The sip application session key will be of the form (UUID,APPNAME)
	 * @param applicationName the name of the application that will be the second component of the key
	 * @param id the Id composing the first component of the key
	 * @return the computed key 
	 * @throws NullPointerException if one of the two parameters is null
	 */
	public static SipApplicationSessionKey getSipApplicationSessionKey(final String applicationName, final String id, final String appGeneratedKey) {
		if(applicationName == null) {
			throw new NullPointerException("the application name cannot be null for sip application session key creation");
		}
		return new SipApplicationSessionKey(
				id,
				applicationName,
				appGeneratedKey);		
	}
	
	/**
	 * Parse a sip application key that was previously generated and put as an http request param
	 * through the encodeURL method of SipApplicationSession
	 * @param sipApplicationKey the stringified version of the sip application key
	 * @return the corresponding sip application session key
	 * @throws ParseException if the stringfied key cannot be parse to a valid key
	 */
	public static SipApplicationSessionKey parseSipApplicationSessionKey(
			String sipApplicationKey) throws ParseException {
				
		int indexOfComma = sipApplicationKey.indexOf(SESSION_KEY_SEPARATOR);
		if(indexOfComma == -1) {
			throw new ParseException("The comma could not be found in the following key " + sipApplicationKey, 0);
		}
		
		String appGeneratedKey = null;
		String uuid = sipApplicationKey.substring(0, indexOfComma);
		String leftover = sipApplicationKey.substring(indexOfComma + 1, sipApplicationKey.length());
		indexOfComma = leftover.indexOf(SESSION_KEY_SEPARATOR);
		SipApplicationSessionKey sipApplicationSessionKey = null;
		if(indexOfComma != -1) {
			appGeneratedKey = uuid;
			uuid = leftover.substring(0, indexOfComma);
			String applicationName = leftover.substring(indexOfComma + 1, leftover.length());
			sipApplicationSessionKey = getSipApplicationSessionKey(applicationName, uuid, appGeneratedKey);
		} else {
			String applicationName = leftover;
			sipApplicationSessionKey = getSipApplicationSessionKey(applicationName, uuid, null);
		}	
		return sipApplicationSessionKey;
	}
	
	/**
	 * Parse a sip application key that was previously generated and put as an http request param
	 * through the encodeURL method of SipApplicationSession
	 * @param sipSessionKey the stringified version of the sip application key
	 * @return the corresponding sip application session key
	 * @throws ParseException if the stringfied key cannot be parse to a valid key
	 */
	public static SipSessionKey parseSipSessionKey(
			String sipSessionKey) throws ParseException {
		
		int indexOfLeftParenthesis = sipSessionKey.indexOf("(");
		// see http://code.google.com/p/sipservlets/issues/detail?id=207
		// support for Call-ID with ')' character
		int indexOfRightParenthesis = sipSessionKey.lastIndexOf(")");
		if(indexOfLeftParenthesis == -1) {
			throw new ParseException("The left parenthesis could not be found in the following key " + sipSessionKey, 0);
		}
		if(indexOfRightParenthesis == -1) {
			throw new ParseException("The right parenthesis could not be found in the following key " + sipSessionKey, 0);
		}
		String sipSessionKeyToParse = sipSessionKey.substring(indexOfLeftParenthesis+1, indexOfRightParenthesis);
		if(logger.isDebugEnabled()) {
			logger.debug("sipSession key to parse " + sipSessionKeyToParse );
		}
		StringTokenizer stringTokenizer = new StringTokenizer(sipSessionKeyToParse, SESSION_KEY_SEPARATOR);		
		String fromTag = stringTokenizer.nextToken();
		String toTag = null;
		if(stringTokenizer.countTokens() == 4) {
			// Issue 2365 : to tag needed for getApplicationSession().getSipSession(<sessionId>) to return forked session and not the parent one
			toTag = stringTokenizer.nextToken();
		}
		String callId = stringTokenizer.nextToken();
		String applicationSessionId = stringTokenizer.nextToken();
		String applicationName = stringTokenizer.nextToken();
		
		return new SipSessionKey(fromTag, toTag, callId, applicationSessionId, applicationName);
	}
	
	
	/**
	 * Parse a sip application key that was previously generated and put as an http request param
	 * through the encodeURL method of SipApplicationSession
	 * @param sipSessionKey the stringified version of the sip application key
	 * @return the corresponding sip application session key
	 * @throws ParseException if the stringfied key cannot be parse to a valid key
	 */
	public static SipSessionKey parseHaSipSessionKey(
			String sipSessionKey, String sipAppSessionId, String sipApplicationName) throws ParseException {
		
		if(logger.isDebugEnabled()) {
			logger.debug("sipSession ha key to parse " + sipSessionKey );
		}
		StringTokenizer stringTokenizer = new StringTokenizer(sipSessionKey, SESSION_KEY_SEPARATOR);
		String fromTag = stringTokenizer.nextToken();
		String callId = stringTokenizer.nextToken();
		
		return new SipSessionKey(fromTag, null, callId, sipAppSessionId, sipApplicationName);
	}

	public static String getSipSessionHaKey(SipSessionKey key) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(key.getFromTag()).append(SESSION_KEY_SEPARATOR).append(key.getCallId());
		return stringBuilder.toString();
	}
}
