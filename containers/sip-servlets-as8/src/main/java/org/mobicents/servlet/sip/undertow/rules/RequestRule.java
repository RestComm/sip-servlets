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

package org.mobicents.servlet.sip.undertow.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.sip.SipServletRequest;

import org.mobicents.servlet.sip.undertow.rules.request.DisplayName;
import org.mobicents.servlet.sip.undertow.rules.request.Extractor;
import org.mobicents.servlet.sip.undertow.rules.request.From;
import org.mobicents.servlet.sip.undertow.rules.request.Host;
import org.mobicents.servlet.sip.undertow.rules.request.Method;
import org.mobicents.servlet.sip.undertow.rules.request.Param;
import org.mobicents.servlet.sip.undertow.rules.request.Port;
import org.mobicents.servlet.sip.undertow.rules.request.Scheme;
import org.mobicents.servlet.sip.undertow.rules.request.Tel;
import org.mobicents.servlet.sip.undertow.rules.request.To;
import org.mobicents.servlet.sip.undertow.rules.request.Uri;
import org.mobicents.servlet.sip.undertow.rules.request.User;
import org.mobicents.servlet.sip.core.descriptor.MatchingRule;

/**
 * @author Thomas Leseney
 *
 *         This class is based on the contents of org.mobicents.servlet.sip.catalina.rules package from sip-servlet-as7 project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public abstract class RequestRule implements MatchingRule {

    private String varName;
    private List<Extractor> extractors;

    protected RequestRule(String varName) {
        this.varName = varName;
        extractors = new ArrayList<Extractor>();
        StringTokenizer st = new StringTokenizer(varName, ".");
        String lastToken = st.nextToken();
        if (!lastToken.equals("request")) {
            throw new IllegalArgumentException("Expression does not start with request: " + varName);
        }

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("from")) {
                extractors.add(new From(lastToken));
            } else if (token.equals("uri")) {
                extractors.add(new Uri(lastToken));
            } else if (token.equals("method")) {
                extractors.add(new Method(lastToken));
            } else if (token.equals("user")) {
                extractors.add(new User(lastToken));
            } else if (token.equals("scheme")) {
                extractors.add(new Scheme(lastToken));
            } else if (token.equals("host")) {
                extractors.add(new Host(lastToken));
            } else if (token.equals("port")) {
                extractors.add(new Port(lastToken));
            } else if (token.equals("tel")) {
                extractors.add(new Tel(lastToken));
            } else if (token.equals("display-name")) {
                extractors.add(new DisplayName(lastToken));
            } else if (token.equals("to")) {
                extractors.add(new To(lastToken));
            } else if (token.equals("param")) {
                if (!st.hasMoreTokens()) {
                    throw new IllegalArgumentException("No param name: " + varName);
                }
                String param = st.nextToken();
                extractors.add(new Param(lastToken, param));
                if (st.hasMoreTokens()) {
                    throw new IllegalArgumentException("Invalid var: " + st.nextToken() + " in " + varName);
                }
            } else {
                throw new IllegalArgumentException("Invalid property: " + token + " in " + varName);
            }
            lastToken = token;
        }
    }

    public String getValue(SipServletRequest request) {
        Object o = request;
        for (Extractor e : extractors) {
            o = e.extract(o);
            if (o == null) {
                return null;
            }
        }
        return o.toString();
    }

    public String getVarName() {
        return varName;
    }
}
