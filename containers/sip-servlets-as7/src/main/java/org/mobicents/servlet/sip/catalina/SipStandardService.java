/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.sip.catalina;


import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.message.MessageFactoryExt;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sip.SipStack;
import javax.sip.header.ServerHeader;
import javax.sip.header.UserAgentHeader;

import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.coyote.ProtocolHandler;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.ext.javax.sip.dns.DNSAwareRouter;
import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.ext.javax.sip.dns.DefaultDNSServerLocator;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingListener;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingService;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingServiceImpl;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.javax.servlet.CongestionControlPolicy;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.message.OutboundProxy;
import org.mobicents.servlet.sip.message.Servlet3SipServletMessageFactory;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * Sip Servlet implementation of the <code>SipService</code> interface.  
 * This class inherits from the Tomcat StandardService. It adds a SipApplicationDispatcher
 * that will be listen for sip messages received by the sip stacks started by 
 * the sip connectors associated with this context.
 * This has one attribute which is the sipApplicationDispatcherClassName allowing one
 * to specify the class name of the sipApplicationDispacther to easily replace
 * the default sipApplicationDispatcher with a custom one.
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 */
public class SipStandardService extends StandardService implements CatalinaSipService {
	//the logger
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
	public  static final String JVM_ROUTE = "jvmRoute";
	/**
     * The descriptive information string for this implementation.
     */
    private static final String INFO =
        "org.mobicents.servlet.sip.startup.SipStandardService/1.0";
	//the sip application dispatcher class name defined in the server.xml
	protected String sipApplicationDispatcherClassName;
	//instatiated class from the sipApplicationDispatcherClassName of the sip application dispatcher 
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
	//this should be made available to the application router as a system prop
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
	
	//the balancers to send heartbeat to and our health info
	@Deprecated
	private String balancers;
	
	@Override
    public String getInfo() {
        return (INFO);
    }
	
	@Override
	public void addConnector(Connector connector) {
		if (initialized) {
			MobicentsExtendedListeningPoint extendedListeningPoint = null;
			if (connector.getProtocolHandler() instanceof SipProtocolHandler) {
			extendedListeningPoint = (MobicentsExtendedListeningPoint)
					((SipProtocolHandler)connector.getProtocolHandler()).getAttribute(ExtendedListeningPoint.class.getSimpleName());}
			if(extendedListeningPoint != null) {
				try {
					extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
					sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
				} catch (TooManyListenersException e) {
					logger.error("Connector.initialize", e);
				}			
			}
			ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if(protocolHandler instanceof SipProtocolHandler) {
				connector.setPort(((SipProtocolHandler)protocolHandler).getPort());
				((SipProtocolHandler)protocolHandler).setSipStack(sipStack);
				((SipProtocolHandler)protocolHandler).setAttribute(SipApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);			
				registerSipConnector(connector);
			}		
		}
		else {
		}
		super.addConnector(connector);
	}

	/**
	 * Register the sip connector under a different name than HTTP Connector and we add the transport to avoid clashing with 2 connectors having the same port and address
	 * @param connector connector to register
	 */
	protected void registerSipConnector(Connector connector) {
		try {
		     
		    ObjectName objectName = createSipConnectorObjectName(connector, getName(), "SipConnector");
		    Registry.getRegistry(null, null)
		        .registerComponent(connector, objectName, null);
//TODO		    connector.setController(objectName);
		} catch (Exception e) {
		    logger.error( "Error registering connector ", e);
		}
		if(logger.isDebugEnabled())
		    logger.debug("Creating name for connector " + getObjectName());
	}
	
	@Override
	public void removeConnector(Connector connector) {
		MobicentsExtendedListeningPoint extendedListeningPoint = null;
		if (connector.getProtocolHandler() instanceof SipProtocolHandler){
		extendedListeningPoint = (MobicentsExtendedListeningPoint)
			((SipProtocolHandler)connector.getProtocolHandler()).getAttribute(ExtendedListeningPoint.class.getSimpleName());}
		if(extendedListeningPoint != null) {
			extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
			sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(extendedListeningPoint);
		}
		super.removeConnector(connector);
	}
	
	@Override
	public void initialize() throws LifecycleException {
		//load the sip application disptacher from the class name specified in the server.xml file
		//and initializes it
		StaticServiceHolder.sipStandardService = this;
		try {
			sipApplicationDispatcher = (SipApplicationDispatcher)
				Class.forName(sipApplicationDispatcherClassName).newInstance();					
		} catch (InstantiationException e) {
			throw new LifecycleException("Impossible to load the Sip Application Dispatcher",e);
		} catch (IllegalAccessException e) {
			throw new LifecycleException("Impossible to load the Sip Application Dispatcher",e);
		} catch (ClassNotFoundException e) {
			throw new LifecycleException("Impossible to load the Sip Application Dispatcher",e);
		} catch (ClassCastException e) {
			throw new LifecycleException("Sip Application Dispatcher defined does not implement " + SipApplicationDispatcher.class.getName(),e);
		}
		if(logger.isInfoEnabled()) {
			logger.info("Pretty encoding of headers enabled ? " + usePrettyEncoding);
		}
		if(sipPathName == null) {
			sipPathName = DEFAULT_SIP_PATH_NAME;
		}
		if(logger.isInfoEnabled()) {
			logger.info("Sip Stack path name : " + sipPathName);
		}
		sipApplicationDispatcher.setSipService(this);
		sipApplicationDispatcher.getSipFactory().initialize(sipPathName, usePrettyEncoding);
		
		String catalinaBase = getCatalinaBase();
		if(darConfigurationFileLocation != null) {
			if(!darConfigurationFileLocation.startsWith("file:///")) {
				darConfigurationFileLocation = "file:///" + catalinaBase.replace(File.separatorChar, '/') + "/" + darConfigurationFileLocation;
			}
			System.setProperty("javax.servlet.sip.dar", darConfigurationFileLocation);
 		}
		super.initialize();
		sipApplicationDispatcher.setDomain(this.getName());
		if(baseTimerInterval < 1) {
			throw new LifecycleException("It's forbidden to set the Base Timer Interval to a non positive value");		
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
		synchronized (connectors) {
			for (Connector connector : connectors) {				
				ProtocolHandler protocolHandler = connector.getProtocolHandler();
				if(protocolHandler instanceof SipProtocolHandler) {
					connector.setPort(((SipProtocolHandler)protocolHandler).getPort());
					((SipProtocolHandler)protocolHandler).setSipStack(sipStack);
					((SipProtocolHandler)protocolHandler).setAttribute(SipApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);			
					registerSipConnector(connector);
				}		
			}
		}
	}
	
	@Override
	public void start() throws LifecycleException {		
		super.start();		
		synchronized (connectors) {
			for (Connector connector : connectors) {
				final ProtocolHandler protocolHandler = connector.getProtocolHandler();
				//Jboss sepcific loading case
				Boolean isSipConnector = false;
				if (protocolHandler instanceof SipProtocolHandler)
					isSipConnector = (Boolean) ((SipProtocolHandler)protocolHandler).getAttribute(SipProtocolHandler.IS_SIP_CONNECTOR);								
				if(isSipConnector != null && isSipConnector) {
					if(logger.isDebugEnabled()) {
						logger.debug("Attaching the sip application dispatcher " +
							"as a sip listener to connector listening on port " + 
							connector.getPort());
					}
					((SipProtocolHandler)protocolHandler).setAttribute(SipApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);
					((SipProtocolHandler)protocolHandler).setSipStack(sipStack);	
					connectorsStartedExternally = true;
				} 
				//Tomcat specific loading case
				MobicentsExtendedListeningPoint extendedListeningPoint = null;
				if (protocolHandler instanceof SipProtocolHandler) {
				extendedListeningPoint = (MobicentsExtendedListeningPoint)
					((SipProtocolHandler)protocolHandler).getAttribute(ExtendedListeningPoint.class.getSimpleName());}						
				if(extendedListeningPoint != null && sipStack != null) {					
					try {
						extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
						sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
			        	connectorsStartedExternally = false;	
					} catch (TooManyListenersException e) {
						throw new LifecycleException("Couldn't add the sip application dispatcher " 
								+ sipApplicationDispatcher + " as a listener to the following listening point provider " + extendedListeningPoint, e);
					}		        				        		
				}
			}
		}
		if(!connectorsStartedExternally) {
			sipApplicationDispatcher.start();
		}
		
		if(this.getSipMessageQueueSize() <= 0)
			throw new LifecycleException("Message queue size can not be 0 or less");

		if(logger.isDebugEnabled()) {
			logger.debug("SIP Standard Service Started.");
		}
	}
	
	public String getJvmRoute() {
		return this.jvmRoute;
	}
	
	public void setJvmRoute(String jvmRoute) {
		this.jvmRoute = jvmRoute;
	}

	protected void initSipStack() throws LifecycleException {
		try {	
			if(logger.isDebugEnabled()) {
				logger.debug("Initializing SIP stack");
			}						
			
			// This simply puts HTTP and SSL port numbers in JVM properties menat to be read by jsip ha when sending heart beats with Node description.
			initializeSystemPortProperties();
			
			String catalinaBase = getCatalinaBase();
	        if(sipStackPropertiesFileLocation != null && !sipStackPropertiesFileLocation.startsWith("file:///")) {
				sipStackPropertiesFileLocation = "file:///" + catalinaBase.replace(File.separatorChar, '/') + "/" + sipStackPropertiesFileLocation;
	 		}
	        boolean isPropsLoaded = false;
	        if(sipStackProperties == null) {
	        	sipStackProperties = new Properties();
	        } else {
	        	isPropsLoaded = true;
	        }
	        
			if (logger.isDebugEnabled()) {
				logger.debug("Loading SIP stack properties from following file : " + sipStackPropertiesFileLocation);
			}
			if(sipStackPropertiesFileLocation != null) {
				//hack to get around space char in path see http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html, 
				// we create a URL since it's permissive enough
				File sipStackPropertiesFile = null;
				URL url = null;
				try {
					url = new URL(sipStackPropertiesFileLocation);
				} catch (MalformedURLException e) {
					logger.fatal("Cannot find the sip stack properties file ! ",e);
					throw new IllegalArgumentException("The Default Application Router file Location : "+sipStackPropertiesFileLocation+" is not valid ! ",e);
				}
				try {
					sipStackPropertiesFile = new File(new URI(sipStackPropertiesFileLocation));
				} catch (URISyntaxException e) {
					//if the uri contains space this will fail, so getting the path will work
					sipStackPropertiesFile = new File(url.getPath());
				}		
				FileInputStream sipStackPropertiesInputStream = null;
				try {
					sipStackPropertiesInputStream = new FileInputStream(sipStackPropertiesFile);
					sipStackProperties.load(sipStackPropertiesInputStream);
				} catch (Exception e) {
					logger.warn("Could not find or problem when loading the sip stack properties file : " + sipStackPropertiesFileLocation, e);		
				} finally {			
					if(sipStackPropertiesInputStream != null) {
						try {
							sipStackPropertiesInputStream.close();
						} catch (IOException e) {
							logger.error("fail to close the following file " + sipStackPropertiesFile.getAbsolutePath(), e);
						}
					}
				}	
					
				String debugLog = sipStackProperties.getProperty(DEBUG_LOG_STACK_PROP);
				if(debugLog != null && debugLog.length() > 0 && !debugLog.startsWith("file:///")) {				
					sipStackProperties.setProperty(DEBUG_LOG_STACK_PROP,
						catalinaBase + "/" + debugLog);
				}
				String serverLog = sipStackProperties.getProperty(SERVER_LOG_STACK_PROP);
				if(serverLog != null && serverLog.length() > 0 && !serverLog.startsWith("file:///")) {
					sipStackProperties.setProperty(SERVER_LOG_STACK_PROP,
						catalinaBase + "/" + serverLog);
				}
				// The whole MSS is built upon those assumptions, so those properties are not overrideable
				sipStackProperties.setProperty(AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");
				sipStackProperties.setProperty(LOOSE_DIALOG_VALIDATION, "true");
				sipStackProperties.setProperty(PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
				isPropsLoaded = true;
			} else {
				logger.warn("no sip stack properties file defined ");		
			}	
			if(!isPropsLoaded) {
				logger.warn("loading default Mobicents Sip Servlets sip stack properties");
				// Silently set default values
				sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
						"true");
				sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
						"LOG4J");
				sipStackProperties.setProperty(DEBUG_LOG_STACK_PROP,
						catalinaBase + "/" + "mss-jsip-" + getName() +"-debug.txt");
				sipStackProperties.setProperty(SERVER_LOG_STACK_PROP,
						catalinaBase + "/" + "mss-jsip-" + getName() +"-messages.xml");
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
			
			if(sipStackProperties.get(TCP_POST_PARSING_THREAD_POOL_SIZE) == null) {
				sipStackProperties.setProperty(TCP_POST_PARSING_THREAD_POOL_SIZE, "30");
			}	
			
			// set the DNSServerLocator allowing to support RFC 3263 and do DNS lookups to resolve uris
			if(dnsServerLocatorClass != null && dnsServerLocatorClass.trim().length() > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("Sip Stack " + sipStackProperties.getProperty("javax.sip.STACK_NAME") +" will be using " + dnsServerLocatorClass + " as DNSServerLocator");
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
		            DNSServerLocator dnsServerLocator = (DNSServerLocator) dnsServerLocatorConstructor.newInstance(conArgs);
		            sipApplicationDispatcher.setDNSServerLocator(dnsServerLocator);
		            if(sipStackProperties.getProperty("javax.sip.ROUTER_PATH") == null) {
		            	sipStackProperties.setProperty("javax.sip.ROUTER_PATH", DNSAwareRouter.class.getCanonicalName());
		            }
		        } catch (Exception e) {
		            logger.error("Couldn't set the AddressResolver " + addressResolverClass, e);
		            throw e;
		        }
			} else {
				if(logger.isInfoEnabled()) {
					logger.info("no DNSServerLocator will be used since none has been specified.");
				}
			}	
			
			String serverHeaderValue = sipStackProperties.getProperty(SERVER_HEADER);
			if(serverHeaderValue != null) {
				List<String> serverHeaderList = new ArrayList<String>();
				StringTokenizer stringTokenizer = new StringTokenizer(serverHeaderValue, ",");
				while(stringTokenizer.hasMoreTokens()) {
					serverHeaderList.add(stringTokenizer.nextToken());
				}
				ServerHeader serverHeader = sipApplicationDispatcher.getSipFactory().getHeaderFactory().createServerHeader(serverHeaderList);
				((MessageFactoryExt)sipApplicationDispatcher.getSipFactory().getMessageFactory()).setDefaultServerHeader(serverHeader);
			}
			String userAgent = sipStackProperties.getProperty(USER_AGENT_HEADER);
			if(userAgent != null) {
				List<String> userAgentList = new ArrayList<String>();
				StringTokenizer stringTokenizer = new StringTokenizer(userAgent, ",");
				while(stringTokenizer.hasMoreTokens()) {
					userAgentList.add(stringTokenizer.nextToken());
				}
				UserAgentHeader userAgentHeader = sipApplicationDispatcher.getSipFactory().getHeaderFactory().createUserAgentHeader(userAgentList);
				((MessageFactoryExt)sipApplicationDispatcher.getSipFactory().getMessageFactory()).setDefaultUserAgentHeader(userAgentHeader);
			}			
			if(balancers != null) {
				if(sipStackProperties.get(LoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME) == null) {
					sipStackProperties.put(LoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME, LoadBalancerHeartBeatingServiceImpl.class.getCanonicalName());
				}
				if(sipStackProperties.get(LoadBalancerHeartBeatingService.BALANCERS) == null) {
					sipStackProperties.put(LoadBalancerHeartBeatingService.BALANCERS, balancers);
				}
			}		
			String replicationStrategyString = sipStackProperties.getProperty(ClusteredSipStack.REPLICATION_STRATEGY_PROPERTY);
			if(replicationStrategyString == null) {				
				replicationStrategyString = ReplicationStrategy.ConfirmedDialog.toString();
			}			
			boolean replicateApplicationData = false;
			if(replicationStrategyString.equals(ReplicationStrategy.EarlyDialog.toString())) {
				replicateApplicationData = true;
			}			
			if(replicationStrategyString != null) {
				replicationStrategy = ReplicationStrategy.valueOf(replicationStrategyString);
			}
			sipStackProperties.put(ClusteredSipStack.REPLICATION_STRATEGY_PROPERTY, replicationStrategyString);
			sipStackProperties.put(ClusteredSipStack.REPLICATE_APPLICATION_DATA, Boolean.valueOf(replicateApplicationData).toString());
			if(logger.isInfoEnabled()) {
				logger.info("Mobicents Sip Servlets sip stack properties : " + sipStackProperties);
			}
			// Create SipStack object
			sipStack = sipApplicationDispatcher.getSipFactory().getJainSipFactory().createSipStack(sipStackProperties);		
			LoadBalancerHeartBeatingService loadBalancerHeartBeatingService = null;
			if(sipStack instanceof ClusteredSipStack) {
				loadBalancerHeartBeatingService = ((ClusteredSipStack) sipStack).getLoadBalancerHeartBeatingService();
				if ((this.container != null) && (this.container instanceof Engine) && ((Engine)container).getJvmRoute() != null) {
					final String jvmRoute = ((Engine)container).getJvmRoute();
					if(jvmRoute != null) {
						loadBalancerHeartBeatingService.setJvmRoute(jvmRoute);
						setJvmRoute(jvmRoute);
					}
				}
			}
			if(sipApplicationDispatcher != null && loadBalancerHeartBeatingService != null && sipApplicationDispatcher instanceof LoadBalancerHeartBeatingListener) {
				loadBalancerHeartBeatingService.addLoadBalancerHeartBeatingListener((LoadBalancerHeartBeatingListener)sipApplicationDispatcher);
			}
			// for nist sip stack set the DNS Address resolver allowing to make DNS SRV lookups
			if(sipStack instanceof SipStackExt && addressResolverClass != null && addressResolverClass.trim().length() > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("Sip Stack " + sipStack.getStackName() +" will be using " + addressResolverClass + " as AddressResolver");
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
				if(logger.isInfoEnabled()) {
					logger.info("no AddressResolver will be used since none has been specified.");
				}
			}			
			if(logger.isInfoEnabled()) {
				logger.info("SIP stack initialized");
			}
		} catch (Exception ex) {			
			throw new LifecycleException("A problem occured while initializing the SIP Stack", ex);
		}
	}	

	@Override
	public void stop() throws LifecycleException {

		// Tomcat specific unloading case
		// Issue 1411 http://code.google.com/p/mobicents/issues/detail?id=1411
		// Sip Connectors should be removed after removing all Sip Servlets to allow them to send BYE to terminate cleanly
		synchronized (connectors) {
			for (Connector connector : connectors) {
				MobicentsExtendedListeningPoint extendedListeningPoint = null;
				if (connector.getProtocolHandler() instanceof SipProtocolHandler) {
					extendedListeningPoint = (MobicentsExtendedListeningPoint)
						((SipProtocolHandler)connector.getProtocolHandler()).getAttribute(ExtendedListeningPoint.class.getSimpleName());
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
		super.stop();
		if(logger.isDebugEnabled()) {
			logger.debug("SIP Standard Service Stopped.");
		}
//		setState(LifecycleState.STOPPING);		
	}
	
	/**
	 * Retrieve the sip application dispatcher class name
	 * @return the sip application dispatcher class name
	 */
	public String getSipApplicationDispatcherClassName() {
		return sipApplicationDispatcherClassName;
	}

	/**
	 * Set the sip application dispatcher class name
	 * @param sipApplicationDispatcherClassName the sip application dispatcher class name to be set
	 */
	public void setSipApplicationDispatcherClassName(String sipApplicationDispatcherName) {
		this.sipApplicationDispatcherClassName = sipApplicationDispatcherName;
	}

	/**
	 * @return the sipApplicationDispatcher
	 */
	public SipApplicationDispatcher getSipApplicationDispatcher() {
		return sipApplicationDispatcher;
	}

	/**
	 * @param sipApplicationDispatcher the sipApplicationDispatcher to set
	 */
	public void setSipApplicationDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {
		this.sipApplicationDispatcher = sipApplicationDispatcher;
	}

	/**
	 * @return the darConfigurationFileLocation
	 */
	public String getDarConfigurationFileLocation() {
		return darConfigurationFileLocation;
	}

	/**
	 * @param darConfigurationFileLocation the darConfigurationFileLocation to set
	 */
	public void setDarConfigurationFileLocation(String darConfigurationFileLocation) {
		this.darConfigurationFileLocation = darConfigurationFileLocation;
	}
	
	/**
	 * Message queue size. If the number of pending requests exceeds this number they are rejected.
	 * 
	 * @return
	 */
	public int getSipMessageQueueSize() {
		return sipMessageQueueSize;
	}

	/**
	 * Message queue size. If the number of pending requests exceeds this number they are rejected.
	 * 
	 * @return
	 */
	public void setSipMessageQueueSize(int sipMessageQueueSize) {
		this.sipMessageQueueSize = sipMessageQueueSize;
	}

	/**
	 * ConcurrencyControl control mode is SipSession, AppSession or None
	 * Specifies the isolation level of concurrently executing requests.
	 * 
	 * @return
	 */
	public String getConcurrencyControlMode() {
		return concurrencyControlMode;
	}

	/**
	 * ConcurrencyControl control mode is SipSession, AppSession or None
	 * Specifies the isolation level of concurrently executing requests.
	 * 
	 * @return
	 */
	public void setConcurrencyControlMode(String concurrencyControlMode) {
		this.concurrencyControlMode = concurrencyControlMode;
	}


	/**
	 * @param memoryThreshold the memoryThreshold to set
	 */
	public void setMemoryThreshold(int memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}


	/**
	 * @return the memoryThreshold
	 */
	public int getMemoryThreshold() {
		return memoryThreshold;
	}

	/**
	 * @param skipStatistics the skipStatistics to set
	 */
	public void setGatherStatistics(boolean skipStatistics) {
		this.gatherStatistics = skipStatistics;
		if(logger.isInfoEnabled()) {
			logger.info("Gathering Statistics set to " + skipStatistics);
		}
	}

	/**
	 * @return the skipStatistics
	 */
	public boolean isGatherStatistics() {
		return gatherStatistics;
	}
	
	/**
	 * PRESENT TO ACCOMODATE JOPR. NEED TO FILE A BUG ON THIS
	 * @return the skipStatistics
	 */
	public boolean getGatherStatistics() {
		return gatherStatistics;
	}

	/**
	 * @param backToNormalPercentageOfMemoryUsed the backToNormalPercentageOfMemoryUsed to set
	 */
	public void setBackToNormalMemoryThreshold(
			int backToNormalMemoryThreshold) {
		this.backToNormalMemoryThreshold = backToNormalMemoryThreshold;
	}

	/**
	 * @return the backToNormalPercentageOfMemoryUsed
	 */
	public int getBackToNormalMemoryThreshold() {
		return backToNormalMemoryThreshold;
	}

	/**
	 * @param backToNormalQueueSize the backToNormalQueueSize to set
	 */
	public void setBackToNormalSipMessageQueueSize(int backToNormalSipMessageQueueSize) {
		this.backToNormalSipMessageQueueSize = backToNormalSipMessageQueueSize;
	}

	/**
	 * @return the backToNormalQueueSize
	 */
	public int getBackToNormalSipMessageQueueSize() {
		return backToNormalSipMessageQueueSize;
	}
	

	/**
	 * @param congestionControlPolicy the congestionControlPolicy to set
	 */
	public void setCongestionControlPolicy(String congestionControlPolicy) {
		this.congestionControlPolicy = congestionControlPolicy;
	}


	/**
	 * @return the congestionControlPolicy
	 */
	public String getCongestionControlPolicy() {
		return congestionControlPolicy;
	}


	/**
	 * @param congestionControlCheckingInterval the congestionControlCheckingInterval to set
	 */
	public void setCongestionControlCheckingInterval(
			long congestionControlCheckingInterval) {
		this.congestionControlCheckingInterval = congestionControlCheckingInterval;
	}


	/**
	 * @return the congestionControlCheckingInterval
	 */
	public long getCongestionControlCheckingInterval() {
		return congestionControlCheckingInterval;
	}


	public String getAdditionalParameterableHeaders() {
		return additionalParameterableHeaders;
	}


	public void setAdditionalParameterableHeaders(
			String additionalParameterableHeaders) {
		this.additionalParameterableHeaders = additionalParameterableHeaders;
		String[] headers = additionalParameterableHeaders.split(",");
		for(String header : headers) {
			if(header != null && header.length()>0) {
				JainSipUtils.PARAMETERABLE_HEADER_NAMES.add(header);
			}
		}
	}


	/**
	 * @return the bypassResponseExecutor
	 */
	public boolean isBypassResponseExecutor() {
		return bypassResponseExecutor;
	}


	/**
	 * @param bypassResponseExecutor the bypassResponseExecutor to set
	 */
	public void setBypassResponseExecutor(boolean bypassResponseExecutor) {
		this.bypassResponseExecutor = bypassResponseExecutor;
	}


	/**
	 * @return the bypassRequestExecutor
	 */
	public boolean isBypassRequestExecutor() {
		return bypassRequestExecutor;
	}


	/**
	 * @param bypassRequestExecutor the bypassRequestExecutor to set
	 */
	public void setBypassRequestExecutor(boolean bypassRequestExecutor) {
		this.bypassRequestExecutor = bypassRequestExecutor;
	}

	public boolean isMd5ContactUserPart() {
		return md5ContactUserPart;
	}


	public void setMd5ContactUserPart(boolean md5ContactUserPart) {
		this.md5ContactUserPart = md5ContactUserPart;
	}


	/**
	 * @param usePrettyEncoding the usePrettyEncoding to set
	 */
	public void setUsePrettyEncoding(boolean usePrettyEncoding) {
		this.usePrettyEncoding = usePrettyEncoding;
	}

	/**
	 * @return the usePrettyEncoding
	 */
	public boolean isUsePrettyEncoding() {
		return usePrettyEncoding;
	}	
	
	/**
	 * @param sipPathName the sipPathName to set
	 */
	public void setSipPathName(String sipPathName) {
		this.sipPathName = sipPathName;
	}

	/**
	 * @return the sipPathName
	 */
	public String getSipPathName() {
		return sipPathName;
	}


	/**
	 * @param baseTimerInterval the baseTimerInterval to set
	 */
	public void setBaseTimerInterval(int baseTimerInterval) {
		this.baseTimerInterval = baseTimerInterval;
	}


	/**
	 * @return the baseTimerInterval
	 */
	public int getBaseTimerInterval() {
		return baseTimerInterval;
	}


	public OutboundProxy getOutboundProxy() {
		return outboundProxy;
	}


	public void setOutboundProxy(String outboundProxy) {
		if(outboundProxy != null) {
			this.outboundProxy =  new OutboundProxy(outboundProxy);
		}
	}
	
	public int getDispatcherThreadPoolSize() {
		return dispatcherThreadPoolSize;
	}


	public void setDispatcherThreadPoolSize(int dispatcherThreadPoolSize) {
		this.dispatcherThreadPoolSize = dispatcherThreadPoolSize;
	}

	public int getCanceledTimerTasksPurgePeriod() {
		return canceledTimerTasksPurgePeriod;
	}
	
	public void setCanceledTimerTasksPurgePeriod(int purgePeriod) {
		this.canceledTimerTasksPurgePeriod = purgePeriod;
	}
	
	/**
	 * @deprecated
	 * @param balancers the balancers to set
	 */	
	public void setBalancers(String balancers) {
		this.balancers = balancers;
	}
	
	public boolean addSipConnector(SipConnector sipConnector) throws Exception {
		if(sipConnector == null) {
			throw new IllegalArgumentException("The sip connector passed is null");
		}
		Connector connectorToAdd = findSipConnector(sipConnector.getIpAddress(), sipConnector.getPort(),
				sipConnector.getTransport());
		if(connectorToAdd == null) {
			Connector connector = new Connector(
					SipProtocolHandler.class.getName());
			SipProtocolHandler sipProtocolHandler = (SipProtocolHandler) connector
					.getProtocolHandler();
			sipProtocolHandler.setSipConnector(sipConnector);		
			sipProtocolHandler.setSipStack(sipStack);
			connector.setService(this);
//TODO			connector.setContainer(container);
			connector.init();		
			addConnector(connector);			
			MobicentsExtendedListeningPoint extendedListeningPoint = (MobicentsExtendedListeningPoint)
				sipProtocolHandler.getAttribute(ExtendedListeningPoint.class.getSimpleName());
			if(extendedListeningPoint != null) {
				try {
					extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
					sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
				} catch (TooManyListenersException e) {
					logger.error("Connector.initialize", e);
					removeConnector(connector);
					return false;
				}			
			}
			if(!sipProtocolHandler.isStarted()) {
				if(logger.isDebugEnabled()) {
					logger.debug("Sip Connector couldn't be started, removing it automatically");
				}
				removeConnector(connector);
			}
			return sipProtocolHandler.isStarted();
		}
		return false;
	}
	
	public boolean removeSipConnector(String ipAddress, int port, String transport) throws Exception {
		Connector connectorToRemove = findSipConnector(ipAddress, port,
				transport);
		if(connectorToRemove != null) {
			removeConnector(connectorToRemove);
			return true;
		}
		return false;
	}


	/**
	 * Find a sip Connector by it's ip address, port and transport
	 * @param ipAddress ip address of the connector to find
	 * @param port port of the connector to find
	 * @param transport transport of the connector to find
	 * @return the found sip connector or null if noting found 
	 */
	private Connector findSipConnector(String ipAddress, int port,
			String transport) {
		Connector connectorToRemove = null;
		for (Connector connector : connectors) {
			final ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if(protocolHandler instanceof SipProtocolHandler) {
				final SipProtocolHandler sipProtocolHandler = (SipProtocolHandler) protocolHandler;
				if(sipProtocolHandler.getIpAddress().equals(ipAddress) && sipProtocolHandler.getPort() == port && sipProtocolHandler.getSignalingTransport().equals(transport)) {
//					connector.destroy();
					connectorToRemove = connector;
					break;
				}
			}
		}
		return connectorToRemove;
	}
	
	public SipConnector findSipConnector(String transport) {
		List<SipConnector> sipConnectors = new ArrayList<SipConnector>();
		for (Connector connector : connectors) {
			final ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if(protocolHandler instanceof SipProtocolHandler) {
				SipConnector sc =  (((SipProtocolHandler)protocolHandler).getSipConnector());
				if(sc.getTransport().equalsIgnoreCase(transport)) return sc;
			}
		}
		return null;
	}
	
	public SipConnector[] findSipConnectors() {
		List<SipConnector> sipConnectors = new ArrayList<SipConnector>();
		for (Connector connector : connectors) {
			final ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if(protocolHandler instanceof SipProtocolHandler) {
				sipConnectors.add(((SipProtocolHandler)protocolHandler).getSipConnector());
			}
		}
		return sipConnectors.toArray(new SipConnector[sipConnectors.size()]);
	}
	
	/**
	 * This method simply makes the HTTP and SSL ports avaialble everywhere in the JVM in order jsip ha to read them for
	 * balancer description purposes. There is no other good way to communicate the properies to jsip ha without adding
	 * more dependencies.
	 */
	public void initializeSystemPortProperties() {
		for (Connector connector : connectors) {
			if(connector.getProtocol().contains("HTTP")) {
				if(connector.getSecure()) {
					System.setProperty("org.mobicents.properties.sslPort", Integer.toString(connector.getPort()));
				} else {
					System.setProperty("org.mobicents.properties.httpPort", Integer.toString(connector.getPort()));
				}
			}
		}
	}
	
	 protected ObjectName createSipConnectorObjectName(Connector connector, String domain, String type)
	     throws MalformedObjectNameException {
		 String encodedAddr = null;
		 if (connector.getProperty("address") != null) {
		     encodedAddr = URLEncoder.encode(connector.getProperty("address").toString());
		 }
		 String addSuffix = (connector.getProperty("address") == null) ? "" : ",address="
		         + encodedAddr;
		 ObjectName _oname = new ObjectName(domain + ":type=" + type + ",port="
		         + connector.getPort() + ",transport=" + connector.getProperty("transport") + addSuffix);
		 return _oname;
	}


	/**
	 * @param t2Interval the t2Interval to set
	 */
	public void setT2Interval(int t2Interval) {
		this.t2Interval = t2Interval;
	}


	/**
	 * @return the t2Interval
	 */
	public int getT2Interval() {
		return t2Interval;
	}


	/**
	 * @param t4Interval the t4Interval to set
	 */
	public void setT4Interval(int t4Interval) {
		this.t4Interval = t4Interval;
	}


	/**
	 * @return the t4Interval
	 */
	public int getT4Interval() {
		return t4Interval;
	}


	/**
	 * @param timerDInterval the timerDInterval to set
	 */
	public void setTimerDInterval(int timerDInterval) {
		this.timerDInterval = timerDInterval;
	}


	/**
	 * @return the timerDInterval
	 */
	public int getTimerDInterval() {
		return timerDInterval;
	}
	
	/**
	 * @param sipStackPropertiesFile the sipStackPropertiesFile to set
	 */
	public void setSipStackPropertiesFile(String sipStackPropertiesFile) {
		sipStackPropertiesFileLocation =  sipStackPropertiesFile;
	}

	/**
	 * @return the sipStackProperties
	 */
	public Properties getSipStackProperties() {
		return sipStackProperties;
	}
	
	/**
	 * @param sipStackProperties the sipStackProperties to set
	 */
	public void setSipStackProperties(Properties sipStackProperties) {
		this.sipStackProperties = sipStackProperties;
	}

	/**
	 * @return the sipStackPropertiesFile
	 */
	public String getSipStackPropertiesFile() {
		return sipStackPropertiesFileLocation;
	}
	
	/**
	 * @param dnsAddressResolverClass the dnsAddressResolverClass to set
	 */
	@Deprecated
	public void setAddressResolverClass(String dnsAddressResolverClass) {
		this.addressResolverClass = dnsAddressResolverClass;
	}

	/**
	 * @return the dnsAddressResolverClass
	 */
	@Deprecated
	public String getAddressResolverClass() {
		return addressResolverClass;
	}
	
	/**
	 * Whether we check for pending requests and return 491 response if there are any
	 * 
	 * @return the flag value
	 */
	public boolean isDialogPendingRequestChecking() {
		return dialogPendingRequestChecking;
	}

	/**
	 *
	 * Whether we check for pending requests and return 491 response if there are any
	 *
	 * @param dialogPendingRequestChecking
	 */
	public void setDialogPendingRequestChecking(boolean dialogPendingRequestChecking) {
		this.dialogPendingRequestChecking = dialogPendingRequestChecking;
	}


	public boolean isHttpFollowsSip() {
		return httpFollowsSip;
	}


	public void setHttpFollowsSip(boolean httpFollowsSip) {
		this.httpFollowsSip = httpFollowsSip;
	}	

	/**
	 * @return the sipStack
	 */
	public SipStack getSipStack() {
		return sipStack;
	}

	/**
	 * @param dnsServerLocatorClass the dnsServerLocatorClass to set
	 */
	public void setDnsServerLocatorClass(String dnsServerLocatorClass) {
		this.dnsServerLocatorClass = dnsServerLocatorClass;
	}

	/**
	 * @return the dnsServerLocatorClass
	 */
	public String getDnsServerLocatorClass() {
		return dnsServerLocatorClass;
	}
	
	/**
	 * Returns first the catalina.base if it is defined then the catalina.home if it is defined
	 * then the current dir if none is specified
	 */
	protected String getCatalinaBase() {
		String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null) {
        	catalinaBase = System.getProperty("catalina.home");
        }
        if(catalinaBase == null) {
        	catalinaBase = ".";
        }
		return catalinaBase;
	}
	
	public ReplicationStrategy getReplicationStrategy() {
		return replicationStrategy;
	}

	@Override
	public String getMobicentsSipServletMessageFactoryClassName() {
		return mobicentsSipServletMessageFactoryClassName;
	}

	@Override
	public void setMobicentsSipServletMessageFactoryClassName(
			String mobicentsSipServletMessageFactoryClassName) {
		this.mobicentsSipServletMessageFactoryClassName = mobicentsSipServletMessageFactoryClassName;
	}
	
	public void sendHeartBeat(String localAddress, int localPort, String transport, String remoteIpAddress, int remotePort) throws IOException {
		MobicentsExtendedListeningPoint extendedListeningPoint = sipApplicationDispatcher.getSipNetworkInterfaceManager().findMatchingListeningPoint(localAddress, localPort, transport);
		if(extendedListeningPoint != null) {
			extendedListeningPoint.getListeningPoint().sendHeartbeat(remoteIpAddress, remotePort);
		}		
	}
	
    public boolean setKeepAliveTimeout(SipConnector sipConnector, String clientAddress, int clientPort, long timeout) {
        SIPTransactionStack sipStack = ((SIPTransactionStack) sipApplicationDispatcher.getSipStack());

        return sipStack.setKeepAliveTimeout(sipConnector.getIpAddress(), sipConnector.getPort(), sipConnector.getTransport(),
                clientAddress, clientPort, timeout);
    }

    public void closeReliableConnection(SipConnector sipConnector, String clientAddress, int clientPort) {
        SIPTransactionStack sipStack = ((SIPTransactionStack) sipApplicationDispatcher.getSipStack());

        sipStack.closeReliableConnection(sipConnector.getIpAddress(),sipConnector.getPort(), sipConnector.getTransport(),
                clientAddress, clientPort);
    }
}
