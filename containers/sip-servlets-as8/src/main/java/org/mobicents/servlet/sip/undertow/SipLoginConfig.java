/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
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
package org.mobicents.servlet.sip.undertow;

import io.undertow.servlet.api.LoginConfig;

import java.util.HashMap;

import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
/**
 *
 * This class is based on  org.mobicents.servlet.sip.catalina.SipLoginConfig class from sip-servlet-as7 project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipLoginConfig extends LoginConfig implements MobicentsSipLoginConfig {
    private static final long serialVersionUID = 1L;

    private HashMap<String, String> identityAssertionSchemes = new HashMap<String, String>();

    public SipLoginConfig(String realmName, String loginPage, String errorPage) {
        super(realmName,loginPage,errorPage);
    }

    public SipLoginConfig(String realmName) {
        super(realmName);
    }

    public SipLoginConfig(String mechanismName, String realmName, String loginPage, String errorPage) {
        super(realmName,loginPage,errorPage);
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
