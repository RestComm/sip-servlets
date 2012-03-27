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

import javax.servlet.sip.SipSessionsUtil;

import org.jboss.as.ee.component.InjectionSource;
import org.jboss.as.ee.component.deployers.EEResourceReferenceProcessor;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;

/**
 * Processes {@link javax.annotation.Resource @Resource} and {@link javax.annotation.Resources @Resources} annotations
 * for a {@link SipSessionsUtil} type resource
 * <p/>
 *
 * @author Jaikiran Pai
 * @author josemrecio
 *
 */
public final class SipSessionsUtilResourceProcessor implements EEResourceReferenceProcessor {

    DeploymentUnit sipDeploymentUnit;

    SipSessionsUtilResourceProcessor(DeploymentUnit du) {
        super();
        sipDeploymentUnit = du;
    }

    @Override
    public String getResourceReferenceType() {
        return SipSessionsUtil.class.getName();
    }

    @Override
    public InjectionSource getResourceReferenceBindingSource() throws DeploymentUnitProcessingException {
        return new SipSessionsUtilInjectionSource(sipDeploymentUnit);
    }

}
