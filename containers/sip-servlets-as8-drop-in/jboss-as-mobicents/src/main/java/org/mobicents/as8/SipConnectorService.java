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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.jboss.as.network.ManagedBinding;
import org.jboss.as.network.SocketBinding;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.undertow.SipProtocolHandler;

import static org.mobicents.as8.SipMessages.MESSAGES;

/**
 * Service creating and starting a web connector.
 *
 * @author Emanuel Muckenhuber
 *
 *         This class is based on the contents of org.mobicents.as7 package from jboss-as7-mobicents project, re-implemented for
 *         jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
class SipConnectorService implements Service<SipConnectorListener> {

    private String protocol = "SIP/2.0";
    private String scheme = "sip";
    private Boolean useStaticAddress = false;
    private String staticServerAddress = null;
    private int staticServerPort = -1;
    private Boolean useStun = false;
    private String stunServerAddress = null;
    private int stunServerPort = -1;
    private String hostNames = null;

    private SipConnectorListener connector;

    private final InjectedValue<Executor> executor = new InjectedValue<Executor>();
    private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();
    private final InjectedValue<SipServer> server = new InjectedValue<SipServer>();

    public SipConnectorService(String protocol, String scheme, boolean useStaticAddress, String staticServerAddress,
            int staticServerPort, boolean useStun, String stunServerAddress, int stunServerPort, String hostNames) {
        if (protocol != null)
            this.protocol = protocol;
        if (scheme != null)
            this.scheme = scheme;
        this.useStaticAddress = useStaticAddress;
        if (staticServerAddress != null)
            this.staticServerAddress = staticServerAddress;
        this.staticServerPort = staticServerPort;
        this.useStun = useStun;
        if (stunServerAddress != null)
            this.stunServerAddress = stunServerAddress;
        this.stunServerPort = stunServerPort;
        this.hostNames = hostNames;
    }

    /**
     * Start, register and bind the web connector.
     *
     * @param context the start context
     * @throws StartException if the connector cannot be started
     */
    public synchronized void start(StartContext context) throws StartException {
        Logger.getLogger("org.mobicents.as8").debug(
                "SipConnectorService.start(), protocol = " + protocol + " - scheme = " + scheme);
        final SocketBinding binding = this.binding.getValue();
        final InetSocketAddress address = binding.getSocketAddress();
        final Executor executor = this.executor.getOptionalValue();
        try {

            SipConnector sipConnector = new SipConnector();
            sipConnector.setIpAddress(address.getAddress().getHostAddress());
            sipConnector.setPort(address.getPort());
            // https://github.com/Mobicents/sip-servlets/issues/44 support multiple connectors
            sipConnector.setTransport(binding.getName().substring(binding.getName().lastIndexOf("sip-") + ("sip-".length())));
            sipConnector.setUseStaticAddress(useStaticAddress);
            sipConnector.setStaticServerAddress(staticServerAddress);
            sipConnector.setStaticServerPort(staticServerPort);
            sipConnector.setUseStun(useStun);
            sipConnector.setStunServerAddress(stunServerAddress);
            sipConnector.setStunServerPort(stunServerPort);
            sipConnector.setHostNames(hostNames);

            Logger.getLogger("org.mobicents.as8").debug(
                    "SipConnectorService.start(), address = " + address.getAddress().getHostAddress() + " - port = "
                            + address.getPort() + " - transport = " + sipConnector.getTransport());
            Logger.getLogger("org.mobicents.as8").debug("SipConnectorService.start(), hostnames = " + hostNames);

            /*
             * FIXME: kakonyii: need an implementation in SipConnector class for the following fields: enableLookups proxyName
             * proxyPort redirectPort secure maxPostSize maxSavePostSize
             */

            SipProtocolHandler sipProtocolHandler = new SipProtocolHandler(sipConnector);

            // TODO set Executor on ProtocolHandler
            // TODO use server socket factory - or integrate with {@code ManagedBinding}

            final SipConnectorListener connector = new SipConnectorListener(sipProtocolHandler);

            connector.init();
            getSipServer().addConnector(connector);
            connector.start();

            this.connector = connector;
        } catch (Exception e) {
            throw new StartException(MESSAGES.connectorStartError(), e);
        }
        // Register the binding after the connector is started
        binding.getSocketBindings().getNamedRegistry().registerBinding(new ConnectorBinding(binding));
    }

    /** {@inheritDoc} */
    public synchronized void stop(StopContext context) {
        final SocketBinding binding = this.binding.getValue();
        binding.getSocketBindings().getNamedRegistry().unregisterBinding(binding.getName());
        final SipConnectorListener connector = this.connector;
        try {
            connector.pause();
        } catch (Exception e) {
        }
        try {
            connector.stop();
        } catch (Exception e) {
        }
        getSipServer().removeConnector(connector);
        this.connector = null;
    }

    /** {@inheritDoc} */
    public synchronized SipConnectorListener getValue() throws IllegalStateException {
        final SipConnectorListener connector = this.connector;
        if (connector == null) {
            throw MESSAGES.nullValue();
        }
        return connector;
    }

    InjectedValue<Executor> getExecutor() {
        return executor;
    }

    InjectedValue<SocketBinding> getBinding() {
        return binding;
    }

    InjectedValue<SipServer> getServer() {
        return server;
    }

    private SipServer getSipServer() {
        return server.getValue();
    }

    static class ConnectorBinding implements ManagedBinding {

        private final SocketBinding binding;

        private ConnectorBinding(final SocketBinding binding) {
            this.binding = binding;
        }

        @Override
        public String getSocketBindingName() {
            return binding.getName();
        }

        @Override
        public InetSocketAddress getBindAddress() {
            return binding.getSocketAddress();
        }

        @Override
        public void close() throws IOException {
            // TODO should this do something?
        }
    }

}
