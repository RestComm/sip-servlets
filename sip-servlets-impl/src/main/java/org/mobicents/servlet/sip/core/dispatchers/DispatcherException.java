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

import javax.sip.message.Response;

/**
 * This exception is thrown when a problem occurs in the Dispatching of tha SIP message
 * It contains the error code to return in response to the sip message.
 *  
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class DispatcherException extends Exception {
	int errorCode = Response.SERVER_INTERNAL_ERROR;
	/**
	 * 
	 */
	public DispatcherException(String message) {	
		super(message);
	}
	
	/**
	 * 
	 */
	public DispatcherException(Throwable cause) {	
		super(cause);
	} 
	
	/**
	 * @param message
	 * @param cause
	 */
	public DispatcherException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * 
	 */
	public DispatcherException(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @param message
	 */
	public DispatcherException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * @param cause
	 */
	public DispatcherException(int errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DispatcherException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

}
