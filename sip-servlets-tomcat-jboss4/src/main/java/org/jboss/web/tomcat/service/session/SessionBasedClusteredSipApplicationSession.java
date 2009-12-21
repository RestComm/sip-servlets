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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Implementation of a clustered sip application session for the JBossCacheManager.
 * This class is based on the following Jboss class org.jboss.web.tomcat.service.session.SessionBasedClusteredSession JBOSS AS 4.2.2 Tag
 *
 * The replication granularity
 * level is session based; that is, we replicate per whole session object.
 * We use JBossCache for our internal replicated data store.
 * The internal structure in JBossCache is as follows:
 * <pre>
 * /SIPSESSION
 *    /hostname
 *       /sip_app_name    (path + session id is unique)
 *          /sipapplicationsessionid    Map(id, session)         
 *                    (VERSION_KEY, version)  // Used for version tracking. version is an Integer.
 * </pre>
 * <p/>
 * Note that the isolation level of the cache dictates the
 * concurrency behavior.</p>
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class SessionBasedClusteredSipApplicationSession extends
		JBossCacheClusteredSipApplicationSession {
	private static final Logger logger = Logger.getLogger(SessionBasedClusteredSipApplicationSession.class);
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
			SipContext sipContext) {
		super(key, sipContext);
	}

	// ---------------------------------------------- Overridden Public Methods

	/**
	 * Return a string representation of this object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SessionBasedClusteredSipApplicationSession[");
		sb.append(super.toString());
		sb.append("]");
		return (sb.toString());

	}

	public void removeMyself() {
		proxy_.removeSipApplicationSession(getHaId());
	}

	public void removeMyselfLocal() {
		proxy_.removeSipApplicationSessionLocal(getHaId());
	}

	// ----------------------------------------------HttpSession Public Methods

	/**
	 * Does nothing -- all attributes are populated already
	 */
	protected void populateAttributes() {
		// no-op
	}

	protected Object getJBossInternalAttribute(String name) {
		Object result = getAttributeMap().get(name);

		// Do dirty check even if result is null, as w/ SET_AND_GET null
		// still makes us dirty (ensures timely replication w/o using ACCESS)
		if (isGetDirty(result)) {
			sessionAttributesDirty();
		}

		return result;

	}

	protected Object removeJBossInternalAttribute(String name,
			boolean localCall, boolean localOnly) {
		if (localCall)
			sessionAttributesDirty();
		return getAttributeMap().remove(name);
	}

	protected Map getJBossInternalAttributes() {
		return getAttributeMap();
	}

	protected Object setJBossInternalAttribute(String name, Object value) {
		sessionAttributesDirty();
		return getAttributeMap().put(name, value);
	}

	/**
	 * Overrides the superclass version by additionally reading the attributes
	 * map.
	 * 
	 * <p>
	 * This method is deliberately public so it can be used to reset the
	 * internal state of a session object using serialized contents replicated
	 * from another JVM via JBossCache.
	 * </p>
	 * 
	 * @see org.jboss.web.tomcat.service.session.ClusteredSession#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
//		synchronized (this) {
			// Let superclass read in everything but the attribute map
			super.readExternal(in);

			final String sipAppSessionKey = getHaId();
			
			Integer size = (Integer) proxy_.getSipApplicationSessionAttribute(sipAppSessionKey, "attributesSize");
			if(size != null) {
				Object[][] attributesArray = (Object[][] )proxy_.getSipApplicationSessionAttribute(sipAppSessionKey, "attributes");
				Map<String, Object> attributesMap = new ConcurrentHashMap<String, Object>();
				for (int i = 0; i < size; i++) {
					String key = (String) attributesArray[0][i];
					Object value = attributesArray[1][i];
					attributesMap.put(key, value);
				}						
				sipApplicationSessionAttributeMap = attributesMap;
			}
//		}
	}

	/**
	 * Overrides the superclass version by appending the attributes map. Does
	 * not write any attributes whose names are found in
	 * {@link ClusteredSession#excludedAttributes}.
	 * 
	 * @see org.jboss.web.tomcat.service.session.ClusteredSession#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
//		synchronized (this) {
			// Let superclass write out everything but the attribute map
			super.writeExternal(out);

			if(sessionAttributesDirty) {
				final String sipAppSessionKey = getHaId();
				// Don't replicate any excluded attributes
				Map excluded = removeExcludedAttributes(getAttributeMap());

				final int size = sipApplicationSessionAttributeMap.size();
				Object[][] attributesArray = new Object[2][size];
				int i = 0;
				for (Entry<String, Object> entry : sipApplicationSessionAttributeMap.entrySet()) {
					attributesArray [0][i] = entry.getKey(); 
					attributesArray [1][i] = entry.getValue();
					i++;
				}
				proxy_.putSipApplicationSessionAttribute(sipAppSessionKey, "attributesSize", size);
				proxy_.putSipApplicationSessionAttribute(sipAppSessionKey, "attributes", attributesArray);

				// Restore any excluded attributes
				if (excluded != null)
					sipApplicationSessionAttributeMap.putAll(excluded);								
			}
//		}

	}

	/**
	 * Overrides the superclass version to return <code>true</code> if either
	 * the metadata or the attributes are dirty.
	 */
	public boolean getReplicateSessionBody() {
		return isSessionDirty() || getExceedsMaxUnreplicatedInterval();
	}
}
