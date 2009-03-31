/*
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

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipApplicationSessionActivationListenerAttribute implements
		Serializable, SipApplicationSessionActivationListener {

	private static Logger logger = Logger.getLogger(SipApplicationSessionActivationListenerAttribute.class);
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionDidActivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionDidActivate(SipApplicationSessionEvent event) {
		logger.info("Following sip application session just activated " + event.getApplicationSession().getId());

	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSessionActivationListener#sessionWillPassivate(javax.servlet.sip.SipApplicationSessionEvent)
	 */
	public void sessionWillPassivate(SipApplicationSessionEvent event) {
		logger.info("Following sip application session just passivated " + event.getApplicationSession().getId());

	}

}
