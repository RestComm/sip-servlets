/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
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
 * This class is based org.mobicents.servlet.sip.catalina.security.CatalinaSipPrincipal class from sip-servlet-as7 project,
 * re-implemented for jboss as8 (wildfly) by:
 *
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

    /*
     * (non-Javadoc)
     *
     * @see java.security.Principal#getName()
     */
    public String getName() {
        return account.getPrincipal().getName();
    }

    /*
     * (non-Javadoc)
     *
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
