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
package org.mobicents.as7.deployment;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * A service starting a sip deployment.
 * For sip, this is required so far only as a way to provide the context to management methods 
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 */
class SipDeploymentService implements Service<Context> {

    private StandardContext sipContext;
    private final DeploymentUnit anchorDu;

    public SipDeploymentService(final DeploymentUnit du) {
    	this.anchorDu =SIPWebContext.getSipContextAnchorDu(du);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void start(StartContext startContext) throws StartException {
        if (sipContext == null) {
        	this.sipContext = anchorDu.getAttachment(SIPWebContext.ATTACHMENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void stop(StopContext stopContext) {
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Context getValue() throws IllegalStateException {
        final Context sipContext = this.sipContext;
        if (sipContext == null) {
            throw new IllegalStateException();
        }
        return sipContext;
    }

}
