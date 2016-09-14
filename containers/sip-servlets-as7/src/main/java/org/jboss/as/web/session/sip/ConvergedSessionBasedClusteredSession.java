/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.web.session.sip;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;

import org.apache.catalina.security.SecurityUtil;
import org.apache.log4j.Logger;
import org.jboss.as.clustering.web.OutgoingSessionGranularitySessionData;
import org.jboss.as.web.session.ExposedSessionBasedClusteredSession;
import org.mobicents.servlet.sip.catalina.session.ConvergedSessionDelegate;
import org.mobicents.servlet.sip.catalina.session.ConvergedSessionFacade;
import org.mobicents.servlet.sip.core.session.ConvergedSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;

/**
 * Extension of the Jboss SessionBasedClusteredSession class so that applications
 * are able to cast this session implementation to javax.servlet.sip.ConvergedHttpSession interface.
 * 
 * Based on SessionBasedClusteredSession JBOSS AS 5.1.0.GA Tag
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * @author posfai.gergely@ext.alerant.hu
 */
public class ConvergedSessionBasedClusteredSession extends
		ExposedSessionBasedClusteredSession implements ConvergedSession {

	private static transient Logger logger = Logger.getLogger(ConvergedSessionBasedClusteredSession.class);
	
	/**
     * The facade associated with this session.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    protected transient ConvergedSessionFacade facade = null;
    
	private ConvergedSessionDelegate convergedSessionDelegate = null;
	
	/**
	 * @param manager
	 * @param sipNetworkInterfaceManager 
	 */
	public ConvergedSessionBasedClusteredSession(ClusteredSipSessionManager<OutgoingSessionGranularitySessionData> manager) {
		super(manager);
		convergedSessionDelegate = new ConvergedSessionDelegate(manager, this);
	}
	
	@Override
	public HttpSession getSession() {
        if (facade == null){
            if (SecurityUtil.isPackageProtectionEnabled()){
                final ConvergedSession fsession = this;
                facade = (ConvergedSessionFacade)AccessController.doPrivileged(new PrivilegedAction<ConvergedSessionFacade>(){
                    public ConvergedSessionFacade run(){
                        return new ConvergedSessionFacade(fsession);
                    }
                });
            } else {
                facade = new ConvergedSessionFacade(this);
            }
        }
        return (facade);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String)
	 */
	public String encodeURL(String url) {
		return convergedSessionDelegate.encodeURL(url);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String, java.lang.String)
	 */
	public String encodeURL(String relativePath, String scheme) {
		return convergedSessionDelegate.encodeURL(relativePath, scheme);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {		
		return convergedSessionDelegate.getApplicationSession(true);
	}
	
	public MobicentsSipApplicationSession getApplicationSession(boolean create) {		
		return convergedSessionDelegate.getApplicationSession(create);
	}
	
	public boolean isValidIntern() {
		return isValid(false);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		MobicentsSipApplicationSession sipApplicationSession = convergedSessionDelegate.getApplicationSession(false);
		if(sipApplicationSession != null) {
			sipApplicationSession.tryToInvalidate();
		}
	}
	
	@Override
	public void access() {		
		super.access();
		MobicentsSipApplicationSession sipApplicationSession = convergedSessionDelegate.getApplicationSession(false);
		if(sipApplicationSession != null) {
			sipApplicationSession.access();
		}
	}
	
	@Override
	public boolean isOutdated() {
		// if creationTime == 0 we've neither been synced with the
		// distributed cache nor had creation time set (i.e. brand new session)
		return super.isOutdated() || this.getCreationTimeInternal() == 0;
	} 
}
