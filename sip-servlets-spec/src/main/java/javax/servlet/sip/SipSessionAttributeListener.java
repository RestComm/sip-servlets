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
 * This listener interface can be implemented in order to get notifications of changes to the attribute lists of sessions within this SIP servlet application.
 */
public interface SipSessionAttributeListener extends java.util.EventListener{
    /**
     * Notification that an attribute has been added to a session. Called after the attribute is added.
     */
    void attributeAdded(javax.servlet.sip.SipSessionBindingEvent ev);

    /**
     * Notification that an attribute has been removed from a session. Called after the attribute is removed.
     */
    void attributeRemoved(javax.servlet.sip.SipSessionBindingEvent ev);

    /**
     * Notification that an attribute has been replaced in a session. Called after the attribute is replaced.
     */
    void attributeReplaced(javax.servlet.sip.SipSessionBindingEvent ev);

}
