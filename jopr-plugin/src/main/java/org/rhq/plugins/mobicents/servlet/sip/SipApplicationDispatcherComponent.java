/*
  * Jopr Management Platform
  * Copyright (C) 2005-2008 Red Hat, Inc.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License, version 2, as
  * published by the Free Software Foundation, and/or the GNU Lesser
  * General Public License, version 2.1, also as published by the Free
  * Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License and the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License
  * and the GNU Lesser General Public License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */

package org.rhq.plugins.mobicents.servlet.sip;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.operation.OperationResult;
import org.rhq.plugins.jmx.MBeanResourceComponent;

/**
 * Get statistics for SipApplicationDispatcher instances
 * @author jean deruelle
 *
 */
public class SipApplicationDispatcherComponent extends MBeanResourceComponent {
	
	private static final Log log = LogFactory.getLog(SipApplicationDispatcherComponent.class);
    
    public OperationResult invokeOperation(String name, Configuration parameters) throws InterruptedException,
        Exception {

    	log.info("Name " + name + " / parameters " + parameters);
    	
        OperationResult result = null;
//        if ("resetStatistics".equals(name)) {
//            for (EmsBean bean : interceptors) {
//                EmsOperation ops = bean.getOperation("resetStatistics");
//                if (ops != null) // base bean has no resetStatistics
//                    ops.invoke(new Object[] {});
//            }
//            result = null; // no result 
//
//        } else if ("listAssociatedMBeans".equals(name)) {
//            StringBuilder sb = new StringBuilder();
//            for (EmsBean bean : interceptors) {
//                sb.append(bean.getBeanName().getCanonicalName());
//                sb.append(" ");
//            }
//            result = new OperationResult(sb.toString());
//        }

        return result;
    }
}
