/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.core.session;

import java.text.ParseException;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSessionsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jean Deruelle
 *
 */
public class SipSessionsUtilImpl implements SipSessionsUtil {
	private static transient Log logger = LogFactory.getLog(SipSessionsUtilImpl.class);
	
	private SessionManager sessionManager;
	private String applicationName;

	public SipSessionsUtilImpl(SessionManager sessionManager, String applicationName) {
		this.sessionManager = sessionManager;
		this.applicationName = applicationName;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionsUtil#getApplicationSession(java.lang.String)
	 */
	public SipApplicationSession getApplicationSession(String applicationSessionId) {
		if(applicationSessionId == null) {
			throw new NullPointerException("the given id is null !");
		}
		SipApplicationSessionKey applicationSessionKey;
		try {
			applicationSessionKey = SessionManager.parseSipApplicationSessionKey(applicationSessionId);
		} catch (ParseException e) {
			logger.error("the given application session id : " + applicationSessionId + 
					" couldn't be parsed correctly ",e);
			return null;
		}
		if(applicationSessionKey.getApplicationName().equals(applicationName)) {
			return sessionManager.getSipApplicationSession(applicationSessionKey, false, null);
		} else {
			logger.warn("the given application session id : " + applicationSessionId + 
					" tried to be retrieved from incorret application " + applicationName);
			return null;
		}
	}

}
