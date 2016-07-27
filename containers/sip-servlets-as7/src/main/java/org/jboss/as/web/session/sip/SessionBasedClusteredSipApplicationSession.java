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

import org.apache.log4j.Logger;
import org.jboss.as.clustering.web.OutgoingSessionGranularitySessionData;
import org.jboss.as.clustering.web.sip.DistributableSipApplicationSessionMetadata;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.SipContext;

/**
 * Implementation of a clustered sip application session for the JBossCacheManager.
 * This class is based on the following Jboss class org.jboss.web.tomcat.service.session.SessionBasedClusteredSession JBOSS AS 5.1.0.GA Tag
 *
 * The replication granularity
 * level is session based; that is, we replicate per whole session object.
 *
 * <p/>
 * Note that the isolation level of the cache dictates the
 * concurrency behavior.</p>
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class SessionBasedClusteredSipApplicationSession extends ClusteredSipApplicationSession<OutgoingSessionGranularitySessionData> {

	private static transient Logger logger = Logger.getLogger(SessionBasedClusteredSipApplicationSession.class);
	
	/**
	 * Descriptive information describing this Session implementation.
	 */
	protected static final String info = "SessionBasedClusteredSipApplicationSession/1.0";

	/**
	 * @param key
	 * @param sipFactoryImpl
	 * @param mobicentsSipApplicationSession
	 */
	public SessionBasedClusteredSipApplicationSession(SipApplicationSessionKey key,
			SipContext sipContext, boolean useJK) {
		super(key, sipContext, useJK);
		if (logger.isDebugEnabled()){
			logger.debug("SessionBasedClusteredSipApplicationSession constructor - "
					+ "key.getApplicationName()=" + key.getApplicationName()
					+ ", key.getId()=" + key.getId()
					+ ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey());
		}
	}

	// ---------------------------------------------- Overridden Public Methods

	@Override
	public String getInfo() {
		return (info);
	}

	@Override
	protected OutgoingSessionGranularitySessionData getOutgoingSipApplicationSessionData() {
		if (logger.isDebugEnabled()){
			logger.debug("getOutgoingSipApplicationSessionData - isSessionAttributeMapDirty()=" + isSessionAttributeMapDirty() + ", isSessionMetadataDirty()=" + isSessionMetadataDirty());
		}
		
		Map<String, Object> attrs = isSessionAttributeMapDirty() ? getSessionAttributeMap() : null;
		DistributableSipApplicationSessionMetadata metadata = isSessionMetadataDirty() ? (DistributableSipApplicationSessionMetadata)getSessionMetadata() : null;
		
		Long timestamp = attrs != null || metadata != null
				|| getMustReplicateTimestamp() ? Long
				.valueOf(getSessionTimestamp()) : null;
		if (logger.isDebugEnabled()){
			logger.debug("getOutgoingSipApplicationSessionData - create outgoingData with null realId: "
					+ "key.getApplicationName()=" + ((key == null) ? "key is null" : key.getApplicationName())
					+ ", key.getId()=" + ((key == null) ? "key is null" : key.getId())
					+ ", key.getAppGeneratedKey()=" + ((key == null) ? "key is null" : key.getAppGeneratedKey())
					+ ", timestamp=" + timestamp
					+ ", getVersion()=" + getVersion()
					+ ", metadata.getId()=" + ((metadata == null) ? "metadata is null" : metadata.getId())
					+ ", this.getId()=" + this.getId()
					+ ", this.getHaId()=" + this.getHaId());
			
			if (metadata != null && metadata.getMetaData() != null){
				logger.debug("getOutgoingSipApplicationSessionData - metadata keys:");
				for (String metadataKey: metadata.getMetaData().keySet()){
					logger.debug("getOutgoingSipApplicationSessionData - metadata key: " + metadataKey);	
				}
			} else {
				logger.debug("getOutgoingSipApplicationSessionData - either metadata or metadata.getMetaData() is null");
			}
		}
		
		OutgoingData outgoingData = new OutgoingData(null, getVersion(), timestamp, key.getId(), metadata, attrs);
		outgoingData.setSessionMetaDataDirty(isSessionMetadataDirty());
		return outgoingData;
	}

	@Override
	protected Object removeAttributeInternal(String name, boolean localCall,
			boolean localOnly) {
		if (localCall)
			sessionAttributesDirty();
		return getAttributesInternal().remove(name);
	}

	@Override
	protected Object setAttributeInternal(String name, Object value) {
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
		if (logger.isDebugEnabled()){
			logger.debug("getSessionAttributeMap - return=" + attrs);
		}
		return attrs;
	}

	// ----------------------------------------------------------------- Classes

	private static class OutgoingData extends
			OutgoingDistributableSipApplicationSessionDataImpl implements
			OutgoingSessionGranularitySessionData {
		private final Map<String, Object> attributes;

		public OutgoingData(String realId, int version, Long timestamp, String key,
				DistributableSipApplicationSessionMetadata metadata,
				Map<String, Object> attributes) {
			super(realId, version, timestamp, key, metadata);
			this.attributes = attributes;
		}

		public Map<String, Object> getSessionAttributes() {
			return attributes;
		}
	}
}