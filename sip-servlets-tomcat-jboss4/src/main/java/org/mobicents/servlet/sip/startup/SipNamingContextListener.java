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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.NamingContextListener;
import org.apache.log4j.Logger;
import org.apache.naming.ContextAccessController;
import org.mobicents.servlet.sip.annotations.SipAnnotationProcessor;

/**
 * Helper class used to initialize and populate the JNDI context associated
 * with each context with the sip factory.
 * 
 * @author Jean Deruelle
 *
 */
public class SipNamingContextListener extends NamingContextListener {
	private static transient final Logger logger = Logger.getLogger(SipNamingContextListener.class);
	
	public static final String NAMING_CONTEXT_SIP_SUBCONTEXT_ADDED_EVENT = "addSipSubcontext";
	public static final String NAMING_CONTEXT_SIP_SUBCONTEXT_REMOVED_EVENT = "removeSipSubContext";
	public static final String NAMING_CONTEXT_APPNAME_SUBCONTEXT_ADDED_EVENT = "addAppNameSubcontext";
	public static final String NAMING_CONTEXT_APPNAME_SUBCONTEXT_REMOVED_EVENT = "removeAppNameSubcontext";
	public static final String NAMING_CONTEXT_SIP_FACTORY_ADDED_EVENT = "addSipFactory";
	public static final String NAMING_CONTEXT_SIP_FACTORY_REMOVED_EVENT = "removeSipFactory";
	public static final String NAMING_CONTEXT_SIP_SESSIONS_UTIL_ADDED_EVENT = "addSipSessionsUtil";
	public static final String NAMING_CONTEXT_SIP_SESSIONS_UTIL_REMOVED_EVENT = "removeSipSessionsUtil";
	public static final String NAMING_CONTEXT_TIMER_SERVICE_ADDED_EVENT = "addTimerService";
	public static final String NAMING_CONTEXT_TIMER_SERVICE_REMOVED_EVENT = "removeTimerService";
	public static final String SIP_SUBCONTEXT = "sip";
	public static final String SIP_FACTORY_JNDI_NAME = "SipFactory";
	public static final String SIP_SESSIONS_UTIL_JNDI_NAME = "SipSessionsUtil";
	public static final String TIMER_SERVICE_JNDI_NAME = "TimerService";	
	
	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		super.lifecycleEvent(event);
		if (event.getType().equalsIgnoreCase(Lifecycle.START_EVENT)) {
			if (container instanceof SipContext) {
				((SipAnnotationProcessor)((SipContext)container).getAnnotationProcessor()).setContext(envCtx);
			}
		}
	}
	
	@Override
	public void containerEvent(ContainerEvent event) {		
		super.containerEvent(event);
		
		// Setting the context in read/write mode
        ContextAccessController.setWritable(getName(), container);        
        String type = event.getType();
        SipContext sipContext = null;
        String appName = null;
        if(event.getContainer() instanceof SipContext) {
        	sipContext = (SipContext)event.getContainer();
        	appName = sipContext.getApplicationName();
        }
        if (type.equals(NAMING_CONTEXT_SIP_SUBCONTEXT_ADDED_EVENT)) {
            addSipSubcontext(envCtx);
            if(logger.isDebugEnabled()) {
            	logger.debug("Sip Subcontext added to the JNDI context for container " + event.getContainer());
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_SUBCONTEXT_REMOVED_EVENT)) {
            removeSipSubcontext(envCtx);
            if(logger.isDebugEnabled()) {
            	logger.debug("Sip Subcontext removed from the JNDI context for container " + event.getContainer());
            }
        } if (type.equals(NAMING_CONTEXT_APPNAME_SUBCONTEXT_ADDED_EVENT)) {
            addAppNameSubContext(envCtx, appName);
            if(logger.isDebugEnabled()) {
            	logger.debug(appName + " Subcontext added to the JNDI context for container " + event.getContainer());
            }
        } else if (type.equals(NAMING_CONTEXT_APPNAME_SUBCONTEXT_REMOVED_EVENT)) {
            removeAppNameSubContext(envCtx, appName);
            if(logger.isDebugEnabled()) {
            	logger.debug(appName + " Subcontext removed from the JNDI context for container " + event.getContainer());
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_FACTORY_ADDED_EVENT)) {
			SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                addSipFactory(envCtx, appName, sipFactory);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_FACTORY_REMOVED_EVENT)) {
        	SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                removeSipFactory(envCtx, appName, sipFactory);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory removed from the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_SESSIONS_UTIL_ADDED_EVENT)) {
			SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) event.getData();
            if (sipSessionsUtil != null) {                
                addSipSessionsUtil(envCtx, appName, sipSessionsUtil);
                if(logger.isDebugEnabled()) {
                	logger.debug("SipSessionsUtil added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_SESSIONS_UTIL_REMOVED_EVENT)) {
        	SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) event.getData();
            if (sipSessionsUtil != null) {                
                removeSipSessionsUtil(envCtx, appName, sipSessionsUtil);
                if(logger.isDebugEnabled()) {
                	logger.debug("SipSessionsUtil removed from the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_TIMER_SERVICE_ADDED_EVENT)) {
			TimerService timerService = (TimerService) event.getData();
            if (timerService != null) {                
                addTimerService(envCtx, appName, timerService);
                if(logger.isDebugEnabled()) {
                	logger.debug("TimerService added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_FACTORY_REMOVED_EVENT)) {
        	TimerService timerService = (TimerService) event.getData();
            if (timerService != null) {                
                removeTimerService(envCtx, appName, timerService);
                if(logger.isDebugEnabled()) {
                	logger.debug("TimerService removed from the JNDI context for container " + event.getContainer());
                }
            }
        }
		// Setting the context in read only mode
        ContextAccessController.setReadOnly(getName());
	}
	
	/**
	 * Removes the sip subcontext from JNDI
	 * @param envCtx the envContext from which the sip subcontext should be removed
	 */
	public static void removeSipSubcontext(Context envCtx) {
		try {
			envCtx.destroySubcontext(SIP_SUBCONTEXT);
		} catch (NamingException e) {
			logger.error(sm.getString("naming.unbindFailed", e));
		}
	}
	/**
	 * Add the sip subcontext to JNDI
	 * @param envCtx the envContext to which the sip subcontext should be added
	 */
	public static void addSipSubcontext(Context envCtx) {
		try {
			envCtx.createSubcontext(SIP_SUBCONTEXT);
		} catch (NamingException e) {
			logger.error(sm.getString("naming.bindFailed", e));
		}
	}
	
	/**
	 * Removes the app name subcontext from the jndi mapping
	 * @param appName sub context Name
	 */
	public static void removeAppNameSubContext(Context envCtx, String appName) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.destroySubcontext(appName);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the application name subcontext from the jndi mapping
	 * @param appName sub context Name
	 */
	public static void addAppNameSubContext(Context envCtx, String appName) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.createSubcontext(appName);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}

	/**
	 * Removes the sip sessions util binding from the jndi mapping
	 * @param appName the application name subcontext
	 * @param sipSessionsUtil the sip sessions util to remove
	 */
	public static void removeSipSessionsUtil(Context envCtx, String appName, SipSessionsUtil sipSessionsUtil) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT + "/" + appName);
				sipContext.unbind(SIP_SESSIONS_UTIL_JNDI_NAME);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the sip sessions util binding from the jndi mapping
	 * and bind the sip sessions util in parameter to it
	 * @param appName the application name subcontext
	 * @param sipSessionsUtil the sip sessions util to add
	 */
	public static void addSipSessionsUtil(Context envCtx, String appName, SipSessionsUtil sipSessionsUtil) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT + "/" + appName);
				sipContext.bind(SIP_SESSIONS_UTIL_JNDI_NAME, sipSessionsUtil);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
	
	/**
	 * Removes the Timer Service binding from the jndi mapping
	 * @param appName the application name subcontext
	 * @param timerService the Timer Service to remove
	 */
	public static void removeTimerService(Context envCtx, String appName, TimerService timerService) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT + "/" + appName);
				sipContext.unbind(TIMER_SERVICE_JNDI_NAME);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the sip timer service from the jndi mapping
	 * and bind the timer service in parameter to it
	 * @param appName the application name subcontext 
	 * @param timerService the Timer Service to add
	 */
	public static void addTimerService(Context envCtx, String appName, TimerService timerService) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT + "/" + appName);
				sipContext.bind(TIMER_SERVICE_JNDI_NAME, timerService);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
	
	/**
	 * Removes the sip factory binding from the jndi mapping
	 * @param appName the application name subcontext
	 * @param sipFactory the sip factory to remove
	 */
	public static void removeSipFactory(Context envCtx, String appName, SipFactory sipFactory) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT + "/" + appName);
				sipContext.unbind(SIP_FACTORY_JNDI_NAME);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the sip factory binding from the jndi mapping
	 * and bind the sip factory in paramter to it
	 * @param appName 
	 * @param sipFactory the sip factory to add
	 */
	public static void addSipFactory(Context envCtx, String appName, SipFactory sipFactory) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT + "/" + appName);
				sipContext.bind(SIP_FACTORY_JNDI_NAME, sipFactory);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
}
