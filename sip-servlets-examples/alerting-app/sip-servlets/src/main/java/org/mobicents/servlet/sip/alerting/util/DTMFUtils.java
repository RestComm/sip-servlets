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

package org.mobicents.servlet.sip.alerting.util;

import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class DTMFUtils {
	
	private static Logger logger = Logger.getLogger(DTMFUtils.class);		

	public static void answerBack(String alertId, String signal, String feedbackUrl) {
		if(signal != null) {
			logger.info("Sending signal " + signal + " for alertId " + alertId + " to the alerting application  on URL " + feedbackUrl);
			String finalUrl = feedbackUrl + "?alertId=" + alertId + "&action=" + signal;
			
			try {
				URL url = new URL(finalUrl);
				InputStream in = url.openConnection().getInputStream();
		
				byte[] buffer = new byte[in.available()];
				int len = in.read(buffer);
				String httpResponse = "";
				for (int q = 0; q < len; q++)
					httpResponse += (char) buffer[q];
				logger.info("Received the follwing HTTP response: " + httpResponse);
			} catch(Exception e) {
				logger.error("couldn't connect to " +  finalUrl + " : " + e.getMessage());
			}
		} else {
			logger.debug("signal is null, not sending anything to feedback URL");
		}
	}
}
