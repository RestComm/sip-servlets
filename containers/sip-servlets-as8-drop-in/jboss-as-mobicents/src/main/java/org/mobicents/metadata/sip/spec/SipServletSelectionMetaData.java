/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.metadata.sip.spec;

import java.util.ArrayList;
import java.util.List;

import org.jboss.metadata.javaee.support.IdMetaDataImpl;
import org.mobicents.servlet.sip.ruby.SipRubyController;

/**
 * @author jean.deruelle@gmail.com
 * @version $Revision$
 *
 *          This class is based on the contents of org.mobicents.metadata.sip.spec package from jboss-as7-mobicents project,
 *          re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class SipServletSelectionMetaData extends IdMetaDataImpl {
    private static final long serialVersionUID = 1;

    private String mainServlet;

    private SipRubyController sipRubyController;

    private List<SipServletMappingMetaData> sipServletMappings = null;

    public String getMainServlet() {
        return mainServlet;
    }

    public void setMainServlet(String mainServlet) {
        this.mainServlet = mainServlet;
    }

    public List<SipServletMappingMetaData> getSipServletMappings() {
        return sipServletMappings;
    }

    public void addToSipServletMappings(SipServletMappingMetaData sipServletMapping) {
        if (sipServletMappings == null) {
            sipServletMappings = new ArrayList<SipServletMappingMetaData>();
        }
        sipServletMappings.add(sipServletMapping);
    }

    public void setSipServletMappings(List<SipServletMappingMetaData> sipServletMappings) {
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
     * @param rubyController the rubyController to set
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
