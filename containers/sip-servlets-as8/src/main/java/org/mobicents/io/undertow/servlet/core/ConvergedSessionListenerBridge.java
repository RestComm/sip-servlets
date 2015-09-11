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
package org.mobicents.io.undertow.servlet.core;

import java.security.AccessController;
import java.util.HashSet;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.core.ApplicationListeners;
import io.undertow.servlet.core.SessionListenerBridge;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.servlet.spec.HttpSessionImpl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * This class extends io.undertow.servlet.core.SessionListenerBridge to create ConvergedHttpSessions instead of plain HttpSessions.
 *
 * @author kakonyi.istvan@alerant.hu
 */
public class ConvergedSessionListenerBridge extends SessionListenerBridge{
    private final ThreadSetupAction threadSetup;
    private final ApplicationListeners applicationListeners;
    private final ServletContext servletContext;
    private final SessionManager manager;

    public ConvergedSessionListenerBridge(final ThreadSetupAction threadSetup, final ApplicationListeners applicationListeners,
            final ServletContext servletContext, final SessionManager manager) {
        super(threadSetup, applicationListeners, servletContext);
        this.threadSetup = threadSetup;
        this.applicationListeners = applicationListeners;
        this.servletContext = servletContext;
        this.manager = manager;
    }

    @Override
    public void sessionCreated(final Session session, final HttpServerExchange exchange) {
        final HttpSession httpSession = SecurityActions.forSession(session, servletContext, true, manager);
        applicationListeners.sessionCreated(httpSession);
    }

    @Override
    public void sessionDestroyed(final Session session, final HttpServerExchange exchange, final SessionDestroyedReason reason) {
        ThreadSetupAction.Handle handle = null;
        try {
            final HttpSession httpSession = SecurityActions.forSession(session, servletContext, false, manager);
            if (reason == SessionDestroyedReason.TIMEOUT) {
                handle = threadSetup.setup(exchange);
            }
            applicationListeners.sessionDestroyed(httpSession);
            //we make a defensive copy here, as there is no guarantee that the underlying session map
            //is a concurrent map, and as a result a concurrent modification exception may be thrown
            HashSet<String> names = new HashSet<>(session.getAttributeNames());
            for(String attribute : names) {
                session.removeAttribute(attribute);
            }
        } finally {
            if (handle != null) {
                handle.tearDown();
            }
            ServletRequestContext current = SecurityActions.currentServletRequestContext();
            Session underlying = null;
            if(current != null && current.getSession() != null) {
                if(System.getSecurityManager() == null) {
                    underlying = current.getSession().getSession();
                } else {
                    underlying = AccessController.doPrivileged(new HttpSessionImpl.UnwrapSessionAction(current.getSession()));
                }
            }

            if (current != null && underlying == session) {
                current.setSession(null);
            }
        }
    }

    @Override
    public void attributeAdded(final Session session, final String name, final Object value) {
        if(name.startsWith(IO_UNDERTOW)) {
            return;
        }
        final HttpSession httpSession = SecurityActions.forSession(session, servletContext, false, manager);
        applicationListeners.httpSessionAttributeAdded(httpSession, name, value);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(httpSession, name, value));
        }
    }

    @Override
    public void attributeUpdated(final Session session, final String name, final Object value, final Object old) {
        if(name.startsWith(IO_UNDERTOW)) {
            return;
        }
        final HttpSession httpSession = SecurityActions.forSession(session, servletContext, false, manager);
        if (old != value) {
            if (old instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) old).valueUnbound(new HttpSessionBindingEvent(httpSession, name, old));
            }
            applicationListeners.httpSessionAttributeReplaced(httpSession, name, old);
        }
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(httpSession, name, value));
        }
    }

    @Override
    public void attributeRemoved(final Session session, final String name, final Object old) {
        if(name.startsWith(IO_UNDERTOW)) {
            return;
        }
        final HttpSession httpSession = SecurityActions.forSession(session, servletContext, false, manager);
        if (old != null) {
            applicationListeners.httpSessionAttributeRemoved(httpSession, name, old);
            if (old instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) old).valueUnbound(new HttpSessionBindingEvent(httpSession, name, old));
            }
        }
    }
}
