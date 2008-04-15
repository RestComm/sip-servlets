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
 * Containers are required to invoke init() on the servlets before the servlets are ready for service. The servlet can only be used after succesful initialization. Since SIP is a peer-to-peer protocol and some servlets may act as UACs, the container is required to let the servlet know when it is succesfully initialized by invoking SipServletListener.
 * Since: 1.1 See Also:SipServletContextEvent
 */
public interface SipServletListener{
    /**
     * Notification that the servlet was succesfully initialized
     */
    void servletInitialized(javax.servlet.sip.SipServletContextEvent ce);

}
