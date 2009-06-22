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
import java.util.TooManyListenersException;

import javax.sip.SipStack;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
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
	private static transient Logger logger = Logger.getLogger(SipStandardService.class);
	/**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.mobicents.servlet.sip.startup.SipStandardService/1.0";
	//the sip application dispatcher class name defined in the server.xml
	protected String sipApplicationDispatcherClassName;
	//instatiated class from the sipApplicationDispatcherClassName of the sip application dispatcher 
	protected SipApplicationDispatcher sipApplicationDispatcher;
	protected int sipMessageQueueSize = 1500;
	protected int memoryThreshold = 90;
	protected long congestionControlCheckingInterval = 30000;
	
	protected String concurrencyControlMode = ConcurrencyControlMode.SipSession.toString();
	protected String congestionControlPolicy = CongestionControlPolicy.ErrorResponse.toString();
	protected String additionalParameterableHeaders;
	//the sip application router class name defined in the server.xml
//	private String sipApplicationRouterClassName;
	//this should be made available to the application router as a system prop
	protected String darConfigurationFileLocation;
	//
	protected boolean connectorsStartedExternally = false;
	
	@Override
    public String getInfo() {
        return (info);
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
		if(darConfigurationFileLocation != null) {
			if(!darConfigurationFileLocation.startsWith("file:///")) {
				darConfigurationFileLocation = "file:///" + System.getProperty("catalina.home").replace(File.separatorChar, '/') + "/" + darConfigurationFileLocation;
			}
			System.setProperty("javax.servlet.sip.dar", darConfigurationFileLocation);
 		}		
		super.initialize();
		sipApplicationDispatcher.setDomain(this.domain);
		
		sipApplicationDispatcher.setMemoryThreshold(getMemoryThreshold());
		sipApplicationDispatcher.setCongestionControlCheckingInterval(getCongestionControlCheckingInterval());
		sipApplicationDispatcher.setCongestionControlPolicyByName(getCongestionControlPolicy());
		sipApplicationDispatcher.setQueueSize(getSipMessageQueueSize());
		sipApplicationDispatcher.setConcurrencyControlMode(ConcurrencyControlMode.valueOf(getConcurrencyControlMode()));

		sipApplicationDispatcher.init();
	}
	
	@Override
	public void start() throws LifecycleException {
		super.start();		
		synchronized (connectors) {
			for (Connector connector : connectors) {
				//Jboss sepcific loading case
				Boolean isSipConnector = (Boolean)
					connector.getProtocolHandler().getAttribute("isSipConnector");				
				if(isSipConnector != null && isSipConnector) {
					if(logger.isDebugEnabled()) {
						logger.debug("Attaching the sip application dispatcher " +
							"as a sip listener to connector listening on port " + 
							connector.getPort());
					}
					connector.getProtocolHandler().setAttribute(SipApplicationDispatcher.class.getSimpleName(), sipApplicationDispatcher);
					connectorsStartedExternally = true;
				} 
				//Tomcat specific loading case
				ExtendedListeningPoint extendedListeningPoint = (ExtendedListeningPoint)
					connector.getProtocolHandler().getAttribute(ExtendedListeningPoint.class.getSimpleName());
				SipStack sipStack = (SipStack)
					connector.getProtocolHandler().getAttribute(SipStack.class.getSimpleName());
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
				JainSipUtils.parameterableHeadersNames.add(header);
			}
		}
	}

}
