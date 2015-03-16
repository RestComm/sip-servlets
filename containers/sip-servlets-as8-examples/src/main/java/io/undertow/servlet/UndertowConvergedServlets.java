package io.undertow.servlet;

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.UndertowConvergedServletContainer;
import io.undertow.servlet.api.ServletContainer;

public class UndertowConvergedServlets extends ConvergedServlets {

    private static volatile UndertowConvergedServletContainer container;

    protected UndertowConvergedServlets() {
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
            return container = UndertowConvergedServletContainer.Factory.newInstance();
        }
    }

}
