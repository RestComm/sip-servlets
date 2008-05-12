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

import java.util.HashMap;

/**
 * Enumerates application properties that can be set and retrieved via the 
 * <code>admin.setAppProperties</code> and <code>admin.getAppProperties</code> calls, 
 * respectively.
 * 
 * @see <a href="http://wiki.developers.facebook.com/index.php/ApplicationProperties">
 *   Developer Wiki: Application Properties</a>
 */
public enum ApplicationProperty {
  /** 
   * The name of your application.
   */
  APPLICATION_NAME("application_name", false),
  /** Your application's callback URL. The callback URL cannot be longer than 100 characters. */
  CALLBACK_URL("callback_url", false),
  /** The URL where a user gets redirected after installing your application. The post-install URL cannot be longer than 100 characters. */
  POST_INSTALL_URL("post_install_url", false),
  /** */
  EDIT_URL("edit_url", false), 
  /** */
  DASHBOARD_URL("dashboard_url", false),
  /** The URL where a user gets redirected after removing your application. */
  UNINSTALL_URL("uninstall_url", false),
  /** For Web-based applications, these are the IP addresses of your servers that can access Facebook's servers and serve information to your application. */
  IP_LIST("ip_list", false),
  /** The email address associated with the application; the email address Facebook uses to contact you about your application. (default value is your Facebook email address.) */
  EMAIL("email", false),
  /** The description of your application. */
  DESCRIPTION("description", false),
  /** Indicates whether you render your application with FBML (0) or in an iframe (1). (default value is 1) */
  USE_IFRAME("use_iframe", true),
  /** Indicates whether your application is Web-based (0) or gets installed on a user's desktop (1). (default value is 1) */
  DESKTOP("desktop", true),
  /** Indicates whether your application can run on a mobile device (1) or not (0). (default value is 1) */
  IS_MOBILE("is_mobile", true),
  /** The default FBML code that appears in the user's profile box when he or she adds your application. */
  DEFAULT_FBML("default_fbml", false),
  /** The default FBML code that defines what, if anything, appears in the user's profile actions when he or she adds your application. */
  DEFAULT_ACTION_FBML("default_action_fbml", false),
  /** Indicates whether your application appears in the wide (1) or narrow (0) column of a user's Facebook profile. (default value is 1) */
  DEFAULT_COLUMN("default_column", true),
  /** For applications that can create attachments, this is the URL where you store the attachment's content. */
  MESSAGE_URL("message_url", false),
  /** For applications that can create attachments, this is the label for the action that creates the attachment. It cannot be more than 20 characters. */
  MESSAGE_ACTION("message_action", false),
  /** This is the URL to your application's About page. About pages are now Facebook Pages. */
  ABOUT_URL("about_url", false),
  /** Indicates whether you want to disable (1) or enable (0) News Feed and Mini-Feed stories when a user installs your application. (default value is 1) */
  PRIVATE_INSTALL("private_install", true),
  /** Indicates whether a user can (1) or cannot (0) install your application. (default value is 1) */
  INSTALLABLE("installable", true),
  /** The URL to your application's privacy terms. */
  PRIVACY_URL("privacy_url", false),
  /** The URL to your application's help page. */
  HELP_URL("help_url", false),
  /** The URL to your application's Terms of Service. */
  TOS_URL("tos_url", false),
  /** */
  SEE_ALL_URL("see_all_url", false),
  /** Indicates whether developer mode is enabled (1) or disabled (0). Only developers can install applications in developer mode. (default value is 1) */
  DEV_MODE("dev_mode", true),
  /** A preloaded FQL query. */
  PRELOAD_FQL("preload_fql", false),
  ;

  private String propertyName;
  private boolean isBooleanProperty;
  
  private ApplicationProperty(String name, boolean isBooleanProperty) {
    this.propertyName = name;
    this.isBooleanProperty = isBooleanProperty;
  }

  private static HashMap<String, ApplicationProperty> nameToProperty = new HashMap<String, ApplicationProperty>();
  static {
    for (ApplicationProperty prop: ApplicationProperty.values()) {
      nameToProperty.put(prop.propertyName(), prop);
    }
  }

  public static ApplicationProperty getProperty(String name) {
    return nameToProperty.get(name);
  }

  public String propertyName() {
    return this.propertyName;
  }

  public String toString() {
    return propertyName();
  }
  
  public boolean isBooleanProperty() {
    return this.isBooleanProperty;
  }

  public boolean isStringProperty() {
    return !this.isBooleanProperty;
  }

  /**
   * Returns true if this field has a particular name.
   */
  public boolean isName(String name) {
    return toString().equals(name);
  }
}
