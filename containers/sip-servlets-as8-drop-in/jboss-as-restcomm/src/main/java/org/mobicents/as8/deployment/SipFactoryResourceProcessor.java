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

import javax.servlet.sip.SipFactory;

import org.jboss.as.ee.component.InjectionSource;
import org.jboss.as.ee.component.deployers.EEResourceReferenceProcessor;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;

/**
 * Processes {@link javax.annotation.Resource @Resource} and {@link javax.annotation.Resources @Resources} annotations for a
 * {@link javax.servlet.sip.SipFactory} type resource
 * <p/>
 *
 * @author Jaikiran Pai
 *
 *         This class is based on the contents of org.mobicents.as7.deployment package from jboss-as7-mobicents project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public final class SipFactoryResourceProcessor implements EEResourceReferenceProcessor {

    DeploymentUnit sipDeploymentUnit;

    SipFactoryResourceProcessor(DeploymentUnit du) {
        super();
        sipDeploymentUnit = du;
    }

    @Override
    public String getResourceReferenceType() {
        return SipFactory.class.getName();
    }

    @Override
    public InjectionSource getResourceReferenceBindingSource() throws DeploymentUnitProcessingException {
        return new SipFactoryInjectionSource(sipDeploymentUnit);
    }

}
