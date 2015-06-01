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
package io.undertow.servlet;

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.ConvergedServletContainer;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.core.ConvergedServletContainerImpl;
import io.undertow.servlet.core.ServletContainerImpl;

/**
 * @author alerant.appngin@gmail.com
 *
 */
public class ConvergedServlets extends Servlets {

    private static volatile ConvergedServletContainer container;

    protected ConvergedServlets() {
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
            return container = ConvergedServletContainer.Factory.newInstance();
        }
    }
    
    public static ServletContainer newContainer() {
        return new ConvergedServletContainerImpl();
    }


}
