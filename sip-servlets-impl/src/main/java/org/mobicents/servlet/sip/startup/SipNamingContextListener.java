/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import javax.naming.NamingException;
import javax.servlet.sip.SipFactory;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.core.NamingContextListener;
import org.apache.naming.ContextAccessController;

/**
 * Helper class used to initialize and populate the JNDI context associated
 * with each context with the sip factory.
 * 
 * @author Jean Deruelle
 *
 */
public class SipNamingContextListener extends NamingContextListener {
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
		if (type.equals("addSipFactory")) {
			SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                addSipFactory(sipFactory);
            }
        } else if (type.equals("removeSipFactory")) {
        	SipFactory sipFactory = (SipFactory) event.getData();
            if (sipFactory != null) {                
                removeSipFactory(sipFactory);
            }
        }
		// Setting the context in read only mode
        ContextAccessController.setReadOnly(getName());
	}

	/**
	 * Removes the sip factory binding from the jndi mapping along with the sip subcontext
	 * @param sipFactory the sip factory to remove
	 */
	private void removeSipFactory(SipFactory sipFactory) {
		try {
			envCtx.unbind(SIPFACTORY_JNDI_NAME);
			envCtx.destroySubcontext(SIP_SUBCONTEXT);
		} catch (NamingException e) {
			logger.error(sm.getString("naming.bindFailed", e));
		}
	}

	/**
	 * Add the sip factory binding from the jndi mapping along with the sip subcontext
	 * and bind the sip factory in paramter to it
	 * @param sipFactory the sip factory to add
	 */
	private void addSipFactory(SipFactory sipFactory) {
		try {
			envCtx.createSubcontext(SIP_SUBCONTEXT);
			envCtx.bind(SIPFACTORY_JNDI_NAME, sipFactory);
		} catch (NamingException e) {
			logger.error(sm.getString("naming.bindFailed", e));
		}			
	}
}
