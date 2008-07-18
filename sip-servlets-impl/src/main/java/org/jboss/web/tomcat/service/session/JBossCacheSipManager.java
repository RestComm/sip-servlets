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
package org.jboss.web.tomcat.service.session;

import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Session;
import org.jboss.metadata.WebMetaData;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class JBossCacheSipManager extends JBossCacheManager implements SipManager {

	private SipManagerDelegate sipManagerDelegate;
	
	public JBossCacheSipManager() {
		super();		
	}
	
	@Override
	public Session createEmptySession() {	
		return createEmptyClusteredSession(sipManagerDelegate.getSipFactoryImpl().getSipNetworkInterfaceManager());		
	}
	
	private ClusteredSession createEmptyClusteredSession(
			SipNetworkInterfaceManager sipNetworkInterfaceManager) {
		log_.debug("Creating an empty ClusteredSession");

		ClusteredSession session = null;
		switch (replicationGranularity_) {
		case (WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE): {
			if (super.container_ instanceof SipContext) {
				session = new ConvergedAttributeBasedClusteredSession(this,
						sipNetworkInterfaceManager);
			} else {
				session = new AttributeBasedClusteredSession(this);
			}
			break;
		}
		case (WebMetaData.REPLICATION_GRANULARITY_FIELD): {
			if (super.container_ instanceof SipContext) {
				session = new ConvergedFieldBasedClusteredSession(this,
						sipNetworkInterfaceManager);
			} else {
				session = new FieldBasedClusteredSession(this);
			}
			break;
		}
		default:
			if (super.container_ instanceof SipContext) {
				session = new ConvergedSessionBasedClusteredSession(this,
						sipNetworkInterfaceManager);
			} else {
				session = new SessionBasedClusteredSession(this);
			}
			break;
		}
		return session;
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
		container_ = container;
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
}
