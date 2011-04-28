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

package javax.servlet.sip;

import java.util.Iterator;

/**
 * Base interface for any type of URI. These are used in the request line of SIP requests to identify the callee and also in Contact, From, and To headers.
 * The only feature common to all URIs is that they can be represented as strings beginning with a token identifying the scheme of the URI followed by a colon followed by a scheme-specific part.
 * The generic syntax of URIs is defined in RFC 2396.
 */
public interface URI extends java.lang.Cloneable{
	/**
	 * Returns a clone of this URI.
	 * @return URI a clone of this URI object
	 */
	URI clone();
    /**
     * Compares the given URI with this URI. The comparison rules to be followed shall depend upon the underlying URI scheme being used. For general purpose URIs RFC 2396 should be consulted for equality. If the URIs are of scheme for which comparison rules are further specified in their specications, then they must be used for any comparison.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the value of the named parameter, or null if it is not set. A zero-length String indicates flag parameter.
     */
    java.lang.String getParameter(java.lang.String key);

    /**
     * Returns an Iterator over the names of all parameters present in this URI.
     */
    Iterator<java.lang.String> getParameterNames();

    /**
     * Returns the scheme of this URI, for example "sip", "sips" or "tel".
     */
    java.lang.String getScheme();

    /**
     * Returns true if the scheme is "sip" or "sips", false otherwise.
     */
    boolean isSipURI();

    /**
     * Removes the named parameter from this URL. Nothing is done if the URL did not already contain the specific parameter.
     */
    void removeParameter(java.lang.String name);

    /**
     * Sets the value of the named parameter. If this URL previously contained a value for the given parameter name, then the old value is replaced by the specified value. The setting of a flag parameter is indicated by specifying a zero-length String for the parameter value.
     */
    void setParameter(java.lang.String name, java.lang.String value);

    /**
     * Returns the value of this URI as a String. The result must be appropriately URL escaped.
     */
    java.lang.String toString();

}
