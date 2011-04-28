/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package javax.servlet.sip;
/**
 * A utility class providing additional support for converged HTTP/SIP applications and converged J2EE/SIP applications.
 * This class can be accessed through the ServletContext parameter named 
 * javax.servlet.sip.SipSessionsUtil or 
 * it can be injected using the @Resource annotation.
 * Since: 1.1
 */
public interface SipSessionsUtil{
    /**
     * Returns the SipApplicationSession for a given applicationSessionId. The applicationSessionId String is the same as that obtained through SipApplicationSession.getId(). The method shall return the Application Session only if the queried application session belongs to the application from where this method is invoked. As an example if there exists a SIP Application with some J2EE component like a Message Driven Bean, bundled in the same application archive file (.war), then if the id of SipApplicationSession is known to the MDB it can get a reference to the SipApplicationSession object using this method. If this MDB were in a different application then it would not possible for it to access the SipApplicationSession. The method returns null in case the container does not find the SipApplicationSession instance matching the ID.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSessionById(java.lang.String applicationSessionId);
    
    /**
     * Returns the SipApplicationSession for a given session applicationSessionKey. 
     * The applicationSessionKey String is the same as that supplied to SipFactory#createApplicationSessionByKey. 
     * The method shall return the Application Session only if the queried application session 
     * belongs to the application from where this method is invoked. 
     * The method returns null in case the container does not find the SipApplicationSession instance 
     * matching the applicationSessionKey.  
     * @param applicationSessionKey session applicationSessionKey of the SipApplicationSession
     * @param create controls whether new session should be created upon lookup failure 
     * @return SipApplicationSession object or a null if it is not found and create is set to false. If create  is true, create a new SipApplicationSession with the given applicationSessionKey
     * @throws NullPointerException if the applicationSessionKey is null.
     */
    SipApplicationSession getApplicationSessionByKey(java.lang.String applicationSessionKey, boolean create);
    
    /**
     *     Returns related SipSession. This method is helpful when the application code wants to carry out session join or replacement as described by RFC 3911 and RFC 3891 respectively.
     *     The association is made implicitly by the container implementation. An example is shown below.
     *     
     *     @Resource
     *     SipSessionsUtil sipSessionsUtil;
     *     protected void doInvite(SipServletRequest req) {
     *     		SipSession joining = req.getSession(true);
     *     		SipSession beingJoined = sipSessionsUtil.getCorrespondingSipSession(
     *                     joining,"Join");	
     *          [...]
     *     }
     * @param session  one of the two related SIP sessions. For example, it can be the joining session or the replacing session.
     * @param headerName the header name through which the association is made. For example, for RFC 3911, it is Join, for RFC 3891, it is Replaces
     * @return SipSession related to the supplied session. For RFC 3911, if joining session is passed in, the session being joined is returned. For RFC 3891, if the replacing session is passed in, the session being replaced is returned. If none is found, this method returns null.
     */
    SipSession getCorrespondingSipSession(SipSession session, java.lang.String headerName);
}
