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

package org.mobicents.servlet.sip.undertow.rules.request;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletRequest;

/**
 * @author Thomas Leseney
 *
 *         This class is based on the contents of org.mobicents.servlet.sip.catalina.rules.request package from sip-servlet-as7
 *         project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class Uri implements Extractor {

    private static final int REQUEST = 1;
    private static final int ADDRESS = 2;

    private int inputType;

    public Uri(String token) {
        if (token.equals("request")) {
            inputType = REQUEST;
        } else if (token.equals("from")) {
            inputType = ADDRESS;
        } else if (token.equals("to")) {
            inputType = ADDRESS;
        } else {
            throw new IllegalArgumentException("Invalid expression: uri after " + token);
        }
    }

    public Object extract(Object input) {
        if (inputType == REQUEST) {
            return ((SipServletRequest) input).getRequestURI();
        } else {
            return ((Address) input).getURI();
        }
    }
}
