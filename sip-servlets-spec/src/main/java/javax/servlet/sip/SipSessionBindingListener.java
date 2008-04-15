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
 * Causes an object to be notified when it is bound to or unbound from a SipSession. The object is notified by an SipSessionBindingEvent object. This may be as a result of a servlet programmer explicitly unbinding an attribute from a session, due to a session being invalidated, or due to a session timing out.
 * See Also:SipSession, SipSessionBindingEvent
 */
public interface SipSessionBindingListener extends java.util.EventListener{
    /**
     * Notifies the object that it is being bound to a session and identifies the session.
     */
    void valueBound(javax.servlet.sip.SipSessionBindingEvent event);

    /**
     * Notifies the object that it is being unbound from a session and identifies the session.
     */
    void valueUnbound(javax.servlet.sip.SipSessionBindingEvent event);

}
