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

import org.apache.log4j.Logger;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.core.SipContext;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * @author posfai.gergely@ext.alerant.hu
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
	
	private ClusteredSipSessionManager clusteredSipSessionManager;
	/**
	 * @param replicationGranularity 
	 * 
	 */
	public ClusteredSipManagerDelegate(ReplicationGranularity replicationGranularity, boolean useJK, ClusteredSipSessionManager clusteredSipManager) {
		if (logger.isDebugEnabled()){
			logger.debug("constructor - replicationGranularity=" + replicationGranularity + " useJK=" + useJK + ", clusteredSipManager=" + clusteredSipManager);
		}
		this.replicationGranularity = replicationGranularity;
		this.useJK = useJK;
		this.clusteredSipSessionManager = clusteredSipManager;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManagerDelegate#getNewMobicentsSipApplicationSession(org.mobicents.servlet.sip.core.session.SipApplicationSessionKey, org.mobicents.servlet.sip.startup.SipContext)
	 */
	@Override
	protected MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext) {
		if (logger.isDebugEnabled()){
			logger.debug("getNewMobicentsSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + " key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey());
		}
		return getNewMobicentsSipApplicationSession(key, sipContext, false);
	}
	
	protected MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext, boolean recreate) {
		if (logger.isDebugEnabled()){
			logger.debug("getNewMobicentsSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + " key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey() + ", recreate=" + recreate);
		}
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = null;				
		
		if(!recreate) {
			clusteredSipSessionManager.checkSipApplicationSessionPassivation(key);
		}
		
		
		// Only session granularity is supported so far
		//if (replicationGranularity.equals(ReplicationGranularity.ATTRIBUTE)) {
		//	session = new AttributeBasedClusteredSipApplicationSession(key,sipContext, useJK);
		//} else if (replicationGranularity.equals(ReplicationGranularity.FIELD)) {
		//	session = new FieldBasedClusteredSipApplicationSession(key,sipContext, useJK);
		//} else {
			session = new SessionBasedClusteredSipApplicationSession(key,sipContext, useJK);
		//}
			
		clusteredSipSessionManager.getDistributedCacheConvergedSipManager().sipApplicationSessionCreated(key.getId());		
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
		if (logger.isDebugEnabled()){
			logger.debug("getNewMobicentsSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", mobicentsSipApplicationSession.getId()=" + mobicentsSipApplicationSession.getId());
		}
		
		return getNewMobicentsSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, false);
	}
	
	protected MobicentsSipSession getNewMobicentsSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession, boolean recreate) {
		if (logger.isDebugEnabled()){
			logger.debug("getNewMobicentsSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", mobicentsSipApplicationSession.getId()=" + mobicentsSipApplicationSession.getId() + ", recreate=" + recreate);
		}
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = null;
		
		if(!recreate) {
			clusteredSipSessionManager.checkSipSessionPassivation(key);
		}
		
		// Only session granularity is supported so far
		//if (replicationGranularity.equals(ReplicationGranularity.ATTRIBUTE)) {
		//	session = new AttributeBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
		//} else if (replicationGranularity.equals(ReplicationGranularity.FIELD)) {
		//	session = new FieldBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
		//} else {
			session = new SessionBasedClusteredSipSession(key, sipFactoryImpl, mobicentsSipApplicationSession, useJK);
		//}
			
		if(logger.isDebugEnabled()) {
			logger.debug("getNewMobicentsSipSession - newly created session: session.getId()=" + session.getId()
					+ ", session.getHaId()=" + session.getHaId()
					+ ", session.getState()=" + session.getState()
					+ ", session.getCallId()=" + session.getCallId()
					+ ", session.getApplicationSession().getId()=" + session.getApplicationSession().getId());
		}
			
		clusteredSipSessionManager.getDistributedCacheConvergedSipManager().sipSessionCreated(mobicentsSipApplicationSession.getKey().getId(), SessionManagerUtil.getSipSessionHaKey(key));
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
		if (logger.isDebugEnabled()){
			logger.debug("putSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", session.getHaId()=" + session.getHaId() + ", session.getId()=" + session.getId());
		}
		return (ClusteredSipSession) sipSessions.put(key, session);
	}

	public ClusteredSipApplicationSession putSipApplicationSession(SipApplicationSessionKey key, ClusteredSipApplicationSession session) {
		if (logger.isDebugEnabled()){
			logger.debug("putSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + " key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey() + ", session.getHaId()=" + session.getHaId() + ", session.getId()=" + session.getId());
		}
		return (ClusteredSipApplicationSession) sipApplicationSessions.put(key, session);
	}
}
