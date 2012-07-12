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
    protected static final SimpleAttributeDefinition USE_PRETTY_ENCODING =
            new SimpleAttributeDefinitionBuilder(Constants.USE_PRETTY_ENCODING, ModelType.BOOLEAN, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.USE_PRETTY_ENCODING)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(true))
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
        registration.registerReadWriteAttribute(CONCURRENCY_CONTROL_MODE, null, new ReloadRequiredWriteAttributeHandler(CONCURRENCY_CONTROL_MODE));
        registration.registerReadWriteAttribute(USE_PRETTY_ENCODING, null, new ReloadRequiredWriteAttributeHandler(USE_PRETTY_ENCODING));
    }
}
