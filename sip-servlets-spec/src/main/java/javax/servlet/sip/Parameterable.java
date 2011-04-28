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
import java.util.Map;
import java.util.Set;

/**
 * The Parameterable interface is used to indicate a SIP header field value with optional parameters. All of the Address header fields are Parameterable, including Contact, From, To, Route, Record-Route, and Reply-To. In addition, the header fields Accept, Accept-Encoding, Alert-Info, Call-Info, Content-Disposition, Content-Type, Error-Info, Retry-After and Via are also Parameterable.
 * @since 1.1
 */
public interface Parameterable extends java.lang.Cloneable{
    /**
     * Returns a clone of this Parameterable. The cloned Parameterable has identical field value and parameters.
     */
    java.lang.Object clone();

    /**
     * Compares the given Parameterable type with this one. The comparison rules to be used for the Parameterable comparison should be taken as specified in the underlying specifications. Most of the headers of Parameterable type are defined in RFC 3261, however for others their respective specifications should be consulted for comaprison.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the value of the named parameter, or null if it is not set. A zero-length String indicates a flag parameter.
     */
    java.lang.String getParameter(java.lang.String key);

    /**
     * Returns an Iterator of the names of all parameters contained in this object. The order is the order of appearance of the parameters in the Parameterable.
     */
    Iterator<java.lang.String> getParameterNames();

    /**
     * Returns a Collection view of the parameter name-value mappings contained in this Parameterable. The order is the order of appearance of the parameters in the Parameterable.
     */
    Set<Map.Entry<String,String>> getParameters();

    /**
     * Returns the field value as a string.
     */
    java.lang.String getValue();

    /**
     * Removes the named parameter from this object. Nothing is done if the object did not already contain the specific parameter.
     */
    void removeParameter(java.lang.String name);

    /**
     * Sets the value of the named parameter. If this object previously contained a value for the given parameter name, then the old value is replaced by the specified value. The setting of a flag parameter is indicated by specifying a zero-length String for the parameter value. Calling this method with null value is equivalent to calling
     */
    void setParameter(java.lang.String name, java.lang.String value);

    /**
     * Set the value of the field.
     */
    void setValue(java.lang.String value);

}
