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
package org.mobicents.servlet.sip.pbx;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Leseney
 */
public class ResponseException extends Exception {

	private int status;
	private String reason;
	
	private Map<String, String> headers;
	
	public ResponseException(int status) {
		this(status, null);
	}
	
	public ResponseException(int status, String reason) {
		super(status + (reason == null ? "" : " " + reason));
		this.status = status;
		this.reason = reason;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void addHeader(String name, String value) {
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		headers.put(name, value);
	}
}
