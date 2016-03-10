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
package org.mobicents.as8;

import org.mobicents.servlet.sip.undertow.SipProtocolHandler;

/**
 * @author kakonyi.istvan@alerant.hu
 */
public class SipConnectorListener {

    private SipProtocolHandler protocolHandler;

    public SipConnectorListener(SipProtocolHandler protocolHandler) {

        this.protocolHandler = protocolHandler;

    }

    public SipProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void init() {
        try {
            this.protocolHandler.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            this.protocolHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        this.protocolHandler.resume();
    }

    public void stop() {
        try {
            this.protocolHandler.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
