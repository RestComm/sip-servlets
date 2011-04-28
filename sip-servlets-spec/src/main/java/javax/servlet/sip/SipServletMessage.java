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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

/**
 * Defines common aspects of SIP requests and responses.
 * The Servlet API is defined with an implicit assumption that servlets receives requests from clients, 
 * inspects various aspects of the corresponding ServletRequest object, and generates a response by setting various attributes 
 * of a ServletResponse object. 
 * This model fits HTTP well, because HTTP servlets always execute origin servers; 
 * they execute only to process incoming requests and never initiates HTTP requests of their own.
 * SIP services, on the other hand, does need to be able to initiate requests of their own. 
 * This implies that SIP request and response classes are more symmetric, that is, 
 * requests must be writable as well as readable, and likewise, responses must be readable as well as writable.
 * The SipServletMessage interface defines a number of methods which are common to SipServletRequest and SipServletResponse, 
 * for example setters and getters for message headers and content. 
 * System Headers 
 * 
 * Applications must not add, delete, or modify so-called "system" headers. 
 * These are header fields that the servlet container manages: 
 * From, To, Call-ID, CSeq, Via, Route (except through pushRoute), Record-Route. 
 * Contact is a system header field in messages other than REGISTER requests 
 * and responses, 3xx and 485 responses, and 200/OPTIONS responses. 
 * Additionally, for containers implementing the reliable provisional responses extension, 
 * RAck and RSeq are considered system headers also. Note that From and To are system header 
 * fields only with respect to their tags (i.e., tag parameters on these headers 
 * are not allowed to be modified but modifications are allowed to the other parts).  
 * 
 * Implicit Transaction State 
 * 
 * SipServletMessage objects always implicitly belong to a SIP transaction, 
 * and the transaction state machine (as defined by the SIP specification) constrains what 
 * messages can legally be sent at various points of processing. 
 * If a servlet attempts to send a message which would violate the SIP specification 
 * (for example, the transaction state machine), the container throws an IllegalStateException.
 */
public interface SipServletMessage extends java.lang.Cloneable{
    /**
     * Adds an acceptable Locale of this user agent. The language identified by the Locale will be listed in an Accept-Language header with a lower q-value than any existing Accept-Language value, meaning the locale is less preferred than those already identified in this message.
     */
    void addAcceptLanguage(java.util.Locale locale);

    /**
     * Adds the specified Address as a new value of the named header field. The address is added as the last header field value.
     * This method can be used with headers which are defined to contain one or more entries matching (name-addr | addr-spec) *(SEMI generic-param) as defined in RFC 3261. This includes, for example, Contact and Route.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     */
    void addAddressHeader(java.lang.String name, javax.servlet.sip.Address addr, boolean first);

    /**
     * Adds a header with the given name and value. This method allows headers to have multiple values. The container MAY check that the specified header field can legally appear in the this message.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent. The list of assigned compact form is available in the IANA registry at http://www.isi.edu/in-notes/iana/assignments/sip-parameters
     * Note: applications should never attempt to set the From, To, Call-ID, CSeq, Via, Record-Route, and Route headers. Also, setting of the Contact header is subject to the constraints mentioned in the introduction.
     */
    void addHeader(java.lang.String name, java.lang.String value);

    /**
     * Adds the specified Parameterable as a new value of the named header field. The parameterable is added as the last header field value.
     * This method can be used with headers which are defined to contain one or more entries matching field-value *(;parameter-name=parameter-value) as defined in RFC 3261. This includes, for example, Event and Via.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     */
    void addParameterableHeader(java.lang.String name, javax.servlet.sip.Parameterable param, boolean first);

    /**
     * Returns the preferred Locale that the UA originating this message will accept content in, based on the Accept-Language header. If this message doesn't contain an Accept-Language header, this method returns the default locale for the server.
     */
    java.util.Locale getAcceptLanguage();

    /**
     * Returns an Iterator over Locale objects indicating, in decreasing order starting with the preferred locale, the locales that are acceptable to the sending UA based on the Accept-Language header. If this message doesn't provide an Accept-Language header, this method returns an Iterator containing one Locale, the default locale for the server.
     */
    Iterator<Locale> getAcceptLanguages();

    /**
     * Returns the value of the specified header as a Address object.
     * This method can be used with headers which are defined to contain one or more entries matching (name-addr | addr-spec) *(SEMI generic-param) as defined in RFC 3261. This includes, for example, Contact and Route.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     * If there is more than one header field value the first is returned.
     */
    Address getAddressHeader(java.lang.String name) throws javax.servlet.sip.ServletParseException;

    /**
     * Returns a ListIterator over all Address header field values for the specified header. The values returned by the Iterator follow the order in which they appear in the message header.
     * This method can be used with headers which are defined to contain one or more entries matching (name-addr | addr-spec) *(SEMI generic-param) as defined in RFC 3261. This includes, for example, Contact and Route.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     * Attempts to modify the specified header field through the returned list iterator must fail with an IllegalStateException if the header field is a system header. For non-system headers the argument to the add and set methods of the iterator returned by getAddressHeaders must be Address objects.
     * Note: This is a fail-fast iterator and can throw ConcurrentModificationException if the underlying implementation does not allow modification after the iterator is created.
     */
    ListIterator<javax.servlet.sip.Address> getAddressHeaders(java.lang.String name) throws javax.servlet.sip.ServletParseException;

    /**
     * Returns the application session to which this message belongs. If the session doesn't already exist it is created. 
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession();

    /**
     * Returns the app session to which this message belongs.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession(boolean create);

    /**
     * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
     * Attributes can be set two ways. The servlet container may set attributes to make available custom information about a request or a response. For example, for requests made using HTTPS, the attribute javax.servlet.request.X509Certificate can be used to retrieve information on the certificate of the client. Attributes can also be set programatically using setAttribute(String, Object). This allows information to be embedded into a request or response before a RequestDispatcher call.
     * Attribute names should follow the same conventions as package names. Names beginning with javax.servlet.sip. are reserved for definition by the SIP Servlet API.
     */
    java.lang.Object getAttribute(java.lang.String name);

    /**
     * Returns an Enumeration containing the names of the attributes available to this message object. This method returns an empty Enumeration if the request has no attributes available to it.
     */
    Enumeration<String> getAttributeNames();

    /**
     * Returns the value of the Call-ID header in this SipServletMessage.
     */
    java.lang.String getCallId();

    /**
     * Returns the name of the charset used for the MIME body sent in this message. This method returns null if the message does not specify a character encoding.
     * The message character encoding is used when converting between bytes and characters. If the character encoding hasn't been set explicitly UTF-8 will be used for this purpose.
     * For more information about character encodings and MIME see RFC 2045 (http://www.ietf.org/rfc/rfc2045.txt).
     */
    java.lang.String getCharacterEncoding();

    /**
     * Returns the content as a Java object. The actual type of the returned object 
     * depends on the MIME type of the content itself (the Content-Type). 
     * Containers are required to return a String object for MIME type text/plain 
     * as for other text/* MIME types for which the container doesn't have specific knowledge.
     * It is encouraged that the object returned for "multipart" MIME content 
     * is a javax.mail.Multipart object. 
     * A byte array is returned for content-types that are unknown to the container.
     * The message's character encoding is used when the MIME type indicates 
     * that the content consists of character data.
     * Note: This method, together with setContent, is modelled over similar methods 
     * in the JavaMail API. Whereas the JavaMail API mandates 
     * the use of the Java Activation Framework (JAF) as the underlying data handling system, 
     * the SIP servlet API doesn't currently require JAF.
     */
    java.lang.Object getContent() throws java.io.IOException, java.io.UnsupportedEncodingException;

    /**
     * Returns the locale of this message. This method returns the Locale identified by the Content-Language header of the message, or null if the Content-Language header is not present.
     */
    java.util.Locale getContentLanguage();

    /**
     * Returns the length in number of bytes of the content part of this message. This directly reflects the value of the Content-Length header field.
     */
    int getContentLength();

    /**
     * Returns the value of the Content-Type header field.
     */
    java.lang.String getContentType();

    /**
     * Returns the value of the Expires header. The Expires header field gives the relative time after which the message (or content) expires. The unit of measure is seconds.
     */
    int getExpires();

    /**
     * Returns the value of the From header.
     */
    javax.servlet.sip.Address getFrom();

    /**
     * Returns the value of the specified header as a String. If the request did not include a header of the specified name, this method returns null. If multiple headers exist, the first one is returned. The header name is case insensitive.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent. The list of assigned compact form is available in the IANA registry at http://www.isi.edu/in-notes/iana/assignments/sip-parameters
     * For example, getHeader("Content-Type"); getHeader("c"); will both return the same value.
     */
    java.lang.String getHeader(java.lang.String name);

    javax.servlet.sip.SipServletMessage.HeaderForm getHeaderForm();

    /**
     * Returns an Iterator over all the header names this message contains. If the message has no headers, this method returns an empty Iterator.
     * Note: This is a fail-fast iterator and can throw ConcurrentModificationException if the underlying implementation does not allow modification after the iterator is created.
     * Some servlet containers do not allow servlets to access headers using this method, in which case this method returns null.
     */
    Iterator<String> getHeaderNames();

    /**
     * Returns all the values of the specified request header as a ListIterator over a number of String objects. The values returned by the Iterator follow the order in which they appear in the message header.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent. The list of assigned compact form is available in the IANA registry at http://www.isi.edu/in-notes/iana/assignments/sip-parameters
     * Some headers, such as Accept-Language can be sent by clients as several headers each with a different value rather than sending the header as a comma separated list.
     * If the request did not include any headers of the specified name, this method returns an empty Iterator. The header name is case insensitive.
     * Note: This is a fail-fast iterator and can throw ConcurrentModificationException if the underlying implementation does not allow modification after the iterator is created.
     * Attempts to modify the specified header field through the returned list iterator must fail with an IllegalStateException if the header field is a system header.
     */
    ListIterator<String> getHeaders(java.lang.String name);

    /**
     * Returns the IP address of the upstream/downstream hop from which this message was initially received by the container.
     * Unlike getRemoteAddr(), this method returns the same value regardless of which application 
     * invokes it in the same application composition chain of a specific application router. 
     * @return a String containing the IP address of the sender of this message, or null if it was locally generated
     * @since 1.1
     */
    java.lang.String getInitialRemoteAddr();
    
    /**
     * Returns the port number of the upstream/downstream hop from which this message initially received by the container.
     * Unlike getRemotePort(), this method returns the same value regardless of which application 
     * invokes it in the same application composition chain of a specific application router. 
     * @return the port number of the sender of this message, or -1 if it was locally generated.
     * @since 1.1
     */
    int getInitialRemotePort();

    /**
     * Returns the name of the protocol with which this message was initially received by the container, e.g. "UDP", "TCP", "TLS", or "SCTP". 
     * 
     * @return name of the protocol this message was initially received with, or null if it was locally generated.
     * @since 1.1
     */
    java.lang.String getInitialTransport();
    
    /**
     * Returns the IP address of the interface this message was received on.
     */
    java.lang.String getLocalAddr();

    /**
     * Returns the local port this message was received on.
     */
    int getLocalPort();

    /**
     * Returns the SIP method of this message. This is a token consisting of all upper-case letters, for example "INVITE". For requests, the SIP method is in the request line while for responses it may be extracted from the CSeq header.
     */
    java.lang.String getMethod();

    /**
     * Returns the value of the specified header field as a Parameterable object.
     * This method can be used with headers which are defined to contain one or more entries matching field-value *(;parameter-name=parameter-value) as defined in RFC 3261. This includes, for example, Event and Via.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     * If there is more than one header field value the first is returned.
     */
    javax.servlet.sip.Parameterable getParameterableHeader(java.lang.String name) throws javax.servlet.sip.ServletParseException;

    /**
     * Returns a ListIterator over all Parameterable header field values for the specified header name. The values returned by the Iterator follow the order in which they appear in the message header.
     * This method can be used with headers which are defined to contain one or more entries matching field-value *(;parameter-name=parameter-value) as defined in RFC 3261. This includes, for example, Event and Via.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     * Attempts to modify the specified header field through the returned list iterator must fail with an IllegalStateException if the header field is a system header.
     * Note: This is a fail-fast iterator and can throw ConcurrentModificationException if the underlying implementation does not allow modification after the iterator is created.
     */
    ListIterator<? extends Parameterable> getParameterableHeaders(java.lang.String name) throws javax.servlet.sip.ServletParseException;

    /**
     * Returns the name and version of the protocol of this message. This is in the form
     * protocol> "/"
     * major-version-number> "."
     * minor-version-number>, for example "SIP/2.0".
     * For this version of the SIP Servlet API this is always "SIP/2.0".
     */
    java.lang.String getProtocol();

    /**
     * Returns message content as a byte array. The reference is returned if the application wants to re-use the content for another message it should make a copy.
     */
    byte[] getRawContent() throws java.io.IOException;

    /**
     * Returns the IP address of the next upstream/downstream hop from which this message was received. 
     * Applications can determine the actual IP address of the UA that originated 
     * the message from the message Via header fields.
     * 
     *  <br/>
     *  If the message was internally routed (from one application to the
     *  next within the same container), then this method returns the address
     *  of the container's SIP interface.
     */
    java.lang.String getRemoteAddr();

    /**
     * Returns the port number of the next upstream/downstream hop from which this message was received. <br/>
     * If the message was internally routed (from one application to the next within 
     * the same container), then this method returns a valid port number chosen 
     * by the container or the host TCP/IP stack.
     */
    int getRemotePort();

    /**
     * Returns the login of the user sending this message, if the user has been authenticated, or null if the user has not been authenticated.
     */
    java.lang.String getRemoteUser();

    /**
     * Returns the SipSession to which this message belongs. If the session didn't already exist it is created. This method is equivalent to calling getSession(true).
     */
    javax.servlet.sip.SipSession getSession();

    /**
     * Returns the SipSession to which this message belongs.
     */
    javax.servlet.sip.SipSession getSession(boolean create);

    /**
     * Returns the value of the To header.
     */
    javax.servlet.sip.Address getTo();

    /**
     * Returns the name of the protocol with which this message was received, e.g. "UDP", "TCP", "TLS", or "SCTP".
     */
    java.lang.String getTransport();

    /**
     * Returns a java.security.Principal object containing the name of the authenticated user agent sending this message. If the user agent has not been authenticated, the method returns null.
     */
    java.security.Principal getUserPrincipal();

    /**
     * Returns true if this message is committed, that is, if one of the following conditions is true: This message is an incoming request for which a final response has already been generated. This message is an outgoing request which has already been sent. This message is an incoming response received by a servlet acting as a UAC. If this is a response to an INVITE then the response is not committed until the ACK has been generated. This message is a response which has already been forwarded upstream
     */
    boolean isCommitted();

    /**
     * Returns a boolean indicating whether this message was received over a secure channel, such as TLS.
     */
    boolean isSecure();

    /**
     * Returns a boolean indicating whether the authenticated user is included in the specified logical "role". Roles and role membership can be defined using deployment descriptors. If the user has not been authenticated, the method returns false.
     */
    boolean isUserInRole(java.lang.String role);

    /**
     * Removes the named attribute from this message. Nothing is done if the message did not already contain the specified attribute. 
     * 
     * Attribute names should follow the same conventions as package names. Names beginning with javax.servlet.sip.* are reserved for definition by the SIP Servlet API.
     * @param name a String specifying the name of the attribute
     * @throws NullPointerException if name is null.
     */
    void removeAttribute(java.lang.String name);
    
    /**
     * Removes the specified header. If multiple headers exists with the given name, they're all removed.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     */
    void removeHeader(java.lang.String name);

    /**
     * Sends this SipServletMessage.
     */
    void send() throws java.io.IOException;

    /**
     * Sets the preferred Locale that this user agent will accept content, reason phrases, warnings, etc. in. The language identified by the Locale will be listed in an Accept-Language header.
     * A null argument is valid and removes and existing Accept-Language headers.
     */
    void setAcceptLanguage(java.util.Locale locale);

    /**
     * Sets the header with the specified name to have the value specified by the address argument.
     * This method can be used with headers which are defined to contain one or more entries matching (name-addr | addr-spec) *(SEMI generic-param) as defined in RFC 3261. This includes, for example, Contact and Route.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     * 
     *  <p>If the message did not include any headers of the specified name,
     *   this method returns an empty Iterator. If the message included headers of
     *   the specified name with no values, this method returns an Iterator over empty <code>String</code>s.
     * 
     * @throws IllegalArgumentException if the specified header isn't defined to hold address values or if the specified header field is a system header
     */
    void setAddressHeader(java.lang.String name, javax.servlet.sip.Address addr);

    /**
     * Stores an attribute in this message. Attributes are reset between messages. 
     * This method is most often used in conjunction with RequestDispatcher.
     * Attribute names should follow the same conventions as package names. 
     * Names beginning with javax.servlet.sip.* are reserved for definition by the SIP Servlet API.
     *
     */
    void setAttribute(java.lang.String name, java.lang.Object o);

    /**
     * Overrides the name of the character encoding that will be used to convert the body of this message from bytes to characters or vice versa.
     * Explicitly setting a message's character encoding potentially affects the behavior of subsequent calls to getContent() and setContent(java.lang.Object, java.lang.String). This method must be called prior to calling either of those methods.
     */
    void setCharacterEncoding(java.lang.String enc) throws java.io.UnsupportedEncodingException;

    /**
     * Sets the content of this message to the specified Object.
     * This method only works if the implementation "knows about" the specified object and MIME type. Containers are required to handle byte[] content with any MIME type.
     * Furthermore, containers are required to handle String content when used with a text/* content type. When invoked with non-String objects and a text/* content type, containers may invoke toString() on the content Object in order to obtain the body's character data. It is also recommended that implementations know how to handle javax.mail.Multipart content when used together with "multipart" MIME types.
     * When converting String content, this method may use the the message's character encoding (as set by setCharacterEncoding(java.lang.String), setContentType(java.lang.String) or setContentLanguage(java.util.Locale)) to map the String to a byte array.
     * Note: This method, together with getContent(), is modelled over a similar method in the JavaMail API. Whereas the JavaMail API mandates the use of the Java Activation Framework (JAF) as the underlying data handling system, the SIP servlet API doesn't currently require JAF.
     */
    void setContent(java.lang.Object content, java.lang.String contentType) throws java.io.UnsupportedEncodingException;

    /**
     * Sets the locale of this message, setting the headers (Content-Language and the Content-Type's charset) as appropriate. This method should be called before a call to setContent.
     */
    void setContentLanguage(java.util.Locale locale);

    /**
     * Sets the value of the Content-Length header.
     * Applications are discouraged from setting the Content-Length directly using this method; they should instead use the setContent methods which guarantees that the Content-Length is computed and set correctly.
     */
    void setContentLength(int len);

    /**
     * Sets the content type of the response being sent to the client. The content type may include the type of character encoding used, for example, text/html; charset=UTF-8. This will cause the message's current character encoding to be set.
     * If obtaining a PrintWriter or calling setContent, this method should be called first.
     */
    void setContentType(java.lang.String type);

    /**
     * Sets the value of the Expires header in this message. This method is equivalent to: setHeader("Expires", String.valueOf(seconds));
     */
    void setExpires(int seconds);

    /**
     * Sets a header with the given name and value. If the header had already been set, the new value overwrites the previous one.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent. The applications choice of long or compact form shall take effect only of the HeaderForm parameter is set to SipServletMessage.HeaderForm.DEFAULT.
     * Note: applications should never attempt to set the From, To, Call-ID, CSeq, Via, Record-Route, and Route headers. Also, setting of the Contact header is subject to the constraints mentioned in the introduction.
     */
    void setHeader(java.lang.String name, java.lang.String value);

    /**
     * Indicates which of the compact or long form should the headers in this message have. If compact is selected then all the headers that have compact names should be represented with them, regardless of how they were added to the message. When long is selected then all headers change to their long form. Instead if the applications wish to mix the compact and long form then they must not invoke the setUseCompactForm method or set it to use
     * and instead set the non-system headers directly using the compact or long form
     * . eg. SipServletMessage message; ..... message.setHeader("s", "Meeting at 5pm"); // Subject header compact form message.setHeader("Allow-Events", "telephone-event"); // Long form ..... For applications to set each header individually the value of the HeaderForm MUST be
     * The list of assigned compact form is available in the IANA registry at http://www.isi.edu/in-notes/iana/assignments/sip-parameters
     */
    void setHeaderForm(javax.servlet.sip.SipServletMessage.HeaderForm form);

    /**
     * Sets the header with the specified name to have the value specified by the address argument.
     * This method can be used with headers which are defined to contain one or more entries matching field-value *(;parameter-name=parameter-value) as defined in RFC 3261. This includes, for example, Event and Via.
     * Either the long or compact name can be used to access the header field, as both are treated as equivalent.
     * 
     *  <p>If the message did not include any headers of the specified name,
     *   this method returns an empty Iterator. If the message included headers of
     *   the specified name with no values, this method returns an Iterator over empty <code>String</code>s.
     * 
     * @throws IllegalArgumentException if the specified header isn't defined to hold address values or if the specified header field is a system header
     */
    void setParameterableHeader(java.lang.String name, javax.servlet.sip.Parameterable param);

    /**
     * Type header forms.
     * @since 1.1
     *
     */
    public enum HeaderForm{
    	COMPACT,
    	/**
    	 * Default container form, also if this is set the indvidual headers can be set in different forms.
    	 */
    	DEFAULT, 
    	LONG;
    }
}
