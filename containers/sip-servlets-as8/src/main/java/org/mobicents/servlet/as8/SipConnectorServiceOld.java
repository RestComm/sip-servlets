package org.mobicents.servlet.as8;

import java.net.InetSocketAddress;
import static org.mobicents.as8.SipMessages.MESSAGES;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.undertow.SipProtocolHandler;

@Deprecated
public class SipConnectorServiceOld {

    private String protocol = "SIP/2.0";
    private String scheme = "sip";
    private Boolean useStaticAddress = false;
    private String staticServerAddress = null;
    private int staticServerPort = -1;
    private Boolean useStun = false;
    private String stunServerAddress = null;
    private int stunServerPort = -1;

    private Boolean enableLookups = null;
    private String proxyName = null;
    private Integer proxyPort = null;
    private Integer redirectPort = null;
    private Boolean secure = null;
    private Integer maxPostSize = null;
    private Integer maxSavePostSize = null;

    private SipProtocolHandler connector;

    // TODO:
    // private final InjectedValue<Executor> executor = new InjectedValue<Executor>();
    // private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();
    // private final InjectedValue<SipServer> server = new InjectedValue<SipServer>();

    public SipConnectorServiceOld(String protocol, String scheme, boolean useStaticAddress, String staticServerAddress,
            int staticServerPort, boolean useStun, String stunServerAddress, int stunServerPort) {
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
    }

    /**
     * Start, register and bind the web connector.
     *
     * @param context the start context
     * @throws StartException if the connector cannot be started
     */
    public synchronized void start(InetSocketAddress address, String transport/* TODO:StartContext context */)
            throws Exception {
        Logger.getLogger("org.mobicents.as7").debug(
                "SipConnectorService.start(), protocol = " + protocol + " - scheme = " + scheme);
        // final SocketBinding binding = this.binding.getValue();
        // final InetSocketAddress address = binding.getSocketAddress();
        // final Executor executor = this.executor.getOptionalValue();
        try {
            // Create connector
            // connector.setPort(address.getPort());
            // connector.setScheme(scheme);
            SipConnector sipConnector = new SipConnector();
            sipConnector.setIpAddress(address.getHostName());
            sipConnector.setPort(address.getPort());
            sipConnector.setTransport(transport/* binding.getName().substring("sip-".length()) */);
            sipConnector.setUseStaticAddress(useStaticAddress);
            sipConnector.setStaticServerAddress(staticServerAddress);
            sipConnector.setStaticServerPort(staticServerPort);
            sipConnector.setUseStun(useStun);
            sipConnector.setStunServerAddress(stunServerAddress);
            sipConnector.setStunServerPort(stunServerPort);
            SipProtocolHandler sipProtocolHandler = new SipProtocolHandler();// (SipProtocolHandler)connector.getProtocolHandler();
            sipProtocolHandler.setSipConnector(sipConnector);
            connector = sipProtocolHandler;
            ;
            // if (enableLookups != null)
            // connector.setEnableLookups(enableLookups);
            // if (maxPostSize != null)
            // connector.setMaxPostSize(maxPostSize);
            // if (maxSavePostSize != null)
            // connector.setMaxSavePostSize(maxSavePostSize);
            // if (proxyName != null)
            // connector.setProxyName(proxyName);
            // if (proxyPort != null)
            // connector.setProxyPort(proxyPort);
            // if (redirectPort != null)
            // connector.setRedirectPort(redirectPort);
            // if (secure != null)
            // connector.setSecure(secure);
            // TODO set Executor on ProtocolHandler
            // TODO use server socket factory - or integrate with {@code ManagedBinding}

            // getSipServer().addConnector(connector);
            // connector.init();
            // connector.start();
        } catch (Exception e) {
            throw new Exception(/* MESSAGES.connectorStartError(), */e);
        }
        // Register the binding after the connector is started
        // TODO:binding.getSocketBindings().getNamedRegistry().registerBinding(new ConnectorBinding(binding));
    }

    /** {@inheritDoc} */
    public synchronized void stop(/* StopContext context */) {
        /*
         * TODO:final SocketBinding binding = this.binding.getValue();
         * binding.getSocketBindings().getNamedRegistry().unregisterBinding(binding.getName()); final Connector
         * connector = this.connector; try { connector.pause(); } catch (Exception e) { } try { connector.stop(); }
         * catch (Exception e) { } getSipServer().removeConnector(connector);
         */
        this.connector = null;
    }

    public synchronized SipProtocolHandler getValue() throws IllegalStateException {
        final SipProtocolHandler connector = this.connector;
        if (connector == null) {
            throw MESSAGES.nullValue();
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

    /*
     * InjectedValue<Executor> getExecutor() { return executor; }
     * 
     * InjectedValue<SocketBinding> getBinding() { return binding; }
     * 
     * InjectedValue<SipServer> getServer() { return server; }
     * 
     * private SipServer getSipServer() { return server.getValue(); }
     */

    /*
     * static class ConnectorBinding implements ManagedBinding {
     * 
     * private final SocketBinding binding;
     * 
     * private ConnectorBinding(final SocketBinding binding) { this.binding = binding; }
     * 
     * @Override public String getSocketBindingName() { return binding.getName(); }
     * 
     * @Override public InetSocketAddress getBindAddress() { return binding.getSocketAddress(); }
     * 
     * @Override public void close() throws IOException { // TODO should this do something? } }
     */
}
