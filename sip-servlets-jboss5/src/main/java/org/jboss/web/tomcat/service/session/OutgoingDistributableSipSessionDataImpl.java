/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.tomcat.service.session;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSipSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipSessionData;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class OutgoingDistributableSipSessionDataImpl extends OutgoingDistributableSessionDataImpl implements
		OutgoingDistributableSipSessionData {
	SipApplicationSessionKey sipApplicationSessionKey;
	SipSessionKey sipSessionKey;
	
	public OutgoingDistributableSipSessionDataImpl(String realId,
			int version, Long timestamp, SipApplicationSessionKey sipApplicationSessionKey, SipSessionKey sipSessionKey, DistributableSipSessionMetadata metadata) {
		super(realId, version, timestamp, metadata);
		this.sipApplicationSessionKey = sipApplicationSessionKey;
		this.sipSessionKey = sipSessionKey;
	}

	public SipApplicationSessionKey getSipApplicationSessionKey() {
		return this.sipApplicationSessionKey;
	}

	public SipSessionKey getSipSessionKey() {
		return this.sipSessionKey;
	}
}
