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
