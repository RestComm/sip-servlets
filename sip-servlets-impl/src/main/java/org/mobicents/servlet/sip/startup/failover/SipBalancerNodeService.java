package org.mobicents.servlet.sip.startup.failover;

import java.io.IOException;

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
	public String[] getBalancers();

	public boolean addBalancerAddress(String addr)
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
	 * @throws IllegalArgumentException if something goes wrong when adding the balancer address or while trying to connect to it
	 */
	public boolean addBalancerAddress(String hostName, int index)
			throws IllegalArgumentException;

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
	 */
	public boolean removeBalancerAddress(String addr)
			throws IllegalArgumentException;

	public boolean removeBalancerAddress(String hostName, int index)
			throws IllegalArgumentException;

	public void removeBalancerAddress(int index)
			throws IllegalArgumentException;
	
	// --------------- GETTERS AND SETTERS

	public long getHeartBeatInterval();

	public void setHeartBeatInterval(long heartBeatInterval);
}
