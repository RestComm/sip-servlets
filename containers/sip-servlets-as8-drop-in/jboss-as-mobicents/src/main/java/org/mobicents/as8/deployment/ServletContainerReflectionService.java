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
package org.mobicents.as8.deployment;

import io.undertow.servlet.api.JBossConvergedServletContainer;

import java.lang.reflect.Field;

import org.jboss.as.server.ServerLogger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.extension.undertow.ServletContainerService;

/**
 * @author alerant.appngin@gmail.com
 * 
 */
public class ServletContainerReflectionService implements Service<ServletContainerReflectionService> {
    private static final String SERVLETCONTAINERFIELDNAME = "servletContainer";
    public static final ServiceName SERVICE_NAME = ServiceName.of("ServletContainerReflectionService");
    private ServletContainerService service = null;


    public ServletContainerReflectionService(ServletContainerService service) {
        this.service = service;
    }

    @Override
    public ServletContainerReflectionService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.MODULE_SERVICE_LOGGER.debug("ServletContainerReflectionService.start()");
        Class serviceClass = service.getClass();

        for (Field field : serviceClass.getDeclaredFields()) {
            if (ServletContainerReflectionService.SERVLETCONTAINERFIELDNAME.equals(field.getName())) {
                field.setAccessible(true);
                try {
                    JBossConvergedServletContainer container = JBossConvergedServletContainer.Factory.newInstance();
                    field.set(service, container);
                    ServerLogger.MODULE_SERVICE_LOGGER.debug("ServletContainerReflectionService.start() servlet container injected:"+container);
                } catch (IllegalArgumentException e) {
                    ServerLogger.MODULE_SERVICE_LOGGER.error(e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    ServerLogger.MODULE_SERVICE_LOGGER.error(e.getMessage(),e);
                }
                field.setAccessible(false);
                break;
            }
        }
        ServerLogger.MODULE_SERVICE_LOGGER.debug("ServletContainerReflectionService.start() finished");
    }


    @Override
    public void stop(StopContext context) {
        // TODO Auto-generated method stub
    }
}