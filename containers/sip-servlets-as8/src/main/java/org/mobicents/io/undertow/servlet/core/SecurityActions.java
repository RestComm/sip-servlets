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

import io.undertow.server.HttpHandler;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.core.CompositeThreadSetupAction;
import io.undertow.servlet.handlers.ServletInitialHandler;
import io.undertow.servlet.handlers.ServletPathMatches;
import io.undertow.servlet.handlers.ServletRequestContext;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.mobicents.io.undertow.servlet.handlers.ConvergedServletInitialHandler;
import org.mobicents.io.undertow.servlet.spec.ConvergedHttpSessionFacade;
import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

/**
 * This class is based on protected class io.undertow.servlet.core.SecurityActions.
 *
 * @author kakonyi.istvan@alerant.hu
 */
class SecurityActions {

    static HttpSession forSession(final Session session, final ServletContext servletContext, final boolean newSession, final SessionManager manager) {
        if (System.getSecurityManager() == null) {
            return ConvergedHttpSessionFacade.forConvergedSession(session, servletContext, newSession, manager);
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<HttpSession>() {
                @Override
                public HttpSession run() {
                    return ConvergedHttpSessionFacade.forConvergedSession(session, servletContext, newSession, manager);
                }
            });
        }
    }

    static ServletRequestContext currentServletRequestContext() {
        if (System.getSecurityManager() == null) {
            return ServletRequestContext.current();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ServletRequestContext>() {
                @Override
                public ServletRequestContext run() {
                    return ServletRequestContext.current();
                }
            });
        }
    }

    static String getSystemProperty(final String prop) {
        if (System.getSecurityManager() == null) {
           return System.getProperty(prop);
        } else {
            return (String) AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    return System.getProperty(prop);
                }
            });
        }
    }

    static ServletInitialHandler createServletInitialHandler(final ServletPathMatches paths, final HttpHandler next, final CompositeThreadSetupAction setupAction, final ServletContext servletContext) {
        if (System.getSecurityManager() == null) {
            return new ConvergedServletInitialHandler(paths, next, setupAction, /*TODO*/(ConvergedServletContextImpl) servletContext);
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ServletInitialHandler>() {
                @Override
                public ServletInitialHandler run() {
                    return new ConvergedServletInitialHandler(paths, next, setupAction, /*TODO*/(ConvergedServletContextImpl) servletContext);
                }
            });
        }
    }

}
