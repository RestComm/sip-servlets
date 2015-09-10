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
package org.mobicents.io.undertow.servlet.handlers;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

import io.undertow.server.HttpHandler;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.SessionPersistenceManager;
import io.undertow.servlet.api.SessionPersistenceManager.PersistentSession;
import io.undertow.servlet.handlers.SessionRestoringHandler;

/**
 * This class extends io.undertow.servlet.SessionRestoringHandler.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class ConvergedSessionRestoringHandler extends SessionRestoringHandler{

    public ConvergedSessionRestoringHandler(String deploymentName, SessionManager sessionManager,
            ServletContext servletContext, HttpHandler next, SessionPersistenceManager sessionPersistenceManager) {
        super(deploymentName, sessionManager, /*FIXME*/((ConvergedServletContextImpl)servletContext).getDelegatedContext(), next, sessionPersistenceManager);
    }

    public void stop() {
        ClassLoader old = getTccl();
        try {
            //lets get access of superclass private fields using reflection:
            Field servletContextField = SessionRestoringHandler.class.getDeclaredField("servletContext");
            servletContextField.setAccessible(true);
            ServletContext servletContext = (ServletContext)servletContextField.get(this);
            servletContextField.setAccessible(false);

            Field sessionManagerField = SessionRestoringHandler.class.getDeclaredField("sessionManager");
            sessionManagerField.setAccessible(true);
            SessionManager sessionManager = (SessionManager) sessionManagerField.get(this);
            sessionManagerField.setAccessible(false);

            Field sessionPersistenceManagerField = SessionRestoringHandler.class.getDeclaredField("sessionPersistenceManager");
            sessionPersistenceManagerField.setAccessible(true);
            SessionPersistenceManager sessionPersistenceManager = (SessionPersistenceManager) sessionPersistenceManagerField.get(this);
            sessionPersistenceManagerField.setAccessible(false);

            Field deploymentNameField = SessionRestoringHandler.class.getDeclaredField("deploymentName");
            deploymentNameField.setAccessible(true);
            String deploymentName = (String) deploymentNameField.get(this);
            deploymentNameField.setAccessible(false);

            Field dataField = SessionRestoringHandler.class.getDeclaredField("data");
            dataField.setAccessible(true);
            Map<String, SessionPersistenceManager.PersistentSession> data = (Map<String, SessionPersistenceManager.PersistentSession>) dataField.get(this);
            dataField.setAccessible(false);

            setTccl(servletContext.getClassLoader());

            Field startedField = SessionRestoringHandler.class.getDeclaredField("started");
            startedField.setAccessible(true);
            startedField.set(this, false);
            startedField.setAccessible(false);

            final Map<String, SessionPersistenceManager.PersistentSession> objectData = new HashMap<>();
            for (String sessionId : sessionManager.getTransientSessions()) {
                Session session = sessionManager.getSession(sessionId);
                if (session != null) {
                    final HttpSessionEvent event = new HttpSessionEvent(SecurityActions.forSession(session, servletContext, false, sessionManager));
                    final Map<String, Object> sessionData = new HashMap<>();
                    for (String attr : session.getAttributeNames()) {
                        final Object attribute = session.getAttribute(attr);
                        sessionData.put(attr, attribute);
                        if (attribute instanceof HttpSessionActivationListener) {
                            ((HttpSessionActivationListener) attribute).sessionWillPassivate(event);
                        }
                    }
                    objectData.put(sessionId, new PersistentSession(new Date(session.getLastAccessedTime() + (session.getMaxInactiveInterval() * 1000)), sessionData));
                }
            }
            sessionPersistenceManager.persistSessions(deploymentName, objectData);
            data.clear();
        }catch(NoSuchFieldException | IllegalAccessException e){
            //FIXME: kakonyii: handle reflection errors or just omit them.
        }finally {
            setTccl(old);
        }
    }

    private ClassLoader getTccl() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        }
    }

    private void setTccl(final ClassLoader classLoader) {
        if (System.getSecurityManager() == null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    return null;
                }
            });
        }
    }
}
