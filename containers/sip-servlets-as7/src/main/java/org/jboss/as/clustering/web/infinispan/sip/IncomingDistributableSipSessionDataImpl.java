/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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

package org.jboss.as.clustering.web.infinispan.sip;

import java.util.Map;

import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.as.clustering.web.impl.IncomingDistributableSessionDataImpl;
import org.jboss.logging.Logger;

/**
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class IncomingDistributableSipSessionDataImpl extends IncomingDistributableSessionDataImpl {
	
	private static Logger logger = Logger.getLogger(IncomingDistributableSipSessionDataImpl.class);
	
	public IncomingDistributableSipSessionDataImpl(Integer version, Long timestamp, DistributableSessionMetadata metadata) {
        super(version, timestamp, metadata);
    }
	
	@Override
    public Map<String, Object> getSessionAttributes() {
		if (logger.isDebugEnabled()){
			logger.debug("getSessionAttributes");	
		}
		
    	try {
    		return super.getSessionAttributes();
    	} catch (IllegalStateException e){
    		if (logger.isDebugEnabled()){
    			logger.debug("getSessionAttributes - returning null because an exception occured while calling super.getSessionAttributes(): ", e);	
    		}
    		return null;
    	}
    }
	
}
