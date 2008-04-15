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
 * Events of this type are either sent to an object that implements SipSessionBindingListener when it is bound or unbound from a session, or to a SipSessionAttributeListener that has been configured in the deployment descriptor when any attribute is bound, unbound or replaced in a session.
 * The session binds the object by a call to SipSession.setAttribute and unbinds the object by a call to SipSession.removeAttribute.
 * @see SipSession, SipSessionBindingListener, SipSessionAttributeListener
 */
public class SipSessionBindingEvent extends java.util.EventObject{
	private String name;
    /**
     * Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the event, the object must implement
     * .
     * @param session the session to which the object is bound or unboundname - the name with which the object is bound or unbound
     */
    public SipSessionBindingEvent(javax.servlet.sip.SipSession session, java.lang.String name){
         super(session);
         this.name = name;
    }

    /**
     * Returns the name with which the object is bound to or unbound from the session.
     */
    public java.lang.String getName(){
        return name; 
    }

    /**
     * Returns the session to or from which the object is bound or unbound.
     */
    public javax.servlet.sip.SipSession getSession(){
        return (SipSession)getSource(); 
    }

}
