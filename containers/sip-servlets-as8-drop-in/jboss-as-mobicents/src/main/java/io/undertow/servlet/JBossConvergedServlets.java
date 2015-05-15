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
package io.undertow.servlet;

import io.undertow.servlet.ConvergedServlets;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.JBossConvergedServletContainer;
import io.undertow.servlet.api.ServletContainer;
/**
 * @author alerant.appngin@gmail.com
 *
 */
public class JBossConvergedServlets extends ConvergedServlets{

    private static volatile JBossConvergedServletContainer container;

    protected JBossConvergedServlets() {
        super();
    }

    public static ConvergedDeploymentInfo deployment() {
        return new ConvergedDeploymentInfo();
    }

    public static ServletContainer defaultContainer() {
        if (container != null) {
            return container;
        }
        synchronized (Servlets.class) {
            if (container != null) {
                return container;
            }
            return container = JBossConvergedServletContainer.Factory.newInstance();
        }
    }
}
