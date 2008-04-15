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
package javax.servlet.sip;

import java.util.Enumeration;

/**
 * Represents point-to-point SIP relationships. It roughly corresponds to a SIP dialog. In particular, for UAs it maintains (or is otherwise associated with) dialog state so as to be able to create subequent requests belonging to that dialog (using createRequest).
 * For UACs, SipSession extend the notion of SIP dialogs to have well-defined state before a dialog has been established and after a final non-2xx terminates an early dialog. This allows UACs to create "subsequent" requests without having an established dialog. The effect is that the subsequent request will have the same Call-ID, From and To headers (with the same From tag and without a To tag), the will exist in the same CSeq space.
 * All messages are potentially associated with a SipSession. The SipSession can be retrieved from the message by calling SipServletMessage.getSession().
 */
public interface SipSession{
    /**
     * Returns a new request object. This method is used by user agents only.
     * Note that this method must not be used to create ACK or CANCEL requests. User agents create ACKs by calling SipServletResponse.createAck() and CANCELs are created by calling SipServletRequest.createCancel().
     */
    javax.servlet.sip.SipServletRequest createRequest(java.lang.String method);

    /**
     * Returns the application session with which this SipSession is associated.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession();

    /**
     * Returns the object bound with the specified name in this session, or null if no object is bound under the name.
     */
    java.lang.Object getAttribute(java.lang.String name);

    /**
     * Returns an Enumeration over the String objects containing the names of all the objects bound to this session.
     */
    Enumeration<java.lang.String> getAttributeNames();

    /**
     * Returns the Call-ID for this SipSession. This is the value of the Call-ID header for all messages belonging to this session.
     */
    java.lang.String getCallId();

    /**
     * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT.
     */
    long getCreationTime();

    /**
     * Returns a string containing the unique identifier assigned to this session. The identifier is assigned by the servlet container and is implementation dependent.
     */
    java.lang.String getId();

    /**
     * Returns the last time the client sent a request associated with this session, as the number of milliseconds since midnight January 1, 1970 GMT. Actions that your application takes, such as getting or setting a value associated with the session, do not affect the access time.
     */
    long getLastAccessedTime();

    /**
     * Returns the Address identifying the local party. This is the value of the From header of locally initiated requests in this leg.
     */
    javax.servlet.sip.Address getLocalParty();

    /**
     * This method allows the application to obtain the region that the application is in with respect to this SipSession. It indicates where the subscriber whom the application is invoked to serve is. If region is ORIGINATING, the subscriber is the caller. If region is TERMINATING, the subscriber is the callee.
     * If this SipSession is created when this servlet receives an initial request, this method returns the region in which this servlet is invoked.
     * If this SipSession is created by this servlet, the region depends on the routing directive used when creating the outgoing intial request. If NEW directive is used, region is ORIGINATING. If CONTINUE directive is used to proxy or relay an origRequest, the region is the same as that of the SipSession of origRequest. If REVERSE directive is used to relay an origRequest, the region is the opposite of that of the SipSession of origRequest.
     */
    javax.servlet.sip.SipApplicationRoutingRegion getRegion();

    /**
     * Returns the Address identifying the remote party. This is the value of the To header of locally initiated requests in this leg.
     */
    javax.servlet.sip.Address getRemoteParty();

    /**
     * Returns the current SIP dialog state, which is one of INITIAL, EARLY, CONFIRMED, or TERMINATED. These states are defined in RFC3261.
     */
    javax.servlet.sip.SipSession.State getState();

    /**
     * Returns the URI of the subscriber for which this application is invoked to serve. This is only available if this SipSession received an initial request. Otherwise, this method throws IllegalStateException.
     */
    javax.servlet.sip.URI getSubscriberURI();

    /**
     * Invalidates this session and unbinds any objects bound to it. A session cannot be invalidate if it is in the EARLY or CONFIRMED state, or if there exist ongoing transactions where a final response is expected. One exception is if this session has an associated unsupervised proxy, in which case the session can be invalidate even if transactions are ongoing.
     */
    void invalidate();

    /**
     * Returns true if this SipSession has one or more ongoing transactions. An ongoing transaction will exist if either of the following is true: if a request has been created and sent with this SipSession but but no final response has been received. if a request has been received on this SipSession but no final response has been sent in response to it. However, the above behavior is overridden if the servlet has operated as an unsupervised proxy. Once the servlet has issues proxy.setSupervised(false) for a request associated with this SipSession, then this method will return false regardless of whether there are in fact any ongoing transactions.
     */
    boolean hasOngoingTransaction();

    /**
     * Returns true if this SipSession is valid, false otherwise. The SipSession can be invalidated by calling the method
     * on it. Also the SipSession can be invalidated by the container when either the associated
     * times out or
     * is invoked.
     */
    boolean isValid();

    /**
     * Removes the object bound with the specified name from this session. If the session does not have an object bound with the specified name, this method does nothing.
     */
    void removeAttribute(java.lang.String name);

    /**
     * Binds an object to this session, using the name specified. If an object of the same name is already bound to the session, the object is replaced.
     */
    void setAttribute(java.lang.String name, java.lang.Object attribute);

    /**
     * Sets the handler for this SipSession. This method can be used to explicitly specify the name of the servlet which should handle all subsequently received messages for this SipSession. The servlet must belong to the same application (i.e. same ServletContext) as the caller.
     */
    void setHandler(java.lang.String name) throws javax.servlet.ServletException;

    /**
     * If multihoming is supported, then this method can be used to select the outbound interface to use when sending requests for this SipSession. The specified address must be the address of one of the configured outbound interfaces. The set of SipURI objects which represent the supported outbound interfaces can be obtained from the servlet context attribute named javax.servlet.sip.outboundInterfaces.
     */
    void setOutboundInterface(javax.servlet.sip.SipURI uri);

    /**
     * Possible SIP dialog states from SipSession FSM.
     * @since 1.1
     *
     */
    public enum State{
    	INITIAL,
    	EARLY,
    	CONFIRMED,
    	TERMINATED;
    }
}
