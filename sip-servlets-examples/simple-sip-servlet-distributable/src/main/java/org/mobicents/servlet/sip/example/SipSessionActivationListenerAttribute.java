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

import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionEvent;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.notification.SessionActivationNotificationCause;
import org.mobicents.servlet.sip.notification.SipSessionActivationEvent;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipSessionActivationListenerAttribute implements Serializable,
		SipSessionActivationListener {

	public static final String SIP_SESSION_ACTIVATED = "sipSessionActivated";
	private static Logger logger = Logger.getLogger(SipSessionActivationListenerAttribute.class);
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionDidActivate(SipSessionEvent event) {
		logger.info("Following sip session just activated " + event.getSession().getId() + " cause " + ((SipSessionActivationEvent)event).getCause());
		if(((SipSessionActivationEvent)event).getCause() == SessionActivationNotificationCause.FAILOVER) {
			event.getSession().setAttribute(SIP_SESSION_ACTIVATED, "true" );
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipSessionEvent)
	 */
	public void sessionWillPassivate(SipSessionEvent event) {
		logger.info("Following sip session just passivated " + event.getSession().getId() + " cause " + ((SipSessionActivationEvent)event).getCause());
	}

}
