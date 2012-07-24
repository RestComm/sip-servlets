package org.mobicents.as7;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.controller.AbstractRuntimeOnlyHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.mobicents.as7.deployment.SIPWebContext;
import org.mobicents.servlet.sip.core.SipManager;

/**
 * @author Tomaz Cerar
 * @author <a href="mailto:torben@jit-central.com">Torben Jaeger</a>
 * @created 23.2.12 18:32
 * @author josemrecio@gmail.com
 */
public class SipDeploymentDefinition extends SimpleResourceDefinition {
    public static final SipDeploymentDefinition INSTANCE = new SipDeploymentDefinition();

    private SipDeploymentDefinition() {
        super(PathElement.pathElement(SUBSYSTEM, SipExtension.SUBSYSTEM_NAME),
              SipExtension.getResourceDescriptionResolver("deployment"));
    }


    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        for (SessionStat stat : SessionStat.values()) {
            resourceRegistration.registerMetric(stat.definition, SessionManagerStatsHandler.getInstance());
        }
    }

    static class SessionManagerStatsHandler extends AbstractRuntimeOnlyHandler {

        static SessionManagerStatsHandler INSTANCE = new SessionManagerStatsHandler();

        private SessionManagerStatsHandler() {
        }

        public static SessionManagerStatsHandler getInstance() {
            return INSTANCE;
        }

        @Override
        protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {

        	final ServiceController<?> controller = context.getServiceRegistry(false).getService(SipSubsystemServices.deploymentServiceName());
        	SessionStat stat = SessionStat.getStat(operation.require(ModelDescriptionConstants.NAME).asString());

        	if (stat == null) {
        		context.getFailureDescription().set(SipMessages.MESSAGES.unknownMetric(operation.require(ModelDescriptionConstants.NAME).asString()));
            } else {
                final SIPWebContext sipContext = SIPWebContext.class.cast(controller.getValue());
                SipManager sm = (SipManager) sipContext.getManager();
                ModelNode result = new ModelNode();
                switch (stat) {
                    case ACTIVE_SIP_SESSIONS:
//                    	// TODO: what about other manager implementations?
//                    	if (sm.getDistributable() && (sm instanceof DistributableSessionManager)) {
//                    		result.set(((DistributableSessionManager)sm).getActiveSessionCount());
//                    	}
                    	result.set(sm.getActiveSipSessions());
                        break;
                    case ACTIVE_SIP_APP_SESSIONS:
//                    	// TODO: what about other manager implementations?
//                    	if (sm.getDistributable() && (sm instanceof DistributableSessionManager)) {
//                    		result.set(((DistributableSessionManager)sm).getActiveSessionCount());
//                    	}
                    	result.set(sm.getActiveSipApplicationSessions());
                        break;
                    case EXPIRED_SIP_SESSIONS:
                        result.set(sm.getExpiredSipSessions());
                        break;
                    case EXPIRED_SIP_APP_SESSIONS:
                        result.set(sm.getExpiredSipApplicationSessions());
                        break;
                    case MAX_ACTIVE_SIP_SESSIONS:
                        result.set(sm.getMaxActiveSipSessions());
                        break;
                    case SIP_SESSIONS_CREATED:
                        result.set(sm.getSipSessionCounter());
                        break;
                    case SIP_APP_SESSIONS_CREATED:
                        result.set(sm.getSipApplicationSessionCounter());
                        break;
                    case SIP_SESSIONS_CREATION_RATE:
                        result.set(sm.getNumberOfSipSessionCreationPerSecond());
                        break;
                    case SIP_APP_SESSIONS_CREATION_RATE:
                        result.set(sm.getNumberOfSipApplicationSessionCreationPerSecond());
                        break;
                    case SIP_SESSION_AVG_ALIVE_TIME:
                        result.set(sm.getSipSessionAverageAliveTime());
                        break;
                    case SIP_APP_SESSION_AVG_ALIVE_TIME:
                        result.set(sm.getSipApplicationSessionAverageAliveTime());
                        break;
                    case SIP_SESSION_MAX_ALIVE_TIME:
                        result.set(sm.getSipSessionMaxAliveTime());
                        break;
                    case SIP_APP_SESSION_MAX_ALIVE_TIME:
                        result.set(sm.getSipApplicationSessionMaxAliveTime());
                        break;
                    case REJECTED_SIP_SESSIONS:
                        result.set(sm.getRejectedSipSessions());
                        break;
                    case REJECTED_SIP_APP_SESSIONS:
                        result.set(sm.getRejectedSipSessions());
                        break;
                    default:
                        throw new IllegalStateException(SipMessages.MESSAGES.unknownMetric(stat));
                }
                context.getResult().set(result);
            }

            context.completeStep();
        }

    }

    public enum SessionStat {
        ACTIVE_SIP_SESSIONS(new SimpleAttributeDefinition("active-sip-sessions", ModelType.INT, false)),
        ACTIVE_SIP_APP_SESSIONS(new SimpleAttributeDefinition("active-sip-application-sessions", ModelType.INT, false)),
        EXPIRED_SIP_SESSIONS(new SimpleAttributeDefinition("expired-sip-sessions", ModelType.INT, false)),
        EXPIRED_SIP_APP_SESSIONS(new SimpleAttributeDefinition("expired-sip-application-sessions", ModelType.INT, false)),
        SIP_SESSIONS_CREATED(new SimpleAttributeDefinition("sip-sessions-created", ModelType.INT, false)),
        SIP_APP_SESSIONS_CREATED(new SimpleAttributeDefinition("sip-application-sessions-created", ModelType.INT, false)),
        SIP_SESSIONS_CREATION_RATE(new SimpleAttributeDefinition("sip-sessions-per-sec", ModelType.INT, false)),
        SIP_APP_SESSIONS_CREATION_RATE(new SimpleAttributeDefinition("sip-application-sessions-per-sec", ModelType.INT, false)),
        SIP_SESSION_AVG_ALIVE_TIME(new SimpleAttributeDefinition("sip-session-avg-alive-time", ModelType.INT, false)),
        SIP_APP_SESSION_AVG_ALIVE_TIME(new SimpleAttributeDefinition("sip-application-session-avg-alive-time", ModelType.INT, false)),
        SIP_SESSION_MAX_ALIVE_TIME(new SimpleAttributeDefinition("sip-session-max-alive-time", ModelType.INT, false)),
        SIP_APP_SESSION_MAX_ALIVE_TIME(new SimpleAttributeDefinition("sip-application-session-max-alive-time", ModelType.INT, false)),
        REJECTED_SIP_SESSIONS(new SimpleAttributeDefinition("rejected-sip-sessions", ModelType.INT, false)),
        REJECTED_SIP_APP_SESSIONS(new SimpleAttributeDefinition("rejected-sip-application-sessions", ModelType.INT, false)),
        MAX_ACTIVE_SIP_SESSIONS(new SimpleAttributeDefinition("max-active-sip-sessions", ModelType.INT, false));

        private static final Map<String, SessionStat> MAP = new HashMap<String, SessionStat>();

        static {
            for (SessionStat stat : EnumSet.allOf(SessionStat.class)) {
                MAP.put(stat.toString(), stat);
            }
        }

        final AttributeDefinition definition;

        private SessionStat(final AttributeDefinition definition) {
            this.definition = definition;
        }

        @Override
        public final String toString() {
            return definition.getName();
        }

        public static synchronized SessionStat getStat(final String stringForm) {
            return MAP.get(stringForm);
        }
    }

}
