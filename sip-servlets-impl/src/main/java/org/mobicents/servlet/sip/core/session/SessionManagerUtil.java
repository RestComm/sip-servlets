/*
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
	private static transient Logger logger = Logger.getLogger(SessionManagerUtil.class);
	
	public final static String TAG_PARAMETER_NAME = "tag";
	
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
		
		String toUri = ((ToHeader) message.getHeader(ToHeader.NAME)).getAddress().getURI().toString();
		String fromUri = ((FromHeader) message.getHeader(FromHeader.NAME)).getAddress().getURI().toString();
		String toTag = ((ToHeader) message.getHeader(ToHeader.NAME)).getTag();
		String fromTag = 	((FromHeader) message.getHeader(FromHeader.NAME)).getTag();

		
		if(inverted) {
			return new SipSessionKey(
					toUri,
					toTag,
					fromUri,
					fromTag,
					((CallIdHeader) message.getHeader(CallIdHeader.NAME)).getCallId(),
					applicationSessionId,
					applicationName);
		} else {
			return new SipSessionKey(
				fromUri,
				fromTag,
				toUri,
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
	public static SipApplicationSessionKey getSipApplicationSessionKey(final String applicationName, final String id) {
		if(applicationName == null) {
			throw new NullPointerException("the application name cannot be null for sip application session key creation");
		}
		return new SipApplicationSessionKey(
				id,
				applicationName);		
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
		
		int indexOfLeftParenthesis = sipApplicationKey.indexOf("(");
		int indexOfComma = sipApplicationKey.indexOf(",");
		int indexOfRightParenthesis = sipApplicationKey.indexOf(")");
		if(indexOfLeftParenthesis == -1) {
			throw new ParseException("The left parenthesis could not be found in the following key " + sipApplicationKey, 0);
		}
		if(indexOfComma == -1) {
			throw new ParseException("The comma could not be found in the following key " + sipApplicationKey, 0);
		}
		if(indexOfRightParenthesis == -1) {
			throw new ParseException("The right parenthesis could not be found in the following key " + sipApplicationKey, 0);
		}
		
		String uuid = sipApplicationKey.substring(indexOfLeftParenthesis + 1, indexOfComma);
		String applicationName = sipApplicationKey.substring(indexOfComma + 1, indexOfRightParenthesis);
		
		return getSipApplicationSessionKey(applicationName, uuid);			
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
		int indexOfRightParenthesis = sipSessionKey.indexOf(")");
		if(indexOfLeftParenthesis == -1) {
			throw new ParseException("The left parenthesis could not be found in the following key " + sipSessionKey, 0);
		}
		if(indexOfRightParenthesis == -1) {
			throw new ParseException("The right parenthesis could not be found in the following key " + sipSessionKey, 0);
		}
		String sipSessionKeyToParse = sipSessionKey.substring(indexOfLeftParenthesis+1, indexOfRightParenthesis);
		if(logger.isInfoEnabled()) {
			logger.info("sipSession key to parse " + sipSessionKeyToParse );
		}
		StringTokenizer stringTokenizer = new StringTokenizer(sipSessionKeyToParse, ",");
		String fromAddress = stringTokenizer.nextToken();
		String fromTag = stringTokenizer.nextToken();
		String toAddress = stringTokenizer.nextToken();
		String callId = stringTokenizer.nextToken();
		String applicationSessionId = stringTokenizer.nextToken();
		String applicationName = stringTokenizer.nextToken();
		
		return new SipSessionKey(fromAddress, fromTag, toAddress, null, callId, applicationSessionId, applicationName);			
	}
}
