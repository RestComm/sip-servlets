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
/**
 * Represents tel URLs as defined by RFC 3966. Tel URLs represent telephone numbers. SIP servlet containers may be able to route requests based on tel URLs but are not required to.
 * See Also:RFC 3966
 */
public interface TelURL extends javax.servlet.sip.URI{
    /**
     * Compares the given TelURL with this TelURL. The comparison rules to be followed must be as specified in RFC 3966 section 5.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the phone number of this TelURL. The returned string includes any visual separators present in the phone number part of the URL but does not include a leading "+" for global tel URLs.
     */
    java.lang.String getPhoneNumber();

    /**
     * Returns true if this TelURL is global, and false otherwise.
     */
    boolean isGlobal();

    /**
     * Sets the (global) phone number of this TelURL. The specified number must be a valid global number for the "tel" scheme as described in RFC3966 (URLs for Telephone Calls). The following usage of this method will result in valid global phone number: setPhoneNumber("+1-201-555-0123")
     * @param number the new global phone number 
     * @throws IllegalArgumentException if the phone number was invalid according to validation rules specified in RFC3966
     * @since 1.1 
     */
    void setPhoneNumber(java.lang.String number);

    /**
     * Sets the (local) phone number of this TelURL. The specified number must be a local phone number for the "tel" scheme as described in RFC3966 (URLs for Telephone Calls). The following usage of this method will result in a valid local phone number: setPhoneNumber("7042","example.com")
     * @param number the new local phone number
     * @param phoneContext the phone-context parameter of this TelURI 
     * @throws IllegalArgumentException if the phone number was invalid according to validation rules specified in RFC3966
     * @since 1.1
     */
    void setPhoneNumber(java.lang.String number, java.lang.String phoneContext);
    
    /**
     * Returns the String representation of this TelURL. Any reserved characters will be properly escaped according to RFC2396.
     */
    java.lang.String toString();

    /**
     * Returns the phone context of this TelURL for local numbers or null if the phone number is global 
     * @return the phone-context of this TelURL for local numbers or null if the phone number is global
     * @since 1.1
     */
    java.lang.String getPhoneContext();
}
