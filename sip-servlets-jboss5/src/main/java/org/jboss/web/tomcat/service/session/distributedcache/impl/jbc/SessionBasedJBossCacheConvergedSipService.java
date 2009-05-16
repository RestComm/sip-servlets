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
package org.jboss.web.tomcat.service.session.distributedcache.impl.jbc;

import java.util.Map;

import org.jboss.cache.Cache;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.SessionBasedJBossCacheService;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class SessionBasedJBossCacheConvergedSipService extends
		SessionBasedJBossCacheService implements DistributedCacheConvergedSipManager<OutgoingSessionGranularitySessionData> {
	
	DistributedCacheConvergedSipManagerDelegate delegate;
	
	public SessionBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager)
			throws ClusteringNotSupportedException {
		super(localManager);
		delegate = new DistributedCacheConvergedSipManagerDelegate((AbstractJBossCacheService)this);
	}

	public SessionBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager,
			Cache<Object, Object> plainCache) {
		super(localManager, plainCache);
		delegate = new DistributedCacheConvergedSipManagerDelegate((AbstractJBossCacheService)this);
	}

	@Override
	public void start() {	
		super.start();
		delegate.start();
	}
	
	public void evictSession(SipSessionKey key) {
		delegate.evictSession(key);      
	}

	public void evictSession(SipApplicationSessionKey key) {
		delegate.evictSession(key);         
	}

	public void evictSession(SipSessionKey key, String dataOwner) {
		delegate.evictSession(key, dataOwner);
	}

	public void evictSession(SipApplicationSessionKey key, String dataOwner) {
		delegate.evictSession(key, dataOwner);
	}

	public IncomingDistributableSessionData getSessionData(SipSessionKey key,
			boolean initialLoad) {
		return delegate.getSessionData(key, initialLoad);		
	}

	public IncomingDistributableSessionData getSessionData(SipSessionKey key,
			String dataOwner, boolean includeAttributes) {
		return delegate.getSessionData(key, dataOwner, includeAttributes);
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, boolean initialLoad) {
		return delegate.getSessionData(key, initialLoad);		
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, String dataOwner,
			boolean includeAttributes) {
		return delegate.getSessionData(key, dataOwner, includeAttributes);
	}

	public Map<SipApplicationSessionKey, String> getSipApplicationSessionKeys() {
		return delegate.getSipApplicationSessionKeys();
	}
		
	public Map<SipSessionKey, String> getSipSessionKeys() {
		return delegate.getSipSessionKeys();
	}

	public void removeSessionLocal(SipApplicationSessionKey key) {
		delegate.removeSessionLocal(key);
	}

	public void removeSessionLocal(SipSessionKey key) {
		delegate.removeSessionLocal(key);
	}
	
	public void removeSessionLocal(SipApplicationSessionKey key,
			String dataOwner) {
		delegate.removeSessionLocal(key, dataOwner);
	}

	public void removeSessionLocal(SipSessionKey key, String dataOwner) {
		delegate.removeSessionLocal(key, dataOwner);
	}
}
