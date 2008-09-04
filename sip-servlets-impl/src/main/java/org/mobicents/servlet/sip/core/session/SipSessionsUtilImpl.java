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

import java.io.Serializable;
import java.text.ParseException;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.startup.SipContext;
import org.xbill.DNS.APLRecord;

/**
 * @author Jean Deruelle
 *
 */
public class SipSessionsUtilImpl implements SipSessionsUtil, Serializable {
	private static transient Log logger = LogFactory.getLog(SipSessionsUtilImpl.class);
	
	private transient SipContext sipContext;
	private String applicationName;

	public SipSessionsUtilImpl(SipContext sipContext, String applicationName) {
		this.sipContext = sipContext;
		this.applicationName = applicationName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SipApplicationSession getApplicationSessionById(String applicationSessionId) {
		if(applicationSessionId == null) {
			throw new NullPointerException("the given id is null !");
		}
		SipApplicationSessionKey applicationSessionKey;
		try {
			applicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(applicationSessionId);
		} catch (ParseException e) {
			logger.error("the given application session id : " + applicationSessionId + 
					" couldn't be parsed correctly ",e);
			return null;
		}
		if(applicationSessionKey.getApplicationName().equals(applicationName)) {
			return ((SipManager)sipContext.getManager()).getSipApplicationSession(applicationSessionKey, false);
		} else {
			logger.warn("the given application session id : " + applicationSessionId + 
					" tried to be retrieved from incorret application " + applicationName);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SipApplicationSession getApplicationSessionByKey(String applicationSessionKey,
			boolean create) {
		if(applicationSessionKey == null) {
			throw new NullPointerException("the given key is null !");
		}
		SipApplicationSessionKey sipApplicationSessionKey = new SipApplicationSessionKey(applicationSessionKey, applicationName, true);
		
		return ((SipManager)sipContext.getManager()).getSipApplicationSession(sipApplicationSessionKey, create);		
	}

	/**
	 * {@inheritDoc}
	 */
	public SipSession getCorrespondingSipSession(SipSession sipSession, String headerName) {
		throw new UnsupportedOperationException("RFC 3911 and RFC 3891 are not currently supported");
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

}
