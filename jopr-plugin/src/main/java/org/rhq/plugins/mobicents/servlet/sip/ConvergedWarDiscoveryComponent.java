 /*
  * Jopr Management Platform
  * Copyright (C) 2005-2008 Red Hat, Inc.
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
package org.rhq.plugins.mobicents.servlet.sip;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jbossas.ApplicationDiscoveryComponent;
import org.rhq.plugins.jbossas.JBossASServerComponent;
import org.rhq.plugins.jmx.JMXComponent;

/**
 * A JON plugin discovery component for discovering standalone webapps deployed to a JBossAS server.
 *
 * Original file copied over from jopr jboss as plugin trunk rev 26
 * It has been copied because it uses ConvergedWarDiscoveryHelper and 
 * since methods in this class are static they couldn't be extended by inheritance
 *
 * @author Ian Springer
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class ConvergedWarDiscoveryComponent extends ApplicationDiscoveryComponent {
	private static final Log log = LogFactory.getLog(ConvergedWarDiscoveryComponent.class);
    @Override
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<JMXComponent> discoveryContext) {
        Set<DiscoveredResourceDetails> resources = super.discoverResources(discoveryContext);
        JBossASServerComponent parentJBossASComponent = (JBossASServerComponent) discoveryContext
            .getParentResourceComponent();
        resources = ConvergedWarDiscoveryHelper.initPluginConfigurations(parentJBossASComponent, resources);
        log.debug("resources size" + resources.size());
        return resources;
    }
}