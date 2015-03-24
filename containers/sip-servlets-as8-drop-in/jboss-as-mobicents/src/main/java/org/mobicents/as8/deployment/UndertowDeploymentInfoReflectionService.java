package org.mobicents.as8.deployment;

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.DeploymentInfo;

import java.lang.reflect.Field;

import org.jboss.as.server.ServerLogger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;

public class UndertowDeploymentInfoReflectionService implements Service<UndertowDeploymentInfoReflectionService> {
    public static final ServiceName SERVICE_NAME = ServiceName.of("UndertowDeploymentInfoReflectionService");

    private static final String DEPLOYMENTINFOFIELDNAME = "deploymentInfo";

    private InjectedValue<UndertowDeploymentInfoService> deploymentInfoService = new InjectedValue<UndertowDeploymentInfoService>();

    @Override
    public UndertowDeploymentInfoReflectionService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start()");
        DeploymentInfo info = this.deploymentInfoService.getValue().getValue();
        ConvergedDeploymentInfo convergedInfo = new ConvergedDeploymentInfo(info);

        Class<? extends UndertowDeploymentInfoService> serviceClass = deploymentInfoService.getValue().getClass();
        for (Field field : serviceClass.getDeclaredFields()) {
            if (UndertowDeploymentInfoReflectionService.DEPLOYMENTINFOFIELDNAME.equals(field.getName())) {
                field.setAccessible(true);
                try {
                    field.set(deploymentInfoService.getValue(), convergedInfo);
                    /*
                     * this.deploymentService.getValue().getDeploymentInfoInjectedValue().setValue( new
                     * ImmediateValue<DeploymentInfo>(convergedInfo));
                     */
                    ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start() deploymentInfo injected:"
                            + convergedInfo);
                } catch (IllegalArgumentException e) {
                    ServerLogger.DEPLOYMENT_LOGGER.error(e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    ServerLogger.DEPLOYMENT_LOGGER.error(e.getMessage(),e);
                }
                field.setAccessible(false);
                break;
            }
        }
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start() finished");
    }

    public InjectedValue<UndertowDeploymentInfoService> getDeploymentInfoServiceInjectedValue() {
        return deploymentInfoService;
    }

    @Override
    public void stop(StopContext context) {
        // TODO Auto-generated method stub
    }
}
