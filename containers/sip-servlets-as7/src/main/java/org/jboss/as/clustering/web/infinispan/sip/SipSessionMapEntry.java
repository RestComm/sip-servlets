package org.jboss.as.clustering.web.infinispan.sip;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.as.clustering.web.sip.DistributableSipApplicationSessionMetadata;
import org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;

/**
 * Enumerates the properties of a session to be stored in a cache entry. Provides get/put methods which encapsulate away the
 * details of the map key.
 *
 * @author Paul Ferraro
 */
public enum SipSessionMapEntry {
	
	VERSION(Integer.class), TIMESTAMP(Long.class), METADATA(DistributableSessionMetadata.class), ATTRIBUTES(Object.class), SIP_SERVLETS_METADATA(Object.class);
	
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

