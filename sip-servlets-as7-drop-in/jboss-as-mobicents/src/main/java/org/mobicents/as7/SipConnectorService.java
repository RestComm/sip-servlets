/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.as7;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.apache.catalina.connector.Connector;
import org.jboss.as.network.ManagedBinding;
import org.jboss.as.network.SocketBinding;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;

/**
 * Service creating and starting a web connector.
 *
 * @author Emanuel Muckenhuber
 */
class SipConnectorService implements Service<Connector> {

    private String protocol = "SIP/2.0";
    private String scheme = "sip";

    private Boolean enableLookups = null;
    private String proxyName = null;
    private Integer proxyPort = null;
    private Integer redirectPort = null;
    private Boolean secure = null;
    private Integer maxPostSize = null;
    private Integer maxSavePostSize = null;

    private Connector connector;

    private final InjectedValue<Executor> executor = new InjectedValue<Executor>();
    private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();
    private final InjectedValue<SipServer> server = new InjectedValue<SipServer>();

    public SipConnectorService(String protocol, String scheme) {
        Logger.getLogger("org.mobicents.as7").info("SipConnectorService(), protocol = " + protocol + " - scheme = " + scheme);
        if (protocol != null)
            this.protocol = protocol;
        if (scheme != null)
            this.scheme = scheme;
    }

    /**
     * Start, register and bind the web connector.
     *
     * @param context the start context
     * @throws StartException if the connector cannot be started
     */
    public synchronized void start(StartContext context) throws StartException {
        Logger.getLogger("org.mobicents.as7").info("SipConnectorService.start(), protocol = " + protocol + " - scheme = " + scheme);
        final SocketBinding binding = this.binding.getValue();
        final InetSocketAddress address = binding.getSocketAddress();
        final Executor executor = this.executor.getOptionalValue();
        try {
            // Create connector
            final Connector connector = new Connector(SipProtocolHandler.class.getName());
            connector.setPort(address.getPort());
            connector.setScheme(scheme);
            SipConnector sipConnector = new SipConnector();
            sipConnector.setIpAddress(address.getHostName());
            sipConnector.setPort(address.getPort());
            sipConnector.setTransport(binding.getName().substring("sip-".length()));
            SipProtocolHandler sipProtocolHandler = (SipProtocolHandler) connector.getProtocolHandler();
            sipProtocolHandler.setSipConnector(sipConnector);
            if (enableLookups != null)
                connector.setEnableLookups(enableLookups);
            if (maxPostSize != null)
                connector.setMaxPostSize(maxPostSize);
            if (maxSavePostSize != null)
                connector.setMaxSavePostSize(maxSavePostSize);
            if (proxyName != null)
                connector.setProxyName(proxyName);
            if (proxyPort != null)
                connector.setProxyPort(proxyPort);
            if (redirectPort != null)
                connector.setRedirectPort(redirectPort);
            if (secure != null)
                connector.setSecure(secure);
            // TODO set Executor on ProtocolHandler
            // TODO use server socket factory - or integrate with {@code ManagedBinding}

            getSipServer().addConnector(connector);
            connector.init();
            connector.start();
            this.connector = connector;
        } catch (Exception e) {
            throw new StartException(e);
        }
        // Register the binding after the connector is started
        binding.getSocketBindings().getNamedRegistry().registerBinding(new ConnectorBinding(binding));
    }

    /** {@inheritDoc} */
    public synchronized void stop(StopContext context) {
        final SocketBinding binding = this.binding.getValue();
        binding.getSocketBindings().getNamedRegistry().unregisterBinding(binding.getName());
        final Connector connector = this.connector;
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
    public synchronized Connector getValue() throws IllegalStateException {
        final Connector connector = this.connector;
        if (connector == null) {
            throw new IllegalStateException();
        }
        return connector;
    }

    protected boolean isEnableLookups() {
        return enableLookups;
    }

    protected void setEnableLookups(boolean enableLookups) {
        this.enableLookups = enableLookups;
    }

    protected String getProxyName() {
        return proxyName;
    }

    protected void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    protected int getProxyPort() {
        return proxyPort;
    }

    protected void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    protected int getRedirectPort() {
        return redirectPort;
    }

    protected void setRedirectPort(int redirectPort) {
        this.redirectPort = redirectPort;
    }

    protected boolean isSecure() {
        return secure;
    }

    protected void setSecure(boolean secure) {
        this.secure = secure;
    }

    protected int getMaxPostSize() {
        return maxPostSize;
    }

    protected void setMaxPostSize(int maxPostSize) {
        this.maxPostSize = maxPostSize;
    }

    protected int getMaxSavePostSize() {
        return maxSavePostSize;
    }

    protected void setMaxSavePostSize(int maxSavePostSize) {
        this.maxSavePostSize = maxSavePostSize;
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
