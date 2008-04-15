/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.core;

import java.util.List;
import java.util.Set;

import javax.servlet.sip.SipApplicationRouterInfo;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipURI;
import javax.sip.SipListener;
import javax.sip.SipProvider;

import org.apache.catalina.LifecycleException;
import org.mobicents.servlet.sip.core.session.SessionManager;
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

	/**
	 * Initialize the sip application dispatcher and its associated sip application router implementation
	 * @param sipApplicationRouterClassName the class name of the sip application router
	 * to load and initialize at the initializaton
	 * @throws LifecycleException The Sip Application Router cannot be initialized correctly
	 */
	void init(String sipApplicationRouterClassName) throws LifecycleException;
	
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
	 * Add a sip Provider to the current set of sip providers
	 * @param sipProvider the sip provider to add
	 */
	void addSipProvider(SipProvider sipProvider);
	/**
	 * remove the sip provider form the current set of sip providers
	 * @param sipProvider the sip provider to remove
	 */
	void removeSipProvider(SipProvider sipProvider);
	/**
	 * This method returns a read only set of the sip providers 
	 * @return read only set of the sip providers
	 */
	Set<SipProvider> getSipProviders();
	/**
	 * retrieve the sip factory
	 * @return the sip factory
	 */
	SipFactory getSipFactory();
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
	List<String> findHostNames();
	
	/**
	 * Retrieve the session manager associated with this application disptacher 
	 * @return the session manager associated with this application disptacher
	 */
	SessionManager getSessionManager();
	
	/**
	 * 
	 * @param sipServletRequestImpl
	 * @return
	 */
	SipApplicationRouterInfo getNextInterestedApplication(SipServletRequestImpl sipServletRequestImpl);
}
