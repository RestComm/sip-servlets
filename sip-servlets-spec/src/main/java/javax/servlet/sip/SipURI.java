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
 * Represents sip and sips URIs.
 * SIP and SIPS URIs are used for addressing. They are similar to email addresses in that they are of the form user@host where user is either a user name or telephone number, and host is a host or domain name, or a numeric IP address. Additionally, SIP and SIPS URIs may contain parameters and headers (although headers are not legal in all contexts).
 * Syntactically, SIP and SIPS URIs are identical except for the name of URI scheme. The semantics differs in that the SIPS scheme implies that the identified resource is to be contacted using TLS. The following quote is from RFC 3261:
 * Because sip and sips URIs are syntactically identical and because they're used the same way, they're both represented by the SipURI interface.
 * The string form of SIP and SIPS URIs may contain escaped characters. The SIP servlet container is responsible for unescaping those characters before presenting URIs to servlets. Likewise, string values passed to setters for various SIP(S) URI components may contain reserved or excluded characters that need escaping before being used. The container is responsible for escaping those values.
 * See Also:Address, SipFactory.createSipURI(java.lang.String, java.lang.String), SipServletRequest.getRequestURI()
 */
public interface SipURI extends javax.servlet.sip.URI{
    /**
     * Compares the given SipURI with this SipURI. The rules specified in section 19.1.4 RFC 3261 must be used for comparison.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the value of the specified header. SIP/SIPS URIs may specify headers. As an example, the URI sip:joe@example.com?Priority=emergency has a header "Priority" whose value is "emergency".
     */
    java.lang.String getHeader(java.lang.String name);

    /**
     * Returns an Iterator over the names of all headers present in this SipURI.
     */
    Iterator<java.lang.String> getHeaderNames();

    /**
     * Returns the host part of this SipURI.
     */
    java.lang.String getHost();

    /**
     * Returns true if the "lr" flag parameter is set, and false otherwise. This is equivalent to "".equals(getParameter("lr")).
     */
    boolean getLrParam();

    /**
     * Returns the value of the "maddr" parameter, or null if this is not set. This is equivalent to getParameter("maddr").
     */
    java.lang.String getMAddrParam();

    /**
     * Returns the value of the "method" parameter, or null if this is not set. This is equivalent to getParameter("method").
     */
    java.lang.String getMethodParam();

    /**
     * Returns the port number of this SipURI, or -1 if this is not set.
     */
    int getPort();

    /**
     * Returns the value of the "transport" parameter, or null if this is not set. This is equivalent to getParameter("transport").
     */
    java.lang.String getTransportParam();

    /**
     * Returns the value of the "ttl" parameter, or -1 if this is not set. This method is equivalent to getParameter("ttl").
     */
    int getTTLParam();

    /**
     * Returns the user part of this SipURI.
     */
    java.lang.String getUser();

    /**
     * Returns the value of the "user" parameter, or null if this is not set. This is equivalent to getParameter("user").
     */
    java.lang.String getUserParam();

    /**
     * Returns the password of this SipURI, or null if this is not set.
     */
    java.lang.String getUserPassword();

    /**
     * Returns true if this SipURI is secure, that is, if this it represents a sips URI. For "ordinary" sip URIs, false is returned.
     */
    boolean isSecure();

    /**
     * Removes the value of the specified header in this SipURI.
     * @param name header name
     */
    void removeHeader(java.lang.String name);
    
    /**
     * Sets the value of the specified header in this SipURI.
     */
    void setHeader(java.lang.String name, java.lang.String value);

    /**
     * Sets the host part of this SipURI. This should be a fully qualified domain name or a numeric IP address.
     */
    void setHost(java.lang.String host);

    /**
     * Sets or removes the "lr" parameter depending on the value of the flag.
     */
    void setLrParam(boolean flag);

    /**
     * Sets the value of the "maddr" parameter. This is equivalent to setParameter("maddr", maddr).
     */
    void setMAddrParam(java.lang.String maddr);

    /**
     * Sets the value of the "method" parameter. This specifies which SIP method to use in requests directed at this SIP/SIPS URI.
     * This method is equivalent to setParameter("method", method).
     */
    void setMethodParam(java.lang.String method);

    /**
     * Sets the port number of this SipURI.
     */
    void setPort(int port);

    /**
     * Sets the scheme of this URI to sip or sips depending on whether the argument is true or not.
     */
    void setSecure(boolean b);

    /**
     * Sets the value of the "transport" parameter. This parameter specifies which transport protocol to use for sending requests and responses to this entity. The following values are defined: "udp", "tcp", "sctp", "tls", but other values may be used also
     * This method is equivalent to setParameter("transport", transport).
     */
    void setTransportParam(java.lang.String transport);

    /**
     * Sets the value of the "ttl" parameter. The ttl parameter specifies the time-to-live value when packets are sent using UDP multicast.
     * This is equivalent to setParameter("ttl", ttl).
     */
    void setTTLParam(int ttl);

    /**
     * Sets the user part of this SipURI.
     */
    void setUser(java.lang.String user);

    /**
     * Sets the value of the "user" parameter. This is equivalent to setParameter("user", user).
     */
    void setUserParam(java.lang.String user);

    /**
     * Sets the password of this SipURI. The use of passwords in SIP or SIPS URIs is discouraged as sending passwords in clear text is a security risk.
     */
    void setUserPassword(java.lang.String password);

    /**
     * Returns the String representation of this SipURI. Any reserved characters will be properly escaped according to RFC2396.
     */
    java.lang.String toString();

}
