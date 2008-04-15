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


import java.io.File;
import java.util.TooManyListenersException;

import javax.sip.SipProvider;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

/**
 * Sip Servlet implementation of the <code>Service</code> interface.  
 * This class inherits from the Tomcat StandardService. It adds a SipApplicationDispatcher
 * that will be listen for sip messages received by the sip stacks started by 
 * the sip connectors associated with this context.
 * This has one attribute which is the sipApplicationDispatcherClassName allowing one
 * to specify the class name of the sipApplicationDispacther to easily replace
 * the default sipApplicationDispatcher with a custom one.
 *
 * @author Jean Deruelle
 */
public class SipStandardService extends StandardService implements SipService {
	//the logger
	private static Log logger = LogFactory.getLog(SipStandardService.class);
	//the sip application dispatcher class name defined in the server.xml
	private String sipApplicationDispatcherClassName;
	//instatiated class from the sipApplicationDispatcherClassName of the sip application dispatcher 
	private SipApplicationDispatcher sipApplicationDispatcher;
	//the sip application router class name defined in the server.xml
	private String sipApplicationRouterClassName;
	//this should be made available to the application router as a system prop
	private String darConfigurationFileLocation;
	//
	private boolean connectorsStartedExternally = false;
	/**
	 * 
	 */
	public SipStandardService() {
		super();
	}
	
	@Override
	public void init() {			
		super.init();		
	}
	
	@Override
	public void addConnector(Connector connector) {
		SipProvider sipProvider = (SipProvider)
		connector.getProtocolHandler().getAttribute("sipProvider");
		if(sipProvider != null) {
			try {
				sipProvider.addSipListener(sipApplicationDispatcher);
				sipApplicationDispatcher.addSipProvider(sipProvider);
			} catch (TooManyListenersException e) {
				logger.error("Connector.initialize", e);
			}			
		}
		super.addConnector(connector);
	}
	
	@Override
	public void removeConnector(Connector connector) {
		SipProvider sipProvider = (SipProvider)
		connector.getProtocolHandler().getAttribute("sipProvider");
		if(sipProvider != null) {
			sipProvider.removeSipListener(sipApplicationDispatcher);
			sipApplicationDispatcher.removeSipProvider(sipProvider);
		}
		super.removeConnector(connector);
	}
	
	@Override
	public void initialize() throws LifecycleException {
		//load the sip application disptacher from the class name specified in the server.xml file
		//and initializes it
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
		if(darConfigurationFileLocation != null && !darConfigurationFileLocation.startsWith("file:///")) {
			darConfigurationFileLocation = "file:///" + System.getProperty("catalina.base").replace(File.separatorChar, '/') + "/" + darConfigurationFileLocation;
 		}
		System.setProperty("javax.servlet.sip.dar", darConfigurationFileLocation);			
		super.initialize();
		sipApplicationDispatcher.setDomain(this.domain);
		sipApplicationDispatcher.init(sipApplicationRouterClassName);
	}
	
	@Override
	public void start() throws LifecycleException {
		super.start();		
		synchronized (connectors) {
			for (int i = 0; i < connectors.length; i++) {
				//Jboss sepcific loading case
				Boolean isSipConnector = (Boolean)
					connectors[i].getProtocolHandler().getAttribute("isSipConnector");				
				if(isSipConnector != null && isSipConnector) {
					logger.info("Attaching the sip application dispatcher " +
							"as a sip listener to connector listening on port " + 
							connectors[i].getPort());
					connectors[i].getProtocolHandler().setAttribute("SipApplicationDispatcher", sipApplicationDispatcher);
					connectorsStartedExternally = true;
				} 
				//Tomcat specific loading case
				SipProvider sipProvider = (SipProvider)
					connectors[i].getProtocolHandler().getAttribute("sipProvider");
				if(sipProvider != null) {
					try {
						sipProvider.addSipListener(sipApplicationDispatcher);
						sipApplicationDispatcher.addSipProvider(sipProvider);
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
	}

	@Override
	public void stop() throws LifecycleException {
		//Tomcat specific unloading case
		synchronized (connectors) {
			for (int i = 0; i < connectors.length; i++) {
				SipProvider sipProvider = (SipProvider)
					connectors[i].getProtocolHandler().getAttribute("sipProvider");
				if(sipProvider != null) {					
					sipProvider.removeSipListener(sipApplicationDispatcher);
					sipApplicationDispatcher.removeSipProvider(sipProvider);
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

	/**
	 * @return the sipApplicationRouterClassName
	 */
	public String getSipApplicationRouterClassName() {
		return sipApplicationRouterClassName;
	}

	/**
	 * @param sipApplicationRouterClassName the sipApplicationRouterClassName to set
	 */
	public void setSipApplicationRouterClassName(
			String sipApplicationRouterClassName) {
		this.sipApplicationRouterClassName = sipApplicationRouterClassName;
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
}
