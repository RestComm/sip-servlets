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

/*
  +---------------------------------------------------------------------------+
  | Facebook Development Platform Java Client                                 |
  +---------------------------------------------------------------------------+
  | Copyright (c) 2007-2008 Facebook, Inc.                                    |
  | All rights reserved.                                                      |
  |                                                                           |
  | Redistribution and use in source and binary forms, with or without        |
  | modification, are permitted provided that the following conditions        |
  | are met:                                                                  |
  |                                                                           |
  | 1. Redistributions of source code must retain the above copyright         |
  |    notice, this list of conditions and the following disclaimer.          |
  | 2. Redistributions in binary form must reproduce the above copyright      |
  |    notice, this list of conditions and the following disclaimer in the    |
  |    documentation and/or other materials provided with the distribution.   |
  |                                                                           |
  | THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR      |
  | IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES |
  | OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.   |
  | IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,          |
  | INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT  |
  | NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, |
  | DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY     |
  | THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT       |
  | (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF  |
  | THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.         |
  +---------------------------------------------------------------------------+
  | For help with this library, contact developers-help@facebook.com          |
  +---------------------------------------------------------------------------+
*/

package com.facebook.api;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Base class for interacting with the Facebook Application Programming Interface (API).
 * Most Facebook API methods map directly to function calls of this class.
 * <br/>
 * Instances of FacebookRestClient should be initialized via calls to
 * {@link #auth_createToken}, followed by {@link #auth_getSession}.
 * <br/>
 * For continually updated documentation, please refer to the
 * <a href="http://wiki.developers.facebook.com/index.php/API">
 * Developer Wiki</a>.
 */
public abstract class FacebookRestClient<T>
  implements IFacebookRestClient<T> {

  public static URL SERVER_URL = null;
  public static URL HTTPS_SERVER_URL = null;
  static {
    try {
      SERVER_URL = new URL(SERVER_ADDR);
      HTTPS_SERVER_URL = new URL(HTTPS_SERVER_ADDR);
    } catch (MalformedURLException e) {
      System.err.println("MalformedURLException: " + e.getMessage());
      System.exit(1);
    }
  }

  protected final String _secret;
  protected final String _apiKey;
  protected final URL _serverUrl;

  protected String _sessionKey;
  protected boolean _isDesktop = false;
  protected int _userId = -1;

  /**
   * filled in when session is established
   * only used for desktop apps
   */
  protected String _sessionSecret;

  /**
   * The number of parameters required for every request.
   * @see #callMethod(IFacebookMethod,Collection)
   */
  public static int NUM_AUTOAPPENDED_PARAMS = 6;

  private static boolean DEBUG = false;
  protected Boolean _debug = null;

  protected File _uploadFile = null;
  protected static final String CRLF = "\r\n";
  protected static final String PREF = "--";
  protected static final int UPLOAD_BUFFER_SIZE = 512;

  public static final String MARKETPLACE_STATUS_DEFAULT = "DEFAULT";
  public static final String MARKETPLACE_STATUS_NOT_SUCCESS = "NOT_SUCCESS";
  public static final String MARKETPLACE_STATUS_SUCCESS = "SUCCESS";


  protected FacebookRestClient(URL serverUrl, String apiKey, String secret, String sessionKey) {
    _sessionKey = sessionKey;
    _apiKey = apiKey;
    _secret = secret;
    _serverUrl = (null != serverUrl) ? serverUrl : SERVER_URL;
  }

  /**
   * The response format in which results to FacebookMethod calls are returned
   * @return the format: either XML, JSON, or null (API default)
   */
  public String getResponseFormat() {
    return null;
  }

  /**
   * Retrieves whether two users are friends.
   * @param userId1
   * @param userId2
   * @return T
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.areFriends">
   *      Developers Wiki: Friends.areFriends</a>
   */
  public T friends_areFriends(int userId1, int userId2)
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.FRIENDS_ARE_FRIENDS,
                           new Pair<String, CharSequence>("uids1", Integer.toString(userId1)),
                           new Pair<String, CharSequence>("uids2", Integer.toString(userId2)));
  }

  /**
   * Retrieves whether pairs of users are friends.
   * Returns whether the first user in <code>userIds1</code> is friends with the first user in
   * <code>userIds2</code>, the second user in <code>userIds1</code> is friends with the second user in
   * <code>userIds2</code>, etc.
   * @param userIds1
   * @param userIds2
   * @return T
   * @throws IllegalArgumentException if one of the collections is null, or empty, or if the
   *         collection sizes differ.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.areFriends">
   *      Developers Wiki: Friends.areFriends</a>
   */
  public T friends_areFriends(Collection<Integer> userIds1, Collection<Integer> userIds2)
    throws FacebookException, IOException {
    if (userIds1 == null || userIds2 == null || userIds1.isEmpty() || userIds2.isEmpty()) {
      throw new IllegalArgumentException("Collections passed to friends_areFriends should not be null or empty");
    }
    if (userIds1.size() != userIds2.size()) {
      throw new IllegalArgumentException(String.format("Collections should be same size: got userIds1: %d elts; userIds2: %d elts",
                                                       userIds1.size(), userIds2.size()));
    }

    return this.callMethod(FacebookMethod.FRIENDS_ARE_FRIENDS,
                           new Pair<String, CharSequence>("uids1", delimit(userIds1)),
                           new Pair<String, CharSequence>("uids2", delimit(userIds2)));
  }

  /**
   * Gets the FBML for a user's profile, including the content for both the profile box
   * and the profile actions.
   * @param userId the user whose profile FBML to set
   * @return a T containing FBML markup
   */
  public T profile_getFBML(Integer userId)
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.PROFILE_GET_FBML,
                           new Pair<String, CharSequence>("uid", Integer.toString(userId)));

  }

  /**
   * Recaches the referenced url.
   * @param url string representing the URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshRefUrl(String url)
    throws FacebookException, IOException {
    return fbml_refreshRefUrl(new URL(url));
  }

  /**
   * Helper function: assembles the parameters used by feed_publishActionOfUser and
   * feed_publishStoryToUser
   * @param feedMethod feed_publishStoryToUser / feed_publishActionOfUser
   * @param title title of the story
   * @param body body of the story
   * @param images optional images to be included in he story
   * @param priority
   * @return whether the call to <code>feedMethod</code> was successful
   */
  protected boolean feedHandler(IFacebookMethod feedMethod, CharSequence title, CharSequence body,
                                Collection<IFeedImage> images, Integer priority)
    throws FacebookException, IOException {
    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(feedMethod.numParams());

    params.add(new Pair<String, CharSequence>("title", title));
    if (null != body)
      params.add(new Pair<String, CharSequence>("body", body));
    if (null != priority)
      params.add(new Pair<String, CharSequence>("priority", priority.toString()));

    handleFeedImages(params, images);

    return extractBoolean(this.callMethod(feedMethod, params));
  }

  /**
   * Adds image parameters to a list of parameters
   * @param params
   * @param images
   */
  protected void handleFeedImages(List<Pair<String, CharSequence>> params,
                                  Collection<IFeedImage> images) {
    if (images != null && images.size() > 4) {
      throw new IllegalArgumentException("At most four images are allowed, got " +
                                         Integer.toString(images.size()));
    }
    if (null != images && !images.isEmpty()) {
      int image_count = 0;
      for (IFeedImage image : images) {
        ++image_count;
        String imageUrl = image.getImageUrlString(); 
        assert null != imageUrl && "".equals(imageUrl) : "Image URL must be provided";
        params.add(new Pair<String, CharSequence>(String.format("image_%d", image_count),
                                                  image.getImageUrlString()));
        assert null != image.getLinkUrl() : "Image link URL must be provided";
        params.add(new Pair<String, CharSequence>(String.format("image_%d_link", image_count),
                                                  image.getLinkUrl().toString()));
      }
    }
  }

  /**
   * Publish the notification of an action taken by a user to newsfeed.
   * @param title the title of the feed story (up to 60 characters, excluding tags)
   * @param body (optional) the body of the feed story (up to 200 characters, excluding tags)
   * @param images (optional) up to four pairs of image URLs and (possibly null) link URLs
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishActionOfUser">
   *      Developers Wiki: Feed.publishActionOfUser</a>
   */
  public boolean feed_publishActionOfUser(CharSequence title, CharSequence body,
                                          Collection<IFeedImage> images)
    throws FacebookException, IOException {
    return feedHandler(FacebookMethod.FEED_PUBLISH_ACTION_OF_USER, title, body, images, null);
  }

  /**
   * Publish the notification of an action taken by a user to newsfeed.
   * @param title the title of the feed story (up to 60 characters, excluding tags)
   * @param body (optional) the body of the feed story (up to 200 characters, excluding tags)
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishActionOfUser">
   *      Developers Wiki: Feed.publishActionOfUser</a>
   */
  public boolean feed_publishActionOfUser(CharSequence title, CharSequence body)
    throws FacebookException, IOException {
    return feed_publishActionOfUser(title, body, null);
  }

  /**
   * Call this function to retrieve the session information after your user has
   * logged in.
   * @param authToken the token returned by auth_createToken or passed back to your callback_url.
   */
  public abstract String auth_getSession(String authToken)
    throws FacebookException, IOException;

  /**
   * Publish a story to the logged-in user's newsfeed.
   * @param title the title of the feed story
   * @param body the body of the feed story
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishStoryToUser">
   *      Developers Wiki: Feed.publishStoryToUser</a>
   */
  public boolean feed_publishStoryToUser(CharSequence title, CharSequence body)
    throws FacebookException, IOException {
    return feed_publishStoryToUser(title, body, null, null);
  }

  /**
   * Publish a story to the logged-in user's newsfeed.
   * @param title the title of the feed story
   * @param body the body of the feed story
   * @param images (optional) up to four pairs of image URLs and (possibly null) link URLs
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishStoryToUser">
   *      Developers Wiki: Feed.publishStoryToUser</a>
   */
  public boolean feed_publishStoryToUser(CharSequence title, CharSequence body,
                                         Collection<IFeedImage> images)
    throws FacebookException, IOException {
    return feed_publishStoryToUser(title, body, images, null);
  }

  /**
   * Publish a story to the logged-in user's newsfeed.
   * @param title the title of the feed story
   * @param body the body of the feed story
   * @param priority
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishStoryToUser">
   *      Developers Wiki: Feed.publishStoryToUser</a>
   */
  public boolean feed_publishStoryToUser(CharSequence title, CharSequence body, Integer priority)
    throws FacebookException, IOException {
    return feed_publishStoryToUser(title, body, null, priority);
  }

  /**
   * Publish a story to the logged-in user's newsfeed.
   * @param title the title of the feed story
   * @param body the body of the feed story
   * @param images (optional) up to four pairs of image URLs and (possibly null) link URLs
   * @param priority
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishStoryToUser">
   *      Developers Wiki: Feed.publishStoryToUser</a>
   */
  public boolean feed_publishStoryToUser(CharSequence title, CharSequence body,
                                         Collection<IFeedImage> images, Integer priority)
    throws FacebookException, IOException {
    return feedHandler(FacebookMethod.FEED_PUBLISH_STORY_TO_USER, title, body, images, priority);
  }

  /**
   * Publishes a Mini-Feed story describing an action taken by a user, and
   * publishes aggregating News Feed stories to the friends of that user.
   * Stories are identified as being combinable if they have matching templates and substituted values.
   * @param actorId deprecated
   * @param titleTemplate markup (up to 60 chars, tags excluded) for the feed story's title
   *        section. Must include the token <code>{actor}</code>.
   * @return whether the action story was successfully published; false in case
   *         of a permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction">
   *      Developers Wiki: Feed.publishTemplatizedAction</a>
   * @see <a href="http://developers.facebook.com/tools.php?feed">
   *      Developers Resources: Feed Preview Console </a>
   * @deprecated since 01/18/2008
   */
  public boolean feed_publishTemplatizedAction(Integer actorId, CharSequence titleTemplate)
    throws FacebookException, IOException {
    if (null != actorId && actorId != this._userId) {
      throw new IllegalArgumentException("Actor ID parameter is deprecated");
    }
    return feed_publishTemplatizedAction(titleTemplate, null, null, null, null, null, null, /*pageActorId*/ null);
  }

  /**
   * Publishes a Mini-Feed story describing an action taken by the logged-in user, and
   * publishes aggregating News Feed stories to their friends.
   * Stories are identified as being combinable if they have matching templates and substituted values.
   * @param titleTemplate markup (up to 60 chars, tags excluded) for the feed story's title
   *        section. Must include the token <code>{actor}</code>.
   * @return whether the action story was successfully published; false in case
   *         of a permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction">
   *      Developers Wiki: Feed.publishTemplatizedAction</a>
   * @see <a href="http://developers.facebook.com/tools.php?feed">
   *      Developers Resources: Feed Preview Console </a>
   */
  public boolean feed_publishTemplatizedAction(CharSequence titleTemplate)
    throws FacebookException, IOException {
    return feed_publishTemplatizedAction(titleTemplate, null, null, null, null, null, null, /*pageActorId*/ null);
  }

  /**
   * Publishes a Mini-Feed story describing an action taken by the logged-in user (or, if 
   * <code>pageActorId</code> is provided, page), and publishes aggregating News Feed stories 
   * to the user's friends/page's fans.
   * Stories are identified as being combinable if they have matching templates and substituted values.
   * @param titleTemplate markup (up to 60 chars, tags excluded) for the feed story's title
   *        section. Must include the token <code>{actor}</code>.
   * @param pageActorId (optional) the ID of the page into whose mini-feed the story is being published
   * @return whether the action story was successfully published; false in case
   *         of a permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction">
   *      Developers Wiki: Feed.publishTemplatizedAction</a>
   * @see <a href="http://developers.facebook.com/tools.php?feed">
   *      Developers Resources: Feed Preview Console </a>
   */
  public boolean feed_publishTemplatizedAction(CharSequence titleTemplate, Long pageActorId)
    throws FacebookException, IOException {
    return feed_publishTemplatizedAction(titleTemplate, null, null, null, null, null, null, pageActorId);
  }

  /**
   * Publishes a Mini-Feed story describing an action taken by the logged-in user, and
   * publishes aggregating News Feed stories to their friends.
   * Stories are identified as being combinable if they have matching templates and substituted values.
   * @param actorId deprecated.
   * @param titleTemplate markup (up to 60 chars, tags excluded) for the feed story's title
   *        section. Must include the token <code>{actor}</code>.
   * @param titleData (optional) contains token-substitution mappings for tokens that appear in
   *        titleTemplate. Should not contain mappings for the <code>{actor}</code> or
   *        <code>{target}</code> tokens. Required if tokens other than <code>{actor}</code>
   *        or <code>{target}</code> appear in the titleTemplate.
   * @param bodyTemplate (optional) markup to be displayed in the feed story's body section.
   *        can include tokens, of the form <code>{token}</code>, to be substituted using
   *        bodyData.
   * @param bodyData (optional) contains token-substitution mappings for tokens that appear in
   *        bodyTemplate. Required if the bodyTemplate contains tokens other than <code>{actor}</code>
   *        and <code>{target}</code>.
   * @param bodyGeneral (optional) additional body markup that is not aggregated. If multiple instances
   *        of this templated story are combined together, the markup in the bodyGeneral of
   *        one of their stories may be displayed.
   * @param targetIds The user ids of friends of the actor, used for stories about a direct action between
   *        the actor and these targets of his/her action. Required if either the titleTemplate or bodyTemplate
   *        includes the token <code>{target}</code>.
   * @param images (optional) additional body markup that is not aggregated. If multiple instances
   *        of this templated story are combined together, the markup in the bodyGeneral of
   *        one of their stories may be displayed.
   * @return whether the action story was successfully published; false in case
   *         of a permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction">
   *      Developers Wiki: Feed.publishTemplatizedAction</a>
   * @see <a href="http://developers.facebook.com/tools.php?feed">
   *      Developers Resources: Feed Preview Console </a>
   * @deprecated since 01/18/2008
   */
  public boolean feed_publishTemplatizedAction(Integer actorId, CharSequence titleTemplate,
                                               Map<String, CharSequence> titleData,
                                               CharSequence bodyTemplate,
                                               Map<String, CharSequence> bodyData,
                                               CharSequence bodyGeneral,
                                               Collection<Integer> targetIds,
                                               Collection<IFeedImage> images)
  throws FacebookException, IOException {
    return feed_publishTemplatizedAction(titleTemplate, titleData, bodyTemplate, bodyData, bodyGeneral,
                                         targetIds, images, /*pageActorId*/ null);
  }

  /**
   * Publishes a Mini-Feed story describing an action taken by the logged-in user (or, if 
   * <code>pageActorId</code> is provided, page), and publishes aggregating News Feed stories 
   * to the user's friends/page's fans.
   * Stories are identified as being combinable if they have matching templates and substituted values.
   * @param titleTemplate markup (up to 60 chars, tags excluded) for the feed story's title
   *        section. Must include the token <code>{actor}</code>.
   * @param titleData (optional) contains token-substitution mappings for tokens that appear in
   *        titleTemplate. Should not contain mappings for the <code>{actor}</code> or
   *        <code>{target}</code> tokens. Required if tokens other than <code>{actor}</code>
   *        or <code>{target}</code> appear in the titleTemplate.
   * @param bodyTemplate (optional) markup to be displayed in the feed story's body section.
   *        can include tokens, of the form <code>{token}</code>, to be substituted using
   *        bodyData.
   * @param bodyData (optional) contains token-substitution mappings for tokens that appear in
   *        bodyTemplate. Required if the bodyTemplate contains tokens other than <code>{actor}</code>
   *        and <code>{target}</code>.
   * @param bodyGeneral (optional) additional body markup that is not aggregated. If multiple instances
   *        of this templated story are combined together, the markup in the bodyGeneral of
   *        one of their stories may be displayed.
   * @param targetIds The user ids of friends of the actor, used for stories about a direct action between
   *        the actor and these targets of his/her action. Required if either the titleTemplate or bodyTemplate
   *        includes the token <code>{target}</code>.
   * @param images (optional) additional body markup that is not aggregated. If multiple instances
   *        of this templated story are combined together, the markup in the bodyGeneral of
   *        one of their stories may be displayed.
   * @param pageActorId (optional) the ID of the page into whose mini-feed the story is being published
   * @return whether the action story was successfully published; false in case
   *         of a permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction">
   *      Developers Wiki: Feed.publishTemplatizedAction</a>
   * @see <a href="http://developers.facebook.com/tools.php?feed">
   *      Developers Resources: Feed Preview Console </a>
   */
  public boolean feed_publishTemplatizedAction(CharSequence titleTemplate,
                                               Map<String, CharSequence> titleData,
                                               CharSequence bodyTemplate,
                                               Map<String, CharSequence> bodyData,
                                               CharSequence bodyGeneral,
                                               Collection<Integer> targetIds,
                                               Collection<IFeedImage> images,
                                               Long pageActorId)
    throws FacebookException, IOException {
    assert null != titleTemplate && !"".equals(titleTemplate);

    FacebookMethod method = FacebookMethod.FEED_PUBLISH_TEMPLATIZED_ACTION;
    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(method.numParams());

    params.add(new Pair<String, CharSequence>("title_template", titleTemplate));
    if (null != titleData && !titleData.isEmpty()) {
      JSONObject titleDataJson = new JSONObject();
      titleDataJson.putAll(titleData);
      params.add(new Pair<String, CharSequence>("title_data", titleDataJson.toString()));
    }

    if (null != bodyTemplate && !"".equals(bodyTemplate)) {
      params.add(new Pair<String, CharSequence>("body_template", bodyTemplate));
      if (null != bodyData && !bodyData.isEmpty()) {
        JSONObject bodyDataJson = new JSONObject();
        bodyDataJson.putAll(bodyData);
        params.add(new Pair<String, CharSequence>("body_data", bodyDataJson.toString()));
      }
    }

    if (null != bodyGeneral && !"".equals(bodyGeneral)) {
      params.add(new Pair<String, CharSequence>("body_general", bodyGeneral));
    }

    if (null != targetIds && !targetIds.isEmpty()) {
      params.add(new Pair<String, CharSequence>("target_ids", delimit(targetIds)));
    }

    if (null != pageActorId) {
      params.add(new Pair<String, CharSequence>("page_actor_id", pageActorId.toString()));
    }

    handleFeedImages(params, images);
    return extractBoolean(this.callMethod(method, params));
  }


  /**
   * Retrieves the membership list of a group
   * @param groupId the group id
   * @return a T containing four membership lists of
   * 'members', 'admins', 'officers', and 'not_replied'
   */
  public T groups_getMembers(Number groupId)
    throws FacebookException, IOException {
    assert (null != groupId);
    return this.callMethod(FacebookMethod.GROUPS_GET_MEMBERS,
                           new Pair<String, CharSequence>("gid", groupId.toString()));
  }

  private static String encode(CharSequence target) {
    String result = target.toString();
    try {
      result = URLEncoder.encode(result, "UTF8");
    } catch (UnsupportedEncodingException e) {
      System.err.printf("Unsuccessful attempt to encode '%s' into UTF8", result);
    }
    return result;
  }

  /**
   * Retrieves the membership list of an event
   * @param eventId event id
   * @return T consisting of four membership lists corresponding to RSVP status, with keys
   * 'attending', 'unsure', 'declined', and 'not_replied'
   */
  public T events_getMembers(Number eventId)
    throws FacebookException, IOException {
    assert (null != eventId);
    return this.callMethod(FacebookMethod.EVENTS_GET_MEMBERS,
                           new Pair<String, CharSequence>("eid", eventId.toString()));
  }

  /**
   * Retrieves the friends of the currently logged in user, who are also users
   * of the calling application.
   * @return array of friends
   */
  public T friends_getAppUsers()
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.FRIENDS_GET_APP_USERS);
  }

  /**
   * Retrieves the results of a Facebook Query Language query
   * @param query : the FQL query statement
   * @return varies depending on the FQL query
   */
  public T fql_query(CharSequence query)
    throws FacebookException, IOException {
    assert (null != query);
    return this.callMethod(FacebookMethod.FQL_QUERY,
                           new Pair<String, CharSequence>("query", query));
  }

  private String generateSignature(List<String> params, boolean requiresSession) {
    String secret = (isDesktop() && requiresSession) ? this._sessionSecret : this._secret;
    return FacebookSignatureUtil.generateSignature(params, secret);
  }

  public static void setDebugAll(boolean isDebug) {
    FacebookRestClient.DEBUG = isDebug;
  }

  private static CharSequence delimit(Collection iterable) {
    // could add a thread-safe version that uses StringBuffer as well
    if (iterable == null || iterable.isEmpty())
      return null;

    StringBuilder buffer = new StringBuilder();
    boolean notFirst = false;
    for (Object item : iterable) {
      if (notFirst)
        buffer.append(",");
      else
        notFirst = true;
      buffer.append(item.toString());
    }
    return buffer;
  }

  /**
   * Call the specified method, with the given parameters, and return a DOM tree with the results.
   *
   * @param method the fieldName of the method
   * @param paramPairs a list of arguments to the method
   * @throws Exception with a description of any errors given to us by the server.
   */
  protected T callMethod(IFacebookMethod method, Pair<String, CharSequence>... paramPairs)
    throws FacebookException, IOException {
    return callMethod(method, Arrays.asList(paramPairs));
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param albumId retrieve from photos from this album (optional)
   * @param photoIds retrieve from this list of photos (optional)
   * @return an T of photo objects.
   * @see #photos_get(Integer, Long, Collection)
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Long albumId, Collection<Long> photoIds)
    throws FacebookException, IOException {
    return photos_get( /*subjId*/null, albumId, photoIds);
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param photoIds retrieve from this list of photos (optional)
   * @return an T of photo objects.
   * @see #photos_get(Integer, Long, Collection)
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Collection<Long> photoIds)
    throws FacebookException, IOException {
    return photos_get( /*subjId*/null, /*albumId*/null, photoIds);
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param subjId retrieve from photos associated with this user (optional).
   * @param albumId retrieve from photos from this album (optional)
   * @return an T of photo objects.
   * @see #photos_get(Integer, Long, Collection)
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Integer subjId, Long albumId)
    throws FacebookException, IOException {
    return photos_get(subjId, albumId, /*photoIds*/null) ;
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param subjId retrieve from photos associated with this user (optional).
   * @param photoIds retrieve from this list of photos (optional)
   * @return an T of photo objects.
   * @see #photos_get(Integer, Long, Collection)
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Integer subjId, Collection<Long> photoIds)
    throws FacebookException, IOException {
    return photos_get(subjId, /*albumId*/null, photoIds);
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param subjId retrieve from photos associated with this user (optional).
   * @return an T of photo objects.
   * @see #photos_get(Integer, Long, Collection)
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Integer subjId)
    throws FacebookException, IOException {
    return photos_get(subjId, /*albumId*/null, /*photoIds*/null);
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param albumId retrieve from photos from this album (optional)
   * @return an T of photo objects.
   * @see #photos_get(Integer, Long, Collection)
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Long albumId)
    throws FacebookException, IOException {
    return photos_get(/*subjId*/null, albumId, /*photoIds*/null);
  }

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param subjId retrieve from photos associated with this user (optional).
   * @param albumId retrieve from photos from this album (optional)
   * @param photoIds retrieve from this list of photos (optional)
   * @return an T of photo objects.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get">
   *      Developers Wiki: Photos.get</a>
   */
  public T photos_get(Integer subjId, Long albumId, Collection<Long> photoIds)
    throws FacebookException, IOException {
    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.PHOTOS_GET.numParams());

    boolean hasUserId = null != subjId && 0 != subjId;
    boolean hasAlbumId = null != albumId && 0 != albumId;
    boolean hasPhotoIds = null != photoIds && !photoIds.isEmpty();
    if (!hasUserId && !hasAlbumId && !hasPhotoIds) {
      throw new IllegalArgumentException("At least one of photoIds, albumId, or subjId must be provided");
    }

    if (hasUserId)
      params.add(new Pair<String, CharSequence>("subj_id", Integer.toString(subjId)));
    if (hasAlbumId)
      params.add(new Pair<String, CharSequence>("aid", Long.toString(albumId)));
    if (hasPhotoIds)
      params.add(new Pair<String, CharSequence>("pids", delimit(photoIds)));

    return this.callMethod(FacebookMethod.PHOTOS_GET, params);
  }

  /**
   * Retrieves the requested info fields for the requested set of users.
   * @param userIds a collection of user IDs for which to fetch info
   * @param fields a set of strings describing the info fields desired, such as "last_name", "sex"
   * @return a T consisting of a list of users, with each user element
   * containing the requested fields.
   */
  public T users_getInfo(Collection<Integer> userIds, Set<CharSequence> fields)
    throws FacebookException, IOException {
    // assertions test for invalid params
    if (null == userIds) {
      throw new IllegalArgumentException("userIds cannot be null");
    }
    if (fields == null || fields.isEmpty()) {
      throw new IllegalArgumentException("fields should not be empty");
    }

    return this.callMethod(FacebookMethod.USERS_GET_INFO,
                           new Pair<String, CharSequence>("uids", delimit(userIds)),
                           new Pair<String, CharSequence>("fields", delimit(fields)));
  }

  /**
   * Retrieves the tags for the given set of photos.
   * @param photoIds The list of photos from which to extract photo tags.
   * @return the created album
   */
  public T photos_getTags(Collection<Long> photoIds)
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.PHOTOS_GET_TAGS,
                           new Pair<String, CharSequence>("pids", delimit(photoIds)));
  }

  /**
   * Retrieves the groups associated with a user
   * @param userId Optional: User associated with groups.
   * A null parameter will default to the session user.
   * @param groupIds Optional: group ids to query.
   * A null parameter will get all groups for the user.
   * @return array of groups
   */
  public T groups_get(Integer userId, Collection<Long> groupIds)
    throws FacebookException, IOException {
    boolean hasGroups = (null != groupIds && !groupIds.isEmpty());
    if (null != userId)
      return hasGroups ?
             this.callMethod(FacebookMethod.GROUPS_GET, new Pair<String, CharSequence>("uid", userId.toString()),
                             new Pair<String, CharSequence>("gids", delimit(groupIds))) :
             this.callMethod(FacebookMethod.GROUPS_GET, new Pair<String, CharSequence>("uid", userId.toString()));
    else
      return hasGroups ?
             this.callMethod(FacebookMethod.GROUPS_GET, new Pair<String, CharSequence>("gids", delimit(groupIds))) :
             this.callMethod(FacebookMethod.GROUPS_GET);
  }

  /**
   * Call the specified method, with the given parameters, and return a DOM tree with the results.
   *
   * @param method the fieldName of the method
   * @param paramPairs a list of arguments to the method
   * @throws Exception with a description of any errors given to us by the server.
   */
  protected T callMethod(IFacebookMethod method, Collection<Pair<String, CharSequence>> paramPairs)
    throws FacebookException, IOException {
    HashMap<String, CharSequence> params =
      new HashMap<String, CharSequence>(2 * method.numTotalParams());

    params.put("method", method.methodName());
    params.put("api_key", _apiKey);
    params.put("v", TARGET_API_VERSION);

    String format = getResponseFormat();
    if (null != format) {
      params.put("format", format);
    }

    if (method.requiresSession()) {
      params.put("call_id", Long.toString(System.currentTimeMillis()));
      params.put("session_key", _sessionKey);
    }
    CharSequence oldVal;
    for (Pair<String, CharSequence> p : paramPairs) {
      oldVal = params.put(p.first, p.second);
      if (oldVal != null)
        System.err.printf("For parameter %s, overwrote old value %s with new value %s.", p.first,
                          oldVal, p.second);
    }

    assert (!params.containsKey("sig"));
    String signature =
      generateSignature(FacebookSignatureUtil.convert(params.entrySet()), method.requiresSession());
    params.put("sig", signature);

    boolean doHttps = this.isDesktop() && FacebookMethod.AUTH_GET_SESSION.equals(method);
    InputStream data = method.takesFile() 
      ? postFileRequest(method.methodName(), params) 
      : postRequest(method.methodName(), params, doHttps, /*doEncode*/true);
    return parseCallResult(data, method);
  }

  /**
   * Parses the result of an API call into a T.
   * @param data an InputStream with the results of a request to the Facebook servers
   * @param method the method called
   * @throws FacebookException if <code>data</code> represents an error
   * @throws IOException if <code>data</code> is not readable
   * @return a T
   */
  protected abstract T parseCallResult(InputStream data, IFacebookMethod method)
    throws FacebookException, IOException;

  /**
   * Recaches the referenced url.
   * @param url the URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshRefUrl(URL url)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.FBML_REFRESH_REF_URL,
                                          new Pair<String, CharSequence>("url", url.toString())));
  }

  /**
   * Retrieves the outstanding notifications for the session user.
   * @return a T containing
   * notification count pairs for 'messages', 'pokes' and 'shares',
   * a uid list of 'friend_requests', a gid list of 'group_invites',
   * and an eid list of 'event_invites'
   */
  public T notifications_get()
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.NOTIFICATIONS_GET);
  }

  /**
   * Retrieves the requested info fields for the requested set of users.
   * @param userIds a collection of user IDs for which to fetch info
   * @param fields a set of ProfileFields
   * @return a T consisting of a list of users, with each user element
   * containing the requested fields.
   */
  public T users_getInfo(Collection<Integer> userIds, EnumSet<ProfileField> fields)
    throws FacebookException, IOException {
    // assertions test for invalid params
    assert (userIds != null);
    assert (fields != null);
    assert (!fields.isEmpty());

    return this.callMethod(FacebookMethod.USERS_GET_INFO,
                           new Pair<String, CharSequence>("uids", delimit(userIds)),
                           new Pair<String, CharSequence>("fields", delimit(fields)));
  }

  /**
   * Retrieves the user ID of the user logged in to this API session
   * @return the Facebook user ID of the logged-in user
   */
  public int users_getLoggedInUser()
    throws FacebookException, IOException {
    T result = this.callMethod(FacebookMethod.USERS_GET_LOGGED_IN_USER);
    return extractInt(result);
  }

  /**
   * Call this function to get the user ID.
   *
   * @return The ID of the current session's user, or -1 if none.
   */
  public int auth_getUserId(String authToken)
    throws FacebookException, IOException {
    /*
     * Get the session information if we don't have it; this will populate
	   * the user ID as well.
	   */
    if (null == this._sessionKey)
      auth_getSession(authToken);
    return this._userId;
  }

  public boolean isDesktop() {
    return this._isDesktop;
  }

  private boolean photos_addTag(Long photoId, Double xPct, Double yPct, Integer taggedUserId,
                                CharSequence tagText)
    throws FacebookException, IOException {
    assert (null != photoId && !photoId.equals(0));
    assert (null != taggedUserId || null != tagText);
    assert (null != xPct && xPct >= 0 && xPct <= 100);
    assert (null != yPct && yPct >= 0 && yPct <= 100);
    T d =
      this.callMethod(FacebookMethod.PHOTOS_ADD_TAG, new Pair<String, CharSequence>("pid", photoId.toString()),
                      new Pair<String, CharSequence>("tag_uid", taggedUserId.toString()),
                      new Pair<String, CharSequence>("x", xPct.toString()),
                      new Pair<String, CharSequence>("y", yPct.toString()));
    return extractBoolean(d);
  }

  /**
   * Retrieves an indicator of whether the logged-in user has installed the
   * application associated with the _apiKey.
   * @return boolean indicating whether the user has installed the app
   */
  public boolean users_isAppAdded()
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.USERS_IS_APP_ADDED));
  }

  /**
   * Retrieves whether the logged-in user has granted the specified permission
   * to this application.
   * @param permission an extended permission (e.g. FacebookExtendedPerm.MARKETPLACE,
   *        "photo_upload")
   * @return boolean indicating whether the user has the permission
   * @see FacebookExtendedPerm
   * @see <a href="http://wiki.developers.facebook.com/index.php/Users.hasAppPermission">
   *      Developers Wiki: Users.hasAppPermission</a>
   */
  public boolean users_hasAppPermission(CharSequence permission)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.USERS_HAS_APP_PERMISSION,
                                          new Pair<String, CharSequence>("ext_perm", permission)));
  }

  /**
   * Sets the logged-in user's Facebook status.
   * Requires the status_update extended permission.
   * @return whether the status was successfully set
   * @see #users_hasAppPermission
   * @see FacebookExtendedPerm#STATUS_UPDATE
   * @see <a href="http://wiki.developers.facebook.com/index.php/Users.setStatus">
   *      Developers Wiki: Users.setStatus</a>
   */
  public boolean users_setStatus(String status)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.USERS_SET_STATUS,
                                          new Pair<String, CharSequence>("status", status)));
  }

  /**
   * Clears the logged-in user's Facebook status.
   * Requires the status_update extended permission.
   * @return whether the status was successfully cleared
   * @see #users_hasAppPermission
   * @see FacebookExtendedPerm#STATUS_UPDATE
   * @see <a href="http://wiki.developers.facebook.com/index.php/Users.setStatus">
   *      Developers Wiki: Users.setStatus</a>
   */
  public boolean users_clearStatus()
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.USERS_SET_STATUS,
                                          new Pair<String, CharSequence>("clear", "1")));
  }

  /**
   * Adds a tag to a photo.
   * @param photoId The photo id of the photo to be tagged.
   * @param xPct The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
   * @param yPct The list of photos from which to extract photo tags.
   * @param tagText The text of the tag.
   * @return whether the tag was successfully added.
   */
  public boolean photos_addTag(Long photoId, CharSequence tagText, Double xPct, Double yPct)
    throws FacebookException, IOException {
    return photos_addTag(photoId, xPct, yPct, null, tagText);
  }

  /**
   * Helper function for posting a request that includes raw file data, eg {@link #photos_upload(File)}.
   * @param methodName the name of the method
   * @param params request parameters (not including the file)
   * @return an InputStream with the request response
   * @see #photos_upload(File)
   */
  protected InputStream postFileRequest(String methodName, Map<String, CharSequence> params)
    throws IOException {
    assert (null != _uploadFile);
    try {
      BufferedInputStream bufin = new BufferedInputStream(new FileInputStream(_uploadFile));

      String boundary = Long.toString(System.currentTimeMillis(), 16);
      URLConnection con = SERVER_URL.openConnection();
      con.setDoInput(true);
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);
      con.setRequestProperty("MIME-version", "1.0");

      DataOutputStream out = new DataOutputStream(con.getOutputStream());

      for (Map.Entry<String, CharSequence> entry : params.entrySet()) {
        out.writeBytes(PREF + boundary + CRLF);
        out.writeBytes("Content-disposition: form-data; name=\"" + entry.getKey() + "\"");
        out.writeBytes(CRLF + CRLF);
        byte[] bytes = entry.getValue().toString().getBytes("UTF-8");
        out.write(bytes);
        out.writeBytes(CRLF);
      }

      out.writeBytes(PREF + boundary + CRLF);
      out.writeBytes("Content-disposition: form-data; filename=\"" + _uploadFile.getName() + "\"" +
                     CRLF);
      out.writeBytes("Content-Type: image/jpeg" + CRLF);
      // out.writeBytes("Content-Transfer-Encoding: binary" + CRLF); // not necessary

      // Write the file
      out.writeBytes(CRLF);
      byte b[] = new byte[UPLOAD_BUFFER_SIZE];
      int byteCounter = 0;
      int i;
      while (-1 != (i = bufin.read(b))) {
        byteCounter += i;
        out.write(b, 0, i);
      }
      out.writeBytes(CRLF + PREF + boundary + PREF + CRLF);

      out.flush();
      out.close();

      InputStream is = con.getInputStream();
      return is;
    } catch (Exception e) {
      logException(e);
      return null;
    }
  }

  /**
   * Logs an exception with default message
   * @param e the exception
   */
  protected final void logException(Exception e) {
    logException("exception", e);
  }

  /**
   * Logs an exception with an introductory message in addition to the
   * exception's getMessage().
   * @param msg message
   * @param e   exception
   * @see Exception#getMessage
   */
  protected void logException(CharSequence msg, Exception e) {
    System.err.println(msg + ":" + e.getMessage());
    e.printStackTrace();
  }

  /**
   * Logs a message. Override this for more detailed logging.
   * @param message
   */
  protected void log(CharSequence message) {
    System.out.println(message);
  }

  /**
   * @return whether debugging is activated
   */
  public boolean isDebug() {
    return (null == _debug) ? DEBUG : _debug.booleanValue();
  }

  /**
   * Send a notification message to the specified users on behalf of the logged-in user.
   *
   * @param recipientIds the user ids to which the message is to be sent. if empty,
   *        notification will be sent to logged-in user.
   * @param notification the FBML to be displayed on the notifications page; only a stripped-down 
   *        set of FBML tags that result in text and links is allowed
   * @return a URL, possibly null, to which the user should be redirected to finalize
   * the sending of the email
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail">
   *      Developers Wiki: notifications.send</a>
   */
  public void notifications_send(Collection<Integer> recipientIds, CharSequence notification)
    throws FacebookException, IOException {
    assert (null != notification);
    ArrayList<Pair<String, CharSequence>> args = new ArrayList<Pair<String, CharSequence>>(3);
    if (null != recipientIds && !recipientIds.isEmpty()) {
      args.add(new Pair<String, CharSequence>("to_ids", delimit(recipientIds)));
    }
    args.add(new Pair<String, CharSequence>("notification", notification));
    this.callMethod(FacebookMethod.NOTIFICATIONS_SEND, args);
  }

  /**
   * Send a notification message to the logged-in user.
   *
   * @param notification the FBML to be displayed on the notifications page; only a stripped-down 
   *    set of FBML tags that result in text and links is allowed
   * @return a URL, possibly null, to which the user should be redirected to finalize
   * the sending of the email
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail">
   *      Developers Wiki: notifications.send</a>
   */
  public void notifications_send(CharSequence notification)
    throws FacebookException, IOException {
    notifications_send(/*recipients*/null, notification);
  }

  /**
   * Sends a notification email to the specified users, who must have added your application.
   * You can send five (5) emails to a user per day. Requires a session key for desktop applications, which may only 
   * send email to the person whose session it is. This method does not require a session for Web applications. 
   * Either <code>fbml</code> or <code>text</code> must be specified.
   * 
   * @param recipientIds up to 100 user ids to which the message is to be sent
   * @param subject the subject of the notification email (optional)
   * @param fbml markup to be sent to the specified users via email; only a stripped-down set of FBML tags
   *    that result in text, links and linebreaks is allowed
   * @param text the plain text to send to the specified users via email
   * @return a comma-separated list of the IDs of the users to whom the email was successfully sent
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.send">
   *      Developers Wiki: notifications.sendEmail</a>
   */
  public String notifications_sendEmail(Collection<Integer> recipientIds, CharSequence subject, CharSequence fbml, CharSequence text)
    throws FacebookException, IOException {
    if (null == recipientIds || recipientIds.isEmpty()) {
      throw new IllegalArgumentException("List of email recipients cannot be empty");
    }
    boolean hasText = null != text && (0 != text.length());
    boolean hasFbml = null != fbml && (0 != fbml.length());
    if (!hasText && !hasFbml) {
      throw new IllegalArgumentException("Text and/or fbml must not be empty");
    }
    ArrayList<Pair<String, CharSequence>> args = new ArrayList<Pair<String, CharSequence>>(4);
    args.add(new Pair<String, CharSequence>("recipients", delimit(recipientIds)));
    args.add(new Pair<String, CharSequence>("subject", subject));
    if (hasText) {
      args.add(new Pair<String, CharSequence>("text", text));
    }
    if (hasFbml) {
      args.add(new Pair<String, CharSequence>("fbml", fbml));
    }
    // this method requires a session only if we're dealing with a desktop app
    T result = this.callMethod(this.isDesktop() ? FacebookMethod.NOTIFICATIONS_SEND_EMAIL
                 : FacebookMethod.NOTIFICATIONS_SEND_EMAIL, args);
    return extractString(result);
  }

  /**
   * Sends a notification email to the specified users, who must have added your application.
   * You can send five (5) emails to a user per day. Requires a session key for desktop applications, which may only
   * send email to the person whose session it is. This method does not require a session for Web applications.
   *
   * @param recipientIds up to 100 user ids to which the message is to be sent
   * @param subject the subject of the notification email (optional)
   * @param fbml markup to be sent to the specified users via email; only a stripped-down set of FBML
   *    that allows only tags that result in text, links and linebreaks is allowed
   * @return a comma-separated list of the IDs of the users to whom the email was successfully sent
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.send">
   *      Developers Wiki: notifications.sendEmail</a>
   */
  public String notifications_sendEmail(Collection<Integer> recipientIds, CharSequence subject, CharSequence fbml)
    throws FacebookException, IOException {
    return notifications_sendEmail(recipientIds, subject, fbml, /*text*/null);
  }

  /**
   * Sends a notification email to the specified users, who must have added your application.
   * You can send five (5) emails to a user per day. Requires a session key for desktop applications, which may only
   * send email to the person whose session it is. This method does not require a session for Web applications.
   *
   * @param recipientIds up to 100 user ids to which the message is to be sent
   * @param subject the subject of the notification email (optional)
   * @param text the plain text to send to the specified users via email
   * @return a comma-separated list of the IDs of the users to whom the email was successfully sent
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail">
   *      Developers Wiki: notifications.sendEmail</a>
   */
  public String notifications_sendEmailPlain(Collection<Integer> recipientIds, CharSequence subject, CharSequence text)
    throws FacebookException, IOException {
    return notifications_sendEmail(recipientIds, subject, /*fbml*/null, text);
  }

  /**
   * Extracts a URL from a result that consists of a URL only.
   * @param result
   * @return the URL
   */
  protected abstract URL extractURL(T result)
    throws IOException;

  /**
   * Recaches the image with the specified imageUrl.
   * @param imageUrl String representing the image URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshImgSrc(String imageUrl)
    throws FacebookException, IOException {
    return fbml_refreshImgSrc(new URL(imageUrl));
  }

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   *      Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo)
    throws FacebookException, IOException {
    return photos_upload(photo, /*caption*/ null, /*albumId*/ null) ;
  }

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @param caption a description of the image contents
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   *      Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo, String caption)
    throws FacebookException, IOException {
    return photos_upload(photo, caption, /*albumId*/null) ;
  }

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @param albumId the album into which the photo should be uploaded
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   *      Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo, Long albumId)
    throws FacebookException, IOException {
    return photos_upload(photo, /*caption*/null, albumId);
  }

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @param caption a description of the image contents
   * @param albumId the album into which the photo should be uploaded
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   * Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo, String caption, Long albumId)
    throws FacebookException, IOException {
    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.PHOTOS_UPLOAD.numParams());
    assert (photo.exists() && photo.canRead());
    this._uploadFile = photo;
    if (null != albumId)
      params.add(new Pair<String, CharSequence>("aid", Long.toString(albumId)));
    if (null != caption)
      params.add(new Pair<String, CharSequence>("caption", caption));
    return callMethod(FacebookMethod.PHOTOS_UPLOAD, params);
  }

  /**
   * Creates an album.
   * @param albumName The list of photos from which to extract photo tags.
   * @return the created album
   */
  public T photos_createAlbum(String albumName)
    throws FacebookException, IOException {
    return this.photos_createAlbum(albumName, null, /*description*/null) /*location*/;
  }

  /**
   * Adds a tag to a photo.
   * @param photoId The photo id of the photo to be tagged.
   * @param xPct The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
   * @param yPct The vertical position of the tag, as a percentage from 0 to 100, from the top of the photo.
   * @param taggedUserId The list of photos from which to extract photo tags.
   * @return whether the tag was successfully added.
   */
  public boolean photos_addTag(Long photoId, Integer taggedUserId, Double xPct, Double yPct)
    throws FacebookException, IOException {
    return photos_addTag(photoId, xPct, yPct, taggedUserId, null);
  }

  /**
   * Adds several tags to a photo.
   * @param photoId The photo id of the photo to be tagged.
   * @param tags A list of PhotoTags.
   * @return a list of booleans indicating whether the tag was successfully added.
   */
  public T photos_addTags(Long photoId, Collection<PhotoTag> tags)
    throws FacebookException, IOException {
    assert (photoId > 0);
    assert (null != tags && !tags.isEmpty());

    JSONArray jsonTags = new JSONArray();
    for (PhotoTag tag : tags) {
      jsonTags.add(tag.jsonify());
    }

    return this.callMethod(FacebookMethod.PHOTOS_ADD_TAG,
                           new Pair<String, CharSequence>("pid", photoId.toString()),
                           new Pair<String, CharSequence>("tags", jsonTags.toString()));
  }

  public void setIsDesktop(boolean isDesktop) {
    this._isDesktop = isDesktop;
  }

  /**
   * Returns all visible events according to the filters specified. This may be used to find all events of a user, or to query specific eids.
   * @param eventIds filter by these event ID's (optional)
   * @param userId filter by this user only (optional)
   * @param startTime UTC lower bound (optional)
   * @param endTime UTC upper bound (optional)
   * @return T of events
   */
  public T events_get(Integer userId, Collection<Long> eventIds, Long startTime, Long endTime)
    throws FacebookException, IOException {
    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.EVENTS_GET.numParams());

    boolean hasUserId = null != userId && 0 != userId;
    boolean hasEventIds = null != eventIds && !eventIds.isEmpty();
    boolean hasStart = null != startTime && 0 != startTime;
    boolean hasEnd = null != endTime && 0 != endTime;

    if (hasUserId)
      params.add(new Pair<String, CharSequence>("uid", Integer.toString(userId)));
    if (hasEventIds)
      params.add(new Pair<String, CharSequence>("eids", delimit(eventIds)));
    if (hasStart)
      params.add(new Pair<String, CharSequence>("start_time", startTime.toString()));
    if (hasEnd)
      params.add(new Pair<String, CharSequence>("end_time", endTime.toString()));
    return this.callMethod(FacebookMethod.EVENTS_GET, params);
  }

  /**
   * Sets the FBML for a user's profile, including the content for both the profile box
   * and the profile actions.
   * @param userId the user whose profile FBML to set
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @deprecated Use {@link FacebookRestClient#profile_setFBML(CharSequence,CharSequence,Long)} instead.
   * @see #profile_setFBML(CharSequence,CharSequence,Long)
   */
  public boolean profile_setFBML(CharSequence fbmlMarkup, Integer userId)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.PROFILE_SET_FBML,
                                          new Pair<String, CharSequence>("uid", Integer.toString(userId)),
                                          new Pair<String, CharSequence>("markup", fbmlMarkup)));
  }

  /**
   * Sets the FBML for a profile box on the logged-in user's profile.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFbml</a>
   */
  public boolean profile_setProfileFBML(CharSequence fbmlMarkup)
    throws FacebookException, IOException {
    return profile_setFBML(fbmlMarkup, /* profileActionFbmlMarkup */null, /* mobileFbmlMarkup */null,
        /* profileId */null);
  }

  /**
   * Sets the FBML for profile actions for  the logged-in user.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setProfileActionFBML(CharSequence fbmlMarkup)
    throws FacebookException, IOException {
    return profile_setFBML( /* profileFbmlMarkup */null, fbmlMarkup, /* mobileFbmlMarkup */null,
        /* profileId */null);
  }

  /**
   * Sets the FBML for the logged-in user's profile on mobile devices.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setMobileFBML(CharSequence fbmlMarkup)
    throws FacebookException, IOException {
    return profile_setFBML( /* profileFbmlMarkup */null, /* profileActionFbmlMarkup */null, fbmlMarkup,
        /* profileId */null);
  }

  /**
   * Sets the FBML for a profile box on the user or page profile with ID <code>profileId</code>.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFbml</a>
   */
  public boolean profile_setProfileFBML(CharSequence fbmlMarkup, Long profileId)
    throws FacebookException, IOException {
    return profile_setFBML(fbmlMarkup, /* profileActionFbmlMarkup */null, /* mobileFbmlMarkup */null, profileId);
  }

  /**
   * Sets the FBML for profile actions for the user or page profile with ID <code>profileId</code>.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setProfileActionFBML(CharSequence fbmlMarkup, Long profileId)
    throws FacebookException, IOException {
    return profile_setFBML( /* profileFbmlMarkup */null, fbmlMarkup, /* mobileFbmlMarkup */null, profileId);
  }

  /**
   * Sets the FBML for the user or page profile with ID <code>profileId</code> on mobile devices.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setMobileFBML(CharSequence fbmlMarkup, Long profileId)
    throws FacebookException, IOException {
    return profile_setFBML( /* profileFbmlMarkup */null, /* profileActionFbmlMarkup */null, fbmlMarkup, profileId);
  }

  /**
   * Sets the FBML for the profile box and profile actions for the logged-in user.
   * Refer to the FBML documentation for a description of the markup and its role in various contexts.
   * @param profileFbmlMarkup the FBML for the profile box
   * @param profileActionFbmlMarkup the FBML for the profile actions
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setFBML(CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup)
    throws FacebookException, IOException {
    return profile_setFBML(profileFbmlMarkup, profileActionFbmlMarkup, /* mobileFbmlMarkup */null, /* profileId */null);
  }

  /**
   * Sets the FBML for the profile box and profile actions for the user or page profile with ID <code>profileId</code>.
   * Refer to the FBML documentation for a description of the markup and its role in various contexts.
   * @param profileFbmlMarkup the FBML for the profile box
   * @param profileActionFbmlMarkup the FBML for the profile actions
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setFBML(CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, Long profileId)
    throws FacebookException, IOException {
    return profile_setFBML(profileFbmlMarkup, profileActionFbmlMarkup, /* mobileFbmlMarkup */null, profileId);
  }

  /**
   * Sets the FBML for the profile box, profile actions, and mobile devices for the logged-in user.
   * Refer to the FBML documentation for a description of the markup and its role in various contexts.
   * @param profileFbmlMarkup the FBML for the profile box
   * @param profileActionFbmlMarkup the FBML for the profile actions
   * @param mobileFbmlMarkup the FBML for mobile devices
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setFBML(CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, CharSequence mobileFbmlMarkup)
    throws FacebookException, IOException {
    return profile_setFBML(profileFbmlMarkup, profileActionFbmlMarkup, mobileFbmlMarkup, /* profileId */null);
  }

  /**
   * Sets the FBML for the profile box, profile actions, and mobile devices for the user or page profile with ID <code>profileId</code>.
   * Refer to the FBML documentation for a description of the markup and its role in various contexts.
   * @param profileFbmlMarkup the FBML for the profile box
   * @param profileActionFbmlMarkup the FBML for the profile actions
   * @param mobileFbmlMarkup the FBML for mobile devices
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setFBML(CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup,
                                 CharSequence mobileFbmlMarkup, Long profileId)
    throws FacebookException, IOException {
    if (null == profileFbmlMarkup && null == profileActionFbmlMarkup && null == mobileFbmlMarkup) {
      throw new IllegalArgumentException("At least one of the FBML parameters must be provided");
    }
    if (this.isDesktop() && (null != profileId && this._userId != profileId)) {
      throw new IllegalArgumentException("Can't set FBML for another user from desktop app");
    }
    
    FacebookMethod method = FacebookMethod.PROFILE_SET_FBML;
    ArrayList<Pair<String, CharSequence>> params = new ArrayList<Pair<String, CharSequence>>(method.numParams());
    if (null != profileId && !this.isDesktop())
      params.add(new Pair<String, CharSequence>("uid", profileId.toString()));
    if (null != profileFbmlMarkup)
      params.add(new Pair<String, CharSequence>("profile", profileFbmlMarkup));
    if (null != profileActionFbmlMarkup)
      params.add(new Pair<String, CharSequence>("profile_action", profileActionFbmlMarkup));
    if (null != mobileFbmlMarkup)
      params.add(new Pair<String, CharSequence>("mobile_fbml", mobileFbmlMarkup));
    return extractBoolean(this.callMethod(method, params));
  }

  /**
   * Associates a "<code>handle</code>" with FBML markup so that the handle can be used within the
   * <a href="http://wiki.developers.facebook.com/index.php/Fb:ref">fb:ref</a> FBML tag.
   * A handle is unique within an application and allows an application to publish identical FBML
   * to many user profiles and do subsequent updates without having to republish FBML for each user.
   *
   * @param handle a string, unique within the application, that
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Fbml.setRefHandle">
   *      Developers Wiki: Fbml.setRefHandle</a>
   */
  public boolean fbml_setRefHandle(CharSequence handle, CharSequence fbmlMarkup)
    throws FacebookException, IOException {

    return extractBoolean(this.callMethod(FacebookMethod.FBML_SET_REF_HANDLE,
                                          new Pair<String, CharSequence>("handle", handle),
                                          new Pair<String, CharSequence>("fbml", fbmlMarkup)));

  }

  /**
   * Determines whether this application can send SMS to the user identified by <code>userId</code>
   * @param userId a user ID
   * @return true if sms can be sent to the user
   * @see FacebookExtendedPerm#SMS
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages">
   *      Developers Wiki: Mobile: Application Generated Messages</a>
   */
  public boolean sms_canSend(Integer userId)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.SMS_CAN_SEND,
                                          new Pair<String, CharSequence>("uid",
                                                                         userId.toString())));
  }

  /**
   * Sends a message via SMS to the user identified by <code>userId</code> in response 
   * to a user query associated with <code>mobileSessionId</code>.
   *
   * @param userId a user ID
   * @param response the message to be sent via SMS
   * @param mobileSessionId the mobile session
   * @throws FacebookException in case of error
   * @throws IOException
   * @see FacebookExtendedPerm#SMS
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages">
   * Developers Wiki: Mobile: Application Generated Messages</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Workflow">
   * Developers Wiki: Mobile: Workflow</a>
   */
  public void sms_sendResponse(Integer userId, CharSequence response, Integer mobileSessionId)
    throws FacebookException, IOException {
    this.callMethod(FacebookMethod.SMS_SEND_MESSAGE,
                    new Pair<String, CharSequence>("uid", userId.toString()),
                    new Pair<String, CharSequence>("message", response),
                    new Pair<String, CharSequence>("session_id", mobileSessionId.toString()));
  }

  /**
   * Sends a message via SMS to the user identified by <code>userId</code>.
   * The SMS extended permission is required for success.
   *
   * @param userId a user ID
   * @param message the message to be sent via SMS
   * @throws FacebookException in case of error
   * @throws IOException
   * @see FacebookExtendedPerm#SMS
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages">
   * Developers Wiki: Mobile: Application Generated Messages</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Workflow">
   * Developers Wiki: Mobile: Workflow</a>
   */
  public void sms_sendMessage(Integer userId, CharSequence message)
    throws FacebookException, IOException {
    this.callMethod(FacebookMethod.SMS_SEND_MESSAGE,
                    new Pair<String, CharSequence>("uid", userId.toString()),
                    new Pair<String, CharSequence>("message", message),
                    new Pair<String, CharSequence>("req_session", "0"));
  }

  /**
   * Sends a message via SMS to the user identified by <code>userId</code>, with
   * the expectation that the user will reply. The SMS extended permission is required for success.
   * The returned mobile session ID can be stored and used in {@link #sms_sendResponse} when
   * the user replies.
   *
   * @param userId a user ID
   * @param message the message to be sent via SMS
   * @return a mobile session ID (can be used in {@link #sms_sendResponse})
   * @throws FacebookException in case of error, e.g. SMS is not enabled
   * @throws IOException
   * @see FacebookExtendedPerm#SMS
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages">
   *      Developers Wiki: Mobile: Application Generated Messages</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Workflow">
   *      Developers Wiki: Mobile: Workflow</a>
   */
  public int sms_sendMessageWithSession(Integer userId, CharSequence message)
    throws FacebookException, IOException {
    return extractInt(this.callMethod(FacebookMethod.SMS_SEND_MESSAGE,
                               new Pair<String, CharSequence>("uid", userId.toString()),
                               new Pair<String, CharSequence>("message", message),
                               new Pair<String, CharSequence>("req_session", "1")));
  }


  /**
   * Delimits a collection entries into a single CharSequence, using <code>delimiter</code>
   * to delimit each entry, and <code>equals</code> to delimit the key from the value inside
   * each entry.
   * @param entries
   * @param delimiter used to delimit one entry from another
   * @param equals used to delimit key from value
   * @param doEncode whether to encode the value of each entry
   * @return a CharSequence that contains all the entries, appropriately delimited
   */
  protected static CharSequence delimit(Collection<Map.Entry<String, CharSequence>> entries,
                                        CharSequence delimiter, CharSequence equals,
                                        boolean doEncode) {
    if (entries == null || entries.isEmpty())
      return null;

    StringBuilder buffer = new StringBuilder();
    boolean notFirst = false;
    for (Map.Entry<String, CharSequence> entry : entries) {
      if (notFirst)
        buffer.append(delimiter);
      else
        notFirst = true;
      CharSequence value = entry.getValue();
      buffer.append(entry.getKey()).append(equals).append(doEncode ? encode(value) : value);
    }
    return buffer;
  }

  /**
   * Creates an album.
   * @param name The album name.
   * @param location The album location (optional).
   * @param description The album description (optional).
   * @return an array of photo objects.
   */
  public T photos_createAlbum(String name, String description, String location)
    throws FacebookException, IOException {
    assert (null != name && !"".equals(name));
    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.PHOTOS_CREATE_ALBUM.numParams());
    params.add(new Pair<String, CharSequence>("name", name));
    if (null != description)
      params.add(new Pair<String, CharSequence>("description", description));
    if (null != location)
      params.add(new Pair<String, CharSequence>("location", location));
    return this.callMethod(FacebookMethod.PHOTOS_CREATE_ALBUM, params);
  }

  public void setDebug(boolean isDebug) {
    _debug = isDebug;
  }

  /**
   * Extracts a Boolean from a result that consists of a Boolean only.
   * @param result
   * @return the Boolean
   */
  protected boolean extractBoolean(T result) {
    return 1 == extractInt(result);
  }

  /**
   * Extracts an Integer from a result that consists of an Integer only.
   * @param result
   * @return the Integer
   */
  protected abstract int extractInt(T result);

  /**
   * Extracts an Long from a result that consists of a Long only.
   * @param result
   * @return the Long
   */
  protected abstract Long extractLong(T result);

  /**
   * Retrieves album metadata for a list of album IDs.
   * @param albumIds the ids of albums whose metadata is to be retrieved
   * @return album objects
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums">
   *      Developers Wiki: Photos.getAlbums</a>
   */
  public T photos_getAlbums(Collection<Long> albumIds)
    throws FacebookException, IOException {
    return photos_getAlbums(null, /*userId*/albumIds);
  }

  /**
   * Retrieves album metadata for albums owned by a user.
   * @param userId   (optional) the id of the albums' owner (optional)
   * @return album objects
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums">
   *      Developers Wiki: Photos.getAlbums</a>
   */
  public T photos_getAlbums(Integer userId)
    throws FacebookException, IOException {
    return photos_getAlbums(userId, null) /*albumIds*/;
  }

  /**
   * Retrieves album metadata. Pass a user id and/or a list of album ids to specify the albums
   * to be retrieved (at least one must be provided)
   *
   * @param userId   (optional) the id of the albums' owner (optional)
   * @param albumIds (optional) the ids of albums whose metadata is to be retrieved
   * @return album objects
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums">
   *      Developers Wiki: Photos.getAlbums</a>
   */
  public T photos_getAlbums(Integer userId, Collection<Long> albumIds)
    throws FacebookException, IOException {
    boolean hasUserId = null != userId && userId != 0;
    boolean hasAlbumIds = null != albumIds && !albumIds.isEmpty();
    assert (hasUserId || hasAlbumIds); // one of the two must be provided

    if (hasUserId)
      return (hasAlbumIds) ?
             this.callMethod(FacebookMethod.PHOTOS_GET_ALBUMS, new Pair<String, CharSequence>("uid",
                                                                                              Integer.toString(userId)),
                             new Pair<String, CharSequence>("aids", delimit(albumIds))) :
             this.callMethod(FacebookMethod.PHOTOS_GET_ALBUMS,
                             new Pair<String, CharSequence>("uid", Integer.toString(userId)));
    else
      return this.callMethod(FacebookMethod.PHOTOS_GET_ALBUMS,
                             new Pair<String, CharSequence>("aids", delimit(albumIds)));
  }

  /**
   * Recaches the image with the specified imageUrl.
   * @param imageUrl the image URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshImgSrc(URL imageUrl)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.FBML_REFRESH_IMG_SRC,
                                          new Pair<String, CharSequence>("url",
                                                                         imageUrl.toString())));
  }

  /**
   * Retrieves the friends of the currently logged in user.
   * @return T of friends
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.get">
   *      Developers Wiki: Friends.get</a>
   */
  public T friends_get()
    throws FacebookException, IOException {
    return this.friends_get( /*friendListId */null);
  }

  /**
   * Retrieves the friends of the currently logged in user that
   * are members of the friends list with ID <code>friendListId</code>.
   * @param friendListId the friend list for which friends should be fetched.
   *   if <code>null</code>, all friends will be retrieved.
   * @return T of friends
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.get">
   *      Developers Wiki: Friends.get</a>
   */
  public T friends_get(Long friendListId)
    throws FacebookException, IOException {                                                                                                                
    FacebookMethod method = FacebookMethod.FRIENDS_GET;
    Collection<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(method.numParams());
    if (null != friendListId) {
      if (0L >= friendListId) {
        throw new IllegalArgumentException("given invalid friendListId " + friendListId.toString());
      }
      params.add(new Pair<String, CharSequence>("flid", friendListId.toString()));
    }
    
    return this.callMethod(method, params);
  }

  /**
   * Retrieves the friend lists of the currently logged in user.
   * @return T of friend lists
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.getLists">
   *      Developers Wiki: Friends.getLists</a>
   */
  public T friends_getLists()
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.FRIENDS_GET_LISTS);  
  }

  private InputStream postRequest(CharSequence method, Map<String, CharSequence> params,
                                  boolean doHttps, boolean doEncode)
    throws IOException {
    CharSequence buffer = (null == params) ? "" : delimit(params.entrySet(), "&", "=", doEncode);
    URL serverUrl = (doHttps) ? HTTPS_SERVER_URL : _serverUrl;
    if (isDebug()) {
      StringBuilder debugMsg =
        new StringBuilder().append(method).append(" POST: ").append(serverUrl.toString()).append("?");
      debugMsg.append(buffer);
      log(debugMsg);
    }

    HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
    try {
      conn.setRequestMethod("POST");
    } catch (ProtocolException ex) {
      logException(ex);
    }
    conn.setDoOutput(true);
    conn.connect();
    conn.getOutputStream().write(buffer.toString().getBytes());

    return conn.getInputStream();
  }

  /**
   * Call this function and store the result, using it to generate the
   * appropriate login url and then to retrieve the session information.
   * @return an authentication token
   */
  public String auth_createToken()
    throws FacebookException, IOException {
    T d = this.callMethod(FacebookMethod.AUTH_CREATE_TOKEN);
    return extractString(d);
  }

  /**
   * Extracts a String from a T consisting entirely of a String.
   * @param result
   * @return the String
   */
  protected abstract String extractString(T result);

  /**
   * Create a marketplace listing
   * @param showOnProfile whether the listing can be shown on the user's profile
   * @param attrs the properties of the listing
   * @return the id of the created listing
   * @see MarketplaceListing
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing">
   *      Developers Wiki: marketplace.createListing</a>
   */
  public Long marketplace_createListing(Boolean showOnProfile, MarketplaceListing attrs)
    throws FacebookException, IOException {
    T result =
      this.callMethod(FacebookMethod.MARKETPLACE_CREATE_LISTING, 
                      new Pair<String, CharSequence>("show_on_profile", showOnProfile ? "1" : "0"),
                      new Pair<String, CharSequence>("listing_id", "0"),
                      new Pair<String, CharSequence>("listing_attrs", attrs.jsonify().toString()));
    return this.extractLong(result);
  }

  /**
   * Modify a marketplace listing
   * @param listingId identifies the listing to be modified
   * @param showOnProfile whether the listing can be shown on the user's profile
   * @param attrs the properties of the listing
   * @return the id of the edited listing
   * @see MarketplaceListing
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing">
   *      Developers Wiki: marketplace.createListing</a>
   */
  public Long marketplace_editListing(Long listingId, Boolean showOnProfile,
                                      MarketplaceListing attrs)
    throws FacebookException, IOException {
    T result =
      this.callMethod(FacebookMethod.MARKETPLACE_CREATE_LISTING,
                      new Pair<String, CharSequence>("show_on_profile", showOnProfile ? "1" : "0"),
                      new Pair<String, CharSequence>("listing_id", listingId.toString()),
                      new Pair<String, CharSequence>("listing_attrs", attrs.jsonify().toString()));
    return this.extractLong(result);
  }

  /**
   * Remove a marketplace listing
   * @param listingId the listing to be removed
   * @return boolean indicating whether the listing was removed
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing">
   *      Developers Wiki: marketplace.removeListing</a>
   */
  public boolean marketplace_removeListing(Long listingId)
    throws FacebookException, IOException {
    return marketplace_removeListing(listingId, MARKETPLACE_STATUS_DEFAULT);
  }

  /**
   * Remove a marketplace listing
   * @param listingId the listing to be removed
   * @param status MARKETPLACE_STATUS_DEFAULT, MARKETPLACE_STATUS_SUCCESS, or MARKETPLACE_STATUS_NOT_SUCCESS
   * @return boolean indicating whether the listing was removed
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing">
   *      Developers Wiki: marketplace.removeListing</a>
   */
  public boolean marketplace_removeListing(Long listingId, CharSequence status)
    throws FacebookException, IOException {
    assert MARKETPLACE_STATUS_DEFAULT.equals(status) || MARKETPLACE_STATUS_SUCCESS.equals(status) ||
      MARKETPLACE_STATUS_NOT_SUCCESS.equals(status) : "Invalid status: " + status;

    T result =
      this.callMethod(FacebookMethod.MARKETPLACE_REMOVE_LISTING, new Pair<String, CharSequence>("listing_id",
                                                                                                listingId.toString()),
                      new Pair<String, CharSequence>("status", status));
    return this.extractBoolean(result);
  }

  /**
   * Get the categories available in marketplace.
   * @return a T listing the marketplace categories
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getCategories">
   *      Developers Wiki: marketplace.getCategories</a>
   */
  public T marketplace_getCategories()
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.MARKETPLACE_GET_CATEGORIES);
  }

  /**
   * Get the subcategories available for a category.
   * @param category a category, e.g. "HOUSING"
   * @return a T listing the marketplace sub-categories
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getSubCategories">
   *      Developers Wiki: marketplace.getSubCategories</a>
   */
  public T marketplace_getSubCategories(CharSequence category)
    throws FacebookException, IOException {
    return this.callMethod(FacebookMethod.MARKETPLACE_GET_SUBCATEGORIES,
                           new Pair<String, CharSequence>("category", category));
  }

  /**
   * Fetch marketplace listings, filtered by listing IDs and/or the posting users' IDs.
   * @param listingIds listing identifiers (required if uids is null/empty)
   * @param userIds posting user identifiers (required if listingIds is null/empty)
   * @return a T of marketplace listings
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getListings">
   *      Developers Wiki: marketplace.getListings</a>
   */
  public T marketplace_getListings(Collection<Long> listingIds, Collection<Integer> userIds)
    throws FacebookException, IOException {

    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.MARKETPLACE_GET_LISTINGS.numParams());
    if (null != listingIds && !listingIds.isEmpty()) {
      params.add(new Pair<String, CharSequence>("listing_ids", delimit(listingIds)));
    }
    if (null != userIds && !userIds.isEmpty()) {
      params.add(new Pair<String, CharSequence>("uids", delimit(userIds)));
    }

    assert !params.isEmpty() : "Either listingIds or userIds should be provided";
    return this.callMethod(FacebookMethod.MARKETPLACE_GET_LISTINGS, params);
  }

  /**
   * Search for marketplace listings, optionally by category, subcategory, and/or query string.
   * @param category the category of listings desired (optional except if subcategory is provided)
   * @param subCategory the subcategory of listings desired (optional)
   * @param query a query string (optional)
   * @return a T of marketplace listings
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.search">
   *      Developers Wiki: marketplace.search</a>
   */
  public T marketplace_search(CharSequence category, CharSequence subCategory, CharSequence query)
    throws FacebookException, IOException {

    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.MARKETPLACE_SEARCH.numParams());
    if (null != category && !"".equals(category)) {
      params.add(new Pair<String, CharSequence>("category", category));
      if (null != subCategory && !"".equals(subCategory)) {
        params.add(new Pair<String, CharSequence>("subcategory", subCategory));
      }
    }
    if (null != query && !"".equals(query)) {
      params.add(new Pair<String, CharSequence>("query", category));
    }

    return this.callMethod(FacebookMethod.MARKETPLACE_SEARCH, params);
  }
  
  /**
   * Retrieves the requested profile fields for the Facebook Pages with the given 
   * <code>pageIds</code>. Can be called for pages that have added the application 
   * without establishing a session.
   * @param pageIds the page IDs
   * @param fields a set of page profile fields
   * @return a T consisting of a list of pages, with each page element
   *     containing the requested fields.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo">
   *      Developers Wiki: Pages.getInfo</a>
   */
  public T pages_getInfo(Collection<Long> pageIds, EnumSet<PageProfileField> fields)
    throws FacebookException, IOException {
    if (pageIds == null || pageIds.isEmpty()) {
      throw new IllegalArgumentException("pageIds cannot be empty or null");
    }
    if (fields == null || fields.isEmpty()) {
      throw new IllegalArgumentException("fields cannot be empty or null");
    }
    IFacebookMethod method =
      null == this._sessionKey ? FacebookMethod.PAGES_GET_INFO_NO_SESSION : FacebookMethod.PAGES_GET_INFO;
    return this.callMethod(method,
                           new Pair<String, CharSequence>("page_ids", delimit(pageIds)),
                           new Pair<String, CharSequence>("fields", delimit(fields)));
  }
  
  /**
   * Retrieves the requested profile fields for the Facebook Pages with the given 
   * <code>pageIds</code>. Can be called for pages that have added the application 
   * without establishing a session.
   * @param pageIds the page IDs
   * @param fields a set of page profile fields
   * @return a T consisting of a list of pages, with each page element
   *     containing the requested fields.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo">
   *      Developers Wiki: Pages.getInfo</a>
   */
  public T pages_getInfo(Collection<Long> pageIds, Set<CharSequence> fields)
    throws FacebookException, IOException {
    if (pageIds == null || pageIds.isEmpty()) {
      throw new IllegalArgumentException("pageIds cannot be empty or null");
    }
    if (fields == null || fields.isEmpty()) {
      throw new IllegalArgumentException("fields cannot be empty or null");
    }
    IFacebookMethod method =
      null == this._sessionKey ? FacebookMethod.PAGES_GET_INFO_NO_SESSION : FacebookMethod.PAGES_GET_INFO;
    return this.callMethod(method,
                           new Pair<String, CharSequence>("page_ids", delimit(pageIds)),
                           new Pair<String, CharSequence>("fields", delimit(fields)));
  }
  
  /**
   * Retrieves the requested profile fields for the Facebook Pages of the user with the given 
   * <code>userId</code>.
   * @param userId the ID of a user about whose pages to fetch info (defaulted to the logged-in user)
   * @param fields a set of PageProfileFields
   * @return a T consisting of a list of pages, with each page element
   *     containing the requested fields.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo">
   *      Developers Wiki: Pages.getInfo</a>
   */
  public T pages_getInfo(Integer userId, EnumSet<PageProfileField> fields)
    throws FacebookException, IOException {
    if (fields == null || fields.isEmpty()) {
      throw new IllegalArgumentException("fields cannot be empty or null");
    }
    if (userId == null) {
      userId = this._userId;
    }
    return this.callMethod(FacebookMethod.PAGES_GET_INFO,
                           new Pair<String, CharSequence>("uid",    userId.toString()),
                           new Pair<String, CharSequence>("fields", delimit(fields)));
  }

  /**
   * Retrieves the requested profile fields for the Facebook Pages of the user with the given
   * <code>userId</code>.
   * @param userId the ID of a user about whose pages to fetch info (defaulted to the logged-in user)
   * @param fields a set of page profile fields
   * @return a T consisting of a list of pages, with each page element
   *     containing the requested fields.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo">
   *      Developers Wiki: Pages.getInfo</a>
   */
  public T pages_getInfo(Integer userId, Set<CharSequence> fields)
    throws FacebookException, IOException {
    if (fields == null || fields.isEmpty()) {
      throw new IllegalArgumentException("fields cannot be empty or null");
    }
    if (userId == null) {
      userId = this._userId;
    }
    return this.callMethod(FacebookMethod.PAGES_GET_INFO,
                           new Pair<String, CharSequence>("uid",    userId.toString()),
                           new Pair<String, CharSequence>("fields", delimit(fields)));
  }

  /**
   * Checks whether a page has added the application
   * @param pageId the ID of the page
   * @return true if the page has added the application
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAppAdded">
   *      Developers Wiki: Pages.isAppAdded</a>
   */
  public boolean pages_isAppAdded(Long pageId)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.PAGES_IS_APP_ADDED,
                                          new Pair<String,CharSequence>("page_id", pageId.toString())));
  }
  
  /**
   * Checks whether a user is a fan of the page with the given <code>pageId</code>.
   * @param pageId the ID of the page
   * @param userId the ID of the user (defaults to the logged-in user if null)
   * @return true if the user is a fan of the page
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan">
   *      Developers Wiki: Pages.isFan</a>
   */
  public boolean pages_isFan(Long pageId, Integer userId)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.PAGES_IS_FAN,
                                          new Pair<String,CharSequence>("page_id", pageId.toString()),
                                          new Pair<String,CharSequence>("uid", userId.toString())));
  }
  
  /**
   * Checks whether the logged-in user is a fan of the page with the given <code>pageId</code>.
   * @param pageId the ID of the page
   * @return true if the logged-in user is a fan of the page
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan">
   *      Developers Wiki: Pages.isFan</a>
   */
  public boolean pages_isFan(Long pageId)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.PAGES_IS_FAN,
                                          new Pair<String,CharSequence>("page_id", pageId.toString())));
  }

  /**
   * Checks whether the logged-in user for this session is an admin of the page
   * with the given <code>pageId</code>.
   * @param pageId the ID of the page
   * @return true if the logged-in user is an admin
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAdmin">
   *      Developers Wiki: Pages.isAdmin</a>
   */
  public boolean pages_isAdmin(Long pageId)
    throws FacebookException, IOException {
    return extractBoolean(this.callMethod(FacebookMethod.PAGES_IS_ADMIN,
                                          new Pair<String, CharSequence>("page_id",
                                                                         pageId.toString())));
  }

  /**
   * Sets several property values for an application. The properties available
   * are analogous to the ones editable via the Facebook Developer application.
   * A session is not required to use this method.
   * @param properties an ApplicationPropertySet that is translated into
   *    a single JSON String.
   * @return a boolean indicating whether the properties were successfully set
   */
  public boolean admin_setAppProperties(ApplicationPropertySet properties)
    throws FacebookException, IOException {
    if (null == properties || properties.isEmpty()) {
      throw new IllegalArgumentException("expecting a non-empty set of application properties");
    }

    return extractBoolean(this.callMethod(FacebookMethod.ADMIN_SET_APP_PROPERTIES,
                                          new Pair<String, CharSequence>("properties", properties.toJsonString())));
  }

  /**
   * Gets property values previously set for an application on either the Facebook Developer application or 
   * the with the <code>admin.setAppProperties</code> call.
   * A session is not required to use this method.
   * @param properties an enumeration of the properties to get
   * @return an ApplicationPropertySet
   * @see ApplicationProperty
   * @see <a href="http://wiki.developers.facebook.com/index.php/Admin.getAppProperties">
   *      Developers Wiki: Admin.getAppProperties</a>
   */
  public ApplicationPropertySet admin_getAppProperties(EnumSet<ApplicationProperty> properties)
    throws FacebookException, IOException {
    if (null == properties || properties.isEmpty()) {
      throw new IllegalArgumentException("expecting a non-empty set of application properties");
    }

    JSONArray propList = new JSONArray();
    for (ApplicationProperty prop : properties) {
      propList.add(prop.propertyName());
    }

    String propJson =
      extractString(this.callMethod(FacebookMethod.ADMIN_GET_APP_PROPERTIES, 
                    new Pair<String, CharSequence>("properties", propList.toString())));
    return new ApplicationPropertySet(propJson);
  }

  /**
   * Retrieves all cookies for the application and the given <code>userId</code>.
   * If a <code>cookieName</code> is specified, only that cookie will be returned.
   * 
   * @param userId The user from whom to get the cookies (defaults to logged-in user).
   * @param cookieName The name of the cookie to get. If null, all cookies
   *    will be returned
   * @return a T of cookies.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getCookies">
   *      Developers Wiki: Data.getCookies</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Cookies">
   *      Developers Wiki: Cookies</a>
   */
  public T data_getCookies(Integer userId, CharSequence cookieName)
    throws FacebookException, IOException {
    Collection<Pair<String, CharSequence>> params = 
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.DATA_GET_COOKIES.numParams());
    params.add(new Pair<String,CharSequence>("uid", userId.toString()));
    if (null != cookieName) {
      params.add(new Pair<String,CharSequence>("name", cookieName.toString()));
    }

    return this.callMethod(FacebookMethod.DATA_GET_COOKIES, params);
  }

  /**
   * Retrieves all cookies for the application and the given <code>userId</code>.
   * 
   * @param userId The user from whom to get the cookies (defaults to logged-in user).
   * @return a T of cookies.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getCookies">
   *      Developers Wiki: Data.getCookies</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Cookies">
   *      Developers Wiki: Cookies</a>
   */
  public T data_getCookies(Integer userId)
    throws FacebookException, IOException {
    return data_getCookies(userId, /*cookieName*/ null);
  }

  /**
   * Sets a cookie for a given user and application.
   * @param userId The user for whom this cookie needs to be set
   * @param cookieName Name of the cookie. 
   * @param cookieValue Value of the cookie.
   * @return true if cookie was successfully set, false otherwise
   * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getCookies">
   *      Developers Wiki: Data.setCookie</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Cookies">
   *      Developers Wiki: Cookies</a>
   */
  public boolean data_setCookie(Integer userId, CharSequence cookieName, CharSequence cookieValue)
    throws FacebookException, IOException {
    return data_setCookie(userId, cookieName, cookieValue, /*expiresTimestamp*/null, /*path*/ null);
  }

  /**
   * Sets a cookie for a given user and application.
   * @param userId The user for whom this cookie needs to be set
   * @param cookieName Name of the cookie. 
   * @param cookieValue Value of the cookie.
   * @param path Path relative to the application's callback URL, with which the cookie should be associated. 
   *  If null, defaulted to "/"
   * @return true if cookie was successfully set, false otherwise
   * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getCookies">
   *      Developers Wiki: Data.setCookie</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Cookies">
   *      Developers Wiki: Cookies</a>
   */
  public boolean data_setCookie(Integer userId, CharSequence cookieName, CharSequence cookieValue, CharSequence path)
    throws FacebookException, IOException {
    return data_setCookie(userId, cookieName, cookieValue, /*expiresTimestamp*/null, path);
  }

  /**
   * Sets a cookie for a given user and application.
   * @param userId The user for whom this cookie needs to be set
   * @param cookieName Name of the cookie. 
   * @param cookieValue Value of the cookie.
   * @param expiresTimestamp Time stamp when the cookie should expire. If not specified, the cookie never expires. 
   * @return true if cookie was successfully set, false otherwise
   * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getCookies">
   *      Developers Wiki: Data.setCookie</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Cookies">
   *      Developers Wiki: Cookies</a>
   */
  public boolean data_setCookie(Integer userId, CharSequence cookieName, CharSequence cookieValue, Long expiresTimestamp)
    throws FacebookException, IOException {
    return data_setCookie(userId, cookieName, cookieValue, expiresTimestamp, /*path*/ null);
  }

  /**
   * Sets a cookie for a given user and application.
   * @param userId The user for whom this cookie needs to be set
   * @param cookieName Name of the cookie. 
   * @param cookieValue Value of the cookie.
   * @param expiresTimestamp Time stamp when the cookie should expire. If not specified, the cookie never expires. 
   * @return true if cookie was successfully set, false otherwise
   * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getCookies">
   *      Developers Wiki: Data.setCookie</a>
   * @see <a href="http://wiki.developers.facebook.com/index.php/Cookies">
   *      Developers Wiki: Cookies</a>
   */
  public boolean data_setCookie(Integer userId, CharSequence cookieName, CharSequence cookieValue, Long expiresTimestamp, CharSequence path)
    throws FacebookException, IOException {

    if (null == userId || 0 >= userId)
      throw new IllegalArgumentException("userId should be provided.");
    if (null == cookieName || null == cookieValue)
      throw new IllegalArgumentException("cookieName and cookieValue should be provided.");

    ArrayList<Pair<String, CharSequence>> params =
      new ArrayList<Pair<String, CharSequence>>(FacebookMethod.DATA_GET_COOKIES.numParams());
    params.add(new Pair<String, CharSequence>("uid", userId.toString()));
    params.add(new Pair<String, CharSequence>("name", cookieName));
    params.add(new Pair<String, CharSequence>("value", cookieValue));
    if (null != expiresTimestamp && expiresTimestamp >= 0L)
      params.add(new Pair<String, CharSequence>("expires", expiresTimestamp.toString()));
    if (null != path)
      params.add(new Pair<String, CharSequence>("path", path));
    
    return extractBoolean(this.callMethod(FacebookMethod.DATA_GET_COOKIES, params));
  }
  
  public boolean data_setUserPreference(Integer did, String value)
  throws FacebookException, IOException {

  ArrayList<Pair<String, CharSequence>> params =
    new ArrayList<Pair<String, CharSequence>>(2);
  params.add(new Pair<String, CharSequence>("pref_id", did.toString()));
  params.add(new Pair<String, CharSequence>("value", value));
 
  return extractBoolean(this.callMethod(FacebookMethod.DATA_SET_USERPREF, params));
}
  public T data_getUserPreference(Integer did)
  throws FacebookException, IOException {

  ArrayList<Pair<String, CharSequence>> params =
    new ArrayList<Pair<String, CharSequence>>(1);
  params.add(new Pair<String, CharSequence>("pref_id", did.toString()));
 
  return this.callMethod(FacebookMethod.DATA_GET_USERPREF, params);
}
}
