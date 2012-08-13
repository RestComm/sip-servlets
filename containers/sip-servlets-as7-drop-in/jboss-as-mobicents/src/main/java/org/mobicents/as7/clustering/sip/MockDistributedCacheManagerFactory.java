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

import java.util.Collection;
import java.util.Collections;

import org.jboss.as.clustering.web.ClusteringNotSupportedException;
import org.jboss.as.clustering.web.DistributedCacheManager;
import org.jboss.as.clustering.web.DistributedCacheManagerFactory;
import org.jboss.as.clustering.web.LocalDistributableSessionManager;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.mobicents.metadata.sip.jboss.JBossConvergedSipMetaData;

/**
 * 
 * 
 * @author Jose M Recio
 * 
 * @version $Revision: $
 */
public class MockDistributedCacheManagerFactory implements DistributedCacheManagerFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends OutgoingDistributableSessionData> DistributedCacheManager<T> getDistributedCacheManager(LocalDistributableSessionManager localManager) throws ClusteringNotSupportedException {
        return (DistributedCacheManager<T>) MockDistributedCacheManager.INSTANCE;
    }

    @Override
    public boolean addDeploymentDependencies(ServiceName deploymentServiceName, ServiceRegistry registry, ServiceTarget target, ServiceBuilder<?> builder, JBossWebMetaData metaData) {
    	if (metaData instanceof JBossConvergedSipMetaData) {
    		return true;
    	}
    	return true;
    }

    @Override
    public Collection<ServiceController<?>> installServices(ServiceTarget target) {
        return Collections.emptySet();
    }
}
