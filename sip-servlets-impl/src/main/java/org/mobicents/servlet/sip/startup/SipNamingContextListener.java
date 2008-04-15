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
	
	public static final String NAMING_CONTEXT_SIPFACTORY_ADDED_EVENT = "addSipFactory";
	public static final String NAMING_CONTEXT_SIPFACTORY_REMOVED_EVENT = "removeSipFactory";
	public static final String SIP_SUBCONTEXT = "sip";
	public static final String SIPFACTORY_JNDI_NAME = "SipFactory";
	
	@Override
	public void containerEvent(ContainerEvent event) {		
		super.containerEvent(event);
		// Setting the context in read/write mode
        ContextAccessController.setWritable(getName(), container);        
        String type = event.getType();
		if (type.equals(NAMING_CONTEXT_SIPFACTORY_ADDED_EVENT)) {
			SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                addSipFactory(envCtx, sipFactory);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory added to the JNDI context for container " + event.getContainer());
                }
            }
        } else if (type.equals(NAMING_CONTEXT_SIPFACTORY_REMOVED_EVENT)) {
        	SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                removeSipFactory(envCtx, sipFactory);
                if(logger.isDebugEnabled()) {
                	logger.debug("Sip Factory removed from the JNDI context for container " + event.getContainer());
                }
            }
        }
		// Setting the context in read only mode
        ContextAccessController.setReadOnly(getName());
	}

	/**
	 * Removes the sip factory binding from the jndi mapping along with the sip subcontext
	 * @param sipFactory the sip factory to remove
	 */
	public static void removeSipFactory(Context envCtx, SipFactory sipFactory) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = (javax.naming.Context)envCtx.lookup(SIP_SUBCONTEXT);
				sipContext.unbind(SIPFACTORY_JNDI_NAME);
				envCtx.destroySubcontext(SIP_SUBCONTEXT);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}
		}
	}

	/**
	 * Add the sip factory binding from the jndi mapping along with the sip subcontext
	 * and bind the sip factory in paramter to it
	 * @param sipFactory the sip factory to add
	 */
	public static void addSipFactory(Context envCtx, SipFactory sipFactory) {
		if(envCtx != null) {
			try {
				javax.naming.Context sipContext = envCtx.createSubcontext(SIP_SUBCONTEXT);
				sipContext.bind(SIPFACTORY_JNDI_NAME, sipFactory);
			} catch (NamingException e) {
				logger.error(sm.getString("naming.bindFailed", e));
			}			
		}
	}
}
