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
public interface ClusteredSipApplicationSessionNotificationPolicy {
	/**
	 * Are invocations of <code>SipApplicationSessionListener</code> callbacks allowed
	 * under the given conditions?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param local
	 *            <code>true</code> if the event driving the notification
	 *            originated on this node; <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the notification is allowed,
	 *         <code>false</code> if not
	 */
	boolean isSipApplicationSessionListenerInvocationAllowed(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, boolean local);

	/**
	 * Under the given conditions, are invocations of
	 * <code>SipApplicationSessionAttributeListener</code> callbacks allowed?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param attributeName
	 *            value that would be passed to the <code>name</code> param of
	 *            the <code>SipApplicationSessionBindingEvent</code> if the listener were
	 *            invoked
	 * @param local
	 *            <code>true</code> if the event driving the notification
	 *            originated on this node; <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the notification is allowed,
	 *         <code>false</code> if not
	 */
	boolean isSipApplicationSessionAttributeListenerInvocationAllowed(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, String attributeName,
			boolean local);

	/**
	 * Under the given conditions, are invocations of
	 * <code>SipApplicationSessionBindingListener</code> callbacks allowed?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param attributeName
	 *            value that would be passed to the <code>name</code> param of
	 *            the <code>SipApplicationSessionBindingEvent</code> if the listener were
	 *            invoked
	 * @param local
	 *            <code>true</code> if the event driving the notification
	 *            originated on this node; <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if the notification is allowed,
	 *         <code>false</code> if not
	 */
	boolean isSipApplicationSessionBindingListenerInvocationAllowed(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, String attributeName,
			boolean local);

	/**
	 * Under the given conditions, are invocations of
	 * <code>SipApplicationSessionActivationListener</code> callbacks allowed?
	 * 
	 * @param status
	 *            the status of the session
	 * @param cause
	 *            the cause of the session notification
	 * @param attributeName
	 *            value that would be passed to the <code>name</code> param of
	 *            the <code>SipApplicationSessionEvent</code> if the listener were invoked
	 * 
	 * @return <code>true</code> if the notification is allowed,
	 *         <code>false</code> if not
	 */
	boolean isSipApplicationSessionActivationListenerInvocationAllowed(
			ClusteredSessionManagementStatus status,
			ClusteredSessionNotificationCause cause, String attributeName);

	/**
	 * Provides the policy information about the container's capabilities with
	 * respect to issuing notifications. Will be invoked by the container before
	 * the first invocation of any of the other methods in this interface.
	 * 
	 * @param capability
	 *            the capability, Will not be <code>null</code>.
	 */
	void setClusteredSipApplicationSessionNotificationCapability(
			ClusteredSipApplicationSessionNotificationCapability capability);
}
