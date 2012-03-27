package org.mobicents.as7;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * Handler responsible for removing the subsystem resource from the model
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class SipSubsystemRemove extends AbstractRemoveStepHandler {

    static final SipSubsystemRemove INSTANCE = new SipSubsystemRemove();

    private final Logger log = Logger.getLogger(SipSubsystemRemove.class);

    private SipSubsystemRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        //Remove any services installed by the corresponding add handler here
        //context.removeService(ServiceName.of("some", "name"));
    }


}
