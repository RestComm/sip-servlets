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

package org.mobicents.servlet.sip.catalina.rules;

import javax.servlet.sip.SipServletRequest;

import org.mobicents.servlet.sip.core.descriptor.MatchingRule;

/**
 * @author Thomas Leseney
 */
public class SubdomainRule extends RequestRule implements MatchingRule {
	
    private String value;
    
    public SubdomainRule(String var, String value) {
        super(var);
        this.value = value;
    }
    
    public boolean matches(SipServletRequest request) {
    	String requestValue = getValue(request);
    	if (requestValue == null) {
    		return false;
    	}
    	if (requestValue.endsWith(value)) {
    		int len1 = requestValue.length();
    	    int len2 = value.length();
    	    return (len1 == len2 || (requestValue.charAt(len1-len2-1) == '.'));
    	}
    	return false;
    }

    public String getExpression() {
        return "(" + getVarName() + " subdomainOf " + value + ")";
    }
}
