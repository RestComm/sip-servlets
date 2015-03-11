package io.undertow.servlet.core;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletException;
import org.mobicents.servlet.sip.undertow.SipServletImpl;
import org.mobicents.servlet.sip.undertow.UndertowSipContextDeployment;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.HttpContinueReadHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.UndertowServletLogger;
import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.MetricsCollector;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.api.DeploymentManager.State;
import io.undertow.servlet.handlers.ConvergedApplicationInitHandler;
import io.undertow.servlet.handlers.ServletDispatchingHandler;
import io.undertow.servlet.handlers.ServletHandler;
import io.undertow.servlet.handlers.ServletInitialHandler;
import io.undertow.servlet.predicate.DispatcherTypePredicate;
import io.undertow.servlet.spec.ServletContextImpl;

public class ConvergedDeploymentManagerImpl extends DeploymentManagerImpl implements DeploymentManager {

    private final DeploymentInfo originalDeployment;
    private final ServletContainer servletContainer;
    private volatile State state = State.UNDEPLOYED;

    public ConvergedDeploymentManagerImpl(DeploymentInfo deployment, ServletContainer servletContainer) {
        super(deployment, servletContainer);
        originalDeployment = deployment;
        this.servletContainer = servletContainer;
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
            return super.start();
        }
    }

    @Override
    public void deploy() {
        if (originalDeployment instanceof ConvergedDeploymentInfo) {
            ConvergedDeploymentInfo deploymentInfo = (ConvergedDeploymentInfo) originalDeployment;
            Map<String, ServletInfo> sipServlets = deploymentInfo.getSipServlets();

            if (deploymentInfo.getServletStackTraces() == ServletStackTraces.ALL) {
                UndertowServletLogger.REQUEST_LOGGER.servletStackTracesAll(deploymentInfo.getDeploymentName());
            }

            deploymentInfo.validate();
            final UndertowSipContextDeployment deployment = new UndertowSipContextDeployment(this, deploymentInfo,
                    servletContainer);
            this.deployment = deployment;

            final ServletContextImpl servletContext = deployment.getServletContext();// new
                                                                                     // ServletContextImpl(servletContainer,
                                                                                     // deployment);

            for (ServletInfo info : sipServlets.values()) {
                SipServletImpl servlet = new SipServletImpl(info, servletContext);
                servlet.setDescription(info.getName());
                servlet.setDisplayName(info.getName());
                servlet.setServletName(info.getName());
                servlet.setupMultipart(servletContext);

                deployment.addChild(servlet);
            }

            // deployment.setServletContext(servletContext);

            handleExtensions(deploymentInfo, servletContext);

            deployment.setDefaultCharset(Charset.forName(deploymentInfo.getDefaultEncoding()));

            handleDeploymentSessionConfig(deploymentInfo, servletContext);

            // deployment.setSessionManager(deploymentInfo.getSessionManagerFactory().createSessionManager(deployment));
            deployment.getSessionManager().setDefaultSessionTimeout(deploymentInfo.getDefaultSessionTimeout());

            for (SessionListener listener : deploymentInfo.getSessionListeners()) {
                deployment.getSessionManager().registerSessionListener(listener);
            }

            final List<ThreadSetupAction> setup = new ArrayList<>();
            setup.add(new ContextClassLoaderSetupAction(deploymentInfo.getClassLoader()));
            setup.addAll(deploymentInfo.getThreadSetupActions());
            final CompositeThreadSetupAction threadSetupAction = new CompositeThreadSetupAction(setup);
            deployment.setThreadSetupAction(threadSetupAction);

            ThreadSetupAction.Handle handle = threadSetupAction.setup(null);
            try {

                final ApplicationListeners listeners = createListeners();
                listeners.start();

                deployment.setApplicationListeners(listeners);

                // now create the servlets and filters that we know about. We can still get more later
                createServletsAndFilters(deployment, deploymentInfo);

                // first run the SCI's
                for (final ServletContainerInitializerInfo sci : deploymentInfo.getServletContainerInitializers()) {
                    final InstanceHandle<? extends ServletContainerInitializer> instance = sci.getInstanceFactory()
                            .createInstance();
                    try {
                        instance.getInstance().onStartup(sci.getHandlesTypes(), servletContext);
                    } finally {
                        instance.release();
                    }
                }

                deployment.getSessionManager().registerSessionListener(
                        new SessionListenerBridge(threadSetupAction, listeners, servletContext));

                initializeErrorPages(deployment, deploymentInfo);
                initializeMimeMappings(deployment, deploymentInfo);
                initializeTempDir(servletContext, deploymentInfo);
                listeners.contextInitialized();
                // run

                HttpHandler wrappedHandlers = ServletDispatchingHandler.INSTANCE;
                wrappedHandlers = wrapHandlers(wrappedHandlers, deploymentInfo.getInnerHandlerChainWrappers());
                HttpHandler securityHandler = setupSecurityHandlers(wrappedHandlers);
                wrappedHandlers = new PredicateHandler(DispatcherTypePredicate.REQUEST, securityHandler,
                        wrappedHandlers);

                HttpHandler outerHandlers = wrapHandlers(wrappedHandlers, deploymentInfo.getOuterHandlerChainWrappers());
                wrappedHandlers = new PredicateHandler(DispatcherTypePredicate.REQUEST, outerHandlers, wrappedHandlers);
                wrappedHandlers = handleDevelopmentModePersistentSessions(wrappedHandlers, deploymentInfo,
                        deployment.getSessionManager(), servletContext);

                MetricsCollector metrics = deploymentInfo.getMetricsCollector();
                if (metrics != null) {
                    wrappedHandlers = new MetricsChainHandler(wrappedHandlers, metrics, deployment);
                }

                final ServletInitialHandler servletInitialHandler = SecurityActions.createServletInitialHandler(
                        deployment.getServletPaths(), wrappedHandlers, deployment.getThreadSetupAction(),
                        servletContext);

                HttpHandler initialHandler = wrapHandlers(servletInitialHandler, deployment.getDeploymentInfo()
                        .getInitialHandlerChainWrappers());
                initialHandler = new HttpContinueReadHandler(initialHandler);
                if (deploymentInfo.getUrlEncoding() != null) {
                    initialHandler = Handlers.urlDecodingHandler(deploymentInfo.getUrlEncoding(), initialHandler);
                }
                deployment.setInitialHandler(initialHandler);
                deployment.setServletHandler(servletInitialHandler);
                deployment.getServletPaths().invalidate(); // make sure we have a fresh set of servlet paths
                servletContext.initDone();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                handle.tearDown();
            }
            state = State.DEPLOYED;

        } else {

            super.deploy();
        }
    }

    public State getState() {
        return state;
    }

}
