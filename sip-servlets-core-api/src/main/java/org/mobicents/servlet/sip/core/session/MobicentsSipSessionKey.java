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

package org.mobicents.servlet.sip.core.session;

/**
 * <p>
 * Class representing the key (which will also be its id) for a sip session.<br/>
 * It is composed of the From Header parameter Tag, the To Header parameter tag, the Call-Id, the app session id and the application Name.
 * </p>
 * IT is maaped to the SIP Dialog from RFC3261 (from tag, to tag + call-ID)
 * <p>
 * It is to be noted that the To Header parameter Tag will not be used in SipSessionKey comparison (equals() and hashcode() methods).<br/>
 * It will only be used to check if a new derived sip session needs to be created.
 * </p>
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsSipSessionKey {

	String getApplicationName();
	String getApplicationSessionId();
	String getToTag();
	void setToTag(String currentKeyToTag, boolean b);
	String getCallId();
	String getFromTag();

}
