/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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

package org.jboss.as.web.session.notification.sip;

import org.jboss.as.web.session.notification.ClusteredSessionManagementStatus;
import org.jboss.as.web.session.notification.ClusteredSessionNotificationCause;

/**
 * @author jean.deruelle@gmail.com
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class IgnoreUndeployLegacyClusteredSipSessionNotificationPolicy extends
		LegacyClusteredSipSessionNotificationPolicy {
	/**
	 * Overrides superclass to return <code>false</code> if the cause of the
	 * notification is {@link ClusteredSessionNotificationCause.UNDEPLOY}.
	 * 
	 * @return <code>true</code> if <code>status.isLocallyUsed()</code> is
	 *         <code>true</code> and the cause of the notification is not
	 *         {@link ClusteredSessionNotificationCause.UNDEPLOY}.
	 */
	public boolean isSipSessionAttributeListenerInvocationAllowed(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, String attributeName,
			boolean local) {
		return !ClusteredSessionNotificationCause.UNDEPLOY.equals(cause)
				&& super.isSipSessionAttributeListenerInvocationAllowed(
						status, cause, attributeName, local);
	}

	/**
	 * Overrides superclass to return <code>false</code> if the cause of the
	 * notification is {@link ClusteredSessionNotificationCause.UNDEPLOY}.
	 * 
	 * @return <code>true</code> if <code>status.isLocallyUsed()</code> is
	 *         <code>true</code> and the cause of the notification is not
	 *         {@link ClusteredSessionNotificationCause.UNDEPLOY}.
	 */
	public boolean isSipSessionListenerInvocationAllowed(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, boolean local) {
		return !ClusteredSessionNotificationCause.UNDEPLOY.equals(cause)
				&& super.isSipSessionListenerInvocationAllowed(status, cause,
						local);
	}
}
