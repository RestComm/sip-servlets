/*
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
package org.mobicents.servlet.sip.startup.failover;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;
import org.mobicents.servlet.sip.startup.SipStandardService;
import org.mobicents.servlet.sip.utils.Inet6Util;
import org.mobicents.tools.sip.balancer.NodeRegisterRMIStub;
import org.mobicents.tools.sip.balancer.SIPNode;

/**
 *  <p>Sip Servlet implementation of the <code>Service</code> interface.</p>
 *   
 *  <p>This implementation extends the <code>SipStandardService</code> (that allows Tomcat to become a converged container)
 *  with the failover features.<br/>
 *  This implementation will send heartbeats and health information to the sip balancers configured for this service 
 *  (configured in the server.xml as balancers attribute fo the Service Tag)</p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipStandardBalancerNodeService extends SipStandardService implements SipBalancerNodeService {
	private static final String BALANCER_SIP_PORT_CHAR_SEPARATOR = ":";
	private static final String BALANCERS_CHAR_SEPARATOR = ";";
	private static final int DEFAULT_LB_SIP_PORT = 5065;
	//the logger
	private static transient Logger logger = Logger.getLogger(SipStandardBalancerNodeService.class);
	/**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.mobicents.servlet.sip.startup.failover.SipStandardBalancerNodeService/1.0";
    
    //the balancers to send heartbeat to and our health info
	private String balancers;
    //the balancers names to send heartbeat to and our health info
	private Map<String, BalancerDescription> register = new ConcurrentHashMap<String, BalancerDescription>();
	//heartbeat interval, can be modified through JMX
	private long heartBeatInterval = 5000;
	private Timer heartBeatTimer = new Timer();
	private TimerTask hearBeatTaskToRun = null;

    private boolean started = false;
    
    private boolean displayBalancerWarining = true;
    private boolean displayBalancerFound = true;
    
    @Override
    public String getInfo() {
        return (info);
    }
    
    @Override
    public void initialize() throws LifecycleException {
    	super.initialize();    	
    }
    
    @Override
    public void start() throws LifecycleException {
    	super.start();
    	if (!started) {
			if (balancers != null && balancers.length() > 0) {
				String[] balancerDescriptions = balancers.split(BALANCERS_CHAR_SEPARATOR);
				for (String balancerDescription : balancerDescriptions) {
					String balancerAddress = balancerDescription;
					int sipPort = DEFAULT_LB_SIP_PORT;
					if(balancerDescription.indexOf(BALANCER_SIP_PORT_CHAR_SEPARATOR) != -1) {
						String[] balancerDescriptionSplitted = balancerDescription.split(BALANCER_SIP_PORT_CHAR_SEPARATOR);
						balancerAddress = balancerDescriptionSplitted[0];
						try {
							sipPort = Integer.parseInt(balancerDescriptionSplitted[1]);
						} catch (NumberFormatException e) {
							throw new LifecycleException("Impossible to parse the following sip balancer port " + balancerDescriptionSplitted[1], e);
						}
					} 
					if(Inet6Util.isValidIP6Address(balancerAddress) || Inet6Util.isValidIPV4Address(balancerAddress)) {
						try {
							this.addBalancer(InetAddress.getByName(balancerAddress).getHostAddress(), sipPort);
						} catch (UnknownHostException e) {
							throw new LifecycleException("Impossible to parse the following sip balancer address " + balancerAddress, e);
						}
					} else {
						this.addBalancer(balancerAddress, sipPort, 0);
					}
				}
			}		
			started = true;
		}
		this.hearBeatTaskToRun = new BalancerPingTimerTask();
		this.heartBeatTimer.scheduleAtFixedRate(this.hearBeatTaskToRun, 0,
				this.heartBeatInterval);
		if(logger.isDebugEnabled()) {
			logger.debug("Created and scheduled tasks for sending heartbeats to the sip balancer.");
		}
    }
    
    @Override
    public void stop() throws LifecycleException {
    	// Force removal from load balancer upon shutdown 
    	// added for Issue 308 (http://code.google.com/p/mobicents/issues/detail?id=308)
    	ArrayList<SIPNode> info = getConnectorsAsSIPNode();
    	removeNodesFromBalancers(info);
    	//cleaning 
//    	balancerNames.clear();
    	register.clear();
    	if(hearBeatTaskToRun != null) {
    		this.hearBeatTaskToRun.cancel();
    	}
		this.hearBeatTaskToRun = null;
		started = false;
    	super.stop();    	
    }
	
    /**
     * {@inheritDoc}
     */
	public long getHeartBeatInterval() {
		return heartBeatInterval;
	}
	/**
     * {@inheritDoc}
     */
	public void setHeartBeatInterval(long heartBeatInterval) {
		if (heartBeatInterval < 100)
			return;
		this.heartBeatInterval = heartBeatInterval;
		this.hearBeatTaskToRun.cancel();
		this.hearBeatTaskToRun = new BalancerPingTimerTask();
		this.heartBeatTimer.scheduleAtFixedRate(this.hearBeatTaskToRun, 0,
				this.heartBeatInterval);

	}

	/**
	 * 
	 * @param hostName
	 * @param index
	 * @return
	 */
	private InetAddress fetchHostAddress(String hostName, int index) {
		if (hostName == null)
			throw new NullPointerException("Host name cant be null!!!");

		InetAddress[] hostAddr = null;
		try {
			hostAddr = InetAddress.getAllByName(hostName);
		} catch (UnknownHostException uhe) {
			throw new IllegalArgumentException(
					"HostName is not a valid host name or it doesnt exists in DNS",
					uhe);
		}

		if (index < 0 || index >= hostAddr.length) {
			throw new IllegalArgumentException(
					"Index in host address array is wrong, it should be [0]<x<["
							+ hostAddr.length + "] and it is [" + index + "]");
		}

		InetAddress address = hostAddr[index];
		return address;
	}

	/**
     * {@inheritDoc}
     */
	public String[] getBalancers() {
		return this.register.keySet().toArray(new String[register.keySet().size()]);
	}

	/**
     * {@inheritDoc}
     */
	public boolean addBalancer(String addr, int sipPort) {
		if (addr == null)
			throw new NullPointerException("addr cant be null!!!");

		InetAddress address = null;
		try {
			address = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(
					"Somethign wrong with host creation.", e);
		}		
		String balancerName = address.getCanonicalHostName();

		if (register.get(balancerName) != null) {
			logger.info("Sip balancer " + balancerName + " already present, not added");
			return false;
		}

		if(logger.isDebugEnabled()) {
			logger.debug("Adding following balancer name : " + balancerName +"/address:"+ addr);
		}

		BalancerDescription balancerDescription = new BalancerDescription(address, sipPort);
		register.put(balancerName, balancerDescription);

		//notify the sip factory 
		if(sipApplicationDispatcher.getSipFactory().getLoadBalancerToUse() == null) {
			sipApplicationDispatcher.getSipFactory().setLoadBalancerToUse(balancerDescription);
		}
		
		return true;
	}

	/**
     * {@inheritDoc}
     */
	public boolean addBalancer(String hostName, int sipPort, int index) {
		return this.addBalancer(fetchHostAddress(hostName, index)
				.getHostAddress(), sipPort);
	}

	/**
     * {@inheritDoc}
     */
	public boolean removeBalancer(String addr, int sipPort) {
		if (addr == null)
			throw new NullPointerException("addr cant be null!!!");

		InetAddress address = null;
		try {
			address = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(
					"Something wrong with host creation.", e);
		}

		BalancerDescription balancerDescription = new BalancerDescription(address, sipPort);

		String keyToRemove = null;
		Iterator<String> keyIterator = register.keySet().iterator();
		while (keyIterator.hasNext() && keyToRemove ==null) {
			String key = keyIterator.next();
			if(register.get(key).equals(balancerDescription)) {
				keyToRemove = key;
			}
		}
		
		if(keyToRemove !=null ) {
			if(logger.isDebugEnabled()) {
				logger.debug("Removing following balancer name : " + keyToRemove +"/address:"+ addr);
			}
			register.remove(keyToRemove);
			
			if(sipApplicationDispatcher.getSipFactory().getLoadBalancerToUse() != null && 
					sipApplicationDispatcher.getSipFactory().getLoadBalancerToUse().equals(balancerDescription)) {
				sipApplicationDispatcher.getSipFactory().setLoadBalancerToUse(null);
			}
			return true;
		}

		return false;
	}

	/**
     * {@inheritDoc}
     */
	public boolean removeBalancer(String hostName, int sipPort, int index) {
		InetAddress[] hostAddr = null;
		try {
			hostAddr = InetAddress.getAllByName(hostName);
		} catch (UnknownHostException uhe) {
			throw new IllegalArgumentException(
					"HostName is not a valid host name or it doesnt exists in DNS",
					uhe);
		}

		if (index < 0 || index >= hostAddr.length) {
			throw new IllegalArgumentException(
					"Index in host address array is wrong, it should be [0]<x<["
							+ hostAddr.length + "] and it is [" + index + "]");
		}

		InetAddress address = hostAddr[index];

		return this.removeBalancer(address.getHostAddress(), sipPort);
	}

	private ArrayList<SIPNode> getConnectorsAsSIPNode() {
		ArrayList<SIPNode> info = new ArrayList<SIPNode>();
		// Gathering info about server' sip listening points
		for (Connector connector : connectors) {
			ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if(protocolHandler instanceof SipProtocolHandler) {
				SipProtocolHandler sipProtocolHandler = (SipProtocolHandler) protocolHandler;
				String address = sipProtocolHandler.getIpAddress();
				// From Vladimir: for some reason I get "localhost" here instead of IP and this confiuses the LB
				if(address.equals("localhost")) address = "127.0.0.1";
				
				int port = sipProtocolHandler.getPort();
				String transport = sipProtocolHandler.getSignalingTransport();
				String[] transports = new String[] {transport};
				
				String hostName = null;
				try {
					InetAddress[] aArray = InetAddress
							.getAllByName(address);
					if (aArray != null && aArray.length > 0) {
						// Damn it, which one we should pick?
						hostName = aArray[0].getCanonicalHostName();
					}
				} catch (UnknownHostException e) {
					logger.error("An exception occurred while trying to retrieve the hostname of a sip connector", e);
				}
				Engine e = null;
				for (Container c = connector.getContainer(); e == null && c != null; c = c.getParent())
				{
					if (c != null && c instanceof Engine)
					{
						e = (Engine) c;
					}
				}
				SIPNode node = new SIPNode(hostName, address, port,
						transports, e.getJvmRoute());

				info.add(node);
			}
		}
		return info;
	}
	
	/**
	 * @param info
	 */
	private void sendKeepAliveToBalancers(ArrayList<SIPNode> info) {
		for(BalancerDescription  balancerDescription:new HashSet<BalancerDescription>(register.values())) {
			try {
				Registry registry = LocateRegistry.getRegistry(balancerDescription.getAddress().getHostAddress(),2000);
				NodeRegisterRMIStub reg=(NodeRegisterRMIStub) registry.lookup("SIPBalancer");
				reg.handlePing(info);
				displayBalancerWarining = true;
				if(displayBalancerFound) {
					logger.info("SIP Load Balancer Found!");
					displayBalancerFound = false;
				}
			} catch (Exception e) {
				if(displayBalancerWarining) {
					logger.warn("Cannot access the SIP load balancer RMI registry: " + e.getMessage() +
							"\nIf you need a cluster configuration make sure the SIP load balancer is running.");
					logger.error("Cannot access the SIP load balancer RMI registry: " , e);
					displayBalancerWarining = false;
				}
				displayBalancerFound = true;
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Finished gathering");
			logger.debug("Gathered info[" + info + "]");
		}
	}
	
	/**
	 * @param info
	 */
	public void sendSwitchoverInstruction(String fromJvmRoute, String toJvmRoute) {
		logger.info("switching over from " + fromJvmRoute + " to " + toJvmRoute);
		if(fromJvmRoute == null || toJvmRoute == null) {
			return;
		}
		for(BalancerDescription  balancerDescription:new HashSet<BalancerDescription>(register.values())) {
			try {
				Registry registry = LocateRegistry.getRegistry(balancerDescription.getAddress().getHostAddress(),2000);
				NodeRegisterRMIStub reg=(NodeRegisterRMIStub) registry.lookup("SIPBalancer");
				reg.switchover(fromJvmRoute, toJvmRoute);
				displayBalancerWarining = true;
				if(displayBalancerFound) {
					logger.info("SIP Load Balancer Found!");
					displayBalancerFound = false;
				}
			} catch (Exception e) {
				if(displayBalancerWarining) {
					logger.warn("Cannot access the SIP load balancer RMI registry: " + e.getMessage() +
							"\nIf you need a cluster configuration make sure the SIP load balancer is running.");
					logger.error("Cannot access the SIP load balancer RMI registry: " , e);
					displayBalancerWarining = false;
				}
				displayBalancerFound = true;
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Finished gathering");
			logger.debug("Gathered info[" + info + "]");
		}
	}
	
	/**
	 * @param info
	 */
	private void removeNodesFromBalancers(ArrayList<SIPNode> info) {
		for(BalancerDescription balancerDescription:new HashSet<BalancerDescription>(register.values())) {
			try {
				Registry registry = LocateRegistry.getRegistry(balancerDescription.getAddress().getHostAddress(),2000);
				NodeRegisterRMIStub reg=(NodeRegisterRMIStub) registry.lookup("SIPBalancer");
				reg.forceRemoval(info);
				displayBalancerWarining = true;
				if(displayBalancerFound) {
					logger.info("SIP Load Balancer Found!");
					displayBalancerFound = false;
				}
			} catch (Exception e) {
				if(displayBalancerWarining) {
					logger.warn("Cannot access the SIP load balancer RMI registry: " + e.getMessage() +
							"\nIf you need a cluster configuration make sure the SIP load balancer is running.");
					logger.error("Cannot access the SIP load balancer RMI registry: " , e);
					displayBalancerWarining = false;
				}
				displayBalancerFound = true;
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Finished gathering");
			logger.debug("Gathered info[" + info + "]");
		}
	}
	
	/**
	 * 
	 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
	 *
	 */
	class BalancerPingTimerTask extends TimerTask {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if(logger.isDebugEnabled()) {
				logger.debug("Start");
			}
			ArrayList<SIPNode> info = getConnectorsAsSIPNode();						
			sendKeepAliveToBalancers(info);
		}
	}

	/**
	 * @param balancers the balancers to set
	 */
	public void setBalancers(String balancers) {
		this.balancers = balancers;
	}
}
