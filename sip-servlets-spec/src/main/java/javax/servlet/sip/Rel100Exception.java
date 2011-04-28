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
 * Indicates that a provisional response cannot be sent reliably or PRACK was attempted 
 * to be created on a non reliable provisional response.
 * This is thrown by the container when an application requested that a provisional response be sent reliably 
 * (using the 100rel extension defined in RFC 3262) but one or more of the conditions for using 100rel is not satisfied: 
 * the status code of the response is not in the range 101-199 
 * the request was not an INVITE the UAC did not indicate support for the 100rel extension 
 * in the request the container doesn't support the 100rel extension 
 * This exception is also thrown when SipServletResponse.createPrack() is called for 
 * non-reliable provisional response or a final response or if the original request was not an INVITE. .
 * The actual reason why SipServletResponse.sendReliably() or SipServletResponse.createPrack() failed can be discovered through getReason(). 
 */
public class Rel100Exception extends javax.servlet.ServletException{
	private int reason;

    /**
     * Reason code indicating that
     * was invoked on a final or a 100 response.
     */
    public static final int NOT_1XX=0;
    /**
     * Reason code indicating that
     * was invoked for a response to a non-INVITE request.
     */
    public static final int NOT_INVITE=1;
    /**
     * Reason code indicating that the UAC didn't indicate support for the reliable responses extension in the request.
     */
    public static final int NO_REQ_SUPPORT=2;
    /**
     * Reason code indicating that the container does not support reliable provisional response.
     */
    public static final int NOT_SUPPORTED=3;
    /**
     * Reason code indicating that SipServletResponse.createPrack()  was invoked on a provisional response that is not reliable. 
     */
    public static final int NOT_100rel=4;

    /**
     * Constructs a new Rel100Exception with the specified error reason.
     * @param reason - one of NOT_1XX, NOT_INVITE, NO_REQ_SUPPORT, NOT_SUPPORTED, NOT_100rel 
     */
    public Rel100Exception(int reason){
         this.reason = reason;
    }

    /**
     * Returns message phrase suitable for the reason integer code.
     */
    public java.lang.String getMessage(){
    	switch (reason) {
		case 0:
			return "Response not a non-100 1xx";

		case 1:
			return "Response is not for an INVITE";

		case 2:
			return "The UAC didn't indicate support for 100rel in the request";

		case 3: 
			return "100rel not supported by the container";
			
		case 4:
			return "SipServletResponse.createPrack()  was invoked on a provisional response that is not reliable.";
		}
		return "Failed to send response reliably";
    }

    /**
     * Returns an integer code indicating the specific reason why this exception was thrown.
     */
    public int getReason(){
        return reason; 
    }

}
