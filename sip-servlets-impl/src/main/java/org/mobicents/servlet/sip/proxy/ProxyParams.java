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
package org.mobicents.servlet.sip.proxy;

import javax.servlet.sip.SipURI;

public class ProxyParams
{
	public ProxyParams(SipURI destination, SipURI outboundInterface, SipURI routeRecord, SipURI path)
	{
		this.destination = destination;
		this.outboundInterface = outboundInterface;
		this.routeRecord = routeRecord;
		this.path = path;
	}
	public SipURI destination;
	public SipURI outboundInterface;
	public SipURI routeRecord;
	public SipURI path;// for REGISTER path header
}