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

import javax.management.ObjectName;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.tomcat.util.modeler.Registry;
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
     * The descriptive name of this Manager implementation (for logging).
     */
    protected final static String name = "SipStandardManager";

    
	/**
	 * 
	 */
	public SipStandardManager() {
		super();
		sipManagerDelegate = new SipStandardManagerDelegate();
	}

	@Override
	public void init() {
		if( initialized ) return;	                
	        
        if(oname==null && this.getContainer() instanceof SipContext) {
            SipContext ctx=(SipContext)this.getContainer();
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

		super.init();
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

        // De-register from the old Container (if any)
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);

        this.container = container;
		sipManagerDelegate.setContainer(container);

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
	
	
	// JMX Statistics
	/**
	 * Return descriptive information about this Manager implementation and the
	 * corresponding version number, in the format
	 * <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	public String getInfo() {
		return (info);
	}

	/**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {
        return (name);
    }
	
	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipSessions() {
		return (this.sipManagerDelegate.maxActiveSipSessions);
	}

	/**
	 * Set the maximum number of actives Sip Sessions allowed, or -1 for no
	 * limit.
	 * 
	 * @param max
	 *            The new maximum number of sip sessions
	 */
	public void setMaxActiveSipSessions(int max) {
		int oldMaxActiveSipSessions = this.sipManagerDelegate.maxActiveSipSessions;
		this.sipManagerDelegate.maxActiveSipSessions = max;
		support.firePropertyChange("maxActiveSipSessions", new Integer(
				oldMaxActiveSipSessions), new Integer(
				this.sipManagerDelegate.maxActiveSipSessions));
	}

	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipApplicationSessions() {
		return (this.sipManagerDelegate.maxActiveSipApplicationSessions);
	}

	/**
	 * Set the maximum number of actives Sip Application Sessions allowed, or -1
	 * for no limit.
	 * 
	 * @param max
	 *            The new maximum number of sip application sessions
	 */
	public void setMaxActiveSipApplicationSessions(int max) {
		int oldMaxActiveSipApplicationSessions = this.sipManagerDelegate.maxActiveSipApplicationSessions;
		this.sipManagerDelegate.maxActiveSipApplicationSessions = max;
		support
				.firePropertyChange(
						"maxActiveSipApplicationSessions",
						new Integer(oldMaxActiveSipApplicationSessions),
						new Integer(
								this.sipManagerDelegate.maxActiveSipApplicationSessions));
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipSessions() {
		return sipManagerDelegate.rejectedSipSessions;
	}

	public void setRejectedSipSessions(int rejectedSipSessions) {
		this.sipManagerDelegate.rejectedSipSessions = rejectedSipSessions;
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipApplicationSessions() {
		return sipManagerDelegate.rejectedSipApplicationSessions;
	}

	public void setRejectedSipApplicationSessions(
			int rejectedSipApplicationSessions) {
		this.sipManagerDelegate.rejectedSipApplicationSessions = rejectedSipApplicationSessions;
	}

	public void setSipSessionCounter(int sipSessionCounter) {
		this.sipManagerDelegate.sipSessionCounter = sipSessionCounter;
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipSessionCounter() {
		return sipManagerDelegate.sipSessionCounter;
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipSessions() {
		return sipManagerDelegate.sipSessions.size();
	}

	/**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
	public int getSipSessionMaxAliveTime() {
		return sipManagerDelegate.sipSessionMaxAliveTime;
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
		this.sipManagerDelegate.sipSessionMaxAliveTime = sipSessionMaxAliveTime;
	}

	/**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
	public int getSipSessionAverageAliveTime() {
		return sipManagerDelegate.sipSessionAverageAliveTime;
	}

	/**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
	public void setSipSessionAverageAliveTime(int sipSessionAverageAliveTime) {
		this.sipManagerDelegate.sipSessionAverageAliveTime = sipSessionAverageAliveTime;
	}

	public void setSipApplicationSessionCounter(int sipApplicationSessionCounter) {
		this.sipManagerDelegate.sipApplicationSessionCounter = sipApplicationSessionCounter;
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipApplicationSessionCounter() {
		return sipManagerDelegate.sipApplicationSessionCounter;
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipApplicationSessions() {
		return sipManagerDelegate.sipApplicationSessions.size();
	}

	/**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
	public int getSipApplicationSessionMaxAliveTime() {
		return sipManagerDelegate.sipApplicationSessionMaxAliveTime;
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
		this.sipManagerDelegate.sipApplicationSessionMaxAliveTime = sipApplicationSessionMaxAliveTime;
	}

	/**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
	public int getSipApplicationSessionAverageAliveTime() {
		return sipManagerDelegate.sipApplicationSessionAverageAliveTime;
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
		this.sipManagerDelegate.sipApplicationSessionAverageAliveTime = sipApplicationSessionAverageAliveTime;
	}

	/**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
	public int getExpiredSipSessions() {
		return sipManagerDelegate.expiredSipSessions;
	}

	/**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
	public void setExpiredSipSessions(int expiredSipSessions) {
		this.sipManagerDelegate.expiredSipSessions = expiredSipSessions;
	}

	/**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
	public int getExpiredSipApplicationSessions() {
		return sipManagerDelegate.expiredSipApplicationSessions;
	}

	/**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
	public void setExpiredSipApplicationSessions(
			int expiredSipApplicationSessions) {
		this.sipManagerDelegate.expiredSipApplicationSessions = expiredSipApplicationSessions;
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
	
}
