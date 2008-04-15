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
 * Implementations of this interface can receive notifications about invalidated and/or activated SipApplicationSession objects in the SIP application they are part of. To receive notification events, the implementation class must be configured in the deployment descriptor for the servlet application.
 */
public interface SipApplicationSessionListener extends java.util.EventListener{
    /**
     * Notification that a session was created.
     */
    void sessionCreated(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that a session was invalidated. Either it timed out or it was explicitly invalidated. It is not possible to extend the application sessions lifetime.
     */
    void sessionDestroyed(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that the application session has just been activated.
     */
    void sessionDidActivate(javax.servlet.sip.SipApplicationSessionEvent se);

    /**
     * Notification that an application session has expired. The application may request an extension of the lifetime of the application session by invoking
     * .
     */
    void sessionExpired(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that the application session is about to be passivated.
     */
    void sessionWillPassivate(javax.servlet.sip.SipApplicationSessionEvent se);

}
