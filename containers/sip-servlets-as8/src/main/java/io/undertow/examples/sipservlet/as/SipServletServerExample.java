package io.undertow.examples.sipservlet.as;

import static io.undertow.servlet.ConvergedServlets.defaultContainer;
import static io.undertow.servlet.ConvergedServlets.deployment;
import static io.undertow.servlet.ConvergedServlets.servlet;

import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.udp.RootUdpHandler;
import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.handlers.ConvergedApplicationInitHandler;

public class SipServletServerExample {
    public static final String MYAPP = "/my-sipapp";

    public static void main(final String[] args) {
        try {
            DeploymentInfo servletBuilder = deployment().setClassLoader(SipServletServerExample.class.getClassLoader())
                    .setContextPath(MYAPP).setDeploymentName("test.war");

            ((ConvergedDeploymentInfo) servletBuilder).addSipServlets(servlet("HelloSipWorld", HelloSipWorld.class)
                    .addMapping("/*"));

            // ((ConvergedDeploymentInfo)
            // servletBuilder).addSipApplicationListener("io.undertow.examples.sipservlet.SimpleSipServlet");

            // max match dar property:
            ((ConvergedDeploymentInfo) servletBuilder)
                    .setApplicationName("io.undertow.examples.sipservlet.as.HelloSipWorld");

            servletBuilder.addServlets(servlet("MessageServlet", MessageServlet.class).addInitParam("message",
                    "Hello World").addMapping("/*"));

            DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);

            manager.deploy();

            RootUdpHandler rootHandler = new RootUdpHandler();
            ConvergedApplicationInitHandler servletHandler = (ConvergedApplicationInitHandler) manager.start();
            rootHandler.wrapNextHandler(servletHandler);

            PathHandler path = Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP,
                    servletHandler.getHttpHandler());

            Undertow server = Undertow.builder().addHttpListener(8080, "localhost").addUdpListener(5080, "localhost")
                    .setHandler(rootHandler).setHandler(path).build();
            server.start();

        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
