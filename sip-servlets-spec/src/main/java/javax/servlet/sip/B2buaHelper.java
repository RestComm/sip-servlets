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
import java.util.Set;

/**
 * Helper class providing support for B2BUA applications. An instance of this
 * class can be retrieved from a SipServletRequest by invoking the method
 * SipServletRequest.getB2buaHelper().
 * 
 * @since 1.1
 */
public interface B2buaHelper {
	/**
	 * Creates a new request object belonging to a new SipSession. The new
	 * request is similar to the specified origRequest in that the method and
	 * the majority of header fields are copied from origRequest to the new
	 * request. The headerMap parameter can contain From and To headers and any
	 * non system header. The header field map is then used to override the
	 * headers in the newly created request. The SipSession created for the new
	 * request also shares the same SipApplicationSession associated with the
	 * original request. This method satisfies the following rules: Whether the
	 * From header is overridden through the headerMap or not the From header
	 * field of the new request has a new tag chosen by the container. If the
	 * From header is included in the headerMap and has a tag then it is ignored
	 * and container chosen tag is inserted instead. The To header field of the
	 * new request has no tag. Record-Route and Via header fields are not
	 * copied. As usual, the container will add its own Via header field to the
	 * request when it's actually sent outside the application server. For
	 * non-REGISTER requests, the Contact header field is not copied but is
	 * populated by the container as usual but if Contact header is present in
	 * the headerMap then relevant portions of Contact header is to be used in
	 * the request created, in accordance with section 4.1.3 of the
	 * specification. This method provides a convenient and efficient way of
	 * constructing the second "leg" of a B2BUA application, giving the
	 * additional flexibility of changing the headers including To and From.
	 * This method will also perform loop detection. If the value of the
	 * original request's Max-Forwards header field is 0, then
	 * TooManyHopsException is thrown and a 483 (Too many hops) response is sent
	 * for the original request. Otherwise, the value of the new requests
	 * Max-Forwards header is set to that of the original request minus one. It
	 * is used only for the initial request. Subsequent requests in either leg
	 * must be created using SipSession.createRequest(java.lang.String) or
	 * createRequest(SipSession, SipServletRequest, java.util.Map) as usual.
	 */
	javax.servlet.sip.SipServletRequest createRequest(
			javax.servlet.sip.SipServletRequest origRequest, boolean linked,
			java.util.Map<java.lang.String, Set<java.lang.String>> headerMap)
			throws java.lang.IllegalArgumentException;

	/**
	 * Creates a new subsequent request based on the specified original request.
	 * This results in automatically linking the two SipSessions (if they are
	 * not already linked) and the two SipServletRequests. This method, though
	 * similar to the factory method of creating the request for a B2BUA for
	 * initial requests, is to be used for subsequent requests. The semantics
	 * are similar to SipSession.createRequest(String) except that here it
	 * copies non system headers from the original request onto the new request,
	 * the system headers are created based on the session that this request is
	 * created on. Further the Route headers are set as based on the session
	 * route set. The method of the new request is same as that of the
	 * origRequest. If Contact header is present in the headerMap then relevant
	 * portions of Contact header is to be used in the request created, in
	 * accordance with section 4.1.3 of the specification.
	 */
	javax.servlet.sip.SipServletRequest createRequest(
			javax.servlet.sip.SipSession session,
			javax.servlet.sip.SipServletRequest origRequest,
			java.util.Map<java.lang.String, Set<java.lang.String>> headerMap)
			throws java.lang.IllegalArgumentException;

	/**
	 * The request that results in creation of a SipSession is termed as the
	 * original request, a response to this original request can be created by
	 * the application even if the request was committed and application does
	 * not have a reference to this Request. This is required because the B2BUA
	 * application may require to send more than one successful response to a
	 * request. For example when a downstream proxy forked and more than one
	 * success responses are to be forwarded upstream. This can only be required
	 * on initial requests, as only original requests shall need such multiple
	 * responses. The response thus generated must have a different â€œToâ€? tag
	 * from the other responses generated to the Request and must result in a
	 * different SipSession. In this (and similar) cases the container clones
	 * the original SipSession for the second and subsequentdialogs, as detailed
	 * above. The cloned session object will contain the same application data
	 * but its createRequest method will create requests belonging to that
	 * second or subsequent dialog, that is, with a â€œToâ€? tag specific to
	 * that dialog.
	 */
	javax.servlet.sip.SipServletResponse createResponseToOriginalRequest(
			javax.servlet.sip.SipSession session, int status,
			java.lang.String reasonPhrase);

	/**
	 * Returns the other SipSession that is linked to the specified SipSession,
	 * or null if none. The sessions get linked by either explicit call to or
	 */
	javax.servlet.sip.SipSession getLinkedSession(
			javax.servlet.sip.SipSession session);

	/**
	 * If a new request is created based on an existing one with the link
	 * argument true using or using the new request gets implicitly linked with
	 * the original request. This method is to be used to retrieve the linked
	 * request. There is no explicit linking/unlinking like that of the session
	 * for requests. However unlinking of sessions also result in unlinking of
	 * linked requests.
	 */
	javax.servlet.sip.SipServletRequest getLinkedSipServletRequest(
			javax.servlet.sip.SipServletRequest req);

	/**
	 * For the specified SipSession, returns a List of all uncommitted messages
	 * in the order of increasing CSeq number for the given mode of the session.
	 */
	List<javax.servlet.sip.SipServletMessage> getPendingMessages(
			javax.servlet.sip.SipSession session, javax.servlet.sip.UAMode mode);

	/**
	 * Links the specified sessions, such that there is a 1-1 mapping between
	 * them. Each session can retrieved from the other by calling . One
	 * SipSession at any given time can be linked to only one other SipSession
	 * belonging to the same SipApplicationSession. Calling linkSessions() with
	 * already linked sessions to each other is a no-op and is to be silently
	 * ignored, while calling linkSessions() with any of the session linked with
	 * sessions result in java.lang.IllegalArgumentException
	 */
	void linkSipSessions(javax.servlet.sip.SipSession session1,
			javax.servlet.sip.SipSession session2);

	/**
	 * If the specified SipSession is linked to another session, then unlinks
	 * the two sessions from each other. This also implicitly unlinks the
	 * linkage at the request level.
	 */
	void unlinkSipSessions(javax.servlet.sip.SipSession session);

}
