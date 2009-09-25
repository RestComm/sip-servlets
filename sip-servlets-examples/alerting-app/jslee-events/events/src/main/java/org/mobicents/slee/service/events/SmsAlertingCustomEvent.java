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
 * @author jean.deruelle@gmail.com
 * 
 */
public class SmsAlertingCustomEvent implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String alertText;	
	
	private String alertId;
	
	private String tel;

	public SmsAlertingCustomEvent(String alertText, String alertId, String tel) {
		this.alertText = alertText;
		this.alertId = alertId;
		this.tel = tel;
	}	
	
	public Object clone() {
		SmsAlertingCustomEvent clonedCustomEvent = new SmsAlertingCustomEvent(this.getAlertText(), this.getAlertId(), this.getTel());
		return clonedCustomEvent;
	}

	/**
	 * @return the alertText
	 */
	public String getAlertText() {
		return alertText;
	}

	/**
	 * @return the alertId
	 */
	public String getAlertId() {
		return alertId;
	}

	/**
	 * @return the tel
	 */
	public String getTel() {
		return tel;
	}	
}
