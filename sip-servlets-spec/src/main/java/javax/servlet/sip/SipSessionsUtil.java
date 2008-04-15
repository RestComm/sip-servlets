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
package javax.servlet.sip;
/**
 * A utility class providing additional support for converged HTTP/SIP applications and converged J2EE/SIP applications.
 * This class can be accessed through the ServletContext parameter named javax.servlet.sip.sessionsutil or it can be injected using the annotation @SipSessionsUtil.
 * Since: 1.1
 */
public interface SipSessionsUtil{
    /**
     * Returns the SipApplicationSession for a given applicationSessionId. The applicationSessionId String is the same as that obtained through SipApplicationSession.getId(). The method shall return the Application Session only if the queried application session belongs to the application from where this method is invoked. As an example if there exists a SIP Application with some J2EE component like a Message Driven Bean, bundled in the same application archive file (.war), then if the id of SipApplicationSession is known to the MDB it can get a reference to the SipApplicationSession object using this method. If this MDB were in a different application then it would not possible for it to access the SipApplicationSession. The method returns null in case the container does not find the SipApplicationSession instance matching the ID.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession(java.lang.String applicationSessionId);

}
