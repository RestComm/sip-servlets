/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
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
package org.mobicents.as8.deployment;

import javax.servlet.ServletException;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
//import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.Logger;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;

/**
 * The Sip context factory.
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.as7.deployment package from jboss-as7-mobicents project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
class SIPContextFactory {
    static AttachmentKey<SIPContextFactory> ATTACHMENT = AttachmentKey.create(SIPContextFactory.class);
    Logger logger = Logger.getLogger(SIPContextFactory.class);

    public SIPWebContext addDeplyomentUnitToContext(final DeploymentUnit deploymentUnit,
            UndertowDeploymentInfoService deploymentInfoservice, SIPWebContext context) throws ServletException {
        logger.debug("create context for " + deploymentUnit.getName());
        // Create the SIP specific context
        return context.addDeploymentUnit(deploymentUnit).createContextConfig(deploymentInfoservice).attachContext();
    }

    public void postProcessContext(DeploymentUnit deploymentUnit, SIPWebContext webContext) {
        logger.debug("postProcessContext() for " + deploymentUnit.getName());
        webContext.postProcessContext(deploymentUnit);
    }
}
