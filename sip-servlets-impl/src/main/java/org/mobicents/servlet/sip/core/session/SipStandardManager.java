/**
 * 
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
