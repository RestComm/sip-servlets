/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package javax.servlet.sip.ar;

import java.util.List;
import java.util.Properties;

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
    javax.servlet.sip.ar.SipApplicationRouterInfo getNextApplication(javax.servlet.sip.SipServletRequest initialRequest, javax.servlet.sip.ar.SipApplicationRoutingRegion region, javax.servlet.sip.ar.SipApplicationRoutingDirective directive, SipTargetedRequestInfo targetedRequestInfo, java.io.Serializable stateInfo);

    /**
     * Initializes the SipApplicationRouter.
     */
    void init();

    /**
     * Container calls this method to initialize the application router, with applications that are currently deployed.
     */
    void init(Properties properties);

}
