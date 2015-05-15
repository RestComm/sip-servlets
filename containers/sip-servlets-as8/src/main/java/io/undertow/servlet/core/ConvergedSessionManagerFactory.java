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
package io.undertow.servlet.core;

import org.mobicents.servlet.sip.undertow.UndertowSipManager;

import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SessionManagerFactory;
import io.undertow.servlet.core.InMemorySessionManagerFactory;
/**
 * @author alerant.appngin@gmail.com
 *
 */
public class ConvergedSessionManagerFactory extends InMemorySessionManagerFactory implements SessionManagerFactory{
    private final int maxSessions;

    public ConvergedSessionManagerFactory() {
        this(-1);
    }
    
    public ConvergedSessionManagerFactory(int maxSessions) {
        super(maxSessions);
        this.maxSessions = maxSessions;
    }

    @Override
    public SessionManager createSessionManager(Deployment deployment) {
        return new UndertowSipManager(deployment.getDeploymentInfo().getDeploymentName(), maxSessions);
    }
}
