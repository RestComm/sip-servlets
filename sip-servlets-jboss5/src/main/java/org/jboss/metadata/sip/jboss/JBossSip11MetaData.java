package org.jboss.metadata.sip.jboss;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Sip application spec metadata if no xsd is defined in the sip.xml.
 * 
 * @author jean.deruelle@gmail.com
 */
@XmlRootElement(name="sip-app", namespace="")
@JBossXmlSchema(
      xmlns={@XmlNs(namespaceURI = "", prefix = "sipservlet")},
      ignoreUnresolvedFieldOrClass=false,
      namespace="",
      elementFormDefault=XmlNsForm.UNSET,
      normalizeSpace=true,
      strict=false)
public class JBossSip11MetaData extends SipMetaData {
	private static final long serialVersionUID = 1;
	private boolean metadataComplete;

	public boolean isMetadataComplete() {
		return metadataComplete;
	}

	@XmlAttribute(name = "metadata-complete")
	public void setMetadataComplete(boolean metadataComplete) {
		this.metadataComplete = metadataComplete;
	}

	@Override
	public String getVersion() {
		return "1.1";
	}	
}
