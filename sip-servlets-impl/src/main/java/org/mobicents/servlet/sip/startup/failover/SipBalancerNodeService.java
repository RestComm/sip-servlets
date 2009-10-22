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

import java.io.IOException;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * @deprecated
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

	public boolean addBalancer(String addr, int sipPort, int rmiPort)
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
	public boolean addBalancer(String hostName, int sipPort, int index, int rmiPort)
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
	public boolean removeBalancer(String addr, int sipPort, int rmiPort)
			throws IllegalArgumentException;

	public boolean removeBalancer(String hostName, int sipPort, int index, int rmiPort)
			throws IllegalArgumentException;

	
	// --------------- GETTERS AND SETTERS

	public long getHeartBeatInterval();

	public void setHeartBeatInterval(long heartBeatInterval);
}
