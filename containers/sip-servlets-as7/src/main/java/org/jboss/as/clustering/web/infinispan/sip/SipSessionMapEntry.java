package org.jboss.as.clustering.web.infinispan.sip;

import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.as.clustering.web.DistributableSessionMetadata;

/**
 * Enumerates the properties of a session to be stored in a cache entry. Provides get/put methods which encapsulate away the
 * details of the map key.
 *
 * @author Paul Ferraro
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
    	
    	/*T value = this.<T> cast(map.get(this.key()));
    	// TODO: torolni
    	if(logger.isDebugEnabled()) {
    		if (this.key() == 2){
    			if (value instanceof DistributableSipSessionMetadata){
					logger.debug("get - getting sip metadata from cache, metadata map=" + ((DistributableSipSessionMetadata)value).getMetaData());
					if (((DistributableSipSessionMetadata)value).getMetaData() != null){
						for (String metadataKey: ((DistributableSipSessionMetadata)value).getMetaData().keySet()){
							logger.debug("get - sip metadata map entry: " + metadataKey + "=" + ((DistributableSipSessionMetadata)value).getMetaData().get(metadataKey));
						}
					}
    			}	
    			if (value instanceof DistributableSipApplicationSessionMetadata){
					logger.debug("get - getting sip app metadata from cache, metadata map=" + ((DistributableSipApplicationSessionMetadata)value).getMetaData());
					if (((DistributableSipApplicationSessionMetadata)value).getMetaData() != null){
						for (String metadataKey: ((DistributableSipApplicationSessionMetadata)value).getMetaData().keySet()){
							logger.debug("get - sip app metadata map entry: " + metadataKey + "=" + ((DistributableSipApplicationSessionMetadata)value).getMetaData().get(metadataKey));
						}
					}
    			}
			}
    	}
    	if(logger.isDebugEnabled()) {
    		if (this.key() == 4){
    			logger.debug("get - getting key 5 map=" + value);
			}
    	}
    	return value;*/
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
			
			// TODO: torolni
			/*if (this.key() == 2){
				if (value instanceof DistributableSipSessionMetadata){
					logger.debug("put - putting sip metadata into cache, metadata map=" + ((DistributableSipSessionMetadata)value).getMetaData());
					if (((DistributableSipSessionMetadata)value).getMetaData() != null){
						for (String metadataKey: ((DistributableSipSessionMetadata)value).getMetaData().keySet()){
							logger.debug("put - sip metadata map entry: " + metadataKey + "=" + ((DistributableSipSessionMetadata)value).getMetaData().get(metadataKey));
						}
					}
				}
				if (value instanceof DistributableSipApplicationSessionMetadata){
					logger.debug("put - putting sip app metadata into cache, metadata map=" + ((DistributableSipApplicationSessionMetadata)value).getMetaData());
					if (((DistributableSipApplicationSessionMetadata)value).getMetaData() != null){
						for (String metadataKey: ((DistributableSipApplicationSessionMetadata)value).getMetaData().keySet()){
							logger.debug("put - sip app metadata map entry: " + metadataKey + "=" + ((DistributableSipApplicationSessionMetadata)value).getMetaData().get(metadataKey));
						}
					}
				}
			}
			if(logger.isDebugEnabled()) {
	    		if (this.key() == 4){
	    			logger.debug("put - putting key 5 map=" + value);
				}
	    	}*/
			
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

        
        /*T result = this.<T> cast(map.put(this.key(), value));
        
        // TODO: torolni
        if(logger.isDebugEnabled()) {
        	if (this.key() == 2){
        		
        		if (value instanceof DistributableSipSessionMetadata){
		    		org.jboss.as.clustering.web.DistributableSessionMetadata metadata = SessionMapEntry.METADATA.get(map);
		    		logger.debug("put - map entry after put: metadata=" + metadata + " - casting to sip meta data");
		    		if (metadata != null){
		        		DistributableSipSessionMetadata metadataSip = (DistributableSipSessionMetadata)metadata;
		        		logger.debug("put - map entry after put and cast: metadataSip=" + metadataSip);
		        		if (metadataSip != null){
		        			logger.debug("put - map entry after put metadataSip.getMetaData()=" + metadataSip.getMetaData());
		        			if (metadataSip.getMetaData() != null){
		        				for (String tmpKey: metadataSip.getMetaData().keySet()){
		        					logger.debug("put - map entry after put metadataSip.getMetaData() entry: " + tmpKey + "=" + metadataSip.getMetaData().get(tmpKey));		
		        				}
		        			}
		        		}
		    		}
        		}
        		if (value instanceof DistributableSipApplicationSessionMetadata){
		    		org.jboss.as.clustering.web.DistributableSessionMetadata metadata = SessionMapEntry.METADATA.get(map);
		    		logger.debug("put - map entry after put: metadata=" + metadata + " - casting to sip meta data");
		    		if (metadata != null){
		    			DistributableSipApplicationSessionMetadata metadataSip = (DistributableSipApplicationSessionMetadata)metadata;
		        		logger.debug("put - map entry after put and cast: metadataSip=" + metadataSip);
		        		if (metadataSip != null){
		        			logger.debug("put - map entry after put metadataSip.getMetaData()=" + metadataSip.getMetaData());
		        			if (metadataSip.getMetaData() != null){
		        				for (String tmpKey: metadataSip.getMetaData().keySet()){
		        					logger.debug("put - map entry after put metadataSip.getMetaData() entry: " + tmpKey + "=" + metadataSip.getMetaData().get(tmpKey));		
		        				}
		        			}
		        		}
		    		}
        		}
        		
        	}
		}
        
        
        return result;*/
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

