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
 * A ProxyBranch represents a branch which the Proxy sends out the request on. 
 * The ProxyBranch object models the branch as used in RFC3261 while describing a stateful proxy. 
 * For example, 
 * 
 * public void doInvite(SipServletRequest req) { 
 * 		... 
 * 		Proxy p = req.getProxy(); 
 * 		p.setRecordRoute(true); 
 * 		List branches = p.createProxyBranches(getTargets(req)); 
 * 		branches.get(0).setProxyBranchTimeout(5); 
 * 		branches.get(1).setProxyBranchTimeout(10); 
 * 		p.startProxy(); 
 * 		... 
 * }
 * @since 1.1
 */
public interface ProxyBranch{
    /**
     * Cancels this branch and all the child branches if recursion is enabled and sends a CANCEL to the proxied INVITEs. The effect is similar to
     * except that it is limited to this branch and its children only.
     * 
     * @throws IllegalStateException if the transaction has already been completed and it has no child branches
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
     * Returns true if subsequent invocations of startProxy() will add a Path header to the proxied request, false otherwise. 
     * @return value of the "addToPath" flag
     * @since 1.1
     */
    boolean getAddToPath();
    
    /**
     * Returns a SipURI that the application can use to add parameters to the Path header. 
     * This may be used by Path header adding proxy applications in order to push state 
     * to the Registrar and have it returned in subsequent requests for the registered UA.
     * 
     * Parameters added through a URI returned by this method can be retrieved 
     * from a subsequent request in the same dialog by calling ServletRequest.getParameter(java.lang.String).
     * 
     * Note that the URI returned is good only for specifying a set of parameters 
     * that the application can retrieve when invoked to handle subsequent requests 
     * Other components of the URI are irrelevant and cannot be trusted to reflect 
     * the actual values that the container will be using when inserting a Path header into proxied request. 
     * @return SIP URI whose parameters can be modified and then retrieved by this application when processing subsequent requests for the UA
     * @throws IllegalStateException if addToPath is not enabled 
     */
    SipURI getPathURI();
    
    Proxy getProxy();

    /**
     * Returns the current value of the search timeout associated with this ProxyBranch object. If this value is not explicitly set using the
     * then the value is inherited from the Proxy setting.
     */
    int getProxyBranchTimeout();

    /**
     * Returns true if subsequent invocations of proxyTo(URI)  will add a Record-Route header to the proxied request, false otherwise. 
     * @return value of the "recordroute" flag
     */
    boolean getRecordRoute();
    
    /**
     * Returns true if this proxy branch object is set to recurse, or false otherwise. 
     * @return true if recursing is enabled, false otherwise
     */
    boolean getRecurse();
    
    /**
     * Returns a SipURI that the application can use to add parameters to the Record-Route header. This is used by record-routing proxy applications in order to push state to the endpoints and have it returned in subsequent requests belonging to the same dialog.
     * Parameters added through a URI returned by this method can be retrieved from a subsequent request in the same dialog by calling ServletRequest.getParameter(java.lang.String).
     * Note that the URI returned is good only for specifying a set of parameters that the application can retrieve when invoked to handle subsequent requests in the same dialog. Other components of the URI are irrelevant and cannot be trusted to reflect the actual values that the container will be using when inserting a Record-Route header into proxied request.
     */
    javax.servlet.sip.SipURI getRecordRouteURI();

    /**
     * Receipt of a 3xx class redirect response on a branch can result in recursed branches if the proxy or the branch has recursion enabled. This can result in several levels of recursed branches in a tree like fashion. This method returns the top level branches directly below this ProxyBranch
     */
    List<javax.servlet.sip.ProxyBranch> getRecursedProxyBranches();

    /**
     * Returns the request associated with this branch.
     */
    javax.servlet.sip.SipServletRequest getRequest();

    /**
     * Returns the last response received on this branch.
     */
    javax.servlet.sip.SipServletResponse getResponse();

    /**
     * The branch can be created using
     * and may be started at a later time by using
     * . This method tells if the given branch has been started yet or not. The branches created as a result of proxyTo are always started on creation.
     */
    boolean isStarted();

    /**
     * Specifies whether this branch should include a Record-Route header for this servlet engine or not.
     * 
     * Record-routing is used to specify that this servlet engine must stay on the signaling path of subsequent requests. 
     * 
     * @param rr if true the engine will record-route, otherwise it won't 
     * @throws IllegalStateException if the proxy has already been started
     */
    void setRecordRoute(boolean rr);
    
    /**
     * Specifies whether the servlet engine will automatically recurse or not. 
     * If recursion is enabled the servlet engine will automatically attempt to proxy 
     * to contact addresses received in redirect (3xx) responses. 
     * If recursion is disabled and no better response is received, a redirect response 
     * will be passed to the application and will be passed upstream towards the client.
     * @param recurse if true enables recursion, otherwise disables it
     */
    void setRecurse(boolean recurse);
    
    /**
     * Specifies whether this branch should include a Path header for the REGISTER request 
     * for this servlet container or not.
     * Path header is used to specify that this Proxy must stay on the signaling path 
     * of subsequent requests sent to the Registered UA from the Home Proxy in the network. 
     * 
     * As a best practice, before calling this method a proxy should check if the UA has indicated support 
     * for the Path extension by checking the Supported header field value in the request being proxied. 
     * The detailed procedure of Path header handling is defined in RFC 3327.
     * 
     * @param p if true the container will add Path header
     * @since 1.1
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
     * Sets the search timeout value for this ProxyBranch object. This is the amount of time the container waits for a final response when proxying on this branch. This method can be used to override the default timeout the branch obtains from the
     * object. When the timer expires the container CANCELs this branch and proxies to the next element in the target set in case the proxy is a sequential proxy. In case the proxy is a parallel proxy then this can only set the timeout value of this branch to a value lower than the value in the proxy
     * . The effect of expiry of this timeout in case of parallel proxy is just to cancel this branch as if an explicit call to
     * has been made.
     */
    void setProxyBranchTimeout(int seconds);

}
