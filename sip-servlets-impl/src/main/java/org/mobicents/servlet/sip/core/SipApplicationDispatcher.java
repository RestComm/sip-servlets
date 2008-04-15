package org.mobicents.servlet.sip.core;

import javax.sip.SipListener;

import org.apache.catalina.LifecycleException;
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
	public void init(String sipApplicationRouterClassName) throws LifecycleException;
	
	/**
	 * Start the sip application dispatcher
	 */
	public void start();
	
	/**
	 * Stop the sip application dispatcher
	 */
	public void stop();
	
	/**
	 * Add a new sip application to which sip messages can be routed
	 * @param sipApplicationName the sip application logical name
	 * @param sipApplication the sip context representing the application 
	 */
	public void addSipApplication(String sipApplicationName, SipContext sipApplication);
	/**
	 * Remove a sip application to which sip messages can be routed
	 * @param sipApplicationName the sip application logical name of the application to remove
	 */
	public SipContext removeSipApplication(String sipApplicationName);
}
