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

import org.apache.log4j.Logger;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
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
	private static final Logger logger = Logger.getLogger(ClusteredSipManagerDelegate.class);
	/**
     * The descriptive information about this implementation.
     */
    protected static final String info = "ClusteredSipManager/1.0";
	
	private ReplicationGranularity replicationGranularity = ReplicationGranularity.SESSION;

	private boolean useJK;
	
	private ClusteredSipManager clusteredSipManager;
	/**
	 * @param replicationGranularity 
	 * 
	 */
	public ClusteredSipManagerDelegate(ReplicationGranularity replicationGranularity, boolean useJK, ClusteredSipManager clusteredSipManager) {
		this.replicationGranularity = replicationGranularity;
		this.useJK = useJK;
		this.clusteredSipManager = clusteredSipManager;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManagerDelegate#getNewMobicentsSipApplicationSession(org.mobicents.servlet.sip.core.session.SipApplicationSessionKey, org.mobicents.servlet.sip.startup.SipContext)
	 */
	@Override
	protected MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext) {
		return getNewMobicentsSipApplicationSession(key, sipContext, false);
	}
	
	protected MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext, boolean recreate) {
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = null;				
		
		if(!recreate) {
			clusteredSipManager.checkSipApplicationSessionPassivation(key);
		}
		
		if (replicationGranularity.equals(ReplicationGranularity.ATTRIBUTE)) {
			session = new AttributeBasedClusteredSipApplicationSession(key,sipContext, useJK);
		} else if (replicationGranularity.equals(ReplicationGranularity.FIELD)) {
			session = new FieldBasedClusteredSipApplicationSession(key,sipContext, useJK);
		} else {
			session = new SessionBasedClusteredSipApplicationSession(key,sipContext, useJK);
		}
		clusteredSipManager.getDistributedCacheConvergedSipManager().sipApplicationSessionCreated(key.getId());		
		MobicentsSipApplicationSession sipApplicationSessionImpl = sipApplicationSessions.putIfAbsent(key, session);
		if (sipApplicationSessionImpl == null) {
			// put succeeded, use new value
			if(logger.isDebugEnabled()) {
				logger.debug("Adding a recreated sip application session with the key : " + key);
			}
            sipApplicationSessionImpl = session;
            final String appGeneratedKey = key.getAppGeneratedKey(); 
    		if(appGeneratedKey != null) {    		
            	sipApplicationSessionsByAppGeneratedKey.putIfAbsent(appGeneratedKey, sipApplicationSessionImpl);
            }
    		session.setNew(true);
    		((ClusteredSipApplicationSession<OutgoingDistributableSessionData>)session).clearOutdated();
        }
		if(!recreate) {
			scheduleExpirationTimer(sipApplicationSessionImpl);
		}		
		return sipApplicationSessionImpl;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManagerDelegate#getNewMobicentsSipSession(org.mobicents.servlet.sip.core.session.SipSessionKey, org.mobicents.servlet.sip.message.SipFactoryImpl, org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession)
	 */
	@Override
	protected MobicentsSipSession getNewMobicentsSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		return getNewMobicentsSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, false);
	}
	
	protected MobicentsSipSession getNewMobicentsSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession, boolean recreate) {
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = null;
		
		if(!recreate) {
			clusteredSipManager.checkSipSessionPassivation(key);
		}
		
		if (replicationGranularity.equals(ReplicationGranularity.ATTRIBUTE)) {
			session = new AttributeBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
		} else if (replicationGranularity.equals(ReplicationGranularity.FIELD)) {
			session = new FieldBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
		} else {
			session = new SessionBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
		}
		clusteredSipManager.getDistributedCacheConvergedSipManager().sipSessionCreated(mobicentsSipApplicationSession.getKey().getId(), SessionManagerUtil.getSipSessionHaKey(key));
		session.setNew(true);
		MobicentsSipSession sipSessionImpl = sipSessions.putIfAbsent(key, session);
		if(sipSessionImpl == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Adding a recreated sip session with the key : " + key);
			}
			// put succeeded, use new value
            sipSessionImpl = session;
		}
		return sipSessionImpl;
	}

	public ClusteredSipSession putSipSession(SipSessionKey key, ClusteredSipSession session) {
		return (ClusteredSipSession) sipSessions.put(key, session);
	}

	public ClusteredSipApplicationSession putSipApplicationSession(SipApplicationSessionKey key, ClusteredSipApplicationSession session) {
		return (ClusteredSipApplicationSession) sipApplicationSessions.put(key, session);
	}
}
