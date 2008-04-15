/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.core.timers.TimerServiceImpl;
import org.mobicents.servlet.sip.message.SipFactoryFacade;
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

	private String applicationName;
	private String smallIcon;
	private String largeIcon;
	private String description;
	private int proxyTimeout;
	private SipListenersHolder listeners;
	private String mainServlet;	
//	private Map securityRoles;
//	private Map<String,Object> sipApplicationSessionAttributeMap;
	private SipFactoryFacade sipFactoryFacade;
	
	/**
     * The set of sip application listener class names configured for this
     * application, in the order they were encountered in the sip.xml file.
     */
    private String sipApplicationListeners[] = new String[0];
    
    private SipApplicationDispatcher sipApplicationDispatcher = null;    
	/**
	 * 
	 */
	public SipStandardContext() {
		listeners = new SipListenersHolder();
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
		// call the super method to correctly initialize the context and fire
		// up the
		// init event on the new registered SipContextConfig, so that the
		// standardcontextconfig
		// is correctly initialized too
		super.init();
		
		Container container = getParent().getParent();
		if(container instanceof Engine) {
			Service service = ((Engine)container).getService();
			if(service instanceof SipService) {
				sipApplicationDispatcher = 
					((SipService)service).getSipApplicationDispatcher();								
			}
		}
		if(sipApplicationDispatcher == null) {
			throw new Exception("cannot find any application dispatcher for this context " + name);
		}		
		
		sipFactoryFacade = new SipFactoryFacade(sipApplicationDispatcher.getSipFactory(), this);
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SIP_FACTORY,
				sipFactoryFacade);
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.OUTBOUND_INTERFACES,
				sipApplicationDispatcher.getOutboundInterfaces());
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.TIMER_SERVICE,
				TimerServiceImpl.getInstance());	
		logger.info("sip context Initialized");
	}

	@Override
	public synchronized void start() throws LifecycleException {
		logger.info("Starting the sip context");
		 // Add missing components as necessary
        if (getResources() == null) {   // (1) Required by Loader
            if (logger.isDebugEnabled())
                logger.debug("Configuring default Resources");
            try {
                if ((getDocBase() != null) && (getDocBase().endsWith(".sar")) && (!(new File(getBasePath())).isDirectory()))
                    setResources(new SARDirContext());                
            } catch (IllegalArgumentException e) {
                logger.error("Error initializing resources: " + e.getMessage());
//                ok = false;
            }
        }
		//JSR 289 Section 2.1.1 Step 1.Deploy the application.
		//This will make start the sip context config, which will in turn parse the sip descriptor deployment
		//and call load on startup which is equivalent to
		//JSR 289 Section 2.1.1 Step 2.Invoke servlet.init(), the initialization method on the Servlet. Invoke the init() on all the load-on-startup Servlets in the applicatio
		super.start();		
		//JSR 289 Section 2.1.1 Step 3.Invoke SipApplicationRouter.applicationDeployed() for this application.
		//called implicitly within sipApplicationDispatcher.addSipApplication
		if(getAvailable()) {
			sipApplicationDispatcher.addSipApplication(applicationName, this);
			logger.info("sip context started");
		} else {
			logger.info("sip context didn't started due to errors");
		}
										
	}

	@Override
	public boolean listenerStart() {
		boolean ok = super.listenerStart();
		//the web listeners couldn't be started so we don't even try to load the sip ones
		if(!ok) {
			return ok;
		}
		if (logger.isDebugEnabled())
            logger.debug("Configuring sip listeners");

        // Instantiate the required listeners
        ClassLoader loader = getLoader().getClassLoader();
        ok = listeners.loadListeners(sipApplicationListeners, loader);
        if(!ok) {
			return ok;
		}
        
        List<ServletContextListener> servletContextListeners = listeners.getServletContextListeners();
        if (servletContextListeners != null) {
            ServletContextEvent event =
                new ServletContextEvent(getServletContext());
            for (ServletContextListener servletContextListener : servletContextListeners) {						
                if (servletContextListener == null)
                    continue;
                
                try {
                    fireContainerEvent("beforeContextInitialized", servletContextListener);
                    servletContextListener.contextInitialized(event);
                    fireContainerEvent("afterContextInitialized", servletContextListener);
                } catch (Throwable t) {
                    fireContainerEvent("afterContextInitialized", servletContextListener);
                    getLogger().error
                        (sm.getString("standardContext.listenerStart",
                        		servletContextListener.getClass().getName()), t);
                    ok = false;
                }
                
                // TODO Annotation processing                 
            }
        }
        return ok;
	}
	
	@Override
	public boolean listenerStop() {
		boolean ok = super.listenerStop();
		if (logger.isDebugEnabled())
            logger.debug("Sending application stop events");
        
        List<ServletContextListener> servletContextListeners = listeners.getServletContextListeners();
        if (servletContextListeners != null) {
            ServletContextEvent event =
                new ServletContextEvent(getServletContext());
            for (ServletContextListener servletContextListener : servletContextListeners) {						
                if (servletContextListener == null)
                    continue;
                
                try {
                    fireContainerEvent("beforeContextDestroyed", servletContextListener);
                    servletContextListener.contextDestroyed(event);
                    fireContainerEvent("afterContextDestroyed", servletContextListener);
                } catch (Throwable t) {
                    fireContainerEvent("afterContextDestroyed", servletContextListener);
                    getLogger().error
                        (sm.getString("standardContext.listenerStop",
                        		servletContextListener.getClass().getName()), t);
                    ok = false;
                }
                
                // TODO Annotation processing                 
            }
        }

        // TODO Annotation processing check super class on tomcat 6
        
        listeners.clean();

        return ok;
	}

	/**
     * Get base path. Copy pasted from StandardContext Tomcat class
     */
    protected String getBasePath() {
        String docBase = null;
        Container container = this;
        while (container != null) {
            if (container instanceof Host)
                break;
            container = container.getParent();
        }
        File file = new File(getDocBase());
        if (!file.isAbsolute()) {
            if (container == null) {
                docBase = (new File(engineBase(), getDocBase())).getPath();
            } else {
                // Use the "appBase" property of this container
                String appBase = ((Host) container).getAppBase();
                file = new File(appBase);
                if (!file.isAbsolute())
                    file = new File(engineBase(), appBase);
                docBase = (new File(file, getDocBase())).getPath();
            }
        } else {
            docBase = file.getPath();
        }
        return docBase;
    }
	
	@Override
	public synchronized void stop() throws LifecycleException {
		logger.info("Stopping the sip context");
		if(sipApplicationDispatcher != null) {				
			sipApplicationDispatcher.removeSipApplication(applicationName);		
		}	
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

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getApplicationName()
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setApplicationName(java.lang.String)
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getLargeIcon()
	 */
	public String getLargeIcon() {
		return largeIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setLargeIcon(java.lang.String)
	 */
	public void setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getListeners()
	 */
	public SipListenersHolder getListeners() {
		return listeners;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setListeners(org.mobicents.servlet.sip.core.session.SipListenersHolder)
	 */
	public void setListeners(SipListenersHolder listeners) {
		this.listeners = listeners;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getMainServlet()
	 */
	public String getMainServlet() {
		return mainServlet;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#setMainServlet(java.lang.String)
	 */
	public void setMainServlet(String mainServlet) {
		this.mainServlet = mainServlet;
	}
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
	
//	/* (non-Javadoc)
//	 * @see org.mobicents.servlet.sip.startup.SipContext#getSecurityRoles()
//	 */
//	public Map getSecurityRoles() {
//		return securityRoles;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.mobicents.servlet.sip.startup.SipContext#setSecurityRoles(java.util.HashMap)
//	 */
//	public void setSecurityRoles(Map securityRoles) {
//		this.securityRoles = securityRoles;
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.mobicents.servlet.sip.startup.SipContext#getSipApplicationSessionAttributeMap()
//	 */
//	public Map<String, Object> getSipApplicationSessionAttributeMap() {
//		return sipApplicationSessionAttributeMap;
//	}
//	/* (non-Javadoc)
//	 * @see org.mobicents.servlet.sip.startup.SipContext#setSipApplicationSessionAttributeMap(java.util.HashMap)
//	 */
//	public void setSipApplicationSessionAttributeMap(
//			Map<String, Object> sipApplicationSessionAttributeMap) {
//		this.sipApplicationSessionAttributeMap = sipApplicationSessionAttributeMap;
//	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.SipContext#getSmallIcon()
	 */
	public String getSmallIcon() {
		return smallIcon;
	}
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
	
	/**
     * Add a new Listener class name to the set of Listeners
     * configured for this application.
     *
     * @param listener Java class name of a listener class
     */
    public void addSipApplicationListener(String listener) {

        synchronized (sipApplicationListeners) {
            String results[] =new String[sipApplicationListeners.length + 1];
            for (int i = 0; i < sipApplicationListeners.length; i++) {
                if (listener.equals(sipApplicationListeners[i]))
                    return;
                results[i] = sipApplicationListeners[i];
            }
            results[sipApplicationListeners.length] = listener;
            sipApplicationListeners = results;
        }
        fireContainerEvent("addSipApplicationListener", listener);

        // FIXME - add instance if already started?

    }
    
    /**
     * Remove the specified application listener class from the set of
     * listeners for this application.
     *
     * @param listener Java class name of the listener to be removed
     */
    public void removeSipApplicationListener(String listener) {

        synchronized (sipApplicationListeners) {
            // Make sure this listener is currently present
            int n = -1;
            for (int i = 0; i < sipApplicationListeners.length; i++) {
                if (sipApplicationListeners[i].equals(listener)) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;

            // Remove the specified listener
            int j = 0;
            String results[] = new String[sipApplicationListeners.length - 1];
            for (int i = 0; i < sipApplicationListeners.length; i++) {
                if (i != n)
                    results[j++] = sipApplicationListeners[i];
            }
            sipApplicationListeners = results;

        }

        // Inform interested listeners
        fireContainerEvent("removeSipApplicationListener", listener);

        // FIXME - behavior if already started?

    }
    
    /**
     * Return the set of sip application listener class names configured
     * for this application.
     */
    public String[] findSipApplicationListeners() {
        return (sipApplicationListeners);
    }
}
