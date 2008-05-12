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

public enum ProfileField {
  ABOUT_ME("about_me"), // Text element corresponding to Facebook 'About Me' profile section. May be blank
  ACTIVITIES("activities"), // User-entered "Activities" profile field. No guaranteed formatting.
  AFFILIATIONS("affiliations"), // list of network affiliations, as affiliations_elt elements, each of which contain year, type, status, name, and key child elements. If no affiliations are returned, this element will be blank. The user's primary network will be listed first.
  /* Notes on each of the children:
          o type takes the following values:
                + 1: college network
                + 2: high school network
                + 3: work network
                + 4: geography network
          o year may be blank, depending on the network type
          o name is the name of the network
          o key is a unique identifier for the network. The user-to-network relation may be stored.
          */
  BIRTHDAY("birthday"), // User-entered "Birthday" profile field. No guaranteed formatting.
  BOOKS("books"), // User-entered "Favorite Books" profile field. No guaranteed formatting.
  CURRENT_LOCATION("current_location"), // User-entered "Current Location" profile fields. Contains three children, city, state_or_region, and country.
  /*
      Notes on each of the children:
          o city is user-entered, and may be blank
          o state_or_region is a well-defind two-letter American state or Canadian province abbreviation, and may be blank
          o country is well-defined, and may be blank.
          */
  EDUCATION_HISTORY("education_history"), // From the user-entered Education Info profile fields. List of school information, as school_info_elt elements, each of which contain name, year, and key child elements. If no school information is returned, this element will be blank.
   /*
       Notes on each of the children:
           o year is a four-digit year, and may be blank
           o name is the name of the school, and is user-specified
           o key is a unique identifier for the school network. The user-to-network relation may be stored. This may be blank also. */
  FIRST_NAME("first_name"), // Generated from the user-entered "Name" profile field.
  HAS_ADDED_APP("has_added_app"), // Indicates whether the user has added the calling application to his Facebook account.
  HOMETOWN_LOCATION("hometown_location"), // User-entered "Hometown" profile fields. Contains three children, city, state_or_region, and country.
  /*
      Notes on each of the children:
          o city is user-entered, and may be blank
          o state_or_region is a well-defind two-letter American state or Canadian province abbreviation, and may be blank
          o country is well-defined, and may be blank
          */
  HS_INFO("hs_info"), // User-entered high school information. Contains five children, hs1_name, hs2_name, grad_year, hs1_key, and hs2_key.
  /*
      Notes on each of the children:
          o hs1_name is well-defined, and may be left blank
          o hs2_name is well-defined, and may be left blank, though may not have information if is blank.
          o year is a four-digit year, or may be blank
          o hs1_key is a unique key representing that school, and is not blank if and only if hs1_name is not blank.
          o hs2_key is a unique key representing that school, and is not blank if and only if hs2_name is not blank.
          */
  INTERESTS("interests"), // User-entered "Interests" profile field. No guaranteed formatting.
  IS_APP_USER("is_app_user"), // Indicates whether the user has used the calling application.
  LAST_NAME("last_name"), // is generated from the user-entered "Name" profile field.
  MEETING_FOR("meeting_for"), // list of desired relationship types corresponding to the "Looking For" profile element. If no relationship typed are specified, the meeting_for element will be empty. Otherwise represented as a list of meeting_for_elt child text elements, which may each contain one of the following strings: Friendship, A Relationship, Dating, Random Play, Whatever I can get
  MEETING_SEX("meeting_sex"), // list of desired relationship genders corresponding to the "Interested In" profile element. If no relationship genders are specified, the meeting_sex element will be empty. Otherwise represented as a list of meeting_sex_elt child text elements, which may each contain one of the following strings: Male, Female
  MOVIES("movies"), // User-entered "Favorite Movies" profile field. No guaranteed formatting.
  MUSIC("music"), // User-entered "Favorite Music" profile field. No guaranteed formatting.
  NAME("name"), // User-entered "Name" profile field. May not be blank.
  NOTES_COUNT("notes_count"), // Total number of notes written by the user.
  PIC("pic"), // URL of user profile picture, with max width 100px and max height 300px. May be blank.
  PIC_BIG("pic_big"), // URL of user profile picture, with max width 200px and max height 600px. May be blank.
  PIC_SMALL("pic_small"), // URL of user profile picture, with max width 50px and max height 150px. May be blank.
  PIC_SQUARE("pic_square"), // URL of a square section of the user profile picture, with width 50px and height 50px. May be blank.
  POLITICAL("political"), // User-entered "Political View" profile field. Is either blank or one of the following strings: Very Liberal, Liberal, Moderate, Conservative, Very Conservative, Apathetic, Liberation, Other
  PROFILE_UPDATE_TIME("profile_update_time"), // Time (in seconds since epoch) that the user's profile was last updated.
  QUOTES("quotes"), // User-entered "Favorite Quotes" profile field. No guaranteed formatting.
  RELATIONSHIP_STATUS("relationship_status"), // User-entered "Relationship Status" profile field. Is either blank or one of the following strings: Single, In a Relationship, In an Open Relationship, Engaged, Married, It's Complicated
  RELIGION("religion"), // User-entered "Religious Views" profile field. No guaranteed formatting.
  SIGNIFICANT_OTHER_ID("significant_other_id"), // the id of the person the user is in a relationship with. Only shown if both people in the relationship are users of the application making the request.
  SEX("sex"), // User-entered "Sex" profile file. Either "Male", "Female", or left blank.
  STATUS("status"), // Contains a "message" child with user-entered status information, as well as a "time" child with the time (in seconds since epoch) at which the status message was set.
  TIMEZONE("timezone"), // offset from GMT (e.g. California is -8).
  TV("tv"), // User-entered "Favorite TV Shows" profile field. No guaranteed formatting.
  WALL_COUNT("wall_count"), // Total number of posts to the user's wall.
  WORK_HISTORY("work_history"), // list of work history information, as work_info elements, each of which contain location, company_name, position, description, start_date and end_date child elements. If no work history information is returned, this element will be blank.
    /*
      Notes on each of the children:
          o location is user-entered, and has a similar format to current_location and hometown_location above
          o company_name is user-entered, and does not necessarily correspond to a Facebook work network
          o description is user-entered, and may be blank
          o position is user-entered, and may be blank
          o start_date is of the form YYYY-MM, YYYY, or MM. It may be blank
          o end_date is of the form YYYY-MM, YYYY, or MM. It may be blank.
     */
  ;
  /*
      Privacy note: For any user submitted to this method, the following user fields will be visible to an application only if that user has signed up for that application:

      * "meeting_for"
      * "meeting_sex"
      * "religion"
      *  "significant_other_id"

      Additionally, the significant_other_id field is only shown if the user contained in that field has signed up for that application.

      The other fields are limited only by the user's privacy settings on the Facebook site.
    */
  private String fieldName;

  ProfileField(String name) {
    this.fieldName = name;
  }

  public String fieldName() {
    return this.fieldName;
  }

  public String toString() {
    return fieldName();
  }
  
  /**
   * Returns true if this field has a particular name.
   */
  public boolean isName(String name) {
    return toString().equals(name);
  }
}
