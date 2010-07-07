/*
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
package org.mobicents.servlet.sip.startup;


import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.SipStackExt;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sip.SipStack;

import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.coyote.ProtocolHandler;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.CongestionControlPolicy;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

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
public class SipStandardService extends StandardService implements SipService {
	//the logger
	private static final Logger logger = Logger.getLogger(SipStandardService.class);
	private static final String DEFAULT_SIP_PATH_NAME = "gov.nist";
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
	protected String outboundProxy;
	protected long congestionControlCheckingInterval = 30000;
	// base timer interval for jain sip tx 
	private int baseTimerInterval = 500;
	private int t2Interval = 4000;
	private int t4Interval = 5000;
	private int timerDInterval = 32000;
	
	protected int dispatcherThreadPoolSize = 4;
	
	protected String concurrencyControlMode = ConcurrencyControlMode.None.toString();
	protected String congestionControlPolicy = CongestionControlPolicy.ErrorResponse.toString();
	protected String additionalParameterableHeaders;
	protected boolean bypassResponseExecutor = true;
	protected boolean bypassRequestExecutor = true;
	//the sip application router class name defined in the server.xml
//	private String sipApplicationRouterClassName;
	//this should be made available to the application router as a system prop
	protected String darConfigurationFileLocation;
	//
	protected boolean connectorsStartedExternally = false;
	
	/**
	 * the sip stack path name. Since the sip factory is per classloader it should be set here for all underlying stacks
	 */
	private String sipPathName;
	/*
	 * use Pretty Encoding
	 */
	private boolean usePrettyEncoding = true;
	
	//the balancers to send heartbeat to and our health info
	@Deprecated
	private String balancers;
	
	@Override
    public String getInfo() {
        return (INFO);
    }

	
	@Override
	public void addConnector(Connector connector) {
		ExtendedListeningPoint extendedListeningPoint = (ExtendedListeningPoint)
			connector.getProtocolHandler().getAttribute(ExtendedListeningPoint.class.getSimpleName());
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
			protocolHandler.setAttribute(SipApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);
			if(balancers != null) {
				protocolHandler.setAttribute("balancers", balancers);
			}
			registerSipConnector(connector);
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
		    connector.setController(objectName);
		} catch (Exception e) {
		    logger.error( "Error registering connector ", e);
		}
		if(logger.isDebugEnabled())
		    logger.debug("Creating name for connector " + oname);
	}
	
	@Override
	public void removeConnector(Connector connector) {
		ExtendedListeningPoint extendedListeningPoint = (ExtendedListeningPoint)
			connector.getProtocolHandler().getAttribute(ExtendedListeningPoint.class.getSimpleName());
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
		SipFactories.initialize(sipPathName, usePrettyEncoding);
		if(darConfigurationFileLocation != null) {
			if(!darConfigurationFileLocation.startsWith("file:///")) {
				darConfigurationFileLocation = "file:///" + System.getProperty("catalina.home").replace(File.separatorChar, '/') + "/" + darConfigurationFileLocation;
			}
			System.setProperty("javax.servlet.sip.dar", darConfigurationFileLocation);
 		}		
		super.initialize();
		sipApplicationDispatcher.setDomain(this.getName());
		if(baseTimerInterval < 1) {
			throw new LifecycleException("It's forbidden to set the Base Timer Interval to a non positive value");		
		}
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
		sipApplicationDispatcher.init();
	}
	
	@Override
	public void start() throws LifecycleException {
		super.start();		
		synchronized (connectors) {
			for (Connector connector : connectors) {
				final ProtocolHandler protocolHandler = connector.getProtocolHandler();
				//Jboss sepcific loading case
				Boolean isSipConnector = (Boolean) protocolHandler.getAttribute(SipProtocolHandler.IS_SIP_CONNECTOR);				
				if(isSipConnector != null && isSipConnector) {
					if(logger.isDebugEnabled()) {
						logger.debug("Attaching the sip application dispatcher " +
							"as a sip listener to connector listening on port " + 
							connector.getPort());
					}
					protocolHandler.setAttribute(SipApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);
					if(balancers != null) {
						protocolHandler.setAttribute("balancers", balancers);
					}
					if ((this.container != null) && (this.container instanceof Engine) && ((Engine)container).getJvmRoute() != null) {			            
						protocolHandler.setAttribute("jvmRoute", ((Engine)container).getJvmRoute());
					}
					connectorsStartedExternally = true;
				} 
				//Tomcat specific loading case
				ExtendedListeningPoint extendedListeningPoint = (ExtendedListeningPoint)
					protocolHandler.getAttribute(ExtendedListeningPoint.class.getSimpleName());
				SipStack sipStack = (SipStack)
					protocolHandler.getAttribute(SipStack.class.getSimpleName());
				if(extendedListeningPoint != null && sipStack != null) {
					// for nist sip stack set the DNS Address resolver allowing to make DNS SRV lookups
					String dnsAddressResolverClass = ((SipProtocolHandler)protocolHandler).getAddressResolverClass();
					if(sipStack instanceof SipStackExt && dnsAddressResolverClass != null && dnsAddressResolverClass.trim().length() > 0) {
						if(logger.isDebugEnabled()) {
							logger.debug("Sip Stack " + sipStack.getStackName() +" will be using " + dnsAddressResolverClass + " as AddressResolver");
						}
						try {
							extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
				        	sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
				        	connectorsStartedExternally = false;
				        	//setting up the Address Resolver :
				            // get constructor of AddressResolver in order to instantiate
				            Class[] paramTypes = new Class[1];
				            paramTypes[0] = SipApplicationDispatcher.class;
				            Constructor addressResolverConstructor = Class.forName(dnsAddressResolverClass).getConstructor(
				                    paramTypes);
				            // Wrap properties object in order to pass to constructor of AddressResolver
				            Object[] conArgs = new Object[1];
				            conArgs[0] = sipApplicationDispatcher;
				            // Creates a new instance of AddressResolver Class with the supplied sipApplicationDispatcher.
				            AddressResolver addressResolver = (AddressResolver) addressResolverConstructor.newInstance(conArgs);
				            ((SipStackExt) sipStack).setAddressResolver(addressResolver);				            
				        } catch (Exception e) {
				            throw new LifecycleException("Couldn't set the AddressResolver " + dnsAddressResolverClass, e);
				        }				        
					} else {
						if(logger.isInfoEnabled()) {
							logger.info("no AddressResolver will be used since none has been specified.");
						}
					}	
				}
			}
		}
		if(!connectorsStartedExternally) {
			sipApplicationDispatcher.start();
		}
		
		if(this.getSipMessageQueueSize() <= 0)
			throw new IllegalArgumentException("Message queue size can not be 0 or less");

	}

	@Override
	public void stop() throws LifecycleException {
		if(!connectorsStartedExternally) {
			sipApplicationDispatcher.stop();
		}	
		super.stop();
		//Tomcat specific unloading case
		// Issue 1411 http://code.google.com/p/mobicents/issues/detail?id=1411
		// Sip Connectors should be removed after removing all Sip Servlets to allow them to send BYE to terminate cleanly
		synchronized (connectors) {
			for (Connector connector : connectors) {
				ExtendedListeningPoint extendedListeningPoint = (ExtendedListeningPoint)
					connector.getProtocolHandler().getAttribute(ExtendedListeningPoint.class.getSimpleName());
				if(extendedListeningPoint != null) {					
					extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
					sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(extendedListeningPoint);
				}
			}
		}		
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

//	/**
//	 * @return the sipApplicationRouterClassName
//	 */
//	public String getSipApplicationRouterClassName() {
//		return sipApplicationRouterClassName;
//	}
//
//	/**
//	 * @param sipApplicationRouterClassName the sipApplicationRouterClassName to set
//	 */
//	public void setSipApplicationRouterClassName(
//			String sipApplicationRouterClassName) {
//		this.sipApplicationRouterClassName = sipApplicationRouterClassName;
//	}

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


	public String getOutboundProxy() {
		return outboundProxy;
	}


	public void setOutboundProxy(String outboundProxy) {
		this.outboundProxy = outboundProxy;
	}
	
	public int getDispatcherThreadPoolSize() {
		return dispatcherThreadPoolSize;
	}


	public void setDispatcherThreadPoolSize(int dispatcherThreadPoolSize) {
		this.dispatcherThreadPoolSize = dispatcherThreadPoolSize;
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
			connector.setService(this);
			connector.setContainer(container);
			connector.init();		
			addConnector(connector);			
			ExtendedListeningPoint extendedListeningPoint = (ExtendedListeningPoint)
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
}
