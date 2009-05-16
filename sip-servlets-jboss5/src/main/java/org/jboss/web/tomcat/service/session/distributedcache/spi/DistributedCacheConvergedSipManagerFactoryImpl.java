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

import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.web.tomcat.service.session.distributedcache.impl.DistributedCacheManagerFactoryImpl;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.AttributeBasedJBossCacheConvergedSipService;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.AttributeBasedJBossCacheService;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.FieldBasedJBossCacheConvergedSipService;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.FieldBasedJBossCacheService;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.SessionBasedJBossCacheConvergedSipService;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.SessionBasedJBossCacheService;
import org.jboss.web.tomcat.service.session.distributedcache.impl.jbc.Util;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributedCacheConvergedSipManagerFactoryImpl extends
		DistributedCacheManagerFactoryImpl {	

	/* (non-Javadoc)
	 * @see org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactory#getDistributedCacheManager(org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DistributedCacheManager getDistributedCacheManager(
			LocalDistributableSessionManager localManager)
			throws ClusteringNotSupportedException {
		ReplicationGranularity granularity = Util.getReplicationGranularity(localManager);
	    switch(granularity)
	    {
	    	case SESSION:
	    		return getPlainCache() == null? new SessionBasedJBossCacheConvergedSipService(localManager) : new SessionBasedJBossCacheConvergedSipService(localManager, getPlainCache());
	        case ATTRIBUTE:
	        	return getPlainCache() == null? new AttributeBasedJBossCacheConvergedSipService(localManager) : new AttributeBasedJBossCacheConvergedSipService(localManager, getPlainCache());
	        case FIELD:
	        	return getPojoCache() == null? new FieldBasedJBossCacheConvergedSipService(localManager) : new FieldBasedJBossCacheConvergedSipService(localManager, getPojoCache());
	        default:
	        	throw new IllegalStateException("Unknown ReplicationGranularity " + granularity);
	    }
	}
}
