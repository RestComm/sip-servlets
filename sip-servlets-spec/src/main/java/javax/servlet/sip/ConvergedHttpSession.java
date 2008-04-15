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
 * The ConvergedHttpSession class provides access to HttpSession related functionality which is only present in a converged HTTP/SIP container. In a converged container, an instance of HttpSession can be cast to ConvergedHttpSession in order to access methods available available only to converged applications.
 * Since: 1.1
 */
public interface ConvergedHttpSession extends javax.servlet.http.HttpSession{
    /**
     * This method encodes the HTTP URL with the jsessionid. ";jsessionid=http-session-id". The URL parameter should be an absolute URL. For example, http://server:7001/mywebapp/foo.jsp. Where "/mywebapp" is the context path of the the current ServletContext, because that is where the httpSession belongs to.
     */
    java.lang.String encodeURL(java.lang.String url);

    /**
     * Converts the given relative path to an absolute URL by prepending the contextPath for the current ServletContext, the given scheme ("http" or "https"), and the host:port, and then encoding the resulting URL with the jsessionid.
     * For example, this method converts: from: "/foo.jsp" to: "http://server:8888/mywebapp/foo.jsp;jsessionid=http-session-id" Where, "/mywebapp" is the contextPath for the current ServletContext server is the front end host defined for the web server.
     */
    java.lang.String encodeURL(java.lang.String relativePath, java.lang.String scheme);

    /**
     * Returns the parent SipApplicationSession if it exists, if none exists then a new one is created and returned after associating it with the converged http session.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession();

}
