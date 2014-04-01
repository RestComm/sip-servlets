/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.javax.servlet.sip;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSessionsUtil;

/**
 * Interface Extension that adds extra features to the JSR 289 SipFactory interface to allow creation of Sip Application Session outside the scope of container managed threads.</br>
 * 
 * @author jean.deruelle@telestax.com
 * 
 * @since 7.0.2
 */
public interface SipSessionsUtilExt extends SipSessionsUtil {
	/**
     * Returns the SipApplicationSession for a given applicationSessionId. The applicationSessionId String is the same as that obtained through SipApplicationSession.getId(). The method shall return the Application Session only if the queried application session belongs to the application from where this method is invoked. As an example if there exists a SIP Application with some J2EE component like a Message Driven Bean, bundled in the same application archive file (.war), then if the id of SipApplicationSession is known to the MDB it can get a reference to the SipApplicationSession object using this method. If this MDB were in a different application then it would not possible for it to access the SipApplicationSession. The method returns null in case the container does not find the SipApplicationSession instance matching the ID.
     * 
     * @param isContainerManaged whether or not the container should bind and lock the sip application session in the same thread as the one that created it
     * @return SipApplicationSession object
     * @since 7.0.2
     */
    javax.servlet.sip.SipApplicationSession getApplicationSessionById(java.lang.String applicationSessionId, boolean isContainerManaged);
	/**
     * Returns the SipApplicationSession for a given session applicationSessionKey. 
     * The applicationSessionKey String is the same as that supplied to SipFactory#createApplicationSessionByKey. 
     * The method shall return the Application Session only if the queried application session 
     * belongs to the application from where this method is invoked. 
     * The method returns null in case the container does not find the SipApplicationSession instance 
     * matching the applicationSessionKey.  
     * @param applicationSessionKey session applicationSessionKey of the SipApplicationSession
     * @param create controls whether new session should be created upon lookup failure
     * @param isContainerManaged whether or not the container should bind and lock the sip application session in the same thread as the one that created it 
     * @return SipApplicationSession object or a null if it is not found and create is set to false. If create  is true, create a new SipApplicationSession with the given applicationSessionKey
     * @throws NullPointerException if the applicationSessionKey is null.
     * @since 7.0.2
     */
    SipApplicationSession getApplicationSessionByKey(java.lang.String applicationSessionKey, boolean create, boolean isContainerManaged);   
    
    /**
	 * This method allows an application to access a SipSession in an asynchronous manner. 
	 * This method is useful for accessing the SipSession from Web or EJB modules in a converged application 
	 * or from unmanaged threads started by the application itself.
	 * 
     * When this API is used in conjunction with the Mobicents Concurrency Control in SipSession mode, 
     * the container guarantees that the business logic contained within the SipSessionAsynchronousWork
     * will be executed in a thread-safe manner. 
     * 
     * It has to be noted that the work may never execute if the session gets invalidated in the meantime
     * and the work will be executed locally on the node on a cluster.
     * 
	 * @param work the work to be performed on this SipSession. 
	 */
    void scheduleAsynchronousWork(String sipSessionId, SipSessionAsynchronousWork work);
    
    /**
	 * This method allows an application to access a SipApplicationSession in an asynchronous manner. 
	 * This method is useful for accessing the SipApplicationSession from Web or EJB modules in a converged application 
	 * or from unmanaged threads started by the application itself.
	 * 
     * When this API is used in conjunction with the Mobicents Concurrency Control in SipApplicationSession mode, 
     * the container guarantees that the business logic contained within the SipApplicationSessionAsynchronousWork
     * will be executed in a thread-safe manner. 
     * 
     * It has to be noted that the work may never execute if the session gets invalidated in the meantime
     * and the work will be executed locally on the node on a cluster.
     *     
	 * @param work the work to be performed on this SipApplicationSession. 
	 */
    void scheduleAsynchronousWork(String sipApplicationSessionId, SipApplicationSessionAsynchronousWork work);
}
