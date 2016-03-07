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

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.UndertowServletLogger;
import io.undertow.servlet.spec.AsyncContextImpl;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.servlet.spec.ServletContextImpl;
import io.undertow.util.HttpString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

/**
 *  HttpServletRequest implementor and HttpServletRequestImpl wrapper class.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class ConvergedHttpServletRequestFacade implements HttpServletRequest{

    private final HttpServletRequestImpl httpServletRequest;
    private final ServletContextImpl originalServletContext;
    private ConvergedServletContextImpl servletContext;

    public ConvergedHttpServletRequestFacade(HttpServletRequestImpl httpServletRequest, final ConvergedServletContextImpl servletContext){
        this.httpServletRequest = httpServletRequest;
        this.originalServletContext = httpServletRequest.getServletContext();
        this.servletContext = servletContext;
    }

    public HttpServerExchange getExchange() {
        return httpServletRequest.getExchange();
    }

    @Override
    public String getAuthType() {
        return httpServletRequest.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return httpServletRequest.getCookies();
    }

    @Override
    public long getDateHeader(final String name) {
        return httpServletRequest.getDateHeader(name);
    }

    @Override
    public String getHeader(final String name) {
       return httpServletRequest.getHeader(name);
    }

    public String getHeader(final HttpString name) {
        return httpServletRequest.getHeader(name);
    }


    @Override
    public Enumeration<String> getHeaders(final String name) {
        return httpServletRequest.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return httpServletRequest.getHeaderNames();
    }

    @Override
    public int getIntHeader(final String name) {
        return httpServletRequest.getIntHeader(name);
    }

    @Override
    public String getMethod() {
        return httpServletRequest.getMethod();
    }

    @Override
    public String getPathInfo() {
        return httpServletRequest.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return httpServletRequest.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return httpServletRequest.getContextPath();
    }

    @Override
    public String getQueryString() {
        return httpServletRequest.getQueryString();
    }

    @Override
    public String getRemoteUser() {
       return httpServletRequest.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(final String role) {
       return httpServletRequest.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return httpServletRequest.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return httpServletRequest.getRequestedSessionId();
    }

    public String changeSessionId() {
        return httpServletRequest.changeSessionId();
    }

    @Override
    public String getRequestURI() {
        return httpServletRequest.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return httpServletRequest.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return httpServletRequest.getServletPath();
    }

    @Override
    public HttpSession getSession(final boolean create) {
        return servletContext.getSession(originalServletContext, httpServletRequest.getExchange(), create);
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }


    @Override
    public boolean isRequestedSessionIdValid() {
        HttpSession session = servletContext.getSession(originalServletContext, httpServletRequest.getExchange(), false);
        return session != null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return httpServletRequest.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return httpServletRequest.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return httpServletRequest.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
        return httpServletRequest.authenticate(response);
    }

    @Override
    public void login(final String username, final String password) throws ServletException {
        httpServletRequest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        httpServletRequest.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return httpServletRequest.getParts();
    }

    @Override
    public Part getPart(final String name) throws IOException, ServletException {
        return httpServletRequest.getPart(name);
    }


    public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException {
      return httpServletRequest.upgrade(handlerClass);
    }

    @Override
    public Object getAttribute(final String name) {
        return httpServletRequest.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return httpServletRequest.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return httpServletRequest.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
       httpServletRequest.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        return httpServletRequest.getContentLength();
    }

    public long getContentLengthLong() {
        return httpServletRequest.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return httpServletRequest.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return httpServletRequest.getInputStream();
    }

    public void closeAndDrainRequest() throws IOException {
        httpServletRequest.closeAndDrainRequest();
    }

    /**
     * Frees any resources (namely buffers) that may be associated with this request.
     *
     */
    public void freeResources() throws IOException {
        httpServletRequest.freeResources();
    }

    @Override
    public String getParameter(final String name) {
        return httpServletRequest.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return httpServletRequest.getParameterNames();
    }

    @Override
    public String[] getParameterValues(final String name) {
        return httpServletRequest.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return httpServletRequest.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return httpServletRequest.getProtocol();
    }

    @Override
    public String getScheme() {
        return httpServletRequest.getScheme();
    }

    @Override
    public String getServerName() {
        return httpServletRequest.getServerName();
    }

    @Override
    public int getServerPort() {
        return httpServletRequest.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return httpServletRequest.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return httpServletRequest.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return httpServletRequest.getRemoteHost();
    }

    @Override
    public void setAttribute(final String name, final Object object) {
        httpServletRequest.setAttribute(name, object);
    }

    @Override
    public void removeAttribute(final String name) {
        httpServletRequest.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        return httpServletRequest.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return httpServletRequest.getLocales();
    }

    @Override
    public boolean isSecure() {
        return httpServletRequest.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path) {
        return httpServletRequest.getRequestDispatcher(path);
    }

    @Override
    public String getRealPath(final String path) {
        return httpServletRequest.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return httpServletRequest.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return httpServletRequest.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return httpServletRequest.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return httpServletRequest.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return httpServletRequest.startAsync();
    }

    @Override
    public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse) throws IllegalStateException {
        return httpServletRequest.startAsync();
    }

    @Override
    public boolean isAsyncStarted() {
        return httpServletRequest.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return httpServletRequest.isAsyncSupported();
    }

    @Override
    public AsyncContextImpl getAsyncContext() {
        return httpServletRequest.getAsyncContext();
    }

    public AsyncContextImpl getAsyncContextInternal() {
        return httpServletRequest.getAsyncContextInternal();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return httpServletRequest.getDispatcherType();
    }


    public Map<String, Deque<String>> getQueryParameters() {
        return httpServletRequest.getQueryParameters();
    }

    public void setQueryParameters(final Map<String, Deque<String>> queryParameters) {
        httpServletRequest.setQueryParameters(queryParameters);
    }

    public void setServletContext(final ConvergedServletContextImpl servletContext) {
        this.servletContext = servletContext;
        httpServletRequest.setServletContext(servletContext.getDelegatedContext());
    }

    void asyncRequestDispatched() {
        //accessing protected methods using reflection:
        //httpServletRequest.asyncRequestDispatched();
        try{
            Method asyncRequestDispatched = HttpServletRequestImpl.class.getDeclaredMethod("asyncRequestDispatched", null);
            asyncRequestDispatched.setAccessible(true);
            asyncRequestDispatched.invoke(httpServletRequest, null);
            asyncRequestDispatched.setAccessible(false);
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            UndertowServletLogger.REQUEST_LOGGER.warn("Exception occured during invoking HttpServletRequestImpl.asyncRequestDispatched() method using reflection:",e);
            //FIXME: kakonyii: handle reflection errors or just omit them.
        }

    }

    public String getOriginalRequestURI() {
        return httpServletRequest.getOriginalRequestURI();
    }


    public String getOriginalServletPath() {
        return httpServletRequest.getOriginalServletPath();
    }

    public String getOriginalPathInfo() {
        return httpServletRequest.getOriginalPathInfo();
    }

    public String getOriginalContextPath() {
        return httpServletRequest.getOriginalContextPath();
    }

    public String getOriginalQueryString() {
        return httpServletRequest.getOriginalQueryString();
    }

    public HttpServletRequestImpl getHttpServletRequestDelegated() {
        return httpServletRequest;
    }

    @Override
    public String toString() {
        return "ConvergedHttpServletRequestFacade [ " + getMethod() + ' ' + getRequestURI() + " ]";
    }

    public void clearAttributes() {
        httpServletRequest.clearAttributes();
    }
}
