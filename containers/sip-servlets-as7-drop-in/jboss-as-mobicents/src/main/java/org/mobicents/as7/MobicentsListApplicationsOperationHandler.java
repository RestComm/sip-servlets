package org.mobicents.as7;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE_TYPE;

import java.util.Iterator;
import java.util.Locale;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public class MobicentsListApplicationsOperationHandler implements OperationStepHandler , DescriptionProvider {

	public static final MobicentsListApplicationsOperationHandler INSTANCE = new MobicentsListApplicationsOperationHandler(); 

	private String operationName = "listSipAppplications";
	private String description = "Get a list of deployed applications";

	@Override
	public ModelNode getModelDescription(Locale locale) {
		final ModelNode result = new ModelNode();
		result.get(OPERATION_NAME).set(operationName);
		result.get(DESCRIPTION).set(description);

		result.get(REPLY_PROPERTIES, TYPE).set(ModelType.OBJECT);
		result.get(REPLY_PROPERTIES, VALUE_TYPE, "AppName").get(TYPE).set(ModelType.LIST);
		result.get(REPLY_PROPERTIES, VALUE_TYPE, "AppName").get(VALUE_TYPE).set(ModelType.STRING);
		return result;
	}

	@Override
	public void execute(OperationContext context, ModelNode operation)
			throws OperationFailedException {

		ModelNode result = context.getResult();
		result.get("AppName").setEmptyList();
		if(context.isNormalServer()){
			SipApplicationDispatcher sipApplicationDispatcher = StaticServiceHolder.sipStandardService.getSipApplicationDispatcher();
			Iterator<SipContext> sipApps = sipApplicationDispatcher.findSipApplications();

			while(sipApps.hasNext()){
				result.get("AppName").add(sipApps.next().getApplicationName());
			}
		} else {
			throw new OperationFailedException(new ModelNode().set("Operation available only for Standalone mode"));
		}
		context.completeStep();	
	}

}
