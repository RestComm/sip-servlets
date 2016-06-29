package org.jboss.as.clustering.web.infinispan.sip;

import org.jboss.as.clustering.infinispan.io.ExternalizableExternalizer;
import org.jboss.as.clustering.web.sip.DistributableSipApplicationSessionMetadata;

public class DistributableSipApplicationSessionMetadataExternalizer extends ExternalizableExternalizer<DistributableSipApplicationSessionMetadata> {
    
	private static final long serialVersionUID = 4997693145234715183L;

	public DistributableSipApplicationSessionMetadataExternalizer() {
        super(DistributableSipApplicationSessionMetadata.class);
    }
}
