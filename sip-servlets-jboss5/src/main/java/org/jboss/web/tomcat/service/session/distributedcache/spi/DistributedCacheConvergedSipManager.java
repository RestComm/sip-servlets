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
package org.jboss.web.tomcat.service.session.distributedcache.spi;

import java.util.Map;

import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public interface DistributedCacheConvergedSipManager<T extends OutgoingDistributableSessionData>
		extends DistributedCacheManager<T> {

	/**
	 * Remove a non-locally active sip application session from the distributed
	 * cache, but on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void removeSessionLocal(SipApplicationSessionKey key, String dataOwner);

	/**
	 * Remove a non-locally active sip session from the distributed cache, but
	 * on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void removeSessionLocal(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key, String dataOwner);

	/**
	 * Store or update a sip session in the distributed cache.
	 * 
	 * @param session
	 *            the sip session
	 */
	void storeSipSessionData(T sipSessionData);

	/**
	 * Store or update a sip application session in the distributed cache.
	 * 
	 * @param session
	 *            the sip application session
	 */
	void storeSipApplicationSessionData(T sipApplicationSessionData);

	/**
	 * Store or update a sip session in the distributed cache.
	 * 
	 * @param session
	 *            the sip session
	 */
	void storeSipSessionAttributes(Map<Object, Object> map, T sipSessionData);

	/**
	 * Store or update a sip application session in the distributed cache.
	 * 
	 * @param session
	 *            the sip application session
	 */
	void storeSipApplicationSessionAttributes(Map<Object, Object> map,
			T sipApplicationSessionData);

	/**
	 * Get the {@link IncomingDistributableSessionData} that encapsulates the
	 * distributed cache's information about the given sip session.
	 * 
	 * @param key
	 *            the session's key
	 * @param initialLoad
	 *            <code>true</code> if this is the first access of this
	 *            session's data on this node
	 * 
	 * @return the session data
	 */
	IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey sipAppSessionKey, SipSessionKey key,
			boolean initialLoad);

	/**
	 * Get the {@link IncomingDistributableSessionData} that encapsulates the
	 * distributed cache's information about the given sip session.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active;
	 *            <code>null</code> if locally active or location where active
	 *            is unknown
	 * @param includeAttributes
	 *            should
	 * @link IncomingDistributableSessionData#providesSessionAttributes()}
	 *       return <code>true</code>?
	 * 
	 * @return the session data
	 */
	IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey sipAppSessionKey, SipSessionKey key,
			String dataOwner, boolean includeAttributes);

	/**
	 * Get the {@link IncomingDistributableSessionData} that encapsulates the
	 * distributed cache's information about the given sip application session.
	 * 
	 * @param key
	 *            the session's key
	 * @param initialLoad
	 *            <code>true</code> if this is the first access of this
	 *            session's data on this node
	 * 
	 * @return the session data
	 */
	IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, boolean initialLoad);

	/**
	 * Get the {@link IncomingDistributableSessionData} that encapsulates the
	 * distributed cache's information about the given sip application session.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active;
	 *            <code>null</code> if locally active or location where active
	 *            is unknown
	 * @param includeAttributes
	 *            should
	 * @link IncomingDistributableSessionData#providesSessionAttributes()}
	 *       return <code>true</code>?
	 * 
	 * @return the session data
	 */
	IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, String dataOwner,
			boolean includeAttributes);

	/**
	 * Evict a sip session from the in-memory portion of the distributed cache,
	 * on this node only.
	 * 
	 * @param key
	 *            the session's key
	 */
	void evictSession(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key);

	/**
	 * Evict a sip application session from the in-memory portion of the
	 * distributed cache, on this node only.
	 * 
	 * @param key
	 *            the session's key
	 */
	void evictSession(SipApplicationSessionKey key);

	/**
	 * Evict a non-locally-active sip session from the in-memory portion of the
	 * distributed cache, on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void evictSession(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key, String dataOwner);

	/**
	 * Evict a non-locally-active sip application session from the in-memory
	 * portion of the distributed cache, on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void evictSession(SipApplicationSessionKey key, String dataOwner);

	/**
	 * Gets the ids of all sip sessions in the underlying cache.
	 * 
	 * @return Map<SipSessionKey, String> containing all of the sip session key
	 *         of sessions in the cache (with any jvmRoute removed) as keys, and
	 *         the identifier of the data owner for the session as value (or a
	 *         <code>null</code> value if buddy replication is not enabled.)
	 *         Will not return <code>null</code>.
	 */
	Map<SipSessionKey, String> getSipSessionKeys();

	/**
	 * Gets the ids of all sip application sessions in the underlying cache.
	 * 
	 * @return Map<SipApplicationSessionKey, String> containing all of the sip
	 *         application session key of sessions in the cache (with any
	 *         jvmRoute removed) as keys, and the identifier of the data owner
	 *         for the session as value (or a <code>null</code> value if buddy
	 *         replication is not enabled.) Will not return <code>null</code>.
	 */
	Map<SipApplicationSessionKey, String> getSipApplicationSessionKeys();

	/**
	 * Notification to the distributed cache that a session has been newly
	 * created.
	 * 
	 * @param sipApplicationSessionKey
	 *            the parent session's key
	 * @param sipSessionKey
	 *            the session's key
	 */
	void sipSessionCreated(SipApplicationSessionKey sipApplicationSessionKey, SipSessionKey sipSessionKey);

	/**
	 * Notification to the distributed cache that a session has been newly
	 * created.
	 * 
	 * @param key
	 *            the session's key
	 */
	void sipApplicationSessionCreated(SipApplicationSessionKey key);
}