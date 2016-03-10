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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.UndertowServletLogger;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import io.undertow.servlet.spec.ServletContextImpl;
import io.undertow.util.HttpString;
import io.undertow.util.RedirectBuilder;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

/**
 *  HttpServletResponse implementor and HttpServletResponseImpl wrapper class.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class ConvergedHttpServletResponseFacade implements HttpServletResponse{
    private final HttpServletResponseImpl httpServletResponse;
    private final ServletContextImpl originalServletContext;
    private volatile ConvergedServletContextImpl servletContext;

    public ConvergedHttpServletResponseFacade(HttpServletResponseImpl httpServletResponse, final ConvergedServletContextImpl servletContext) {
        this.httpServletResponse = httpServletResponse;
        this.originalServletContext = servletContext.getDelegatedContext();
        this.servletContext = servletContext;
    }

    public HttpServerExchange getExchange() {
        return httpServletResponse.getExchange();
    }

    @Override
    public void addCookie(final Cookie cookie) {
        httpServletResponse.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(final String name) {
        return httpServletResponse.containsHeader(name);
    }

    @Override
    public String encodeUrl(final String url) {
        return httpServletResponse.encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(final String url) {
        return httpServletResponse.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException {
        httpServletResponse.sendError(sc, msg);
    }

    public void doErrorDispatch(int sc, String error) throws IOException {
        httpServletResponse.doErrorDispatch(sc, error);
    }

    @Override
    public void sendError(final int sc) throws IOException {
        httpServletResponse.sendError(sc);
    }

    @Override
    public void sendRedirect(final String location) throws IOException {
        httpServletResponse.sendRedirect(location);
    }

    @Override
    public void setDateHeader(final String name, final long date) {
        httpServletResponse.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(final String name, final long date) {
        httpServletResponse.addDateHeader(name, date);
    }

    @Override
    public void setHeader(final String name, final String value) {
        httpServletResponse.setHeader(name, value);
    }


    public void setHeader(final HttpString name, final String value) {
        httpServletResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(final String name, final String value) {
        httpServletResponse.addHeader(name, value);
    }

    public void addHeader(final HttpString name, final String value) {
        httpServletResponse.addHeader(name, value);
    }

    @Override
    public void setIntHeader(final String name, final int value) {
        httpServletResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(final String name, final int value) {
        httpServletResponse.addIntHeader(name, value);
    }

    @Override
    public void setStatus(final int sc) {
        httpServletResponse.setStatus(sc);
    }

    @Override
    public void setStatus(final int sc, final String sm) {
        httpServletResponse.setStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return httpServletResponse.getStatus();
    }

    @Override
    public String getHeader(final String name) {
        return httpServletResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(final String name) {
        return httpServletResponse.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return httpServletResponse.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return httpServletResponse.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return httpServletResponse.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return httpServletResponse.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return httpServletResponse.getWriter();
    }

    @Override
    public void setCharacterEncoding(final String charset) {
        httpServletResponse.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(final int len) {
        httpServletResponse.setContentLength(len);
    }

    public void setContentLengthLong(final long len) {
        httpServletResponse.setContentLengthLong(len);
    }

    boolean isIgnoredFlushPerformed() {
        //accessing protected methods using reflection:
        //return httpServletResponse.isIgnoredFlushPerformed();
        boolean result=false;
        try{
            Method isIgnoredFlushPerformed = HttpServletResponseImpl.class.getDeclaredMethod("isIgnoredFlushPerformed", null);
            isIgnoredFlushPerformed.setAccessible(true);
            result=(boolean)isIgnoredFlushPerformed.invoke(httpServletResponse, null);
            isIgnoredFlushPerformed.setAccessible(false);
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            UndertowServletLogger.ROOT_LOGGER.warn("Exception occured during invoking HttpServletResponseImpl.isIgnoredFlushPerformed() method using reflection:",e);
            //FIXME: kakonyii: handle reflection errors or just omit them.
        }
        return result;
    }

    void setIgnoredFlushPerformed(boolean ignoredFlushPerformed) {
        //accessing protected methods using reflection:
        //httpServletResponse.setIgnoredFlushPerformed(ignoredFlushPerformed);
        try{
            Method setIgnoredFlushPerformed = HttpServletResponseImpl.class.getDeclaredMethod("setIgnoredFlushPerformed", boolean.class);
            setIgnoredFlushPerformed.setAccessible(true);
            setIgnoredFlushPerformed.invoke(httpServletResponse, ignoredFlushPerformed);
            setIgnoredFlushPerformed.setAccessible(false);
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            UndertowServletLogger.ROOT_LOGGER.warn("Exception occured during invoking HttpServletResponseImpl.setIgnoredFlushPerformed() method using reflection:",e);
            //FIXME: kakonyii: handle reflection errors or just omit them.
        }
    }

    @Override
    public void setContentType(final String type) {
        httpServletResponse.setContentType(type);
    }

    @Override
    public void setBufferSize(final int size) {
        httpServletResponse.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return httpServletResponse.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        httpServletResponse.flushBuffer();
    }

    public void closeStreamAndWriter() throws IOException {
        httpServletResponse.closeStreamAndWriter();
    }

    public void freeResources() throws IOException {
        httpServletResponse.freeResources();
    }

    @Override
    public void resetBuffer() {
        httpServletResponse.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return httpServletResponse.isCommitted();
    }

    @Override
    public void reset() {
        httpServletResponse.reset();
    }

    @Override
    public void setLocale(final Locale loc) {
        httpServletResponse.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return httpServletResponse.getLocale();
    }

    public void responseDone() {
        httpServletResponse.responseDone();
    }

    public boolean isInsideInclude() {
        return httpServletResponse.isInsideInclude();
    }

    public void setInsideInclude(final boolean insideInclude) {
        httpServletResponse.setInsideInclude(insideInclude);
    }

    public void setServletContext(final ConvergedServletContextImpl servletContext) {
        this.servletContext = servletContext;
        httpServletResponse.setServletContext(servletContext.getDelegatedContext());
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public String encodeURL(String url) {
        String absolute = toAbsolute(url);
        if (isEncodeable(absolute)) {
            // W3c spec clearly said
            if (url.equalsIgnoreCase("")) {
                url = absolute;
            }
            return originalServletContext.getSessionConfig().rewriteUrl(url, servletContext.getSession(originalServletContext, httpServletResponse.getExchange(), true).getId());
        } else {
            return (url);
        }

    }

    /**
     * Encode the session identifier associated with this response
     * into the specified redirect URL, if necessary.
     *
     * @param url URL to be encoded
     */
    public String encodeRedirectURL(String url) {
        if (isEncodeable(toAbsolute(url))) {
            return originalServletContext.getSessionConfig().rewriteUrl(url, servletContext.getSession(originalServletContext, httpServletResponse.getExchange(), true).getId());
        } else {
            return url;
        }
    }

    /**
     * Convert (if necessary) and return the absolute URL that represents the
     * resource referenced by this possibly relative URL.  If this URL is
     * already absolute, return it unchanged.
     *
     * @param location URL to be (possibly) converted and then returned
     * @throws IllegalArgumentException if a MalformedURLException is
     *                                  thrown when converting the relative URL to an absolute one
     */
    private String toAbsolute(String location) {

        if (location == null) {
            return location;
        }

        boolean leadingSlash = location.startsWith("/");

        if (leadingSlash || !hasScheme(location)) {
            return RedirectBuilder.redirect(httpServletResponse.getExchange(), location, false);
        } else {
            return location;
        }

    }

    /**
     * Determine if a URI string has a <code>scheme</code> component.
     */
    private boolean hasScheme(String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == ':') {
                return i > 0;
            } else if (!Character.isLetterOrDigit(c) &&
                    (c != '+' && c != '-' && c != '.')) {
                return false;
            }
        }
        return false;
    }

    /**
     * Return <code>true</code> if the specified URL should be encoded with
     * a session identifier.  This will be true if all of the following
     * conditions are met:
     * <ul>
     * <li>The request we are responding to asked for a valid session
     * <li>The requested session ID was not received via a cookie
     * <li>The specified URL points back to somewhere within the web
     * application that is responding to this request
     * </ul>
     *
     * @param location Absolute URL to be validated
     */
    protected boolean isEncodeable(final String location) {
        //accessing protected methods using reflection:
        //return httpServletResponse.isEncodeable(location);
        boolean result=false;
        try{
            Method isEncodeable = HttpServletResponseImpl.class.getDeclaredMethod("isEncodeable", String.class);
            isEncodeable.setAccessible(true);
            result= (boolean) isEncodeable.invoke(httpServletResponse, location);
            isEncodeable.setAccessible(false);
        }catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            UndertowServletLogger.ROOT_LOGGER.warn("Exception occured during invoking HttpServletResponseImpl.isEncodeable() method using reflection:",e);
            //FIXME: kakonyii: handle reflection errors or just omit them.
        }
        return result;
    }

    public long getContentLength() {
        return httpServletResponse.getContentLength();
    }

    public boolean isTreatAsCommitted() {
        return httpServletResponse.isTreatAsCommitted();
    }

    public HttpServletResponseImpl getHttpServletResponseDelegated() {
        return httpServletResponse;
    }
}
