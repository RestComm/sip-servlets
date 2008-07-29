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

import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Extension of the Standard implementation of the <b>Manager</b> interface provided by Tomcat
 * to be able to make the httpsession available as ConvergedHttpSession as for
 * Spec JSR289 Section 13.5 and to handle management of sip sessions and sip application sessions for a given container (context)
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipStandardManager extends StandardManager implements SipManager {

	private SipManagerDelegate sipManagerDelegate;
	
	/**
     * The descriptive information about this implementation.
     */
    protected static final String info = "SipStandardManager/1.0";
    
	/**
	 * 
	 * @param sipFactoryImpl
	 */
	public SipStandardManager() {
		super();
		sipManagerDelegate = new SipStandardManagerDelegate();
	}
	
	@Override
	protected StandardSession getNewSession() {
		//return a converged session only if it is managing a sipcontext
		if(container instanceof SipContext) {
			return new ConvergedSession(this, sipManagerDelegate.getSipFactoryImpl().getSipNetworkInterfaceManager());
		} else {
			return super.getNewSession();
		}
	}

	/**
	 * @return the SipFactoryImpl
	 */
	public SipFactoryImpl getSipFactoryImpl() {
		return sipManagerDelegate.getSipFactoryImpl();
	}

	/**
	 * @param sipFactoryImpl the SipFactoryImpl to set
	 */
	public void setSipFactoryImpl(SipFactoryImpl sipFactoryImpl) {
		sipManagerDelegate.setSipFactoryImpl(sipFactoryImpl);
	}
		
	/**
	 * @return the container
	 */
	public Container getContainer() {
		return sipManagerDelegate.getContainer();
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(Container container) {
		this.container = container;
		sipManagerDelegate.setContainer(container);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession removeSipSession(final SipSessionKey key) {
		return sipManagerDelegate.removeSipSession(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession removeSipApplicationSession(final SipApplicationSessionKey key) {
		return sipManagerDelegate.removeSipApplicationSession(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(final SipApplicationSessionKey key, final boolean create) {
		return sipManagerDelegate.getSipApplicationSession(key, create);
	}	


	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession getSipSession(final SipSessionKey key, final boolean create, final SipFactoryImpl sipFactoryImpl, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		return sipManagerDelegate.getSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<MobicentsSipSession> getAllSipSessions() {
		return sipManagerDelegate.getAllSipSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<MobicentsSipApplicationSession> getAllSipApplicationSessions() {
		return sipManagerDelegate.getAllSipApplicationSessions();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession findSipApplicationSession(HttpSession httpSession) {		
		return sipManagerDelegate.findSipApplicationSession(httpSession);
	}

	/**
	 * 
	 */
	public void dumpSipSessions() {
		sipManagerDelegate.dumpSipSessions();
	}

	/**
	 * 
	 */
	public void dumpSipApplicationSessions() {
		sipManagerDelegate.dumpSipApplicationSessions();
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void removeAllSessions() {		
		sipManagerDelegate.removeAllSessions();
	}

	/**
	 * {@inheritDoc} 
	 */
	public void changeSessionKey(SipSessionKey oldKey, SipSessionKey newKey) {
		sipManagerDelegate.changeSessionKey(oldKey, newKey);
		
	}
}
