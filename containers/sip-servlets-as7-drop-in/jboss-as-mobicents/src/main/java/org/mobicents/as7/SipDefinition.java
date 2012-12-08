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
package org.mobicents.as7;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import java.util.EnumSet;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.DefaultResourceAddDescriptionProvider;
import org.jboss.as.controller.descriptions.DefaultResourceRemoveDescriptionProvider;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author Tomaz Cerar
 * @created 22.2.12 14:29
 */
public class SipDefinition extends SimpleResourceDefinition {
    public static final SipDefinition INSTANCE = new SipDefinition();

    protected static final SimpleAttributeDefinition INSTANCE_ID =
            new SimpleAttributeDefinitionBuilder(Constants.INSTANCE_ID, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.INSTANCE_ID)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();   
    protected static final SimpleAttributeDefinition APPLICATION_ROUTER =
            new SimpleAttributeDefinitionBuilder(Constants.APPLICATION_ROUTER, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.APPLICATION_ROUTER)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition SIP_STACK_PROPS =
            new SimpleAttributeDefinitionBuilder(Constants.SIP_STACK_PROPS, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.SIP_STACK_PROPS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition SIP_PATH_NAME =
            new SimpleAttributeDefinitionBuilder(Constants.SIP_PATH_NAME, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.SIP_PATH_NAME)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition SIP_APP_DISPATCHER_CLASS =
            new SimpleAttributeDefinitionBuilder(Constants.SIP_APP_DISPATCHER_CLASS, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.SIP_APP_DISPATCHER_CLASS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition CONCURRENCY_CONTROL_MODE =
            new SimpleAttributeDefinitionBuilder(Constants.CONCURRENCY_CONTROL_MODE, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.CONCURRENCY_CONTROL_MODE)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition CONGESTION_CONTROL_INTERVAL =
            new SimpleAttributeDefinitionBuilder(Constants.CONGESTION_CONTROL_INTERVAL, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.CONGESTION_CONTROL_INTERVAL)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    //.setValidator(new IntRangeValidator(1, true))
                    .setDefaultValue(new ModelNode(-1))
                    .build();
    protected static final SimpleAttributeDefinition CONGESTION_CONTROL_POLICY =
            new SimpleAttributeDefinitionBuilder(Constants.CONGESTION_CONTROL_POLICY, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.CONGESTION_CONTROL_POLICY)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    //.setValidator(new IntRangeValidator(1, true))
                    .setDefaultValue(new ModelNode("ErrorResponse"))
                    .build();
    protected static final SimpleAttributeDefinition USE_PRETTY_ENCODING =
            new SimpleAttributeDefinitionBuilder(Constants.USE_PRETTY_ENCODING, ModelType.BOOLEAN, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.USE_PRETTY_ENCODING)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(true))
                    .build();
    protected static final SimpleAttributeDefinition ADDITIONAL_PARAMETERABLE_HEADERS =
            new SimpleAttributeDefinitionBuilder(Constants.ADDITIONAL_PARAMETERABLE_HEADERS, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.ADDITIONAL_PARAMETERABLE_HEADERS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition BASE_TIMER_INTERVAL =
            new SimpleAttributeDefinitionBuilder(Constants.BASE_TIMER_INTERVAL, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.BASE_TIMER_INTERVAL)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(500))
                    .build();
    protected static final SimpleAttributeDefinition T2_INTERVAL =
            new SimpleAttributeDefinitionBuilder(Constants.T2_INTERVAL, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.T2_INTERVAL)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(4000))
                    .build();
    protected static final SimpleAttributeDefinition T4_INTERVAL =
            new SimpleAttributeDefinitionBuilder(Constants.T4_INTERVAL, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.T4_INTERVAL)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(5000))
                    .build();
    protected static final SimpleAttributeDefinition TIMER_D_INTERVAL =
            new SimpleAttributeDefinitionBuilder(Constants.TIMER_D_INTERVAL, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.TIMER_D_INTERVAL)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(32000))
                    .build();
    protected static final SimpleAttributeDefinition DIALOG_PENDING_REQUEST_CHECKING =
            new SimpleAttributeDefinitionBuilder(Constants.DIALOG_PENDING_REQUEST_CHECKING, ModelType.BOOLEAN, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.DIALOG_PENDING_REQUEST_CHECKING)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(false))
                    .build();
    protected static final SimpleAttributeDefinition CANCELED_TIMER_TASKS_PURGE_PERIOD =
            new SimpleAttributeDefinitionBuilder(Constants.CANCELED_TIMER_TASKS_PURGE_PERIOD, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.CANCELED_TIMER_TASKS_PURGE_PERIOD)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(-1))
                    .build();
    protected static final SimpleAttributeDefinition MEMORY_THRESHOLD =
            new SimpleAttributeDefinitionBuilder(Constants.MEMORY_THRESHOLD, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.MEMORY_THRESHOLD)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(85))
                    .build();
    protected static final SimpleAttributeDefinition BACK_TO_NORMAL_MEMORY_THRESHOLD =
            new SimpleAttributeDefinitionBuilder(Constants.BACK_TO_NORMAL_MEMORY_THRESHOLD, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.BACK_TO_NORMAL_MEMORY_THRESHOLD)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(75))
                    .build();
    protected static final SimpleAttributeDefinition OUTBOUND_PROXY =
            new SimpleAttributeDefinitionBuilder(Constants.OUTBOUND_PROXY, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.OUTBOUND_PROXY)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    
    
    private SipDefinition() {
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, SipExtension.SUBSYSTEM_NAME),
                SipExtension.getResourceDescriptionResolver(null));
    }

    @Override
    public void registerOperations(final ManagementResourceRegistration rootResourceRegistration) {
        final ResourceDescriptionResolver rootResolver = getResourceDescriptionResolver();
        // Ops to add and remove the root resource
        final DescriptionProvider subsystemAddDescription = new DefaultResourceAddDescriptionProvider(rootResourceRegistration, rootResolver);
        rootResourceRegistration.registerOperationHandler(ADD, SipSubsystemAdd.INSTANCE, subsystemAddDescription, EnumSet.of(OperationEntry.Flag.RESTART_ALL_SERVICES));
        final DescriptionProvider subsystemRemoveDescription = new DefaultResourceRemoveDescriptionProvider(rootResolver);
        rootResourceRegistration.registerOperationHandler(REMOVE, ReloadRequiredRemoveStepHandler.INSTANCE, subsystemRemoveDescription, EnumSet.of(OperationEntry.Flag.RESTART_ALL_SERVICES));
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration registration) {
        registration.registerReadWriteAttribute(INSTANCE_ID, null, new ReloadRequiredWriteAttributeHandler(INSTANCE_ID));
        registration.registerReadWriteAttribute(APPLICATION_ROUTER, null, new ReloadRequiredWriteAttributeHandler(APPLICATION_ROUTER));
        registration.registerReadWriteAttribute(SIP_STACK_PROPS, null, new ReloadRequiredWriteAttributeHandler(SIP_STACK_PROPS));
        registration.registerReadWriteAttribute(SIP_PATH_NAME, null, new ReloadRequiredWriteAttributeHandler(SIP_PATH_NAME));
        registration.registerReadWriteAttribute(SIP_APP_DISPATCHER_CLASS, null, new ReloadRequiredWriteAttributeHandler(SIP_APP_DISPATCHER_CLASS));
        registration.registerReadWriteAttribute(CONGESTION_CONTROL_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(CONGESTION_CONTROL_INTERVAL));
        registration.registerReadWriteAttribute(CONGESTION_CONTROL_POLICY, null, new ReloadRequiredWriteAttributeHandler(CONGESTION_CONTROL_POLICY));
        registration.registerReadWriteAttribute(CONCURRENCY_CONTROL_MODE, null, new ReloadRequiredWriteAttributeHandler(CONCURRENCY_CONTROL_MODE));
        registration.registerReadWriteAttribute(USE_PRETTY_ENCODING, null, new ReloadRequiredWriteAttributeHandler(USE_PRETTY_ENCODING));
        registration.registerReadWriteAttribute(ADDITIONAL_PARAMETERABLE_HEADERS, null, new ReloadRequiredWriteAttributeHandler(ADDITIONAL_PARAMETERABLE_HEADERS));
        registration.registerReadWriteAttribute(BASE_TIMER_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(BASE_TIMER_INTERVAL));
        registration.registerReadWriteAttribute(T2_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(T2_INTERVAL));
        registration.registerReadWriteAttribute(T4_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(T4_INTERVAL));
        registration.registerReadWriteAttribute(TIMER_D_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(TIMER_D_INTERVAL));
        registration.registerReadWriteAttribute(DIALOG_PENDING_REQUEST_CHECKING, null, new ReloadRequiredWriteAttributeHandler(DIALOG_PENDING_REQUEST_CHECKING));
        registration.registerReadWriteAttribute(CANCELED_TIMER_TASKS_PURGE_PERIOD, null, new ReloadRequiredWriteAttributeHandler(CANCELED_TIMER_TASKS_PURGE_PERIOD));
        registration.registerReadWriteAttribute(MEMORY_THRESHOLD, null, new ReloadRequiredWriteAttributeHandler(MEMORY_THRESHOLD));
        registration.registerReadWriteAttribute(BACK_TO_NORMAL_MEMORY_THRESHOLD, null, new ReloadRequiredWriteAttributeHandler(BACK_TO_NORMAL_MEMORY_THRESHOLD));
        registration.registerReadWriteAttribute(OUTBOUND_PROXY, null, new ReloadRequiredWriteAttributeHandler(OUTBOUND_PROXY));
    }
}
