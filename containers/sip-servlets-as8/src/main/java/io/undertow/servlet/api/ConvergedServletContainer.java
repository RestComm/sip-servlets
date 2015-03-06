package io.undertow.servlet.api;

import io.undertow.servlet.core.ConvergedServletContainerImpl;

public interface ConvergedServletContainer extends ServletContainer{

    public static class Factory {

        public static ConvergedServletContainer newInstance() {
            return new ConvergedServletContainerImpl();
        }
    }

}
