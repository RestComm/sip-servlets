/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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

import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.LifecycleInterceptor;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.handlers.DefaultServlet;

/**
 * Utility class for invoking servlet and filter lifecycle methods.
 * 
 * @author kakonyi.istvan@alerant.hu
 * @author balogh.gabor@alerant.hu
 */
public class LifecyleInterceptorInvocation implements LifecycleInterceptor.LifecycleContext {
    private final List<LifecycleInterceptor> list;
    private final ServletInfo servletInfo;
    private final FilterInfo filterInfo;
    private final Servlet servlet;
    private final Filter filter;
    private int i;
    private final ServletConfig servletConfig;
    private final ServletConfig convergedServletConfig;
    private final FilterConfig filterConfig;

    public LifecyleInterceptorInvocation(List<LifecycleInterceptor> list, ServletInfo servletInfo, Servlet servlet,
            ServletConfig servletConfig, ServletConfig convergedServletConfig) {
        this.list = list;
        this.servletInfo = servletInfo;
        this.servlet = servlet;
        this.servletConfig = servletConfig;
        this.convergedServletConfig = convergedServletConfig;
        this.filter = null;
        this.filterConfig = null;
        this.filterInfo = null;
        i = list.size();
    }

    public LifecyleInterceptorInvocation(List<LifecycleInterceptor> list, ServletInfo servletInfo, Servlet servlet) {
        this.list = list;
        this.servlet = servlet;
        this.servletInfo = servletInfo;
        this.filterInfo = null;
        this.servletConfig = null;
        this.convergedServletConfig = null;
        this.filter = null;
        this.filterConfig = null;
        i = list.size();
    }

    public LifecyleInterceptorInvocation(List<LifecycleInterceptor> list, FilterInfo filterInfo, Filter filter,
            FilterConfig filterConfig) {
        this.list = list;
        this.servlet = null;
        this.servletConfig = null;
        this.convergedServletConfig = null;
        this.filter = filter;
        this.filterConfig = filterConfig;
        this.filterInfo = filterInfo;
        this.servletInfo = null;
        i = list.size();
    }

    public LifecyleInterceptorInvocation(List<LifecycleInterceptor> list, FilterInfo filterInfo, Filter filter) {
        this.list = list;
        this.servlet = null;
        this.servletConfig = null;
        this.filter = filter;
        this.filterConfig = null;
        this.filterInfo = filterInfo;
        this.servletInfo = null;
        i = list.size();
    }

    @Override
    public void proceed() throws ServletException {
        if (--i >= 0) {
            final LifecycleInterceptor next = list.get(i);
            if (filter != null) {
                if (filterConfig == null) {
                    next.destroy(filterInfo, filter, this);
                } else {
                    next.init(filterInfo, filter, this);
                }
            } else {
                if (servletConfig == null && convergedServletConfig == null) {
                    next.destroy(servletInfo, servlet, this);
                } else {
                    next.init(servletInfo, servlet, this);
                }
            }
        } else if (i == -1) {
            if (filter != null) {
                if (filterConfig == null) {
                    filter.destroy();
                } else {
                    filter.init(filterConfig);
                }
            } else {
                if (servletConfig == null && convergedServletConfig == null) {
                    servlet.destroy();
                } else {
                    if (servlet instanceof DefaultServlet) {
                        servlet.init(servletConfig);
                    } else {
                        servlet.init(convergedServletConfig);
                    }
                }
            }
        }
    }
}
