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

import org.apache.log4j.Logger;
import org.jboss.as.clustering.web.DistributableSessionMetadata;

/**
 * Enumerates the properties of a session to be stored in a cache entry. Provides get/put methods which encapsulate away the
 * details of the map key.
 *
 * @author Paul Ferraro
 * @author posfai.gergely@ext.alerant.hu
 */
public enum SipSessionMapEntry {
	
	VERSION(Integer.class), TIMESTAMP(Long.class), METADATA(DistributableSessionMetadata.class), ATTRIBUTES(Object.class), SIP_METADATA_MAP(Object.class);
	
	private transient static final Logger logger = Logger.getLogger(SipSessionMapEntry.class);
	
    private Class<?> targetClass;

    private SipSessionMapEntry(Class<?> targetClass) {
    	this.targetClass = targetClass;
    }

    /**
     * Returns the value associated with this atomic map entry.
     *
     * @param <T> the value type
     * @param map an atomic map
     * @return the entry value
     */
    public <T> T get(Map<Object, Object> map) {
    	if(logger.isDebugEnabled()) {
			logger.debug("get - key=" + this.key() + " from Map<Object, Object>=" + map);
		}
    	
    	return this.<T> cast(map.get(this.key()));
    }

    /**
     * Add this entry to the specified map if the specified value is non-null.
     *
     * @param <T> the value type
     * @param map an atomic map
     * @param value the entry value
     * @return the old entry value, or null if no previous entry existed
     */
    public <T> T put(Map<Object, Object> map, Object value) throws IllegalArgumentException {
    	if(logger.isDebugEnabled()) {
			logger.debug("put - putting value=" + value + " into map<Object, Object>=" + map + " with key=" + this.key() + ", targetClass.getCanonicalName()=" + targetClass.getCanonicalName());
		}
        if (value == null){
        	return null;
        }
        
        if (map.containsKey(this.key())){
        	if(logger.isDebugEnabled()) {
    			logger.debug("put - map already contains key=" + this.key() + " with value= " + map.get(this.key()));
    		}
        }

        if (!this.targetClass.isInstance(value)) {
        	if(logger.isDebugEnabled()) {
    			logger.debug("put - invalid map value (" + value+ "), targetClass.getCanonicalName()=" + targetClass.getCanonicalName() );
    		}
            throw InfinispanSipMessages.MESSAGES.invalidMapValue(value.getClass().getName(), this);
        }
        return this.<T> cast(map.put(this.key(), value));
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object value) {
    	if(logger.isDebugEnabled()) {
			logger.debug("cast - value=" + value + ", targetClass.getCanonicalName()=" + targetClass.getCanonicalName());
		}
        Class<T> targetClass = (Class<T>) this.targetClass;
        return (value != null) ? targetClass.cast(value) : null;
    }

    private Byte key() {
        return Byte.valueOf((byte) this.ordinal());
    }
}

