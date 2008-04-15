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

import java.util.List;

/**
 * This interface class specifies the API between the container and the application router.
 * Since: 1.1
 */
public interface SipApplicationRouter{
    /**
     * Container notifies application router that new applications are deployed.
     */
    void applicationDeployed(List<String> newlyDeployedApplicationNames);

    /**
     * Container notifies application router that some applications are undeployed.
     */
    void applicationUndeployed(List<String> undeployedApplicationNames);

    /**
     * Container calls this method when it finishes using this application router.
     */
    void destroy();

    /**
     * This method is called by the container when a servlet sends or proxies an initial SipServletRequest. The application router returns a set of information. See @{link SipApplicationRouterInfo} for details.
     */
    javax.servlet.sip.SipApplicationRouterInfo getNextApplication(javax.servlet.sip.SipServletRequest initialRequest, javax.servlet.sip.SipApplicationRoutingRegion region, javax.servlet.sip.SipApplicationRoutingDirective directive, java.io.Serializable stateInfo);

    /**
     * Initializes the SipApplicationRouter.
     */
    void init();

    /**
     * Container calls this method to initialize the application router, with applications that are currently deployed.
     */
    void init(List<String> deployedApplicationNames);

}
