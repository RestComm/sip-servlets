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
package org.mobicents.servlet.sip.startup;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.spec.ServletContextImpl;
import io.undertow.servlet.spec.SessionCookieConfigImpl;
import io.undertow.util.AttachmentKey;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpSession;

import org.apache.jasper.Constants;
import org.apache.jasper.security.SecurityUtil;
import org.mobicents.io.undertow.server.session.ConvergedInMemorySessionManager;
import org.mobicents.io.undertow.servlet.spec.ConvergedHttpSessionFacade;
import org.mobicents.servlet.sip.core.session.SipRequestDispatcher;
import org.mobicents.servlet.sip.undertow.SipContextImpl;
import org.mobicents.servlet.sip.undertow.SipServletImpl;

/**
 * Facade object which masks the internal <code>ApplicationContext</code> object from the web application.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Id: ApplicationContextFacade.java 1002556 2010-09-29 10:07:10Z markt $
 *
 *          This class is based on org.mobicents.servlet.sip.startup.ConvergedApplicationContextFacade class from
 *          sip-servlet-as7 project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public final class ConvergedServletContextImpl implements ServletContext {

    // ---------------------------------------------------------- Attributes
    /**
     * Cache Class object used for reflection.
     */
    private HashMap<String, Class<?>[]> classCache;

    /**
     * Cache method object.
     */
    private HashMap<String, Method> objectCache;

    private final AttachmentKey<ConvergedHttpSessionFacade> sessionAttachmentKey = AttachmentKey.create(ConvergedHttpSessionFacade.class);

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this class, associated with the specified Context instance.
     *
     * @param context The associated Context instance
     */
    public ConvergedServletContextImpl(ServletContextImpl context) {

        this.context = context;
        this.classCache = new HashMap<String, Class<?>[]>();
        this.objectCache = new HashMap<String, Method>();
        initClassCache();
    }

    public void addSipContext(SipContextImpl sipContext){
        this.sipContext = sipContext;
    }

    private void initClassCache() {
        Class<?>[] clazz = new Class[] { String.class };
        classCache.put("getContext", clazz);
        classCache.put("getMimeType", clazz);
        classCache.put("getResourcePaths", clazz);
        classCache.put("getResource", clazz);
        classCache.put("getResourceAsStream", clazz);
        classCache.put("getRequestDispatcher", clazz);
        classCache.put("getNamedDispatcher", clazz);
        classCache.put("getServlet", clazz);
        classCache.put("setInitParameter", new Class[] { String.class, String.class });
        classCache.put("createServlet", new Class[] { Class.class });
        classCache.put("addServlet", new Class[] { String.class, String.class });
        classCache.put("createFilter", new Class[] { Class.class });
        classCache.put("addFilter", new Class[] { String.class, String.class });
        classCache.put("createListener", new Class[] { Class.class });
        classCache.put("addListener", clazz);
        classCache.put("getFilterRegistration", clazz);
        classCache.put("getServletRegistration", clazz);
        classCache.put("getInitParameter", clazz);
        classCache.put("setAttribute", new Class[] { String.class, Object.class });
        classCache.put("removeAttribute", clazz);
        classCache.put("getRealPath", clazz);
        classCache.put("getAttribute", clazz);
        classCache.put("log", clazz);
        classCache.put("setSessionTrackingModes", new Class[] { EnumSet.class });
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * Wrapped application context.
     */
    private ServletContextImpl context = null;
    private SipContextImpl sipContext = null;

    // ------------------------------------------------- ServletContextImpl Methods
    public void initDone() {
        context.initDone();
    }

    public HttpSession getSession(final String sessionId) {
        final SessionManager sessionManager = context.getDeployment().getSessionManager();
        Session session = sessionManager.getSession(sessionId);
        if (session != null) {
            return SecurityActions.forSession(session, this, false, sessionManager);
        }
        return null;
    }

    public HttpSession getSession(final ServletContextImpl originalServletContext, final HttpServerExchange exchange, boolean create) {
        SessionConfig c = originalServletContext.getSessionConfig();
        ConvergedHttpSessionFacade httpSession = exchange.getAttachment(sessionAttachmentKey);
        if (httpSession != null && httpSession.isValidIntern()) {
            exchange.removeAttachment(sessionAttachmentKey);
            httpSession = null;
        }
        if (httpSession == null) {
            final SessionManager sessionManager = context.getDeployment().getSessionManager();
            Session session = sessionManager.getSession(exchange, c);
            if (session != null) {
                httpSession = (ConvergedHttpSessionFacade) SecurityActions.forSession(session, this, false, sessionManager);
                exchange.putAttachment(sessionAttachmentKey, httpSession);
            } else if (create) {

                String existing = c.findSessionId(exchange);
                if (originalServletContext != this.context) {
                    //this is a cross context request
                    //we need to make sure there is a top level session
                    originalServletContext.getSession(originalServletContext, exchange, true);
                } else if (existing != null) {
                    c.clearSession(exchange, existing);
                }

                final Session newSession = sessionManager.createSession(exchange, c);
                httpSession = (ConvergedHttpSessionFacade) SecurityActions.forSession(newSession, this, true, sessionManager);
                //call access after creation to set LastAccessTime at sipAppSession.
                httpSession.access();
                //add delegate to InMemorySession to call sipAppSession.access(), when necessary:
                ((ConvergedInMemorySessionManager)sessionManager).addConvergedSessionDeletegateToSession(c, exchange, httpSession.getConvergedSessionDelegate());

                exchange.putAttachment(sessionAttachmentKey, httpSession);
            }
        }
        return httpSession;
    }

    public void updateSessionAccessTime(final HttpServerExchange exchange) {
        context.updateSessionAccessTime(exchange);
    }

    public Deployment getDeployment() {
        return context.getDeployment();
    }

    public SessionConfig getSessionConfig() {
        return context.getSessionConfig();
    }

    public void destroy() {
        context.destroy();
    }

    public void setDefaultSessionTrackingModes(HashSet<SessionTrackingMode> sessionTrackingModes) {
        context.setDefaultSessionTrackingModes(sessionTrackingModes);
    }

    // ------------------------------------------------- ServletContext Methods

    @Override
    public String getContextPath() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getContextPath", null);
        } else {
            return context.getContextPath();
        }
    }

    @Override
    public ServletContext getContext(String uripath) {
        ServletContext theContext = null;
        if (SecurityUtil.isPackageProtectionEnabled()) {
            theContext = (ServletContext) doPrivileged("getContext", new Object[] { uripath });
        } else {
            theContext = context.getContext(uripath);
        }
        if ((theContext != null) && (theContext instanceof ServletContextImpl)) {
            //theContext = this.context;
        }
        return theContext;
    }

    @Override
    public int getMajorVersion() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return ((Integer) doPrivileged("getMajorVersion", null)).intValue();
        } else {
            return context.getMajorVersion();
        }
    }

    @Override
    public int getMinorVersion() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return ((Integer) doPrivileged("getMinorVersion", null)).intValue();
        } else {
            return context.getMinorVersion();
        }
    }

    @Override
    public int getEffectiveMajorVersion() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return ((Integer) doPrivileged("getEffectiveMajorVersion", null)).intValue();
        } else {
            return context.getEffectiveMajorVersion();
        }
    }

    @Override
    public int getEffectiveMinorVersion() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return ((Integer) doPrivileged("getEffectiveMinorVersion", null)).intValue();
        } else {
            return context.getEffectiveMinorVersion();
        }
    }

    @Override
    public String getMimeType(String file) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getMimeType", new Object[] { file });
        } else {
            return context.getMimeType(file);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Set<String> getResourcePaths(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Set<String>) doPrivileged("getResourcePaths", new Object[] { path });
        } else {
            return context.getResourcePaths(path);
        }
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (Constants.IS_SECURITY_ENABLED) {
            try {
                return (URL) invokeMethod(context, "getResource", new Object[] { path });
            } catch (Throwable t) {
                if (t instanceof MalformedURLException) {
                    throw (MalformedURLException) t;
                }
                return null;
            }
        } else {
            return context.getResource(path);
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (InputStream) doPrivileged("getResourceAsStream", new Object[] { path });
        } else {
            return context.getResourceAsStream(path);
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (RequestDispatcher) doPrivileged("getRequestDispatcher", new Object[] { path });
        } else {
            return context.getRequestDispatcher(path);
        }
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        // Validate the name argument
        if (name == null)
            return (null);

        // Create and return a corresponding request dispatcher
        Servlet servlet = (Servlet) sipContext.findSipServletByName(name);

        if (servlet == null)
            return context.getNamedDispatcher(name);
        // return (null);

        if (servlet instanceof SipServletImpl) {
            return new SipRequestDispatcher((SipServletImpl) servlet);
        } else {
            return context.getNamedDispatcher(name);
        }
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public Servlet getServlet(String name) throws ServletException {
        //FIXME: kakonyii: also return sip servlets?
        if (SecurityUtil.isPackageProtectionEnabled()) {
            try {
                return (Servlet) invokeMethod(context, "getServlet", new Object[] { name });
            } catch (Throwable t) {
                handleThrowable(t);
                if (t instanceof ServletException) {
                    throw (ServletException) t;
                }
                return null;
            }
        } else {
            return context.getServlet(name);
        }
    }

    /**
     * @deprecated
     */
    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration<Servlet>) doPrivileged("getServlets", null);
        } else {
            return context.getServlets();
        }
    }

    /**
     * @deprecated
     */
    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    @Deprecated
    public Enumeration<String> getServletNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration<String>) doPrivileged("getServletNames", null);
        } else {
            return context.getServletNames();
        }
    }

    @Override
    public void log(String msg) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Object[] { msg });
        } else {
            context.log(msg);
        }
    }

    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public void log(Exception exception, String msg) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Class[] { Exception.class, String.class }, new Object[] { exception, msg });
        } else {
            context.log(exception, msg);
        }
    }

    @Override
    public void log(String message, Throwable throwable) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("log", new Class[] { String.class, Throwable.class }, new Object[] { message, throwable });
        } else {
            context.log(message, throwable);
        }
    }

    @Override
    public String getRealPath(String path) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getRealPath", new Object[] { path });
        } else {
            return context.getRealPath(path);
        }
    }

    @Override
    public String getServerInfo() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getServerInfo", null);
        } else {
            return context.getServerInfo();
        }
    }

    @Override
    public String getInitParameter(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getInitParameter", new Object[] { name });
        } else {
            return context.getInitParameter(name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Enumeration<String> getInitParameterNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration<String>) doPrivileged("getInitParameterNames", null);
        } else {
            return context.getInitParameterNames();
        }
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return ((Boolean) doPrivileged("setInitParameter", new Object[] { name, value })).booleanValue();
        } else {
            return context.setInitParameter(name, value);
        }
    }

    @Override
    public Object getAttribute(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return doPrivileged("getAttribute", new Object[] { name });
        } else {
            return context.getAttribute(name);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Enumeration<String> getAttributeNames() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Enumeration<String>) doPrivileged("getAttributeNames", null);
        } else {
            return context.getAttributeNames();
        }
    }

    @Override
    public void setAttribute(String name, Object object) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setAttribute", new Object[] { name, object });
        } else {
            context.setAttribute(name, object);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("removeAttribute", new Object[] { name });
        } else {
            context.removeAttribute(name);
        }
    }

    @Override
    public String getServletContextName() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getServletContextName", null);
        } else {
            return context.getServletContextName();
        }
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (ServletRegistration.Dynamic) doPrivileged("addServlet", new Object[] { servletName, className });
        } else {
            return context.addServlet(servletName, className);
        }
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (ServletRegistration.Dynamic) doPrivileged("addServlet", new Class[] { String.class, Servlet.class },
                    new Object[] { servletName, servlet });
        } else {
            return context.addServlet(servletName, servlet);
        }
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (ServletRegistration.Dynamic) doPrivileged("addServlet",
                    new Object[] { servletName, servletClass.getName() });
        } else {
            return context.addServlet(servletName, servletClass);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            try {
                return (T) invokeMethod(context, "createServlet", new Object[] { c });
            } catch (Throwable t) {
                handleThrowable(t);
                if (t instanceof ServletException) {
                    throw (ServletException) t;
                }
                return null;
            }
        } else {
            return context.createServlet(c);
        }
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (ServletRegistration) doPrivileged("getServletRegistration", new Object[] { servletName });
        } else {
            return context.getServletRegistration(servletName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Map<String, ? extends ServletRegistration>) doPrivileged("getServletRegistrations", null);
        } else {
            return context.getServletRegistrations();
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (FilterRegistration.Dynamic) doPrivileged("addFilter", new Object[] { filterName, className });
        } else {
            return context.addFilter(filterName, className);
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (FilterRegistration.Dynamic) doPrivileged("addFilter", new Class[] { String.class, Filter.class },
                    new Object[] { filterName, filter });
        } else {
            return context.addFilter(filterName, filter);
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (FilterRegistration.Dynamic) doPrivileged("addFilter", new Object[] { filterName, filterClass.getName() });
        } else {
            return context.addFilter(filterName, filterClass);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            try {
                return (T) invokeMethod(context, "createFilter", new Object[] { c });
            } catch (Throwable t) {
                handleThrowable(t);
                if (t instanceof ServletException) {
                    throw (ServletException) t;
                }
                return null;
            }
        } else {
            return context.createFilter(c);
        }
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (FilterRegistration) doPrivileged("getFilterRegistration", new Object[] { filterName });
        } else {
            return context.getFilterRegistration(filterName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (Map<String, ? extends FilterRegistration>) doPrivileged("getFilterRegistrations", null);
        } else {
            return context.getFilterRegistrations();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (EnumSet<SessionTrackingMode>) doPrivileged("getDefaultSessionTrackingModes", null);
        } else {
            return context.getDefaultSessionTrackingModes();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (EnumSet<SessionTrackingMode>) doPrivileged("getEffectiveSessionTrackingModes", null);
        } else {
            return context.getEffectiveSessionTrackingModes();
        }
    }

    @Override
    public SessionCookieConfigImpl getSessionCookieConfig() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (SessionCookieConfigImpl) doPrivileged("getSessionCookieConfig", null);
        } else {
            return context.getSessionCookieConfig();
        }
    }

   @Override
   public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("setSessionTrackingModes", new Object[] { sessionTrackingModes });
        } else {
            context.setSessionTrackingModes(sessionTrackingModes);
        }
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addListener", new Object[] { listenerClass.getName() });
        } else {
            context.addListener(listenerClass);
        }
    }

    @Override
    public void addListener(String className) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addListener", new Object[] { className });
        } else {
            context.addListener(className);
        }
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            doPrivileged("addListener", new Object[] { t.getClass().getName() });
        } else {
            context.addListener(t);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    // doPrivileged() returns the correct type
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            try {
                return (T) invokeMethod(context, "createListener", new Object[] { c });
            } catch (Throwable t) {
                handleThrowable(t);
                if (t instanceof ServletException) {
                    throw (ServletException) t;
                }
                return null;
            }
        } else {
            return context.createListener(c);
        }
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (JspConfigDescriptor) doPrivileged("getJspConfigDescriptor", null);
        } else {
            return context.getJspConfigDescriptor();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (ClassLoader) doPrivileged("getClassLoader", null);
        } else {
            return context.getClassLoader();
        }
    }

    public String getVirtualServerName() {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            return (String) doPrivileged("getVirtualServerName", null);
        } else {
            return context.getVirtualServerName();
        }
    }

    @Override
    public void declareRoles(String... roleNames) {
        if (SecurityUtil.isPackageProtectionEnabled()) {
            // FIXME
            doPrivileged("declareRoles", new Object[] { roleNames });
        } else {
            context.declareRoles(roleNames);
        }
    }

    /**
     * Use reflection to invoke the requested method. Cache the method object to speed up the process
     *
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private Object doPrivileged(final String methodName, final Object[] params) {
        try {
            return invokeMethod(context, methodName, params);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    /**
     * Use reflection to invoke the requested method. Cache the method object to speed up the process
     *
     * @param appContext The AppliationContext object on which the method will be invoked
     * @param methodName The method to call.
     * @param params The arguments passed to the called method.
     */
    private Object invokeMethod(ServletContextImpl appContext, final String methodName, Object[] params) throws Throwable {

        try {
            Method method = objectCache.get(methodName);
            if (method == null) {
                method = appContext.getClass().getMethod(methodName, classCache.get(methodName));
                objectCache.put(methodName, method);
            }

            return executeMethod(method, appContext, params);
        } catch (Exception ex) {
            handleException(ex);
            return null;
        } finally {
            params = null;
        }
    }

    /**
     * Use reflection to invoke the requested method. Cache the method object to speed up the process
     *
     * @param methodName The method to invoke.
     * @param clazz The class where the method is.
     * @param params The arguments passed to the called method.
     */
    private Object doPrivileged(final String methodName, final Class<?>[] clazz, Object[] params) {

        try {
            Method method = context.getClass().getMethod(methodName, clazz);
            return executeMethod(method, context, params);
        } catch (Exception ex) {
            try {
                handleException(ex);
            } catch (Throwable t) {
                handleThrowable(t);
                throw new RuntimeException(t.getMessage());
            }
            return null;
        } finally {
            params = null;
        }
    }

    /**
     * Executes the method of the specified <code>ApplicationContext</code>
     *
     * @param method The method object to be invoked.
     * @param context The AppliationContext object on which the method will be invoked
     * @param params The arguments passed to the called method.
     */
    private Object executeMethod(final Method method, final ServletContextImpl context, final Object[] params)
            throws PrivilegedActionException, IllegalAccessException, InvocationTargetException {

        if (SecurityUtil.isPackageProtectionEnabled()) {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws IllegalAccessException, InvocationTargetException {
                    return method.invoke(context, params);
                }
            });
        } else {
            return method.invoke(context, params);
        }
    }

    /**
     *
     * Throw the real exception.
     *
     * @param ex The current exception
     */
    private void handleException(Exception ex) throws Throwable {

        Throwable realException;

        if (ex instanceof PrivilegedActionException) {
            ex = ((PrivilegedActionException) ex).getException();
        }

        if (ex instanceof InvocationTargetException) {
            realException = ((InvocationTargetException) ex).getTargetException();
        } else {
            realException = ex;
        }

        throw realException;
    }

    // copied from org.apache.tomcat.util.ExceptionUtils
    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

    public ServletContextImpl getDelegatedContext() {
        return context;
    }
}
