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

/**
 * Enum describing the profile fields of Facebook Pages.
 * @see IFacebookRestClient#pages_getInfo
 */
public enum PageProfileField {
  /**The id corresponding to the page returned. This is always returned (whether included in fields or not, and always as the first subelement.*/
  PAGE_ID("page_id"),

  /**Page entered profile field. May not be blank.*/
  NAME("name"),

  /**URL of profile picture, with max width 50px and max height 150px. May be blank.*/
  PIC_SMALL("pic_small"),

  /** URL of a square section of the profile picture, with width 50px and height 50px. May be blank. */
  PIC_SQUARE("pic_square"),

  /**URL of profile picture with max width 200px and max height 600px. May be blank.*/
  PIC_BIG("pic_big"),

  /**URL of profile picture with max width 100px and max height 300px. May be blank.*/
  PIC("pic"),

  /**URL of profile picture with max width 396px and max height 1188px. May be blank.*/
  PIC_LARGE("pic_large"),

  /**Contains the type of the page.*/
  TYPE("type"),

  /**Contains the website of the page.*/
  WEBSITE("website"),

  /**
   * Contains the location of the entity. Applies to Local Businesses. 
   * Contains five children: 
   *   street - may be blank, 
   *   city - may be blank, 
   *   state - well-defined two-letter American state or Canadian province abbreviation, and may be blank,
   *   country - well-defined, may be blank, 
   *   zip - an integer, 0 if unspecified.
   */
  LOCATION("location"),

  /**
   * Contains the operating hours. Each local business will be allowed to specify up to two sets of operating hours per day. 
   * Contains the following children: <br/>
   *   mon_1_open, mon_1_close, tue_1_open, tue_1_close, wed_1_open, wed_1_close, thu_1_open, thu_1_close, 
   *   fri_1_open, fri_1_close, sat_1_open, sat_1_close, sun_1_open, sun_1_close, </br>
   *   mon_2_open, mon_2_close, tue_2_open, tue_2_close, wed_2_open, wed_2_close, thu_2_open, thu_2_close,
   *   fri_2_open, fri_2_close, sat_2_open, sat_2_close, sun_2_open, sun_2_close. </br>
   *   
   * Each field is returned with time (in seconds since epoch). For example, 9:00 AM is represented as 406800
   */
  HOURS("hours"),

  /**
   * Parking options available. <br/>
   * Contains three children: street, lot, and valet. Each field returned is a boolean value (1 or 0) 
   * indicating if the Page has the specified parking option.
   */
  PARKING("parking"),

  /** Public transit details, e.g. "Take Caltrain to Palo Alto station. Walk down University Ave one block." */
  PUBLIC_TRANSIT("public_transit"),
  
  /** Restaurant recommended attire, may be one of Unspecfied, Casual, or Dressy */
  ATTIRE("attire"),

  /** Payment options accepted. Contains five children: cash_only, visa, amex, master_card, and discover.
   * Notes on the children: 
   * <ul>
   * <li>Each field returned is a boolean value (1 or 0) indicating if the Page accepts the given payment option.</li>
   * <li>Note that if <b>cash_only</b> is set to 1, the others would be set to 0.</li>
   * </ul>
   */
  PAYMENT_OPTIONS("payment_options"),

  /**Members of the band, may be blank.*/
  BAND_MEMBERS("band_members"),

  /**biography field, may be blank.*/
  BIO("bio"),

  /**hometown field, may be blank.*/
  HOMETOWN("hometown"),

  /**genre of music. Contains the following children: dance, party, relax, talk, think, workout, sing, intimate, raunchy, headphones .
   * Notes on the children:
   * Zero or more of them may be set
   * May be 1 or 0. */
  GENRE("genre"),

  /**record label, may be blank*/
  RECORD_LABEL("record_label"),

  /**influences, may be blank*/
  INFLUENCES("influences"),

  /**Bool (0 or 1) indicating whether the page has added the calling application to their Facebook account.*/
  HAS_ADDED_APP("has_added_app"),

  /**When company was founded, may be blank*/
  FOUNDED("founded"),

  /**overview of company, may be blank*/
  COMPANY_OVERVIEW("company_overview"),

  /**Mission of company, may be blank*/
  MISSION("mission"),

  /**Company's products, may be blank*/
  PRODUCTS("products"),

  /**Release date of film, may be blank*/
  RELEASE_DATE("release_date"),

  /**Who's starring in TV/Film, may be blank*/
  STARRING("starring"),

  /**Who wrote TV/Film, may be blank*/
  WRITTEN_BY("written_by"),

  /**Who directed TV/Film, may be blank*/
  DIRECTED_BY("directed_by"),

  /**Who produced TV/Film, may be blank*/
  PRODUCED_BY("produced_by"),

  /**Studio Film was produced, may be blank*/
  STUDIO("studio"),

  /**Awards received by TV/Film, may be blank*/
  AWARDS("awards"),

  /**Plot outline of TV/Film, may be blank*/
  PLOT_OUTLINE("plot_outline"),

  /**Network of TV show, may be blank*/
  NETWORK("network"),

  /**Season of TV show, may be blank*/
  SEASON("season"),

  /**Schedule of TV show, may be blank */
  SCHEDULE("schedule"),

  /**Current location, may be blank*/
  CURRENT_LOCATION("current_location"),

  /**Boooking agent, may be blank*/
  BOOKING_AGENT("booking_agent"),

  /**Artists also liked by the musician, may be blank*/
  ARTISTS_WE_LIKE("artists_we_like"),

  /**Band interests, may be blank*/
  BAND_INTERESTS("band_interests"),

  /**Affiliation field of person or team, may be blank*/
  AFFILIATION("affiliation"),

  /**Birthday field, may be blank. In the format mm/dd/yyyy*/
  BIRTHDAY("birthday"),

  /**Personal information of public figure, may be blank*/
  PERSONAL_INFO("personal_info"),

  /**Personal interests of public figure, may be blank*/
  PERSONAL_INTERESTS("personal_interests"),

  /**members of team, may be blank*/
  MEMBERS("members"),

  /**when automotive was built, may be blank*/
  BUILT("built"),

  /**features of automotive, may be blank*/
  FEATURES("features"),

  /**mpg of automotive, may be blank*/
  MPG("mpg"),

  /**general info field, may be blank*/
  GENERAL_INFO("general_info");

  private String fieldName;

  PageProfileField(String name) {
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
