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
 * Helper class providing support for B2BUA applications. An instance of this
 * class can be retrieved from a SipServletRequest by invoking the method
 * SipServletRequest.getB2buaHelper().
 * 
 * @since 1.1
 */
public interface B2buaHelper {
	/**
	 * Creates a new CANCEL request to cancel the initial request sent on the other leg. 
	 * The CANCEL is created by the container using the initial request stored in the session corresponding to the other leg. 
	 * @param session the session whose initial request is to be cancelled. 
	 * @return the new CANCEL request to be sent on the other leg 
	 * @throws NullPointerException if the session is null
	 */
	SipServletRequest createCancel(SipSession session);
	/**
	 * Creates a new request object belonging to a new SipSession. The new request is similar to the specified origRequest  in that the method and the majority of header fields are copied from origRequest to the new request. The SipSession created for the new request also shares the same SipApplicationSession associated with the original request.
	 * 
	 * This method satisfies the following rules:
	 * 
	 *     * The From header field of the new request has a new tag chosen by the container.
	 *     * The To header field of the new request has no tag.
	 *     * The new request (and the corresponding SipSession)is assigned a new Call-ID.
	 *     * Record-Route and Via header fields are not copied. As usual, the container will add its own Via header field to the request when it's actually sent outside the application server.
	 *     * For non-REGISTER requests, the Contact header field is not copied but is populated by the container as usual. 
	 *     
	 *     This method provides a convenient and efficient way of constructing a new "leg" of a B2BUA application. It is used only for the initial request. Subsequent requests in either leg must be created using SipSession.createRequest(java.lang.String) or createRequest(SipSession, SipServletRequest, java.util.Map) as usual. 
	 *     
	 * @param origRequest request to be "copied" 
	 * @return the "copied" request object
	 */
	SipServletRequest createRequest(SipServletRequest origRequest);
	
	/**
	 * Creates a new request object belonging to a new SipSession. The new
	 * request is similar to the specified origRequest in that the method and
	 * the majority of header fields are copied from origRequest to the new
	 * request. The headerMap parameter can contain From, To, Contact, Route headers and any
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
	 * additional flexibility of changing the headers including To, From, Contact, Route.
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
			java.util.Map<java.lang.String, List<java.lang.String>> headerMap)
			throws java.lang.IllegalArgumentException, TooManyHopsException;

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
	 * origRequest. IIf Contact header is present in the headerMap then relevant portions 
	 * of the Contact header are to be used in the request created, in accordance with section 4.1.3 of the specification.
	 * If From and To headers are present in the headerMap then all parts of
	 * those headers except the tag parameter are to be used in the request 
	 * created, in accordance with section 4.1.2 of the specification.
	 * @throws IllegalArgumentException if the header map contains a system header other than Contact (see section 4.1.3 of specification document)
	 * or other header which does not makes sense in the context, 
	 * or in case when the <code>session</code> does not belong to the same 
	 * SipApplicationSession as the <code>origRequest</code>, or the original request or session is 
	 * already linked with some other request/session,
	 * or if the <code>origRequest</code> is not initial
	 * @throws IllegalArgumentException     
	 * 	if the header map contains a system header other than Contact, From or To (see sections 4.1.2 and 4.1.3 of specification document) 
	 *  or other header which does not makes sense in the context, or in case when 
	 *  the session does not belong to the same SipApplicationSession as the origRequest, 
	 *  or the original request or session is already linked with some other request/session, 
	 *  or if the origRequest is not initial 
	 *  @throws java.lang.NullPointerException - if the original request or the session is null
	 */
	javax.servlet.sip.SipServletRequest createRequest(
			javax.servlet.sip.SipSession session,
			javax.servlet.sip.SipServletRequest origRequest,
			java.util.Map<java.lang.String, List<java.lang.String>> headerMap)
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
	 * responses. The response thus generated must have a different “To�? tag
	 * from the other responses generated to the Request and must result in a
	 * different SipSession. In this (and similar) cases the container clones
	 * the original SipSession for the second and subsequentdialogs, as detailed
	 * above. The cloned session object will contain the same application data
	 * but its createRequest method will create requests belonging to that
	 * second or subsequent dialog, that is, with a “To�? tag specific to
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
