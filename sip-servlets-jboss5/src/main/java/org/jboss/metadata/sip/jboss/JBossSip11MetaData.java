package org.jboss.metadata.sip.jboss;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.metadata.sip.spec.ParamValueMetaData;
import org.jboss.metadata.sip.spec.Sip11ParamValueMetaData;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.sip.spec.SipServletsMetaData;
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
	private JBossSip11ServletsMetaData servlets;
	private List<JBossSip11ParamValueMetaData> contextParams;

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

	public JBossSip11ServletsMetaData getServlets() {
		return servlets;
	}

	public List<JBossSip11ParamValueMetaData> getContextParams() {
		return contextParams;
	}

	@Override
	@XmlTransient
	public void setContextParams(List<? extends ParamValueMetaData> params) {
		this.contextParams = (List<JBossSip11ParamValueMetaData>) params;
	}

	@XmlElement(name = "context-param")
	public void setSipContextParams(List<JBossSip11ParamValueMetaData> params) {
		this.contextParams = params;
	}

	@XmlElement(name = "servlet")
	public void setServlets(JBossSip11ServletsMetaData sipServlets) {
		this.servlets = (JBossSip11ServletsMetaData) sipServlets;
	}

	@XmlTransient
	public void setServlets(SipServletsMetaData sipServlets) {
		this.servlets = (JBossSip11ServletsMetaData) sipServlets;
	}	
}
