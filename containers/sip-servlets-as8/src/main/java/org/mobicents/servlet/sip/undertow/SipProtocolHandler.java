package org.mobicents.servlet.sip.undertow;

import gov.nist.javax.sip.ListeningPointExt;
import gov.nist.javax.sip.UndertowSipStackImpl;
import io.undertow.server.handlers.udp.UdpHandler;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sip.CreateListeningPointResult;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.SipStack;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

public class SipProtocolHandler implements MBeanRegistration {
    // the logger
    private static final Logger logger = Logger.getLogger(SipProtocolHandler.class.getName());

    public static final String IS_SIP_CONNECTOR = "isSipConnector";

    protected ObjectName tpOname = null;
    // *
    protected ObjectName rgOname = null;

    private SipConnector sipConnector;

    private SipStack sipStack;

    public ExtendedListeningPoint extendedListeningPoint;

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public SipProtocolHandler() {
        sipConnector = new SipConnector();
    }

    public SipProtocolHandler(SipConnector connector) {
        sipConnector = connector;
    }

    public void init() throws Exception {
        setAttribute(IS_SIP_CONNECTOR, Boolean.TRUE);
    }

    public CreateListeningPointResult start(UdpHandler handler) throws Exception {
        CreateListeningPointResult result = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Starting a sip protocol handler");
        }
        Integer portFromConfig = sipConnector.getPort();
        String set = System.getProperty("jboss.service.binding.set");
        int setIncrememt = 0;
        if (set != null) {
            int setNumber = set.charAt(set.length() - 1) - '0';
            if (setNumber > 0 && setNumber < 10) {
                setIncrememt = (setNumber) * 100; // don't attempt to compute if the port set is custom outside that
                                                  // range
            }
            logger.info("Computed port increment for MSS SIP ports is " + setIncrememt + " from " + set);
        }
        portFromConfig += setIncrememt;
        sipConnector.setPort(portFromConfig);

        final String ipAddress = sipConnector.getIpAddress();
        final int port = sipConnector.getPort();
        final String signalingTransport = sipConnector.getTransport();

        try {
            // checking the external ip address if stun enabled
            String globalIpAddress = null;
            int globalPort = -1;
            boolean useStun = sipConnector.isUseStun();
            /*
             * TODO:if (useStun) { if(InetAddress.getByName(ipAddress).isLoopbackAddress()) {
             * logger.warn("The Ip address provided is the loopback address, stun won't be enabled for it"); } else {
             * //chooses stun port randomly DatagramSocket randomSocket = initRandomPortSocket(); int randomPort =
             * randomSocket.getLocalPort(); randomSocket.disconnect(); randomSocket.close(); randomSocket = null;
             * StunAddress localStunAddress = new StunAddress(ipAddress, randomPort);
             * 
             * StunAddress serverStunAddress = new StunAddress( sipConnector.getStunServerAddress(),
             * sipConnector.getStunServerPort());
             * 
             * NetworkConfigurationDiscoveryProcess addressDiscovery = new NetworkConfigurationDiscoveryProcess(
             * localStunAddress, serverStunAddress); addressDiscovery.start(); StunDiscoveryReport report =
             * addressDiscovery .determineAddress(); if(report.getPublicAddress() != null) { globalIpAddress =
             * report.getPublicAddress().getSocketAddress().getAddress().getHostAddress(); globalPort =
             * report.getPublicAddress().getPort(); //TODO set a timer to retry the binding and provide a callback to
             * update the global ip address and port } else { useStun = false;
             * logger.error("Stun discovery failed to find a valid public ip address, disabling stun !"); }
             * logger.info("Stun report = " + report); addressDiscovery.shutDown(); } //TODO add it as a listener for
             * global ip address changes if STUN rediscover a new addess at some point }
             */
            result = ((UndertowSipStackImpl) sipStack).createListeningPoint(handler, signalingTransport);
            ListeningPointExt listeningPoint = (ListeningPointExt) result.getListeningPoint();
            if (useStun) {
                // TODO: (ISSUE-CONFUSION) Check what is the confusion here, why not use the globalport. It ends up
                // putting the local port everywhere
                // listeningPoint.setSentBy(globalIpAddress + ":" + globalPort);
                listeningPoint.setSentBy(globalIpAddress + ":" + port);
            }

            boolean createSipProvider = false;
            SipProvider sipProvider = null;
            if (sipStack.getSipProviders().hasNext()) {
                sipProvider = (SipProvider) sipStack.getSipProviders().next();
                for (ListeningPoint listeningPointTemp : sipProvider.getListeningPoints()) {
                    if (!(listeningPointTemp.getIPAddress().equalsIgnoreCase(listeningPoint.getIPAddress()) && listeningPointTemp
                            .getPort() == listeningPoint.getPort())) {
                        createSipProvider = true;
                        break;
                    }
                }
            } else {
                createSipProvider = true;
            }
            if (createSipProvider) {
                sipProvider = sipStack.createSipProvider(listeningPoint);
            } else {
                sipProvider.addListeningPoint(listeningPoint);
            }

            // creating the extended listening point
            extendedListeningPoint = new ExtendedListeningPoint(sipProvider, listeningPoint, sipConnector);
            extendedListeningPoint.setUseStaticAddress(false);
            extendedListeningPoint.setGlobalIpAddress(globalIpAddress);
            extendedListeningPoint.setGlobalPort(globalPort);

            // make the extended listening Point available to the service implementation
            setAttribute(ExtendedListeningPoint.class.getSimpleName(), extendedListeningPoint);
            SipApplicationDispatcher sipApplicationDispatcher = (SipApplicationDispatcher) getAttribute(SipApplicationDispatcher.class
                    .getSimpleName());
            // Jboss specific loading case
            if (sipApplicationDispatcher != null) {
                // Let's add it to hostnames, so that IP load balancer appears as localhost. Otherwise requestst
                // addressed there will
                // get forwarded and especially in case of failover this might be severe error.
                if (sipConnector.getStaticServerAddress() != null) {
                    sipApplicationDispatcher.addHostName(sipConnector.getStaticServerAddress() + ":"
                            + sipConnector.getStaticServerPort());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding hostname for IP load balancer " + sipConnector.getStaticServerAddress());
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Adding the Sip Application Dispatcher as a sip listener for connector listening on port "
                            + port);
                }
                sipProvider.addSipListener(sipApplicationDispatcher);
                sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(
                        extendedListeningPoint);
            }

            logger.info("Sip Connector started on ip address: " + ipAddress + ", port " + port + ", transport "
                    + signalingTransport + ", useStun " + sipConnector.isUseStun() + ", stunAddress "
                    + sipConnector.getStunServerAddress() + ", stunPort : " + sipConnector.getStunServerPort()
                    + ", useStaticAddress: " + sipConnector.isUseStaticAddress() + ", staticServerAddress "
                    + sipConnector.getStaticServerAddress() + ", staticServerPort "
                    + sipConnector.getStaticServerPort());

            // TODO: if (this.domain != null) {
            // rgOname=new ObjectName
            // (domain + ":type=GlobalRequestProcessor,name=" + getName());
            // Registry.getRegistry(null, null).registerComponent
            // ( sipStack, rgOname, null );
            // }
            // setStarted(true);
        } catch (Exception ex) {
            logger.error("A problem occured while setting up SIP Connector " + ipAddress + ":" + port + "/"
                    + signalingTransport + "-- check server.xml for tomcat. ", ex);
        } // finally {
        // if(!isStarted()) {
        // destroy();
        // }
        // }
        return result;
    }

    public void destroy() throws Exception {
        if(logger.isDebugEnabled()) {
            logger.debug("Stopping a sip protocol handler");
        }
        //Jboss specific unloading case
        SipApplicationDispatcher sipApplicationDispatcher = (SipApplicationDispatcher)
            getAttribute(SipApplicationDispatcher.class.getSimpleName());
        if(sipApplicationDispatcher != null && extendedListeningPoint != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("Removing the Sip Application Dispatcher as a sip listener for listening point " + extendedListeningPoint);
            }
            extendedListeningPoint.getSipProvider().removeSipListener(sipApplicationDispatcher);
            sipApplicationDispatcher.getSipNetworkInterfaceManager().removeExtendedListeningPoint(extendedListeningPoint);
        }
        // removing listening point and sip provider
        if(sipStack != null) {
            if(extendedListeningPoint != null) {
                if(extendedListeningPoint.getSipProvider().getListeningPoints().length == 1) {
                    sipStack.deleteSipProvider(extendedListeningPoint.getSipProvider());
                    if(logger.isDebugEnabled()) {
                        logger.debug("Removing the sip provider");
                    }
                } else {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Removing the following Listening Point " + extendedListeningPoint + " from the sip provider");
                    }
                    extendedListeningPoint.getSipProvider().removeListeningPoint(extendedListeningPoint.getListeningPoint());
                }
                if(logger.isDebugEnabled()) {
                    logger.debug("Removing the following Listening Point " + extendedListeningPoint);
                }               
                sipStack.deleteListeningPoint(extendedListeningPoint.getListeningPoint());
                extendedListeningPoint = null;
            }               
        }
        if (tpOname != null)
            //TODO:Registry.getRegistry(null, null).unregisterComponent(tpOname);
        if (rgOname != null)
            //TODO:Registry.getRegistry(null, null).unregisterComponent(rgOname);
        //TODO:setStarted(false);
        sipStack = null;
    }

    public void resume(){
        
    }
    
    /**
     * @param sipConnector the sipConnector to set
     */
    public void setSipConnector(SipConnector sipConnector) {
        this.sipConnector = sipConnector;
    }

    /**
     * @return the sipConnector
     */
    public SipConnector getSipConnector() {
        return sipConnector;
    }

    public void setAttribute(String arg0, Object arg1) {
        attributes.put(arg0, arg1);
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    // -------------------- JMX related methods --------------------

    // *
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        oname = name;
        mserver = server;
        domain = name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public void setSipStack(SipStack sipStack) {
        this.sipStack = sipStack;
    }

}
