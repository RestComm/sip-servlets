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

package org.mobicents.servlet.sip.core;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipContextEventImpl implements SipContextEvent {
	
	SipContextEventType eventType;
	Object eventObject;
	
	/**
	 * @param eventType
	 * @param eventObject
	 */
	public SipContextEventImpl(SipContextEventType eventType, Object eventObject) {
		super();
		this.eventType = eventType;
		this.eventObject = eventObject;
	}

	/**
	 * @return the eventType
	 */
	public SipContextEventType getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(SipContextEventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the eventObject
	 */
	public Object getEventObject() {
		return eventObject;
	}

	/**
	 * @param eventObject the eventObject to set
	 */
	public void setEventObject(Object eventObject) {
		this.eventObject = eventObject;
	}	
}
