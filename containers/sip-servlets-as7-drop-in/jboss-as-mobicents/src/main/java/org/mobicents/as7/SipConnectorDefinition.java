package org.mobicents.as7;

import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.InetAddressValidator;
import org.jboss.as.controller.operations.validation.IntRangeValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author Tomaz Cerar
 * @created 22.2.12 15:03
 * @author josemrecio@gmail.com
 */
public class SipConnectorDefinition extends SimpleResourceDefinition {
    protected static final SipConnectorDefinition INSTANCE = new SipConnectorDefinition();


    protected static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder(Constants.NAME, ModelType.STRING)
                    .setXmlName(Constants.NAME)
                    .setAllowNull(true) // todo should be false, but 'add' won't validate then
                    .build();

    protected static final SimpleAttributeDefinition PROTOCOL =
            new SimpleAttributeDefinitionBuilder(Constants.PROTOCOL, ModelType.STRING)
                    .setXmlName(Constants.PROTOCOL)
                    .setAllowNull(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    protected static final SimpleAttributeDefinition SOCKET_BINDING =
            new SimpleAttributeDefinitionBuilder(Constants.SOCKET_BINDING, ModelType.STRING)
                    .setXmlName(Constants.SOCKET_BINDING)
                    .setAllowNull(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    protected static final SimpleAttributeDefinition SCHEME =
            new SimpleAttributeDefinitionBuilder(Constants.SCHEME, ModelType.STRING)
                    .setXmlName(Constants.SCHEME)
                    .setAllowNull(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new StringLengthValidator(1))
                    //.setDefaultValue(new ModelNode("http"))
                    .build();

    protected static final SimpleAttributeDefinition ENABLED =
            new SimpleAttributeDefinitionBuilder(Constants.ENABLED, ModelType.BOOLEAN)
                    .setXmlName(Constants.ENABLED)
                    .setAllowNull(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(true))
                    .build();

    protected static final SimpleAttributeDefinition USE_STATIC_ADDRESS =
            new SimpleAttributeDefinitionBuilder(Constants.USE_STATIC_ADDRESS, ModelType.BOOLEAN)
                    .setXmlName(Constants.USE_STATIC_ADDRESS)
                    .setAllowNull(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(false))
                    .build();

    protected static final SimpleAttributeDefinition STATIC_SERVER_ADDRESS =
            new SimpleAttributeDefinitionBuilder(Constants.STATIC_SERVER_ADDRESS, ModelType.STRING)
                    .setXmlName(Constants.STATIC_SERVER_ADDRESS)
                    .setAllowNull(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new InetAddressValidator(true, false))
                    .setRequires(Constants.USE_STATIC_ADDRESS)
                    .build();

    protected static final SimpleAttributeDefinition STATIC_SERVER_PORT =
            new SimpleAttributeDefinitionBuilder(Constants.STATIC_SERVER_PORT, ModelType.INT)
                    .setXmlName(Constants.STATIC_SERVER_PORT)
                    .setAllowNull(true)
                    .setValidator(new IntRangeValidator(1, true))
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setRequires(Constants.USE_STATIC_ADDRESS)
                    .build();

    protected static final SimpleAttributeDefinition USE_STUN =
            new SimpleAttributeDefinitionBuilder(Constants.USE_STUN, ModelType.BOOLEAN)
                    .setXmlName(Constants.USE_STUN)
                    .setAllowNull(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(false))
                    .build();

    protected static final SimpleAttributeDefinition STUN_SERVER_ADDRESS =
            new SimpleAttributeDefinitionBuilder(Constants.STUN_SERVER_ADDRESS, ModelType.STRING)
                    .setXmlName(Constants.STUN_SERVER_ADDRESS)
                    .setAllowNull(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new InetAddressValidator(true,false))
                    .setRequires(Constants.USE_STUN)
                    .build();

    protected static final SimpleAttributeDefinition STUN_SERVER_PORT =
            new SimpleAttributeDefinitionBuilder(Constants.STUN_SERVER_PORT, ModelType.INT)
                    .setXmlName(Constants.STUN_SERVER_PORT)
                    .setAllowNull(true)
                    .setValidator(new IntRangeValidator(1, true))
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setRequires(Constants.USE_STUN)
                    .build();

    protected static final SimpleAttributeDefinition[] CONNECTOR_ATTRIBUTES = {
            //NAME, // name is read-only
            // IMPORTANT -- keep these in xsd order as this order controls marshalling
            PROTOCOL,
            SCHEME,
            SOCKET_BINDING,
            ENABLED,
            USE_STATIC_ADDRESS,
            STATIC_SERVER_ADDRESS,
            STATIC_SERVER_PORT,
            USE_STUN,
            STUN_SERVER_ADDRESS,
            STUN_SERVER_PORT

    };

    private SipConnectorDefinition() {
        super(SipExtension.CONNECTOR_PATH,
                SipExtension.getResourceDescriptionResolver(Constants.CONNECTOR),
                SipConnectorAdd.INSTANCE,
                SipConnectorRemove.INSTANCE);
    }


    @Override
    public void registerAttributes(ManagementResourceRegistration connectors) {
        connectors.registerReadOnlyAttribute(NAME, null);
        for (SimpleAttributeDefinition def : CONNECTOR_ATTRIBUTES) {
            connectors.registerReadWriteAttribute(def, null, new ReloadRequiredWriteAttributeHandler(def));
        }

        for (final SimpleAttributeDefinition def : SipConnectorMetrics.ATTRIBUTES) {
            connectors.registerMetric(def, SipConnectorMetrics.INSTANCE);
        }
    }
}
