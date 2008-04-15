/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.servlet.sip;

import java.util.List;

/**
 * A ProxyBranch represents a branch which the Proxy sends out the request on. The ProxyBranch object models the branch as used in RFC3261 while describing a stateful proxy. For example, public void doInvite(SipServletRequest req) { ... Proxy p = req.getProxy(); p.setRecordRoute(true); List branches = createProxyBranches(getTargets(req)); branches.get(0).setProxyBranchTimeout(5); branches.get(1).setProxyBranchTimeout(10); p.startProxy(); ... }
 * @since 1.1
 */
public interface ProxyBranch{
    /**
     * Cancels this branch and all the child branches if recursion is enabled and sends a CANCEL to the proxied INVITEs. The effect is similar to
     * except that it is limited to this branch and its children only.
     */
    void cancel();

    javax.servlet.sip.Proxy getProxy();

    /**
     * Returns the current value of the search timeout associated with this ProxyBranch object. If this value is not explicitly set using the
     * then the value is inherited from the Proxy setting.
     */
    int getProxyBranchTimeout();

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
     * If multihoming is supported, then this method can be used to select the outbound interface to when forwarding requests for this proxy branch. The specified address must be the address of one of the configured outbound interfaces. The set of SipURI objects which represent the supported outbound interfaces can be obtained from the servlet context attribute named javax.servlet.sip.outboundInterfaces.
     */
    void setOutboundInterface(javax.servlet.sip.SipURI uri);

    /**
     * Sets the search timeout value for this ProxyBranch object. This is the amount of time the container waits for a final response when proxying on this branch. This method can be used to override the default timeout the branch obtains from the
     * object. When the timer expires the container CANCELs this branch and proxies to the next element in the target set in case the proxy is a sequential proxy. In case the proxy is a parallel proxy then this can only set the timeout value of this branch to a value lower than the value in the proxy
     * . The effect of expiry of this timeout in case of parallel proxy is just to cancel this branch as if an explicit call to
     * has been made.
     */
    void setProxyBranchTimeout(int seconds);

}
