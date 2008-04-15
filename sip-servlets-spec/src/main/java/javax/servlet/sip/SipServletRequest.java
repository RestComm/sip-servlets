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
/**
 * Represents SIP request messages. When receiving an incoming SIP request the container creates a SipServletRequest and passes it to the handling servlet. For outgoing, locally initiated requests, applications call SipFactory.createRequest to obtain a SipServletRequest that can then be modified and sent.
 */
public interface SipServletRequest extends javax.servlet.sip.SipServletMessage, javax.servlet.ServletRequest{
    /**
     * Returns a CANCEL request object. This method is used by applications to cancel outstanding transactions for which they act as a user agent client (UAC). The CANCEL request is sent when the application invokes
     * on it.
     * Note that proxy applications MUST use Proxy.cancel() to cancel outstanding branches.
     */
    javax.servlet.sip.SipServletRequest createCancel();

    /**
     * Creates a response for this request with the specifies status code.
     */
    javax.servlet.sip.SipServletResponse createResponse(int statuscode);

    /**
     * Creates a response for this request with the specifies status code and reason phrase.
     */
    javax.servlet.sip.SipServletResponse createResponse(int statusCode, java.lang.String reasonPhrase);

    /**
     * Returns the B2buaHelper associated with this request. Invocation of this method also indicates to the container that the application wishes to be a B2BUA, and any subsequent call to
     * will result in IllegalStateException.
     */
    javax.servlet.sip.B2buaHelper getB2buaHelper();

    /**
     * Always returns null. SIP is not a content transfer protocol and having stream based content accessors is of little utility.
     * Message content can be retrieved using SipServletMessage.getContent() and SipServletMessage.getRawContent().
     */
    javax.servlet.ServletInputStream getInputStream() throws java.io.IOException;

    /**
     * Returns the value of the Max-Forwards header.
     */
    int getMaxForwards();

    /**
     * If a top route header had been removed by the container upon receiving this request, then this method can be used to retrieve it. Otherwise, if no route header had been popped then this method will return null.
     */
    javax.servlet.sip.Address getPoppedRoute();

    /**
     * Returns the Proxy object associated with this request. A Proxy instance will be created if one doesn't already exist. This method behaves the same as getProxy(true).
     * Note that the container must return the same Proxy instance whenever a servlet invokes getProxy on messages belonging to the same transaction. In particular, a response to a proxied request is associated with the same Proxy object as is the original request.
     * This method throws an IllegalStateException if the Proxy object didn't already exist and the transaction underlying this SIP message is in a state which doesn't allow proxying, for example if this is a SipServletRequest for which a final response has already been generated.
     * If the request contains a Max-Forwards header field value of 0, then this method will generate a 483 (Too many hops) error response and throw TooManyHopsException.
     */
    javax.servlet.sip.Proxy getProxy() throws javax.servlet.sip.TooManyHopsException;

    /**
     * Returns the Proxy object associated with this request. If no Proxy object has yet been created for this request, the create argument specifies whether a Proxy object is to be created or not.
     * Once a Proxy object has been associated with a request subsequent invocations of this method will yield the same Proxy object, as will the no-argument getProxy() method and SipServletResponse.getProxy() for responses received to proxied requests.
     */
    javax.servlet.sip.Proxy getProxy(boolean create) throws javax.servlet.sip.TooManyHopsException;

    /**
     * Always returns null. SIP is not a content transfer protocol and having stream based content accessors is of little utility.
     * Message content can be retrieved using SipServletMessage.getContent() and SipServletMessage.getRawContent().
     */
    java.io.BufferedReader getReader() throws java.io.IOException;

    /**
     * Returns the request URI of this request.
     */
    javax.servlet.sip.URI getRequestURI();

    /**
     * Returns true if this is an initial request. An initial request is one that is dispatched to applications based on the containers configured rule set, as opposed to subsequent requests which are routed based on the application path established by a previous initial request.
     */
    boolean isInitial();

    /**
     * Adds a Path header field value to this request. The new value is added ahead of any existing Path header fields. If this request does not already container a Path header, one is added with the value specified in the argument. This method allows a UAC or a proxy to add Path on a REGISTER Request.
     */
    void pushPath(javax.servlet.sip.Address uri);

    /**
     * Adds a Route header field value to this request with Address argument. The new value is added ahead of any existing Route header fields. If this request does not already contains a Route header, one is added with the value as specified in the argument.
     * This method allows a UAC or a proxy to specify that the request should visit one or more proxies before being delivered to the destination.
     */
    void pushRoute(javax.servlet.sip.Address uri);

    /**
     * Adds a Route header field value to this request. The new value is added ahead of any existing Route header fields. If this request does not already contains a Route header, one is added with the value as specified in the argument.
     * This method allows a UAC or a proxy to specify that the request should visit one or more proxies before being delivered to the destination.
     */
    void pushRoute(javax.servlet.sip.SipURI uri);

    /**
     * Causes this request to be sent. This method is used by SIP servlets acting as user agent clients (UACs) only. Proxying applications use
     * instead.
     */
    void send() throws java.io.IOException;

    /**
     * Sets the value of the Max-Forwards header. Max-Forwards serves to limit the number of hops a request can make on the way to its destination. It consists of an integer that is decremented by one at each hop.
     * This method is equivalent to: setHeader("Max-Forwards", String.valueOf(n));
     */
    void setMaxForwards(int n);

    /**
     * Sets the request URI of this request. This then becomes the destination used in a subsequent invocation of
     * .
     */
    void setRequestURI(javax.servlet.sip.URI uri);

    /**
     * Sets the application routing directive for an outgoing request.
     * By default, a request created by SipFactory.createRequest(SipServletRequest origRequest, boolean sameCallId) continues the application selection process from origRequest, i.e. directive is CONTINUE. A request created by the other SipFactory.createRequest() methods starts the application selection process afresh, i.e. directive is NEW.
     * This method allows the servlet to assign a routing directive different from the default.
     * If directive is NEW, origRequest parameter is ignored. If directive is CONTINUE or REVERSE, the parameter origRequest must be an initial request dispatched by the container to this application, i.e. origRequest.isInitial() must be true. This request must be a request created in a new SipSession or from an initial request, and must not have been sent. If any one of these preconditions are not met, the method throws an IllegalStateException.
     * Note that when a servlet acts as a proxy and calls Proxy.proxyTo() to proxy a request, the request is always a continuation.
     */
    void setRoutingDirective(javax.servlet.sip.SipApplicationRoutingDirective directive, javax.servlet.sip.SipServletRequest origRequest) throws java.lang.IllegalStateException;

}
