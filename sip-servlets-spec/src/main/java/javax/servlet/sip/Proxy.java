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

import java.util.List;

/**
 * Represents the operation of proxying a SIP request.
 * A number of parameters control how proxying is carried out:
 * <ul>
 * <li> 
 * addToPath: addToPath: Whether the application adds a Path header to the REGISTER request. The default is false.
 * </li>
 * <li> 
 * recurse: Whether to autmotically recurse or not. The default is true.
 * </li>
 * <li>
 * recordRoute: Whether to record-route or not. The default is false. 
 * </li>
 * <li>
 * parallel: Whether to proxy in parallel or sequentially. The default is true. 
 * </li>
 * <li>
 * stateful: Whether to remain transaction stateful for the duration of the proxying operation. The default is true. 
 * </li>
 * <li>
 * supervised: Whether the application will be invoked on incoming responses related to this proxying. 
 * </li>
 * <li>
 * proxyTimeout: The timeout for the proxy in general. In case the proxy is a 
 * sequential proxy then this value behaves like the sequential-search-timeout 
 * which is deprecated since v1.1. In case the proxy is a parallel proxy then 
 * this timeout acts as the timeout for the entire proxy i.e each of its parallel branches 
 * before it starts to send out CANCELs waiting for final responses on all INVITE branches 
 * and sends the best final response upstream. 
 * </li>
 * <li>
 * sequentialSearchTimeout: The time the container waits for a final response before 
 * it CANCELs the branch and proxies to the next destination in the target set. 
 * The usage of this explicit sequential timeout setting is deprecated and 
 * replaced by a general proxyTimeout parameter. 
 * </li>
 * </ul>
 * The effect of the various parameters is explained further below.
 */
public interface Proxy{
    /**
     * Cancels this proxy transaction and any of its child branches if recursion was enabled.
     */
    void cancel();

    /**
     * This overloaded method of cancel() provides a way to specify the reason 
     * for cancelling this Proxy by including the appropriate Reason headers [RFC 3326].
     * @param protocol describes the source of the 'cause' field in the Reason header field.
     * @param reasonCode corresponds to the 'cause' field. For eg, if protocol is SIP, 
     * the reasonCode would be the status code of the response which caused the cancel
     * @param reasonText describes the reason for cancelling the Proxy.
     * @since 1.1
     */
    void cancel(java.lang.String[] protocol,
            int[] reasonCode,
            java.lang.String[] reasonText);
    /**
     * Returns the list of
     * objects given a set of targets. The resulting branches will not have associated client transactions until
     * is invoked.
     * Implementations are required to support SipURI arguments and may support other types of URIs.
     */
    List<javax.servlet.sip.ProxyBranch> createProxyBranches(List<? extends URI> targets);

    /**
     * Returns true if subsequent invocations of
     * will add a Path header to the proxied request, false otherwise.
     */
    boolean getAddToPath();

    /**
     * Returns the request received from the upstream caller.
     */
    javax.servlet.sip.SipServletRequest getOriginalRequest();

    /**
     * Returns true if this proxy object is set to proxy in parallel, or false if it is set to proxy sequentially.
     */
    boolean getParallel();

    /**
     * Returns a SipURI that the application can use to add parameters to the Path header. This may be used by Path header adding proxy applications in order to push state to the Registrar and have it returned in subsequent requests for the registered UA.
     * Parameters added through a URI returned by this method can be retrieved from a subsequent request in the same dialog by calling ServletRequest.getParameter(java.lang.String).
     * Note that the URI returned is good only for specifying a set of parameters that the application can retrieve when invoked to handle subsequent requests Other components of the URI are irrelevant and cannot be trusted to reflect the actual values that the container will be using when inserting a Path header into proxied request.
     */
    javax.servlet.sip.SipURI getPathURI();

    /**
     * Any branch has a primary URI associated with it, using which it was created. The ProxyBranch may have been created using
     * method, implicitly when proxyTo() is called or when any of the proxy branch recurses as a result of a redirect response. A URI uniquely identifies a branch.
     */
    javax.servlet.sip.ProxyBranch getProxyBranch(javax.servlet.sip.URI uri);

    /**
     * More than one branches are associated with a proxy when
     * or
     * is used. This method returns the top level branches thus created. If recursion is enabled on proxy or on any of its branches then on receipt of a 3xx class response on that branch, the branch may recurse into sub-branches. This method returns just the top level branches started.
     */
    List<javax.servlet.sip.ProxyBranch> getProxyBranches();

    /**
     * The current value of the overall proxy timeout value. This is measured in seconds.
     */
    int getProxyTimeout();

    /**
     * Returns true if subsequent invocations of
     * will add a Record-Route header to the proxied request, false otherwise.
     */
    boolean getRecordRoute();

    /**
     * Returns a SipURI that the application can use to add parameters to the Record-Route header. This is used by record-routing proxy applications in order to push state to the endpoints and have it returned in subsequent requests belonging to the same dialog.
     * Parameters added through a URI returned by this method can be retrieved from a subsequent request in the same dialog by calling ServletRequest.getParameter(java.lang.String).
     * Note that the URI returned is good only for specifying a set of parameters that the application can retrieve when invoked to handle subsequent requests in the same dialog. Other components of the URI are irrelevant and cannot be trusted to reflect the actual values that the container will be using when inserting a Record-Route header into proxied request.
     * Applications must not set SIP URI parameters defined in RFC3261. This includes transport, user, method, ttl, maddr, and lr. Other components of the URI, e.g. host, port, and URI scheme must also not by modified by the application. These Record-Route URI components will be populated by the container and may or may not have valid values at the time an application proxies a request. Any attempt to set these parameters or URI contents will thrown an IllegalArgumentException. Record Route parameters thus added affect the branches created right after. If there are branches that were created prior to this modification then they MUST continue to have the Record-Route header as was when they were created. This means that the ProxyBranch gets a cloned copy of the header.
     */
    javax.servlet.sip.SipURI getRecordRouteURI();

    /**
     * Returns true if this proxy object is set to recurse, or false otherwise.
     */
    boolean getRecurse();

    /**
     * @deprecated
     * Returns the current value of the sequential search timeout parameter. This is measured in seconds.
     */
    int getSequentialSearchTimeout();

    /**
     * @deprecated
     * Returns true if this proxy operation is transaction stateful (the default), or false if it is stateless.
     */
    boolean getStateful();

    /**
     * Returns true if the controlling servlet will be invoked on incoming responses for this proxying operation, and false otherwise.
     */
    boolean getSupervised();

    /**
     * Proxies a SIP request to the specified set of destinations.
     */
    void proxyTo(List<? extends URI> uris);

    /**
     * Proxies a SIP request to the specified destination.
     * Implementations are required to support SipURI arguments and may support other types of URIs.
     */
    void proxyTo(javax.servlet.sip.URI uri);

    /**
     * Specifies whether branches initiated in this proxy operation should include a Path header for the REGISTER request for this servlet container or not.
     * Path header is used to specify that this Proxy must stay on the signaling path of subsequent requests sent to the Registered UA from the Home Proxy in the network. The detailed procedure of Path header handling is defined in RFC 3327.
     */
    void setAddToPath(boolean p);

    /**
     * In multi-homed environment this method can be used to select the outbound interface 
     * and port number to use for proxy branches. 
     * The specified address must be the address of one of the configured outbound interfaces. 
     * The set of SipURI objects which represent the supported outbound interfaces can be obtained from the servlet context attribute named javax.servlet.sip.outboundInterfaces.
     * 
     * The port is interpreted as an advice by the app to the container. 
     * If the port of the socket address has a non-zero value, 
     * the container will make a best-effort attempt to use it as the source port number for UDP packets, 
     * or as a source port number for TCP connections it originates. 
     * If the port is not available, the container will use its default port allocation scheme.
     * 
     * Invocation of this method also impacts the system headers generated by the container for this Proxy, 
     * such as the Record-Route header (getRecordRouteURI()), 
     * the Via and the Contact header. 
     * The IP address part of the socket address is used to construct these system headers.
     * @param address the socket address representing the outbound interface to use when forwarding requests with this proxy 
     * @throws NullPointerException on null address
     * @throws IllegalArgumentException if the address is not understood by the container as one of its outbound interface 
     */
    void setOutboundInterface(java.net.InetAddress address);
    
    /**
     * In multi-homed environment this method can be used to select the outbound interface 
     * and port number to use for proxy branches. 
     * The specified address must be the address of one of the configured outbound interfaces. 
     * The set of SipURI objects which represent the supported outbound interfaces can be obtained from the servlet context attribute named javax.servlet.sip.outboundInterfaces.
     * 
     * Invocation of this method also impacts the system headers generated by the container for this Proxy, 
     * such as the Record-Route header (getRecordRouteURI()), 
     * the Via and the Contact header. 
     * The IP address part of the socket address is used to construct these system headers.
     * @param address the address which represents the outbound interface  
     * @throws NullPointerException on null address
     * @throws IllegalArgumentException if the address is not understood by the container as one of its outbound interface
     * @throws IllegalStateException if this method is called on an invalidated session  
     */
    void setOutboundInterface(java.net.InetSocketAddress address);

    /**
     * Specifies whether to proxy in parallel or sequentially.
     */
    void setParallel(boolean parallel);

    /**
     * Sets the overall proxy timeout. If this proxy is a sequential proxy then the behavior is same as the erstwhile
     * . Further the value set through this method shall override any explicit sequential value set through deprecated
     * . On the other hand if the proxy is parallel then this acts as the upper limit for the entire proxy operation resulting in equivalent of invoking
     * if the the proxy did not complete during this time, which means that a final response was not sent upstream.
     */
    void setProxyTimeout(int seconds);

    /**
     * Specifies whether branches initiated in this proxy operation should include a Record-Route header for this servlet engine or not. This shall affect all the branches created after its invocation.
     * Record-routing is used to specify that this servlet engine must stay on the signaling path of subsequent requests.
     * @throws IllegalStateException if the proxy has already been started
     */
    void setRecordRoute(boolean rr);

    /**
     * Specifies whether the servlet engine will automatically recurse or not. If recursion is enabled the servlet engine will automatically attempt to proxy to contact addresses received in redirect (3xx) responses. If recursion is disabled and no better response is received, a redirect response will be passed to the application and will be passed upstream towards the client.
     */
    void setRecurse(boolean recurse);

    /**
     * @deprecated
     * Sets the sequential search timeout value for this Proxy object. This is the amount of time the container waits for a final response when proxying sequentially. When the timer expires the container CANCELs the current branch and proxies to the next element in the target set.
     * The container is free to ignore this parameter.
     */
    void setSequentialSearchTimeout(int seconds);

    /**
     * @deprecated
     * Specifies whether the server should proxy statelessly or not, that is whether it should maintain transaction state whilst the proxying operation is in progress.
     * This proxy parameter is a hint only. Implementations may choose to maintain transaction state regardless of the value of this flag, but if so the application will not be invoked again for this transaction.
     */
    void setStateful(boolean stateful);

    /**
     * Specifies whether the controlling servlet is to be invoked for incoming responses relating to this proxying.
     */
    void setSupervised(boolean supervised);

    /**
     * Proxies a SIP request to the set of destinations previously specified in
     * . This method will actually start the proxy branches and their associated client transactions. For example, List branches = proxy.createProxyBranches(targets); proxy.startProxy(); is essentially equivalent to Proxy.proxyTo(targets), with the former giving the application finer control over the individual proxy branches through the
     * class. Since the
     * can be invoked multiple times before the startProxy method the effect of startProxy is to start all the branches added in the target set.
     */
    void startProxy();

    /**
     * Returns true if the proxy will not cancel outstanding branches upon receiving the first 2xx INVITE response as in RFC 3841 
     * @return true if the proxy will not cancel outstanding branches upon receiving the first 2xx response, false otherwise
     * @since 1.1
     */
    boolean getNoCancel();
    
    /**
     *  Specifies whether the proxy should, or should not cancel outstanding branches 
     *  upon receiving the first 2xx INVITE response as defined in RFC 3841.
     *  The default proxy behavior, as per RFC 3261 section 16.7 point 10, 
     *  is to cancel outstanding branches upon receiving the first 2xx response; 
     *  this method allows configuring the proxy to keep the branches and forward all 2xx responses upstream.
     *     
     *  Default is false.
     *  
     * 	@param noCancel when true, the proxy will not cancel outstanding branches upon receiving the first 2xx response
     * @since 1.1
     */
    void setNoCancel(boolean noCancel);
}
