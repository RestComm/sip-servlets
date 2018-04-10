package org.mobicents.as7;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public class MobicentsGracefulShutdownOperationHandler implements OperationStepHandler , DescriptionProvider {
	
	public static final MobicentsGracefulShutdownOperationHandler INSTANCE = new MobicentsGracefulShutdownOperationHandler();
	private final ParametersValidator validator = new ParametersValidator();
	
	protected MobicentsGracefulShutdownOperationHandler(){
		validator.registerValidator("timeToWait", new ModelTypeValidator(ModelType.LONG, true));
	}
	
	@Override
	public void execute(OperationContext context, ModelNode operation)
			throws OperationFailedException {
		validator.validate(operation);
		Long timeToWait = operation.require("timeToWait").asLong();
		if(context.isNormalServer()){
			StaticServiceHolder.sipStandardService.stopGracefully(timeToWait);
		} else {
			throw new OperationFailedException(new ModelNode().set("Operation available only for Standalone mode"));
		}

		context.completeStep();
	}

	@Override
	public ModelNode getModelDescription(Locale locale) {
        final ResourceBundle bundle = ResourceBundle.getBundle(MobicentsGracefulShutdownOperationHandler.class.getPackage().getName()+".LocalDescriptions");
		return CommonDescriptions.getSingleParamOnlyOperation(bundle, "gracefulShutdown", "sip", "timeToWait", ModelType.LONG, false);
	}

}
