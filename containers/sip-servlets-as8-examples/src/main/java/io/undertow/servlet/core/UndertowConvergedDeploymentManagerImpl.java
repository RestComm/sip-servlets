package io.undertow.servlet.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.mobicents.servlet.sip.undertow.UndertowSipContextDeployment;

import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.core.Lifecycle;
import io.undertow.servlet.core.ManagedFilter;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.handlers.ConvergedApplicationInitHandler;
import io.undertow.servlet.handlers.ServletHandler;


public class UndertowConvergedDeploymentManagerImpl extends ConvergedDeploymentManagerImpl implements DeploymentManager {

    private final DeploymentInfo originalDeployment;
    private volatile State state = State.UNDEPLOYED;

    public UndertowConvergedDeploymentManagerImpl(DeploymentInfo deployment, ServletContainer servletContainer) {
        super(deployment, servletContainer);
        originalDeployment = deployment;
    }

    @Override
    public void stop() throws ServletException {
        super.stop();
        state = super.getState();
    }
    
    @Override
    public void undeploy() {
        super.undeploy();
        state = super.getState();
    }

    @Override
    public HttpHandler start() throws ServletException {
        if (originalDeployment instanceof ConvergedDeploymentInfo) {

            ThreadSetupAction.Handle handle = deployment.getThreadSetupAction().setup(null);
            try {
                deployment.getSessionManager().start();

                // we need to copy before iterating
                // because listeners can add other listeners
                ArrayList<Lifecycle> lifecycles = new ArrayList<>(deployment.getLifecycleObjects());
                for (Lifecycle object : lifecycles) {
                    object.start();
                }
                HttpHandler root = deployment.getHandler();
                final TreeMap<Integer, List<ManagedServlet>> loadOnStartup = new TreeMap<>();
                for (Map.Entry<String, ServletHandler> entry : deployment.getServlets().getServletHandlers().entrySet()) {
                    ManagedServlet servlet = entry.getValue().getManagedServlet();
                    Integer loadOnStartupNumber = servlet.getServletInfo().getLoadOnStartup();
                    if (loadOnStartupNumber != null) {
                        if (loadOnStartupNumber < 0) {
                            continue;
                        }
                        List<ManagedServlet> list = loadOnStartup.get(loadOnStartupNumber);
                        if (list == null) {
                            loadOnStartup.put(loadOnStartupNumber, list = new ArrayList<>());
                        }
                        list.add(servlet);
                    }
                }
                for (Map.Entry<Integer, List<ManagedServlet>> load : loadOnStartup.entrySet()) {
                    for (ManagedServlet servlet : load.getValue()) {
                        servlet.createServlet();
                    }
                }

                if (deployment.getDeploymentInfo().isEagerFilterInit()) {
                    for (ManagedFilter filter : deployment.getFilters().getFilters().values()) {
                        filter.createFilter();
                    }
                }

                ((UndertowSipContextDeployment) deployment).contextListenerStart();

                ConvergedApplicationInitHandler handler = new ConvergedApplicationInitHandler();
                handler.setHttpHandler(root);
                handler.setDeployment((UndertowSipContextDeployment) deployment);

                state = State.STARTED;

                return handler;
            } finally {
                handle.tearDown();
            }

        } else {
            HttpHandler handler = super.start();
            state = super.getState();
            return handler;
        }
    }
    
    public State getState() {
        return state;
    }

}
