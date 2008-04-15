/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.core.session;

import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

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
		//return a converged session only if it is managing a sipcontext
		if(container instanceof SipContext) {
			return new ConvergedSession(this, sipFactoryImpl);
		} else {
			return super.getNewSession();
		}
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
