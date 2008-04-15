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
 * This interface allows applications to set the authentication 
 * information on servlet initiated requests that are challenged by a Proxy or UAS.
 */
public interface AuthInfo {
	
	/**
	 * Helper method to add authentication info into the AuthInfo object 
	 * for a challenge response of a specific type (401/407) and realm.
	 * @param statusCode Status code (401/407) of the challenge response
	 * @param realm Realm that was returned in the challenge response
	 * @param username
	 * @param password
	 */
	void addAuthInfo(int statusCode,
            java.lang.String realm,
            java.lang.String username,
            java.lang.String password);
}
