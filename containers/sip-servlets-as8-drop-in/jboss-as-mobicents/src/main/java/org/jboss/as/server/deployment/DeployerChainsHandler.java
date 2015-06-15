/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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
 *
 */
package org.jboss.as.server.deployment;

import java.util.List;

import org.jboss.msc.service.ServiceController;

/**
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class DeployerChainsHandler {
    public enum Operations {
        REMOVE
    }

    public static boolean handleDeployerChains(final DeploymentPhaseContext phaseContext, Phase phase,
            Class<DeploymentUnitProcessor> deploymentUnitProcessorClass, Operations operation) {
        boolean result = false;
        ServiceController<?> deployerChainServiceController = phaseContext.getServiceRegistry().getService(
                Services.JBOSS_DEPLOYMENT_CHAINS);
        DeployerChains chains = (DeployerChains) deployerChainServiceController.getValue();
        List<RegisteredDeploymentUnitProcessor> processors = chains.getChain(phase);

        for (RegisteredDeploymentUnitProcessor processor : processors) {
            if (deploymentUnitProcessorClass == processor.getProcessor().getClass()) {
                if (operation == Operations.REMOVE) {
                    result = processors.remove(processor);

                    break;
                }
            }
        }
        return result;
    }
}
