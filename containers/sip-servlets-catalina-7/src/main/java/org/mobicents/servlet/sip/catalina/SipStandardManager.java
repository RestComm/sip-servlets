/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.mobicents.servlet.sip.catalina;

import java.util.Iterator;

import javax.management.ObjectName;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.servlet.sip.catalina.session.ConvergedStandardSession;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.core.session.SipStandardManagerDelegate;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Extension of the Standard implementation of the <b>Manager</b> interface provided by Tomcat
 * to be able to make the httpsession available as ConvergedHttpSession as for
 * Spec JSR289 Section 13.5 and to handle management of sip sessions and sip application sessions for a given container (context)
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipStandardManager extends StandardManager implements CatalinaSipManager {

	private static final Logger logger = Logger.getLogger(SipStandardManager.class);
	private SipManagerDelegate sipManagerDelegate;
	
	/**
     * The descriptive information about this implementation.
     */
    protected static final String INFO = "SipStandardManager/1.0";
    
    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected final static String NAME = "SipStandardManager";

    
	/**
	 * 
	 */
	public SipStandardManager() {
		super();
		sipManagerDelegate = new SipStandardManagerDelegate();
	}

	@Override
	public void initInternal() throws LifecycleException {
		if( this.getState().equals(LifecycleState.INITIALIZED) ) return;	                
	    
		super.initInternal();
		
		ObjectName oname = super.getObjectName();
		String domain; 
	        
        if(oname==null && this.getContainer() instanceof SipContext) {
            CatalinaSipContext ctx = (CatalinaSipContext) this.getContainer();
            domain=ctx.getEngineName();
            distributable = ctx.getDistributable();
            StandardHost hst=(StandardHost)ctx.getParent();
            String path = ctx.getPath();
            if (path.equals("")) {
                path = "/";
            }   
            String objectNameString = domain + ":type=SipManager,path="
            	+ path + ",host=" + hst.getName();
            try {
                oname=new ObjectName(objectNameString);
                Registry.getRegistry(null, null).registerComponent(this, oname, null );
            } catch (Exception e) {
                throw new IllegalStateException("error registering the mbean " + objectNameString, e);
            }
        }
        /*
        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
				if(sipManagerDelegate != null) {
					Iterator<MobicentsSipSession> sessions = sipManagerDelegate.getAllSipSessions();
					while(sessions.hasNext()) {
						SipSessionImpl session = (SipSessionImpl) sessions.next();
						if(session.isReadyToInvalidate()) {
							long idleTime = System.currentTimeMillis() - session.getLastAccessedTime();
							if(idleTime > 2000) {
								session.onTerminatedState();
								
								MobicentsSipApplicationSession sipApplicationSession =  session.getSipApplicationSession();
								if(sipApplicationSession != null) {
									if(sipApplicationSession.isValid() && sipApplicationSession.isReadyToInvalidate()) {				
										sipApplicationSession.tryToInvalidate();
									}		
								}
								if(logger.isInfoEnabled()) {
									logger.info("Reaper thread cleaned up the folowing SIP session" + session.getId());
								}
							}
						}
					}
				}
				} catch (Throwable t) {
					logger.warn("Error reaping inactive SIP sessions. You can ignore this warning as long as it doesn't happen too often", t);
				}
			}
		}, 1000, 4000);*/
	}
	
	@Override
	protected StandardSession getNewSession() {
		//return a converged session only if it is managing a sipcontext
		if(container instanceof SipContext) {
			return new ConvergedStandardSession(this);
		} else {
			return super.getNewSession();
		}
	}

	/**
	 * @return the SipFactoryImpl
	 */
	public MobicentsSipFactory getMobicentsSipFactory() {
		return sipManagerDelegate.getSipFactoryImpl();
	}

	/**
	 * @param sipFactoryImpl the SipFactoryImpl to set
	 */
	public void setMobicentsSipFactory(MobicentsSipFactory sipFactoryImpl) {
		sipManagerDelegate.setSipFactoryImpl((SipFactoryImpl)sipFactoryImpl);
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

        // De-register from the old Container (if any)
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);

        this.container = container;        
        if(container instanceof SipContext)
        	sipManagerDelegate.setContainer((SipContext)container);

        // Register with the new Container (if any)
        if ((this.container != null) && (this.container instanceof Context)) {
            setMaxInactiveInterval
                ( ((Context) this.container).getSessionTimeout()*60 );
            ((Context) this.container).addPropertyChangeListener(this);
        }
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession removeSipSession(final MobicentsSipSessionKey key) {
		return sipManagerDelegate.removeSipSession(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession removeSipApplicationSession(final MobicentsSipApplicationSessionKey key) {
		return sipManagerDelegate.removeSipApplicationSession(key);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(final MobicentsSipApplicationSessionKey key, final boolean create) {
		return sipManagerDelegate.getSipApplicationSession((SipApplicationSessionKey)key, create);
	}	


	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession getSipSession(final MobicentsSipSessionKey key, final boolean create, final MobicentsSipFactory sipFactoryImpl, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		return sipManagerDelegate.getSipSession((SipSessionKey)key, create, (SipFactoryImpl)sipFactoryImpl, sipApplicationSessionImpl);
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
	
	
	// JMX Statistics
	/**
	 * Return descriptive information about this Manager implementation and the
	 * corresponding version number, in the format
	 * <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	public String getInfo() {
		return (INFO);
	}

	/**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {
        return (NAME);
    }
	
	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipSessions() {
		return this.sipManagerDelegate.getMaxActiveSipSessions();
	}

	/**
	 * Set the maximum number of actives Sip Sessions allowed, or -1 for no
	 * limit.
	 * 
	 * @param max
	 *            The new maximum number of sip sessions
	 */
	public void setMaxActiveSipSessions(int max) {
		int oldMaxActiveSipSessions = this.sipManagerDelegate.getMaxActiveSipSessions();
		this.sipManagerDelegate.setMaxActiveSipSessions(max);
		support.firePropertyChange("maxActiveSipSessions", Integer.valueOf(
				oldMaxActiveSipSessions), Integer.valueOf(
				this.sipManagerDelegate.getMaxActiveSipSessions()));
	}

	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipApplicationSessions() {
		return this.sipManagerDelegate.getMaxActiveSipApplicationSessions();
	}

	/**
	 * Set the maximum number of actives Sip Application Sessions allowed, or -1
	 * for no limit.
	 * 
	 * @param max
	 *            The new maximum number of sip application sessions
	 */
	public void setMaxActiveSipApplicationSessions(int max) {
		int oldMaxActiveSipApplicationSessions = this.sipManagerDelegate.getMaxActiveSipApplicationSessions();
		this.sipManagerDelegate.setMaxActiveSipApplicationSessions(max);
		support
				.firePropertyChange(
						"maxActiveSipApplicationSessions",
						Integer.valueOf(oldMaxActiveSipApplicationSessions),
						Integer.valueOf(
								this.sipManagerDelegate.getMaxActiveSipApplicationSessions()));
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipSessions() {
		return sipManagerDelegate.getRejectedSipSessions();
	}

	public void setRejectedSipSessions(int rejectedSipSessions) {
		this.sipManagerDelegate.setRejectedSipSessions(rejectedSipSessions);
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipApplicationSessions() {
		return sipManagerDelegate.getRejectedSipApplicationSessions();
	}

	public void setRejectedSipApplicationSessions(
			int rejectedSipApplicationSessions) {
		this.sipManagerDelegate.setRejectedSipApplicationSessions(rejectedSipApplicationSessions);
	}

	public void setSipSessionCounter(int sipSessionCounter) {
		this.sipManagerDelegate.setSipSessionCounter(sipSessionCounter);
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipSessionCounter() {
		return sipManagerDelegate.getSipSessionCounter();
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipSessions() {
		return sipManagerDelegate.getNumberOfSipSessions();
	}

	/**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
	public int getSipSessionMaxAliveTime() {
		return sipManagerDelegate.getSipSessionMaxAliveTime();
	}

	/**
	 * Sets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @param sessionMaxAliveTime
	 *            Longest time (in seconds) that an expired session had been
	 *            alive.
	 */
	public void setSipSessionMaxAliveTime(int sipSessionMaxAliveTime) {
		this.sipManagerDelegate.setSipSessionMaxAliveTime(sipSessionMaxAliveTime);
	}

	/**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
	public int getSipSessionAverageAliveTime() {
		return sipManagerDelegate.getSipSessionAverageAliveTime();
	}

	/**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
	public void setSipSessionAverageAliveTime(int sipSessionAverageAliveTime) {
		this.sipManagerDelegate.setSipSessionAverageAliveTime(sipSessionAverageAliveTime);
	}

	public void setSipApplicationSessionCounter(int sipApplicationSessionCounter) {
		this.sipManagerDelegate.setSipApplicationSessionCounter(sipApplicationSessionCounter);
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipApplicationSessionCounter() {
		return sipManagerDelegate.getSipApplicationSessionCounter();
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipApplicationSessions() {
		return sipManagerDelegate.getNumberOfSipApplicationSessions();
	}

	/**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
	public int getSipApplicationSessionMaxAliveTime() {
		return sipManagerDelegate.getSipApplicationSessionMaxAliveTime();
	}

	/**
	 * Sets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @param sessionMaxAliveTime
	 *            Longest time (in seconds) that an expired session had been
	 *            alive.
	 */
	public void setSipApplicationSessionMaxAliveTime(
			int sipApplicationSessionMaxAliveTime) {
		this.sipManagerDelegate.setSipApplicationSessionMaxAliveTime(sipApplicationSessionMaxAliveTime);
	}

	/**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
	public int getSipApplicationSessionAverageAliveTime() {
		return sipManagerDelegate.getSipApplicationSessionAverageAliveTime();
	}

	/**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
	public void setSipApplicationSessionAverageAliveTime(
			int sipApplicationSessionAverageAliveTime) {
		this.sipManagerDelegate.setSipApplicationSessionAverageAliveTime(sipApplicationSessionAverageAliveTime);
	}

	/**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
	public int getExpiredSipSessions() {
		return sipManagerDelegate.getExpiredSipSessions();
	}

	/**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
	public void setExpiredSipSessions(int expiredSipSessions) {
		this.sipManagerDelegate.setExpiredSipSessions(expiredSipSessions);
	}

	/**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
	public int getExpiredSipApplicationSessions() {
		return sipManagerDelegate.getExpiredSipApplicationSessions();
	}

	/**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
	public void setExpiredSipApplicationSessions(
			int expiredSipApplicationSessions) {
		this.sipManagerDelegate.setExpiredSipApplicationSessions(expiredSipApplicationSessions);
	}

	/** 
     * For debugging: return a list of all session ids currently active
     *
     */
    public String listSipSessionIds() {
        StringBuffer sb=new StringBuffer();
        Iterator<MobicentsSipSession> sipSessions = sipManagerDelegate.getAllSipSessions();
        while (sipSessions.hasNext()) {
            sb.append(sipSessions.next().getKey()).append(" ");
        }
        return sb.toString();
    }
    
    /** 
     * For debugging: return a list of all session ids currently active
     *
     */
    public String listSipApplicationSessionIds() {
    	StringBuffer sb=new StringBuffer();
        Iterator<MobicentsSipApplicationSession> sipApplicationSessions = sipManagerDelegate.getAllSipApplicationSessions();
        while (sipApplicationSessions.hasNext()) {
            sb.append(sipApplicationSessions.next().getKey()).append(" ");
        }
        return sb.toString();
    }

	public double getNumberOfSipApplicationSessionCreationPerSecond() {
		return sipManagerDelegate.getNumberOfSipApplicationSessionCreationPerSecond();
	}

	public double getNumberOfSipSessionCreationPerSecond() {
		return sipManagerDelegate.getNumberOfSipSessionCreationPerSecond();
	}

	public void updateStats() {
		sipManagerDelegate.updateStats();
	}
	
}
