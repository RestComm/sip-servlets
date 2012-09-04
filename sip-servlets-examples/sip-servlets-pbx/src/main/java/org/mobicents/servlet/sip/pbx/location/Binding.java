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
package org.mobicents.servlet.sip.pbx.location;

/**
 * @author Thomas Leseney
 */
public class Binding {

	private String aor;
	private String contact;
	private String callId;
	private int cseq;
	private Long id;
	private int expires;
	private long expirationTime;
	
	public Binding() {
	}
	
	public Binding(String aor, String contact) {
		this.aor = aor;
		this.contact = contact;
	}
	
	public String getAor() {
		return aor;
	}
	
	public String getCallId() {
		return callId;
	}
	
	public int getCseq() {
		return cseq;
	}
	
	public String getContact() {
		return contact;
	}
	
	public long getExpirationTime() {
		return expirationTime;
	}
	
	public int getExpires() {
		return expires;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setAor(String aor) {
		this.aor = aor;
	}
	
	public void setCallId(String callId) {
		this.callId = callId;
	}
	
	public void setCseq(int cseq) {
		this.cseq = cseq;
	}
	
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
	}
	
	public void setExpires(int expires) {
		this.expires = expires;
		expirationTime = System.currentTimeMillis() + expires * 1000;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String toString() {
		return aor + "->" + contact + " (" + callId + "/" + cseq + "/" + expires +")";
	}
}
