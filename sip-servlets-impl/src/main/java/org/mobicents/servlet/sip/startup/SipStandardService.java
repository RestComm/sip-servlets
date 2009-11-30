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


import gov.nist.javax.sip.SipStackExt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import javax.sip.SipStack;

import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.coyote.ProtocolHandler;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.CongestionControlPolicy;
import org.mobicents.servlet.sip.core.DNSAddressResolver;
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
	protected int sipMessageQueueSize = 1500;
	protected int memoryThreshold = 90;
	protected String outboundProxy;
	protected long congestionControlCheckingInterval = 30000;
	// base timer interval for jain sip tx 
	private int baseTimerInterval = 500;
	
	protected int dispatcherThreadPoolSize = 4;
	
	protected String concurrencyControlMode = ConcurrencyControlMode.None.toString();
	protected String congestionControlPolicy = CongestionControlPolicy.ErrorResponse.toString();
	protected String additionalParameterableHeaders;
	protected boolean bypassResponseExecutor;
	protected boolean bypassRequestExecutor;
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
		}		
		super.addConnector(connector);
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
		sipApplicationDispatcher.setDomain(this.domain);
		if(baseTimerInterval < 1) {
			throw new LifecycleException("It's forbidden to set the Base Timer Interval to a non positive value");		
		}
		sipApplicationDispatcher.setBaseTimerInterval(baseTimerInterval);
		sipApplicationDispatcher.setMemoryThreshold(getMemoryThreshold());
		sipApplicationDispatcher.setCongestionControlCheckingInterval(getCongestionControlCheckingInterval());
		sipApplicationDispatcher.setCongestionControlPolicyByName(getCongestionControlPolicy());
		sipApplicationDispatcher.setQueueSize(getSipMessageQueueSize());
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
					if(sipStack instanceof SipStackExt) {
						if(logger.isDebugEnabled()) {
							logger.debug(sipStack.getStackName() +" will be using DNS SRV lookups as AddressResolver");
						}
						((SipStackExt) sipStack).setAddressResolver(new DNSAddressResolver(sipApplicationDispatcher));
					}
					try {
						extendedListeningPoint.getSipProvider().addSipListener(sipApplicationDispatcher);
						sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
						connectorsStartedExternally = false;											
					} catch (TooManyListenersException e) {					
						throw new LifecycleException(e);
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
		//Tomcat specific unloading case
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
		if(!connectorsStartedExternally) {
			sipApplicationDispatcher.stop();
		}	
		super.stop();
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
	
	public void addSipConnector(SipConnector sipConnector) throws Exception {
		Connector connector = new Connector(
				SipProtocolHandler.class.getName());
		SipProtocolHandler protocolHandler = (SipProtocolHandler) connector
				.getProtocolHandler();
		protocolHandler.setSipConnector(sipConnector);		
		connector.setService(this);
		connector.init();
		addConnector(connector);	
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
	}
	
	public void removeSipConnector(String ipAddress, int port, String transport) throws Exception {
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
		if(connectorToRemove != null) {
			removeConnector(connectorToRemove);
		}
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
}
