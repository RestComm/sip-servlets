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
 * Route modifiers as returned by the Application Router, used to interpret the returned route from the router.
 * @since 1.1
 *
 */
public enum SipRouteModifier{
	/**
	 * Indicates to the container to clear any popped route so far, such that SipServletRequest.getPoppedRoute() should now return null.
	 */
	CLEAR_ROUTE,
	/**
	 * Indicates that SipApplicationRouterInfo.getRoute() does not contain any valid route.
	 */	
	NO_ROUTE,
	/**
	 * Indicates that the route returned by SipApplicationRouterInfo.getRoute() is a valid route.
	 */
	ROUTE;
}
