/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import org.apache.catalina.Service;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

/**
 * This extends the definition of Service from tomcat interface to SIP.
 * A <strong>SipService</strong> is a group of one or more
 * Sip <strong>Connectors</strong> that share a single <strong>Container</strong>
 * to process their requests/responses.  This arrangement allows, for example,
 * a non-secured and secured SIP connectors to share the same population of sip apps and allow
 * for converged apps. The service is responsible for definig the sip application dispatcher
 * that will dispatch sip messages to sip applications.
 * <p>
 */
public interface SipService extends Service {
	/**
	 * Retrieve the sip application dispatcher associated with this service
	 * @return the sip application dispatcher associated with this service
	 */
	public SipApplicationDispatcher getSipApplicationDispatcher();
	/**
	 * Set the sip application dispatcher associated with this service
	 * @param sipApplicationDispatcher the sip application dispatcher associated with this service
	 */
	public void setSipApplicationDispatcher(SipApplicationDispatcher sipApplicationDispatcher);
}
