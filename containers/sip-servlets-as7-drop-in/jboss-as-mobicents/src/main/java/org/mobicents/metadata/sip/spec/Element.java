/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.mobicents.metadata.sip.spec;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Bailey
 */
public enum Element {
    // must be first
    UNKNOWN(null),
    AND("and"),
    APPLICATION_NAME("app-name"),
    AUTH_CONSTRAINT("auth-constraint"),
    AUTH_METHOD("auth-method"),
    CONDITION("condition"),
    CONTAINS("contains"),
    CONTEXT_PARAM("context-param"),
    DESCRIPTION("description"),
    DISPLAY_NAME("display-name"),
    DISTRIBUTABLE("distributable"),
    EQUAL("equal"),
    EXISTS("exists"),
    IDENTITY_ASSERTION("identity-assertion"),
    LISTENER("listener"),
    LOCALE_ENCODING_MAPPING_LIST("locale-encoding-mapping-list"),
    LOGIN_CONFIG("login-config"),
    MAIN_SERVLET("main-servlet"),
    MESSAGE_DESTINATION("message-destination"),
    NOT("not"),
    OR("or"),
    PATTERN("pattern"),
    PROXY_AUTHENTICATION("proxy-authentication"),
    PROXY_CONFIG("proxy-config"),
    PROXY_TIMEOUT("proxy-timeout"),
    REALM_NAME("realm-name"),
    RESOURCE_COLLECTION("resource-collection"),
    RESOURCE_NAME("resource-name"),
    SECURITY_CONSTRAINT("security-constraint"),
    SECURITY_ROLE("security-role"),
    SERVLET_MAPPING("servlet-mapping"),
    SERVLET_NAME("servlet-name"),
    SERVLET_SELECTION("servlet-selection"),
    SERVLET("servlet"),
    SESSION_CONFIG("session-config"),
    SIP_METHOD("sip-method"),
    SUBDOMAIN_OF("subdomain-of"),
    USER_DATA_CONSTRAINT("user-data-constraint"),
    VALUE("value"),
    VAR("var"),
    ;

    private final String name;

    Element(final String name) {
        this.name = name;
    }

    /**
     * Get the local name of this element.
     *
     * @return the local name
     */
    public String getLocalName() {
        return name;
    }

    private static final Map<String, Element> MAP;

    static {
        final Map<String, Element> map = new HashMap<String, Element>();
        for (Element element : values()) {
            final String name = element.getLocalName();
            if (name != null)
                map.put(name, element);
        }
        MAP = map;
    }

    public static Element forName(String localName) {
        final Element element = MAP.get(localName);
        return element == null ? UNKNOWN : element;
    }
}
