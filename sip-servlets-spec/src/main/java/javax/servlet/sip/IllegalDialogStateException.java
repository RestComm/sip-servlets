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
package javax.servlet.sip;
/**
 * Exception thrown if an attempt has been made to invalidate a SipSession while in the EARLY or CONFIRMED state.
 * @since 1.1 
 */
public class IllegalDialogStateException extends javax.servlet.sip.IllegalSessionStateException{
    /**
     * Constructs an IllegalDialogStateException with no detail message. A detail message is a String that describes this particular exception.
     * Parameters:session - the session which is causing this exception to be thrown
     */
    public IllegalDialogStateException(javax.servlet.sip.SipSession session){
         super(session);
    }

    /**
     * Constructs an IllegalDialogStateException with the specified detail message. A detail message is a String that describes this particular exception.
     * Parameters:s - the String that contains a detailed messagesession - the session which is causing this exception to be thrown
     */
    public IllegalDialogStateException(java.lang.String s, javax.servlet.sip.SipSession session){
         super(s, session);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     * Parameters:message - the detail message (which is saved for later retrieval by the Throwable.getMessage() method).cause - the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)session - the session which is causing this exception to be thrown
     */
    public IllegalDialogStateException(java.lang.String message, java.lang.Throwable cause, javax.servlet.sip.SipSession session){
         super(message, cause, session);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for exceptions that are little more than wrappers for other throwables.
     * Parameters:cause - the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)session - the session which is causing this exception to be thrown
     */
    public IllegalDialogStateException(java.lang.Throwable cause, javax.servlet.sip.SipSession session){
    	super(cause, session);
    }

}
