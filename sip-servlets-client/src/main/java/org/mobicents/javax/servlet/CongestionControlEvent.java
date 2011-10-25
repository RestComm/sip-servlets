/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
 * Defines a Congestion Control Event when the congestion control is triggered by the Container
 * 
 * It allows to get the Reason whether the memory usage or CPU usage or any other condition in the future has triggered the callback
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class CongestionControlEvent {
	public enum Reason {
		Memory, Queue //, TODO add CPU Usage congestion control
	}

	Reason reason;
	String message;
	
	public CongestionControlEvent(Reason reason, String message) {
		this.reason = reason;
		this.message = message;
	}
	
	/**
	 * Gives the reason on whether the memory usage or CPU usage or any other condition in the future has triggered the callback
	 * @return the reason on whether the memory usage or CPU usage or any other condition in the future has triggered the callback
	 */
	public Reason getReason() {
		return reason;
	}
	
	/**
	 * Gives the reason message on what exactly triggered the callback
	 * @return the reason message on what exactly triggered the callback
	 */
	public String getMessage() {
		return message;
	}
}
