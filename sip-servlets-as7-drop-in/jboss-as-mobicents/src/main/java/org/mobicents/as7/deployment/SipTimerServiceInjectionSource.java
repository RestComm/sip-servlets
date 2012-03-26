/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import org.jboss.as.ee.component.InjectionSource;
import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;

/**
 * {@link InjectionSource} for {@link javax.servlet.sip.TimerService} resource.
 *
 * User: Jaikiran Pai
 * @author josemrecio@gmail.com
 *
 */
public class SipTimerServiceInjectionSource extends InjectionSource {

    DeploymentUnit sipDeploymentUnit;
    SIPWebContext sipContext;

    SipTimerServiceInjectionSource(DeploymentUnit du) {
        sipDeploymentUnit = du;
        sipContext = du.getAttachment(SIPWebContext.ATTACHMENT);
    }

    @Override
    public void getResourceValue(ResolutionContext resolutionContext, ServiceBuilder<?> serviceBuilder, DeploymentPhaseContext phaseContext, Injector<ManagedReferenceFactory> injector) throws DeploymentUnitProcessingException {
        injector.inject(new SipTimerServiceManagedReferenceFactory());
    }

    private class SipTimerServiceManagedReferenceFactory implements ManagedReferenceFactory {

        @Override
        public ManagedReference getReference() {
            return new SipTimerServiceManagedReference();
        }
    }

    private class SipTimerServiceManagedReference implements ManagedReference {

        @Override
        public void release() {
        }

        @Override
        public Object getInstance() {
            // return the TimerService
            if (sipContext == null) {
                sipContext = sipDeploymentUnit.getAttachment(SIPWebContext.ATTACHMENT);
            }
            return sipContext.getTimerService();
        }
    }

    // all context injection sources are equal since they are thread locals
    public boolean equals(Object o) {
        return o instanceof SipTimerServiceInjectionSource;
    }

    public int hashCode() {
        return 1;
    }
}