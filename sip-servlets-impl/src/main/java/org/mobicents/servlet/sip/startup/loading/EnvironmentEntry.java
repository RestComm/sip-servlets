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


public class EnvironmentEntry {
	public String description;
	public String name;
	public String value;
	public String type;
	public static final String BOOLEAN = "java.lang.Boolean";
	public static final String BYTE = "java.lang.Byte";
	public static final String CHARACTER = "java.lang.Character";
	public static final String STRING = "java.lang.String";
	public static final String SHORT = "java.lang.Short";
	public static final String INTEGER = "java.lang.Integer";
	public static final String LONG = "java.lang.Long";
	public static final String DOUBLE = "java.lang.Double";
	public static final String FLOAT = "java.lang.Float";
}
