/*
 * ***************************************************
 *                                                 *
 *  Mobicents: The Open Source JSLEE Platform      *
 *                                                 *
 *  Distributable under LGPL license.              *
 *  See terms of license at gnu.org.               *
 *                                                 *
 ***************************************************
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
