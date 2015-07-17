/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.servlet.sip.undertow.security;

import io.undertow.security.idm.Account;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SecurityRoleRef;
import io.undertow.servlet.api.ServletInfo;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import org.mobicents.servlet.sip.core.security.SipPrincipal;

/**
 *
 * This class is based org.mobicents.servlet.sip.catalina.security.CatalinaSipPrincipal class from sip-servlet-as7 project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class UndertowSipPrincipal implements SipPrincipal {

    private Account account;
    private Deployment deployment;
    private ServletInfo servletInfo;

    public UndertowSipPrincipal(Account account, Deployment deployment, ServletInfo servletInfo) {
        this.account = account;
        this.deployment = deployment;
        this.servletInfo = servletInfo;
    }

    /* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    public String getName() {
        return account.getPrincipal().getName();
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.security.SipPrincipal#isUserRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        final Map<String, Set<String>> principalVersusRolesMap = deployment.getDeploymentInfo().getPrincipalVersusRolesMap();
        final Set<String> roles = principalVersusRolesMap.get(account.getPrincipal().getName());

        for (SecurityRoleRef ref : servletInfo.getSecurityRoleRefs()) {
            if (ref.getRole().equals(role)) {
                if (roles != null && roles.contains(ref.getLinkedRole())) {
                    return true;
                }
                return account.getRoles().contains(ref.getLinkedRole());
            }
        }
        if (roles != null && roles.contains(role)) {
            return true;
        }
        return account.getRoles().contains(role);
    }

    @Override
    public Principal getPrincipal() {
        return account.getPrincipal();
    }
}
