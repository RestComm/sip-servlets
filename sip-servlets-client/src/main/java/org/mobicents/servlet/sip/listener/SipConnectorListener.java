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

package org.mobicents.servlet.sip.listener;

import java.util.EventListener;

import org.mobicents.servlet.sip.SipConnector;

/**
 * Interface to implement by the class to be notified of sip connector management events.
 * 
 * @apiviz.uses org.mobicents.servlet.sip.SipConnector
 * @author jean.deruelle@gmail.com
 *
 */
public interface SipConnectorListener extends EventListener {
	/**
	 * a Sip Connector has been added
	 * @param connector the connector that has just been added
	 */
	void sipConnectorAdded(SipConnector connector);
	/**
	 * a Sip Connector has been removed
	 * @param connector the connector that has just been removed
	 */
	void sipConnectorRemoved(SipConnector connector);
	/**
	 * if a RFC 5626 KeepAlive timeout has fired which gives the connector on which it happened
	 * and the peer information involved.
	 * @param connector the connector on which the timeout happened
	 * @param peerAddress the peer address which didn't send the needed RFC5626 keepalive
	 * @param peerPort the peer port which didn't send the needed RFC5626 keepalive
	 */
    void onKeepAliveTimeout(SipConnector connector, String peerAddress, int peerPort);
}
