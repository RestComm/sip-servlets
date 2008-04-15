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
    javax.servlet.sip.SipApplicationSession getApplicationSession(java.lang.String applicationSessionId);

}
