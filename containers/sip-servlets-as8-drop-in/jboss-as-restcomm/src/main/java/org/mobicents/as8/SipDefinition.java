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
package org.mobicents.as8;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.controller.AbstractRuntimeOnlyHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationContext.ResultHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
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
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.mobicents.ext.javax.sip.SipStackImpl;

/**
 * @author Tomaz Cerar
 * @created 22.2.12 14:29
 *
 *          This class is based on the contents of org.mobicents.as7 package from jboss-as7-mobicents project, re-implemented
 *          for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
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
                    // .setValidator(new IntRangeValidator(1, true))
                    .setDefaultValue(new ModelNode(-1))
                    .build();
    protected static final SimpleAttributeDefinition PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE =
            new SimpleAttributeDefinitionBuilder(Constants.PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    // .setValidator(new IntRangeValidator(1, true))
                    .setDefaultValue(new ModelNode("standard"))
                    .build();
    protected static final SimpleAttributeDefinition SAS_TIMER_SERVICE_IMPEMENTATION_TYPE =
            new SimpleAttributeDefinitionBuilder(Constants.SAS_TIMER_SERVICE_IMPEMENTATION_TYPE, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.SAS_TIMER_SERVICE_IMPEMENTATION_TYPE)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    // .setValidator(new IntRangeValidator(1, true))
                    .setDefaultValue(new ModelNode("standard"))
                    .build();
    protected static final SimpleAttributeDefinition CONGESTION_CONTROL_POLICY =
            new SimpleAttributeDefinitionBuilder(Constants.CONGESTION_CONTROL_POLICY, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.CONGESTION_CONTROL_POLICY)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    // .setValidator(new IntRangeValidator(1, true))
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
    protected static final SimpleAttributeDefinition GATHER_STATISTICS =
            new SimpleAttributeDefinitionBuilder(Constants.GATHER_STATISTICS, ModelType.BOOLEAN, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.GATHER_STATISTICS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(false))
                    .build();
    protected static final SimpleAttributeDefinition DNS_SERVER_LOCATOR_CLASS =
            new SimpleAttributeDefinitionBuilder(Constants.DNS_SERVER_LOCATOR_CLASS, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.DNS_SERVER_LOCATOR_CLASS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition DNS_TIMEOUT =
            new SimpleAttributeDefinitionBuilder(Constants.DNS_TIMEOUT, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.DNS_TIMEOUT)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(1))
                    .build();
    protected static final SimpleAttributeDefinition DNS_RESOLVER_CLASS =
            new SimpleAttributeDefinitionBuilder(Constants.DNS_RESOLVER_CLASS, ModelType.STRING, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.DNS_RESOLVER_CLASS)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .build();
    protected static final SimpleAttributeDefinition CALL_ID_MAX_LENGTH =
            new SimpleAttributeDefinitionBuilder(Constants.CALL_ID_MAX_LENGTH, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.CALL_ID_MAX_LENGTH)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(-1))
                    .build();
    protected static final SimpleAttributeDefinition TAG_HASH_MAX_LENGTH =
            new SimpleAttributeDefinitionBuilder(Constants.TAG_HASH_MAX_LENGTH, ModelType.INT, true)
                    .setAllowExpression(true)
                    .setXmlName(Constants.TAG_HASH_MAX_LENGTH)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(-1))
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
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, SipExtension.SUBSYSTEM_NAME), SipExtension
                .getResourceDescriptionResolver(null));
    }

    @Override
    public void registerOperations(final ManagementResourceRegistration rootResourceRegistration) {
        final ResourceDescriptionResolver rootResolver = getResourceDescriptionResolver();
        // Ops to add and remove the root resource
        final DescriptionProvider subsystemAddDescription = new DefaultResourceAddDescriptionProvider(rootResourceRegistration,
                rootResolver);
        rootResourceRegistration.registerOperationHandler(ADD, SipSubsystemAdd.INSTANCE, subsystemAddDescription,
                EnumSet.of(OperationEntry.Flag.RESTART_ALL_SERVICES));
        final DescriptionProvider subsystemRemoveDescription = new DefaultResourceRemoveDescriptionProvider(rootResolver);
        rootResourceRegistration.registerOperationHandler(REMOVE, ReloadRequiredRemoveStepHandler.INSTANCE,
                subsystemRemoveDescription, EnumSet.of(OperationEntry.Flag.RESTART_ALL_SERVICES));
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration registration) {
        registration.registerReadWriteAttribute(INSTANCE_ID, null, new ReloadRequiredWriteAttributeHandler(INSTANCE_ID));
        registration.registerReadWriteAttribute(APPLICATION_ROUTER, null, new ReloadRequiredWriteAttributeHandler(
                APPLICATION_ROUTER));
        registration
                .registerReadWriteAttribute(SIP_STACK_PROPS, null, new ReloadRequiredWriteAttributeHandler(SIP_STACK_PROPS));
        registration.registerReadWriteAttribute(SIP_PATH_NAME, null, new ReloadRequiredWriteAttributeHandler(SIP_PATH_NAME));
        registration.registerReadWriteAttribute(SIP_APP_DISPATCHER_CLASS, null, new ReloadRequiredWriteAttributeHandler(
                SIP_APP_DISPATCHER_CLASS));
        registration.registerReadWriteAttribute(PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE, null, new ReloadRequiredWriteAttributeHandler(
                PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE));
        registration.registerReadWriteAttribute(SAS_TIMER_SERVICE_IMPEMENTATION_TYPE, null, new ReloadRequiredWriteAttributeHandler(
                SAS_TIMER_SERVICE_IMPEMENTATION_TYPE));
        registration.registerReadWriteAttribute(CONGESTION_CONTROL_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(
                CONGESTION_CONTROL_INTERVAL));
        registration.registerReadWriteAttribute(CONGESTION_CONTROL_POLICY, null, new ReloadRequiredWriteAttributeHandler(
                CONGESTION_CONTROL_POLICY));
        registration.registerReadWriteAttribute(CONCURRENCY_CONTROL_MODE, null, new ReloadRequiredWriteAttributeHandler(
                CONCURRENCY_CONTROL_MODE));
        registration.registerReadWriteAttribute(USE_PRETTY_ENCODING, null, new ReloadRequiredWriteAttributeHandler(
                USE_PRETTY_ENCODING));
        registration.registerReadWriteAttribute(ADDITIONAL_PARAMETERABLE_HEADERS, null,
                new ReloadRequiredWriteAttributeHandler(ADDITIONAL_PARAMETERABLE_HEADERS));
        registration.registerReadWriteAttribute(BASE_TIMER_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(
                BASE_TIMER_INTERVAL));
        registration.registerReadWriteAttribute(T2_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(T2_INTERVAL));
        registration.registerReadWriteAttribute(T4_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(T4_INTERVAL));
        registration.registerReadWriteAttribute(TIMER_D_INTERVAL, null, new ReloadRequiredWriteAttributeHandler(
                TIMER_D_INTERVAL));
        registration.registerReadWriteAttribute(DIALOG_PENDING_REQUEST_CHECKING, null, new ReloadRequiredWriteAttributeHandler(
                DIALOG_PENDING_REQUEST_CHECKING));
        registration.registerReadWriteAttribute(GATHER_STATISTICS, null, new ReloadRequiredWriteAttributeHandler(
        		GATHER_STATISTICS));
        registration.registerReadWriteAttribute(CALL_ID_MAX_LENGTH, null, new ReloadRequiredWriteAttributeHandler(
        		CALL_ID_MAX_LENGTH));
        registration.registerReadWriteAttribute(TAG_HASH_MAX_LENGTH, null, new ReloadRequiredWriteAttributeHandler(
        		TAG_HASH_MAX_LENGTH));
        registration.registerReadWriteAttribute(DNS_SERVER_LOCATOR_CLASS, null, new ReloadRequiredWriteAttributeHandler(
                DNS_SERVER_LOCATOR_CLASS));
        registration.registerReadWriteAttribute(DNS_TIMEOUT, null, new ReloadRequiredWriteAttributeHandler(DNS_TIMEOUT));
        registration.registerReadWriteAttribute(DNS_RESOLVER_CLASS, null, new ReloadRequiredWriteAttributeHandler(
                DNS_RESOLVER_CLASS));
        registration.registerReadWriteAttribute(CANCELED_TIMER_TASKS_PURGE_PERIOD, null,
                new ReloadRequiredWriteAttributeHandler(CANCELED_TIMER_TASKS_PURGE_PERIOD));
        registration.registerReadWriteAttribute(MEMORY_THRESHOLD, null, new ReloadRequiredWriteAttributeHandler(
                MEMORY_THRESHOLD));
        registration.registerReadWriteAttribute(BACK_TO_NORMAL_MEMORY_THRESHOLD, null, new ReloadRequiredWriteAttributeHandler(
                BACK_TO_NORMAL_MEMORY_THRESHOLD));
        registration.registerReadWriteAttribute(OUTBOUND_PROXY, null, new ReloadRequiredWriteAttributeHandler(OUTBOUND_PROXY));
        for (SipStackStat stat : SipStackStat.values()) {
            registration.registerMetric(stat.definition, SipStackStatsHandler.getInstance());
        }
        for (SipApplicationDispatcherStat stat : SipApplicationDispatcherStat.values()) {
            registration.registerMetric(stat.definition, SipApplicationDispatcherStatsHandler.getInstance());
        }
    }

    static class SipStackStatsHandler extends AbstractRuntimeOnlyHandler {

        static SipStackStatsHandler INSTANCE = new SipStackStatsHandler();

        private SipStackStatsHandler() {
        }

        public static SipStackStatsHandler getInstance() {
            return INSTANCE;
        }

        @Override
        protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {

        	final PathAddress address = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));

            final Resource sip = context.readResourceFromRoot(address.subAddress(0, address.size()), false);

        	final ServiceController<?> controller = context.getServiceRegistry(false).getService(SipSubsystemServices.JBOSS_SIP);
        	SipStackStat stat = SipStackStat.getStat(operation.require(ModelDescriptionConstants.NAME).asString());

        	if (stat == null) {
        		context.getFailureDescription().set(SipMessages.MESSAGES.unknownMetric(operation.require(ModelDescriptionConstants.NAME).asString()));
            } else {
                final SipServerService sipServerService = SipServerService.class.cast(controller.getValue());
                ModelNode result = new ModelNode();
                switch (stat) {
                    case NUMBER_CLIENT_TRANSACTIONS:
                    	result.set(((SipStackImpl)sipServerService.getSipService().getSipStack()).getNumberOfClientTransactions());
                        break;
                    case NUMBER_SERVER_TRANSACTIONS:
                    	result.set(((SipStackImpl)sipServerService.getSipService().getSipStack()).getNumberOfServerTransactions());
                        break;
                    case NUMBER_DIALOG:
                    	result.set(((SipStackImpl)sipServerService.getSipService().getSipStack()).getNumberOfDialogs());
                        break;
                    case NUMBER_EARY_DIALOG:
                    	result.set(((SipStackImpl)sipServerService.getSipService().getSipStack()).getNumberOfEarlyDialogs());
                        break;
                    default:
                        throw new IllegalStateException(SipMessages.MESSAGES.unknownMetric(stat));
                }
                context.getResult().set(result);
            }

            context.completeStep(ResultHandler.NOOP_RESULT_HANDLER);
        }

    }

    public enum SipStackStat {
        NUMBER_CLIENT_TRANSACTIONS(new SimpleAttributeDefinition("number-client-transaction", ModelType.INT, false)),
        NUMBER_SERVER_TRANSACTIONS(new SimpleAttributeDefinition("number-server-transaction", ModelType.INT, false)),
        NUMBER_DIALOG(new SimpleAttributeDefinition("number-dialog", ModelType.INT, false)),
        NUMBER_EARY_DIALOG(new SimpleAttributeDefinition("number-early-dialog", ModelType.INT, false));
//        EXPIRED_SIP_APP_SESSIONS(new SimpleAttributeDefinition("expired-sip-application-sessions", ModelType.INT, false)),
//        SIP_SESSIONS_CREATED(new SimpleAttributeDefinition("sip-sessions-created", ModelType.INT, false)),
//        SIP_APP_SESSIONS_CREATED(new SimpleAttributeDefinition("sip-application-sessions-created", ModelType.INT, false)),
//        SIP_SESSIONS_CREATION_RATE(new SimpleAttributeDefinition("sip-sessions-per-sec", ModelType.INT, false)),
//        SIP_APP_SESSIONS_CREATION_RATE(new SimpleAttributeDefinition("sip-application-sessions-per-sec", ModelType.INT, false)),
//        SIP_SESSION_AVG_ALIVE_TIME(new SimpleAttributeDefinition("sip-session-avg-alive-time", ModelType.INT, false)),
//        SIP_APP_SESSION_AVG_ALIVE_TIME(new SimpleAttributeDefinition("sip-application-session-avg-alive-time", ModelType.INT, false)),
//        SIP_SESSION_MAX_ALIVE_TIME(new SimpleAttributeDefinition("sip-session-max-alive-time", ModelType.INT, false)),
//        SIP_APP_SESSION_MAX_ALIVE_TIME(new SimpleAttributeDefinition("sip-application-session-max-alive-time", ModelType.INT, false)),
//        REJECTED_SIP_SESSIONS(new SimpleAttributeDefinition("rejected-sip-sessions", ModelType.INT, false)),
//        REJECTED_SIP_APP_SESSIONS(new SimpleAttributeDefinition("rejected-sip-application-sessions", ModelType.INT, false)),
//        MAX_ACTIVE_SIP_SESSIONS(new SimpleAttributeDefinition("max-active-sip-sessions", ModelType.INT, false));

        private static final Map<String, SipStackStat> MAP = new HashMap<String, SipStackStat>();

        static {
            for (SipStackStat stat : EnumSet.allOf(SipStackStat.class)) {
                MAP.put(stat.toString(), stat);
            }
        }

        final AttributeDefinition definition;

        private SipStackStat(final AttributeDefinition definition) {
            this.definition = definition;
        }

        @Override
        public final String toString() {
            return definition.getName();
        }

        public static synchronized SipStackStat getStat(final String stringForm) {
            return MAP.get(stringForm);
        }
    }
    
    static class SipApplicationDispatcherStatsHandler extends AbstractRuntimeOnlyHandler {

        static SipApplicationDispatcherStatsHandler INSTANCE = new SipApplicationDispatcherStatsHandler();

        private SipApplicationDispatcherStatsHandler() {
        }

        public static SipApplicationDispatcherStatsHandler getInstance() {
            return INSTANCE;
        }

        @Override
        protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {

        	final PathAddress address = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));

            final Resource sip = context.readResourceFromRoot(address.subAddress(0, address.size()), false);

        	final ServiceController<?> controller = context.getServiceRegistry(false).getService(SipSubsystemServices.JBOSS_SIP);
        	SipApplicationDispatcherStat stat = SipApplicationDispatcherStat.getStat(operation.require(ModelDescriptionConstants.NAME).asString());

        	if (stat == null) {
        		context.getFailureDescription().set(SipMessages.MESSAGES.unknownMetric(operation.require(ModelDescriptionConstants.NAME).asString()));
            } else {
                final SipServerService sipServerService = SipServerService.class.cast(controller.getValue());
                ModelNode result = new ModelNode();
                switch (stat) {
                case PRACK_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("PRACK").longValue());
                    break;
                case SUBSCRIBE_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("SUBSCRIBE").longValue());
                    break;
                case INVITE_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("INVITE").longValue());
                    break;
                case REFER_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("REFER").longValue());
                    break;
                case ACK_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("ACK").longValue());
                    break;
                case NOTIFY_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("NOTIFY").longValue());
                    break;
                case MESSAGE_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("MESSAGE").longValue());
                    break;
                case REGISTER_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("REGISTER").longValue());
                    break;
                case BYE_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("BYE").longValue());
                    break;
                case OPTIONS_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("OPTIONS").longValue());
                    break;
                case INFO_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("INFO").longValue());
                    break;
                case PUBLISH_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("PUBLISH").longValue());
                    break;
                case UPDATE_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("UPDATE").longValue());
                    break;
                case CANCEL_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod().get("CANCEL").longValue());
                    break;
                case PRACK_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("PRACK").longValue());
                    break;
                case SUBSCRIBE_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("SUBSCRIBE").longValue());
                    break;
                case INVITE_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("INVITE").longValue());
                    break;
                case REFER_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("REFER").longValue());
                    break;
                case ACK_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("ACK").longValue());
                    break;
                case NOTIFY_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("NOTIFY").longValue());
                    break;
                case MESSAGE_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("MESSAGE").longValue());
                    break;
                case REGISTER_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("REGISTER").longValue());
                    break;
                case BYE_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("BYE").longValue());
                    break;
                case OPTIONS_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("OPTIONS").longValue());
                    break;
                case INFO_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("INFO").longValue());
                    break;
                case PUBLISH_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("PUBLISH").longValue());
                    break;
                case UPDATE_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("UPDATE").longValue());
                    break;
                case CANCEL_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod().get("CANCEL").longValue());
                    break;
                case ONEXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("1XX").longValue());
                    break;
                case TWOXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("2XX").longValue());
                    break;
                case THREEXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("3XX").longValue());
                    break;
                case FOURXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("4XX").longValue());
                    break;
                case FIVEXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("5XX").longValue());
                    break;
                case SIXXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("6XX").longValue());
                    break;
                case SEVENXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("7XX").longValue());
                    break;
                case EIGHTXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("8XX").longValue());
                    break;
                case NINEXX_PROCESSED:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode().get("9XX").longValue());
                    break;
                case ONEXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("1XX").longValue());
                    break;
                case TWOXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("2XX").longValue());
                    break;
                case THREEXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("3XX").longValue());
                    break;
                case FOURXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("4XX").longValue());
                    break;
                case FIVEXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("5XX").longValue());
                    break;
                case SIXXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("6XX").longValue());
                    break;
                case SEVENXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("7XX").longValue());
                    break;
                case EIGHTXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("8XX").longValue());
                    break;
                case NINEXX_SENT:
                	result.set(sipServerService.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode().get("9XX").longValue());
                    break;
                default:
                    throw new IllegalStateException(SipMessages.MESSAGES.unknownMetric(stat));
                }
                context.getResult().set(result);
            }

            context.completeStep(ResultHandler.NOOP_RESULT_HANDLER);
        }

    }

    public enum SipApplicationDispatcherStat {
    	PRACK_PROCESSED(new SimpleAttributeDefinition("prack-processed", ModelType.STRING, false)),
    	SUBSCRIBE_PROCESSED(new SimpleAttributeDefinition("subscribe-processed", ModelType.STRING, false)),
    	INVITE_PROCESSED(new SimpleAttributeDefinition("invite-processed", ModelType.STRING, false)),
    	REFER_PROCESSED(new SimpleAttributeDefinition("refer-processed", ModelType.STRING, false)),
    	ACK_PROCESSED(new SimpleAttributeDefinition("ack-processed", ModelType.STRING, false)),
    	NOTIFY_PROCESSED(new SimpleAttributeDefinition("notify-processed", ModelType.STRING, false)),
    	MESSAGE_PROCESSED(new SimpleAttributeDefinition("message-processed", ModelType.STRING, false)),
    	REGISTER_PROCESSED(new SimpleAttributeDefinition("register-processed", ModelType.STRING, false)),
    	BYE_PROCESSED(new SimpleAttributeDefinition("bye-processed", ModelType.STRING, false)),
    	OPTIONS_PROCESSED(new SimpleAttributeDefinition("options-processed", ModelType.STRING, false)),
    	INFO_PROCESSED(new SimpleAttributeDefinition("info-processed", ModelType.STRING, false)),
    	PUBLISH_PROCESSED(new SimpleAttributeDefinition("publish-processed", ModelType.STRING, false)),
    	UPDATE_PROCESSED(new SimpleAttributeDefinition("update-processed", ModelType.STRING, false)),
    	CANCEL_PROCESSED(new SimpleAttributeDefinition("cancel-processed", ModelType.STRING, false)),
    	
    	ONEXX_PROCESSED(new SimpleAttributeDefinition("1xx-processed", ModelType.STRING, false)),
    	TWOXX_PROCESSED(new SimpleAttributeDefinition("2xx-processed", ModelType.STRING, false)),
    	THREEXX_PROCESSED(new SimpleAttributeDefinition("3xx-processed", ModelType.STRING, false)),
    	FOURXX_PROCESSED(new SimpleAttributeDefinition("4xx-processed", ModelType.STRING, false)),
    	FIVEXX_PROCESSED(new SimpleAttributeDefinition("5xx-processed", ModelType.STRING, false)),
    	SIXXX_PROCESSED(new SimpleAttributeDefinition("6xx-processed", ModelType.STRING, false)),
    	SEVENXX_PROCESSED(new SimpleAttributeDefinition("7xx-processed", ModelType.STRING, false)),
    	EIGHTXX_PROCESSED(new SimpleAttributeDefinition("8xx-processed", ModelType.STRING, false)),
    	NINEXX_PROCESSED(new SimpleAttributeDefinition("9xx-processed", ModelType.STRING, false)),
    	
    	PRACK_SENT(new SimpleAttributeDefinition("prack-sent", ModelType.STRING, false)),
    	SUBSCRIBE_SENT(new SimpleAttributeDefinition("subscribe-sent", ModelType.STRING, false)),
    	INVITE_SENT(new SimpleAttributeDefinition("invite-sent", ModelType.STRING, false)),
    	REFER_SENT(new SimpleAttributeDefinition("refer-sent", ModelType.STRING, false)),
    	ACK_SENT(new SimpleAttributeDefinition("ack-sent", ModelType.STRING, false)),
    	NOTIFY_SENT(new SimpleAttributeDefinition("notify-sent", ModelType.STRING, false)),
    	MESSAGE_SENT(new SimpleAttributeDefinition("message-sent", ModelType.STRING, false)),
    	REGISTER_SENT(new SimpleAttributeDefinition("register-sent", ModelType.STRING, false)),
    	BYE_SENT(new SimpleAttributeDefinition("bye-sent", ModelType.STRING, false)),
    	OPTIONS_SENT(new SimpleAttributeDefinition("options-sent", ModelType.STRING, false)),
    	INFO_SENT(new SimpleAttributeDefinition("info-sent", ModelType.STRING, false)),
    	PUBLISH_SENT(new SimpleAttributeDefinition("publish-sent", ModelType.STRING, false)),
    	UPDATE_SENT(new SimpleAttributeDefinition("update-sent", ModelType.STRING, false)),
    	CANCEL_SENT(new SimpleAttributeDefinition("cancel-sent", ModelType.STRING, false)),
    	
    	ONEXX_SENT(new SimpleAttributeDefinition("1xx-sent", ModelType.STRING, false)),
    	TWOXX_SENT(new SimpleAttributeDefinition("2xx-sent", ModelType.STRING, false)),
    	THREEXX_SENT(new SimpleAttributeDefinition("3xx-sent", ModelType.STRING, false)),
    	FOURXX_SENT(new SimpleAttributeDefinition("4xx-sent", ModelType.STRING, false)),
    	FIVEXX_SENT(new SimpleAttributeDefinition("5xx-sent", ModelType.STRING, false)),
    	SIXXX_SENT(new SimpleAttributeDefinition("6xx-sent", ModelType.STRING, false)),
    	SEVENXX_SENT(new SimpleAttributeDefinition("7xx-sent", ModelType.STRING, false)),
    	EIGHTXX_SENT(new SimpleAttributeDefinition("8xx-sent", ModelType.STRING, false)),
    	NINEXX_SENT(new SimpleAttributeDefinition("9xx-sent", ModelType.STRING, false));

        private static final Map<String, SipApplicationDispatcherStat> MAP = new HashMap<String, SipApplicationDispatcherStat>();

        static {
            for (SipApplicationDispatcherStat stat : EnumSet.allOf(SipApplicationDispatcherStat.class)) {
                MAP.put(stat.toString(), stat);
            }
        }

        final AttributeDefinition definition;

        private SipApplicationDispatcherStat(final AttributeDefinition definition) {
            this.definition = definition;
        }

        @Override
        public final String toString() {
            return definition.getName();
        }

        public static synchronized SipApplicationDispatcherStat getStat(final String stringForm) {
            return MAP.get(stringForm);
        }
    }
}
