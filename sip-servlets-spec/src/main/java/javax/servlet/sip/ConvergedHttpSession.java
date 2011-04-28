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
 * The ConvergedHttpSession class provides access to HttpSession related functionality which is only present in a converged HTTP/SIP container. In a converged container, an instance of HttpSession can be cast to ConvergedHttpSession in order to access methods available available only to converged applications.
 * Since: 1.1
 */
public interface ConvergedHttpSession extends javax.servlet.http.HttpSession{
    /**
     * This method encodes the HTTP URL with the jsessionid. ";jsessionid=http-session-id". The URL parameter should be an absolute URL. For example, http://server:7001/mywebapp/foo.jsp. Where "/mywebapp" is the context path of the the current ServletContext, because that is where the httpSession belongs to.
     */
    java.lang.String encodeURL(java.lang.String url);

    /**
     * Converts the given relative path to an absolute URL by prepending the contextPath for the current ServletContext, the given scheme ("http" or "https"), and the host:port, and then encoding the resulting URL with the jsessionid.
     * For example, this method converts: from: "/foo.jsp" to: "http://server:8888/mywebapp/foo.jsp;jsessionid=http-session-id" Where, "/mywebapp" is the contextPath for the current ServletContext server is the front end host defined for the web server.
     */
    java.lang.String encodeURL(java.lang.String relativePath, java.lang.String scheme);

    /**
     * Returns the parent SipApplicationSession if it exists, if none exists then a new one is created and returned after associating it with the converged http session.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession();

}
