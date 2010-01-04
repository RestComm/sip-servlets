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

import org.jboss.metadata.WebMetaData;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ClusteredSipManagerDelegate extends SipManagerDelegate {

	/**
     * The descriptive information about this implementation.
     */
    protected static final String info = "ClusteredSipManager/1.0";
	
	private int replicationGranularity = WebMetaData.REPLICATION_GRANULARITY_SESSION;

	/**
	 * @param replicationGranularity 
	 * 
	 */
	public ClusteredSipManagerDelegate(int replicationGranularity) {
		this.replicationGranularity = replicationGranularity;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManagerDelegate#getNewMobicentsSipApplicationSession(org.mobicents.servlet.sip.core.session.SipApplicationSessionKey, org.mobicents.servlet.sip.startup.SipContext)
	 */
	@Override
	protected MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext) {
		MobicentsSipApplicationSession session = null;
		switch (replicationGranularity) {
		case (WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE): {
				session = new AttributeBasedClusteredSipApplicationSession(key,sipContext);
			break;
		}
		case (WebMetaData.REPLICATION_GRANULARITY_FIELD): {
				session = new FieldBasedClusteredSipApplicationSession(key,sipContext);
			break;
		}
		default:
				session = new SessionBasedClusteredSipApplicationSession(key,sipContext);
			break;
		}
		return session;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManagerDelegate#getNewMobicentsSipSession(org.mobicents.servlet.sip.core.session.SipSessionKey, org.mobicents.servlet.sip.message.SipFactoryImpl, org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession)
	 */
	@Override
	protected MobicentsSipSession getNewMobicentsSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		MobicentsSipSession session = null;
		switch (replicationGranularity) {
		case (WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE): {
				session = new AttributeBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, (JBossCacheManager)container.getManager());
			break;
		}
		case (WebMetaData.REPLICATION_GRANULARITY_FIELD): {
				session = new FieldBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, (JBossCacheManager)container.getManager());
			break;
		}
		default:
				session = new SessionBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, (JBossCacheManager)container.getManager());
			break;
		}
		return session;
	}

	public ClusteredSipSession putSipSession(SipSessionKey key, ClusteredSipSession session) {
		return (ClusteredSipSession) sipSessions.put(key, session);
	}

	public ClusteredSipApplicationSession putSipApplicationSession(SipApplicationSessionKey key, ClusteredSipApplicationSession session) {
		return (ClusteredSipApplicationSession) sipApplicationSessions.put(key, session);
	}
}
