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
package javax.servlet.sip;
/**
 * 
 */
public class IllegalSessionStateException extends java.lang.IllegalStateException{
	private SipSession sipSession = null;
    /**
     * Constructs an IllegalSessionStateException with no detail message. A detail message is a String that describes this particular exception.
     */
    public IllegalSessionStateException(javax.servlet.sip.SipSession session){
         this.sipSession = session;
    }

    /**
     * Constructs an IllegalSessionStateException with the specified detail message. A detail message is a String that describes this particular exception.
     * @param s the String that contains a detailed message
     */
    public IllegalSessionStateException(java.lang.String s, javax.servlet.sip.SipSession session){
    	super(s);
    	this.sipSession = session;
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).cause - the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public IllegalSessionStateException(java.lang.String message, java.lang.Throwable cause, javax.servlet.sip.SipSession session){
    	super(message, cause);
    	this.sipSession = session;
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for exceptions that are little more than wrappers for other throwables (for example,
     * ).
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public IllegalSessionStateException(java.lang.Throwable cause, javax.servlet.sip.SipSession session){
    	super(cause);
    	this.sipSession = session;
    }

    /**
     * The SipSession associated with this exception.
     */
    public javax.servlet.sip.SipSession getSipSession(){
        return sipSession;
    }

}
