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
 * Notification that a SipApplicationSession has expired.
 * A SipApplicationSessionListener receiving this notification may attempt to extend the lifetime of the application instance corresponding to the expiring application session by invoking SipApplicationSession.setExpires(int).
 * @since: 1.1 
 */
public class SipApplicationSessionEvent extends java.util.EventObject{
    /**
     * Creates a new SipApplicationSessionEvent object.
     * @param appSession the expired application session
     */
    public SipApplicationSessionEvent(javax.servlet.sip.SipApplicationSession appSession){
    	super(appSession);
    }

    /**
     * Returns the expired session object.
     */
    public javax.servlet.sip.SipApplicationSession getApplicationSession(){
        return (SipApplicationSession)getSource(); 
    }

}
