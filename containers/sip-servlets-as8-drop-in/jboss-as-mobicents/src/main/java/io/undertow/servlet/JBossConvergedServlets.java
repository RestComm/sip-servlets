package io.undertow.servlet;

import io.undertow.servlet.ConvergedServlets;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.JBossConvergedServletContainer;
import io.undertow.servlet.api.ServletContainer;

public class JBossConvergedServlets extends ConvergedServlets{

    private static volatile JBossConvergedServletContainer container;

    protected JBossConvergedServlets() {
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
            return container = JBossConvergedServletContainer.Factory.newInstance();
        }
    }
}
