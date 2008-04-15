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
 * Indicates that a provisional response cannot be sent reliably or PRACK was attempted to be created on a non reliable provisional response.
 * This is thrown by the container when an application requested that a provisional response be sent reliably (using the 100rel extension defined in RFC 3262) but one or more of the conditions for using 100rel is not satisfied: the status code of the response is not in the range 101-199 the request was not an INVITE the UAC did not indicate support for the 100rel extension in the request the container doesn't support the 100rel extension This exception is also thrown when SipServletResponse.createPrack() is called for non-reliable provisional response or a final response.
 * The actual reason why SipServletResponse.sendReliably() failed can be discovered through getReason().
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
    public static final int NO_UAC_SUPPORT=2;
    /**
     * Reason code indicating that the container does not support reliable provisional response.
     */
    public static final int NOT_SUPPORTED=3;

    /**
     * Constructs a new Rel100Exception with the specified error reason.
     * @param reason - one of NOT_1XX, NOT_INVITE, NO_UAC_SUPPORT, NOT_SUPPORTED
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
