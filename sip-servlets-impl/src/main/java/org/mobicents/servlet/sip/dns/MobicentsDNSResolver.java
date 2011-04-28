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

package org.mobicents.servlet.sip.dns;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.javax.servlet.sip.dns.DNSResolver;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsDNSResolver implements DNSResolver {

	private DNSServerLocator dnsServerLocator;

	public MobicentsDNSResolver(DNSServerLocator dnsServerLocator) {
		this.dnsServerLocator = dnsServerLocator;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.dns.DNSResolver#getSipURI(javax.servlet.sip.URI)
	 */
	public SipURI getSipURI(URI uri) {
		javax.sip.address.SipURI jainSipURI = dnsServerLocator.getSipURI(((URIImpl)uri).getURI());
		SipURI sipURI = new SipURIImpl(jainSipURI, ModifiableRule.NotModifiable);
		return sipURI;
	}

}
