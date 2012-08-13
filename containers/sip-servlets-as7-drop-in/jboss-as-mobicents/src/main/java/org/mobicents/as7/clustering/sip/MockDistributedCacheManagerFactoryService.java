/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.as7.clustering.sip;

import java.util.ServiceLoader;

import org.jboss.as.clustering.web.DistributedCacheManagerFactory;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public class MockDistributedCacheManagerFactoryService implements Service<DistributedCacheManagerFactory> {
    public static final ServiceName JVM_ROUTE_REGISTRY_SERVICE_NAME = ServiceName.JBOSS.append("sip", "jvm-route", "registry");
    public static final ServiceName JVM_ROUTE_REGISTRY_ENTRY_PROVIDER_SERVICE_NAME = JVM_ROUTE_REGISTRY_SERVICE_NAME.append("provider");

    private final DistributedCacheManagerFactory factory;

    public MockDistributedCacheManagerFactoryService() {
    	// TODO: plug here sip cache manager factory
    	this (new MockDistributedCacheManagerFactory());
    	// web subsystem loads here org.jboss.as.clustering.web.infinispan.DistributedCacheManagerFactory
        //this(load());
    }

    public MockDistributedCacheManagerFactoryService(DistributedCacheManagerFactory factory) {
        this.factory = factory;
    }

    private static DistributedCacheManagerFactory load() {
        for (DistributedCacheManagerFactory manager: ServiceLoader.load(DistributedCacheManagerFactory.class, DistributedCacheManagerFactory.class.getClassLoader())) {
            return manager;
        }
        return null;
    }

    @Override
    public DistributedCacheManagerFactory getValue() {
        return this.factory;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }
}
