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

import java.util.Iterator;

/**
 * Base interface for any type of URI. These are used in the request line of SIP requests to identify the callee and also in Contact, From, and To headers.
 * The only feature common to all URIs is that they can be represented as strings beginning with a token identifying the scheme of the URI followed by a colon followed by a scheme-specific part.
 * The generic syntax of URIs is defined in RFC 2396.
 */
public interface URI extends java.lang.Cloneable{
    /**
     * Compares the given URI with this URI. The comparison rules to be followed shall depend upon the underlying URI scheme being used. For general purpose URIs RFC 2396 should be consulted for equality. If the URIs are of scheme for which comparison rules are further specified in their specications, then they must be used for any comparison.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the value of the named parameter, or null if it is not set. A zero-length String indicates flag parameter.
     */
    java.lang.String getParameter(java.lang.String key);

    /**
     * Returns an Iterator over the names of all parameters present in this URI.
     */
    Iterator<java.lang.String> getParameterNames();

    /**
     * Returns the scheme of this URI, for example "sip", "sips" or "tel".
     */
    java.lang.String getScheme();

    /**
     * Returns true if the scheme is "sip" or "sips", false otherwise.
     */
    boolean isSipURI();

    /**
     * Removes the named parameter from this URL. Nothing is done if the URL did not already contain the specific parameter.
     */
    void removeParameter(java.lang.String name);

    /**
     * Sets the value of the named parameter. If this URL previously contained a value for the given parameter name, then the old value is replaced by the specified value. The setting of a flag parameter is indicated by specifying a zero-length String for the parameter value.
     */
    void setParameter(java.lang.String name, java.lang.String value);

    /**
     * Returns the value of this URI as a String. The result must be appropriately URL escaped.
     */
    java.lang.String toString();

}
