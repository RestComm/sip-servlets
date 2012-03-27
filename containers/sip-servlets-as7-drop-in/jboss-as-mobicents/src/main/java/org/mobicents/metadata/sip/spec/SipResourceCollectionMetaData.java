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
