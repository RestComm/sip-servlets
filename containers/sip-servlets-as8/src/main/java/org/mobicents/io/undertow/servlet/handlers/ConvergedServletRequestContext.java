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


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mobicents.io.undertow.servlet.spec.ConvergedHttpServletRequestFacade;
import org.mobicents.io.undertow.servlet.spec.ConvergedHttpServletResponseFacade;

import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.handlers.ServletPathMatch;
import io.undertow.servlet.handlers.ServletRequestContext;

/**
 * This class extends io.undertow.servlet.handlers.ServletRequestContext to override get/setServletRequest/Response methods.
 * These methods will handle ConvergedHttpServletRequestFacade and ConvergedHttpServletResponseFacade objects.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class ConvergedServletRequestContext extends ServletRequestContext{
    private final ConvergedHttpServletRequestFacade originalRequest;
    private final ConvergedHttpServletResponseFacade originalResponse;
    private ServletResponse servletResponse;
    private ServletRequest servletRequest;

    public ConvergedServletRequestContext(Deployment deployment, ConvergedHttpServletRequestFacade originalRequest,
            ConvergedHttpServletResponseFacade originalResponse, ServletPathMatch originalServletPathMatch) {
        super(deployment, originalRequest.getHttpServletRequestDelegated(), originalResponse.getHttpServletResponseDelegated(), originalServletPathMatch);
        this.originalRequest = originalRequest;
        this.originalResponse = originalResponse;
        this.servletRequest = originalRequest;
        this.servletResponse = originalResponse;
    }

    public ConvergedHttpServletRequestFacade getConvergedOriginalRequest() {
        return originalRequest;
    }

    public ConvergedHttpServletResponseFacade getConvergedOriginalResponse() {
        return originalResponse;
    }

    @Override
    public ServletResponse getServletResponse() {
        return servletResponse;
    }

    @Override
    public void setServletResponse(ServletResponse servletResponse) {
        super.setServletResponse(servletResponse);
        this.servletResponse = servletResponse;
    }

    @Override
    public ServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(ServletRequest servletRequest) {
        super.setServletRequest(servletRequest);
        this.servletRequest = servletRequest;
    }


}
