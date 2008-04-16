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

	public boolean equals(Object a) {
		Call other = (Call) a;
		if(other.from.equals(from) && other.to.equals(to)) return true;
		return false;
	}

	public int hashCode() {
		return from.hashCode()^to.hashCode();
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
}