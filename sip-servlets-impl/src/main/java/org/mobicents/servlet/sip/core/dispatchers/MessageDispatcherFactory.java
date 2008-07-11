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
package org.mobicents.servlet.sip.core.dispatchers;

import javax.sip.message.Request;

import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

/**
 * This class is responsible for choosing the correct message dispatcher to 
 * dispatch a message to an application 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class MessageDispatcherFactory {

	public static RequestDispatcher getRequestDispatcher(SipServletRequestImpl sipServletRequest, SipApplicationDispatcherImpl sipApplicationDispatcher) {
		if(sipServletRequest.isInitial()) {
			return new InitialRequestDispatcher(sipApplicationDispatcher);										
		} else {			
			if(sipServletRequest.getMethod().equals(Request.CANCEL)) {
				return new CancelRequestDispatcher(sipApplicationDispatcher);
			} else {
				return new SubsequentRequestDispatcher(sipApplicationDispatcher);
			}
		}
	}
	
	public static ResponseDispatcher getResponseDispatcher(SipServletResponseImpl sipServletResponse, SipApplicationDispatcherImpl sipApplicationDispatcher) {
		return new ResponseDispatcher(sipApplicationDispatcher);
	}
}
