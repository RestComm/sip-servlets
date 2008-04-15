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
package org.mobicents.servlet.sip.core.session;

import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Extension of the Standard implementation of the <b>Manager</b> interface provided by Tomcat
 * to be able to make the httpsession available as ConvergedHttpSession as for
 * Spec JSR289 Section 13.5
 * 
 * @author Jean Deruelle
 *
 */
public class SipStandardManager extends StandardManager {

	SipFactoryImpl sipFactoryImpl;
	
	/**
	 * 
	 * @param sipFactoryImpl
	 */
	public SipStandardManager() {
		super();		
	}
	
	@Override
	protected StandardSession getNewSession() {		
		return new ConvergedSession(this, sipFactoryImpl);
	}

	/**
	 * @return the SipFactoryImpl
	 */
	public SipFactoryImpl getSipFactoryImpl() {
		return sipFactoryImpl;
	}

	/**
	 * @param sipFactoryImpl the SipFactoryImpl to set
	 */
	public void setSipFactoryImpl(SipFactoryImpl sipFactoryImpl) {
		this.sipFactoryImpl = sipFactoryImpl;
	}
}
