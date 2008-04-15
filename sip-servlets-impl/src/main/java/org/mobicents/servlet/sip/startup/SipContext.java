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
package org.mobicents.servlet.sip.startup;

import org.apache.catalina.Context;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;

/**
 * A <b>SipContext</b> is a Container that represents a sip/converged servlet context, and
 * therefore an individual sip/converged application, in the Catalina servlet engine.
 *
 * <p>
 * This extends Tomcat Context interface to allow sip capabilities to be used on Tomcat deployed applictions.
 * <p>
 *
 */
public interface SipContext extends Context {

	public static final String APPLICATION_SIP_XML = "WEB-INF/sip.xml";
	
	String getApplicationName();

	void setApplicationName(String applicationName);

	String getDescription();
	
	void setDescription(String description);
	
	String getLargeIcon();

	void setLargeIcon(String largeIcon);

	SipListenersHolder getListeners();

	void setListeners(SipListenersHolder listeners);

	String getMainServlet();

	void setMainServlet(String mainServlet);

	int getProxyTimeout();
	
	void setProxyTimeout(int proxyTimeout);
	
	int getSipApplicationSessionTimeout();
	
	void setSipApplicationSessionTimeout(int proxyTimeout);

	void addConstraint(org.apache.catalina.deploy.SecurityConstraint securityConstraint);
	
	void removeConstraint(org.apache.catalina.deploy.SecurityConstraint securityConstraint);
	
	String getSmallIcon();

	void setSmallIcon(String smallIcon);

}