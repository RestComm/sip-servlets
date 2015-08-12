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
package org.mobicents.metadata.sip.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.metadata.javaee.support.NamedMetaDataWithDescriptions;

/**
 * @author jean.deruelle@gmail.com
 * @version $Revision$
 *
 *          This class is based on the contents of org.mobicents.metadata.sip.spec package from jboss-as7-mobicents project,
 *          re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class SipResourceCollectionMetaData extends NamedMetaDataWithDescriptions {
    private static final long serialVersionUID = 1;

    /** The set of all sip methods: */
    public static final Set<String> ALL_SIP_METHODS;
    public static final String[] ALL_SIP_METHOD_NAMES;

    static {
        TreeSet<String> tmp = new TreeSet<String>();
        tmp.add("INVITE");
        tmp.add("ACK");
        tmp.add("BYE");
        tmp.add("MESSAGE");
        tmp.add("INFO");
        tmp.add("OPTIONS");
        tmp.add("SUBSCRIBE");
        tmp.add("PUBLISH");
        tmp.add("REFER");
        tmp.add("PRACK");
        ALL_SIP_METHODS = Collections.unmodifiableSortedSet(tmp);
        ALL_SIP_METHOD_NAMES = new String[ALL_SIP_METHODS.size()];
        ALL_SIP_METHODS.toArray(ALL_SIP_METHOD_NAMES);
    }

    private List<String> servletNames = new ArrayList<String>();
    private List<String> sipMethods = new ArrayList<String>();

    /**
     * Get sip methods in ALL_SIP_METHODS not in the argument sipMethods.
     *
     * @param sipMethods a set of sip method names
     * @return possibly empty sip methods in ALL_SIP_METHODS not in sipMethods.
     */
    public static String[] getMissingSipMethods(Collection<String> sipMethods) {
        String[] methods = {};
        if (sipMethods.size() > 0 && sipMethods.containsAll(ALL_SIP_METHODS) == false) {
            HashSet<String> missingMethods = new HashSet<String>(ALL_SIP_METHODS);
            missingMethods.removeAll(sipMethods);
            methods = new String[missingMethods.size()];
            missingMethods.toArray(methods);
        }
        return methods;
    }

    public String getResourceName() {
        return getName();
    }

    public void setResourceName(String ResourceName) {
        super.setName(ResourceName);
    }

    public List<String> getServletNames() {
        return servletNames;
    }

    public void setServletNames(List<String> servletNames) {
        this.servletNames = servletNames;
    }

    public List<String> getSipMethods() {
        return sipMethods;
    }

    public void setSipMethods(List<String> sipMethods) {
        this.sipMethods = sipMethods;
    }
}
