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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Generic interface for a FacebookRestClient, parameterized by output format.
 * For continually updated documentation, please refer to the
 * <a href="http://wiki.developers.facebook.com/index.php/API">
 * Developer Wiki</a>.
 */
public interface IFacebookRestClient<T> {
  public static final String TARGET_API_VERSION = "1.0";
  public static final String ERROR_TAG         = "error_response";
  public static final String FB_SERVER         = "api.facebook.com/restserver.php";
  public static final String SERVER_ADDR       = "http://" + FB_SERVER;
  public static final String HTTPS_SERVER_ADDR = "https://" + FB_SERVER;
  
  public void setDebug(boolean isDebug);

  public boolean isDebug();

  public boolean isDesktop();

  public void setIsDesktop(boolean isDesktop);

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
    throws FacebookException, IOException;

  /**
   * Sets the FBML for a profile box on the logged-in user's profile.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFbml</a>
   */
  public boolean profile_setProfileFBML(CharSequence fbmlMarkup)
    throws FacebookException, IOException;

  /**
   * Sets the FBML for profile actions for  the logged-in user.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setProfileActionFBML(CharSequence fbmlMarkup)
    throws FacebookException, IOException;

  /**
   * Sets the FBML for the logged-in user's profile on mobile devices.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setMobileFBML(CharSequence fbmlMarkup)
    throws FacebookException, IOException;

  /**
   * Sets the FBML for a profile box on the user or page profile with ID <code>profileId</code>.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFbml</a>
   */
  public boolean profile_setProfileFBML(CharSequence fbmlMarkup, Long profileId)
    throws FacebookException, IOException;

  /**
   * Sets the FBML for profile actions for the user or page profile with ID <code>profileId</code>.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   *      Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setProfileActionFBML(CharSequence fbmlMarkup, Long profileId)
    throws FacebookException, IOException;

  /**
   * Sets the FBML for the user or page profile with ID <code>profileId</code> on mobile devices.
   * @param fbmlMarkup refer to the FBML documentation for a description of the markup and its role in various contexts
   * @param profileId a page or user ID (null for the logged-in user)
   * @return a boolean indicating whether the FBML was successfully set
   * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">
   * Developers wiki: Profile.setFBML</a>
   */
  public boolean profile_setMobileFBML(CharSequence fbmlMarkup, Long profileId)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

  /**
   * Gets the FBML for a user's profile, including the content for both the profile box
   * and the profile actions.
   * @param userId the user whose profile FBML to set
   * @return a T containing FBML markup
   */
  public T profile_getFBML(Integer userId)
    throws FacebookException, IOException;

  /**
   * Recaches the referenced url.
   * @param url string representing the URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshRefUrl(String url)
    throws FacebookException, IOException;

  /**
   * Recaches the referenced url.
   * @param url the URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshRefUrl(URL url)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

  /**
   * Recaches the image with the specified imageUrl.
   * @param imageUrl String representing the image URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshImgSrc(String imageUrl)
    throws FacebookException, IOException;

  /**
   * Recaches the image with the specified imageUrl.
   * @param imageUrl the image URL to refresh
   * @return boolean indicating whether the refresh succeeded
   */
  public boolean fbml_refreshImgSrc(URL imageUrl)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
  throws FacebookException, IOException;

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
    throws FacebookException, IOException;
  
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
    throws FacebookException, IOException;

  /**
   * Publish the notification of an action taken by a user to newsfeed.
   * @param title the title of the feed story (up to 60 characters, excluding tags)
   * @param body (optional) the body of the feed story (up to 200 characters, excluding tags)
   * @return whether the story was successfully published; false in case of permission error
   * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishActionOfUser">
   *      Developers Wiki: Feed.publishActionOfUser</a>
   */
  public boolean feed_publishActionOfUser(CharSequence title, CharSequence body)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

  /**
   * Returns all visible events according to the filters specified. This may be used to find all events of a user, or to query specific eids.
   * @param eventIds filter by these event ID's (optional)
   * @param userId filter by this user only (optional)
   * @param startTime UTC lower bound (optional)
   * @param endTime UTC upper bound (optional)
   * @return T of events
   */
  public T events_get(Integer userId, Collection<Long> eventIds, Long startTime, Long endTime)
    throws FacebookException, IOException;

  /**
   * Retrieves the membership list of an event
   * @param eventId event id
   * @return T consisting of four membership lists corresponding to RSVP status, with keys
   * 'attending', 'unsure', 'declined', and 'not_replied'
   */
  public T events_getMembers(Number eventId)
    throws FacebookException, IOException;

  /**
   * Retrieves whether two users are friends.
   * @param userId1 
   * @param userId2
   * @return T
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.areFriends">
   *      Developers Wiki: Friends.areFriends</a>
   */
  public T friends_areFriends(int userId1, int userId2)
    throws FacebookException, IOException;

  /**
   * Retrieves whether pairs of users are friends.
   * Returns whether the first user in <code>userIds1</code> is friends with the first user in
   * <code>userIds2</code>, the second user in <code>userIds1</code> is friends with the second user in
   * <code>userIds2</code>, etc.
   * @param userIds1
   * @param userIds2
   * @return T
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.areFriends">
   *      Developers Wiki: Friends.areFriends</a>
   */
  public T friends_areFriends(Collection<Integer> userIds1, Collection<Integer> userIds2)
    throws FacebookException, IOException;

  /**
   * Retrieves the friends of the currently logged in user.
   * @return T of friends
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.get">
   *      Developers Wiki: Friends.get</a>
   */
  public T friends_get()
    throws FacebookException, IOException;

  /**
   * Retrieves the friend lists of the currently logged in user.
   * @return T of friend lists
   * @see <a href="http://wiki.developers.facebook.com/index.php/Friends.getLists">
   *      Developers Wiki: Friends.getLists</a>
   */
  public T friends_getLists()
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;
  
  /**
   * Retrieves the friends of the currently logged in user, who are also users
   * of the calling application.
   * @return array of friends
   */
  public T friends_getAppUsers()
    throws FacebookException, IOException;

  /**
   * Retrieves the requested info fields for the requested set of users.
   * @param userIds a collection of user IDs for which to fetch info
   * @param fields a set of ProfileFields
   * @return a T consisting of a list of users, with each user element
   * containing the requested fields.
   */
  public T users_getInfo(Collection<Integer> userIds, EnumSet<ProfileField> fields)
    throws FacebookException, IOException;

  /**
   * Retrieves the requested info fields for the requested set of users.
   * @param userIds a collection of user IDs for which to fetch info
   * @param fields a set of strings describing the info fields desired, such as "last_name", "sex"
   * @return a T consisting of a list of users, with each user element
   * containing the requested fields.
   */
  public T users_getInfo(Collection<Integer> userIds, Set<CharSequence> fields)
    throws FacebookException, IOException;

  /**
   * Retrieves the user ID of the user logged in to this API session
   * @return the Facebook user ID of the logged-in user
   */
  public int users_getLoggedInUser()
    throws FacebookException, IOException;

  /**
   * Retrieves an indicator of whether the logged-in user has added the
   * application associated with the _apiKey.
   * @return boolean indicating whether the user has added the app
   * @see <a href="http://wiki.developers.facebook.com/index.php/Users.isAppAdded">
   *      Developers Wiki: Users.isAppAdded</a> 
   */
  public boolean users_isAppAdded()
    throws FacebookException, IOException;

  /**
   * Retrieves whether the logged-in user has granted the specified permission to this application.
   * @param permission an extended permission (e.g. FacebookExtendedPerm.MARKETPLACE,
   *        "photo_upload")
   * @return boolean indicating whether the user has the permission
   * @see FacebookExtendedPerm
   * @see <a href="http://wiki.developers.facebook.com/index.php/Users.hasAppPermission">
   *      Developers Wiki: Users.hasAppPermission</a> 
   */
  public boolean users_hasAppPermission(CharSequence permission)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

  /**
   * Used to retrieve photo objects using the search parameters (one or more of the
   * parameters must be provided).
   *
   * @param subjId retrieve from photos associated with this user (optional).
   * @param albumId retrieve from photos from this album (optional)
   * @param photoIds retrieve from this list of photos (optional)
   *
   * @return an T of photo objects.
   */
  public T photos_get(Integer subjId, Long albumId, Collection<Long> photoIds)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

  /**
   * Retrieves album metadata for albums owned by a user.
   * @param userId   (optional) the id of the albums' owner (optional)
   * @return album objects
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums">
   *      Developers Wiki: Photos.getAlbums</a>
   */
  public T photos_getAlbums(Integer userId)
    throws FacebookException, IOException;

  /**
   * Retrieves album metadata for a list of album IDs.
   * @param albumIds the ids of albums whose metadata is to be retrieved
   * @return album objects
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums">
   *      Developers Wiki: Photos.getAlbums</a>
   */
  public T photos_getAlbums(Collection<Long> albumIds)
    throws FacebookException, IOException;

  /**
   * Retrieves the tags for the given set of photos.
   * @param photoIds The list of photos from which to extract photo tags.
   * @return the created album
   */
  public T photos_getTags(Collection<Long> photoIds)
    throws FacebookException, IOException;

  /**
   * Creates an album.
   * @param albumName The list of photos from which to extract photo tags.
   * @return the created album
   */
  public T photos_createAlbum(String albumName)
    throws FacebookException, IOException;

  /**
   * Creates an album.
   * @param name The album name.
   * @param location The album location (optional).
   * @param description The album description (optional).
   * @return an array of photo objects.
   */
  public T photos_createAlbum(String name, String description, String location)
    throws FacebookException, IOException;

  /**
   * Adds several tags to a photo.
   * @param photoId The photo id of the photo to be tagged.
   * @param tags A list of PhotoTags.
   * @return a list of booleans indicating whether the tag was successfully added.
   */
  public T photos_addTags(Long photoId, Collection<PhotoTag> tags)
    throws FacebookException, IOException;

  /**
   * Adds a tag to a photo.
   * @param photoId The photo id of the photo to be tagged.
   * @param xPct The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
   * @param yPct The vertical position of the tag, as a percentage from 0 to 100, from the top of the photo.
   * @param taggedUserId The list of photos from which to extract photo tags.
   * @return whether the tag was successfully added.
   */
  public boolean photos_addTag(Long photoId, Integer taggedUserId, Double xPct, Double yPct)
    throws FacebookException, IOException;

  /**
   * Adds a tag to a photo.
   * @param photoId The photo id of the photo to be tagged.
   * @param xPct The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
   * @param yPct The list of photos from which to extract photo tags.
   * @param tagText The text of the tag.
   * @return whether the tag was successfully added.
   */
  public boolean photos_addTag(Long photoId, CharSequence tagText, Double xPct, Double yPct)
    throws FacebookException, IOException;

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   * Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo)
    throws FacebookException, IOException;

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @param caption a description of the image contents
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   * Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo, String caption)
    throws FacebookException, IOException;

  /**
   * Uploads a photo to Facebook.
   * @param photo an image file
   * @param albumId the album into which the photo should be uploaded
   * @return a T with the standard Facebook photo information
   * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload">
   * Developers wiki: Photos.upload</a>
   */
  public T photos_upload(File photo, Long albumId)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

  /**
   * Retrieves the groups associated with a user
   * @param userId Optional: User associated with groups.
   * A null parameter will default to the session user.
   * @param groupIds Optional: group ids to query.
   * A null parameter will get all groups for the user.
   * @return array of groups
   */
  public T groups_get(Integer userId, Collection<Long> groupIds)
    throws FacebookException, IOException;

  /**
   * Retrieves the membership list of a group
   * @param groupId the group id
   * @return a T containing four membership lists of
   * 'members', 'admins', 'officers', and 'not_replied'
   */
  public T groups_getMembers(Number groupId)
    throws FacebookException, IOException;

  /**
   * Retrieves the results of a Facebook Query Language query
   * @param query : the FQL query statement
   * @return varies depending on the FQL query
   */
  public T fql_query(CharSequence query)
    throws FacebookException, IOException;

  /**
   * Retrieves the outstanding notifications for the session user.
   * @return a T containing
   * notification count pairs for 'messages', 'pokes' and 'shares',
   * a uid list of 'friend_requests', a gid list of 'group_invites',
   * and an eid list of 'event_invites'
   */
  public T notifications_get()
    throws FacebookException, IOException;

  /**
   * Send a notification message to the specified users on behalf of the logged-in user.
   *
   * @param recipientIds the user ids to which the message is to be sent. if empty,
   *        notification will be sent to logged-in user.
   * @param notification the FBML to be displayed on the notifications page; only a stripped-down 
   *        set of FBML tags that result in text and links is allowed
   * @return a URL, possibly null, to which the user should be redirected to finalize
   * the sending of the email
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.send">
   *      Developers Wiki: notifications.send</a>
   */
  public void notifications_send(Collection<Integer> recipientIds, CharSequence notification)
    throws FacebookException, IOException;

  /**
   * Send a notification message to the logged-in user.
   *
   * @param notification the FBML to be displayed on the notifications page; only a stripped-down 
   *    set of FBML tags that result in text and links is allowed
   * @return a URL, possibly null, to which the user should be redirected to finalize
   * the sending of the email
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.send">
   *      Developers Wiki: notifications.send</a>
   */
  public void notifications_send(CharSequence notification)
    throws FacebookException, IOException;

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
   * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail">
   *      Developers Wiki: notifications.sendEmail</a>
   */
  public String notifications_sendEmail(Collection<Integer> recipientIds, CharSequence subject, CharSequence fbml, CharSequence text)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;
  
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
    throws FacebookException, IOException;
  
  /**
   * Determines whether this application can send SMS to the user identified by <code>userId</code>
   * @param userId a user ID
   * @return true if sms can be sent to the user
   * @see FacebookExtendedPerm#SMS
   * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages">
   *      Developers Wiki: Mobile: Application Generated Messages</a>
   */
  public boolean sms_canSend(Integer userId)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;  

  /**
   * Call this function and store the result, using it to generate the
   * appropriate login url and then to retrieve the session information.
   * @return an authentication token
   */
  public String auth_createToken()
    throws FacebookException, IOException;

  /**
   * Call this function to retrieve the session information after your user has
   * logged in.
   * @param authToken the token returned by auth_createToken or passed back to your callback_url.
   */
  public String auth_getSession(String authToken)
    throws FacebookException, IOException;

  /**
   * Call this function to get the user ID.
   *
   * @return The ID of the current session's user, or -1 if none.
   */
  public int auth_getUserId(String authToken)
    throws FacebookException, IOException;
  
  /**
   * Create a marketplace listing. The create_listing extended permission is required. 
   * @param showOnProfile whether 
   * @return the id of the created listing
   * @see #users_hasAppPermission
   * @see FacebookExtendedPerm#MARKETPLACE
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing">
   *      Developers Wiki: marketplace.createListing</a>
   */
  public Long marketplace_createListing(Boolean showOnProfile, MarketplaceListing attrs)
    throws FacebookException, IOException;
  
  /**
   * Modify a marketplace listing. The create_listing extended permission is required. 
   * @return the id of the edited listing
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing">
   *      Developers Wiki: marketplace.createListing</a>
   */
  public Long marketplace_editListing(Long listingId, Boolean showOnProfile, MarketplaceListing attrs)
    throws FacebookException, IOException;
  
  /**
   * Remove a marketplace listing. The create_listing extended permission is required.
   * @param listingId the listing to be removed
   * @return boolean indicating whether the listing was removed
   * @see #users_hasAppPermission
   * @see FacebookExtendedPerm#MARKETPLACE
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing">
   *      Developers Wiki: marketplace.removeListing</a>
   */
  public boolean marketplace_removeListing(Long listingId)
    throws FacebookException, IOException;

  /**
   * Remove a marketplace listing. The create_listing extended permission is required. 
   * @param listingId the listing to be removed
   * @param status MARKETPLACE_STATUS_DEFAULT, MARKETPLACE_STATUS_SUCCESS, or MARKETPLACE_STATUS_NOT_SUCCESS
   * @return boolean indicating whether the listing was removed
   * @see #users_hasAppPermission
   * @see FacebookExtendedPerm#MARKETPLACE
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing">
   *      Developers Wiki: marketplace.removeListing</a>
   */
  public boolean marketplace_removeListing(Long listingId, CharSequence status)
    throws FacebookException, IOException;
  
  /**
   * Get the categories available in marketplace.
   * @return a T listing the marketplace categories
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getCategories">
   *      Developers Wiki: marketplace.getCategories</a>
   */
  public T marketplace_getCategories()
    throws FacebookException, IOException;

  /**
   * Get the subcategories available for a category.
   * @param category a category, e.g. "HOUSING"
   * @return a T listing the marketplace sub-categories
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getSubCategories">
   *      Developers Wiki: marketplace.getSubCategories</a>
   */
  public T marketplace_getSubCategories(CharSequence category)
    throws FacebookException, IOException;

  /**
   * Fetch marketplace listings, filtered by listing IDs and/or the posting users' IDs.
   * @param listingIds listing identifiers (required if uids is null/empty)
   * @param userIds posting user identifiers (required if listingIds is null/empty)
   * @return a T of marketplace listings
   * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getListings">
   *      Developers Wiki: marketplace.getListings</a>
   */
  public T marketplace_getListings(Collection<Long> listingIds, Collection<Integer> userIds)
    throws FacebookException, IOException;
  
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
    throws FacebookException, IOException;
  
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
    throws FacebookException, IOException;
  
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
    throws FacebookException, IOException;
  
  /**
   * Retrieves the requested profile fields for the Facebook Pages of the user with the given 
   * <code>userId</code>.
   * @param userId the ID of a user about whose pages to fetch info
   * @param fields a set of PageProfileFields
   * @return a T consisting of a list of pages, with each page element
   *     containing the requested fields.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo">
   *      Developers Wiki: Pages.getInfo</a>
   */
  public T pages_getInfo(Integer userId, EnumSet<PageProfileField> fields)
    throws FacebookException, IOException;

  /**
   * Retrieves the requested profile fields for the Facebook Pages of the user with the given 
   * <code>userId</code>.
   * @param userId the ID of a user about whose pages to fetch info
   * @param fields a set of page profile fields
   * @return a T consisting of a list of pages, with each page element
   *     containing the requested fields.
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo">
   *      Developers Wiki: Pages.getInfo</a>
   */
  public T pages_getInfo(Integer userId, Set<CharSequence> fields)
    throws FacebookException, IOException;

  /**
   * Checks whether a page has added the application
   * @param pageId the ID of the page
   * @return true if the page has added the application
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAppAdded">
   *      Developers Wiki: Pages.isAppAdded</a>
   */
  public boolean pages_isAppAdded(Long pageId)
    throws FacebookException, IOException;
  
  /**
   * Checks whether a user is a fan of the page with the given <code>pageId</code>.
   * @param pageId the ID of the page
   * @param userId the ID of the user (defaults to the logged-in user if null)
   * @return true if the user is a fan of the page
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan">
   *      Developers Wiki: Pages.isFan</a>
   */
  public boolean pages_isFan(Long pageId, Integer userId)
    throws FacebookException, IOException;
  
  /**
   * Checks whether the logged-in user is a fan of the page with the given <code>pageId</code>.
   * @param pageId the ID of the page
   * @return true if the logged-in user is a fan of the page
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan">
   *      Developers Wiki: Pages.isFan</a>
   */
  public boolean pages_isFan(Long pageId)
    throws FacebookException, IOException;
  
  /**
   * Checks whether the logged-in user for this session is an admin of the page 
   * with the given <code>pageId</code>.
   * @param pageId the ID of the page
   * @return true if the logged-in user is an admin
   * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAdmin">
   *      Developers Wiki: Pages.isAdmin</a>
   */
  public boolean pages_isAdmin(Long pageId)
    throws FacebookException, IOException;

  /**
   * Sets several property values for an application. The properties available
   * are analogous to the ones editable via the Facebook Developer application.
   * A session is not required to use this method.
   * @param properties an ApplicationPropertySet that is translated into
   *    a single JSON String.
   * @return a boolean indicating whether the properties were successfully set
   */
  public boolean admin_setAppProperties(ApplicationPropertySet properties)
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;

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
    throws FacebookException, IOException;
}
