package org.mobicents.servlet.sip.undertow;

import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.core.DeploymentImpl;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.core.ManagedServlets;
import io.undertow.servlet.handlers.ServletPathMatches;
import io.undertow.servlet.spec.ServletContextImpl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.MobicentsSipServlet;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipContextEvent;
import org.mobicents.servlet.sip.core.SipContextEventType;
import org.mobicents.servlet.sip.core.SipListeners;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.descriptor.MobicentsSipServletMapping;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.core.security.SipDigestAuthenticator;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionsUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionCreationThreadLocal;
import org.mobicents.servlet.sip.core.session.SipSessionsUtilImpl;
import org.mobicents.servlet.sip.core.timers.ProxyTimerService;
import org.mobicents.servlet.sip.core.timers.ProxyTimerServiceImpl;
import org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerService;
import org.mobicents.servlet.sip.core.timers.SipServletTimerService;
import org.mobicents.servlet.sip.core.timers.StandardSipApplicationSessionTimerService;
import org.mobicents.servlet.sip.core.timers.TimerServiceImpl;
import org.mobicents.servlet.sip.dns.MobicentsDNSResolver;
import org.mobicents.servlet.sip.listener.SipConnectorListener;
import org.mobicents.servlet.sip.message.SipFactoryFacade;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.ruby.SipRubyController;
import org.mobicents.servlet.sip.startup.ConvergedApplicationContext;

public class UndertowSipContextDeployment extends DeploymentImpl implements SipContext, Deployment {

    private static final Logger logger = Logger.getLogger(UndertowSipContextDeployment.class);

    // as mentionned per JSR 289 Section 6.1.2.1 default lifetime for an
    // application session is 3 minutes
    private static int DEFAULT_LIFETIME = 3;

    private final ConvergedDeploymentInfo deploymentInfo;
    private final UndertowSipManager sessionManager;

    private final ManagedServlets sipServlets;

    // TODO setter
    protected transient SipApplicationDispatcher sipApplicationDispatcher = null;
    // TODO setter
    protected boolean hasDistributableManager;
    // timer service used to schedule sip application session expiration timer
    protected transient SipApplicationSessionTimerService sasTimerService = null;
    // timer service used to schedule sip servlet originated timer tasks
    protected transient SipServletTimerService timerService = null;
    // timer service used to schedule proxy timer tasks
    protected transient ProxyTimerService proxyTimerService = null;
    protected transient SipListeners sipListeners;
    protected transient SipFactoryFacade sipFactoryFacade;
    protected transient SipSessionsUtilImpl sipSessionsUtil;
    // TODO:protected transient SipSecurityUtils sipSecurityUtils;
    // TODO:protected transient SipDigestAuthenticator sipDigestAuthenticator;
    protected transient String securityDomain;
    
    protected String displayName;

    private transient ThreadLocal<SipApplicationSessionCreationThreadLocal> sipApplicationSessionsAccessedThreadLocal = new ThreadLocal<SipApplicationSessionCreationThreadLocal>();
    // http://code.google.com/p/mobicents/issues/detail?id=2534 &&
    // http://code.google.com/p/mobicents/issues/detail?id=2526
    private transient ThreadLocal<Boolean> isManagedThread = new ThreadLocal<Boolean>();

    ConvergedApplicationContext context;

    public UndertowSipContextDeployment(DeploymentManager deploymentManager, DeploymentInfo deploymentInfo,
            ServletContainer servletContainer) {
        super(deploymentManager, deploymentInfo, servletContainer);
        this.deploymentInfo = (ConvergedDeploymentInfo) deploymentInfo;

        // TODO:
        this.sessionManager = (UndertowSipManager) this.deploymentInfo.getSessionManagerFactory().createSessionManager(
                this);

        this.sipServlets = new ManagedServlets(this, new ServletPathMatches(this));

        this.setSipApplicationSessionTimeout(DEFAULT_LIFETIME);
        // pipeline.setBasic(new SipStandardContextValve());
        sipListeners = new UndertowSipListenersHolder(this);
        this.deploymentInfo.setChildrenMap(new HashMap<String, MobicentsSipServlet>());
        this.deploymentInfo.setChildrenMapByClassName(new HashMap<String, MobicentsSipServlet>());
        int idleTime = this.getSipApplicationSessionTimeout();
        if (idleTime <= 0) {
            idleTime = 1;
        }
        this.deploymentInfo.setMainServlet(false);

    }

    public void init() throws ServletException {
        if(logger.isDebugEnabled()) {
            logger.debug("Initializing the sip context");
        }
//      if (this.getParent() != null) {
//          // Add the main configuration listener for sip applications
//          LifecycleListener sipConfigurationListener = new SipContextConfig();
//          this.addLifecycleListener(sipConfigurationListener);            
//          setDelegate(true);
//      }               
        // call the super method to correctly initialize the context and fire
        // up the
        // init event on the new registered SipContextConfig, so that the
        // standardcontextconfig
        // is correctly initialized too
        
        prepareServletContext();
        
        if(logger.isDebugEnabled()) {
            logger.debug("sip context Initialized");
        }   
    }
    
    
    public synchronized void start() throws ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting the sip context " + this.getApplicationName());
        }

        prepareServletContext();

        // TODO:
        hasDistributableManager = false;
        sessionManager.setMobicentsSipFactory(sipApplicationDispatcher.getSipFactory());
        sessionManager.setContainer(this);

        // JSR 289 16.2 Servlet Selection
        // When using this mechanism (the main-servlet) for servlet selection,
        // if there is only one servlet in the application then this
        // declaration is optional and the lone servlet becomes the main servlet
        String mainServlet = this.getMainServlet();
        Map<String, MobicentsSipServlet> childrenMap = this.getChildrenMap();
        if ((mainServlet == null || mainServlet.length() < 1) && childrenMap.size() == 1) {
            setMainServlet(childrenMap.keySet().iterator().next());
        }
        // TODO:sipSecurityUtils = new SipSecurityUtils(this);

        // TODO:sipDigestAuthenticator = new
        // DigestAuthenticator(sipApplicationDispatcher.getSipFactory().getHeaderFactory());
        // JSR 289 Section 2.1.1 Step 3.Invoke SipApplicationRouter.applicationDeployed() for this application.
        // called implicitly within sipApplicationDispatcher.addSipApplication
        sipApplicationDispatcher.addSipApplication(this.getApplicationName(), this);

        if (logger.isDebugEnabled()) {
            logger.debug("sip application session timeout for this context is "
                    + this.getSipApplicationSessionTimeout() + " minutes");
        }

        if (logger.isDebugEnabled()) {
            // TODO:
            logger.debug("http session timeout for this context is " + this.deploymentInfo.getDefaultSessionTimeout()
                    + " minutes");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("sip context started " + this.getApplicationName());
        }
    }

    public synchronized void stop() throws ServletException {
        String name = this.getApplicationName();
        if(logger.isDebugEnabled()) {
            logger.debug("Stopping the sip context " + name);
        }

        this.sessionManager.dumpSipSessions();
        this.sessionManager.dumpSipApplicationSessions();
        logger.warn("number of active sip sessions : " + this.sessionManager.getActiveSipSessions()); 
        logger.warn("number of active sip application sessions : " + this.sessionManager.getActiveSipApplicationSessions());

        // this should happen after so that applications can still do some processing
        // in destroy methods to notify that context is getting destroyed and app removed
        sipListeners.deallocateServletsActingAsListeners();
        this.deploymentInfo.getSipApplicationListeners().clear();
        this.deploymentInfo.getSipServletMappings().clear();
        
        this.getChildrenMap().clear();
        this.deploymentInfo.getChildrenMapByClassName().clear();
        if(sipApplicationDispatcher != null) {
            if(this.getApplicationName() != null) {
                sipApplicationDispatcher.removeSipApplication(this.getApplicationName());
            } else {
                logger.error("the application name is null for the following context : " + name);
            }
        }   
        if(sasTimerService != null && sasTimerService.isStarted()) {
            sasTimerService.stop();         
        }
        // Issue 1478 : nullify the ref to avoid reusing it
        sasTimerService = null;
        // Issue 1791 : don't check is the service is started it makes the stop
        // of tomcat hang
        if(timerService != null) {
            timerService.stop();
        }   
        if(proxyTimerService != null) {
            proxyTimerService.stop();
        }
        // Issue 1478 : nullify the ref to avoid reusing it
        timerService = null;
        getServletContext().setAttribute(javax.servlet.sip.SipServlet.TIMER_SERVICE, null);
        if(logger.isDebugEnabled()) {
            logger.debug("sip context stopped " + name);
        }
    }

    public boolean listenerStop() {
        boolean ok = true;
        if (logger.isDebugEnabled())
            logger.debug("Sending application stop events");
        
        List<ServletContextListener> servletContextListeners = sipListeners.getServletContextListeners();
        if (servletContextListeners != null) {
            ServletContextEvent event =
                new ServletContextEvent(getServletContext());
            for (ServletContextListener servletContextListener : servletContextListeners) {                     
                if (servletContextListener == null)
                    continue;
                
                try {
                    //TODO:fireContainerEvent("beforeContextDestroyed", servletContextListener);
                    servletContextListener.contextDestroyed(event);
                  //TODO:fireContainerEvent("afterContextDestroyed", servletContextListener);
                } catch (Throwable t) {
                  //TODO:fireContainerEvent("afterContextDestroyed", servletContextListener);
//                    getLogger().error
//                        (sm.getString("standardContext.listenerStop",
//                              servletContextListener.getClass().getName()), t);
                    //getLogger().error
                    //    (MESSAGES.errorSendingContextDestroyedEvent(servletContextListener.getClass().getName()), t);                    
                    //TODO:
                    t.printStackTrace();
                    ok = false;
                }
                
                // TODO Annotation processing                 
            }
        }

        // TODO Annotation processing check super class on tomcat 6
        
        sipListeners.clean();

        return ok;
    }

    
    
    protected void prepareServletContext() throws ServletException {
        /*
         * if(sipApplicationDispatcher == null) { setApplicationDispatcher(); }
         */
        if (sipFactoryFacade == null) {
            sipFactoryFacade = new SipFactoryFacade((SipFactoryImpl) sipApplicationDispatcher.getSipFactory(), this);
        }
        if (sipSessionsUtil == null) {
            sipSessionsUtil = new SipSessionsUtilImpl(this);
        }
        // needed when restarting applications through the tomcat manager
        this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SIP_FACTORY, sipFactoryFacade);
        if (timerService == null) {
            // FIXME: distributable not supported
            // if(getDistributable() && hasDistributableManager) {
            // if(logger.isInfoEnabled()) {
            // logger.info("Using the Fault Tolerant Timer Service to schedule fault tolerant timers in a distributed environment");
            // }
            // timerService = new FaultTolerantTimerServiceImpl((DistributableSipManager)getSipManager());
            // } else {
            // timerService = new TimerServiceImpl();
            // }
            timerService = new TimerServiceImpl(sipApplicationDispatcher.getSipService());
        }
        if (proxyTimerService == null) {
            proxyTimerService = new ProxyTimerServiceImpl();
        }
        if (sasTimerService == null || !sasTimerService.isStarted()) {
            // FIXME: distributable not supported
            // distributable if(getDistributable() && hasDistributableManager) {
            // sasTimerService = new FaultTolerantSasTimerService((DistributableSipManager)getSipManager(), 4);
            // } else {
            // sasTimerService = new StandardSipApplicationSessionTimerService();
            // }
            sasTimerService = new StandardSipApplicationSessionTimerService();
        }
        this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.TIMER_SERVICE, timerService);
        this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SUPPORTED,
                Arrays.asList(sipApplicationDispatcher.getExtensionsSupported()));
        this.getServletContext().setAttribute("javax.servlet.sip.100rel", Boolean.TRUE);
        this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SUPPORTED_RFCs,
                Arrays.asList(sipApplicationDispatcher.getRfcSupported()));
        this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SIP_SESSIONS_UTIL, sipSessionsUtil);
        this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.OUTBOUND_INTERFACES,
                sipApplicationDispatcher.getOutboundInterfaces());
        this.getServletContext().setAttribute("org.mobicents.servlet.sip.SIP_CONNECTORS",
                sipApplicationDispatcher.getSipService().findSipConnectors());
        this.getServletContext().setAttribute("org.mobicents.servlet.sip.DNS_RESOLVER",
                new MobicentsDNSResolver(sipApplicationDispatcher.getDNSServerLocator()));
    }

    public void addChild(SipServletImpl sipServletImpl) {
        Map<String, MobicentsSipServlet> childrenMap = this.deploymentInfo.getChildrenMap();
        Map<String, MobicentsSipServlet> childrenMapByClassName = this.deploymentInfo.getChildrenMapByClassName();
        SipServletImpl existingServlet = (SipServletImpl) childrenMap.get(sipServletImpl.getName());
        if (existingServlet != null) {
            logger.warn(sipServletImpl.getName() + " servlet already present, removing the previous one. "
                    + "This might be due to the fact that the definition of the servlet "
                    + "is present both in annotations and in sip.xml");
            // we remove the previous one (annoations) because it may not have init parameters that has been defined in
            // sip.xml
            // See TCK Test ContextTest.testContext1
            childrenMap.remove(sipServletImpl.getName());
            childrenMapByClassName.remove(sipServletImpl.getServletInfo().getName());
            // super.removeChild(existingServlet);
        }
        childrenMap.put(sipServletImpl.getName(), sipServletImpl);
        childrenMapByClassName.put(sipServletImpl.getServletInfo().getName(), sipServletImpl);
        // super.addChild(sipServletImpl);
    }

    public void removeChild(SipServletImpl sipServletImpl) {
        // super.removeChild(sipServletImpl);
        Map<String, MobicentsSipServlet> childrenMap = this.deploymentInfo.getChildrenMap();
        Map<String, MobicentsSipServlet> childrenMapByClassName = this.deploymentInfo.getChildrenMapByClassName();

        childrenMap.remove(sipServletImpl.getName());
        childrenMapByClassName.remove(sipServletImpl.getServletInfo().getName());
    }

    public void setApplicationDispatcher(SipApplicationDispatcher dispatcher) throws ServletException {
        sipApplicationDispatcher = dispatcher;
        if (sipApplicationDispatcher == null) {
            throw new ServletException("cannot find any application dispatcher for this context "
                    + this.getApplicationName());
        }
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    @Override
    public ServletContextImpl getServletContext() {
        if (context == null) {
            context = new ConvergedApplicationContext(super.getServletContainer(), this);
            // TODO if (getAltDDName() != null)
            // context.setAttribute(Globals.ALT_DD_ATTR,getAltDDName());
        }

        return (ServletContextImpl) (context.getFacade());

    }

    // ConvergedDeploymentManager should call this after web listeners starts
    public boolean contextListenerStart() {
        // boolean ok = super.contextListenerStart();
        // the web listeners couldn't be started so we don't even try to load the sip ones
        /*
         * if(!ok) { return ok; }
         */

        if (logger.isDebugEnabled())
            logger.debug("Configuring sip listeners");

        // Instantiate the required listeners
        // TODO jó-e:
        ClassLoader loader = this.getClass().getClassLoader();
        boolean ok = sipListeners.loadListeners(findSipApplicationListeners(), loader);
        if (!ok) {
            return ok;
        }

        List<ServletContextListener> servletContextListeners = sipListeners.getServletContextListeners();
        if (servletContextListeners != null) {
            ServletContextEvent event = new ServletContextEvent(getServletContext());
            for (ServletContextListener servletContextListener : servletContextListeners) {
                if (servletContextListener == null)
                    continue;

                try {
                    // TODO:fireContainerEvent("beforeContextInitialized", servletContextListener);
                    servletContextListener.contextInitialized(event);
                    // TODO:fireContainerEvent("afterContextInitialized", servletContextListener);
                } catch (Throwable t) {
                    // TODO:fireContainerEvent("afterContextInitialized", servletContextListener);
                    // getLogger().error
                    // (sm.getString("standardContext.listenerStart",
                    // servletContextListener.getClass().getName()), t);
                    // getLogger().error
                    // (MESSAGES.errorSendingContextInitializedEvent(servletContextListener.getClass().getName()), t);
                    ok = false;
                }

                // TODO Annotation processing
            }
        }

        return (ok);

    }

    @Override
    public String getApplicationName() {
        return this.deploymentInfo.getApplicationName();
    }

    @Override
    public String getApplicationNameHashed() {
        return sipApplicationDispatcher.getHashFromApplicationName(getApplicationName());
    }

    @Override
    public boolean hasDistributableManager() {
        return this.hasDistributableManager;
    }

    @Override
    public void setApplicationName(String applicationName) {
        // TODO hívni deploy során, sip-xml parse-olásakor
        deploymentInfo.setApplicationName(applicationName);
    }

    @Override
    public String getDescription() {
        return this.deploymentInfo.getDescription();
    }

    @Override
    public void setDescription(String description) {
        // TODO hívni deploy során, sip-xml parse-olásakor
        deploymentInfo.setDescription(description);

    }

    @Override
    public String getLargeIcon() {
        return this.deploymentInfo.getLargeIcon();
    }

    @Override
    public void setLargeIcon(String largeIcon) {
        // TODO hívni deploy során, sip-xml parse-olásakor
        this.deploymentInfo.setLargeIcon(largeIcon);

    }

    @Override
    public SipListeners getListeners() {
        return this.sipListeners;
    }

    @Override
    public void setListeners(SipListeners listeners) {
        this.sipListeners = (UndertowSipListenersHolder) listeners;

    }

    @Override
    public boolean isMainServlet() {
        return deploymentInfo.isMainServlet();
    }

    @Override
    public String getMainServlet() {
        return deploymentInfo.getMainServlet();
    }

    @Override
    public void setMainServlet(String mainServlet) {
        this.deploymentInfo.setMainServlet(mainServlet);
        this.deploymentInfo.setServletHandler(mainServlet);
        this.deploymentInfo.setMainServlet(true);
    }

    @Override
    public void setServletHandler(String servletHandler) {
        this.deploymentInfo.setServletHandler(servletHandler);

    }

    @Override
    public String getServletHandler() {
        return this.deploymentInfo.getServletHandler();
    }

    @Override
    public int getProxyTimeout() {
        return this.deploymentInfo.getProxyTimeout();
    }

    @Override
    public void setProxyTimeout(int proxyTimeout) {
        this.deploymentInfo.setProxyTimeout(proxyTimeout);

    }

    @Override
    public int getSipApplicationSessionTimeout() {
        return this.deploymentInfo.getProxyTimeout();
    }

    @Override
    public void setSipApplicationSessionTimeout(int proxyTimeout) {
        this.deploymentInfo.setSipApplicationSessionTimeout(proxyTimeout);

    }

    @Override
    public String getSmallIcon() {
        return this.deploymentInfo.getSmallIcon();
    }

    @Override
    public void setSmallIcon(String smallIcon) {
        this.deploymentInfo.setSmallIcon(smallIcon);

    }

    @Override
    public void addSipApplicationListener(String listener) {
        this.deploymentInfo.addSipApplicationListener(listener);
        // TODO:fireContainerEvent("addSipApplicationListener", listener);

    }

    @Override
    public void removeSipApplicationListener(String listener) {
        this.deploymentInfo.removeSipApplicationListener(listener);
        // TODO:fireContainerEvent("removeSipApplicationListener", listener);
    }

    @Override
    public String[] findSipApplicationListeners() {
        return this.deploymentInfo.findSipApplicationListeners();
    }

    @Override
    public Method getSipApplicationKeyMethod() {
        return this.deploymentInfo.getSipApplicationKeyMethod();
    }

    @Override
    public void setSipApplicationKeyMethod(Method sipApplicationKeyMethod) {
        this.deploymentInfo.setSipApplicationKeyMethod(sipApplicationKeyMethod);

    }

    @Override
    public void setSipLoginConfig(MobicentsSipLoginConfig config) {
        this.deploymentInfo.setSipLoginConfig(config);

    }

    @Override
    public MobicentsSipLoginConfig getSipLoginConfig() {
        return this.deploymentInfo.getSipLoginConfig();
    }

    @Override
    public void addSipServletMapping(MobicentsSipServletMapping sipServletMapping) {
        this.deploymentInfo.addSipServletMapping(sipServletMapping);

    }

    @Override
    public void removeSipServletMapping(MobicentsSipServletMapping sipServletMapping) {
        this.deploymentInfo.removeSipServletMapping(sipServletMapping);

    }

    @Override
    public List<MobicentsSipServletMapping> findSipServletMappings() {
        return this.deploymentInfo.findSipServletMappings();
    }

    @Override
    public MobicentsSipServletMapping findSipServletMappings(SipServletRequest sipServletRequest) {
        return this.deploymentInfo.findSipServletMappings(sipServletRequest);
    }

    @Override
    public SipManager getSipManager() {
        return this.sessionManager;
    }

    @Override
    public SipApplicationDispatcher getSipApplicationDispatcher() {
        return this.sipApplicationDispatcher;
    }

    @Override
    public String getEngineName() {
        // TODO???
        return null;
    }

    @Override
    public String getBasePath() {
        // TODO???
        return null;
    }

    //callers: SipApplicationDispatcher.addSipApplications
    @Override
    public boolean notifySipContextListeners(SipContextEvent event) {
        boolean ok = true;
        if (logger.isDebugEnabled()) {
            logger.debug(this.deploymentInfo.getSipServlets().size() + " container to notify of "
                    + event.getEventType());
        }
        if (event.getEventType() == SipContextEventType.SERVLET_INITIALIZED) {
            if (!timerService.isStarted()) {
                timerService.start();
            }
            if (!proxyTimerService.isStarted()) {
                proxyTimerService.start();
            }
            if (!sasTimerService.isStarted()) {
                sasTimerService.start();
            }
        }

        // if(this.available) {
        enterSipApp(null, null, false, true);
        boolean batchStarted = enterSipAppHa(true);
        try {
            
            for (String servletName : this.deploymentInfo.getSipServlets().keySet()) {
                ManagedServlet managedServlet = this.sipServlets.getManagedServlet(servletName);
                if (logger.isDebugEnabled()) {
                    logger.debug("managedServlet " + managedServlet.getServletInfo().getName() + ", class : "
                            + managedServlet.getClass().getName());
                }
                try {
                    Servlet sipServlet = managedServlet.getServlet().getInstance();

                    if (sipServlet instanceof SipServlet) {
                        // Fix for issue 1086 (http://code.google.com/p/mobicents/issues/detail?id=1086) :
                        // Cannot send a request in SipServletListener.initialize() for servlet-selection applications
                        boolean servletHandlerWasNull = false;
                        if (this.getServletHandler() == null) {
                            this.setServletHandler(managedServlet.getServletInfo().getName());
                            servletHandlerWasNull = true;
                        }
                        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
                        try {
                            final ClassLoader cl = this.getClass().getClassLoader();
                            Thread.currentThread().setContextClassLoader(cl);
                            // http://code.google.com/p/sipservlets/issues/detail?id=135
                            // TODO:bindThreadBindingListener();

                            switch (event.getEventType()) {
                            case SERVLET_INITIALIZED: {
                                SipServletContextEvent sipServletContextEvent = new SipServletContextEvent(
                                        getServletContext(), (SipServlet) sipServlet);
                                List<SipServletListener> sipServletListeners = sipListeners.getSipServletsListeners();
                                if (logger.isDebugEnabled()) {
                                    logger.debug(sipServletListeners.size()
                                            + " SipServletListener to notify of servlet initialization");
                                }
                                for (SipServletListener sipServletListener : sipServletListeners) {
                                    sipServletListener.servletInitialized(sipServletContextEvent);
                                }
                                break;
                            }
                            case SIP_CONNECTOR_ADDED: {
                                // reload the outbound interfaces if they have changed
                                this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.OUTBOUND_INTERFACES,
                                        sipApplicationDispatcher.getOutboundInterfaces());
                                // https://code.google.com/p/sipservlets/issues/detail?id=246
                                this.getServletContext().setAttribute("org.mobicents.servlet.sip.SIP_CONNECTORS",
                                        sipApplicationDispatcher.getSipService().findSipConnectors());

                                List<SipConnectorListener> sipConnectorListeners = sipListeners
                                        .getSipConnectorListeners();
                                if (logger.isDebugEnabled()) {
                                    logger.debug(sipConnectorListeners.size()
                                            + " SipConnectorListener to notify of sip connector addition");
                                }
                                for (SipConnectorListener sipConnectorListener : sipConnectorListeners) {
                                    sipConnectorListener.sipConnectorAdded((SipConnector) event.getEventObject());
                                }
                                break;
                            }
                            case SIP_CONNECTOR_REMOVED: {
                                // reload the outbound interfaces if they have changed
                                this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.OUTBOUND_INTERFACES,
                                        sipApplicationDispatcher.getOutboundInterfaces());
                                // https://code.google.com/p/sipservlets/issues/detail?id=246
                                this.getServletContext().setAttribute("org.mobicents.servlet.sip.SIP_CONNECTORS",
                                        sipApplicationDispatcher.getSipService().findSipConnectors());

                                List<SipConnectorListener> sipConnectorListeners = sipListeners
                                        .getSipConnectorListeners();
                                if (logger.isDebugEnabled()) {
                                    logger.debug(sipConnectorListeners.size()
                                            + " SipConnectorListener to notify of sip connector removal");
                                }
                                for (SipConnectorListener sipConnectorListener : sipConnectorListeners) {
                                    sipConnectorListener.sipConnectorRemoved((SipConnector) event.getEventObject());
                                }
                                break;
                            }
                            }
                            if (servletHandlerWasNull) {
                                this.setServletHandler("");
                            }
                        } finally {
                            // http://code.google.com/p/sipservlets/issues/detail?id=135
                            // TODO:unbindThreadBindingListener();
                            Thread.currentThread().setContextClassLoader(oldClassLoader);
                        }
                    }
                } catch (ServletException e) {
                    logger.error("Cannot allocate the servlet " + managedServlet.getClass()
                            + " for notifying the listener " + " of the event " + event.getEventType(), e);
                    ok = false;
                } catch (Throwable e) {
                    logger.error("An error occured when notifying the servlet " + managedServlet.getClass()
                            + " of the event " + event.getEventType(), e);
                    ok = false;
                }
                /*
                 * try { if(sipServlet != null) { //TODO??wrapper.deallocate(sipServlet); } } catch (ServletException e)
                 * { logger.error("Deallocate exception for servlet" + wrapper.getName(), e); ok = false; } catch
                 * (Throwable e) { logger.error("Deallocate exception for servlet" + wrapper.getName(), e); ok = false;
                 * }
                 */

            }
        } finally {
            exitSipAppHa(null, null, batchStarted);
            exitSipApp(null, null);
        }
        // }
        return ok;
    }

    @Override
    public void enterSipApp(MobicentsSipApplicationSession sipApplicationSession, MobicentsSipSession sipSession,
            boolean checkIsManagedThread, boolean isContainerManaged) {
        switch (this.getConcurrencyControlMode()) {
        case SipSession:
            if (sipSession != null) {
                sipSession.acquire();
            }
            break;
        case SipApplicationSession:
            if (logger.isDebugEnabled()) {
                logger.debug("checkIsManagedThread " + checkIsManagedThread + " , isManagedThread "
                        + isManagedThread.get() + ", isContainerManaged " + isContainerManaged);
            }
            // http://code.google.com/p/mobicents/issues/detail?id=2534 &&
            // http://code.google.com/p/mobicents/issues/detail?id=2526
            if (!checkIsManagedThread || (checkIsManagedThread && Boolean.TRUE.equals(isManagedThread.get()))) {
                if (isManagedThread.get() == null) {
                    isManagedThread.set(Boolean.TRUE);
                }
                if (sipApplicationSession != null) {
                    SipApplicationSessionCreationThreadLocal sipApplicationSessionCreationThreadLocal = sipApplicationSessionsAccessedThreadLocal
                            .get();
                    if (sipApplicationSessionCreationThreadLocal == null) {
                        sipApplicationSessionCreationThreadLocal = new SipApplicationSessionCreationThreadLocal();
                        sipApplicationSessionsAccessedThreadLocal.set(sipApplicationSessionCreationThreadLocal);
                    }
                    boolean notPresent = sipApplicationSessionCreationThreadLocal.getSipApplicationSessions().add(
                            sipApplicationSession);
                    if (notPresent && isContainerManaged) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("acquiring sipApplicationSession="
                                    + sipApplicationSession
                                    + " since it is not present in our local thread of accessed sip application sessions ");
                        }
                        sipApplicationSession.acquire();
                    } else if (logger.isDebugEnabled()) {
                        if (!isContainerManaged) {
                            logger.debug("not acquiring sipApplicationSession=" + sipApplicationSession
                                    + " since application specified the container shouldn't manage it ");
                        } else {
                            logger.debug("not acquiring sipApplicationSession=" + sipApplicationSession
                                    + " since it is present in our local thread of accessed sip application sessions ");
                        }
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("not acquiring sipApplicationSession=" + sipApplicationSession
                            + " since isManagedThread is " + isManagedThread.get());
                }
            }
            break;
        case None:
            break;
        }
    }

    @Override
    public void exitSipApp(MobicentsSipApplicationSession sipApplicationSession, MobicentsSipSession sipSession) {
        switch (this.getConcurrencyControlMode()) {
        case SipSession:
            if (sipSession != null) {
                sipSession.release();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("NOT RELEASING SipSession on exit sipApplicationSession=" + sipApplicationSession
                            + " sipSession=" + sipSession + " semaphore=null");
                }
            }
            break;
        case SipApplicationSession:
            boolean wasSessionReleased = false;
            SipApplicationSessionCreationThreadLocal sipApplicationSessionCreationThreadLocal = sipApplicationSessionsAccessedThreadLocal
                    .get();
            if (sipApplicationSessionCreationThreadLocal != null) {
                for (MobicentsSipApplicationSession sipApplicationSessionAccessed : sipApplicationSessionsAccessedThreadLocal
                        .get().getSipApplicationSessions()) {
                    sipApplicationSessionAccessed.release();
                    if (sipApplicationSessionAccessed.equals(sipApplicationSession)) {
                        wasSessionReleased = true;
                    }
                }
                sipApplicationSessionsAccessedThreadLocal.get().getSipApplicationSessions().clear();
                sipApplicationSessionsAccessedThreadLocal.set(null);
                sipApplicationSessionsAccessedThreadLocal.remove();
            }
            isManagedThread.set(null);
            isManagedThread.remove();
            if (!wasSessionReleased) {
                if (sipApplicationSession != null) {
                    sipApplicationSession.release();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("NOT RELEASING SipApplicationSession on exit sipApplicationSession="
                                + sipApplicationSession + " sipSession=" + sipSession + " semaphore=null");
                    }
                }
            }
            break;
        case None:
            break;
        }

    }

    @Override
    public boolean enterSipAppHa(boolean startCacheActivity) {
        boolean batchStarted = false;
        return batchStarted;
    }

    @Override
    public void exitSipAppHa(MobicentsSipServletRequest request, MobicentsSipServletResponse response,
            boolean batchStarted) {
        // TODO Auto-generated method stub

    }

    @Override
    public SipFactory getSipFactoryFacade() {
        return sipFactoryFacade;
    }

    @Override
    public MobicentsSipSessionsUtil getSipSessionsUtil() {
        return sipSessionsUtil;
    }

    @Override
    public TimerService getTimerService() {
        return timerService;
    }

    @Override
    public ProxyTimerService getProxyTimerService() {
        return proxyTimerService;
    }

    @Override
    public SipApplicationSessionTimerService getSipApplicationSessionTimerService() {

        return sasTimerService;
    }

    @Override
    public void setConcurrencyControlMode(ConcurrencyControlMode mode) {
        this.deploymentInfo.setConcurrencyControlMode(mode);
        if (this.deploymentInfo.getConcurrencyControlMode() != null && logger.isDebugEnabled()) {
            logger.debug("Concurrency Control set to " + this.deploymentInfo.getConcurrencyControlMode().toString()
                    + " for application " + this.deploymentInfo.getApplicationName());
        }
    }

    @Override
    public ConcurrencyControlMode getConcurrencyControlMode() {
        return this.deploymentInfo.getConcurrencyControlMode();
    }

    @Override
    public void setSipRubyController(SipRubyController rubyController) {
        this.deploymentInfo.setRubyController(rubyController);

    }

    @Override
    public SipRubyController getSipRubyController() {
        return this.deploymentInfo.getRubyController();
    }

    @Override
    public String getPath() {
        // TODO:
        return this.deploymentInfo.getDeploymentName();
    }

    @Override
    public MobicentsSipServlet findSipServletByClassName(String canonicalName) {
        if (canonicalName == null)
            return (null);
        return this.deploymentInfo.getChildrenMapByClassName().get(canonicalName);
    }

    @Override
    public MobicentsSipServlet findSipServletByName(String name) {
        if (name == null)
            return (null);
        return this.deploymentInfo.getChildrenMap().get(name);
    }

    @Override
    public ClassLoader getSipContextClassLoader() {
        // TODO jó-e:
        return this.getClass().getClassLoader();
    }

    @Override
    public Map<String, MobicentsSipServlet> getChildrenMap() {
        return this.deploymentInfo.getChildrenMap();
    }

    @Override
    public boolean isPackageProtectionEnabled() {
        // TODO
        return false;
    }

    @Override
    public boolean authorize(MobicentsSipServletRequest request) {
        // TODO
        return true;
    }

    @Override
    public SipDigestAuthenticator getDigestAuthenticator() {
        return this.getDigestAuthenticator();
    }

    @Override
    public void enterSipContext() {
        final ClassLoader cl = getSipContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        // http://code.google.com/p/sipservlets/issues/detail?id=135
        // TODO:bindThreadBindingListener();

    }

    @Override
    public void exitSipContext(ClassLoader oldClassLoader) {
        // http://code.google.com/p/sipservlets/issues/detail?id=135
        // TODO:unbindThreadBindingListener();
        Thread.currentThread().setContextClassLoader(oldClassLoader);

    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ManagedServlets getSipServlets() {
        return sipServlets;
    }

}
