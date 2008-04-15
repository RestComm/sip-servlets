package org.mobicents.servlet.sip.core;

import java.util.Set;

import javax.servlet.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;

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
	
	/**
	 * Add a sip Provider to the current set of sip providers
	 * @param sipProvider the sip provider to add
	 */
	public void addSipProvider(SipProvider sipProvider);
	/**
	 * remove the sip provider form the current set of sip providers
	 * @param sipProvider the sip provider to remove
	 */
	public void removeSipProvider(SipProvider sipProvider);
	/**
	 * This method returns a read only set of the sip providers 
	 * @return read only set of the sip providers
	 */
	public Set<SipProvider> getSipProviders();
	/**
	 * retrieve the sip factory
	 * @return the sip factory
	 */
	public SipFactory getSipFactory();
}
