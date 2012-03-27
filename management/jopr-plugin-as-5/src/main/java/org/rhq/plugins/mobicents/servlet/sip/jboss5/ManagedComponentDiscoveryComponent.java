 /*
  * Jopr Management Platform
  * Copyright (C) 2005-2009 Red Hat, Inc.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License, version 2, as
  * published by the Free Software Foundation, and/or the GNU Lesser
  * General Public License, version 2.1, also as published by the Free
  * Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License and the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License
  * and the GNU Lesser General Public License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
package org.rhq.plugins.mobicents.servlet.sip.jboss5;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jbossas5.factory.ProfileServiceFactory;
import org.rhq.plugins.jbossas5.util.ConversionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Discovery component for all ManagedComponents exposed by the JBoss AS 5.x Profile Service.
 *
 * @author Jason Dobies
 * @author Mark Spritzer
 */
public class ManagedComponentDiscoveryComponent
        implements ResourceDiscoveryComponent<ApplicationServerComponent>
{
    private final Log log = LogFactory.getLog(this.getClass());

    public Set<DiscoveredResourceDetails> discoverResources(
            ResourceDiscoveryContext<ApplicationServerComponent> discoveryContext)
    {
        ResourceType resourceType = discoveryContext.getResourceType();
        log.trace("Discovering " + resourceType.getName() + " Resources..." );

        // TODO (ips): Only refresh the ManagementView *once* per runtime discovery scan, rather than every time this
        //             method is called. Do this by providing a runtime scan id in the ResourceDiscoveryContext.
        ProfileServiceFactory.refreshCurrentProfileView();

        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();

        ComponentType componentType = ConversionUtils.getComponentType(resourceType);
        Set<ManagedComponent> components;
        try
        {
            components = managementView.getComponentsForType(componentType);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to get component types for " + resourceType + ".", e);
        }

        Set<DiscoveredResourceDetails> discoveredResources = new HashSet<DiscoveredResourceDetails>(components.size());
        /* Create a resource for each managed component found. We know all managed components will be of a
           type we're interested in, so we can just add them all. There may be need for multiple iterations
           over lists retrieved from different component types, but that is possible through the current API.
        */
        for (ManagedComponent component : components)
        {
            String resourceName = component.getName();

            String resourceKey = componentType.getType() + ":" +
                    componentType.getSubtype() + ":" + resourceName;

            String version = "?"; // TODO
            DiscoveredResourceDetails resource =
                    new DiscoveredResourceDetails(resourceType,
                            resourceKey,
                            resourceName,
                            version,
                            resourceType.getDescription(),
                            discoveryContext.getDefaultPluginConfiguration(),
                            null);

            resource.getPluginConfiguration().put(new PropertySimple(ManagedComponentComponent.COMPONENT_NAME_PROPERTY,
                    component.getName()));

            discoveredResources.add(resource);
        }

        log.trace("Discovered " + discoveredResources.size() + " " + resourceType.getName() + " Resources." );
        return discoveredResources;
    }
}
