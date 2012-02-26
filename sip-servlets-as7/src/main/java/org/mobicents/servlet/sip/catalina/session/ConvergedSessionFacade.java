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

package org.mobicents.servlet.sip.catalina.session;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.sip.SipApplicationSession;

import org.mobicents.servlet.sip.core.session.ConvergedSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;

/**
 * Facade for the ConvergedSession object.
 * This code was mostly taken from the Tomcat StandardSessionFacade class
 * @author Jean Deruelle
 *
 */
public class ConvergedSessionFacade implements ConvergedSession {
	ConvergedSession session;

    /**
     * Construct a new session facade.
     */
    public ConvergedSessionFacade(ConvergedSession session) {
        super();
        this.session = session;
    }
    
    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getCreationTime()
     */
    public long getCreationTime() {
        return session.getCreationTime();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getId()
     */
    public String getId() {
        return session.getId();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getLastAccessedTime()
     */
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getServletContext()
     */
    public ServletContext getServletContext() {
        // FIXME : Facade this object ?
        return session.getServletContext();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
     */
    public void setMaxInactiveInterval(int interval) {
        session.setMaxInactiveInterval(interval);
    }
    
    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
     */
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getSessionContext()
     */
    public HttpSessionContext getSessionContext() {
        return session.getSessionContext();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
     */
    public Object getValue(String name) {
        return session.getAttribute(name);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return session.getAttributeNames();
    }
    
    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#getValueNames()
     */
    public String[] getValueNames() {
        return session.getValueNames();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
     */
    public void putValue(String name, Object value) {
        session.setAttribute(name, value);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
     */
    public void removeValue(String name) {
        session.removeAttribute(name);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#invalidate()
     */
    public void invalidate() {
        session.invalidate();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpSession#isNew()
     */
    public boolean isNew() {
        return session.isNew();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String)
     */
	public String encodeURL(String url) {	
		return session.encodeURL(url);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String, java.lang.String)
	 */
	public String encodeURL(String relativePath, String scheme) {
		return session.encodeURL(relativePath, scheme);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {
		return session.getApplicationSession();
	}

	public MobicentsSipApplicationSession getApplicationSession(boolean create) {		
		return session.getApplicationSession(create);
	}

	public boolean isValidIntern() {
		return session.isValidIntern();
	}
}
