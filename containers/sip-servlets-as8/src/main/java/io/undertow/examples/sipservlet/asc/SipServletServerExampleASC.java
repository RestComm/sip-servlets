package io.undertow.examples.sipservlet.asc;

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

public class SipServletServerExampleASC {
    public static final String MYAPP = "/my-sipapp-asc";

    public static void main(final String[] args) {
        try {
            DeploymentInfo servletBuilder = deployment().setClassLoader(SipServletServerExampleASC.class.getClassLoader())
                    .setContextPath(MYAPP).setDeploymentName("test.war");

            ((ConvergedDeploymentInfo) servletBuilder).addSipServlets(servlet("MySipServlet", MySipServlet.class)
                    .addMapping("/*"));

            // ((ConvergedDeploymentInfo)
            // servletBuilder).addSipApplicationListener("io.undertow.examples.sipservlet.SimpleSipServlet");

            // must match dar property:
            ((ConvergedDeploymentInfo) servletBuilder)
                    .setApplicationName("io.undertow.examples.sipservlet.asc.MySipServlet");

            servletBuilder.addServlets(servlet("MyHttpServlet", MyHttpServlet.class).addMapping("/MyHttpServlet"));

            DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);

            manager.deploy();

            ConvergedApplicationInitHandler servletHandler = (ConvergedApplicationInitHandler) manager.start();
            RootUdpHandler rootHandler = new RootUdpHandler();
            rootHandler.wrapNextHandler(servletHandler);
            PathHandler path = Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP,
                    servletHandler.getHttpHandler());

            Undertow server = Undertow.builder().addHttpListener(18080, "localhost").addUdpListener(15080, "localhost")
                    .setHandler(rootHandler).setHandler(path).build();
            server.start();

        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
