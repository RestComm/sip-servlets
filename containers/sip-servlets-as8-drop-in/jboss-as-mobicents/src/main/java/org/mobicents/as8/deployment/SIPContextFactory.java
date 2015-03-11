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
package org.mobicents.as8.deployment;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;

import org.jboss.as.naming.deployment.Attachments;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.SimpleAttachable;
import org.jboss.logging.Logger;

/**
 * The Sip context factory.
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 */
class SIPContextFactory{
    
    Logger logger = Logger.getLogger(SIPContextFactory.class);

    //TODO hoe to call this?
    public SIPWebContext createContext(final DeploymentUnit deploymentUnit,DeploymentManager deploymentManager, DeploymentInfo deploymentInfo, ServletContainer servletContainer) throws DeploymentUnitProcessingException {
        logger.debug("create context for " + deploymentUnit.getName());
        // Create the SIP specific context
        return new SIPWebContext(deploymentUnit, deploymentManager, deploymentInfo, servletContainer);
    }

    public void postProcessContext(DeploymentUnit deploymentUnit, SIPWebContext webContext) {
        logger.debug("postProcessContext() for " + deploymentUnit.getName());
        if (webContext instanceof SIPWebContext) {
            ((SIPWebContext)webContext).postProcessContext(deploymentUnit);
        }
    }

}
