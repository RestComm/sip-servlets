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
 * Events of this type are sent to objects implementing the SipErrorListener interface when an error occurs which is related to the applications processing of a SIP transaction.
 */
public class SipErrorEvent extends java.util.EventObject{
	private SipServletRequest sipServletRequest = null;
	private SipServletResponse sipServletResponse = null;
    /**
     * Constructs a new SipErrorEvent.
     * @param request the request the error relates toresponse - the response the error relates to
     */
    public SipErrorEvent(javax.servlet.sip.SipServletRequest request, javax.servlet.sip.SipServletResponse response){
    	super(request);
        this.sipServletRequest = request;
        this.sipServletResponse = response;
    }

    /**
     * Returns the request object associated with this SipErrorEvent.
     */
    public javax.servlet.sip.SipServletRequest getRequest(){
        return sipServletRequest; 
    }

    /**
     * Returns the response object associated with this SipErrorEvent.
     */
    public javax.servlet.sip.SipServletResponse getResponse(){
        return sipServletResponse; 
    }

}
