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
package org.mobicents.io.undertow.server.session;

import io.undertow.UndertowLogger;
import io.undertow.UndertowMessages;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SecureRandomSessionIdGenerator;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionIdGenerator;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionListeners;
import io.undertow.server.session.SessionManager;
import io.undertow.util.ConcurrentDirectDeque;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.mobicents.io.undertow.servlet.spec.ConvergedSessionDelegate;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.xnio.XnioExecutor;
import org.xnio.XnioWorker;
/**
 * Implementation of io.undertow.server.session.SessionManager interface. This class based on io.undertow.server.session.InMemorySessionManager and extends it
 * with a ConvergedSessionDelegate object (in private class ConvergedInMemorySession) which helps set SipApplicationSession's lastAccessed when appropriate.
 *
 * @author kakonyi.istvan@alerant.hu
 *
*/
public class ConvergedInMemorySessionManager implements SessionManager{
    private volatile SessionIdGenerator sessionIdGenerator = new SecureRandomSessionIdGenerator();

    private final ConcurrentMap<String, ConvergedInMemorySession> sessions;

    private final SessionListeners sessionListeners = new SessionListeners();

    /**
     * 30 minute default
     */
    private volatile int defaultSessionTimeout = 30 * 60;

    private final int maxSize;

    private final ConcurrentDirectDeque<String> evictionQueue;

    private final String deploymentName;

    public ConvergedInMemorySessionManager(String deploymentName, int maxSessions) {
        this.deploymentName = deploymentName;
        this.sessions = new ConcurrentHashMap<>();
        this.maxSize = maxSessions;
        ConcurrentDirectDeque<String> evictionQueue = null;
        if (maxSessions > 0) {
            evictionQueue = ConcurrentDirectDeque.newInstance();
        }
        this.evictionQueue = evictionQueue;
    }

    public ConvergedInMemorySessionManager(String id) {
        this(id, -1);
    }
    @Override
    public String getDeploymentName() {
        return this.deploymentName;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        for (Map.Entry<String, ConvergedInMemorySession> session : sessions.entrySet()) {
            session.getValue().session.destroy();
            sessionListeners.sessionDestroyed(session.getValue().session, null, SessionListener.SessionDestroyedReason.UNDEPLOY);
        }
        sessions.clear();
    }

    @Override
    public Session createSession(HttpServerExchange serverExchange, SessionConfig sessionCookieConfig) {
        if (evictionQueue != null) {
            while (sessions.size() >= maxSize && !evictionQueue.isEmpty()) {
                String key = evictionQueue.poll();
                UndertowLogger.REQUEST_LOGGER.debugf("Removing session %s as max size has been hit", key);
                ConvergedInMemorySession toRemove = sessions.get(key);
                if (toRemove != null) {
                    toRemove.session.invalidate(null, SessionListener.SessionDestroyedReason.TIMEOUT); //todo: better reason
                }
            }
        }
        if (sessionCookieConfig == null) {
            throw UndertowMessages.MESSAGES.couldNotFindSessionCookieConfig();
        }
        String sessionID = sessionCookieConfig.findSessionId(serverExchange);
        int count = 0;
        while (sessionID == null) {
            sessionID = sessionIdGenerator.createSessionId();
            if(sessions.containsKey(sessionID)) {
                sessionID = null;
            }
            if(count++ == 100) {
                //this should never happen
                //but we guard against pathalogical session id generators to prevent an infinite loop
                throw UndertowMessages.MESSAGES.couldNotGenerateUniqueSessionId();
            }
        }
        Object evictionToken;
        if (evictionQueue != null) {
            evictionToken = evictionQueue.offerLastAndReturnToken(sessionID);
        } else {
            evictionToken = null;
        }
        final ConvergedSessionImpl session = new ConvergedSessionImpl(this, sessionID, sessionCookieConfig, serverExchange.getIoThread(), serverExchange.getConnection().getWorker(), evictionToken);
        ConvergedInMemorySession im = new ConvergedInMemorySession(session, defaultSessionTimeout);
        sessions.put(sessionID, im);
        sessionCookieConfig.setSessionId(serverExchange, session.getId());
        im.lastAccessed = System.currentTimeMillis();
        session.bumpTimeout();
        sessionListeners.sessionCreated(session, serverExchange);
        return session;
    }

    //this method should be called after session is created by sessionManager.createSession()
    //FIXME:kakonyii. sessionManager.createSession() is also called from  io.undertow.util.Sessions.getOrCreateSession(), so this method should be called from there also
    public void addConvergedSessionDeletegateToSession(SessionConfig sessionCookieConfig, HttpServerExchange serverExchange, ConvergedSessionDelegate convergedSessionDelegate){
        String sessionId = sessionCookieConfig.findSessionId(serverExchange);
        ConvergedInMemorySession sess = sessions.get(sessionId);
        if(sess!=null){
            sess.addConvergedSessionDelegate(convergedSessionDelegate);
        }

    }

    @Override
    public Session getSession(HttpServerExchange serverExchange, SessionConfig sessionCookieConfig) {
        String sessionId = sessionCookieConfig.findSessionId(serverExchange);
        return getSession(sessionId);
    }

    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        final ConvergedInMemorySession sess = sessions.get(sessionId);
        if (sess == null) {
            return null;
        } else {
            return sess.session;
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {
        sessionListeners.addSessionListener(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        sessionListeners.removeSessionListener(listener);
    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
        defaultSessionTimeout = timeout;
    }

    @Override
    public Set<String> getTransientSessions() {
        return getAllSessions();
    }

    @Override
    public Set<String> getActiveSessions() {
        return getAllSessions();
           }

    @Override
    public Set<String> getAllSessions() {
        return new HashSet<>(sessions.keySet());
    }

    /**
     * session implementation for the in memory session manager
     */
    private static class ConvergedSessionImpl implements Session {

        private final ConvergedInMemorySessionManager sessionManager;

        static volatile AtomicReferenceFieldUpdater<ConvergedSessionImpl, Object> evictionTokenUpdater;
        static {
            //this is needed in case there is unprivileged code on the stack
            //it needs to delegate to the createTokenUpdater() method otherwise the creation will fail
            //as the inner class cannot access the member
            evictionTokenUpdater = AccessController.doPrivileged(new PrivilegedAction<AtomicReferenceFieldUpdater<ConvergedSessionImpl, Object>>() {
                @Override
                public AtomicReferenceFieldUpdater<ConvergedSessionImpl, Object> run() {
                    return createTokenUpdater();
                }
            });
        }

        private static AtomicReferenceFieldUpdater<ConvergedSessionImpl, Object> createTokenUpdater() {
            return AtomicReferenceFieldUpdater.newUpdater(ConvergedSessionImpl.class, Object.class, "evictionToken");
        }


        private String sessionId;
        private volatile Object evictionToken;
        private final SessionConfig sessionCookieConfig;
        private volatile long expireTime = -1;

        final XnioExecutor executor;
        final XnioWorker worker;

        XnioExecutor.Key timerCancelKey;

        Runnable cancelTask = new Runnable() {
            @Override
            public void run() {
                worker.execute(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        if(currentTime >= expireTime) {
                            invalidate(null, SessionListener.SessionDestroyedReason.TIMEOUT);
                        } else {
                            timerCancelKey = executor.executeAfter(cancelTask, expireTime - currentTime, TimeUnit.MILLISECONDS);
                        }
                    }
                });
            }
        };

        private ConvergedSessionImpl(ConvergedInMemorySessionManager sessionManager, final String sessionId, final SessionConfig sessionCookieConfig, final XnioExecutor executor, final XnioWorker worker, final Object evictionToken) {
            this.sessionManager = sessionManager;
            this.sessionId = sessionId;
            this.sessionCookieConfig = sessionCookieConfig;
            this.executor = executor;
            this.worker = worker;
            this.evictionToken = evictionToken;
        }

        synchronized void bumpTimeout() {
            final int maxInactiveInterval = getMaxInactiveInterval();
            if (maxInactiveInterval > 0) {
                long newExpireTime = System.currentTimeMillis() + (maxInactiveInterval * 1000);
                if(timerCancelKey != null && (newExpireTime < expireTime)) {
                    // We have to re-schedule as the new maxInactiveInterval is lower than the old one
                    if (!timerCancelKey.remove()) {
                        return;
                    }
                    timerCancelKey = null;
                }
                expireTime = newExpireTime;
                if(timerCancelKey == null) {
                    //+1 second, to make sure that the time has actually expired
                    //we don't re-schedule every time, as it is expensive
                    //instead when it expires we check if the timeout has been bumped, and if so we re-schedule
                    timerCancelKey = executor.executeAfter(cancelTask, (maxInactiveInterval * 1000) + 1, TimeUnit.MILLISECONDS);
                }
            }
            if (evictionToken != null) {
                Object token = evictionToken;
                if (evictionTokenUpdater.compareAndSet(this, token, null)) {
                    sessionManager.evictionQueue.removeToken(token);
                    this.evictionToken = sessionManager.evictionQueue.offerLastAndReturnToken(sessionId);
                }
            }
        }


        @Override
        public String getId() {
            return sessionId;
        }

        @Override
        public void requestDone(final HttpServerExchange serverExchange) {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess != null) {
                sess.lastAccessed = System.currentTimeMillis();
                if(sess.convergedSessionDelegate!=null){
                    MobicentsSipApplicationSession appSession = sess.convergedSessionDelegate.getApplicationSession(false);
                    if(appSession!=null){
                        appSession.access();
                    }
                }
            }
        }

        @Override
        public long getCreationTime() {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            return sess.creationTime;
        }

        @Override
        public long getLastAccessedTime() {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            return sess.lastAccessed;
        }

        @Override
        public void setMaxInactiveInterval(final int interval) {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            sess.maxInactiveInterval = interval;
            bumpTimeout();
        }

        @Override
        public int getMaxInactiveInterval() {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            return sess.maxInactiveInterval;
        }

        @Override
        public Object getAttribute(final String name) {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            bumpTimeout();
            return sess.attributes.get(name);
        }

        @Override
        public Set<String> getAttributeNames() {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            bumpTimeout();
            return sess.attributes.keySet();
        }

        @Override
        public Object setAttribute(final String name, final Object value) {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            final Object existing = sess.attributes.put(name, value);
            if (existing == null) {
                sessionManager.sessionListeners.attributeAdded(sess.session, name, value);
            } else {
               sessionManager.sessionListeners.attributeUpdated(sess.session, name, value, existing);
            }
            bumpTimeout();
            return existing;
        }

        @Override
        public Object removeAttribute(final String name) {
            final ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                throw UndertowMessages.MESSAGES.sessionNotFound(sessionId);
            }
            final Object existing = sess.attributes.remove(name);
            sessionManager.sessionListeners.attributeRemoved(sess.session, name, existing);
            bumpTimeout();
            return existing;
        }

        @Override
        public void invalidate(final HttpServerExchange exchange) {
            invalidate(exchange, SessionListener.SessionDestroyedReason.INVALIDATED);
        }

        synchronized void invalidate(final HttpServerExchange exchange, SessionListener.SessionDestroyedReason reason) {
            if (timerCancelKey != null) {
                timerCancelKey.remove();
            }
            ConvergedInMemorySession sess = sessionManager.sessions.get(sessionId);
            if (sess == null) {
                if (reason == SessionListener.SessionDestroyedReason.INVALIDATED) {
                    throw UndertowMessages.MESSAGES.sessionAlreadyInvalidated();
                }
                return;
            }
            sessionManager.sessionListeners.sessionDestroyed(sess.session, exchange, reason);
            sessionManager.sessions.remove(sessionId);
            if (exchange != null) {
                sessionCookieConfig.clearSession(exchange, this.getId());
            }
        }

        @Override
        public SessionManager getSessionManager() {
            return sessionManager;
        }

        @Override
        public String changeSessionId(final HttpServerExchange exchange, final SessionConfig config) {
            final String oldId = sessionId;
            final ConvergedInMemorySession sess = sessionManager.sessions.get(oldId);
            String newId = sessionManager.sessionIdGenerator.createSessionId();
            this.sessionId = newId;
            sessionManager.sessions.put(newId, sess);
            sessionManager.sessions.remove(oldId);
            config.setSessionId(exchange, this.getId());
            sessionManager.sessionListeners.sessionIdChanged(sess.session, oldId);
            return newId;
        }

        private synchronized void destroy() {
            if (timerCancelKey != null) {
                timerCancelKey.remove();
            }
            cancelTask = null;
        }

    }

    /**
     * class that holds the real session data
     */
    private static class ConvergedInMemorySession {

        final ConvergedSessionImpl session;
        transient ConvergedSessionDelegate convergedSessionDelegate;

        ConvergedInMemorySession(final ConvergedSessionImpl session, int maxInactiveInterval) {
            this.session = session;
            creationTime = lastAccessed = System.currentTimeMillis();
            this.maxInactiveInterval = maxInactiveInterval;
        }

        void addConvergedSessionDelegate(ConvergedSessionDelegate convergedSessionDelegate){
            this.convergedSessionDelegate = convergedSessionDelegate;
        }

        final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
        volatile long lastAccessed;
        final long creationTime;
        volatile int maxInactiveInterval;
    }
}
