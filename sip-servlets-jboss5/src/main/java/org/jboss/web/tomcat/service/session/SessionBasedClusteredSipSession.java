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
package org.jboss.web.tomcat.service.session;

import java.util.HashMap;
import java.util.Map;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSipSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

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
 * 
 */
public class SessionBasedClusteredSipSession extends
		ClusteredSipSession<OutgoingSessionGranularitySessionData> {

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
		Map<String, Object> attrs = isSessionAttributeMapDirty() ? getSessionAttributeMap()
				: null;
		DistributableSipSessionMetadata metadata = isSessionMetadataDirty() ? (DistributableSipSessionMetadata)getSessionMetadata()
				: null;
		Long timestamp = attrs != null || metadata != null
				|| getMustReplicateTimestamp() ? Long
				.valueOf(getSessionTimestamp()) : null;
		return new OutgoingData(getRealId(), getVersion(), timestamp, sipApplicationSession.getKey(), key, metadata, 
				attrs);
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

		public OutgoingData(String realId, int version, Long timestamp, SipApplicationSessionKey sipApplicationSessionKey, SipSessionKey sipSessionKey,
				DistributableSipSessionMetadata metadata,
				Map<String, Object> attributes) {
			super(realId, version, timestamp, sipApplicationSessionKey, sipSessionKey, metadata);
			this.attributes = attributes;
		}

		public Map<String, Object> getSessionAttributes() {
			return attributes;
		}
	}
}
