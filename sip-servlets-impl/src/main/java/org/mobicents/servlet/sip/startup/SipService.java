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
