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

import org.mobicents.servlet.sip.startup.SipStandardService;

/**
 *  <p>Sip Servlet implementation of the <code>Service</code> interface.</p>
 *   
 *  <p>This implementation extends the <code>SipStandardService</code> (that allows Tomcat to become a converged container)
 *  with the failover features.<br/>
 *  This implementation will send heartbeats and health information to the sip balancers configured for this service 
 *  (configured in the server.xml as balancers attribute fo the Service Tag)</p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * @deprecated
 */
public class SipStandardBalancerNodeService extends SipStandardService implements SipBalancerNodeService {

	public boolean addBalancer(String addr, int sipPort, int rmiPort)
			throws IllegalArgumentException, NullPointerException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addBalancer(String hostName, int sipPort, int index,
			int rmiPort) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] getBalancers() {
		// TODO Auto-generated method stub
		return new String[]{};
	}

	public long getHeartBeatInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean removeBalancer(String addr, int sipPort, int rmiPort)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeBalancer(String hostName, int sipPort, int index,
			int rmiPort) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setHeartBeatInterval(long heartBeatInterval) {
		// TODO Auto-generated method stub
		
	}
	
}
