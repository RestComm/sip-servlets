/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.web.session.sip;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.clustering.web.OutgoingSessionGranularitySessionData;
import org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.apache.log4j.Logger;

/**
 * This class is based on the following Jboss class
 * org.jboss.web.tomcat.service.session.SessionBasedClusteredSession JBOSS AS
 * 5.1.0.GA Tag
 * 
 * ClusteredSession where the replication granularity level is session based;
 * that is, we replicate the entire attribute map whenever a request makes any
 * attribute dirty.
 * 
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class SessionBasedClusteredSipSession extends
		ClusteredSipSession<OutgoingSessionGranularitySessionData> {

	private static final Logger logger = Logger.getLogger(SessionBasedClusteredSipSession.class);
	
	/**
	 * Descriptive information describing this Session implementation.
	 */
	protected static final String info = "SessionBasedClusteredSipSession/1.0";

	/**
	 * @param key
	 * @param sipFactoryImpl
	 * @param mobicentsSipApplicationSession
	 */
	public SessionBasedClusteredSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession,
			boolean useJK) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
	}

	// ---------------------------------------------- Overridden Public Methods

	@Override
	public String getInfo() {
		return (info);
	}

	@Override
	protected OutgoingSessionGranularitySessionData getOutgoingSipSessionData() {
		if (logger.isDebugEnabled()){
			logger.debug("getOutgoingSipSessionData");
		}
		Map<String, Object> attrs = isSessionAttributeMapDirty() ? getSessionAttributeMap() : null;
		DistributableSipSessionMetadata metadata = isSessionMetadataDirty() ? (DistributableSipSessionMetadata) getSessionMetadata() : null;
		Long timestamp = attrs != null || metadata != null
				|| getMustReplicateTimestamp() ? Long
				.valueOf(getSessionTimestamp()) : null;
		OutgoingData outgoingData = new OutgoingData(null, getVersion(), timestamp, sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key), metadata,
				attrs);
		outgoingData.setSessionMetaDataDirty(isSessionMetadataDirty());
		return outgoingData;
	}

	@Override
	protected Object removeAttributeInternal(String name, boolean localCall,
			boolean localOnly) {
		if (logger.isDebugEnabled()){
			logger.debug("removeAttributeInternal - name=" + name + ", localCall=" + localCall);
		}
		if (localCall)
			sessionAttributesDirty();
		return getAttributesInternal().remove(name);
	}

	@Override
	protected Object setAttributeInternal(String name, Object value) {
		if (logger.isDebugEnabled()){
			logger.debug("setAttributeInternal - name=" + name + ", value=" + value);
			logger.debug("setAttributeInternal - original value=" + getAttributesInternal().get(name));
		}
		sessionAttributesDirty();
		return getAttributesInternal().put(name, value);
	}

	// ----------------------------------------------------------------- Private

	private Map<String, Object> getSessionAttributeMap() {
		if (logger.isDebugEnabled()){
			logger.debug("getSessionAttributeMap");
		}
		
		Map<String, Object> attrs = new HashMap<String, Object>(
				getAttributesInternal());
		removeExcludedAttributes(attrs);
		return attrs;
	}

	// ----------------------------------------------------------------- Classes

	private static class OutgoingData extends
			OutgoingDistributableSipSessionDataImpl implements
			OutgoingSessionGranularitySessionData {
		
		private final Map<String, Object> attributes;

		public OutgoingData(String realId, int version, Long timestamp, String sipApplicationSessionKey, String sipSessionKey,
				DistributableSipSessionMetadata metadata,
				Map<String, Object> attributes) {
			super(realId, version, timestamp, sipApplicationSessionKey, sipSessionKey, metadata);
			this.attributes = attributes;
		}

		public Map<String, Object> getSessionAttributes() {
			if (logger.isDebugEnabled()){
				logger.debug("OutgoingData - getSessionAttributes");
			}
			
			return attributes;
		}	
	}
}
