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
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.MetricsCollector;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.handlers.ServletHandler;
import io.undertow.servlet.handlers.ServletRequestContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is based on protected io.undertow.servlet.core.MetricsChainHandler class.
 *
 * @author kakonyi.istvan@alerant.hu
 * */
public class MetricsChainHandler implements HttpHandler {


    private final HttpHandler next;
    private final Map<String, MetricsHandler> servletHandlers;

    public MetricsChainHandler(HttpHandler next, MetricsCollector collector, Deployment deployment) {
        this.next = next;
        final Map<String, MetricsHandler> servletHandlers = new HashMap<>();
        for(Map.Entry<String, ServletHandler> entry : deployment.getServlets().getServletHandlers().entrySet()) {
            MetricsHandler handler = new MetricsHandler(next);
            servletHandlers.put(entry.getKey(), handler);
            collector.registerMetric(entry.getKey(), handler);
        }
        this.servletHandlers = Collections.unmodifiableMap(servletHandlers);
    }
    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        ServletRequestContext context = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        ServletInfo servletInfo = context.getCurrentServlet().getManagedServlet().getServletInfo();
        MetricsHandler handler = servletHandlers.get(servletInfo.getName());
        if(handler != null) {
            handler.handleRequest(exchange);
        } else {
            next.handleRequest(exchange);
        }
    }
}
