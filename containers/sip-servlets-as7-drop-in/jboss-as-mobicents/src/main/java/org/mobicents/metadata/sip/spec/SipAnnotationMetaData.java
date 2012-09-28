/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.metadata.sip.spec;

import java.util.HashMap;

import org.jboss.as.server.deployment.AttachmentKey;

/**
 *
 * @author josemrecio@gmail.com
 *
 */
public class SipAnnotationMetaData extends HashMap<String, SipMetaData> {
    private static final long serialVersionUID = 1L;

    public static final AttachmentKey<SipAnnotationMetaData> ATTACHMENT_KEY = AttachmentKey.create(SipAnnotationMetaData.class);

    // http://code.google.com/p/sipservlets/issues/detail?id=168
	// When no sip.xml but annotations only, Application is not recognized as SIP App by AS7
    private boolean sipApplicationAnnotationPresent = false;

	/**
	 * @return the sipApplicationAnnotationPresent
	 */
	public boolean isSipApplicationAnnotationPresent() {
		return sipApplicationAnnotationPresent;
	}

	/**
	 * @param sipApplicationAnnotationPresent the sipApplicationAnnotationPresent to set
	 */
	public void setSipApplicationAnnotationPresent(
			boolean sipApplicationAnnotationPresent) {
		this.sipApplicationAnnotationPresent = sipApplicationAnnotationPresent;
	}
    
    
}
