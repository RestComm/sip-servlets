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

import java.io.Serializable;

import javax.servlet.sip.SipApplicationSessionActivationListener;
import javax.servlet.sip.SipApplicationSessionEvent;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.notification.SessionActivationNotificationCause;
import org.mobicents.servlet.sip.notification.SipApplicationSessionActivationEvent;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipApplicationSessionActivationListenerAttribute implements
		Serializable, SipApplicationSessionActivationListener {

	public static final String SIP_APPLICATION_SESSION_ACTIVATED = "sipApplicationSessionActivated";
	private static Logger logger = Logger.getLogger(SipApplicationSessionActivationListenerAttribute.class);
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionDidActivate(SipApplicationSessionEvent event) {
		logger.info("Following sip application session just activated " + event.getApplicationSession().getId() + " cause " + ((SipApplicationSessionActivationEvent)event).getCause());
		if(((SipApplicationSessionActivationEvent)event).getCause() == SessionActivationNotificationCause.FAILOVER) {
			event.getApplicationSession().setAttribute(SIP_APPLICATION_SESSION_ACTIVATED, "true" );
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionWillPassivate(SipApplicationSessionEvent event) {
		logger.info("Following sip application session just passivated " + event.getApplicationSession().getId() + " cause " + ((SipApplicationSessionActivationEvent)event).getCause());

	}

}
