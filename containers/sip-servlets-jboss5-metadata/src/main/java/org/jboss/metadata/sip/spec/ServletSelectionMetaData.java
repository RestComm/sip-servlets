/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.jboss.metadata.sip.spec;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.metadata.javaee.support.IdMetaDataImpl;
import org.mobicents.servlet.sip.ruby.SipRubyController;

/**
 * @author jean.deruelle@gmail.com
 * @version $Revision$
 */
@XmlType(name = "servlet-SelectionType")
public class ServletSelectionMetaData extends IdMetaDataImpl {
	private static final long serialVersionUID = 1;

	private String mainServlet;

	private SipRubyController sipRubyController;

	private List<SipServletMappingMetaData> sipServletMappings;

	public String getMainServlet() {
		return mainServlet;
	}

	public void setMainServlet(String mainServlet) {
		this.mainServlet = mainServlet;
	}

	public List<SipServletMappingMetaData> getSipServletMappings() {
		return sipServletMappings;
	}

	@XmlElement(name = "servlet-mapping")
	public void setSipServletMappings(
			List<SipServletMappingMetaData> sipServletMappings) {
		this.sipServletMappings = sipServletMappings;
	}

	public String toString() {
		StringBuilder tmp = new StringBuilder("ServletSelectionMetaData(id=");
		tmp.append(getId());
		tmp.append(",mainServlet=");
		tmp.append(mainServlet);
		tmp.append(')');
		return tmp.toString();
	}

	/**
	 * @param rubyController
	 *            the rubyController to set
	 */
	public void setSipRubyController(SipRubyController sipRubyController) {
		this.sipRubyController = sipRubyController;
	}

	/**
	 * @return the rubyController
	 */
	public SipRubyController getSipRubyController() {
		return sipRubyController;
	}
}
