package io.undertow.servlet.core;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.MetricsCollector;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.core.ApplicationListeners;
import io.undertow.servlet.core.CompositeThreadSetupAction;
import io.undertow.servlet.core.ContextClassLoaderSetupAction;
import io.undertow.servlet.core.DeploymentManagerImpl;
import io.undertow.servlet.core.SessionListenerBridge;
import io.undertow.servlet.handlers.ServletDispatchingHandler;
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

    protected Deployment createDeployment(DeploymentManager manager, DeploymentInfo deploymentInfo,
            ServletContainer servletContainer) {
        return new UndertowSipContextDeployment(manager, deploymentInfo, servletContainer);
    }

    @Override 
    public HttpHandler start() throws ServletException{
        HttpHandler handler = super.start();
        this.state = super.getState();
        return handler;
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
    public void deploy() {
        if (originalDeployment instanceof ConvergedDeploymentInfo) {
            ConvergedDeploymentInfo deploymentInfo = (ConvergedDeploymentInfo) originalDeployment;

            if (deploymentInfo.getServletStackTraces() == ServletStackTraces.ALL) {
                UndertowServletLogger.REQUEST_LOGGER.servletStackTracesAll(deploymentInfo.getDeploymentName());
            }

            deploymentInfo.validate();
            final UndertowSipContextDeployment deployment = (UndertowSipContextDeployment) this
                    .createDeployment(this, deploymentInfo, servletContainer);
            this.deployment = deployment;

            final ServletContextImpl servletContext = deployment.getServletContext();// new
                                                                                     // ServletContextImpl(servletContainer,
                                                                                     // deployment);
            createSipServlets(deployment, deploymentInfo, servletContext);

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
                    //FIXME public MetricsChainHandler
                    wrappedHandlers = new MetricsChainHandler(wrappedHandlers, metrics, deployment);
                }
                //FIXME public SecurityActions and createServletInitialHandler method
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
            state = super.getState();
        }
    }

    protected void createSipServlets(final UndertowSipContextDeployment deployment,
            final ConvergedDeploymentInfo deploymentInfo, ServletContextImpl servletContext) {
        for (Map.Entry<String, ServletInfo> servlet : deploymentInfo.getSipServlets().entrySet()) {
            deployment.getSipServlets().addServlet(servlet.getValue());

            SipServletImpl sipServlet = new SipServletImpl(servlet.getValue(), servletContext);
            sipServlet.setDescription(servlet.getValue().getName());
            sipServlet.setDisplayName(servlet.getValue().getName());
            sipServlet.setServletName(servlet.getValue().getName());
            sipServlet.setupMultipart(servletContext);

            deployment.addChild(sipServlet);
        }

    }

    @Override
    public State getState() {
        return state;
    }
}
