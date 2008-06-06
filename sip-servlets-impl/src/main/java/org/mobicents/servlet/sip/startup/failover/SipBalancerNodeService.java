package org.mobicents.servlet.sip.startup.failover;

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public interface SipBalancerNodeService {

	/**
	 * 
	 * @return - list of String objects representing balancer addresses. Example
	 *         content:
	 *         <ul>
	 *         <li>192.168.1.100</li>
	 *         <li>ala.ma.kota.pl</li>
	 *         </ul>
	 */
	public List<String> getBalancers();

	public boolean addBalancerAddress(byte[] addr)
			throws IllegalArgumentException, NullPointerException, IOException;

	/**
	 * Adds balancer address to distribution list. Tries to connect to it.
	 * 
	 * @param hostName -
	 *            name of the host to be looked up in DNS
	 * @param index -
	 *            possible index of IP address when host has more than one
	 *            address - like InetAddress.getAllByName(..);
	 * @return
	 *            <ul>
	 *            <li><b>true</b> - if address didnt exist and it has been
	 *            injected into list</li>
	 *            <li><b>false</b> - otherwise</li>
	 *            </ul>
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 */
	public boolean addBalancerAddress(String hostName, int index)
			throws IllegalArgumentException, NullPointerException, IOException;

	/**
	 * Tries to remove balancer with name: addr[0].addr[1].addr[2].addr[3]
	 * 
	 * @param addr -
	 *            The argument is address representation in network byte order:
	 *            the highest order byte of the address is in [0].
	 * @param port -
	 *            port on which remote balancer listens
	 * @return
	 *            <ul>
	 *            <li><b>true</b> - if name exists and was removed</li>
	 *            <li><b>false</b> - otherwise</li>
	 *            </ul>
	 * @throws IllegalArgumentException -
	 *             if there is no balancer with that name on the list.
	 * @throws NullPointerException -
	 *             if addr==null
	 */
	public boolean removeBalancerAddress(byte[] addr)
			throws IllegalArgumentException, NullPointerException;

	public boolean removeBalancerAddress(String hostName, int index)
			throws IllegalArgumentException, NullPointerException;

	public void removeBalancerAddress(int index)
			throws IllegalArgumentException;
	
	// --------------- GETTERS AND SETTERS

	public long getHeartBeatInterval();

	public void setHeartBeatInterval(long heartBeatInterval);

	// ******** METHODS FOR SOCKET VERSION

	// public boolean addBalancerAddress(byte[] addr, int port)
	// throws IllegalArgumentException, NullPointerException, IOException;

	/**
	 * Adds balancer address to distribution list. Tries to connect to it.
	 * 
	 * @param hostName -
	 *            name of the host to be looked up in DNS
	 * @param port -
	 *            port to connect to on remote host
	 * @param index -
	 *            possible index of IP address when host has more than one
	 *            address - like InetAddress.getAllByName(..);
	 * @return
	 *            <ul>
	 *            <li><b>true</b> - if address didnt exist and it has been
	 *            injected into list</li>
	 *            <li><b>false</b> - otherwise</li>
	 *            </ul>
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 */
	// public boolean addBalancerAddress(String hostName, int port, int index)
	// throws IllegalArgumentException, NullPointerException, IOException;
	/**
	 * Tries to remove balancer with name: addr[0].addr[1].addr[2].addr[3]:port
	 * 
	 * @param addr -
	 *            The argument is address representation in network byte order:
	 *            the highest order byte of the address is in [0].
	 * @param port -
	 *            port on which remote balancer listens
	 * @return
	 *            <ul>
	 *            <li><b>true</b> - if name exists and was removed</li>
	 *            <li><b>false</b> - otherwise</li>
	 *            </ul>
	 * @throws IllegalArgumentException -
	 *             if there is no balancer with that name on the list.
	 * @throws NullPointerException -
	 *             if addr==null
	 */
	// public boolean removeBalancerAddress(byte[] addr, int port)
	// throws IllegalArgumentException, NullPointerException;
	// public boolean removeBalancerAddress(String hostName, int port, int
	// index)
	// throws IllegalArgumentException, NullPointerException;
	// public void removeBalancerAddress(int index)
	// throws IllegalArgumentException;
	/**
	 * Checks wheather we are connected to a balancer with name at specified
	 * index
	 * 
	 * @param index -
	 *            index of the balancer name in list
	 * @return
	 *            <ul>
	 *            <li><b>true</b> - is connected</li>
	 *            <li><b>false</b> - is not</li>
	 *            </ul>
	 * @throws IllegalArgumentException -
	 *             wrong index
	 */
	// public boolean isConnectedToBalancer(int index)
	// throws IllegalArgumentException;
	/**
	 * Tries to connect to balancer with name at specified index
	 * 
	 * @param index -
	 *            of the name of the balancer
	 * @return
	 *            <ul>
	 *            <li><b>true</b> - if connection was success</li>
	 *            <li><b>false - otherwise</b></li>
	 *            </ul>
	 * @throws IllegalArgumentException -
	 *             wrong index
	 * @throws IllegalStateException -
	 *             already connected
	 */
	// public void connectoToBalancer(int index) throws
	// IllegalArgumentException,
	// IllegalStateException, IOException;
	/**
	 * Disconnects from balancer
	 * 
	 * @param index -
	 *            of the name of th ebalancer
	 * @return
	 *            <ul>
	 *            <li><b>true</b></li>
	 *            <li><b>false</b></li>
	 *            </ul>
	 * @throws IllegalArgumentException -
	 *             wrong index
	 * @throws IllegalStateException -
	 *             already disconnected
	 */
	// public void disconnectFromBalancer(int index)
	// throws IllegalArgumentException, IllegalStateException;
}
