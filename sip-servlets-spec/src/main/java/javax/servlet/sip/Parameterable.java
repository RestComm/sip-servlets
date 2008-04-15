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
import java.util.Map;
import java.util.Set;

/**
 * The Parameterable interface is used to indicate a SIP header field value with optional parameters. All of the Address header fields are Parameterable, including Contact, From, To, Route, Record-Route, and Reply-To. In addition, the header fields Accept, Accept-Encoding, Alert-Info, Call-Info, Content-Disposition, Content-Type, Error-Info, Retry-After and Via are also Parameterable.
 * @since 1.1
 */
public interface Parameterable extends java.lang.Cloneable{
    /**
     * Returns a clone of this Parameterable. The cloned Parameterable has identical field value and parameters.
     */
    java.lang.Object clone();

    /**
     * Compares the given Parameterable type with this one. The comparison rules to be used for the Parameterable comparison should be taken as specified in the underlying specifications. Most of the headers of Parameterable type are defined in RFC 3261, however for others their respective specifications should be consulted for comaprison.
     */
    boolean equals(java.lang.Object o);

    /**
     * Returns the value of the named parameter, or null if it is not set. A zero-length String indicates a flag parameter.
     */
    java.lang.String getParameter(java.lang.String key);

    /**
     * Returns an Iterator of the names of all parameters contained in this object. The order is the order of appearance of the parameters in the Parameterable.
     */
    Iterator<java.lang.String> getParameterNames();

    /**
     * Returns a Collection view of the parameter name-value mappings contained in this Parameterable. The order is the order of appearance of the parameters in the Parameterable.
     */
    Set<Map.Entry<String,String>> getParameters();

    /**
     * Returns the field value as a string.
     */
    java.lang.String getValue();

    /**
     * Removes the named parameter from this object. Nothing is done if the object did not already contain the specific parameter.
     */
    void removeParameter(java.lang.String name);

    /**
     * Sets the value of the named parameter. If this object previously contained a value for the given parameter name, then the old value is replaced by the specified value. The setting of a flag parameter is indicated by specifying a zero-length String for the parameter value. Calling this method with null value is equivalent to calling
     */
    void setParameter(java.lang.String name, java.lang.String value);

    /**
     * Set the value of the field.
     */
    void setValue(java.lang.String value);

}
