/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipSessionNotificationPolicy;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public interface ClusteredSipManager<O extends OutgoingDistributableSessionData> extends ClusteredManager, DistributableSipManager {
	/**
	 * Gets the policy for determining whether the servlet spec notifications
	 * related to sip session events are allowed to be emitted on the local cluster
	 * node.
	 */
	ClusteredSipSessionNotificationPolicy getSipSessionNotificationPolicy();
	
	/**
	 * Gets the policy for determining whether the servlet spec notifications
	 * related to sip application session events are allowed to be emitted on the local cluster
	 * node.
	 */
	ClusteredSipApplicationSessionNotificationPolicy getSipApplicationSessionNotificationPolicy();
	
	/**
	 * Remove the active session locally from the manager without replicating to
	 * the cluster. This can be useful when the session is expired, for example,
	 * where there is not need to propagate the expiration.
	 * 
	 * @param session
	 */
	public void removeLocal(SipSession session);
	
	/**
	 * Remove the active session locally from the manager without replicating to
	 * the cluster. This can be useful when the session is expired, for example,
	 * where there is not need to propagate the expiration.
	 * 
	 * @param session
	 */
	public void removeLocal(SipApplicationSession session);

	boolean storeSipSession(SipSession session);

	boolean storeSipApplicationSession(SipApplicationSession session);
	
	SnapshotSipManager getSnapshotSipManager();

	void notifyRemoteSipApplicationSessionInvalidation(String sessId);

	void notifyRemoteSipSessionInvalidation(String sessId);

	void notifySipApplicationSessionLocalAttributeModification(String sessId);

	void notifySipSessionLocalAttributeModification(String sessId);

	boolean sipApplicationSessionChangedInDistributedCache(String realId,
			String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	boolean sipSessionChangedInDistributedCache(String sipAppSessionId, String sipSessionId, String owner,
			int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	void sipApplicationSessionActivated();

	void sipSessionActivated();
	
	public DistributedCacheConvergedSipManager getDistributedCacheConvergedSipManager();
}
