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

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;

import org.apache.catalina.security.SecurityUtil;

/**
 * Extension of the Tomcat StandardSession class so that applications
 * are able to cast session to javax.servlet.sip.ConvergedHttpSession interface.
 * 
 * @author Jean Deruelle
 *
 */
public class ConvergedStandardSession 
		extends org.apache.catalina.session.StandardSession 
		implements ConvergedSession {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * The facade associated with this session.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    protected transient ConvergedSessionFacade convergedFacade = null;
    
    private transient ConvergedSessionDelegate convergedSessionDelegate = null;
	/**
	 * 
	 * @param sessionManager
	 */
	public ConvergedStandardSession(SipManager manager) {
		super(manager);
		convergedSessionDelegate = new ConvergedSessionDelegate(manager, this);
	}
	
	@Override
	public HttpSession getSession() {
        if (convergedFacade == null){
            if (SecurityUtil.isPackageProtectionEnabled()){
                final ConvergedSession fsession = this;
                convergedFacade = (ConvergedSessionFacade)AccessController.doPrivileged(new PrivilegedAction<ConvergedSessionFacade>(){
                    public ConvergedSessionFacade run(){
                        return new ConvergedSessionFacade(fsession);
                    }
                });
            } else {
                convergedFacade = new ConvergedSessionFacade(this);
            }
        }
        return (convergedFacade);
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
	
	public boolean isValid() {
		return isValidInternal();
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

}
