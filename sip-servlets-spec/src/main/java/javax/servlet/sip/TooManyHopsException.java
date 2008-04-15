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
 * Thrown when a SIP Servlet application attempts to obtain a Proxy object for a request with a Max-Forwards header with value 0.
 * The application may catch this exception and generate its own response. Otherwise the exception will propagate to the container which will catch it and generate a 483 (Too many hops) response.
 */
public class TooManyHopsException extends javax.servlet.ServletException{
    /**
     * Constructs a new TooManyHopsException exception, without any message.
     */
    public TooManyHopsException(){
         super();
    }

    /**
     * Constructs a new TooManyHopsException exception with the specified message.
     * @param msg a String specifying the text of the exception message
     */
    public TooManyHopsException(java.lang.String msg){
         super(msg);
    }

    /**
     * Constructs a new TooManyHopsException exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)Since: 1.1
     */
    public TooManyHopsException(java.lang.String message, java.lang.Throwable cause){
         super(message, cause);
    }

    /**
     * Constructs a new TooManyHopsException exception with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for exceptions that are little more than wrappers for other throwables.
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)Since: 1.1
     */
    public TooManyHopsException(java.lang.Throwable cause){
    	super(cause);
    }

}
