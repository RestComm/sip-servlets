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

package org.mobicents.as7;

import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;

/**
 * Utils for the web container configuration.
 *
 * @author Emanuel Muckenhuber
 */
class SipConfigurationHandlerUtils {


    /**

     subsystem=sip

     */

    /**
     * Initialize the configuration model, since add/remove operations would
     * not make sense. (the operation node should already have the default value set.
     *
     * @param resource the subsystem root resource
     * @param operation the subsystem add operation
     */

    static void initializeConfiguration(final Resource resource, final ModelNode operation) {
        final ModelNode rootModel = resource.getModel();
        if (operation.hasDefined(Constants.INSTANCE_ID)) {
            rootModel.get(Constants.INSTANCE_ID).set(operation.get(Constants.INSTANCE_ID));
        }
        if (operation.hasDefined(Constants.APPLICATION_ROUTER)) {
            rootModel.get(Constants.APPLICATION_ROUTER).set(operation.get(Constants.APPLICATION_ROUTER));
        }
        if (operation.hasDefined(Constants.SIP_STACK_PROPS)) {
            rootModel.get(Constants.SIP_STACK_PROPS).set(operation.get(Constants.SIP_STACK_PROPS));
        }
        if (operation.hasDefined(Constants.SIP_PATH_NAME)) {
            rootModel.get(Constants.SIP_PATH_NAME).set(operation.get(Constants.SIP_PATH_NAME));
        }
        if (operation.hasDefined(Constants.SIP_APP_DISPATCHER_CLASS)) {
            rootModel.get(Constants.SIP_APP_DISPATCHER_CLASS).set(operation.get(Constants.SIP_APP_DISPATCHER_CLASS));
        }
        if (operation.hasDefined(Constants.CONCURRENCY_CONTROL_MODE)) {
            rootModel.get(Constants.CONCURRENCY_CONTROL_MODE).set(operation.get(Constants.CONCURRENCY_CONTROL_MODE));
        }
        if (operation.hasDefined(Constants.CONGESTION_CONTROL_INTERVAL)) {
            rootModel.get(Constants.CONGESTION_CONTROL_INTERVAL).set(operation.get(Constants.CONGESTION_CONTROL_INTERVAL));
        }
        if (operation.hasDefined(Constants.USE_PRETTY_ENCODING)) {
            rootModel.get(Constants.USE_PRETTY_ENCODING).set(operation.get(Constants.USE_PRETTY_ENCODING));
        }
    }

    static void populateModel(final ModelNode subModel, final ModelNode operation) {
        for(final String attribute : operation.keys()) {
            if(operation.hasDefined(attribute)) {
                subModel.get(attribute).set(operation.get(attribute));
            }
        }
    }

    public static void initializeConnector(Resource resource, ModelNode model) {
    }
}
