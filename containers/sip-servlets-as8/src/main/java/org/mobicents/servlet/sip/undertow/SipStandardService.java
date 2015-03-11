package org.mobicents.servlet.sip.undertow;

import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.message.MessageFactoryExt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;

import javax.sip.SipStack;
import javax.sip.header.ServerHeader;
import javax.sip.header.UserAgentHeader;

import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSAwareRouter;
import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.ext.javax.sip.dns.DefaultDNSServerLocator;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingListener;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingService;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingServiceImpl;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.javax.servlet.CongestionControlPolicy;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.core.message.OutboundProxy;
import org.mobicents.servlet.sip.message.Servlet3SipServletMessageFactory;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

public class SipStandardService implements SipService {
    // the logger
    private static final Logger logger = Logger.getLogger(SipStandardService.class);
    public static final String DEFAULT_SIP_PATH_NAME = "gov.nist";
    public static final String PASS_INVITE_NON_2XX_ACK_TO_LISTENER = "gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER";
    public static final String TCP_POST_PARSING_THREAD_POOL_SIZE = "gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE";
    public static final String AUTOMATIC_DIALOG_SUPPORT_STACK_PROP = "javax.sip.AUTOMATIC_DIALOG_SUPPORT";

    public static final String LOOSE_DIALOG_VALIDATION = "gov.nist.javax.sip.LOOSE_DIALOG_VALIDATION";
    public static final String SERVER_LOG_STACK_PROP = "gov.nist.javax.sip.SERVER_LOG";
    public static final String DEBUG_LOG_STACK_PROP = "gov.nist.javax.sip.DEBUG_LOG";
    public static final String SERVER_HEADER = "org.mobicents.servlet.sip.SERVER_HEADER";
    public static final String USER_AGENT_HEADER = "org.mobicents.servlet.sip.USER_AGENT_HEADER";
    public static final String JVM_ROUTE = "jvmRoute";
    /**
     * The descriptive information string for this implementation.
     */
    private static final String INFO = "org.mobicents.servlet.sip.startup.SipStandardService/1.0";
    // the sip application dispatcher class name defined in the server.xml
    protected String sipApplicationDispatcherClassName = "org.mobicents.servlet.sip.core.UndertowSipApplicationDispatcherImpl";
    // instatiated class from the sipApplicationDispatcherClassName of the sip application dispatcher
    protected SipApplicationDispatcher sipApplicationDispatcher;
    private boolean gatherStatistics = true;
    protected int sipMessageQueueSize = 1500;
    private int backToNormalSipMessageQueueSize = 1300;
    protected int memoryThreshold = 95;
    private int backToNormalMemoryThreshold = 90;
    protected OutboundProxy outboundProxy;
    protected long congestionControlCheckingInterval = 30000;
    private int canceledTimerTasksPurgePeriod = 0;
    // base timer interval for jain sip tx
    private int baseTimerInterval = 500;
    private int t2Interval = 4000;
    private int t4Interval = 5000;
    private int timerDInterval = 32000;
    protected int dispatcherThreadPoolSize = 15;
    private boolean md5ContactUserPart = false;

    protected String concurrencyControlMode = ConcurrencyControlMode.SipApplicationSession.toString();
    protected String congestionControlPolicy = CongestionControlPolicy.ErrorResponse.toString();
    protected String additionalParameterableHeaders;
    protected boolean bypassResponseExecutor = true;
    protected boolean bypassRequestExecutor = true;
    // this should be made available to the application router as a system prop
    protected String darConfigurationFileLocation;
    protected boolean connectorsStartedExternally = false;
    protected boolean dialogPendingRequestChecking = false;

    protected boolean httpFollowsSip = false;
    protected String jvmRoute;
    protected ReplicationStrategy replicationStrategy;

    /**
     * the sip stack path name. Since the sip factory is per classloader it should be set here for all underlying stacks
     */
    private String sipPathName;
    /**
     * use Pretty Encoding
     */
    private boolean usePrettyEncoding = true;

    private SipStack sipStack;
    // defining sip stack properties
    private Properties sipStackProperties;
    private String sipStackPropertiesFileLocation;
    @Deprecated
    private String addressResolverClass = null;
    private String dnsServerLocatorClass = DefaultDNSServerLocator.class.getName();
    private String mobicentsSipServletMessageFactoryClassName = Servlet3SipServletMessageFactory.class.getName();

    // the balancers to send heartbeat to and our health info
    @Deprecated
    private String balancers;

    protected SipProtocolHandler connectors[] = new SipProtocolHandler[0];

    private String name;

    public void addConnector(SipProtocolHandler connector) {
        MobicentsExtendedListeningPoint extendedListeningPoint = null;

        extendedListeningPoint = (MobicentsExtendedListeningPoint) connector.getAttribute(ExtendedListeningPoint.class
                .getSimpleName());

        if (extendedListeningPoint != null) {
            try {
                extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
                sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(
                        extendedListeningPoint);
            } catch (TooManyListenersException e) {
                logger.error("Connector.initialize", e);
            }
        }

        // connector.setPort(((SipProtocolHandler)protocolHandler).getPort());
        connector.setSipStack(sipStack);
        connector.setAttribute(SipApplicationDispatcher.class.getSimpleName(),
                sipApplicationDispatcher);
        // registerSipConnector(connector);


        synchronized (connectors) {
            // connector.setContainer(this.container);
            // connector.setService(this);
            SipProtocolHandler results[] = new SipProtocolHandler[connectors.length + 1];
            System.arraycopy(connectors, 0, results, 0, connectors.length);
            results[connectors.length] = connector;
            connectors = results;
        }
    }

    public void removeConnector(SipProtocolHandler connector) {
        MobicentsExtendedListeningPoint extendedListeningPoint = null;
        extendedListeningPoint = (MobicentsExtendedListeningPoint)  connector.getAttribute(ExtendedListeningPoint.class.getSimpleName());

        if (extendedListeningPoint != null) {
            extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
            sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(
                    extendedListeningPoint);
        }

        synchronized (connectors) {
            int j = -1;
            for (int i = 0; i < connectors.length; i++) {
                if (connector == connectors[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;
            /*
             * TODO:if (!DELAY_CONNECTOR_STARTUP) { if (started && (connectors[j] instanceof Lifecycle)) { try {
             * ((Lifecycle) connectors[j]).stop(); } catch (LifecycleException e) {
             * CatalinaLogger.CORE_LOGGER.errorStoppingConnector(e); } } }
             */
            // connectors[j].setContainer(null);
            // connector.setService(null);
            int k = 0;
            SipProtocolHandler results[] = new SipProtocolHandler[connectors.length - 1];
            for (int i = 0; i < connectors.length; i++) {
                if (i != j)
                    results[k++] = connectors[i];
            }
            connectors = results;

            // Report this property change to interested listeners
            // TODO:support.firePropertyChange("connector", connector, null);
        }

    }

    public void initialize() throws Exception {
        // load the sip application disptacher from the class name specified in the server.xml file
        // and initializes it
        StaticServiceHolder.sipStandardService = this;
        try {
            sipApplicationDispatcher = (SipApplicationDispatcher) Class.forName(sipApplicationDispatcherClassName)
                    .newInstance();
        } catch (InstantiationException e) {
            throw new Exception("Impossible to load the Sip Application Dispatcher", e);
        } catch (IllegalAccessException e) {
            throw new Exception("Impossible to load the Sip Application Dispatcher", e);
        } catch (ClassNotFoundException e) {
            throw new Exception("Impossible to load the Sip Application Dispatcher", e);
        } catch (ClassCastException e) {
            throw new Exception("Sip Application Dispatcher defined does not implement "
                    + SipApplicationDispatcher.class.getName(), e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Pretty encoding of headers enabled ? " + usePrettyEncoding);
        }
        if (sipPathName == null) {
            sipPathName = DEFAULT_SIP_PATH_NAME;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Sip Stack path name : " + sipPathName);
        }
        sipApplicationDispatcher.setSipService(this);
        sipApplicationDispatcher.getSipFactory().initialize(sipPathName, usePrettyEncoding);

        String catalinaBase = getCatalinaBase();
        // TODO: undertow base dir
        if (darConfigurationFileLocation != null) {
            if (!darConfigurationFileLocation.startsWith("file:///")) {
                darConfigurationFileLocation = "file:///" + catalinaBase.replace(File.separatorChar, '/') + "/"
                        + darConfigurationFileLocation;
            }
            System.setProperty("javax.servlet.sip.dar", darConfigurationFileLocation);
        }

        sipApplicationDispatcher.setDomain(this.getName());
        if (baseTimerInterval < 1) {
            throw new Exception("It's forbidden to set the Base Timer Interval to a non positive value");
        }
        initSipStack();
        sipApplicationDispatcher.setBaseTimerInterval(baseTimerInterval);
        sipApplicationDispatcher.setT2Interval(t2Interval);
        sipApplicationDispatcher.setT4Interval(t4Interval);
        sipApplicationDispatcher.setTimerDInterval(timerDInterval);
        sipApplicationDispatcher.setMemoryThreshold(getMemoryThreshold());
        sipApplicationDispatcher.setBackToNormalMemoryThreshold(backToNormalMemoryThreshold);
        sipApplicationDispatcher.setCongestionControlCheckingInterval(getCongestionControlCheckingInterval());
        sipApplicationDispatcher.setCongestionControlPolicyByName(getCongestionControlPolicy());
        sipApplicationDispatcher.setQueueSize(getSipMessageQueueSize());
        sipApplicationDispatcher.setBackToNormalQueueSize(backToNormalSipMessageQueueSize);
        sipApplicationDispatcher.setGatherStatistics(gatherStatistics);
        sipApplicationDispatcher.setConcurrencyControlMode(ConcurrencyControlMode.valueOf(getConcurrencyControlMode()));
        sipApplicationDispatcher.setBypassRequestExecutor(bypassRequestExecutor);
        sipApplicationDispatcher.setBypassResponseExecutor(bypassResponseExecutor);
        sipApplicationDispatcher.setSipStack(sipStack);
        sipApplicationDispatcher.init();
        // Tomcat specific loading case where the connectors are added even before the service is initialized
        // so we need to set the sip stack before it starts
        // TODO:synchronized (connectors) {
        // for (Connector connector : connectors) {
        // ProtocolHandler protocolHandler = connector.getProtocolHandler();
        // if(protocolHandler instanceof SipProtocolHandler) {
        // connector.setPort(((SipProtocolHandler)protocolHandler).getPort());
        // ((SipProtocolHandler)protocolHandler).setSipStack(sipStack);
        // ((SipProtocolHandler)protocolHandler).setAttribute(SipApplicationDispatcher.class.getSimpleName(),
        // sipApplicationDispatcher);
        // registerSipConnector(connector);
        // }
        // }
        // }
    }

    public void start() throws Exception {
        synchronized (connectors) {
            for (SipProtocolHandler protocolHandler : connectors) {

                // Jboss sepcific loading case
                Boolean isSipConnector = false;
                if (protocolHandler instanceof SipProtocolHandler)
                    isSipConnector = (Boolean) ((SipProtocolHandler) protocolHandler)
                            .getAttribute(SipProtocolHandler.IS_SIP_CONNECTOR);
                if (isSipConnector != null && isSipConnector) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Attaching the sip application dispatcher "
                                + "as a sip listener to connector listening on port " /*
                                                                                       * + connector.getPort()
                                                                                       */);
                    }
                    ((SipProtocolHandler) protocolHandler).setAttribute(SipApplicationDispatcher.class.getSimpleName(),
                            sipApplicationDispatcher);
                    ((SipProtocolHandler) protocolHandler).setSipStack(sipStack);
                    connectorsStartedExternally = true;
                }
                // Tomcat specific loading case
                MobicentsExtendedListeningPoint extendedListeningPoint = null;
                if (protocolHandler instanceof SipProtocolHandler) {
                    extendedListeningPoint = (MobicentsExtendedListeningPoint) ((SipProtocolHandler) protocolHandler)
                            .getAttribute(ExtendedListeningPoint.class.getSimpleName());
                }
                if (extendedListeningPoint != null && sipStack != null) {
                    try {
                        extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
                        sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(
                                extendedListeningPoint);
                        connectorsStartedExternally = false;
                    } catch (TooManyListenersException e) {
                        throw new Exception("Couldn't add the sip application dispatcher " + sipApplicationDispatcher
                                + " as a listener to the following listening point provider " + extendedListeningPoint,
                                e);
                    }
                }
            }
        }
        if (!connectorsStartedExternally) {
            sipApplicationDispatcher.start();
        }

        if (this.getSipMessageQueueSize() <= 0)
            throw new Exception("Message queue size can not be 0 or less");

        if (logger.isDebugEnabled()) {
            logger.debug("SIP Standard Service Started.");
        }
    }

    public void stop() throws Exception {
        // Tomcat specific unloading case
        // Issue 1411 http://code.google.com/p/mobicents/issues/detail?id=1411
        // Sip Connectors should be removed after removing all Sip Servlets to allow them to send BYE to terminate cleanly
        synchronized (connectors) {
            for (SipProtocolHandler connector : connectors) {
                MobicentsExtendedListeningPoint extendedListeningPoint = null;
                if (connector instanceof SipProtocolHandler) {
                    extendedListeningPoint = (MobicentsExtendedListeningPoint)
                        ((SipProtocolHandler)connector).getAttribute(ExtendedListeningPoint.class.getSimpleName());
                }
                if(extendedListeningPoint != null) {                    
                    extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
                    sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(extendedListeningPoint);
                }
            }
        }   
        if(!connectorsStartedExternally) {
            sipApplicationDispatcher.stop();
        }
        //super.stop();
        if(logger.isDebugEnabled()) {
            logger.debug("SIP Standard Service Stopped.");
        }
    //  setState(LifecycleState.STOPPING);      
    }
    
    protected String getCatalinaBase() {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null) {
            catalinaBase = System.getProperty("catalina.home");
        }
        if (catalinaBase == null) {
            catalinaBase = ".";
        }
        return catalinaBase;
    }

    protected void initSipStack() throws Exception {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Initializing SIP stack");
            }

            // This simply puts HTTP and SSL port numbers in JVM properties menat to be read by jsip ha when sending
            // heart beats with Node description.
            initializeSystemPortProperties();

            String catalinaBase = getCatalinaBase();
            if (sipStackPropertiesFileLocation != null && !sipStackPropertiesFileLocation.startsWith("file:///")) {
                sipStackPropertiesFileLocation = "file:///" + catalinaBase.replace(File.separatorChar, '/') + "/"
                        + sipStackPropertiesFileLocation;
            }
            boolean isPropsLoaded = false;
            if (sipStackProperties == null) {
                sipStackProperties = new Properties();
            } else {
                isPropsLoaded = true;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Loading SIP stack properties from following file : " + sipStackPropertiesFileLocation);
            }
            if (sipStackPropertiesFileLocation != null) {
                // hack to get around space char in path see
                // http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html,
                // we create a URL since it's permissive enough
                File sipStackPropertiesFile = null;
                URL url = null;
                try {
                    url = new URL(sipStackPropertiesFileLocation);
                } catch (MalformedURLException e) {
                    logger.fatal("Cannot find the sip stack properties file ! ", e);
                    throw new IllegalArgumentException("The Default Application Router file Location : "
                            + sipStackPropertiesFileLocation + " is not valid ! ", e);
                }
                try {
                    sipStackPropertiesFile = new File(new URI(sipStackPropertiesFileLocation));
                } catch (URISyntaxException e) {
                    // if the uri contains space this will fail, so getting the path will work
                    sipStackPropertiesFile = new File(url.getPath());
                }
                FileInputStream sipStackPropertiesInputStream = null;
                try {
                    sipStackPropertiesInputStream = new FileInputStream(sipStackPropertiesFile);
                    sipStackProperties.load(sipStackPropertiesInputStream);
                } catch (Exception e) {
                    logger.warn("Could not find or problem when loading the sip stack properties file : "
                            + sipStackPropertiesFileLocation, e);
                } finally {
                    if (sipStackPropertiesInputStream != null) {
                        try {
                            sipStackPropertiesInputStream.close();
                        } catch (IOException e) {
                            logger.error(
                                    "fail to close the following file " + sipStackPropertiesFile.getAbsolutePath(), e);
                        }
                    }
                }

                String debugLog = sipStackProperties.getProperty(DEBUG_LOG_STACK_PROP);
                if (debugLog != null && debugLog.length() > 0 && !debugLog.startsWith("file:///")) {
                    sipStackProperties.setProperty(DEBUG_LOG_STACK_PROP, catalinaBase + "/" + debugLog);
                }
                String serverLog = sipStackProperties.getProperty(SERVER_LOG_STACK_PROP);
                if (serverLog != null && serverLog.length() > 0 && !serverLog.startsWith("file:///")) {
                    sipStackProperties.setProperty(SERVER_LOG_STACK_PROP, catalinaBase + "/" + serverLog);
                }
                // The whole MSS is built upon those assumptions, so those properties are not overrideable
                sipStackProperties.setProperty(AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");
                sipStackProperties.setProperty(LOOSE_DIALOG_VALIDATION, "true");
                sipStackProperties.setProperty(PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
                isPropsLoaded = true;
            } else {
                logger.warn("no sip stack properties file defined ");
            }
            if (!isPropsLoaded) {
                logger.warn("loading default Mobicents Sip Servlets sip stack properties");
                // Silently set default values
                sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "true");
                sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
                sipStackProperties.setProperty(DEBUG_LOG_STACK_PROP, catalinaBase + "/" + "mss-jsip-" + getName()
                        + "-debug.txt");
                sipStackProperties.setProperty(SERVER_LOG_STACK_PROP, catalinaBase + "/" + "mss-jsip-" + getName()
                        + "-messages.xml");
                sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
                sipStackProperties.setProperty(AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");
                sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
                sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
                sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
                sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "0");
                sipStackProperties.setProperty(LOOSE_DIALOG_VALIDATION, "true");
                sipStackProperties.setProperty(PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
                sipStackProperties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
            }

            if (sipStackProperties.get(TCP_POST_PARSING_THREAD_POOL_SIZE) == null) {
                sipStackProperties.setProperty(TCP_POST_PARSING_THREAD_POOL_SIZE, "30");
            }

            // set the DNSServerLocator allowing to support RFC 3263 and do DNS lookups to resolve uris
            if (dnsServerLocatorClass != null && dnsServerLocatorClass.trim().length() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sip Stack " + sipStackProperties.getProperty("javax.sip.STACK_NAME")
                            + " will be using " + dnsServerLocatorClass + " as DNSServerLocator");
                }
                try {
                    // create parameters argument to identify constructor
                    Class[] paramTypes = new Class[0];
                    // get constructor of AddressResolver in order to instantiate
                    Constructor dnsServerLocatorConstructor = Class.forName(dnsServerLocatorClass).getConstructor(
                            paramTypes);
                    // Wrap properties object in order to pass to constructor of AddressResolver
                    Object[] conArgs = new Object[0];
                    // Creates a new instance of AddressResolver Class with the supplied sipApplicationDispatcher.
                    DNSServerLocator dnsServerLocator = (DNSServerLocator) dnsServerLocatorConstructor
                            .newInstance(conArgs);
                    sipApplicationDispatcher.setDNSServerLocator(dnsServerLocator);
                    if (sipStackProperties.getProperty("javax.sip.ROUTER_PATH") == null) {
                        sipStackProperties
                                .setProperty("javax.sip.ROUTER_PATH", DNSAwareRouter.class.getCanonicalName());
                    }
                } catch (Exception e) {
                    logger.error("Couldn't set the AddressResolver " + addressResolverClass, e);
                    throw e;
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("no DNSServerLocator will be used since none has been specified.");
                }
            }

            String serverHeaderValue = sipStackProperties.getProperty(SERVER_HEADER);
            if (serverHeaderValue != null) {
                List<String> serverHeaderList = new ArrayList<String>();
                StringTokenizer stringTokenizer = new StringTokenizer(serverHeaderValue, ",");
                while (stringTokenizer.hasMoreTokens()) {
                    serverHeaderList.add(stringTokenizer.nextToken());
                }
                ServerHeader serverHeader = sipApplicationDispatcher.getSipFactory().getHeaderFactory()
                        .createServerHeader(serverHeaderList);
                ((MessageFactoryExt) sipApplicationDispatcher.getSipFactory().getMessageFactory())
                        .setDefaultServerHeader(serverHeader);
            }
            String userAgent = sipStackProperties.getProperty(USER_AGENT_HEADER);
            if (userAgent != null) {
                List<String> userAgentList = new ArrayList<String>();
                StringTokenizer stringTokenizer = new StringTokenizer(userAgent, ",");
                while (stringTokenizer.hasMoreTokens()) {
                    userAgentList.add(stringTokenizer.nextToken());
                }
                UserAgentHeader userAgentHeader = sipApplicationDispatcher.getSipFactory().getHeaderFactory()
                        .createUserAgentHeader(userAgentList);
                ((MessageFactoryExt) sipApplicationDispatcher.getSipFactory().getMessageFactory())
                        .setDefaultUserAgentHeader(userAgentHeader);
            }
            if (balancers != null) {
                if (sipStackProperties.get(LoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME) == null) {
                    sipStackProperties.put(LoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME,
                            LoadBalancerHeartBeatingServiceImpl.class.getCanonicalName());
                }
                if (sipStackProperties.get(LoadBalancerHeartBeatingService.BALANCERS) == null) {
                    sipStackProperties.put(LoadBalancerHeartBeatingService.BALANCERS, balancers);
                }
            }
            String replicationStrategyString = sipStackProperties
                    .getProperty(ClusteredSipStack.REPLICATION_STRATEGY_PROPERTY);
            if (replicationStrategyString == null) {
                replicationStrategyString = ReplicationStrategy.ConfirmedDialog.toString();
            }
            boolean replicateApplicationData = false;
            if (replicationStrategyString.equals(ReplicationStrategy.EarlyDialog.toString())) {
                replicateApplicationData = true;
            }
            if (replicationStrategyString != null) {
                replicationStrategy = ReplicationStrategy.valueOf(replicationStrategyString);
            }
            sipStackProperties.put(ClusteredSipStack.REPLICATION_STRATEGY_PROPERTY, replicationStrategyString);
            sipStackProperties.put(ClusteredSipStack.REPLICATE_APPLICATION_DATA,
                    Boolean.valueOf(replicateApplicationData).toString());
            if (logger.isInfoEnabled()) {
                logger.info("Mobicents Sip Servlets sip stack properties : " + sipStackProperties);
            }
            // Create SipStack object
            sipStack = sipApplicationDispatcher.getSipFactory().getJainSipFactory().createSipStack(sipStackProperties);
            LoadBalancerHeartBeatingService loadBalancerHeartBeatingService = null;
            if (sipStack instanceof ClusteredSipStack) {
                loadBalancerHeartBeatingService = ((ClusteredSipStack) sipStack).getLoadBalancerHeartBeatingService();
                // TODO:if ((this.container != null) && (this.container instanceof Engine) &&
                // ((Engine)container).getJvmRoute() != null) {
                // final String jvmRoute = ((Engine)container).getJvmRoute();
                // if(jvmRoute != null) {
                // loadBalancerHeartBeatingService.setJvmRoute(jvmRoute);
                // setJvmRoute(jvmRoute);
                // }
                // }
            }
            if (sipApplicationDispatcher != null && loadBalancerHeartBeatingService != null
                    && sipApplicationDispatcher instanceof LoadBalancerHeartBeatingListener) {
                loadBalancerHeartBeatingService
                        .addLoadBalancerHeartBeatingListener((LoadBalancerHeartBeatingListener) sipApplicationDispatcher);
            }
            // for nist sip stack set the DNS Address resolver allowing to make DNS SRV lookups
            if (sipStack instanceof SipStackExt && addressResolverClass != null
                    && addressResolverClass.trim().length() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sip Stack " + sipStack.getStackName() + " will be using " + addressResolverClass
                            + " as AddressResolver");
                }
                try {
                    // create parameters argument to identify constructor
                    Class[] paramTypes = new Class[1];
                    paramTypes[0] = SipApplicationDispatcher.class;
                    // get constructor of AddressResolver in order to instantiate
                    Constructor addressResolverConstructor = Class.forName(addressResolverClass).getConstructor(
                            paramTypes);
                    // Wrap properties object in order to pass to constructor of AddressResolver
                    Object[] conArgs = new Object[1];
                    conArgs[0] = sipApplicationDispatcher;
                    // Creates a new instance of AddressResolver Class with the supplied sipApplicationDispatcher.
                    AddressResolver addressResolver = (AddressResolver) addressResolverConstructor.newInstance(conArgs);
                    ((SipStackExt) sipStack).setAddressResolver(addressResolver);
                } catch (Exception e) {
                    logger.error("Couldn't set the AddressResolver " + addressResolverClass, e);
                    throw e;
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("no AddressResolver will be used since none has been specified.");
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("SIP stack initialized");
            }
        } catch (Exception ex) {
            throw new Exception("A problem occured while initializing the SIP Stack", ex);
        }
    }

    public void initializeSystemPortProperties() {
        // TODO:for (Connector connector : connectors) {
        // if(connector.getProtocol().contains("HTTP")) {
        // if(connector.getSecure()) {
        // System.setProperty("org.mobicents.properties.sslPort", Integer.toString(connector.getPort()));
        // } else {
        // System.setProperty("org.mobicents.properties.httpPort", Integer.toString(connector.getPort()));
        // }
        // }
        // }
    }

    public String getConcurrencyControlMode() {
        return concurrencyControlMode;
    }

    public String getCongestionControlPolicy() {
        return congestionControlPolicy;
    }

    public long getCongestionControlCheckingInterval() {
        return congestionControlCheckingInterval;
    }

    public int getMemoryThreshold() {
        return memoryThreshold;
    }

    public int getSipMessageQueueSize() {
        return sipMessageQueueSize;
    }

    @Override
    public SipConnector findSipConnector(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SipConnector[] findSipConnectors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getCanceledTimerTasksPurgePeriod() {
        return canceledTimerTasksPurgePeriod;
    }

    @Override
    public int getDispatcherThreadPoolSize() {
        return dispatcherThreadPoolSize;
    }

    @Override
    public String getJvmRoute() {
        return jvmRoute;
    }

    @Override
    public String getMobicentsSipServletMessageFactoryClassName() {
        return mobicentsSipServletMessageFactoryClassName;
    }

    @Override
    public OutboundProxy getOutboundProxy() {
        return outboundProxy;
    }

    @Override
    public ReplicationStrategy getReplicationStrategy() {
        return replicationStrategy;
    }

    @Override
    public SipApplicationDispatcher getSipApplicationDispatcher() {
        return this.sipApplicationDispatcher;
    }

    @Override
    public SipStack getSipStack() {
        return sipStack;
    }

    @Override
    public boolean isDialogPendingRequestChecking() {
        return dialogPendingRequestChecking;
    }

    @Override
    public boolean isHttpFollowsSip() {

        return this.httpFollowsSip;
    }

    @Override
    public boolean isMd5ContactUserPart() {
        return md5ContactUserPart;
    }

    @Override
    public void setMobicentsSipServletMessageFactoryClassName(String mobicentsSipServletMessageFactoryClassName) {
        this.mobicentsSipServletMessageFactoryClassName = mobicentsSipServletMessageFactoryClassName;

    }

    @Override
    public void setSipApplicationDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
        this.sipApplicationDispatcher = sipApplicationDispatcher;
    }

    public String getDarConfigurationFileLocation() {
        return darConfigurationFileLocation;
    }

    public void setDarConfigurationFileLocation(String darConfigurationFileLocation) {
        this.darConfigurationFileLocation = darConfigurationFileLocation;
    }

    public void setSipApplicationDispatcherClassName(String sipApplicationDispatcherClassName) {
        this.sipApplicationDispatcherClassName = sipApplicationDispatcherClassName;
    }

    public void setSipPathName(String sipPathName) {
        this.sipPathName = sipPathName;
    }

    public void setCongestionControlCheckingInterval(long congestionControlCheckingInterval) {
        this.congestionControlCheckingInterval = congestionControlCheckingInterval;
    }

    public void setConcurrencyControlMode(String concurrencyControlMode) {
        this.concurrencyControlMode = concurrencyControlMode;
    }

    public void setCongestionControlPolicy(String congestionControlPolicy) {
        this.congestionControlPolicy = congestionControlPolicy;
    }

    public void setSipStackProperties(Properties sipStackProperties) {
        this.sipStackProperties = sipStackProperties;
    }

    public void setUsePrettyEncoding(boolean usePrettyEncoding) {
        this.usePrettyEncoding = usePrettyEncoding;
    }

    public String getSipApplicationDispatcherClassName() {
        return sipApplicationDispatcherClassName;
    }

    public void setSipStackPropertiesFile(String sipStackPropertiesFileLocation) {
        this.sipStackPropertiesFileLocation = sipStackPropertiesFileLocation;
    }

    public void setBaseTimerInterval(int baseTimerInterval2) {
        this.baseTimerInterval = baseTimerInterval2;

    }

    public void setT2Interval(int t2Interval) {
        this.t2Interval = t2Interval;
    }

    public void setT4Interval(int t4Interval) {
        this.t4Interval = t4Interval;
    }

    public void setTimerDInterval(int timerDInterval) {
        this.timerDInterval = timerDInterval;
    }

    public void setAdditionalParameterableHeaders(String additionalParameterableHeaders) {
        this.additionalParameterableHeaders = additionalParameterableHeaders;
    }

    public void setDialogPendingRequestChecking(boolean dialogPendingRequestChecking) {
        this.dialogPendingRequestChecking = dialogPendingRequestChecking;
    }

    public void setCanceledTimerTasksPurgePeriod(int canceledTimerTasksPurgePeriod) {
        this.canceledTimerTasksPurgePeriod = canceledTimerTasksPurgePeriod;
    }

    public void setMemoryThreshold(int memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }

    public void setBackToNormalMemoryThreshold(int backToNormalMemoryThreshold) {
        this.backToNormalMemoryThreshold = backToNormalMemoryThreshold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOutboundProxy(String outboundProxy) {
        if (outboundProxy != null) {
            this.outboundProxy = new OutboundProxy(outboundProxy);
        } else {
            this.outboundProxy = null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Outbound Proxy : " + outboundProxy);
        }
    }
}
