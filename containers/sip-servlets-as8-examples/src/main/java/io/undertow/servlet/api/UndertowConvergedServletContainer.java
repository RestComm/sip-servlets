package io.undertow.servlet.api;

import io.undertow.servlet.core.UndertowConvergedServletContainerImpl;

public interface UndertowConvergedServletContainer extends ServletContainer{

    public static class Factory {

        public static UndertowConvergedServletContainer newInstance() {
            return new UndertowConvergedServletContainerImpl();
        }
    }

}
