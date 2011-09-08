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

package org.mobicents.servlet.sip.core.session;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipStandardManagerDelegate extends SipManagerDelegate {

	private static final Logger logger = Logger.getLogger(SipStandardManagerDelegate.class);
	
	@Override
	protected MobicentsSipSession getNewMobicentsSipSession(SipSessionKey key, SipFactoryImpl sipFactoryImpl, MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		if ((maxActiveSipSessions >= 0) && (sipSessions.size() >= maxActiveSipSessions)) {
			rejectedSipSessions++;
            throw new IllegalStateException
                ("could not create a new sip session because there is currently too many active sip sessions");
		}
		sipSessionCounter++;		
		return new SipSessionImpl(key, sipFactoryImpl, mobicentsSipApplicationSession);
	}

	@Override
	protected MobicentsSipApplicationSession getNewMobicentsSipApplicationSession(
			SipApplicationSessionKey key, SipContext sipContext) {
		if ((maxActiveSipApplicationSessions >= 0) && (sipApplicationSessions.size() >= maxActiveSipApplicationSessions)) {
			rejectedSipApplicationSessions++;
            throw new IllegalStateException
                ("could not create a new sip application session because there is currently too many active sip application sessions");
		}
		sipApplicationSessionCounter++;		
		MobicentsSipApplicationSession sipApplicationSession = new SipApplicationSessionImpl(key, sipContext);		
		
		return sipApplicationSession;
	}

}
