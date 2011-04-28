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

package javax.servlet.sip;

/**
 * <p>Represents SIP addresses as found, for example, in From, To, and
 * Contact headers. Applications use addresses when sending requests
 * as a user agent client (UAC) and when redirecting an incoming
 * request.</p>
 * 
 * <p>Addresses appear in a number of SIP headers and generally adhere
 * to the grammar (constituent non-terminals are defined in the SIP
 * specification, RFC 3261):
 * <pre>(name-addr / addr-spec) *(SEMI generic-param) </pre>
 * 
 * that is to say, Addresses consist of a URI, an optional display name,
 * and a set of name-value parameters.
 * 
 * <p>The <code>Address</code> interface is used to represent the value of 
 * all headers defined to contain one or more addresses as defined above.
 * Apart from From, To, and Contact, this includes
 * Route, Record-Route, Reply-To, Alert-Info, Call-Info, Error-Info,
 *  as well as extension headers like P-Asserted-Identity,
 *  P-Preferred-Identity, and Path.</p>
 *  
 *  <p><code>Address</code> objects can be constructed using one of the
 *  <code>SipFactory.createAddress</code> methods and can be obtained
 *  from messages using  {@link#SipServletMessage.getAddressHeader(java.lang.String)} and
 *  {@lin#}SipServletMessage.getAddressHeaders(java.lang.String)}.</p>
 */
public interface Address extends javax.servlet.sip.Parameterable {
	/**
	 * Returns a clone of this Address. The cloned Address has identical display
	 * name, URI, and parameters, except that it has no tag parameter. This
	 * means the cloned address can be used as an argument to {@link#SipFactory.createRequest}.
	 * 
	 * @return a clone of this Address
	 */
	java.lang.Object clone();

	/**
	 * Compares the given Address with this one. 
	 * <p>As Addresses consist of a URI, an optional display name, and an optional set
	 * of name-value parameters, the following rules should be used for
	 * comparing them:
	 * <ol>
	 * 	<li> URI (SipURI, TelURL, etc.) within the Address should be compared based on their respective equals() methods.
	 * 	<li> Address parameters should be compared in the same way as URI parameters 
	 *  (as specified in RFC 3261 Section 19.1.4) with no restrictions for well-known URI params.
	 *  <li> display-names should be ignored in the comparison.
	 * </ol>
	 * 
	 * @param o given Parameterable to be compared with this.
	 * @return true if the two Addresses are equal.
	 */
	boolean equals(java.lang.Object o);

	/**
	 * Returns the display name of this Address. This is typically a caller or
	 * callees real name and may be rendered by a user agent, for example when
	 * alerting.
	 * 
	 * @return display name of this Address, or null if one doesn't exist
	 */
	java.lang.String getDisplayName();

	/**
	 * Returns the value of the "expires" parameter as delta-seconds.
	 * 
	 * @return value of "expires" parameter measured in delta-seconds, or -1 if the parameter does not exist
	 */
	int getExpires();

	/**
	 * Returns the value of the "q" parameter of this Address. The "qvalue"
	 * indicates the relative preference amongst a set of locations. "qvalue"
	 * values are decimal numbers from 0 to 1, with higher values indicating
	 * higher preference.
	 * 
	 * @return this Address' qvalue or -1.0 if this is not set
	 */
	float getQ();

	/**
	 * Returns the URI component of this Address. This method will return null
	 * for wildcard addresses (@see#isWildCard . For non-wildcard addresses the result will
	 * always be non-null.
	 * 
	 * @return the URI of this Address
	 */
	javax.servlet.sip.URI getURI();

	/**
	 * Returns true if this Address represents the "wildcard" contact address.
	 * This is the case if it represents a Contact header whose string value is
	 * "*". Likewise @see#SipFactory.createAddress("*"), always returns a wildcard Address instance.
	 * 
	 * @return true if this Address represents the "wildcard" contact address, and false otherwise
	 */
	boolean isWildcard();

	/**
	 * Sets the display name of this Address.
	 * 
	 * @param name display name
	 * @throws IllegalStateException if this Address is used in a context where it cannot be modified
	 */
	void setDisplayName(java.lang.String name);

	/**
	 * Sets the value of the "expires" parameter.
	 * 
	 * @param seconds new relative value of the "expires" parameter. A negative value causes the "expires" parameter to be removed.
	 */
	void setExpires(int seconds);

	/**
	 * Sets this Addresss qvalue.
	 * 
	 * @param q new qvalue for this Address or -1 to remove the qvalue
	 * @throws IllegalArgumentException if the new qvalue isn't between 0.0 and 1.0 (inclusive) and isn't -1.0.
	 */
	void setQ(float q);

	/**
	 * Sets the URI of this Address.
	 * @param uri new URI of this Address
	 *  
	 * @throws IllegalStateException if this Address is used in a context where it cannot be modified
	 * @throws NullPointerException on null uri.
	 */
	void setURI(javax.servlet.sip.URI uri);

	/**
	 * Returns the value of this address as a String. The resulting string must
	 * be a valid value of a SIP From or To header.
	 * 
	 * @return value of this Address as a String
	 */
	java.lang.String toString();

}
