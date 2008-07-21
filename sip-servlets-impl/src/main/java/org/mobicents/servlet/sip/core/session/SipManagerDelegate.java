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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * This class handles the management of sip sessions and sip application sessions for a given container (context)
 * It is a delegate since it is used by many manager implementations classes (Standard and clustered ones) 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipManagerDelegate {

	private static transient Log logger = LogFactory.getLog(SipManagerDelegate.class);
	
	private Map<SipApplicationSessionKey, MobicentsSipApplicationSession> sipApplicationSessions = 
		new HashMap<SipApplicationSessionKey, MobicentsSipApplicationSession>();
	//if it's never cleaned up a memory leak will occur
	//Shall we have a thread scanning for invalid sessions and removing them accordingly ?
	//=> after a chat with ranga the better way to go for now is removing on processDialogTerminated
	private Map<SipSessionKey, MobicentsSipSession> sipSessions = 
		new HashMap<SipSessionKey, MobicentsSipSession>();

	private Object sipSessionLock = new Object();
	
	private Object sipApplicationSessionLock = new Object();

	SipFactoryImpl sipFactoryImpl;
	
	Container container;


	/**
     * The descriptive information about this implementation.
     */
    protected static final String info = "SipStandardManager/1.0";
    

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

	/**
	 * @return the container
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(Container container) {
		this.container = container;
	}
	
	/**
	 * Removes a sip session from the manager by its key
	 * @param key the identifier for this session
	 * @return the sip session that had just been removed, null otherwise
	 */
	public MobicentsSipSession removeSipSession(final SipSessionKey key) {
		if(logger.isDebugEnabled()) {
			logger.debug("Removing a sip session with the key : " + key);
		}
		synchronized (sipSessionLock) {			
			return sipSessions.remove(key);
		}
	}
	
	/**
	 * Removes a sip application session from the manager by its key
	 * @param key the identifier for this session
	 * @return the sip application session that had just been removed, null otherwise
	 */
	public MobicentsSipApplicationSession removeSipApplicationSession(final SipApplicationSessionKey key) {
		if(logger.isDebugEnabled()) {
			logger.debug("Removing a sip application session with the key : " + key);
		}
		synchronized (sipApplicationSessionLock) {			
			return sipApplicationSessions.remove(key);
		}
	}
	
	/**
	 * Retrieve a sip application session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true.
	 * @param key the key identifying the sip application session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @return the sip application session matching the key
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(final SipApplicationSessionKey key, final boolean create) {
		MobicentsSipApplicationSession sipApplicationSessionImpl = null;
		synchronized (sipApplicationSessionLock) {
			sipApplicationSessionImpl = sipApplicationSessions.get(key);
			if(sipApplicationSessionImpl == null && create) {
				sipApplicationSessionImpl = new SipApplicationSessionImpl(key, (SipContext) container);
				if(logger.isDebugEnabled()) {
					logger.debug("Adding a sip application session with the key : " + key);
				}			
				sipApplicationSessions.put(key, sipApplicationSessionImpl);			
			}
		}
		return sipApplicationSessionImpl;
	}	


	/**
	 * Retrieve a sip session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true. the sip factory cannot be null
	 * if create is set to true.
	 * @param key the key identifying the sip session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @param sipFactoryImpl needed only for sip session creation.
	 * @param sipApplicationSessionImpl to associate the SipSession with if create is set to true, if false it won't be used
	 * @return the sip session matching the key
	 * @throws IllegalArgumentException if create is set to true and sip Factory is null
	 */
	public MobicentsSipSession getSipSession(final SipSessionKey key, final boolean create, final SipFactoryImpl sipFactoryImpl, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		if(create && sipFactoryImpl == null) {
			throw new IllegalArgumentException("the sip factory should not be null");
		}
		MobicentsSipSession sipSessionImpl = null;
		//TODO check if a reentrant lock would be more efficient
		synchronized (sipSessionLock) {
			sipSessionImpl = sipSessions.get(key);
			if(sipSessionImpl == null && create) {
				sipSessionImpl = new SipSessionImpl(key, sipFactoryImpl, sipApplicationSessionImpl);
				if(logger.isDebugEnabled()) {
					logger.debug("Adding a sip session with the key : " + key);
				}
				sipSessions.put(key, sipSessionImpl);					
			}
			// check if this session key has a to tag.
			if(sipSessionImpl != null) {
				String toTag = sipSessionImpl.getKey().getToTag();
				if(toTag == null && key.getToTag() != null) {
					sipSessionImpl.getKey().setToTag(key.getToTag());
				} else if (key.getToTag() != null && !toTag.equals(key.getToTag())) {
					MobicentsSipSession derivedSipSession = sipSessionImpl.findDerivedSipSession(key.getToTag());
					if(derivedSipSession == null) {
						// if the to tag is different a sip session is created
						if(logger.isDebugEnabled()) {
							logger.debug("Original session " + key + " with To Tag " + sipSessionImpl.getKey().getToTag() + 
									" creates new derived session with following to Tag " + key.getToTag());
						}
						derivedSipSession = createDerivedSipSession(sipSessionImpl, key);
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("Original session " + key + " with To Tag " + sipSessionImpl.getKey().getToTag() + 
									" already has a derived session with following to Tag " + key.getToTag() + " - reusing it");
						}
					}
					return derivedSipSession;	
				}
			}
		}
		return sipSessionImpl;
	}

	/**
	 * clone the parent sip session given in parameter except its attributes (they will be shared) 
	 * and add it to the internal map of derived sessions identifying it by its ToTag
	 * 
	 * @param parentSipSession the parent sip session holding the newly created derived session
	 * @param sessionKey the key of the new derived session to create
	 * @return the newly created derived session
	 */
	protected MobicentsSipSession createDerivedSipSession(MobicentsSipSession parentSipSession, SipSessionKey sessionKey) {
		// clone the session and add it to the map of derived sessions
		MobicentsSipSession sipSessionImpl = new SipSessionImpl(sessionKey, sipFactoryImpl, parentSipSession.getSipApplicationSession());
		sipSessionImpl.setSipSessionAttributeMap(parentSipSession.getSipSessionAttributeMap());
		sipSessionImpl.setSupervisedMode(parentSipSession.getSupervisedMode());
		try {
			sipSessionImpl.setHandler(new String(parentSipSession.getHandler()));
		} catch (ServletException e) {
			//cannot happen
			logger.error(e);
		}
		sipSessionImpl.setRoutingRegion(parentSipSession.getRegion());
		// dialog will be set when the response will be associated with this session
//		sipSessionImpl.sessionCreatingDialog = dialog;
		sipSessionImpl.setState(parentSipSession.getState());
		sipSessionImpl.setStateInfo(parentSipSession.getStateInfo());
		sipSessionImpl.setSupervisedMode(parentSipSession.getSupervisedMode());
		if(parentSipSession.getSipSubscriberURI() != null) {
			sipSessionImpl.setSipSubscriberURI(parentSipSession.getSipSubscriberURI().clone());
		}
		sipSessionImpl.setUserPrincipal(parentSipSession.getUserPrincipal());
		sipSessionImpl.setParentSession(parentSipSession);
		
		parentSipSession.addDerivedSipSessions(sipSessionImpl);
		
		return sipSessionImpl;
	}

	/**
	 * Retrieve all sip sessions currently hold by the session manager
	 * @return an iterator on the sip sessions
	 */
	public Iterator<MobicentsSipSession> getAllSipSessions() {
		return sipSessions.values().iterator();
	}

	/**
	 * Retrieve all sip application sessions currently hold by the session manager
	 * @return an iterator on the sip sessions
	 */
	public Iterator<MobicentsSipApplicationSession> getAllSipApplicationSessions() {
		return sipApplicationSessions.values().iterator();
	}
	
	/**
	 * Retrieves the sip application session holding the converged http session in parameter
	 * @param convergedHttpSession the converged session to look up
	 * @return the sip application session holding a reference to it or null if none references it
	 */
	public MobicentsSipApplicationSession findSipApplicationSession(HttpSession httpSession) {
		for (MobicentsSipApplicationSession sipApplicationSessionImpl : sipApplicationSessions.values()) {			
			if(sipApplicationSessionImpl.findHttpSession(httpSession) != null) {
				return sipApplicationSessionImpl;
			}
		}
		return null;
	}

	/**
	 * 
	 */
	public void dumpSipSessions() {
		if(logger.isDebugEnabled()) {
			logger.debug("sip sessions present in the session manager");
		
			for (SipSessionKey sipSessionKey : sipSessions.keySet()) {
				logger.debug(sipSessionKey.toString());
			}
		}
	}

	/**
	 * 
	 */
	public void dumpSipApplicationSessions() {
		if(logger.isDebugEnabled()) {
			logger.debug("sip application sessions present in the session manager");
		
			for (SipApplicationSessionKey sipApplicationSessionKey : sipApplicationSessions.keySet()) {
				logger.debug(sipApplicationSessionKey.toString());
			}
		}
	}

	
	/**
	 * Remove the sip sessions and sip application sessions 
	 */
	public void removeAllSessions() {		
		List<SipSessionKey> sipSessionsToRemove = new ArrayList<SipSessionKey>(); 
		for (SipSessionKey sipSessionKey : sipSessions.keySet()) {
			sipSessionsToRemove.add(sipSessionKey);
		}
		for (SipSessionKey sipSessionKey : sipSessionsToRemove) {
			removeSipSession(sipSessionKey);
		}
		List<SipApplicationSessionKey> sipApplicationSessionsToRemove = new ArrayList<SipApplicationSessionKey>(); 
		for (SipApplicationSessionKey sipApplicationSessionKey : sipApplicationSessions.keySet()) {
			sipApplicationSessionsToRemove.add(sipApplicationSessionKey);
		}
		for (SipApplicationSessionKey sipApplicationSessionKey : sipApplicationSessionsToRemove) {
			removeSipApplicationSession(sipApplicationSessionKey);
		}				
	}
}
