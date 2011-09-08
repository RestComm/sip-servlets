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

package org.mobicents.servlet.sip.core;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;


/**
 * A <b>SipManager</b> manages the Sip Sessions that are associated with a
 * particular {@link SipContext}.  Different Manager implementations may support
 * value-added features such as the persistent storage of sip session data,
 * as well as migrating sip sessions for distributable sip applications.
 * <p>
 * In order for a <code>SipManager</code> implementation to successfully operate
 * with a <code>SipContext</code> implementation that implements reloading, it
 * must obey the following constraints:
 * <ul>
 * <li>Must allow a call to <code>stop()</code> to be followed by a call to
 *     <code>start()</code> on the same <code>SipManager</code> instance.</li>
 * </ul>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public interface SipManager {

	/**
	 * Removes a sip session from the manager by its key
	 * @param key the identifier for this session
	 * @return the sip session that had just been removed, null otherwise
	 */
	public MobicentsSipSession removeSipSession(final MobicentsSipSessionKey key);
	/**
	 * Removes a sip application session from the manager by its key
	 * @param key the identifier for this session
	 * @return the sip application session that had just been removed, null otherwise
	 */
	public MobicentsSipApplicationSession removeSipApplicationSession(final MobicentsSipApplicationSessionKey key);
	
	/**
	 * Retrieve a sip application session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true.
	 * @param key the key identifying the sip application session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @return the sip application session matching the key
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(final MobicentsSipApplicationSessionKey key, final boolean create);

	/**
	 * Retrieve a sip session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true. the sip factory cannot be null
	 * if create is set to true.
	 * @param key the key identifying the sip session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @param sipFactoryImpl needed only for sip session creation.
	 * @param MobicentsSipApplicationSession to associate the SipSession with if create is set to true, if false it won't be used
	 * @return the sip session matching the key
	 * @throws IllegalArgumentException if create is set to true and sip Factory is null
	 */
	public MobicentsSipSession getSipSession(final MobicentsSipSessionKey key, final boolean create, final MobicentsSipFactory sipFactoryImpl, final MobicentsSipApplicationSession MobicentsSipApplicationSession);
	
	/**
	 * Retrieves the sip application session holding the converged http session in parameter
	 * @param convergedHttpSession the converged session to look up
	 * @return the sip application session holding a reference to it or null if none references it
	 */
	public MobicentsSipApplicationSession findSipApplicationSession(HttpSession httpSession);
	/**
	 * Remove the sip sessions and sip application sessions 
	 */
	public void removeAllSessions();
	
	public void setMobicentsSipFactory(MobicentsSipFactory sipFactoryImpl);
	
	public MobicentsSipFactory getMobicentsSipFactory();
	
	public void dumpSipSessions();
	public void dumpSipApplicationSessions();
	
	public Iterator<MobicentsSipSession> getAllSipSessions();
 	public Iterator<MobicentsSipApplicationSession> getAllSipApplicationSessions();
	
	//JMX Statistics
	 /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSipSessions();

    /**
     * Set the maximum number of actives Sip Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sip sessions
     */
    public void setMaxActiveSipSessions(int max);

    /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSipApplicationSessions();

    /**
     * Set the maximum number of actives Sip Application Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sip application sessions
     */
    public void setMaxActiveSipApplicationSessions(int max);

    
    /** Number of sip session creations that failed due to maxActiveSipSessions
    *
    * @return The count
    */
   public int getRejectedSipSessions();


   public void setRejectedSipSessions(int rejectedSipSessions); 
   
   /** Number of sip session creations that failed due to maxActiveSipSessions
   *
   * @return The count
   */
  public int getRejectedSipApplicationSessions() ;


  public void setRejectedSipApplicationSessions(int rejectedSipApplicationSessions);

  public void setSipSessionCounter(int sipSessionCounter);
  /** 
   * Total sessions created by this manager.
   *
   * @return sessions created
   */
  public int getSipSessionCounter();
  
  /** 
   * Returns the number of active sessions
   *
   * @return number of sessions active
   */
  public int getActiveSipSessions();

  /**
   * Gets the longest time (in seconds) that an expired session had been
   * alive.
   *
   * @return Longest time (in seconds) that an expired session had been
   * alive.
   */
  public int getSipSessionMaxAliveTime();

  /**
   * Sets the longest time (in seconds) that an expired session had been
   * alive.
   *
   * @param sessionMaxAliveTime Longest time (in seconds) that an expired
   * session had been alive.
   */
  public void setSipSessionMaxAliveTime(int sipSessionMaxAliveTime);

  /**
   * Gets the average time (in seconds) that expired sessions had been
   * alive.
   *
   * @return Average time (in seconds) that expired sessions had been
   * alive.
   */
  public int getSipSessionAverageAliveTime();


  /**
   * Sets the average time (in seconds) that expired sessions had been
   * alive.
   *
   * @param sessionAverageAliveTime Average time (in seconds) that expired
   * sessions had been alive.
   */
  public void setSipSessionAverageAliveTime(int sipSessionAverageAliveTime);

  
  public void setSipApplicationSessionCounter(int sipApplicationSessionCounter);

  /** 
   * Total sessions created by this manager.
   *
   * @return sessions created
   */
  public int getSipApplicationSessionCounter();

  /** 
   * Returns the number of active sessions
   *
   * @return number of sessions active
   */
  public int getActiveSipApplicationSessions();

  /**
   * Gets the longest time (in seconds) that an expired session had been
   * alive.
   *
   * @return Longest time (in seconds) that an expired session had been
   * alive.
   */
  public int getSipApplicationSessionMaxAliveTime();


  /**
   * Sets the longest time (in seconds) that an expired session had been
   * alive.
   *
   * @param sessionMaxAliveTime Longest time (in seconds) that an expired
   * session had been alive.
   */
  public void setSipApplicationSessionMaxAliveTime(int sipApplicationSessionMaxAliveTime);

  /**
   * Gets the average time (in seconds) that expired sessions had been
   * alive.
   *
   * @return Average time (in seconds) that expired sessions had been
   * alive.
   */
  public int getSipApplicationSessionAverageAliveTime() ;


  /**
   * Sets the average time (in seconds) that expired sessions had been
   * alive.
   *
   * @param sessionAverageAliveTime Average time (in seconds) that expired
   * sessions had been alive.
   */
  public void setSipApplicationSessionAverageAliveTime(int sipApplicationSessionAverageAliveTime);
  
  /**
   * Gets the number of sessions that have expired.
   *
   * @return Number of sessions that have expired
   */
  public int getExpiredSipSessions();

  /**
   * Sets the number of sessions that have expired.
   *
   * @param expiredSessions Number of sessions that have expired
   */
  public void setExpiredSipSessions(int expiredSipSessions) ;
  
  /**
   * Gets the number of sessions that have expired.
   *
   * @return Number of sessions that have expired
   */
  public int getExpiredSipApplicationSessions();

  /**
   * Sets the number of sessions that have expired.
   *
   * @param expiredSessions Number of sessions that have expired
   */
  public void setExpiredSipApplicationSessions(int expiredSipApplicationSessions);
  
  /**
   * Gets the number of sip application sessions per seconds that have been created.
   *
   * @return number of sip application sessions per seconds that have been created.
   */
  public double getNumberOfSipApplicationSessionCreationPerSecond();
  
  /**
   * Gets the number of sip sessions per seconds that have been created.
   *
   * @return number of sip sessions per seconds that have been created.
   */
  public double getNumberOfSipSessionCreationPerSecond();
  
  /**
   * Update statistics
   */
  public void updateStats();
  
  public Object findSession(String id) throws IOException;
}
