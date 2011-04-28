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

import java.util.EnumSet;

public enum FacebookMethod
  implements IFacebookMethod, CharSequence {
  // Authentication
  AUTH_CREATE_TOKEN("facebook.auth.createToken"),
  AUTH_GET_SESSION("facebook.auth.getSession", 1),
  // FQL Query
  FQL_QUERY("facebook.fql.query",1),
  // Events
  EVENTS_GET("facebook.events.get", 5),
  EVENTS_GET_MEMBERS("facebook.events.getMembers", 1),
  // Friends
  FRIENDS_GET("facebook.friends.get", 1),
  FRIENDS_GET_LISTS("facebook.friends.getLists"),
  FRIENDS_GET_APP_USERS("facebook.friends.getAppUsers"),
  FRIENDS_GET_REQUESTS("facebook.friends.getRequests"),
  FRIENDS_ARE_FRIENDS("facebook.friends.areFriends", 2),
  // Users
  USERS_GET_INFO("facebook.users.getInfo", 2),
  USERS_GET_LOGGED_IN_USER("facebook.users.getLoggedInUser"),
  USERS_SET_STATUS("facebook.users_setStatus",1),
  USERS_IS_APP_ADDED("facebook.users.isAppAdded"),
  USERS_HAS_APP_PERMISSION("facebook.users.hasAppPermission", 1),
  // Photos
  PHOTOS_GET("facebook.photos.get", 2),
  PHOTOS_GET_ALBUMS("facebook.photos.getAlbums", 1),
  PHOTOS_GET_TAGS("facebook.photos.getTags", 1),
  // PhotoUploads
  PHOTOS_CREATE_ALBUM("facebook.photos.createAlbum",3),
  PHOTOS_ADD_TAG("facebook.photos.addTag", 5),
  PHOTOS_UPLOAD("facebook.photos.upload", 3, true),
  // Notifications
  NOTIFICATIONS_GET("facebook.notifications.get"),
  NOTIFICATIONS_SEND("facebook.notifications.send",3),
  NOTIFICATIONS_SEND_EMAIL("facebook.notifications.sendEmail",4),
  NOTIFICATIONS_SEND_EMAIL_SESSION("facebook.notifications.sendEmail",4),
  // Groups
  GROUPS_GET("facebook.groups.get", 1),
  GROUPS_GET_MEMBERS("facebook.groups.getMembers", 1),
  // FBML
  PROFILE_SET_FBML("facebook.profile.setFBML", 4),
  PROFILE_GET_FBML("facebook.profile.getFBML", 1),
  FBML_REFRESH_REF_URL("facebook.fbml.refreshRefUrl", 1),
  FBML_REFRESH_IMG_SRC("facebook.fbml.refreshImgSrc", 1),
  FBML_SET_REF_HANDLE("facebook.fbml.setRefHandle", 2),
  // Feed
  FEED_PUBLISH_ACTION_OF_USER("facebook.feed.publishActionOfUser", 10),
  FEED_PUBLISH_TEMPLATIZED_ACTION("facebook.feed.publishTemplatizedAction", 12),
  FEED_PUBLISH_STORY_TO_USER("facebook.feed.publishStoryToUser", 11),
  // Marketplace
  MARKETPLACE_GET_CATEGORIES("facebook.marketplace.getCategories"),
  MARKETPLACE_GET_SUBCATEGORIES("facebook.marketplace.getSubCategories", 1),  
  MARKETPLACE_GET_LISTINGS("facebook.marketplace.getListings", 2),  
  MARKETPLACE_CREATE_LISTING("facebook.marketplace.createListing", 3),  
  MARKETPLACE_SEARCH("facebook.marketplace.search", 3),  
  MARKETPLACE_REMOVE_LISTING("facebook.marketplace.removeListing", 2),
  // SMS - Mobile
  SMS_CAN_SEND("facebook.sms.canSend", 1),
  SMS_SEND_MESSAGE("facebook.sms.sendMessage", 3),
  // Facebook Pages
  PAGES_IS_APP_ADDED("facebook.pages.isAppAdded", 1),
  PAGES_IS_ADMIN("facebook.pages.isAdmin", 1),
  PAGES_IS_FAN("facebook.pages.isFan", 2),
  PAGES_GET_INFO("facebook.pages.getInfo", 2),
  PAGES_GET_INFO_NO_SESSION("facebook.pages.getInfo", 2),
  // Administration 
  ADMIN_GET_APP_PROPERTIES("facebook.admin.getAppProperties", 2),
  ADMIN_SET_APP_PROPERTIES("facebook.admin.setAppProperties", 2),
  // Data
  DATA_SET_COOKIE("facebook.data.setCookie", 5),
  DATA_GET_COOKIES("facebook.data.getCookies", 2),
  DATA_SET_USERPREF("facebook.data.setUserPreference", 2),
  DATA_GET_USERPREF("facebook.data.getUserPreference", 1),
  ;

  private String methodName;
  private int numParams;
  private int maxParamsWithSession;
  private boolean takesFile;

  private static EnumSet<FacebookMethod> preAuth = null;
  private static EnumSet<FacebookMethod> postAuth = null;

  /**
   * Methods that don't require an active session to be established.
   * @return the methods
   */
  public static EnumSet<FacebookMethod> preAuthMethods() {
    if (null == preAuth)
      preAuth = EnumSet.of(AUTH_CREATE_TOKEN, AUTH_GET_SESSION, SMS_SEND_MESSAGE, 
                           PAGES_GET_INFO_NO_SESSION, NOTIFICATIONS_SEND_EMAIL,
                           ADMIN_GET_APP_PROPERTIES, ADMIN_SET_APP_PROPERTIES);
    return preAuth;
  }

  /**
   * Methods that do require an active session to be established.
   * @return the methods
   */
  public static EnumSet<FacebookMethod> postAuthMethods() {
    if (null == postAuth)
      postAuth = EnumSet.complementOf(preAuthMethods());
    return postAuth;
  }

  FacebookMethod(String name) {
    this(name, 0, false);
  }

  FacebookMethod(String name, int maxParams ) {
    this(name, maxParams, false);
  }

  FacebookMethod(String name, int maxParams, boolean takesFile ) {
    assert (name != null && 0 != name.length());
    this.methodName = name;
    this.numParams = maxParams;
    this.maxParamsWithSession = maxParams + FacebookRestClient.NUM_AUTOAPPENDED_PARAMS;
    this.takesFile = takesFile;
  }


  /**
   * The name of the method, e.g. "facebook.friends.get"
   * @return the method name
   */
  public String methodName() {
    return this.methodName;
  }

  /**
   * Number of method-specific parameters (i.e. excluding universally required parameters 
   * such as api_key and sig)
   * @return the number of parameters expected 
   */
  public int numParams() {
    return this.numParams;
  }

  /**
   * @return whether the method requires an active session to be established 
   * @see #postAuthMethods
   * @see #preAuthMethods
   */
  public boolean requiresSession() {
    return postAuthMethods().contains(this);
  }

  /**
   * @return the total number of parameters this method accepts, including 
   *         required parameters
   */
  public int numTotalParams() {
    return requiresSession() ? this.maxParamsWithSession : this.numParams;
  }

  /**
   * @return whether the method expects a file to be posted
   */
  public boolean takesFile() {
    return this.takesFile;
  }

  /* Implementing CharSequence */
  public char charAt(int index) {
    return this.methodName.charAt(index);
  }

  public int length() {
    return this.methodName.length();
  }

  public CharSequence subSequence(int start, int end) {
    return this.methodName.subSequence(start, end);
  }

  public String toString() {
    return this.methodName;
  }
}
