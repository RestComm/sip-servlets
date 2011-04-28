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

package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class MessageSenderUtil {
	
	private static transient Logger logger = Logger.getLogger(MessageSenderUtil.class);
	
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	/**
	 * 
	 * @param request
	 */
	public static void sendMessageHandlerOK(SipSession session, String message) {
		try {
			SipServletRequest responseMessage = session.createRequest("MESSAGE");
			responseMessage.setContentLength(message.length());
			responseMessage.setContent(message, CONTENT_TYPE);
			responseMessage.send();
		} catch (UnsupportedEncodingException e) {
			logger.error("the encoding is not supported", e);
		} catch (IOException e) {
			logger.error("an IO exception occured", e);
		} 
	}	
}
