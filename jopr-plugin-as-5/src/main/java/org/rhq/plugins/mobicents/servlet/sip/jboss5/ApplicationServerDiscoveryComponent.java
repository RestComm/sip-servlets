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
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.plugins.jbossas5.factory.ProfileServiceFactory;
import org.rhq.plugins.jbossas5.util.PluginDescriptorGenerator;
import org.rhq.plugins.jbossas5.util.ManagedComponentUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;

 /**
 * Discovery component for JBoss AS, 5.1.0.CR1 or later, Servers.
 *
 * @author Ian Springer
 * @author Mark Spritzler  
 */
public class ApplicationServerDiscoveryComponent
        implements ResourceDiscoveryComponent
{
    private static final String DEFAULT_RESOURCE_DESCRIPTION = "JBoss Application Server";
    private static final String JBMANCON_DEBUG_SYSPROP = "jbmancon.debug";

    private final Log log = LogFactory.getLog(this.getClass());

    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext resourceDiscoveryContext)
    {
        log.trace("Discovering " + resourceDiscoveryContext.getResourceType().getName() + " Resources..." );

        Set<DiscoveredResourceDetails> servers = new HashSet<DiscoveredResourceDetails>();

        ProfileService profileService = ProfileServiceFactory.getProfileService();
        Collection<ProfileKey> profileKeys = profileService.getActiveProfileKeys();
        if (profileKeys.isEmpty())
        	throw new IllegalStateException("No active profiles found.");
        log.trace("Found the following active profiles: " + profileKeys);
        ProfileServiceFactory.refreshCurrentProfileView();

        ManagedComponent serverConfigComponent = ManagedComponentUtils.getSingletonManagedComponent(
                new ComponentType("MCBean", "ServerConfig"));
        String serverName = (String)ManagedComponentUtils.getSimplePropertyValue(serverConfigComponent, "serverName");

        // TODO: Figure out if the instance is AS or EAP, and reflect that in the Resource name.
        String resourceName = "JBoss AS 5 (" + serverName + ")";

        String resourceKey = (String)ManagedComponentUtils.getSimplePropertyValue(serverConfigComponent, "serverHomeDir");

        String version = (String)ManagedComponentUtils.getSimplePropertyValue(serverConfigComponent, "specificationVersion");

        Configuration pluginConfig = resourceDiscoveryContext.getDefaultPluginConfiguration();
        pluginConfig.put(new PropertySimple(ApplicationServerComponent.SERVER_NAME_PROPERTY, serverName));

        DiscoveredResourceDetails server =
                new DiscoveredResourceDetails(
                        resourceDiscoveryContext.getResourceType(),
                        resourceKey,
                        resourceName,
                        version,
                        DEFAULT_RESOURCE_DESCRIPTION,
                        pluginConfig,
                        null);
        servers.add(server);
/*
      } catch (NoSuchProfileException e)
      {
         LOG.error("No Active Profile found", e);
      }
*/

        // Implementation to find all profiles
        /*
        Collection<ProfileKey> profileKeys = profileService.getProfileKeys();

        for (ProfileKey key: profileKeys)
        {

           String resourceKey = "Jboss AS 5:" + key.getName();
           // @TODO when the Profile Service gives more AS info, replace hardcoded strings
           DiscoveredResourceDetails server =
                 new DiscoveredResourceDetails(
                       context.getResourceType(),
                       resourceKey,
                       resourceKey,
                       "5.0",
                        "JBoss App Server");
           servers.add( server );
        }
        */
        ResourceType resourceType = resourceDiscoveryContext.getResourceType();
        log.trace("Discovered " + servers.size() + " " + resourceType.getName() + " Resources." );

        boolean debug = Boolean.getBoolean(JBMANCON_DEBUG_SYSPROP);
        if (debug) {
            //new UnitTestRunner().runUnitTests();
            generatePluginDescriptor(resourceDiscoveryContext);
        }

        return servers;
    }

    private void generatePluginDescriptor(ResourceDiscoveryContext resourceDiscoveryContext) {
        log.info("Generating RHQ plugin descriptor...");
        try {
            ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
            File tempDir = resourceDiscoveryContext.getParentResourceContext().getTemporaryDirectory();
            PluginDescriptorGenerator.generatePluginDescriptor(managementView, tempDir);
        }
        catch (Exception e) {
            log.error("Failed to generate RHQ plugin descriptor.", e);
        }
    }
}
