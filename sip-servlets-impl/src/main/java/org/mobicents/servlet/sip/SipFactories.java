/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip;

import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SipFactories {
	private static Log logger = LogFactory.getLog(SipFactories.class.getCanonicalName());

	private static boolean initialized;

	public static AddressFactory addressFactory;

	public static HeaderFactory headerFactory;

	public static SipFactory sipFactory;

	public static MessageFactory messageFactory;

	public static void initialize(String pathName) {
		if (!initialized) {
			try {
				sipFactory = SipFactory.getInstance();
				sipFactory.setPathName(pathName);
				addressFactory = sipFactory.createAddressFactory();
				headerFactory = sipFactory.createHeaderFactory();
				messageFactory = sipFactory.createMessageFactory();
				initialized = true;
			} catch (Exception ex) {
				logger.error("Could not instantiate factories -- exitting", ex);
				throw new RuntimeException("Cannot instantiate factories ", ex);
			}
		}
	}
}
