package org.jboss.as.clustering.web.infinispan.sip;

import org.jboss.as.clustering.infinispan.io.ExternalizableExternalizer;
import org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata;

public class DistributableSipSessionMetadataExternalizer extends ExternalizableExternalizer<DistributableSipSessionMetadata> {
    
	private static final long serialVersionUID = 6462710308649446921L;

	public DistributableSipSessionMetadataExternalizer() {
        super(DistributableSipSessionMetadata.class);
    }
}
