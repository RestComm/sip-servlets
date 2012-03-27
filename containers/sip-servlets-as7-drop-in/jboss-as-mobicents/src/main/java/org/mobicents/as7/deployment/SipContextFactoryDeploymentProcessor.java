/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.mobicents.as7.deployment;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.ext.WebContextFactory;
import org.jboss.logging.Logger;
import org.mobicents.metadata.sip.spec.SipMetaData;

/**
 * The SIP contextFactory deployment process will attach a {@code WebContextFactory} to the {@code DeploymentUnit}
 * in case it finds a SIP deployment.
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 */
public class SipContextFactoryDeploymentProcessor implements DeploymentUnitProcessor {

    private static final Logger logger = Logger.getLogger(SipContextFactoryDeploymentProcessor.class);
    public static final DeploymentUnitProcessor INSTANCE = new SipContextFactoryDeploymentProcessor();

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        // Check if the deployment contains a sip metadata
        final SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        if(sipMetaData == null) {
            if (logger.isDebugEnabled()) logger.debug(deploymentUnit.getName() + " has null sipMetaData, no Sip context factory installed");
            // In case there is no metadata attached, it means this is not a sip deployment, so we can safely ignore it!
            return;
        }
        if (logger.isDebugEnabled()) logger.debug(deploymentUnit.getName() + " sip context factory installed");
        // Just attach the context factory, the web subsystem will pick it up
        final SIPContextFactory contextFactory = new SIPContextFactory();
        deploymentUnit.putAttachment(WebContextFactory.ATTACHMENT, contextFactory);
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        //
    }
}
