/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
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
package org.mobicents.io.undertow.servlet.spec;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.sip.SipApplicationSession;

import io.undertow.server.session.Session;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.servlet.spec.HttpSessionImpl;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.ConvergedSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.undertow.UndertowSipManager;

/**
 * ConvergedSession implementor class which also wraps a HttpSessionImpl object. Based on
 * org.mobicents.servlet.sip.catalina.session.ConvergedStandardSession from mobicents sip-servlet-as7 package.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class ConvergedHttpSessionFacade implements ConvergedSession{
    private static final Logger logger = Logger.getLogger(ConvergedHttpSessionFacade.class);

    private transient ConvergedSessionDelegate convergedSessionDelegate = null;
    private transient HttpSessionImpl httpSessionDelegate = null;

    private ConvergedHttpSessionFacade(HttpSessionImpl httpSessionDelegate, SessionManager manager) {
        this.httpSessionDelegate = httpSessionDelegate;
        this.convergedSessionDelegate = new ConvergedSessionDelegate((UndertowSipManager)manager, this);
    }

    public static HttpSession forConvergedSession(final Session session, final ServletContext servletContext, final boolean newSession, SessionManager manager) {
        // forSession is called by privileged actions only so no need to do it again
        ServletRequestContext current = ServletRequestContext.current();
        if (current == null) {

            return new ConvergedHttpSessionFacade(HttpSessionImpl.forSession(session, servletContext, newSession), manager);
        } else {
            HttpSessionImpl httpSession = current.getSession();
            if (httpSession == null) {
                httpSession = HttpSessionImpl.forSession(session, servletContext, newSession);
                current.setSession(httpSession);
            } else {
                if(httpSession.getSession() != session) {
                    //in some rare cases it may be that there are two different service contexts involved in the one request
                    //in this case we just return a new session rather than using the thread local version
                    httpSession = HttpSessionImpl.forSession(session, servletContext, newSession);
                }
            }
            return new ConvergedHttpSessionFacade(httpSession, manager);
        }
    }

    @Override
    public String encodeURL(String url) {
        return convergedSessionDelegate.encodeURL(url);
    }

    @Override
    public String encodeURL(String relativePath, String scheme) {
        return convergedSessionDelegate.encodeURL(relativePath, scheme);
    }

    @Override
    public SipApplicationSession getApplicationSession() {
        return convergedSessionDelegate.getApplicationSession(true);
    }

    @Override
    public MobicentsSipApplicationSession getApplicationSession(boolean create) {
        return convergedSessionDelegate.getApplicationSession(create);
    }

    @Override
    public boolean isValidIntern() {
        return httpSessionDelegate.isInvalid(); //FIXME: kakonyii, no expiring flag in undertow session handling! || super.getSession().expiring);
    }

    @Override
    public void invalidate() {
        httpSessionDelegate.invalidate();
        MobicentsSipApplicationSession sipApplicationSession = convergedSessionDelegate.getApplicationSession(false);
        if(sipApplicationSession != null) {
            sipApplicationSession.tryToInvalidate();
        }
    }

    //kakonyii: set SipApplicationSession.lastAccessTime
    public void access() {
        MobicentsSipApplicationSession sipApplicationSession = convergedSessionDelegate.getApplicationSession(false);
        if(sipApplicationSession != null) {
            sipApplicationSession.access();
        }
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        logger.info("maxInactiveInterval = " + interval);
        httpSessionDelegate.setMaxInactiveInterval(interval);
    }

    public ConvergedSessionDelegate getConvergedSessionDelegate() {
        return convergedSessionDelegate;
    }

    @Override
    public long getCreationTime() {
        return httpSessionDelegate.getCreationTime();
    }

    @Override
    public String getId() {
        return httpSessionDelegate.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return httpSessionDelegate.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return httpSessionDelegate.getServletContext();
    }

    @Override
    public int getMaxInactiveInterval() {
        return httpSessionDelegate.getMaxInactiveInterval();
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return httpSessionDelegate.getSessionContext();
    }

    @Override
    public Object getAttribute(String name) {
        return httpSessionDelegate.getAttribute(name);
    }

    @Override
    public Object getValue(String name) {
        return httpSessionDelegate.getValue(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return httpSessionDelegate.getAttributeNames();
    }

    @Override
    public String[] getValueNames() {
        return httpSessionDelegate.getValueNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        httpSessionDelegate.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        httpSessionDelegate.putValue(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        httpSessionDelegate.removeAttribute(name);
    }

    @Override
    public void removeValue(String name) {
        httpSessionDelegate.removeValue(name);
    }

    @Override
    public boolean isNew() {
        return httpSessionDelegate.isNew();
    }
}
