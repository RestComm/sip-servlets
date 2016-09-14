/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.web.session.sip;

import org.jboss.as.web.session.OutgoingDistributableSessionDataImpl;
import org.jboss.as.clustering.web.sip.DistributableSipApplicationSessionMetadata;
import org.jboss.as.clustering.web.sip.OutgoingDistributableSipApplicationSessionData;

/**
 * @author jean.deruelle@gmail.com
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class OutgoingDistributableSipApplicationSessionDataImpl extends OutgoingDistributableSessionDataImpl implements
		OutgoingDistributableSipApplicationSessionData {
	
	String sipApplicationSessionKey;
	private boolean isSessionMetaDataDirty;
	
	public OutgoingDistributableSipApplicationSessionDataImpl(String realId,
			int version, Long timestamp, String key, DistributableSipApplicationSessionMetadata metadata) {
		super(realId, version, timestamp, metadata);
		this.sipApplicationSessionKey = key;
	}

	public String getSipApplicationSessionKey() {
		return this.sipApplicationSessionKey;
	}
	/**
	 * @param isSessionMetaDataDirty the isSessionMetaDataDirty to set
	 */
	public void setSessionMetaDataDirty(boolean isSessionMetaDataDirty) {
		this.isSessionMetaDataDirty = isSessionMetaDataDirty;
	}

	/**
	 * @return the isSessionMetaDataDirty
	 */
	public boolean isSessionMetaDataDirty() {
		return isSessionMetaDataDirty;
	}
}
