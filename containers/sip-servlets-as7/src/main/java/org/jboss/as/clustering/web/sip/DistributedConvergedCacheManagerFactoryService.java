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

package org.jboss.as.clustering.web.sip;

import java.util.ServiceLoader;

import org.jboss.as.clustering.web.infinispan.sip.DistributedCacheConvergedSipManagerFactory;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author jean.deruelle@gmail.com
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class DistributedConvergedCacheManagerFactoryService implements Service<DistributedCacheConvergedSipManagerFactory> {
	private static final Logger logger = Logger.getLogger(DistributedConvergedCacheManagerFactoryService.class);
	
	public static final ServiceName JVM_ROUTE_REGISTRY_SERVICE_NAME = ServiceName.JBOSS.append("sip", "jvm-route", "registry");
    public static final ServiceName JVM_ROUTE_REGISTRY_ENTRY_PROVIDER_SERVICE_NAME = JVM_ROUTE_REGISTRY_SERVICE_NAME.append("provider");
    
    private final DistributedCacheConvergedSipManagerFactory factory;

    public DistributedConvergedCacheManagerFactoryService() {
    	this (new DistributedCacheConvergedSipManagerFactory()); 
    	//this (load());
    	if(logger.isDebugEnabled()) {
			logger.debug("DistributedConvergedCacheManagerFactoryService");
		}
    }

    public DistributedConvergedCacheManagerFactoryService(DistributedCacheConvergedSipManagerFactory factory) {
        this.factory = factory;
        if(logger.isDebugEnabled()) {
			logger.debug("DistributedConvergedCacheManagerFactoryService - factory");
		}
    }

    private static DistributedCacheConvergedSipManagerFactory load() {
    	if(logger.isDebugEnabled()) {
			logger.debug("load");
		}
        for (DistributedCacheConvergedSipManagerFactory manager: ServiceLoader.load(DistributedCacheConvergedSipManagerFactory.class, DistributedCacheConvergedSipManagerFactory.class.getClassLoader())) {
        	if(logger.isDebugEnabled()) {
    			logger.debug("load - return manager");
    		}
            return manager;
        }
        return null;
    }

    @Override
    public DistributedCacheConvergedSipManagerFactory getValue() {
    	if(logger.isDebugEnabled()) {
			logger.debug("getValue");
		}
        return this.factory;
    }

    @Override
    public void start(StartContext context) throws StartException {
    	if(logger.isDebugEnabled()) {
			logger.debug("start");
		}
    }

    @Override
    public void stop(StopContext context) {
    	if(logger.isDebugEnabled()) {
			logger.debug("stop");
		}
    }
}
