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
package org.mobicents.servlet.sip.example;

import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.sip.SipSession;


public class Call
{
	private String from;
	private String to;
	private String status;
	private HashSet<SipSession> sessions = new HashSet<SipSession>();

	public Call(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}
	
	public void addSession(SipSession session) {
		this.sessions.add(session);
	}

	public void end() {
		Iterator it = this.sessions.iterator();
		while(it.hasNext()) {
			SipSession session = (SipSession) it.next();
			try {
				session.createRequest("BYE").send();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Call other = (Call) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
}