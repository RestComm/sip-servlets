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

package org.mobicents.slee.service.events;

import java.io.Serializable;

/**
 * CustomEvent to communicate between SBB Entities belonging to different
 * Services
 * 
 * @author amit bhayani
 * 
 */
public class InteropCustomEvent implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private byte[] sdpContent;	
	
	private String boothNumber;
	
	private Object callManagerRef;

	public InteropCustomEvent(String boothNumber, byte[] sdpContent, Object callManagerRef) {
		this.boothNumber = boothNumber;
		this.sdpContent = sdpContent;
		this.callManagerRef = callManagerRef;
	}	

	public byte[] getSdpContent() {
		return sdpContent;
	}
	
	
	public Object clone() {
		InteropCustomEvent clonedCustomEvent = new InteropCustomEvent(this.getBoothNumber(), this.getSdpContent(), this.getCallManagerRef());
		return clonedCustomEvent;
	}	

	/**
	 * @return the callManagerRef
	 */
	public Object getCallManagerRef() {
		return callManagerRef;
	}

	/**
	 * @return the boothNumber
	 */
	public String getBoothNumber() {
		return boothNumber;
	}

}
