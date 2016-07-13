package org.jboss.as.clustering.web.infinispan.sip;

import static org.jboss.as.clustering.web.infinispan.sip.InfinispanSipMessages.MESSAGES;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.as.clustering.infinispan.affinity.KeyAffinityServiceFactory;
import org.jboss.as.clustering.infinispan.affinity.KeyAffinityServiceFactoryService;
import org.jboss.as.clustering.infinispan.atomic.AtomicMapCache;
import org.jboss.as.clustering.infinispan.invoker.BatchCacheInvoker;
import org.jboss.as.clustering.infinispan.invoker.CacheInvoker;
import org.jboss.as.clustering.infinispan.invoker.RetryingCacheInvoker;
import org.jboss.as.clustering.infinispan.invoker.TransactionCacheInvoker;
import org.jboss.as.clustering.infinispan.subsystem.AbstractCacheConfigurationService;
import org.jboss.as.clustering.infinispan.subsystem.CacheService;
import org.jboss.as.clustering.infinispan.subsystem.EmbeddedCacheManagerService;
import org.jboss.as.clustering.lock.SharedLocalYieldingClusterLockManager;
import org.jboss.as.clustering.lock.impl.SharedLocalYieldingClusterLockManagerService;
import org.jboss.as.clustering.msc.AsynchronousService;
import org.jboss.as.clustering.registry.Registry;
import org.jboss.as.clustering.registry.RegistryService;
import org.jboss.as.clustering.web.BatchingManager;
import org.jboss.as.clustering.web.ClusteringNotSupportedException;
import org.jboss.as.clustering.web.LocalDistributableSessionManager;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.jboss.as.clustering.web.SessionAttributeMarshaller;
import org.jboss.as.clustering.web.SessionAttributeMarshallerFactory;
import org.jboss.as.clustering.web.impl.SessionAttributeMarshallerFactoryImpl;
import org.jboss.as.clustering.web.impl.TransactionBatchingManager;
import org.jboss.as.clustering.web.infinispan.SessionAttributeStorage;
import org.jboss.as.clustering.web.infinispan.SessionAttributeStorageFactory;
import org.jboss.as.clustering.web.infinispan.SessionAttributeStorageFactoryImpl;
import org.jboss.as.clustering.web.infinispan.WebSessionCacheConfigurationService;
import org.jboss.as.clustering.web.sip.DistributedConvergedCacheManagerFactoryService;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.ReplicationConfig;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.value.InjectedValue;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.jboss.as.clustering.web.infinispan.DistributedCacheManagerFactory;

public class DistributedCacheConvergedSipManagerFactory extends DistributedCacheManagerFactory {
	
	private static final Logger logger = Logger.getLogger(DistributedCacheConvergedSipManagerFactory.class);
	
	public static final String DEFAULT_CACHE_CONTAINER = "singleton";
	private static final ServiceName JVM_ROUTE_REGISTRY_SERVICE_NAME = DistributedConvergedCacheManagerFactoryService.JVM_ROUTE_REGISTRY_ENTRY_PROVIDER_SERVICE_NAME.getParent();

	private SessionAttributeStorageFactory storageFactory = new SessionAttributeStorageFactoryImpl();//todo? atallni ConvergedSessionAttributeMarshallerFactory-ra?
	private CacheInvoker invoker = new RetryingCacheInvoker(new BatchCacheInvoker(), 10, 100);
    private CacheInvoker txInvoker = new TransactionCacheInvoker();
	private SessionAttributeMarshallerFactory marshallerFactory = new SessionAttributeMarshallerFactoryImpl();
	private final InjectedValue<KeyAffinityServiceFactory> affinityFactory = new InjectedValue<KeyAffinityServiceFactory>();

	@SuppressWarnings("unchecked")
	@Override
    public <T extends OutgoingDistributableSessionData> org.jboss.as.clustering.web.DistributedCacheManager<T> getDistributedCacheManager(LocalDistributableSessionManager manager) throws ClusteringNotSupportedException {
		if(logger.isDebugEnabled()) {
			logger.debug("getDistributedCacheManager");
		}
		
        @SuppressWarnings("unchecked")
        Registry<String, Void> jvmRouteRegistry = ((InjectedValue<Registry>)getRegistryInjector()).getValue();
        @SuppressWarnings("unchecked")
        AdvancedCache<String, Map<Object, Object>> cache = ((InjectedValue<Cache>)getCacheInjector()).getValue().getAdvancedCache();

        if (!cache.getCacheConfiguration().invocationBatching().enabled()) {
            ServiceName cacheServiceName = this.getCacheServiceName(manager.getReplicationConfig());
            throw new ClusteringNotSupportedException(MESSAGES.failedToConfigureSipApp(cacheServiceName.getParent().getSimpleName(), cacheServiceName.getSimpleName()));
        }

        BatchingManager batchingManager = new TransactionBatchingManager(cache.getTransactionManager());
        SessionAttributeMarshaller marshaller = this.marshallerFactory.createMarshaller(manager);
        SessionAttributeStorage<T> storage = this.storageFactory.createStorage(manager.getReplicationConfig().getReplicationGranularity(), marshaller);

        //return new DistributedCacheManager<T>(manager, new AtomicMapCache<String, Object, Object>(cache), jvmRouteRegistry, ((InjectedValue<SharedLocalYieldingClusterLockManager>)getLockManagerInjector()).getOptionalValue(), storage, batchingManager, this.invoker, this.txInvoker, this.affinityFactory.getValue());
        return new DistributedCacheManager<T>(manager, new AtomicMapCache<String, Object, Object>(cache), jvmRouteRegistry, ((InjectedValue<SharedLocalYieldingClusterLockManager>)getLockManagerInjector()).getOptionalValue(), storage, batchingManager, this.invoker, this.txInvoker, this.affinityFactory.getValue(), marshaller);
    }
	
	private ServiceName getCacheServiceName(ReplicationConfig config) {
		if(logger.isDebugEnabled()) {
			logger.debug("getCacheServiceName");
		}
        ServiceName baseServiceName = EmbeddedCacheManagerService.getServiceName(null);
        if(logger.isDebugEnabled()) {
        	if (config == null){
        		logger.debug("getCacheServiceName - config is null, using DEFAULT_CACHE_CONTAINER (=" + DEFAULT_CACHE_CONTAINER + ")");
        	} else {
        		logger.debug("getCacheServiceName - cacheName = " + config.getCacheName());
        	}
		}
        String cacheName = (config != null) ? config.getCacheName() : null;
        ServiceName serviceName = ServiceName.parse((cacheName != null) ? cacheName : DEFAULT_CACHE_CONTAINER);
        if (!baseServiceName.isParentOf(serviceName)) {
            serviceName = baseServiceName.append(serviceName);
        }
        ServiceName result = (serviceName.length() < 4) ? serviceName.append(CacheContainer.DEFAULT_CACHE_NAME) : serviceName;
        if(logger.isDebugEnabled()) {
			logger.debug("getCacheServiceName - result.getCanonicalName()=" + result.getCanonicalName() + ", result.getSimpleName()=" + result.getSimpleName() + ", result.toString()=" + result.toString());
		}
        return result;
    }
	
	@Override
	public void setSessionAttributeMarshallerFactory(SessionAttributeMarshallerFactory marshallerFactory) {
		if(logger.isDebugEnabled()) {
			logger.debug("setSessionAttributeMarshallerFactory");
		}
        this.marshallerFactory = marshallerFactory;
    }

	@Override
    public void setSessionAttributeStorageFactory(SessionAttributeStorageFactory storageFactory) {
		if(logger.isDebugEnabled()) {
			logger.debug("setSessionAttributeStorageFactory");
		}
        this.storageFactory = storageFactory;
    }

    @Override
    public boolean addDeploymentDependencies(ServiceName deploymentServiceName, ServiceRegistry registry, ServiceTarget target, ServiceBuilder<?> builder, JBossWebMetaData metaData) {
    	if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies");
		}
        ServiceName templateCacheServiceName = this.getCacheServiceName(metaData.getReplicationConfig());
        if (registry.getService(templateCacheServiceName) == null) {
        	if(logger.isDebugEnabled()) {
    			logger.debug("addDeploymentDependencies - getService returned null --> returning false");
    		}
            return false;
        }
        String templateCacheName = templateCacheServiceName.getSimpleName();
        ServiceName containerServiceName = templateCacheServiceName.getParent();
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - containerServiceName = " + containerServiceName.getSimpleName());
		}
        
        String containerName = containerServiceName.getSimpleName();
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - containerName = " + containerName);
		}
        ServiceName templateCacheConfigurationServiceName = AbstractCacheConfigurationService.getServiceName(containerName, templateCacheName);
        String host = deploymentServiceName.getParent().getSimpleName();
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - host = " + host);
		}
        String contextPath = deploymentServiceName.getSimpleName();
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - contextPath = " + contextPath);
		}
        StringBuilder cacheNameBuilder = new StringBuilder(host).append(contextPath);
        if (contextPath.equals("/")) {
            cacheNameBuilder.append("ROOT");
        }
        String cacheName = cacheNameBuilder.toString();
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - cacheName = " + cacheName);
		}
        ServiceName cacheConfigurationServiceName = AbstractCacheConfigurationService.getServiceName(containerName, cacheName);
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - cacheConfigurationServiceName = " + cacheConfigurationServiceName.getSimpleName());
		}
        ServiceName cacheServiceName = CacheService.getServiceName(containerName, cacheName);
        if(logger.isDebugEnabled()) {
			logger.debug("addDeploymentDependencies - cacheServiceName = " + cacheServiceName.getSimpleName());
		}
        
        final InjectedValue<EmbeddedCacheManager> container = new InjectedValue<EmbeddedCacheManager>();
        final InjectedValue<Configuration> config = new InjectedValue<Configuration>();
        target.addService(cacheConfigurationServiceName, new WebSessionCacheConfigurationService(cacheName, container, config, metaData))
                .addDependency(containerServiceName, EmbeddedCacheManager.class, container)
                .addDependency(templateCacheConfigurationServiceName, Configuration.class, config)
                .setInitialMode(ServiceController.Mode.ON_DEMAND)
                .install()
        ;
        final InjectedValue<EmbeddedCacheManager> cacheContainer = new InjectedValue<EmbeddedCacheManager>();
        CacheService.Dependencies dependencies = new CacheService.Dependencies() {
            @Override
            public EmbeddedCacheManager getCacheContainer() {
                return cacheContainer.getValue();
            }

            @Override
            public XAResourceRecoveryRegistry getRecoveryRegistry() {
                return null;
            }
        };
        AsynchronousService.addService(target, cacheServiceName, new CacheService<Object, Object>(cacheName, dependencies))
                .addDependency(cacheConfigurationServiceName)
                .addDependency(containerServiceName, EmbeddedCacheManager.class, cacheContainer)
                .setInitialMode(ServiceController.Mode.ON_DEMAND)
                .install()
        ;
        builder.addDependency(cacheServiceName, Cache.class, ((InjectedValue<Cache>)getCacheInjector()));
        builder.addDependency(JVM_ROUTE_REGISTRY_SERVICE_NAME, Registry.class, ((InjectedValue<Registry>)getRegistryInjector()));
        builder.addDependency(DependencyType.OPTIONAL, SharedLocalYieldingClusterLockManagerService.getServiceName(containerName), SharedLocalYieldingClusterLockManager.class, ((InjectedValue<SharedLocalYieldingClusterLockManager>)getLockManagerInjector()));
        builder.addDependency(KeyAffinityServiceFactoryService.getServiceName(containerName), KeyAffinityServiceFactory.class, this.affinityFactory);
        return true;
    }

    /*@SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Collection<ServiceController<?>> installServices(ServiceTarget target) {
        InjectedValue<Cache> cache = new InjectedValue<Cache>();
        InjectedValue<Registry.RegistryEntryProvider> providerValue = new InjectedValue<Registry.RegistryEntryProvider>();
        ServiceController<?> controller = AsynchronousService.addService(target, JVM_ROUTE_REGISTRY_SERVICE_NAME, new RegistryService(cache, providerValue))
                .addDependency(CacheService.getServiceName(DEFAULT_CACHE_CONTAINER, null), Cache.class, cache)
                .addDependency(DistributedCacheManagerFactoryService.JVM_ROUTE_REGISTRY_ENTRY_PROVIDER_SERVICE_NAME, Registry.RegistryEntryProvider.class, providerValue)
                .setInitialMode(ServiceController.Mode.ON_DEMAND)
                .install()
        ;
        return Collections.<ServiceController<?>>singleton(controller);
    }*/
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Collection<ServiceController<?>> installServices(ServiceTarget target) {
    	if(logger.isDebugEnabled()) {
			logger.debug("installServices - target = " + target.toString());
		}
    	
        InjectedValue<Cache> cache = new InjectedValue<Cache>();
        InjectedValue<Registry.RegistryEntryProvider> providerValue = new InjectedValue<Registry.RegistryEntryProvider>();
        ServiceController<?> controller = AsynchronousService.addService(target, JVM_ROUTE_REGISTRY_SERVICE_NAME, new RegistryService(cache, providerValue))
                .addDependency(CacheService.getServiceName(DEFAULT_CACHE_CONTAINER, null), Cache.class, cache)
                .addDependency(DistributedConvergedCacheManagerFactoryService.JVM_ROUTE_REGISTRY_ENTRY_PROVIDER_SERVICE_NAME, Registry.RegistryEntryProvider.class, providerValue)
                .setInitialMode(ServiceController.Mode.ON_DEMAND)
                .install()
        ;
        return Collections.<ServiceController<?>>singleton(controller);
    }
}
