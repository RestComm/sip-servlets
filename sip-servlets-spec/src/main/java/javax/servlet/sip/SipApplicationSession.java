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

import java.util.Collection;
import java.util.Iterator;

/**
 * Represents application instances. The SipApplicationSession interface acts as a store for application data and provides access to contained protocol sessions, e.g. SipSession and HttpSession objects representing point-to-point signaling relationships.
 */
public interface SipApplicationSession{
	
	/**
	 * Possible protocols to which sessions contained in the SipApplicationSession belong to.
	 *
	 * @since 1.1
	 */
	public enum Protocol {
		HTTP, SIP;
	}
	
    /**
     * @deprecated
     * Encodes the ID of this SipApplicationSession into the specified URI. The container must then be prepared to associate this application session with an incoming request which was triggered by activating the encoded URI.
     * In the case of SIP and SIPS URIs, the container may also rewrite the host, port, and transport protocol components of the URI based on its knowledge of local listen points. When doing so it should take existing settings as a hint as to which listen point to select when it has more than one.
     * This method allow applications to correlate events which would otherwise be treated as being independent, that is, as belonging to different application sessions. For example, an application might send an instant message with an HTML body to someone. The IM body may then contain a SIP URI pointing back to the SIP servlet container and the application session in which the IM was generated, thus ensuring that an INVITE triggered by the IM recipient triggering that URI is associated with this application session when received by the container.
     * Containers are required to support rewriting of SIP and SIPS URIs.
     */
    void encodeURI(javax.servlet.sip.URI uri);

    /**
     * Encode specified URL to include the application session ID in a way such that 
     * the parameter used to encode the application session ID should be unique across implementations. 
     * The recommended way is to use the java package name of the implementation, like com.acme.appsession. 
     * This mechanism can be used by the applications to encode the HTTP URL with the application session Id. 
     * This URL can then be sent out through some of out of band mechanism. 
     * When the HTTP Request comes back to the converged container with this request, 
     * the container must associate the new HttpSession with the encoded Application Session. 
     * In case the HTTP request is not a new request but a follow on request already 
     * associated with a HTTP Session then the converged containers must use the HTTP session 
     * association mechanism to route the request to the right HTTP Session. 
     * If that HTTP Session was not associated with the encoded SipApplicationSession in the 
     * request then that association MUST occur. This mechanism is similar to how 
     * the (deprecated) encodeURI() operates for SIP.
     */
    java.net.URL encodeURL(java.net.URL url);

    /**
     * Returns the name of the SIP application this SipApplicationSession is associated with.
     * @return name of the SIP application, this SipApplicationSession is associated with
     */
    java.lang.String getApplicationName();
    
    /**
     * Returns the object bound with the specified name in this session, or null if no object is bound under the name.
     */
    java.lang.Object getAttribute(java.lang.String name);

    /**
     * Returns an Iterator over the String objects containing the names of all the objects bound to this session.
     * Note: This is a fail-fast iterator and can throw ConcurrentModificationException if the underlying implementation does not allow modification after the iterator is created.
     */
    Iterator<String> getAttributeNames();

    /**
     * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT.
     */
    long getCreationTime();

    /**
     * Returns the time in future when this SipApplicationSession will expire. This would be the time of session creation + the expiration time set in milliseconds. For sessions that are set never to expire, this returns 0. For sessions that have already expired this returns
     * The time is returned as the number of milliseconds since midnight January 1, 1970 GMT.
     * @throws IllegalStateException if this application session is not valid
     */
    long getExpirationTime();

    /**
     * Returns a string containing the unique identifier assigned to this session. The identifier is assigned by the servlet container and is implementation dependent. For applications with a method with SipApplicationKey annotation the containers MUST incorporate the return value from that into its Id generation, such that a certain key is consistently associated with one and only one SipApplication instance.
     */
    java.lang.String getId();

    /**
     * Returns true if the container will notify the application when this SipApplicationSession is in the ready-to-invalidate state. 
     * @return value of the invalidateWhenReady flag 
     * @throws IllegalStateException if this application session is not valid
     */
    boolean getInvalidateWhenReady();
    
    /**
     * Specifies whether the container should notify the application when the SipApplicationSession 
     * is in the ready-to-invalidate state as defined above. 
     * 
     * The container notifies the application using the SipApplicationSessionListener.sessionReadyToInvalidate  callback.
     *  
     * @param invalidateWhenReady if true, the container will observe this application session 
     * and notify the application when it is in the ready-to-invalidate state. 
     * The application session is not observed if the flag is false. 
     * The default is true for v1.1 applications and false for v1.0 applications.
     * @throws IllegalStateException if this application session is not valid 
     */
    void setInvalidateWhenReady(boolean invalidateWhenReady);
    
    /**
     * Returns the last time an event occurred on this application session. For SIP, incoming and outgoing requests and incoming responses are considered events. The time is returned as the number of milliseconds since midnight January 1, 1970 GMT.
     * Actions that applications take, such as getting or setting a value associated with the session, do not affect the access time.
     */
    long getLastAccessedTime();

    /**
     * Returns an Iterator over all "protocol" sessions associated with this application session. This may include a mix of different types of protocol sessions, e.g. SipSession and javax.servlet.http.HttpSession objects.
     */
    Iterator<?> getSessions();

    /**
     * Returns an Iterator over the "protocol" session objects associated of the specified protocol associated with this application session. If the specified protocol is not supported, an empty Iterator is returned.
     * If "SIP" is specified the result will be an Iterator over the set of SipSession objects belonging to this application session. For "HTTP" the result will be a list of javax.servlet.http.HttpSession objects.
     */
    Iterator<?> getSessions(java.lang.String protocol);

    /**
     * Returns the SipSession with the specified id belonging to this application session, or null if not found.
     */
    javax.servlet.sip.SipSession getSipSession(java.lang.String id);

    /**
     * Returns the session object with the specified id associated with the specified protocol belonging to this application session, or null if not found.
     * @param id the session id
     * @param protocol an Enum identifying the protocol 
     * @return the corresponding session object or null if none is found. 
     * @throws NullPointerException on null id or protocol
     * @throws IllegalStateException if this application session is not valid
     */
    java.lang.Object getSession(java.lang.String id, SipApplicationSession.Protocol protocol);

    /**
     * Returns the active timer identified by a specific id that is associated with this application session.
     * @param id 
     * @return the ServletTimer object identified by the id belonging to this application session
     * @throws IllegalStateException if this application session is not valid
     */
    ServletTimer getTimer(java.lang.String id);
    
    /**
     * Returns all active timers associated with this application session.
     */
    Collection<ServletTimer> getTimers();

    /**
     * Invalidates this application session and unbinds any objects bound to it. 
     * The invalidation will cause any timers associated with this application session to be cancelled. 
     */
    void invalidate();

    /**
     * Returns true if this application session is in a ready-to-invalidate state. A SipApplicationSession is in the ready-to-invalidate state if the following conditions are met:
     * 1. All the contained SipSessions are in the ready-to-invalidate state.
     * 2. None of the ServletTimers associated with the SipApplicationSession are active. 
     * @return true if the application session is in ready-to-invalidate state, false otherwise 
     * @throws IllegalStateException if this application session is not valid
     */
    boolean isReadyToInvalidate();
    
    /**
     * Returns if this SipApplicationSession is valid, false otherwise. The SipSession can be invalidated by calling the method
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
     * Sets the time of expiry for this application session.
     * This allows servlets to programmatically extend the lifetime of application sessions. This method may be invoked by an application in the notification that the application session has expired: SipApplicationSessionListener.sessionExpired. If the server is willing to extend the session lifetime it returns the actual number of minutes the session lifetime has been extended with, and the listener will be invoked about session expiry again at a later time.
     * This helps applications clean up resources in a reasonable amount of time in situations where it depends on external events to complete an application session. Being able to extend session lifetime means the application is not forced to choose a very high session lifetime to begin with.
     * It is entirely up to server policy whether to grant or deny the applications request to extend session lifetime. Note that any attempt to extend the lifetime of an explicitly invalidated application session, one for which setExpires(int) has been invoked, will always fail.
     * In order to make the SipApplicationSession immortal i.e never expire, setExpires should be called with 0 (or -ve number), again it is upto the container to accept this or not. If the container does accept setting the session to never expire then it returns Integer.MAX_VALUE.
     */
    int setExpires(int deltaMinutes);

}
