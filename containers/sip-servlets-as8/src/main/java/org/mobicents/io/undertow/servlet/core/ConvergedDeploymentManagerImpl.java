/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.io.undertow.servlet.core;

import static javax.servlet.http.HttpServletRequest.BASIC_AUTH;
import static javax.servlet.http.HttpServletRequest.CLIENT_CERT_AUTH;
import static javax.servlet.http.HttpServletRequest.DIGEST_AUTH;
import static javax.servlet.http.HttpServletRequest.FORM_AUTH;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;

import org.mobicents.io.undertow.servlet.handlers.ConvergedSessionRestoringHandler;
import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

import io.undertow.Handlers;
import io.undertow.predicate.Predicates;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanismFactory;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.api.NotificationReceiver;
import io.undertow.security.api.SecurityContextFactory;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.NotificationReceiverHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.security.impl.CachedAuthenticatedSessionMechanism;
import io.undertow.security.impl.ClientCertAuthenticationMechanism;
import io.undertow.security.impl.DigestAuthenticationMechanism;
import io.undertow.security.impl.ExternalAuthenticationMechanism;
import io.undertow.security.impl.SecurityContextFactoryImpl;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.HttpContinueReadHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.form.FormEncodedDataDefinition;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.UndertowServletLogger;
import io.undertow.servlet.UndertowServletMessages;
import io.undertow.servlet.api.AuthMethodConfig;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ErrorPage;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.HttpMethodSecurityInfo;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.MetricsCollector;
import io.undertow.servlet.api.MimeMapping;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletSecurityInfo;
import io.undertow.servlet.api.ServletStackTraces;
import io.undertow.servlet.api.SessionPersistenceManager;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.api.WebResourceCollection;
import io.undertow.servlet.api.SecurityInfo.EmptyRoleSemantic;
import io.undertow.servlet.core.ApplicationListeners;
import io.undertow.servlet.core.CompositeThreadSetupAction;
import io.undertow.servlet.core.ContextClassLoaderSetupAction;
import io.undertow.servlet.core.DeploymentImpl;
import io.undertow.servlet.core.DeploymentManagerImpl;
import io.undertow.servlet.core.ErrorPages;
import io.undertow.servlet.core.ManagedListener;
import io.undertow.servlet.handlers.ServletDispatchingHandler;
import io.undertow.servlet.handlers.ServletInitialHandler;
import io.undertow.servlet.handlers.SessionRestoringHandler;
import io.undertow.servlet.handlers.security.CachedAuthenticatedSessionHandler;
import io.undertow.servlet.handlers.security.SSLInformationAssociationHandler;
import io.undertow.servlet.handlers.security.SecurityPathMatches;
import io.undertow.servlet.handlers.security.ServletAuthenticationCallHandler;
import io.undertow.servlet.handlers.security.ServletAuthenticationConstraintHandler;
import io.undertow.servlet.handlers.security.ServletConfidentialityConstraintHandler;
import io.undertow.servlet.handlers.security.ServletFormAuthenticationMechanism;
import io.undertow.servlet.handlers.security.ServletSecurityConstraintHandler;
import io.undertow.servlet.predicate.DispatcherTypePredicate;
import io.undertow.servlet.spec.ServletContextImpl;
import io.undertow.util.MimeMappings;
/**
 * This class extends io.undertow.servlet.core.DeploymentManagerImpl in order to create org.mobicents.servlet.sip.startup.ConvergedServletContextImpl instead of
 * io.undertow.servlet.spec.ServletContextImpl during deploy. With this modification it is possible to create ConvergedHttpSessions instead of pain HttpSessions during ServletContext.getSession() calls.
 *
 * @author kakonyi.istvan@alerant.hu
 */
public class ConvergedDeploymentManagerImpl extends DeploymentManagerImpl{

    private final DeploymentInfo originalDeployment;

    private final ServletContainer servletContainer;

    public ConvergedDeploymentManagerImpl(DeploymentInfo deployment, ServletContainer servletContainer) {
        super(deployment, servletContainer);
        this.originalDeployment = deployment;
        this.servletContainer = servletContainer;
    }

    private void invokeDeploymentMethod(DeploymentImpl source, String methodName, Class<?>[] parameterTypes, Object... args){
        try{
            //FIXME kakonyii: what if the declared method throws an exception?
            Method method = DeploymentImpl.class.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            method.invoke(source, args);
            method.setAccessible(false);
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deploy() {
        DeploymentInfo deploymentInfo = originalDeployment.clone();

        if (deploymentInfo.getServletStackTraces() == ServletStackTraces.ALL) {
            UndertowServletLogger.REQUEST_LOGGER.servletStackTracesAll(deploymentInfo.getDeploymentName());
        }

        deploymentInfo.validate();
        final DeploymentImpl deployment = new DeploymentImpl(this, deploymentInfo, servletContainer);

        //set deployment to parent using reflection
        try{
            Field deploymentField = DeploymentManagerImpl.class.getDeclaredField("deployment");
            deploymentField.setAccessible(true);
            deploymentField.set(this, deployment);
            deploymentField.setAccessible(false);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }

        final ServletContextImpl servletContexImpl = new ServletContextImpl(servletContainer, deployment);
        final ServletContext servletContext = new ConvergedServletContextImpl(servletContexImpl);

        //deployment.setServletContext(((ConvergedServletContextImpl)servletContext).getDelegatedContext());
        this.invokeDeploymentMethod(deployment, "setServletContext", new Class[] {ServletContextImpl.class}, new Object[] {((ConvergedServletContextImpl)servletContext).getDelegatedContext()});

        handleExtensions(deploymentInfo, servletContext);

        deployment.setDefaultCharset(Charset.forName(deploymentInfo.getDefaultEncoding()));

        handleDeploymentSessionConfig(deploymentInfo, ((ConvergedServletContextImpl)servletContext).getDelegatedContext());

        //deployment.setSessionManager(deploymentInfo.getSessionManagerFactory().createSessionManager(deployment));
        this.invokeDeploymentMethod(deployment, "setSessionManager", new Class[] {SessionManager.class}, new Object[] {deploymentInfo.getSessionManagerFactory().createSessionManager(deployment)});

        deployment.getSessionManager().setDefaultSessionTimeout(deploymentInfo.getDefaultSessionTimeout());
        for(SessionListener listener : deploymentInfo.getSessionListeners()) {
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

            //deployment.setApplicationListeners(listeners);
            this.invokeDeploymentMethod(deployment, "setApplicationListeners", new Class[] {ApplicationListeners.class}, new Object[] {listeners});

            //now create the servlets and filters that we know about. We can still get more later
            createServletsAndFilters(deployment, deploymentInfo);

            //first run the SCI's
            for (final ServletContainerInitializerInfo sci : deploymentInfo.getServletContainerInitializers()) {
                final InstanceHandle<? extends ServletContainerInitializer> instance = sci.getInstanceFactory().createInstance();
                try {
                    instance.getInstance().onStartup(sci.getHandlesTypes(), servletContext);
                } finally {
                    instance.release();
                }
            }

            deployment.getSessionManager().registerSessionListener(new ConvergedSessionListenerBridge(threadSetupAction, listeners, servletContext, deployment.getSessionManager()));

            initializeErrorPages(deployment, deploymentInfo);
            initializeMimeMappings(deployment, deploymentInfo);
            initializeTempDir(servletContext, deploymentInfo);
            listeners.contextInitialized();
            //run

            HttpHandler wrappedHandlers = ServletDispatchingHandler.INSTANCE;
            wrappedHandlers = wrapHandlers(wrappedHandlers, deploymentInfo.getInnerHandlerChainWrappers());
            HttpHandler securityHandler = setupSecurityHandlers(wrappedHandlers);
            wrappedHandlers = new PredicateHandler(DispatcherTypePredicate.REQUEST, securityHandler, wrappedHandlers);

            HttpHandler outerHandlers = wrapHandlers(wrappedHandlers, deploymentInfo.getOuterHandlerChainWrappers());
            wrappedHandlers = new PredicateHandler(DispatcherTypePredicate.REQUEST, outerHandlers, wrappedHandlers);
            wrappedHandlers = handleDevelopmentModePersistentSessions(wrappedHandlers, deploymentInfo, deployment.getSessionManager(), servletContext);

            MetricsCollector metrics = deploymentInfo.getMetricsCollector();
            if(metrics != null) {
                wrappedHandlers = new MetricsChainHandler(wrappedHandlers, metrics, deployment);
            }

            final ServletInitialHandler servletInitialHandler = SecurityActions.createServletInitialHandler(deployment.getServletPaths(), wrappedHandlers, deployment.getThreadSetupAction(), servletContext);

            HttpHandler initialHandler = wrapHandlers(servletInitialHandler, deployment.getDeploymentInfo().getInitialHandlerChainWrappers());
            initialHandler = new HttpContinueReadHandler(initialHandler);
            if(deploymentInfo.getUrlEncoding() != null) {
                initialHandler = Handlers.urlDecodingHandler(deploymentInfo.getUrlEncoding(), initialHandler);
            }
            deployment.setInitialHandler(initialHandler);

            //deployment.setServletHandler(servletInitialHandler);
            this.invokeDeploymentMethod(deployment, "setServletHandler", new Class[] {ServletInitialHandler.class}, new Object[]{servletInitialHandler});

            deployment.getServletPaths().invalidate(); //make sure we have a fresh set of servlet paths
            ((ConvergedServletContextImpl) servletContext).initDone();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            handle.tearDown();
        }

        //set state using reflection:
        try{
            Field stateField = DeploymentManagerImpl.class.getDeclaredField("state");
            stateField.setAccessible(true);
            stateField.set(this, State.DEPLOYED);
            stateField.setAccessible(false);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    private void createServletsAndFilters(final DeploymentImpl deployment, final DeploymentInfo deploymentInfo) {
        for (Map.Entry<String, ServletInfo> servlet : deploymentInfo.getServlets().entrySet()) {
            deployment.getServlets().addServlet(servlet.getValue());
        }
        for (Map.Entry<String, FilterInfo> filter : deploymentInfo.getFilters().entrySet()) {
            deployment.getFilters().addFilter(filter.getValue());
        }
    }

    private void handleExtensions(final DeploymentInfo deploymentInfo, final ServletContext servletContext) {
        Set<Class<?>> loadedExtensions = new HashSet<>();

        for (ServletExtension extension : ServiceLoader.load(ServletExtension.class, deploymentInfo.getClassLoader())) {
            loadedExtensions.add(extension.getClass());
            extension.handleDeployment(deploymentInfo, servletContext);
        }

        if (!ServletExtension.class.getClassLoader().equals(deploymentInfo.getClassLoader())) {
            for (ServletExtension extension : ServiceLoader.load(ServletExtension.class)) {

                // Note: If the CLs are different, but can the see the same extensions and extension might get loaded
                // and thus instantiated twice, but the handleDeployment() is executed only once.

                if (!loadedExtensions.contains(extension.getClass())) {
                    extension.handleDeployment(deploymentInfo, servletContext);
                }
            }
        }
        for(ServletExtension extension : deploymentInfo.getServletExtensions()) {
            extension.handleDeployment(deploymentInfo, servletContext);
        }
    }

    private HttpHandler setupSecurityHandlers(HttpHandler initialHandler) {
        final DeploymentInfo deploymentInfo = super.getDeployment().getDeploymentInfo();
        final LoginConfig loginConfig = deploymentInfo.getLoginConfig();

        final Map<String, AuthenticationMechanismFactory> factoryMap = new HashMap<>(deploymentInfo.getAuthenticationMechanisms());
        if(!factoryMap.containsKey(BASIC_AUTH)) {
            factoryMap.put(BASIC_AUTH, BasicAuthenticationMechanism.FACTORY);
        }
        if(!factoryMap.containsKey(FORM_AUTH)) {
            factoryMap.put(FORM_AUTH, ServletFormAuthenticationMechanism.FACTORY);
        }
        if(!factoryMap.containsKey(DIGEST_AUTH)) {
            factoryMap.put(DIGEST_AUTH, DigestAuthenticationMechanism.FACTORY);
        }
        if(!factoryMap.containsKey(CLIENT_CERT_AUTH)) {
            factoryMap.put(CLIENT_CERT_AUTH, ClientCertAuthenticationMechanism.FACTORY);
        }
        if(!factoryMap.containsKey(ExternalAuthenticationMechanism.NAME)) {
            factoryMap.put(ExternalAuthenticationMechanism.NAME, ExternalAuthenticationMechanism.FACTORY);
        }
        HttpHandler current = initialHandler;
        current = new SSLInformationAssociationHandler(current);

        final SecurityPathMatches securityPathMatches = buildSecurityConstraints();
        current = new ServletAuthenticationCallHandler(current);
        if(deploymentInfo.isDisableCachingForSecuredPages()) {
            current = Handlers.predicate(Predicates.authRequired(), Handlers.disableCache(current), current);
        }
        if (!securityPathMatches.isEmpty()) {
            current = new ServletAuthenticationConstraintHandler(current);
        }
        current = new ServletConfidentialityConstraintHandler(deploymentInfo.getConfidentialPortManager(), current);
        if (!securityPathMatches.isEmpty()) {
            current = new ServletSecurityConstraintHandler(securityPathMatches, current);
        }
        List<AuthenticationMechanism> authenticationMechanisms = new LinkedList<>();
        authenticationMechanisms.add(new CachedAuthenticatedSessionMechanism()); //TODO: does this really need to be hard coded?

        String mechName = null;
        if (loginConfig != null || deploymentInfo.getJaspiAuthenticationMechanism() != null) {

            //we don't allow multipart requests, and always use the default encoding
            FormParserFactory parser = FormParserFactory.builder(false)
                    .addParser(new FormEncodedDataDefinition().setDefaultEncoding(deploymentInfo.getDefaultEncoding()))
                    .build();

            List<AuthMethodConfig> authMethods = Collections.<AuthMethodConfig>emptyList();
            if(loginConfig != null) {
                authMethods = loginConfig.getAuthMethods();
            }

            for(AuthMethodConfig method : authMethods) {
                AuthenticationMechanismFactory factory = factoryMap.get(method.getName());
                if(factory == null) {
                    throw UndertowServletMessages.MESSAGES.unknownAuthenticationMechanism(method.getName());
                }
                if(mechName == null) {
                    mechName = method.getName();
                }

                final Map<String, String> properties = new HashMap<>();
                properties.put(AuthenticationMechanismFactory.CONTEXT_PATH, deploymentInfo.getContextPath());
                properties.put(AuthenticationMechanismFactory.REALM, loginConfig.getRealmName());
                properties.put(AuthenticationMechanismFactory.ERROR_PAGE, loginConfig.getErrorPage());
                properties.put(AuthenticationMechanismFactory.LOGIN_PAGE, loginConfig.getLoginPage());
                properties.putAll(method.getProperties());

                String name = method.getName().toUpperCase(Locale.US);
                // The mechanism name is passed in from the HttpServletRequest interface as the name reported needs to be
                // comparable using '=='
                name = name.equals(FORM_AUTH) ? FORM_AUTH : name;
                name = name.equals(BASIC_AUTH) ? BASIC_AUTH : name;
                name = name.equals(DIGEST_AUTH) ? DIGEST_AUTH : name;
                name = name.equals(CLIENT_CERT_AUTH) ? CLIENT_CERT_AUTH : name;

                authenticationMechanisms.add(factory.create(name, parser, properties));
            }
        }

        ((DeploymentImpl)super.getDeployment()).setAuthenticationMechanisms(authenticationMechanisms);
        //if the JASPI auth mechanism is set then it takes over
        if(deploymentInfo.getJaspiAuthenticationMechanism() == null) {
            current = new AuthenticationMechanismsHandler(current, authenticationMechanisms);
        } else {
            current = new AuthenticationMechanismsHandler(current, Collections.<AuthenticationMechanism>singletonList(deploymentInfo.getJaspiAuthenticationMechanism()));
        }

        current = new CachedAuthenticatedSessionHandler(current, super.getDeployment().getServletContext());
        List<NotificationReceiver> notificationReceivers = deploymentInfo.getNotificationReceivers();
        if (!notificationReceivers.isEmpty()) {
            current = new NotificationReceiverHandler(current, notificationReceivers);
        }

        // TODO - A switch to constraint driven could be configurable, however before we can support that with servlets we would
        // need additional tracking within sessions if a servlet has specifically requested that authentication occurs.
        SecurityContextFactory contextFactory = deploymentInfo.getSecurityContextFactory();
        if (contextFactory == null) {
            contextFactory = SecurityContextFactoryImpl.INSTANCE;
        }
        current = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, deploymentInfo.getIdentityManager(), mechName,
                contextFactory, current);
        return current;
    }

    private SecurityPathMatches buildSecurityConstraints() {
        SecurityPathMatches.Builder builder = SecurityPathMatches.builder(super.getDeployment().getDeploymentInfo());
        final Set<String> urlPatterns = new HashSet<>();
        for (SecurityConstraint constraint : super.getDeployment().getDeploymentInfo().getSecurityConstraints()) {
            builder.addSecurityConstraint(constraint);
            for (WebResourceCollection webResources : constraint.getWebResourceCollections()) {
                urlPatterns.addAll(webResources.getUrlPatterns());
            }
        }

        for (final ServletInfo servlet : super.getDeployment().getDeploymentInfo().getServlets().values()) {
            final ServletSecurityInfo securityInfo = servlet.getServletSecurityInfo();
            if (securityInfo != null) {
                final Set<String> mappings = new HashSet<>(servlet.getMappings());
                mappings.removeAll(urlPatterns);
                if (!mappings.isEmpty()) {
                    final Set<String> methods = new HashSet<>();

                    for (HttpMethodSecurityInfo method : securityInfo.getHttpMethodSecurityInfo()) {
                        methods.add(method.getMethod());
                        if (method.getRolesAllowed().isEmpty() && method.getEmptyRoleSemantic() == EmptyRoleSemantic.PERMIT) {
                            //this is an implict allow
                            continue;
                        }
                        SecurityConstraint newConstraint = new SecurityConstraint()
                                .addRolesAllowed(method.getRolesAllowed())
                                .setTransportGuaranteeType(method.getTransportGuaranteeType())
                                .addWebResourceCollection(new WebResourceCollection().addUrlPatterns(mappings)
                                        .addHttpMethod(method.getMethod()));
                        builder.addSecurityConstraint(newConstraint);
                    }
                    //now add the constraint, unless it has all default values and method constrains where specified
                    if (!securityInfo.getRolesAllowed().isEmpty()
                            || securityInfo.getEmptyRoleSemantic() != EmptyRoleSemantic.PERMIT
                            || methods.isEmpty()) {
                        SecurityConstraint newConstraint = new SecurityConstraint()
                                .setEmptyRoleSemantic(securityInfo.getEmptyRoleSemantic())
                                .addRolesAllowed(securityInfo.getRolesAllowed())
                                .setTransportGuaranteeType(securityInfo.getTransportGuaranteeType())
                                .addWebResourceCollection(new WebResourceCollection().addUrlPatterns(mappings)
                                        .addHttpMethodOmissions(methods));
                        builder.addSecurityConstraint(newConstraint);
                    }
                }

            }
        }

        return builder.build();
    }

    private void initializeTempDir(final ServletContext servletContext, final DeploymentInfo deploymentInfo) {
        if (deploymentInfo.getTempDir() != null) {
            servletContext.setAttribute(ServletContext.TEMPDIR, deploymentInfo.getTempDir());
        } else {
            servletContext.setAttribute(ServletContext.TEMPDIR, new File(SecurityActions.getSystemProperty("java.io.tmpdir")));
        }
    }

    private void initializeMimeMappings(final DeploymentImpl deployment, final DeploymentInfo deploymentInfo) {
        final Map<String, String> mappings = new HashMap<>(MimeMappings.DEFAULT_MIME_MAPPINGS);
        for (MimeMapping mapping : deploymentInfo.getMimeMappings()) {
            mappings.put(mapping.getExtension(), mapping.getMimeType());
        }
        deployment.setMimeExtensionMappings(mappings);
    }

    private void initializeErrorPages(final DeploymentImpl deployment, final DeploymentInfo deploymentInfo) {
        final Map<Integer, String> codes = new HashMap<>();
        final Map<Class<? extends Throwable>, String> exceptions = new HashMap<>();
        String defaultErrorPage = null;
        for (final ErrorPage page : deploymentInfo.getErrorPages()) {
            if (page.getExceptionType() != null) {
                exceptions.put(page.getExceptionType(), page.getLocation());
            } else if (page.getErrorCode() != null) {
                codes.put(page.getErrorCode(), page.getLocation());
            } else {
                if (defaultErrorPage != null) {
                    throw UndertowServletMessages.MESSAGES.moreThanOneDefaultErrorPage(defaultErrorPage, page.getLocation());
                } else {
                    defaultErrorPage = page.getLocation();
                }
            }
        }
        deployment.setErrorPages(new ErrorPages(codes, exceptions, defaultErrorPage));
    }

    private ApplicationListeners createListeners() {
        final List<ManagedListener> managedListeners = new ArrayList<>();
        for (final ListenerInfo listener : super.getDeployment().getDeploymentInfo().getListeners()) {
            managedListeners.add(new ManagedListener(listener, false));
        }
        return new ApplicationListeners(managedListeners, super.getDeployment().getServletContext());
    }


    private static HttpHandler wrapHandlers(final HttpHandler wrapee, final List<HandlerWrapper> wrappers) {
        HttpHandler current = wrapee;
        for (HandlerWrapper wrapper : wrappers) {
            current = wrapper.wrap(current);
        }
        return current;
    }

    private HttpHandler handleDevelopmentModePersistentSessions(HttpHandler next, final DeploymentInfo deploymentInfo, final SessionManager sessionManager, final ServletContext servletContext) {
        final SessionPersistenceManager sessionPersistenceManager = deploymentInfo.getSessionPersistenceManager();
        if (sessionPersistenceManager != null) {
            SessionRestoringHandler handler = new ConvergedSessionRestoringHandler(super.getDeployment().getDeploymentInfo().getDeploymentName(), sessionManager, servletContext, next, sessionPersistenceManager);

            //((DeploymentImpl)super.getDeployment()).addLifecycleObjects(handler);
            this.invokeDeploymentMethod(((DeploymentImpl)super.getDeployment()), "addLifecycleObjects", new Class[]{SessionRestoringHandler.class}, new Object[]{handler});

            return handler;
        }
        return next;
    }
}
