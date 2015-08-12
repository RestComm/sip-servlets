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

import io.undertow.servlet.api.WebResourceCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is based on org.mobicents.servlet.sip.catalina.SipSecurityCollection class from sip-servlet-as7 project,
 * re-implemented for jboss as8 (wildfly) by:
 *
 * @author kakonyi.istvan@alerant.hu
 */
public class SipSecurityCollection extends WebResourceCollection {

    private static final long serialVersionUID = 1L;
    public List<String> servletNames = new ArrayList<String>();
    public List<String> sipMethods = new ArrayList<String>();

    private String name;
    private String description;

    /**
     *
     */
    public SipSecurityCollection() {
        super();
    }

    /**
     *
     * @param name
     * @param description
     */
    public SipSecurityCollection(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     *
     * @param name
     */
    public SipSecurityCollection(String name) {
        this.name = name;
    }

    /**
     *
     * @param servletName
     */
    public void addServletName(String servletName) {
        if (servletName == null) {
            return;
        }
        servletNames.add(servletName);
    }

    /**
     *
     * @param servletName
     */
    public void removeServletName(String servletName) {
        if (servletName == null) {
            return;
        }
        servletNames.remove(servletName);
    }

    public boolean findServletName(String servletName) {
        for (String servletNameFromList : servletNames) {
            if (servletNameFromList.equals(servletName))
                return true;
        }
        return false;
    }

    public boolean findMethod(String method) {
        for (String declaredMethods : sipMethods) {
            if (method.equals(declaredMethods))
                return true;
        }
        return false;
    }

    public String[] findServletNames() {
        String[] ret = new String[servletNames.size()];
        servletNames.toArray(ret);
        return ret;
    }

    /**
     *
     * @param sipMethod
     */
    public void addSipMethod(String sipMethod) {
        if (sipMethod == null) {
            return;
        }
        sipMethods.add(sipMethod);
    }

    /**
     *
     * @param sipMethod
     */
    public void removeSipMethod(String sipMethod) {
        if (sipMethod == null) {
            return;
        }
        sipMethods.remove(sipMethod);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}