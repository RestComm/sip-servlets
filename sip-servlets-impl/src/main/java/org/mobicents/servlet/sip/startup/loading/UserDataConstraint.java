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
package org.mobicents.servlet.sip.startup.loading;

public class UserDataConstraint {
	public String description;
	public Object transportGuarantee;
	public static final String NONE_TRANSPORT_GUARANTEE = "NONE";
	public static final String INTEGRAL_TRANSPORT_GUARANTEE = "INTEGRAL";
	public static final String CONFIDENTIAL_TRANSPORT_GUARANTEE = "CONFIDENTIAL";
}
