/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.tomcat.service.session.notification;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class ClusteredSipApplicationSessionNotificationCapability {

	/**
	 * Does the container support invoking <code>SipApplicationSessionListener</code>
	 * callbacks under the given conditions?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param local
	 *            <code>true</code> if the event driving the notification
	 *            originated on this node; <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the notification is supported,
	 *         <code>false</code> if not
	 */
	public boolean isSipApplicationSessionListenerInvocationSupported(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, boolean local) {
		return local
				&& status.isLocallyUsed()
				&& !ClusteredSessionNotificationCause.STATE_TRANSFER
						.equals(cause);
	}

	/**
	 * Under the given conditions, does the container support invoking
	 * <code>SipApplicationSessionAttributeListener</code> callbacks?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param local
	 *            <code>true</code> if the event driving the notification
	 *            originated on this node; <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the notification is supported,
	 *         <code>false</code> if not
	 */
	public boolean isSipApplicationSessionAttributeListenerInvocationSupported(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, boolean local) {
		return local
				&& status.isLocallyUsed()
				&& !ClusteredSessionNotificationCause.STATE_TRANSFER
						.equals(cause);
	}

	/**
	 * Under the given conditions, does the container support invoking
	 * <code>SipApplicationSessionBindingListener</code> callbacks?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param local
	 *            <code>true</code> if the event driving the notification
	 *            originated on this node; <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the notification is supported,
	 *         <code>false</code> if not
	 */
	public boolean isSipApplicationSessionBindingListenerInvocationSupported(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, boolean local) {
		return local
				&& status.isLocallyUsed()
				&& !ClusteredSessionNotificationCause.STATE_TRANSFER
						.equals(cause);
	}

	/**
	 * Is the container able to distinguish whether a session that has been
	 * {@link ClusteredSessionManagementStatus#isLocallyUsed() locally used} is
	 * also {@link ClusteredSessionManagementStatus#getLocallyActive() locally
	 * active}?
	 * 
	 * @return <code>true</code> if the container is able to make this
	 *         distinction; <code>false</code> if not
	 */
	public boolean isLocallyActiveAware() {
		return false;
	}

	/**
	 * Is the container able to distinguish whether a session is
	 * {@link ClusteredSessionManagementStatus#getLocallyOwned() locally owned}?
	 * 
	 * @return <code>true</code> if the container is able to make this
	 *         distinction; <code>false</code> if not
	 */
	public boolean isLocallyOwnedAware() {
		return false;
	}

	/**
	 * Returns whether the local container is aware of events on remote nodes
	 * that could give rise to notifications.
	 * 
	 * @param cause
	 *            the cause
	 * @return <code>true</code> if the local container is aware of the remote
	 *         event, <code>false</code> if not.
	 */
	public boolean isRemoteCauseAware(ClusteredSessionNotificationCause cause) {
		return ClusteredSessionNotificationCause.CREATE.equals(cause)
				|| ClusteredSessionNotificationCause.MODIFY.equals(cause)
				|| ClusteredSessionNotificationCause.INVALIDATE.equals(cause);
	}
}
