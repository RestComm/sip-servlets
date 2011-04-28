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

package org.mobicents.javax.servlet.sip.dns;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

/**
 * Allows for an application to perform DNS queries to modify the SIP Message before it is sent out.<br/>
 * To get the DNSResolver from your application just use 
 * <pre>
 * DNSResolver dnsResolver = (DNSResolver) getServletContext().getAttribute("org.mobicents.servlet.sip.DNS_RESOLVER");
 * </pre>
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface DNSResolver {

	/**
	 * <p>From the uri passed in parameter, try to find the corresponding SipURI.
	 * If the uri in parameter is already a SipURI without a user=phone param, it is just returned
	 * If the uri in parameter is a TelURL or SipURI with a user=phone param, the phone number is converted to a domain name
	 * then a corresponding NAPTR DNS lookup is done to find the SipURI</p>
	 * 
	 * <p> Usage Example </p>
	 * <pre>
	 * 	DNSResolver dnsResolver = (DNSResolver) getServletContext().getAttribute("org.mobicents.servlet.sip.DNS_RESOLVER");
	 * try {
	 *		URI uri = sipFactory.createURI("tel:+358-555-1234567");
	 *		SipURI sipURI = dnsResolver.getSipURI(uri);
	 * } catch (ServletParseException e) {
	 * 		logger.error("Impossible to create the tel URL", e);
	 * }
	 * </pre>
	 * 
	 * @param uri the uri used to find the corresponding SipURI
	 * @return the SipURI found through ENUM methods or the uri itself if the uri is already a SipURI without a user=phone param
	 */
	SipURI getSipURI(URI uri);
}
