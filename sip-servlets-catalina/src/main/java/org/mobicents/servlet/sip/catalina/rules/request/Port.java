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

package org.mobicents.servlet.sip.catalina.rules.request;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

/**
 * @author Thomas Leseney
 */
public class Port implements Extractor {
	
	public Port(String token) {
		if (!token.equals("uri")) {
			throw new IllegalArgumentException("Invalid expression: port after " + token);
		}
	}
	
	public Object extract(Object input) {
		URI uri = (URI) input;
		if (uri.isSipURI()) {
	        SipURI sipuri = (SipURI) uri;
	        int port = sipuri.getPort();
	        if (port < 0) {
	            String scheme = sipuri.getScheme();
	            if (scheme.equals("sips")) {
	                return "5061"; 
	            } else {
	                return "5060";
	            }
	        } else {
	            return Integer.toString(port);
	        }
	    } else {
	        return null;
	    }
	}
}
