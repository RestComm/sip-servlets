/*
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

package org.mobicents.servlet.sip.startup.loading.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.sip.SipServletRequest;

import org.mobicents.servlet.sip.startup.loading.rules.request.*;

/**
 * @author Thomas Leseney
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
			}
            else {
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
