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
 * Thrown by the container when an application attempts to parse a malformed header or addressing structure.
 * See Also:Serialized Form
 */
public class ServletParseException extends javax.servlet.ServletException{
    /**
     * Constructs a new parse exception, without any message.
     */
    public ServletParseException(){
         super();
    }

    /**
     * Constructs a new parse exception with the specified message.
     * @param msg a String specifying the text of the exception message
     */
    public ServletParseException(java.lang.String msg){
    	super(msg);
    }

    /**
     * Constructs a new parse exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).cause - the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)Since: 1.1
     */
    public ServletParseException(java.lang.String message, java.lang.Throwable cause){
         super(message, cause);
    }

    /**
     * Constructs a new parse exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for exceptions that are little more than wrappers for other throwables.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)Since: 1.1
     */
    public ServletParseException(java.lang.Throwable cause){
    	super(cause);
    }

}
