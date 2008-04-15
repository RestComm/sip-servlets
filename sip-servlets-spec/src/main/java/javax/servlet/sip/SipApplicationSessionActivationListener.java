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

import java.util.EventListener;

/**
 * Objects that are bound to a SipApplicationSession may listen to container events 
 * notifying them when the application session to which they are bound will 
 * be passivated or activated. 
 * A container that migrates application sessions between VMs or persists 
 * them is required to notify all attributes implementing this listener and 
 * that are bound to those application sessions of the events.
 */
public interface SipApplicationSessionActivationListener extends EventListener {
	/**
	 * Notification that the application session is about to be passivated.
	 * @param se event identifying the application session about to be persisted
	 * @since 1.1
	 */
	void sessionWillPassivate(SipApplicationSessionEvent se);
	/**
	 * Notification that the application session has just been activated.
	 * @param se event identifying the activated application session
	 * @since 1.1
	 */
	void sessionDidActivate(SipApplicationSessionEvent se);
}
