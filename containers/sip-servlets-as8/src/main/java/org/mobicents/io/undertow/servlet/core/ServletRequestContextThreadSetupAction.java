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

import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.api.ThreadSetupAction;
import io.undertow.servlet.handlers.ServletRequestContext;

/**
 * This class is based on protected class io.undertow.servlet.core.ServletRequestContextThreadSetupAction.
 *
 * @author kakonyi.istvan@alerant.hu
 */
class ServletRequestContextThreadSetupAction implements ThreadSetupAction {

    static final ServletRequestContextThreadSetupAction INSTANCE = new ServletRequestContextThreadSetupAction();

    private ServletRequestContextThreadSetupAction() {

    }

    private static final Handle HANDLE = new Handle() {
        @Override
        public void tearDown() {
            SecurityActions.clearCurrentServletAttachments();
        }
    };

    @Override
    public Handle setup(HttpServerExchange exchange) {
        if(exchange == null) {
            return null;
        }
        ServletRequestContext servletRequestContext = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        SecurityActions.setCurrentRequestContext(servletRequestContext);
        return HANDLE;
    }
}
