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

import java.io.IOException;

import java.net.URL;

import java.util.EnumSet;

/**
 * An enumeration of the extended permissions available for applications using
 * the Facebook API.
 * <br/>
 * Send users to the result of {@link #authorizationUrl(String, CharSequence)}
 * to request a permission.
 */
public enum FacebookExtendedPerm 
  implements CharSequence {  
  /** Required for users_setStatus method
   * @see FacebookRestClient#users_setStatus
   */
  STATUS_UPDATE("status_update"),
  /** Required for photos_upload and photos_addTag API methods
   * @see FacebookRestClient#photos_upload
   * @see FacebookRestClient#photos_addTag
   */
  PHOTO_UPLOAD("photo_upload"),
  /** Required for marketplace_createListing and marketplace_removeListing API methods 
   * @see FacebookRestClient#marketplace_createListing
   * @see FacebookRestClient#marketplace_editListing
   * @see FacebookRestClient#marketplace_removeListing(Long)
   */
  MARKETPLACE("create_listing"),
  /** Required for sending SMS to the user
   * @see FacebookRestClient#sms_canSend
   * @see FacebookRestClient#sms_sendMessage
   */
  SMS("sms"),
  ;

  public static final String PERM_AUTHORIZE_ADDR = "http://www.facebook.com/authorize.php";
  private String permissionName;

  FacebookExtendedPerm(String name) {
    setPermissionName(name);
  }

  public void setPermissionName(String permissionName) {
    this.permissionName = permissionName;
  }

  public String getPermissionName() {
    return permissionName;
  }

  /**
   * The URL to which to send the user to request the extended permission.
   * @param apiKey
   * @param permission
   * @return a URL
   */
  public static URL authorizationUrl(String apiKey, FacebookExtendedPerm permission)
    throws IOException {
    return authorizationUrl(apiKey, permission.getPermissionName());
  }

  /**
   * The URL to which to send the user to request the extended permission.
   * @param apiKey
   * @param permission
   * @return a URL
   */
  public static URL authorizationUrl(String apiKey, CharSequence permission)
    throws IOException {
    String url = String.format("%s?api_key=%s&v=1.0&ext_perm=%s", PERM_AUTHORIZE_ADDR, apiKey, permission);
    return new URL(url);
  }

  /* Implementing CharSequence */
  public char charAt(int index) {
    return this.permissionName.charAt(index);
  }

  public int length() {
    return this.permissionName.length();
  }

  public CharSequence subSequence(int start, int end) {
    return this.permissionName.subSequence(start, end);
  }

  public String toString() {
    return this.permissionName;
  }
}
