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

import io.undertow.servlet.api.LoginConfig;

import java.util.HashMap;

import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;

/**
 *
 * This class is based on org.mobicents.servlet.sip.catalina.SipLoginConfig class from sip-servlet-as7 project, re-implemented
 * for jboss as8 (wildfly) by:
 *
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipLoginConfig extends LoginConfig implements MobicentsSipLoginConfig {
    private static final long serialVersionUID = 1L;

    private HashMap<String, String> identityAssertionSchemes = new HashMap<String, String>();

    public SipLoginConfig(String realmName, String loginPage, String errorPage) {
        super(realmName, loginPage, errorPage);
    }

    public SipLoginConfig(String realmName) {
        super(realmName);
    }

    public SipLoginConfig(String mechanismName, String realmName, String loginPage, String errorPage) {
        super(realmName, loginPage, errorPage);
        super.addFirstAuthMethod(mechanismName);
    }

    public SipLoginConfig(String mechanismName, final String realmName) {
        super(mechanismName, realmName, null, null);
    }

    public void addIdentityAssertion(String scheme, String support) {
        identityAssertionSchemes.put(scheme, support);
    }

    public String getIdentitySchemeSettings(String scheme) {
        return identityAssertionSchemes.get(scheme);
    }
}
