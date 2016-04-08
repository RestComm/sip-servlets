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

import java.text.ParseException;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.address.Hop;
import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.javax.servlet.sip.dns.DNSResolver;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.URIImpl;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsDNSResolver implements DNSResolver {
        private static final Logger logger = Logger.getLogger(MobicentsDNSResolver.class
            .getName());
        
	private DNSServerLocator dnsServerLocator;
        private AddressFactory createAddressFactory;

	public MobicentsDNSResolver(DNSServerLocator dnsServerLocator) {
		this.dnsServerLocator = dnsServerLocator;
                try {
                    createAddressFactory = SipFactory.getInstance().createAddressFactory();
                } catch (Exception e) {
                    logger.warn("Error getting address factory", e);
                }
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.dns.DNSResolver#getSipURI(javax.servlet.sip.URI)
	 */
	public SipURI getSipURI(URI uri) {
		javax.sip.address.SipURI jainSipURI = dnsServerLocator.getSipURI(((URIImpl)uri).getURI());
		SipURI sipURI = new SipURIImpl(jainSipURI, ModifiableRule.NotModifiable);
		return sipURI;
	}

	@Override
	public void setDnsTimeout(int timeout) {
		dnsServerLocator.getDnsLookupPerformer().setDNSTimeout(timeout);
	}

	@Override
	public int getDnsTimeout() {
		return dnsServerLocator.getDnsLookupPerformer().getDNSTimeout();
	}

	@Override
	public Set<String> resolveHost(String host) {
		Set<String> ipAddresses = new CopyOnWriteArraySet<String>();
		Queue<Hop> hops = dnsServerLocator.resolveHostByAandAAAALookup(host, -1, null);
		if(hops != null) {
			for (Hop hop : hops) {
				ipAddresses.add(hop.getHost());
			}
		}
		return ipAddresses;
	}

        /* (non-Javadoc)
         * @see org.mobicents.javax.servlet.sip.dns.DNSResolver#locateURIs(javax.servlet.sip.URI)
         */        
        @Override
        public List<SipURI> locateURIs(SipURI uri) {
            List<SipURI> uris = new CopyOnWriteArrayList();
            if (uri instanceof SipURIImpl && createAddressFactory != null) {
                SipURIImpl uriImpl = (SipURIImpl) uri;
                Queue<Hop> hops = dnsServerLocator.locateHops(uriImpl.getSipURI());
                if(hops != null) {
                    for (Hop hop : hops) {
                        javax.sip.address.SipURI createSipURI;
                        try {
                            //use null as user so this uri may be used potentially
                            //as Route Header
                            createSipURI = createAddressFactory.createSipURI(null, hop.getHost());
                            createSipURI.setPort(hop.getPort());
                            createSipURI.setTransportParam(hop.getTransport());
                            SipURI sipURI = new SipURIImpl(createSipURI, ModifiableRule.NotModifiable);
                            uris.add(sipURI);
                        } catch (ParseException ex) {
                            logger.debug("Error creating SipURI.", ex);
                        }
                    }
                }
            }
            return uris;
        }

}
