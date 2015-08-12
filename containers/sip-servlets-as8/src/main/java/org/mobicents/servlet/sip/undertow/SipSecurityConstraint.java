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
package org.mobicents.servlet.sip.undertow;

import io.undertow.servlet.api.SecurityConstraint;

/**
 * This class is based on org.mobicents.servlet.sip.catalina.SipSecurityConstraint class from sip-servlet-as7 project,
 * re-implemented for jboss as8 (wildfly) by:
 *
 * @author kakonyi.istvan@alerant.hu
 */
public class SipSecurityConstraint extends SecurityConstraint {
    private static final long serialVersionUID = 1L;
    public boolean proxyAuthentication;

    private String displayName;

    /**
     * @return the proxyAuthentication
     */
    public boolean isProxyAuthentication() {
        return proxyAuthentication;
    }

    /**
     * @param proxyAuthentication the proxyAuthentication to set
     */
    public void setProxyAuthentication(boolean proxyAuthentication) {
        this.proxyAuthentication = proxyAuthentication;
    }

    public void addCollection(SipSecurityCollection sipSecurityCollection) {
        super.addWebResourceCollection(sipSecurityCollection);
    }

    public void removeCollection(SipSecurityCollection sipSecurityCollection) {
        super.getWebResourceCollections().remove(sipSecurityCollection);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
