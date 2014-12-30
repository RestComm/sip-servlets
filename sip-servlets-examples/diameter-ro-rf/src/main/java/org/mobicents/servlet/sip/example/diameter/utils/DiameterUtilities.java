/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.example.diameter.utils;

import org.apache.log4j.Logger;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Message;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;

public class DiameterUtilities {

  private static Logger logger = Logger.getLogger(DiameterUtilities.class);

  public static AvpDictionary AVP_DICTIONARY = AvpDictionary.INSTANCE;

  public static void printMessage(Message message) {
    String reqFlag = message.isRequest() ? "R" : "A";
    String flags = reqFlag += message.isError() ? " | E" : "";

    if(logger.isInfoEnabled()) {
      logger.info("Message [" + flags + "] Command-Code: " + message.getCommandCode() + " / E2E(" 
          + message.getEndToEndIdentifier() + ") / HbH(" + message.getHopByHopIdentifier() + ")");
      logger.info("- - - - - - - - - - - - - - - - AVPs - - - - - - - - - - - - - - - -");
      printAvps(message.getAvps());
    }
  }

  public static void printAvps(AvpSet avps) {
    printAvps(avps, "");
  }

  public static void printAvps(AvpSet avps, String indentation) {
    for(Avp avp : avps) {
      AvpRepresentation avpRep = AVP_DICTIONARY.getAvp(avp.getCode(), avp.getVendorId());
      Object avpValue = null;
      boolean isGrouped = false;

      try {
        String avpType = AvpDictionary.INSTANCE.getAvp(avp.getCode(), avp.getVendorId()).getType();

        if("Integer32".equals(avpType) || "AppId".equals(avpType)) {
          avpValue = avp.getInteger32();
        }
        else if("Unsigned32".equals(avpType) || "VendorId".equals(avpType)) {
          avpValue = avp.getUnsigned32();
        }
        else if("Float64".equals(avpType)) {
          avpValue = avp.getFloat64();
        }
        else if("Integer64".equals(avpType)) {
          avpValue = avp.getInteger64();
        }
        else if("Time".equals(avpType)) {
          avpValue = avp.getTime();
        }
        else if("Unsigned64".equals(avpType)) {
          avpValue = avp.getUnsigned64();
        }
        else if("Grouped".equals(avpType)) {
          avpValue = "<Grouped>";
          isGrouped = true;
        }
        else {
          avpValue = new String(avp.getOctetString(), "UTF-8").replaceAll("\r", "").replaceAll("\n", "");
        }
      }
      catch (Exception ignore) {
        try {
          avpValue = new String(avp.getOctetString()).replaceAll("\r", "").replaceAll("\n", "");
        }
        catch (AvpDataException e) {
          avpValue = avp.toString();
        }
      }

      String avpLine = indentation + avp.getCode() + ": " + avpRep.getName();
      while(avpLine.length() < 50) {
        avpLine += avpLine.length() % 2 == 0 ? "." : " ";
      }
      avpLine += avpValue;

      logger.info(avpLine);

      if(isGrouped) {
        try {
          printAvps(avp.getGrouped(), indentation + "  ");          
        }
        catch (AvpDataException e) {
          // Failed to ungroup... ignore then...
        }
      }
    }
  }
}
