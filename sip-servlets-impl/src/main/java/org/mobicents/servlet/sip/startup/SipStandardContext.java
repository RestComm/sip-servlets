/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sip implementation of the <b>Context</b> interface extending the standard
 * tomcat context to allow deployment of converged applications (sip & web apps)
 * as well as standalone sip servlets applications.
 * 
 * @author jean deruelle
 * 
 */
public class SipStandardContext extends StandardContext {
	private static final String CONFIG_CLASSNAME = "org.open.servlet.sip.startup.SipContextConfig";

	// the logger
	private static transient Log logger = LogFactory
			.getLog(SipStandardContext.class.getCanonicalName());
	
	/**
	 * 
	 */
	public SipStandardContext() {

	}

	@Override
	public void init() throws Exception {
		logger.info("Initializing the sip context");
		if (this.getParent() != null) {
			// Add the main configuration listener for sip applications
			LifecycleListener sipConfigurationListener = new SipContextConfig();
			this.addLifecycleListener(sipConfigurationListener);
			setDelegate(true);
		}
		// call the super method to correctly initialize the context and fire
		// up the
		// init event on the new registered SipContextConfig, so that the
		// standardcontextconfig
		// is correctly initialized too
		super.init();
		logger.info("sip context Initialized");
	}

	@Override
	public synchronized void start() throws LifecycleException {
		logger.info("Starting the sip context");
		super.start();
		logger.info("sip context started");
	}

	@Override
	public synchronized void stop() throws LifecycleException {
		logger.info("Stopping the sip context");
		super.stop();
		logger.info("sip context stopped");
	}

	@Override
	public void loadOnStartup(Container[] containers) {
		super.loadOnStartup(containers);
	}
}
