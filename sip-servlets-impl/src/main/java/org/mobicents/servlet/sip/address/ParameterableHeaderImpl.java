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
package org.mobicents.servlet.sip.address;

import java.util.Map;

import javax.sip.header.Header;

import gov.nist.core.NameValueList;

/**
 * This class is impl of Parameterable object returned
 * 
 * @author baranowb
 * 
 */
public class ParameterableHeaderImpl extends ParameterableImpl {

	protected String value = null;

	public ParameterableHeaderImpl() {
		super();
	}

	public ParameterableHeaderImpl(Header header, String value, Map<String, String> params) {
		// General form of parametrable header
		super(header, params);
		this.value = value;		
	}

	@Override
	public Object clone() {
		ParameterableHeaderImpl cloned = new ParameterableHeaderImpl();
		cloned.parameters = (NameValueList) super.parameters.clone();
		cloned.value = new String(this.value);
		return cloned;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
