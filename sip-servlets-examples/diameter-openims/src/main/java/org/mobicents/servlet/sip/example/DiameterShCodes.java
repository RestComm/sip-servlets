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

package org.mobicents.servlet.sip.example;

/**
 * Diameter Sh Codes constants.
 *
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class DiameterShCodes {

  private DiameterShCodes() {}

  /**
   * AVP Code defined by Diameter specification for User-Identity AVP.  Data type
   * is Grouped.
   */
  public static final int USER_IDENTITY_AVP = 700;

  /**
   * AVP Code defined by Diameter specification for MSISDN AVP.  Data type
   * is OctetString.
   */
  public static final int MSISDN_AVP = 701;

  /**
   * AVP Code defined by Diameter specification for User-Data AVP.  Data type
   * is UserData.
   */
  public static final int USER_DATA_AVP = 702;

  /**
   * AVP Code defined by Diameter specification for Data-Reference AVP.  Data type
   * is Enumerated.
   */
  public static final int DATA_REFERENCE_AVP = 703;

  /**
   * AVP Code defined by Diameter specification for Service-Indication AVP.  Data type
   * is OctetString.
   */
  public static final int SERVICE_INDICATION_AVP = 704;

  /**
   * AVP Code defined by Diameter specification for Subs-Req-Type AVP.  Data type
   * is Enumerated.
   */
  public static final int SUBS_REQ_TYPE_AVP = 705;

  /**
   * AVP Code defined by Diameter specification for Requested-Domain AVP.  Data type
   * is Enumerated.
   */
  public static final int REQUESTED_DOMAIN_AVP = 706;

  /**
   * AVP Code defined by Diameter specification for Current-Location AVP.  Data type
   * is Enumerated.
   */
  public static final int CURRENT_LOCATION_AVP = 707;

  /**
   * AVP Code defined by Diameter specification for Identity-Set AVP.  Data type
   * is Enumerated.
   */
  public static final int IDENTITY_SET_AVP = 708;

  /**
   * AVP Code defined by Diameter specification for Expiry-Time AVP.  Data type
   * is Time.
   */
  public static final int EXPIRY_TIME_AVP = 709;

  /**
   * AVP Code defined by Diameter specification for Send-Data-Indication AVP.  Data type
   * is Enumerated.
   */
  public static final int SEND_DATA_INDICATION_AVP = 710;

  /**
   * AVP Code defined by Diameter specification for Server-Name AVP.  Data type
   * is UTF8String.
   */
  public static final int SERVER_NAME_AVP = 602;

  /**
   * AVP Code defined by Diameter specification for Supported-Features AVP.  Data type
   * is Grouped.
   */
  public static final int SUPPORTED_FEATURES_AVP = 628;

  /**
   * AVP Code defined by Diameter specification for Feature-List-ID AVP.  Data type
   * is Unsigned32.
   */
  public static final int FEATURE_LIST_ID_AVP = 629;

  /**
   * AVP Code defined by Diameter specification for Feature-List AVP.  Data type
   * is Unsigned32.
   */
  public static final int FEATURE_LIST_AVP = 630;

  /**
   * AVP Code defined by Diameter specification for Supported-Applications AVP.  Data type
   * is Grouped.
   */
  public static final int SUPPORTED_APPLICATIONS_AVP = 631;

  /**
   * AVP Code defined by Diameter specification for Public-Identity AVP.  Data type
   * is UTF8String.
   */
  public static final int PUBLIC_IDENTITY_AVP = 601;

  /**
   * Message Code defined by Diameter specification for User-Data-Request/User-Data-Answer
   */
  public static final int USER_DATA_REQUEST = 306;
  public static final int USER_DATA_ANSWER  = 306;

  /**
   * Message Code defined by Diameter specification for Profile-Update-Request/Profile-Update-Answer
   */
  public static final int PROFILE_UPDATE_REQUEST = 307;
  public static final int PROFILE_UPDATE_ANSWER  = 307;

  /**
   * Message Code defined by Diameter specification for Subscribe-Notifications-Request/Subscribe-Notifications-Answer
   */
  public static final int SUBSCRIBE_NOTIFICATIONS_REQUEST = 308;
  public static final int SUBSCRIBE_NOTIFICATIONS_ANSWER = 308;

  /**
   * Message Code defined by Diameter specification for Push-Notification-Request/Push-Notification-Answer
   */
  public static final int PUSH_NOTIFICATION_REQUEST = 309;
  public static final int PUSH_NOTIFICATION_ANSWER = 309;

}
