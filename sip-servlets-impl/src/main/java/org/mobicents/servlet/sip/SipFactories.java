/*
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
package org.mobicents.servlet.sip;

import gov.nist.javax.sip.header.HeaderFactoryImpl;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.apache.log4j.Logger;

public class SipFactories {
	private static transient Logger logger = Logger.getLogger(SipFactories.class.getCanonicalName());

	private static boolean initialized;

	public static AddressFactory addressFactory;

	public static HeaderFactory headerFactory;

	public static SipFactory sipFactory;

	public static MessageFactory messageFactory;

	public static void initialize(String pathName, boolean prettyEncoding) {
		if (!initialized) {
			try {
				System.setProperty("gov.nist.core.STRIP_ADDR_SCOPES", "true");
				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName(pathName);
				addressFactory = sipFactory.createAddressFactory();				
				headerFactory = sipFactory.createHeaderFactory();
				if(prettyEncoding) {
					((HeaderFactoryImpl)headerFactory).setPrettyEncoding(prettyEncoding);
				}
				messageFactory = sipFactory.createMessageFactory();
				initialized = true;
			} catch (Exception ex) {
				logger.error("Could not instantiate factories -- exitting", ex);
				throw new RuntimeException("Cannot instantiate factories ", ex);
			}
		}
	}
}
