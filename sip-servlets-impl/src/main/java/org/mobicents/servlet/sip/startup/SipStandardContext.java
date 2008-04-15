/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import java.util.Map;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

/**
 * Sip implementation of the <b>Context</b> interface extending the standard
 * tomcat context to allow deployment of converged applications (sip & web apps)
 * as well as standalone sip servlets applications.
 * 
 * @author jean deruelle
 * 
 */
public class SipStandardContext extends StandardContext implements SipContext {
	//	 the logger
	private static transient Log logger = LogFactory
			.getLog(SipStandardContext.class);

	public String applicationName;
	public String smallIcon;
	public String largeIcon;
	public String description;
	public int proxyTimeout;
	public SipListenersHolder listeners;
	public String mainServlet;
	public Map sipServlets;	
	public Map securityRoles;
	public Map<String,Object> sipApplicationSessionAttributeMap;
	
	/**
	 * 
	 */
	public SipStandardContext() {

	}

	@Override
	public void init() throws Exception {
		logger.info("Initializing the sip context");
//		if (this.getParent() != null) {
//			// Add the main configuration listener for sip applications
//			LifecycleListener sipConfigurationListener = new SipContextConfig();
//			this.addLifecycleListener(sipConfigurationListener);			
//			setDelegate(true);
//		}
		setWrapperClass(SipServletImpl.class.getName());
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
		//JSR 289 Section 2.1.1 Step 1.Deploy the application.
		//This will make start the sip context config, which will in turn parse the sip descriptor deployment
		//and call load on startup which is equivalent to
		//JSR 289 Section 2.1.1 Step 2.Invoke servlet.init(), the initialization method on the Servlet. Invoke the init() on all the load-on-startup Servlets in the applicatio
		super.start();
		//JSR 289 Section 2.1.1 Step 3.Invoke SipApplicationRouter.applicationDeployed() for this application.
		//called implicitky within sipApplicationDispatcher.addSipApplication
		Container container = getParent().getParent();
		if(container instanceof Engine) {
			Service service = ((Engine)container).getService();
			if(service instanceof SipService) {
				SipApplicationDispatcher sipApplicationDispatcher = 
					((SipService)service).getSipApplicationDispatcher();				
				sipApplicationDispatcher.addSipApplication(applicationName, this);
			}
		}				
		//JSR 289 Section 2.1.1 Step 4.If present invoke SipServletListener.servletInitialized() on each of initialized Servlet's listeners.
		sipListenerStart();
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

	@Override
	public Wrapper createWrapper() {		
		return super.createWrapper();
	}
	
	public void addChild(SipServletImpl sipServletImpl) {	
		super.addChild(sipServletImpl);
	}
	
	public void removeChild(SipServletImpl sipServletImpl) {
		super.removeChild(sipServletImpl);
	}
	
	public boolean sipListenerStart() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean sipListenerStop() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getApplicationName()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getApplicationName()
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setApplicationName(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setApplicationName(java.lang.String)
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getDescription()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setDescription(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getLargeIcon()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getLargeIcon()
	 */
	public String getLargeIcon() {
		return largeIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setLargeIcon(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setLargeIcon(java.lang.String)
	 */
	public void setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getListeners()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getListeners()
	 */
	public SipListenersHolder getListeners() {
		return listeners;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setListeners(org.mobicents.servlet.sip.core.session.SipListenersHolder)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setListeners(org.mobicents.servlet.sip.core.session.SipListenersHolder)
	 */
	public void setListeners(SipListenersHolder listeners) {
		this.listeners = listeners;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getMainServlet()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getMainServlet()
	 */
	public String getMainServlet() {
		return mainServlet;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setMainServlet(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setMainServlet(java.lang.String)
	 */
	public void setMainServlet(String mainServlet) {
		this.mainServlet = mainServlet;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getProxyTimeout()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getProxyTimeout()
	 */
	public int getProxyTimeout() {
		return proxyTimeout;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setProxyTimeout(int)
	 */
	public void setProxyTimeout(int proxyTimeout) {
		this.proxyTimeout = proxyTimeout;
	}
		
	public void addConstraint(SipSecurityConstraint securityConstraint) {		
		super.addConstraint(securityConstraint);
	}
	
	public void removeConstraint(SipSecurityConstraint securityConstraint) {
		super.removeConstraint(securityConstraint);
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getSecurityRoles()
	 */
	public Map getSecurityRoles() {
		return securityRoles;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setSecurityRoles(java.util.HashMap)
	 */
	public void setSecurityRoles(Map securityRoles) {
		this.securityRoles = securityRoles;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getServlets()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getSipServlets()
	 */
	public Map getSipServlets() {
		return sipServlets;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setServlets(java.util.HashMap)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setSipServlets(java.util.HashMap)
	 */
	public void setSipServlets(Map sipServlets) {
		this.sipServlets = sipServlets;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSipApplicationSessionAttributeMap()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getSipApplicationSessionAttributeMap()
	 */
	public Map<String, Object> getSipApplicationSessionAttributeMap() {
		return sipApplicationSessionAttributeMap;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSipApplicationSessionAttributeMap(java.util.HashMap)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setSipApplicationSessionAttributeMap(java.util.HashMap)
	 */
	public void setSipApplicationSessionAttributeMap(
			Map<String, Object> sipApplicationSessionAttributeMap) {
		this.sipApplicationSessionAttributeMap = sipApplicationSessionAttributeMap;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSmallIcon()
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getSmallIcon()
	 */
	public String getSmallIcon() {
		return smallIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSmallIcon(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setSmallIcon(java.lang.String)
	 */
	public void setSmallIcon(String smallIcon) {
		this.smallIcon = smallIcon;
	}
	
	@Override
	public void setLoginConfig(LoginConfig config) {	
		super.setLoginConfig(config);
	}
	
	@Override
	public LoginConfig getLoginConfig() {	
		return super.getLoginConfig();
	}
}
