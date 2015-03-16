package io.undertow.servlet.core;

import org.mobicents.servlet.sip.undertow.UndertowSipManager;

import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SessionManagerFactory;
import io.undertow.servlet.core.InMemorySessionManagerFactory;

public class ConvergedSessionManagerFactory extends InMemorySessionManagerFactory implements SessionManagerFactory{
    private final int maxSessions;

    public ConvergedSessionManagerFactory() {
        this(-1);
    }
    
    public ConvergedSessionManagerFactory(int maxSessions) {
        super(maxSessions);
        this.maxSessions = maxSessions;
    }

    @Override
    public SessionManager createSessionManager(Deployment deployment) {
        return new UndertowSipManager(deployment.getDeploymentInfo().getDeploymentName(), maxSessions);
    }
}
