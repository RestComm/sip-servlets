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

import javax.sip.SipStack;

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
 * 
 * @author Jean Deruelle
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
	/**
	 * *Get the underlying SIP Stack handling the incoming and outgoing SIP Messages
	 * @return the underlying SIP Stack handling the incoming and outgoing SIP Messages
	 */
	public SipStack getSipStack();
}
