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

package org.mobicents.servlet.sip.core;

import gov.nist.javax.sip.SipListenerExt;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.sip.SipStack;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;

import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.javax.servlet.CongestionControlPolicy;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;

/**
 * 
 * Classes implementing this interface can be used in the SipService Class to
 * be the central point getting the sip messages from the different stacks and
 * dispatching them to sip applications. 
 */
public interface SipApplicationDispatcher extends SipListenerExt {	
	/**
	 * Initialize the sip application dispatcher. <br/>
	 * It will look for the first implementation of an application routerand 
	 * packaged in accordance with the rules specified by the Java SE Service Provider framework.<br/>
	 * It will first look for the javax.servlet.sip.ar.spi.SipApplicationRouterProvider system property 
	 * since it can be used to override loading behavior. 
	 * See JSR 289 Section 15.4.2 Application Router Packaging and Deployment for more information 
	 * 
	 * @throws LifecycleException The Sip Application Router cannot be initialized correctly
	 */
	void init();
	
	/**
	 * Start the sip application dispatcher
	 */
	void start();
	
	/**
	 * Stop the sip application dispatcher
	 */
	void stop();
	
	/**
	 * Add a new sip application to which sip messages can be routed
	 * @param sipApplicationName the sip application logical name
	 * @param sipApplication the sip context representing the application 
	 */
	void addSipApplication(String sipApplicationName, SipContext sipApplication);
	/**
	 * Remove a sip application to which sip messages can be routed
	 * @param sipApplicationName the sip application logical name of the application to remove
	 */
	SipContext removeSipApplication(String sipApplicationName);
	
	/**
	 * Find the sip applications to which sip messages can currently be routed
	 * @return the sip applications to which sip messages can currently be routed
	 */
	Iterator<SipContext> findSipApplications();
	
	/**
	 * Find the sip application to which sip messages can currently be routed by its name
	 * @param applicationName the name of the application
	 * @return the sip application to which sip messages can currently be routed by its name
	 * if it has been find, null otherwise
	 */
	SipContext findSipApplication(String applicationName);
	
	/**
	 * Retrieve the manager for the sip network interfaces for this application dispatcher
	 * @return the manager for the sip network interfaces for this application dispatcher
	 */
	SipNetworkInterfaceManager getSipNetworkInterfaceManager();

	/**
	 * retrieve the sip factory
	 * @return the sip factory
	 */
	MobicentsSipFactory getSipFactory();
	/**
	 * Returns An immutable instance of the java.util.List interface containing 
	 * the SipURI representation of IP addresses which are used by the container to send out the messages.
	 * @return immutable List containing the SipURI representation of IP addresses 
	 */
	List<SipURI> getOutboundInterfaces();
	/**
	 * Add a new hostname to the application dispatcher.
	 * This information is used for the routing algorithm of an incoming Request.
	 * @param hostName the host name
	 */
	void addHostName(String hostName);
	/**
	 * Remove the hostname from the application dispatcher.
	 * This information is used for the routing algorithm of an incoming Request.
	 * @param hostName the host name
	 */
	void removeHostName(String hostName);
	/**
	 * Returns An immutable instance of the java.util.List interface containing
	 * the sip application dispatcher registered host names
	 * @return An immutable instance of the java.util.List interface containing
	 * the sip application dispatcher registered host names
	 */
	Set<String> findHostNames();
		
	/**
	 * 
	 * @param sipServletRequestImpl
	 * @return
	 */
	SipApplicationRouterInfo getNextInterestedApplication(MobicentsSipServletRequest sipServletRequestImpl);
	
	String getDomain();
    
    void setDomain(String domain);
    
    boolean isRouteExternal(RouteHeader routeHeader);
    
    boolean isViaHeaderExternal(ViaHeader viaHeader);
    
    boolean isExternal(String host, int port, String transport);

	SipApplicationRouter getSipApplicationRouter();

	// should be in a seperate HA interface, but this may become deprecated in the future
	void sendSwitchoverInstruction(String fromJvmRoute, String toJvmRoute);
	
	String getApplicationNameFromHash(String hash);
	String getHashFromApplicationName(String appName);
	
	ConcurrencyControlMode getConcurrencyControlMode();
	void setConcurrencyControlMode(ConcurrencyControlMode concurrencyControlMode);
	void setConcurrencyControlModeByName(String concurrencyControlMode);

	int getQueueSize();
	void setQueueSize(int queueSize);
	
	void setMemoryThreshold(int memoryThreshold);
	int getMemoryThreshold();
	
	void setCongestionControlCheckingInterval(long interval);
	long getCongestionControlCheckingInterval();
		
	CongestionControlPolicy getCongestionControlPolicy();
	void setCongestionControlPolicy(CongestionControlPolicy congestionControlPolicy);
	String getCongestionControlPolicyByName();
	void setCongestionControlPolicyByName(String congestionControlPolicy);
	
	int getNumberOfMessagesInQueue();
	double getPercentageOfMemoryUsed();
	
	void setBypassRequestExecutor(boolean bypassRequestExecutor);
	boolean isBypassRequestExecutor();

	void setBypassResponseExecutor(boolean bypassResponseExecutor);
	boolean isBypassResponseExecutor();
	
	void setBaseTimerInterval(int baseTimerInterval);
	int getBaseTimerInterval();
	void setT2Interval(int t2Interval);
	int getT2Interval();
	void setT4Interval(int t4Interval);
	int getT4Interval();
	void setTimerDInterval(int timerDInterval);
	int getTimerDInterval();
	
	String[] getExtensionsSupported();
	String[] getRfcSupported();
		
	void setGatherStatistics(boolean gatherStatistics);	
	boolean isGatherStatistics();
	
	void setBackToNormalMemoryThreshold(
			int backToNormalMemoryThreshold);
	int getBackToNormalMemoryThreshold();
	
	void setBackToNormalQueueSize(int backToNormalQueueSize);
	int getBackToNormalQueueSize();

	ExecutorService getAsynchronousExecutor();

	void setSipStack(SipStack sipStack);
	SipStack getSipStack();

	void setDNSServerLocator(DNSServerLocator dnsServerLocator);
	DNSServerLocator getDNSServerLocator();
	
	String getVersion();
	
	public Map<String, List<? extends SipApplicationRouterInfo>> getApplicationRouterConfiguration();
	public Object retrieveApplicationRouterConfiguration();
	public void updateApplicationRouterConfiguration(Object configuration);
	public Serializable retrieveApplicationRouterConfigurationString();
	public void updateApplicationRouterConfiguration(Serializable configuration);
	
	public String[] findInstalledSipApplications();
	
	public SipService getSipService();
	public void setSipService(SipService sipService);
}
