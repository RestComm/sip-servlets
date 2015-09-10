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
package org.mobicents.io.undertow.servlet.core;

import org.mobicents.servlet.sip.undertow.UndertowSipManager;

import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SessionManagerFactory;
import io.undertow.servlet.core.InMemorySessionManagerFactory;

/**
 * Factory class to create ConvergedInMemorySessionManagers.
 *
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class ConvergedSessionManagerFactory extends InMemorySessionManagerFactory implements SessionManagerFactory {
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
