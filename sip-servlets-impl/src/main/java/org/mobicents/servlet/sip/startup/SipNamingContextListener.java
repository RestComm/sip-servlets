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
package org.mobicents.servlet.sip.startup;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.core.NamingContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.naming.ContextAccessController;

/**
 * Helper class used to initialize and populate the JNDI context associated
 * with each context with the sip factory.
 * 
 * @author Jean Deruelle
 *
 */
public class SipNamingContextListener extends NamingContextListener {
	private static transient Log logger = LogFactory
		.getLog(SipNamingContextListener.class);
	
	public static final String NAMING_CONTEXT_SIP_SUBCONTEXT_ADDED_EVENT = "addSipSubcontext";
	public static final String NAMING_CONTEXT_SIP_SUBCONTEXT_REMOVED_EVENT = "removeSipSubContext";
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
	public void containerEvent(ContainerEvent event) {		
		super.containerEvent(event);
		// Setting the context in read/write mode
        ContextAccessController.setWritable(getName(), container);        
        String type = event.getType();
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
        } else if (type.equals(NAMING_CONTEXT_SIP_FACTORY_ADDED_EVENT)) {
			SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                addSipFactory(envCtx, sipFactory);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_FACTORY_REMOVED_EVENT)) {
        	SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                removeSipFactory(envCtx, sipFactory);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory removed from the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_SESSIONS_UTIL_ADDED_EVENT)) {
			SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) event.getData();
            if (sipSessionsUtil != null) {                
                addSipSessionsUtil(envCtx, sipSessionsUtil);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_SESSIONS_UTIL_REMOVED_EVENT)) {
        	SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) event.getData();
            if (sipSessionsUtil != null) {                
                removeSipSessionsUtil(envCtx, sipSessionsUtil);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory removed from the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_TIMER_SERVICE_ADDED_EVENT)) {
			TimerService timerService = (TimerService) event.getData();
            if (timerService != null) {                
                addTimerService(envCtx, timerService);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIP_FACTORY_REMOVED_EVENT)) {
        	TimerService timerService = (TimerService) event.getData();
            if (timerService != null) {                
                removeTimerService(envCtx, timerService);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory removed from the JNDI context for container " + event.getContainer());
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
	 * Removes the sip sessions util binding from the jndi mapping
	 * @param sipSessionsUtil the sip sessions util to remove
	 */
	public static void removeSipSessionsUtil(Context envCtx, SipSessionsUtil sipSessionsUtil) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.unbind(SIP_SESSIONS_UTIL_JNDI_NAME);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the sip sessions util binding from the jndi mapping
	 * and bind the sip sessions util in parameter to it
	 * @param sipSessionsUtil the sip sessions util to add
	 */
	public static void addSipSessionsUtil(Context envCtx, SipSessionsUtil sipSessionsUtil) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.bind(SIP_SESSIONS_UTIL_JNDI_NAME, sipSessionsUtil);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
	
	/**
	 * Removes the Timer Service binding from the jndi mapping
	 * @param timerService the Timer Service to remove
	 */
	public static void removeTimerService(Context envCtx, TimerService timerService) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.unbind(TIMER_SERVICE_JNDI_NAME);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the sip timer service from the jndi mapping
	 * and bind the timer service in parameter to it
	 * @param timerService the Timer Service to add
	 */
	public static void addTimerService(Context envCtx, TimerService timerService) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.bind(TIMER_SERVICE_JNDI_NAME, timerService);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
	
	/**
	 * Removes the sip factory binding from the jndi mapping
	 * @param sipFactory the sip factory to remove
	 */
	public static void removeSipFactory(Context envCtx, SipFactory sipFactory) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.unbind(SIP_FACTORY_JNDI_NAME);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.unbindFailed", e));
			}
		}
	}

	/**
	 * Add the sip factory binding from the jndi mapping
	 * and bind the sip factory in paramter to it
	 * @param sipFactory the sip factory to add
	 */
	public static void addSipFactory(Context envCtx, SipFactory sipFactory) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.bind(SIP_FACTORY_JNDI_NAME, sipFactory);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
}
