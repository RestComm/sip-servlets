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
import java.util.Set;

import org.jboss.cache.Cache;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public interface DistributedCacheConvergedSipManager<T extends OutgoingDistributableSessionData>
		extends DistributedCacheManager<T> {

	/**
	 * Globally remove a session from the distributed cache.
	 * 
	 * @param realId
	 *            the session's id, excluding any jvmRoute
	 */
	void removeSipApplicationSession(String sipApplicationSessionKey);

	/**
	 * Globally remove a session from the distributed cache.
	 * 
	 * @param realId
	 *            the session's id, excluding any jvmRoute
	 */
	void removeSipSession(String sipApplicationSessionKey,
			String sipSessionKey);

	/**
	 * Remove a non-locally active sip application session from the distributed
	 * cache, but on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void removeSipApplicationSessionLocal(String key, String dataOwner);

	/**
	 * Remove a non-locally active sip session from the distributed cache, but
	 * on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void removeSipSessionLocal(String sipAppSessionKey,
			String key, String dataOwner);

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
	IncomingDistributableSessionData getSipSessionData(
			String sipAppSessionKey, String key,
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
	IncomingDistributableSessionData getSipSessionData(
			String sipAppSessionKey, String key,
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
	IncomingDistributableSessionData getSipApplicationSessionData(
			String key, boolean initialLoad);

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
	IncomingDistributableSessionData getSipApplicationSessionData(
			String key, String dataOwner,
			boolean includeAttributes);

	/**
	 * Evict a sip session from the in-memory portion of the distributed cache,
	 * on this node only.
	 * 
	 * @param key
	 *            the session's key
	 */
	void evictSipSession(String sipAppSessionKey,
			String key);

	/**
	 * Evict a sip application session from the in-memory portion of the
	 * distributed cache, on this node only.
	 * 
	 * @param key
	 *            the session's key
	 */
	void evictSipApplicationSession(String key);

	/**
	 * Evict a non-locally-active sip session from the in-memory portion of the
	 * distributed cache, on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void evictSipSession(String sipAppSessionKey,
			String key, String dataOwner);

	/**
	 * Evict a non-locally-active sip application session from the in-memory
	 * portion of the distributed cache, on this node only.
	 * 
	 * @param key
	 *            the session's key
	 * @param dataOwner
	 *            identifier of node where the session is active
	 */
	void evictSipApplicationSession(String key, String dataOwner);

	/**
	 * Gets the ids of all sip sessions in the underlying cache.
	 * 
	 * @return Map<SipSessionKey, String> containing all of the sip session key
	 *         of sessions in the cache (with any jvmRoute removed) as keys, and
	 *         the identifier of the data owner for the session as value (or a
	 *         <code>null</code> value if buddy replication is not enabled.)
	 *         Will not return <code>null</code>.
	 */
	Map<String, String> getSipSessionKeys();

	/**
	 * Gets the ids of all sip application sessions in the underlying cache.
	 * 
	 * @return Map<SipApplicationSessionKey, String> containing all of the sip
	 *         application session key of sessions in the cache (with any
	 *         jvmRoute removed) as keys, and the identifier of the data owner
	 *         for the session as value (or a <code>null</code> value if buddy
	 *         replication is not enabled.) Will not return <code>null</code>.
	 */
	Map<String, String> getSipApplicationSessionKeys();

	/**
	 * Notification to the distributed cache that a session has been newly
	 * created.
	 * 
	 * @param sipApplicationSessionKey
	 *            the parent session's key
	 * @param sipSessionKey
	 *            the session's key
	 */
	void sipSessionCreated(String sipApplicationSessionKey,
			String sipSessionKey);

	/**
	 * Notification to the distributed cache that a session has been newly
	 * created.
	 * 
	 * @param key
	 *            the session's key
	 */
	void sipApplicationSessionCreated(String key);

	/**
	 * Get the value of the attribute with the given key from the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * @return the attribute value, or <code>null</code>
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Object getSipApplicationSessionAttribute(String sipApplicationSessionKey,
			String key);

	/**
	 * Stores the given value under the given key in the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * @param value
	 *            the previous attribute value, or <code>null</code>
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			String key, Object value);

	/**
	 * Stores the given map of attribute key/value pairs in the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param map
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			Map<String, Object> map);

	/**
	 * Removes the attribute with the given key from the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * @return the previous attribute value, or <code>null</code>
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Object removeSipApplicationSessionAttribute(String sipApplicationSessionKey,
			String key);

	/**
	 * Removes the attribute with the given key from the given session, but only
	 * on the local node.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	void removeSipApplicationSessionAttributeLocal(
			String sipApplicationSessionKey, String key);

	/**
	 * Obtain the attribute keys associated with this session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @return the attribute keys or an empty Set if none are found
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Set<String> getSipApplicationSessionAttributeKeys(
			String sipApplicationSessionKey);

	/**
	 * Return all attributes associated with this session id.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @return the attributes, or any empty Map if none are found.
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Map<String, Object> getSipApplicationSessionAttributes(
			String sipApplicationSessionKey);

	/**
	 * Get the value of the attribute with the given key from the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * @return the attribute value, or <code>null</code>
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Object getSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key);

	/**
	 * Stores the given value under the given key in the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * @param value
	 *            the previous attribute value, or <code>null</code>
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key, Object value);

	/**
	 * Stores the given map of attribute key/value pairs in the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param map
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, Map<String, Object> map);

	/**
	 * Removes the attribute with the given key from the given session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * @return the previous attribute value, or <code>null</code>
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Object removeSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key);

	/**
	 * Removes the attribute with the given key from the given session, but only
	 * on the local node.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @param key
	 *            the attribute key
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	void removeSipSessionAttributeLocal(
			String sipApplicationSessionKey,
			String sipSessionKey, String key);

	/**
	 * Obtain the attribute keys associated with this session.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @return the attribute keys or an empty Set if none are found
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Set<String> getSipSessionAttributeKeys(
			String sipApplicationSessionKey,
			String sipSessionKey);

	/**
	 * Return all attributes associated with this session id.
	 * 
	 * @param realId
	 *            the session id with any jvmRoute removed
	 * @return the attributes, or any empty Map if none are found.
	 * 
	 * @throws UnsupportedOperationException
	 *             if {@link #getSupportsAttributeOperations()} would return
	 *             <code>false</code>
	 */
	Map<String, Object> getSipSessionAttributes(
			String sipApplicationSessionKey,
			String sipSessionKey);

	Cache getJBossCache();

	void setApplicationName(String applicationName);

	void setApplicationNameHashed(String applicationNameHashed);	
}