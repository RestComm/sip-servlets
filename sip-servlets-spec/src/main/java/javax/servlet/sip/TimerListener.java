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
 * Listener interface implemented by SIP servlet applications using timers.
 * The application specifies an implementation of this interface in a listener element of the SIP deployment descriptor. There may be at most one TimerListener defined.
 * See Also:TimerService
 */
public interface TimerListener extends java.util.EventListener{
    /**
     * Notifies the listener that the specified timer has expired.
     */
    void timeout(javax.servlet.sip.ServletTimer timer);

}
