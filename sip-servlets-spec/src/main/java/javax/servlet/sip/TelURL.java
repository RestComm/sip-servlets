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
 * Represents tel URLs as defined by RFC 3966. Tel URLs represent telephone numbers. SIP servlet containers may be able to route requests based on tel URLs but are not required to.
 * See Also:RFC 3966
 */
public interface TelURL extends javax.servlet.sip.URI{
    /**
     * Compares the given TelURL with this TelURL. The comparison rules to be followed must be as specified in RFC 3966 section 5.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the phone number of this TelURL. The returned string includes any visual separators present in the phone number part of the URL but does not include a leading "+" for global tel URLs.
     */
    java.lang.String getPhoneNumber();

    /**
     * Returns true if this TelURL is global, and false otherwise.
     */
    boolean isGlobal();

    /**
     * Sets the phone number of this TelURL. The specified number must be a valid global or local phone number for the "tel" scheme as described in RFC3966 (URLs for Telephone Calls).
     */
    void setPhoneNumber(java.lang.String number);

    /**
     * Returns the String representation of this TelURL. Any reserved characters will be properly escaped according to RFC2396.
     */
    java.lang.String toString();

}
