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
package org.mobicents.servlet.sip.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.sip.SipListener;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;

import org.apache.catalina.LifecycleException;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * 
 * Classes implementing this interface can be used in the SipService Class to
 * be the central point getting the sip messages from the different stacks and
 * dispatching them to sip applications. 
 *
 */
public interface SipApplicationDispatcher extends SipListener {

	//list of methods supported by the AR
	public static final String[] METHODS_SUPPORTED = 
		new String[] {"REGISTER", "INVITE", "ACK", "BYE", "CANCEL", "MESSAGE", "INFO", "SUBSCRIBE", "NOTIFY", "UPDATE", "PUBLISH", "REFER", "PRACK", "OPTIONS"};
	
	// List of sip extensions supported by the container	
	public static final String[] EXTENSIONS_SUPPORTED = 
		new String[] {"MESSAGE", "INFO", "SUBSCRIBE", "NOTIFY", "UPDATE", "PUBLISH", "REFER", "PRACK", "100rel", "STUN", "path", "join"};
	// List of sip rfcs supported by the container
	public static final String[] RFC_SUPPORTED = 
		new String[] {"3261", "3428", "2976", "3265", "3311", "3903", "3515", "3262", "3489", "3327", "3911"};
	
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
	void init() throws LifecycleException;
	
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
	SipFactoryImpl getSipFactory();
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
	SipApplicationRouterInfo getNextInterestedApplication(SipServletRequestImpl sipServletRequestImpl);
	
	String getDomain();
    
    void setDomain(String domain);
    
    boolean isRouteExternal(RouteHeader routeHeader);
    
    boolean isViaHeaderExternal(ViaHeader viaHeader);
    
    boolean isExternal(String host, int port, String transport);

	SipApplicationRouter getSipApplicationRouter();

	public String getApplicationNameFromHash(String hash);
	public String getHashFromApplicationName(String appName);
	
	public ConcurrencyControlMode getConcurrencyControlMode();
	public void setConcurrencyControlMode(ConcurrencyControlMode concurrencyControlMode);
	public void setConcurrencyControlModeByName(String concurrencyControlMode);

	public int getQueueSize();
	public void setQueueSize(int queueSize);
	
	public void setMemoryThreshold(int memoryThreshold);
	public int getMemoryThreshold();
	
	public void setCongestionControlCheckingInterval(long interval);
	public long getCongestionControlCheckingInterval();
		
	public CongestionControlPolicy getCongestionControlPolicy();
	public void setCongestionControlPolicy(CongestionControlPolicy congestionControlPolicy);
	public void setCongestionControlPolicyByName(String congestionControlPolicy);
	
	public int getNumberOfMessagesInQueue();
	public double getPercentageOfMemoryUsed();
}
