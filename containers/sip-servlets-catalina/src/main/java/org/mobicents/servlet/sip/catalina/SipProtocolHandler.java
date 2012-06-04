/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.catalina;

import gov.nist.javax.sip.ListeningPointExt;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.SipStack;

import net.java.stun4j.StunAddress;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;
import net.java.stun4j.client.StunDiscoveryReport;

import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

/**
 * This is the sip protocol handler that will get called upon creation of the
 * tomcat connector defined in the server.xml.<br/> To use a sip connector, one
 * need to specify a new connector in server.xml with
 * org.mobicents.servlet.sip.startup.SipProtocolHandler as the value for the
 * protocol attribute.<br/>
 * 
 * Some of the fields (representing the sip stack propeties) get populated
 * automatically by the container.<br/>
 * 
 * @author Jean Deruelle
 * 
 */
public class SipProtocolHandler implements ProtocolHandler, MBeanRegistration {
	
	public static final String IS_SIP_CONNECTOR = "isSipConnector";	
	// the logger
	private static final Logger logger = Logger.getLogger(SipProtocolHandler.class.getName());
	// *
    protected ObjectName tpOname = null;
    // *
    protected ObjectName rgOname = null;
	/**
     * The random port number generator that we use in getRandomPortNumer()
     */
    private static Random portNumberGenerator = new Random();
    
	private Adapter adapter = null;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private SipStack sipStack;

	/*
	 * the extended listening point with global ip address and port for it
	 */
	public ExtendedListeningPoint extendedListeningPoint;

	// defining sip stack properties
	private Properties sipStackProperties;
	
	private boolean started = false;

	private SipConnector sipConnector;
	
	public SipProtocolHandler() {
		sipConnector = new SipConnector();
	}
	
	public SipProtocolHandler(SipConnector connector) {
		sipConnector = connector;
	}
	
	/**
	 * {@inheritDoc}
	 */
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
            Registry.getRegistry(null, null).unregisterComponent(tpOname);
        if (rgOname != null)
            Registry.getRegistry(null, null).unregisterComponent(rgOname);
        setStarted(false);
        sipStack = null;
	}

	public Adapter getAdapter() {		
		return adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAttribute(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator getAttributeNames() {
		return attributes.keySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws Exception {		
		setAttribute(IS_SIP_CONNECTOR,Boolean.TRUE);
	}

	public void pause() throws Exception {
		//This is optionnal, no implementation there

	}

	public void resume() throws Exception {
		// This is optionnal, no implementation there

	}

	public void setAdapter(Adapter adapter) {
		this.adapter = adapter;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}		
	
	public void start() throws Exception {
		
		if(logger.isDebugEnabled()) {
			logger.debug("Starting a sip protocol handler");
		}
		Integer portFromConfig = sipConnector.getPort();
		String set = System.getProperty("jboss.service.binding.set");
		int setIncrememt = 0;
		if(set != null) {
			int setNumber = set.charAt(set.length() - 1) - '0';
			if(setNumber>0 && setNumber<10) {
				setIncrememt = (setNumber) * 100; // don't attempt to compute if the port set is custom outside that range
			}
			logger.info("Computed port increment for MSS SIP ports is " + setIncrememt + " from " + set);
		}
		portFromConfig += setIncrememt;
	    sipConnector.setPort(portFromConfig);
		
		final String ipAddress = sipConnector.getIpAddress();
		final int port = sipConnector.getPort();
		final String signalingTransport = sipConnector.getTransport();
		
		try {	
			//checking the external ip address if stun enabled			
			String globalIpAddress = null;			
			int globalPort = -1;
			boolean useStun = sipConnector.isUseStun();
			if (useStun) {
				if(InetAddress.getByName(ipAddress).isLoopbackAddress()) {
					logger.warn("The Ip address provided is the loopback address, stun won't be enabled for it");
				} else {
					//chooses stun port randomly
					DatagramSocket randomSocket = initRandomPortSocket();
					int randomPort = randomSocket.getLocalPort();
					randomSocket.disconnect();
					randomSocket.close();
					randomSocket = null;
					StunAddress localStunAddress = new StunAddress(ipAddress,
							randomPort);
	
					StunAddress serverStunAddress = new StunAddress(
							sipConnector.getStunServerAddress(), sipConnector.getStunServerPort());
	
					NetworkConfigurationDiscoveryProcess addressDiscovery = new NetworkConfigurationDiscoveryProcess(
							localStunAddress, serverStunAddress);
					addressDiscovery.start();
					StunDiscoveryReport report = addressDiscovery
							.determineAddress();
					if(report.getPublicAddress() != null) {
						globalIpAddress = report.getPublicAddress().getSocketAddress().getAddress().getHostAddress();
						globalPort = report.getPublicAddress().getPort();
						//TODO set a timer to retry the binding and provide a callback to update the global ip address and port
					} else {
						useStun = false;
						logger.error("Stun discovery failed to find a valid public ip address, disabling stun !");
					}
					logger.info("Stun report = " + report);
					addressDiscovery.shutDown();
				}
				//TODO add it as a listener for global ip address changes if STUN rediscover a new addess at some point
			}
			
			ListeningPointExt listeningPoint = (ListeningPointExt) sipStack.createListeningPoint(ipAddress,
					port, signalingTransport);
			if(useStun) {
				// TODO: (ISSUE-CONFUSION) Check what is the confusion here, why not use the globalport. It ends up putting the local port everywhere
//				listeningPoint.setSentBy(globalIpAddress + ":" + globalPort);
				listeningPoint.setSentBy(globalIpAddress + ":" + port);
			}

			boolean createSipProvider = false;
			SipProvider sipProvider = null;
			if(sipStack.getSipProviders().hasNext()) {
				sipProvider = (SipProvider) sipStack.getSipProviders().next();				
				for (ListeningPoint listeningPointTemp : sipProvider.getListeningPoints()) {
					if(!(listeningPointTemp.getIPAddress().equalsIgnoreCase(listeningPoint.getIPAddress()) && listeningPointTemp.getPort() == listeningPoint.getPort())) {
						createSipProvider = true;
						break;
					}
				}					
			} else {
				createSipProvider = true;
			}
			if(createSipProvider) {
				sipProvider = sipStack.createSipProvider(listeningPoint);
			} else {
				sipProvider.addListeningPoint(listeningPoint);
			}
			
			//creating the extended listening point
			extendedListeningPoint = new ExtendedListeningPoint(sipProvider, listeningPoint, sipConnector);
			extendedListeningPoint.setUseStaticAddress(false);
			extendedListeningPoint.setGlobalIpAddress(globalIpAddress);
			extendedListeningPoint.setGlobalPort(globalPort);
		
			
			//make the extended listening Point available to the service implementation			
			setAttribute(ExtendedListeningPoint.class.getSimpleName(), extendedListeningPoint);
			SipApplicationDispatcher sipApplicationDispatcher = (SipApplicationDispatcher)
				getAttribute(SipApplicationDispatcher.class.getSimpleName());
			//Jboss specific loading case
			if(sipApplicationDispatcher != null) {
				// Let's add it to hostnames, so that IP load balancer appears as localhost. Otherwise requestst addressed there will
				// get forwarded and especially in case of failover this might be severe error.
				if(sipConnector.getStaticServerAddress() != null) {
					sipApplicationDispatcher.addHostName(sipConnector.getStaticServerAddress() + ":" + sipConnector.getStaticServerPort());
					if(logger.isDebugEnabled()) {
						logger.debug("Adding hostname for IP load balancer " + sipConnector.getStaticServerAddress());
					}
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("Adding the Sip Application Dispatcher as a sip listener for connector listening on port " + port);
				}
				sipProvider.addSipListener(sipApplicationDispatcher);
				sipApplicationDispatcher.getSipNetworkInterfaceManager().addExtendedListeningPoint(extendedListeningPoint);
			}
			
			logger.info("Sip Connector started on ip address : " + ipAddress
					+ ",port " + port + ", transport " + signalingTransport + ", useStun " + useStun + ", stunAddress " + sipConnector.getStunServerAddress() + ", stunPort : " + sipConnector.getStaticServerPort());
			
			if (this.domain != null) {
//	            try {
//	                tpOname = new ObjectName
//	                    (domain + ":" + "type=ThreadPool,name=" + getName());
//	                Registry.getRegistry(null, null)
//	                    .registerComponent(endpoint, tpOname, null );
//	            } catch (Exception e) {
//	                logger.error("Can't register endpoint");
//	            }
	            rgOname=new ObjectName
	                (domain + ":type=GlobalRequestProcessor,name=" + getName());
	            Registry.getRegistry(null, null).registerComponent
	                ( sipStack, rgOname, null );
	        }
			setStarted(true);
		} catch (Exception ex) {			
			logger.error(
					"A problem occured while setting up SIP Connector " + ipAddress + ":" + port + "/" + signalingTransport + "-- check server.xml for tomcat. ", ex);						
		} finally {
			if(!isStarted()) {
				destroy();
			}
		}
	}	
	
	/**
     * Initializes and binds a socket that on a random port number. The method
     * would try to bind on a random port and retry 5 times until a free port
     * is found.
     *
     * @return the socket that we have initialized on a randomport number.
     */
    private DatagramSocket initRandomPortSocket() {
        int bindRetries = 5;
        int currentlyTriedPort = 
        	getRandomPortNumber(JainSipUtils.MIN_PORT_NUMBER, JainSipUtils.MAX_PORT_NUMBER);

        DatagramSocket resultSocket = null;
        //we'll first try to bind to a random port. if this fails we'll try
        //again (bindRetries times in all) until we find a free local port.
        for (int i = 0; i < bindRetries; i++) {
            try {
                resultSocket = new DatagramSocket(currentlyTriedPort);
                //we succeeded - break so that we don't try to bind again
                break;
            }
            catch (SocketException exc) {
                if (exc.getMessage().indexOf("Address already in use") == -1) {
                    logger.fatal("An exception occurred while trying to create"
                                 + "a local host discovery socket.", exc);                    
                    return null;
                }
                //port seems to be taken. try another one.
                logger.debug("Port " + currentlyTriedPort + " seems in use.");
                currentlyTriedPort = 
                	getRandomPortNumber(JainSipUtils.MIN_PORT_NUMBER, JainSipUtils.MAX_PORT_NUMBER);
                logger.debug("Retrying bind on port " + currentlyTriedPort);
            }
        }

        return resultSocket;
    }
	
	/**
     * Returns a random local port number, greater than min and lower than max.
     *
     * @param min the minimum allowed value for the returned port number.
     * @param max the maximum allowed value for the returned port number.
     *
     * @return a random int located between greater than min and lower than max.
     */
    public static int getRandomPortNumber(int min, int max) {
        return portNumberGenerator.nextInt(max - min) + min;
    }
			

	/**
	 * @return the signalingTransport
	 */
	public String getSignalingTransport() {
		return sipConnector.getTransport();
	}

	/**
	 * @param signalingTransport
	 *            the signalingTransport to set
	 * @throws Exception 
	 */
	public void setSignalingTransport(String transport) throws Exception {
		sipConnector.setTransport(transport);
		if(isStarted()) {
			destroy();
			start();
		}
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return sipConnector.getPort();
	}

	/**
	 * @param port
	 *            the port to set
	 * @throws Exception 
	 */
	public void setPort(int port) throws Exception {
		sipConnector.setPort(port);
		if(isStarted()) {
			destroy();
			start();
		}
	}
		
	public void setIpAddress(String ipAddress) throws Exception {
		sipConnector.setIpAddress(ipAddress);
		if(isStarted()) {
			destroy();
			start();
		}
	}

	public String getIpAddress() {
		return sipConnector.getIpAddress();
	}

	/**
	 * @return the stunServerAddress
	 */
	public String getStunServerAddress() {
		return sipConnector.getStunServerAddress();
	}

	/**
	 * @param stunServerAddress the stunServerAddress to set
	 */
	public void setStunServerAddress(String stunServerAddress) {
		sipConnector.setStunServerAddress(stunServerAddress);
	}

	/**
	 * @return the stunServerPort
	 */
	public int getStunServerPort() {
		return sipConnector.getStunServerPort();
	}

	/**
	 * @param stunServerPort the stunServerPort to set
	 */
	public void setStunServerPort(int stunServerPort) {
		sipConnector.setStunServerPort(stunServerPort);
	}

	/**
	 * @return the useStun
	 */
	public boolean isUseStun() {
		return sipConnector.isUseStun();
	}

	/**
	 * @param useStun the useStun to set
	 */
	public void setUseStun(boolean useStun) {
		sipConnector.setUseStun(useStun);
	}

	public String getStaticServerAddress() {
		return sipConnector.getStaticServerAddress();
	}

	public void setStaticServerAddress(String staticServerAddress) {
		sipConnector.setStaticServerAddress(staticServerAddress);
	}

	public int getStaticServerPort() {
		return sipConnector.getStaticServerPort();
	}

	public void setStaticServerPort(int staticServerPort) {
		sipConnector.setStaticServerPort(staticServerPort);
	}

	public boolean isUseStaticAddress() {
		return sipConnector.isUseStaticAddress();
	}

	public void setUseStaticAddress(boolean useStaticAddress) {
		sipConnector.setUseStaticAddress(useStaticAddress);
	}
	
	public InetAddress getAddress() {
		try {
			return InetAddress.getByName(sipConnector.getIpAddress());
		} catch (UnknownHostException e) {
			logger.error("unexpected exception while getting the ipaddress of the sip protocol handler", e);
			return null;
		}
	}
    public void setAddress(InetAddress ia) { 
    	sipConnector.setIpAddress(ia.getHostAddress());
    }
    
    public String getTransport() {
    	return sipConnector.getTransport();
    }

	/**
	 * @return the sipStackProperties
	 */
	public Properties getSipStackProperties() {
		return sipStackProperties;
	}
	
	/**
	 * @param sipStackProperties the sipStackProperties to set
	 */
	public void setSipStackProperties(Properties sipStackProperties) {
		this.sipStackProperties = sipStackProperties;
	}
    
	public boolean isReplaceStaticServerAddressForInternalRoutingRequest() {
		return sipConnector.isReplaceStaticServerAddressForInternalRoutingRequest();
	}
	
	public void setReplaceStaticServerAddressForInternalRoutingRequest(
			boolean value) {
		sipConnector.setReplaceStaticServerAddressForInternalRoutingRequest(value);
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

	public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
            encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }
        return ("sip-" + getTransport() + "-" + encodedAddr + getPort());
    }
    
    // -------------------- JMX related methods --------------------

   
	// *
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }	

	/**
	 * @param started the started to set
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * @return the started
	 */
	public boolean isStarted() {
		return started;
	}
	
	public void setSipStack(SipStack sipStack) {
		this.sipStack = sipStack;
	}
}
