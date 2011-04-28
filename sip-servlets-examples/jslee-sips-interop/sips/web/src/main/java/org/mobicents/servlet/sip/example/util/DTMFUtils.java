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

package org.mobicents.servlet.sip.example.util;

import javax.servlet.sip.SipSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class DTMFUtils {
	
	private static Log logger = LogFactory.getLog(DTMFUtils.class);
		
	public static boolean updateBoothNumber(SipSession session, String signal) {
		int cause = -1;
		
		try {
			cause = Integer.parseInt(signal);
		} catch (java.lang.NumberFormatException e) {
			//user entered a # sign 
			logger.info("Booth Number is = " + (String) session.getAttribute("boothNumber"));
			return true;
		}

		synchronized(session) {
			String boothNumber = (String) session.getAttribute("boothNumber");
			if(boothNumber == null) {
				boothNumber = "";
			}
	
			switch (cause) {
				case 0:
					boothNumber = boothNumber + "0";
					break;
				case 1:
					boothNumber = boothNumber + "1";
					break;
				case 2:
					boothNumber = boothNumber + "2";
					break;
				case 3:
					boothNumber = boothNumber + "3";
					break;
				case 4:
					boothNumber = boothNumber + "4";
					break;
				case 5:
					boothNumber = boothNumber + "5";
					break;
				case 6:
					boothNumber = boothNumber + "6";
					break;
				case 7:
					boothNumber = boothNumber + "7";
					break;
				case 8:
					boothNumber = boothNumber + "8";
					break;
				case 9:
					boothNumber = boothNumber + "9";
					break;
				default:
					break;
			}
				
			session.setAttribute("boothNumber", boothNumber);
		}	
		return false;
	}		
}
