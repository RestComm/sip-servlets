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

import org.jboss.as.ee.component.InjectionSource;
import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;

/**
 * {@link InjectionSource} for {@link javax.servlet.sip.SipFactory} resource.
 *
 * User: Jaikiran Pai
 *
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.as7.deployment package from jboss-as7-mobicents project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipFactoryInjectionSource extends InjectionSource {

    DeploymentUnit sipDeploymentUnit;
    SIPWebContext sipContext;

    SipFactoryInjectionSource(DeploymentUnit du) {
        sipDeploymentUnit = du;
        sipContext = du.getAttachment(SIPWebContext.ATTACHMENT_KEY);
    }

    @Override
    public void getResourceValue(ResolutionContext resolutionContext, ServiceBuilder<?> serviceBuilder,
            DeploymentPhaseContext phaseContext, Injector<ManagedReferenceFactory> injector)
            throws DeploymentUnitProcessingException {
        injector.inject(new SipFactoryManagedReferenceFactory());
    }

    private class SipFactoryManagedReferenceFactory implements ManagedReferenceFactory {

        @Override
        public ManagedReference getReference() {
            return new SipFactoryManagedReference();
        }
    }

    private class SipFactoryManagedReference implements ManagedReference {

        @Override
        public void release() {
        }

        @Override
        public Object getInstance() {
            // return the SipFactory
            if (sipContext == null) {
                sipContext = sipDeploymentUnit.getAttachment(SIPWebContext.ATTACHMENT_KEY);
            }

            return sipContext.getSipFactoryFacade();
        }
    }

    // all context injection sources are equal since they are thread locals
    public boolean equals(Object o) {
        return o instanceof SipFactoryInjectionSource;
    }

    public int hashCode() {
        return 1;
    }
}