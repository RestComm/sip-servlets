package io.undertow.servlet.api;

import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.core.JBossConvergedServletContainerImpl;

public interface JBossConvergedServletContainer extends ServletContainer{

    public static class Factory {

        public static JBossConvergedServletContainer newInstance() {
            return new JBossConvergedServletContainerImpl();
        }
    }

}
