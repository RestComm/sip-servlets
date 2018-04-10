package org.mobicents.as7;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NILLABLE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public class MobicentsContextGracefulShutdownOperationHandler implements OperationStepHandler , DescriptionProvider {
	
	public static final MobicentsContextGracefulShutdownOperationHandler INSTANCE = new MobicentsContextGracefulShutdownOperationHandler();
	private final ParametersValidator validator = new ParametersValidator();
	private String operationName = "contextGracefulShutdown";
	private String timeToWaitParamName = "timeToWait";
	private String sipAppParamName = "sipApp";
	
	protected MobicentsContextGracefulShutdownOperationHandler(){
		validator.registerValidator("sipApp", new ModelTypeValidator(ModelType.STRING, true));
		validator.registerValidator("timeToWait", new ModelTypeValidator(ModelType.LONG, true));
	}
	
	@Override
	public void execute(OperationContext context, ModelNode operation)
			throws OperationFailedException {
		
		validator.validate(operation);
		
		String sipApp = operation.require("sipApp").asString();
		Long timeToWait = operation.require("timeToWait").asLong();
		if(context.isNormalServer()){
			SipContext sipContext = StaticServiceHolder.sipStandardService.getSipApplicationDispatcher().findSipApplication(sipApp);

			if(sipContext != null){
				sipContext.stopGracefully(timeToWait);
			} else {
				throw new OperationFailedException("Sip Application with name: "+sipApp+" doesn't exists");
			}
		} else {
			throw new OperationFailedException(new ModelNode().set("Operation available only for Standalone mode"));
		}

		
		context.completeStep();
		
	}

	@Override
	public ModelNode getModelDescription(Locale locale) {
        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(operationName);
        node.get(DESCRIPTION).set("Gracefuly shutdown a SIP Application");

        final ModelNode sipAppParam = node.get(REQUEST_PROPERTIES, sipAppParamName);
        sipAppParam.get(DESCRIPTION).set("SIP Application to shutdown" + "." + sipAppParamName);
        sipAppParam.get(TYPE).set(ModelType.STRING);
        sipAppParam.get(REQUIRED).set(true);
        sipAppParam.get(NILLABLE).set(false);
        
        final ModelNode timeToWaitparam = node.get(REQUEST_PROPERTIES, timeToWaitParamName);
        timeToWaitparam.get(DESCRIPTION).set("Time to wait before shutdown" + "." + timeToWaitParamName);
        timeToWaitparam.get(TYPE).set(ModelType.LONG);
        timeToWaitparam.get(REQUIRED).set(true);
        timeToWaitparam.get(NILLABLE).set(false);

        node.get(REPLY_PROPERTIES).setEmptyObject();

        return node;
	}
	
}
