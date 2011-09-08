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

package org.mobicents.servlet.sip.startup;

import gov.nist.javax.sip.stack.SIPTransaction;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipService;

/**
 * There can be only one tomcat service per SAR classloader, so it can be a static safely.
 * No need for get/set, because it's set just once on init and read many times after that.
 * This here is used for deserialization of Session references where, we are at not particular app
 * and do not have any context.
 * 
 * @author vralev
 *
 */
public class StaticServiceHolder {
	public static SipService sipStandardService;
	public static Method disableRetransmissionTimer; 
	private static final Logger logger = Logger.getLogger(StaticServiceHolder.class);
	static {
		try {
			disableRetransmissionTimer = SIPTransaction.class.getDeclaredMethod("disableRetransmissionTimer");
		} catch (Exception e) {
			logger.error("Error with reflection for disableRetransmissionTimer", e);
		}
		disableRetransmissionTimer.setAccessible(true);
	}
}
