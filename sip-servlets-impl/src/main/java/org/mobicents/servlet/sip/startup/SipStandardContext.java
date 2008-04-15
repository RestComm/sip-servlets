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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Stack;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.annotations.SipAnnotationProcessor;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.core.session.SipSessionsUtilImpl;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.core.timers.TimerServiceImpl;
import org.mobicents.servlet.sip.message.SipFactoryFacade;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

/**
 * Sip implementation of the <b>Context</b> interface extending the standard
 * tomcat context to allow deployment of converged applications (sip & web apps)
 * as well as standalone sip servlets applications.
 * 
 * @author Jean Deruelle
 * 
 */
public class SipStandardContext extends StandardContext implements SipContext {
	//	 the logger
	private static transient Log logger = LogFactory
			.getLog(SipStandardContext.class);

	// as mentionned per JSR 289 Section 6.1.2.1 default lifetime for an 
	// application session is 3 minutes
	private static int DEFAULT_LIFETIME = 3;
	
	protected String applicationName;
	protected String smallIcon;
	protected String largeIcon;
	protected String description;
	protected int proxyTimeout;
	protected int sipApplicationSessionTimeout;
	protected SipListenersHolder listeners;
	protected String mainServlet;	
	protected SipFactoryFacade sipFactoryFacade;	
	protected SipSessionsUtilImpl sipSessionsUtil;
	protected SipLoginConfig sipLoginConfig;
	
    protected String namingContextName;
    
    protected Method sipApplicationKeyMethod;
    
	/**
     * The set of sip application listener class names configured for this
     * application, in the order they were encountered in the sip.xml file.
     */
    protected String sipApplicationListeners[] = new String[0];
    
    protected SipApplicationDispatcher sipApplicationDispatcher = null;    
	/**
	 * 
	 */
	public SipStandardContext() {
		super();
		sipApplicationSessionTimeout = DEFAULT_LIFETIME;
		pipeline.setBasic(new SipStandardContextValve());
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
		
		sipFactoryFacade = new SipFactoryFacade((SipFactoryImpl)sipApplicationDispatcher.getSipFactory(), this);
		sipSessionsUtil = new SipSessionsUtilImpl(((SipFactoryImpl)sipApplicationDispatcher.getSipFactory()).getSessionManager(), applicationName);
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SIP_FACTORY,
				sipFactoryFacade);
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.OUTBOUND_INTERFACES,
				sipApplicationDispatcher.getOutboundInterfaces());
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.TIMER_SERVICE,
				TimerServiceImpl.getInstance());
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SUPPORTED,
				SipApplicationDispatcher.EXTENSIONS_SUPPORTED);
		this.getServletContext().setAttribute(javax.servlet.sip.SipServlet.SIP_SESSIONS_UTIL,
				sipSessionsUtil);
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
        // Configure default manager if none was specified
        if (manager == null) {
            if ((getCluster() != null) && getDistributable()) {
                try {
                    manager = getCluster().createManager(getName());
                } catch (Exception ex) {
                    logger.error("standardContext.clusterFail", ex);
//                    ok = false;
                }
            } else {
                setManager(new SipStandardManager());
            }
        }
        // Reading the "catalina.useNaming" environment variable
        String useNamingProperty = System.getProperty("catalina.useNaming");
        if ((useNamingProperty != null)
            && (useNamingProperty.equals("false"))) {
            setUseNaming(false);
        }
        //activating our custom naming context to be able to set the sip factory in JNDI
        if (isUseNaming()) {    
        	if (getNamingContextListener() == null) {
            	NamingContextListener namingContextListener = new SipNamingContextListener();
                namingContextListener.setName(getNamingContextName());
                setNamingContextListener(namingContextListener);
                addLifecycleListener(namingContextListener);
                addContainerListener(namingContextListener);                
            }
        }   
		//JSR 289 Section 2.1.1 Step 1.Deploy the application.
		//This will make start the sip context config, which will in turn parse the sip descriptor deployment
		//and call load on startup which is equivalent to
		//JSR 289 Section 2.1.1 Step 2.Invoke servlet.init(), the initialization method on the Servlet. Invoke the init() on all the load-on-startup Servlets in the applicatio
		super.start();	
								
		if(getAvailable()) {
			// Replace the default annotation processor. This is needed to handle resource injection
			// for SipFactory, Session utils and other objects residing in the servlet context space.
			// Of course if the variable is not found in in the servet context it defaults to the
			// normal lookup method - in the default naming context.
			if(isUseNaming()) {
				//tomcat naming 
				this.setAnnotationProcessor(
						new SipAnnotationProcessor(
								getNamingContextListener().getEnvContext(),
								this));
			} else {
				try {
					InitialContext iniCtx = new InitialContext();
					Context envCtx = (Context) iniCtx.lookup("java:comp/env");
					// jboss or other kind of naming
					this.setAnnotationProcessor(
							new SipAnnotationProcessor(
									envCtx,
									this));
				} catch (NamingException e) {
					logger.error("Impossible to get the naming context ", e);
				}						
			}
			//set the session manager on the specific sipstandardmanager to handle converged http sessions
			//FIXME the session manager should be refactored and made part of the sipstandardmanager
			if(getManager() instanceof SipStandardManager) {
				((SipStandardManager)getManager()).setSipFactoryImpl(
						((SipFactoryImpl)sipApplicationDispatcher.getSipFactory())); 
			}
			//JSR 289 Section 2.1.1 Step 3.Invoke SipApplicationRouter.applicationDeployed() for this application.
			//called implicitly within sipApplicationDispatcher.addSipApplication
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
		super.stop();
		// this should happen after so that applications can still do some processing
		// in destroy methods to notify that context is getting destroyed and app removed
		if(sipApplicationDispatcher != null) {				
			sipApplicationDispatcher.removeSipApplication(applicationName);		
		}	
		// not needed since the JNDI will be destroyed automatically
//		if(isUseNaming()) {
//			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_SIP_FACTORY_REMOVED_EVENT, sipFactoryFacade);
//			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_SIP_SESSIONS_UTIL_REMOVED_EVENT, sipSessionsUtil);
//			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_TIMER_SERVICE_REMOVED_EVENT, TimerServiceImpl.getInstance());
//			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_SIP_SUBCONTEXT_REMOVED_EVENT, null);
//		}  else {
//        	try {
//				InitialContext iniCtx = new InitialContext();
//				Context envCtx = (Context) iniCtx.lookup("java:comp/env");
//				// jboss or other kind of naming
//				SipNamingContextListener.removeSipFactory(envCtx, sipFactoryFacade);
//				SipNamingContextListener.removeSipSessionsUtil(envCtx, sipSessionsUtil);
//				SipNamingContextListener.removeTimerService(envCtx, TimerServiceImpl.getInstance());
//				SipNamingContextListener.removeSipSubcontext(envCtx);
//			} catch (NamingException e) {
//				//It is possible that the context has already been removed so no problem,
//				//we are stopping anyway
////				logger.error("Impossible to get the naming context ", e);				
//			}	        	
//        }
		logger.info("sip context stopped");
	}

	@Override
	public void loadOnStartup(Container[] containers) {
		if(isUseNaming()) {
			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_SIP_SUBCONTEXT_ADDED_EVENT, null);
			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_SIP_FACTORY_ADDED_EVENT, sipFactoryFacade);
			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_SIP_SESSIONS_UTIL_ADDED_EVENT, sipSessionsUtil);
			fireContainerEvent(SipNamingContextListener.NAMING_CONTEXT_TIMER_SERVICE_ADDED_EVENT, TimerServiceImpl.getInstance());
		} else {
        	try {
				InitialContext iniCtx = new InitialContext();
				Context envCtx = (Context) iniCtx.lookup("java:comp/env");
				// jboss or other kind of naming
				SipNamingContextListener.addSipSubcontext(envCtx);
				SipNamingContextListener.addSipFactory(envCtx, sipFactoryFacade);
				SipNamingContextListener.addSipSessionsUtil(envCtx, sipSessionsUtil);
				SipNamingContextListener.addTimerService(envCtx, TimerServiceImpl.getInstance());
			} catch (NamingException e) {
				logger.error("Impossible to get the naming context ", e);
				throw new IllegalStateException(e);
			}	        	
        }   
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
	
	public void setSipLoginConfig(SipLoginConfig config) {
		this.sipLoginConfig = config;
	}
	
	public SipLoginConfig getSipLoginConfig() {
		return this.sipLoginConfig;
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

	/**
	 * @return the sipApplicationDispatcher
	 */
	public SipApplicationDispatcher getSipApplicationDispatcher() {
		return sipApplicationDispatcher;
	}

	/**
	 * @return the sipFactoryFacade
	 */
	public SipFactoryFacade getSipFactoryFacade() {
		return sipFactoryFacade;
	}		
	
	/**
     * Get naming context full name.
     */
    private String getNamingContextName() {    	
	    if (namingContextName == null) {
			Container parent = getParent();
			if (parent == null) {
				namingContextName = getName();
			} else {
				Stack<String> stk = new Stack<String>();
				StringBuffer buff = new StringBuffer();
				while (parent != null) {
					stk.push(parent.getName());
					parent = parent.getParent();
				}
				while (!stk.empty()) {
					buff.append("/" + stk.pop());
				}
				buff.append(getName());
				namingContextName = buff.toString();
			}
		}
		return namingContextName;
    }
    
    @Override
    public synchronized void setManager(Manager manager) {
    	if(manager instanceof SipStandardManager && sipApplicationDispatcher != null) {
			((SipStandardManager)manager).setSipFactoryImpl(
					((SipFactoryImpl)sipApplicationDispatcher.getSipFactory())); 
		}
    	super.setManager(manager);
    }
    
    @Override
    public Manager getManager() {    	
    	return super.getManager();
    }

	/**
	 * @return the sipApplicationSessionTimeout in minutes
	 */
	public int getSipApplicationSessionTimeout() {
		return sipApplicationSessionTimeout;
	}

	/**
	 * @param sipApplicationSessionTimeout the sipApplicationSessionTimeout to set in minutes
	 */
	public void setSipApplicationSessionTimeout(int sipApplicationSessionTimeout) {
		this.sipApplicationSessionTimeout = sipApplicationSessionTimeout;		
	}

	public Method getSipApplicationKeyMethod() {
		return sipApplicationKeyMethod;
	}

	public void setSipApplicationKeyMethod(Method sipApplicationKeyMethod) {
		this.sipApplicationKeyMethod = sipApplicationKeyMethod;
	}
	
	public String getJbossBasePath() {
		return getBasePath();
	}
}
