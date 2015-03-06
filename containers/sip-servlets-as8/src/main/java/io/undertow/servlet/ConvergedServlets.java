package io.undertow.servlet;

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.ConvergedServletContainer;
import io.undertow.servlet.api.ServletContainer;

public class ConvergedServlets extends Servlets {

    private static volatile ConvergedServletContainer container;

    protected ConvergedServlets() {
        super();
    }

    public static ConvergedDeploymentInfo deployment() {
        return new ConvergedDeploymentInfo();
    }

    public static ServletContainer defaultContainer() {
        if (container != null) {
            return container;
        }
        synchronized (Servlets.class) {
            if (container != null) {
                return container;
            }
            return container = ConvergedServletContainer.Factory.newInstance();
        }
    }

}
