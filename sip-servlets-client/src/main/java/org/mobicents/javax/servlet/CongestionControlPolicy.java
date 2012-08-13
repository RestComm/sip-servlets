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

package org.mobicents.javax.servlet;

/**
 * Congestion Control Policy for the Mobicents Sip Servlets Server. <br/>
 * 
 * The congestion control policy defines how an incoming message is handled when the server is overloaded. The following parameters are configurable :
 * 
 * <ul>
 * <li>DropMessage - drop any incoming message</li>
 * <li>ErrorResponse - send a 503 - Service Unavailable response to any incoming request (Default).</li>
 * </ul>
 * @author jean.deruelle@gmail.com
 *
 */
public enum CongestionControlPolicy {
	ErrorResponse,
	DropMessage
}
