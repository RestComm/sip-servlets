package org.jboss.metadata.sip.spec;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jboss.metadata.sip.spec.ParamValueMetaData;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Sip application spec metadata.
 *
 * @author jean.deruelle@gmail.com
 */
@XmlRootElement(name="sip-app", namespace="http://www.jcp.org/xml/ns/sipservlet")
@JBossXmlSchema(
      xmlns={@XmlNs(namespaceURI = "http://www.jcp.org/xml/ns/sipservlet", prefix = "sipservlet"),
    		  @XmlNs(namespaceURI = "http://java.sun.com/xml/ns/javaee", prefix = "javaee")
      },      
      ignoreUnresolvedFieldOrClass=false,
      namespace="http://www.jcp.org/xml/ns/sipservlet",
      elementFormDefault=XmlNsForm.QUALIFIED,
      normalizeSpace=true,
      strict=false)
@XmlType(name="sip-appType",
      namespace="http://www.jcp.org/xml/ns/sipservlet")
public class Sip11MetaData extends SipMetaData
{
   private static final long serialVersionUID = 1;
	private boolean metadataComplete;
//	private Sip11ServletsMetaData servlets;
//	private List<Sip11ParamValueMetaData> contextParams;

	public boolean isMetadataComplete() {
		return metadataComplete;
	}

	@XmlAttribute(name = "metadata-complete")
	public void setMetadataComplete(boolean metadataComplete) {
		this.metadataComplete = metadataComplete;
	}

//	public List<Sip11ParamValueMetaData> getContextParams() {
//		return contextParams;
//	}
//
//	@Override
//	@XmlTransient
//	public void setContextParams(List<? extends ParamValueMetaData> params) {
//		this.contextParams = (List<Sip11ParamValueMetaData>) params;
//	}
//
//	@XmlElement(name = "context-param")
//	public void setSipContextParams(List<Sip11ParamValueMetaData> params) {
//		this.contextParams = params;
//	}

//	public Sip11ServletsMetaData getServlets() {
//		return this.servlets;
//	}
//
//	@XmlElement(name = "servlet")
//	public void setServlets(Sip11ServletsMetaData sipServlets) {
//		this.servlets = sipServlets;
//	}
//
//	@Override
//	@XmlTransient
//	public void setServlets(SipServletsMetaData sipServlets) {
//		this.servlets = (Sip11ServletsMetaData) sipServlets;
//	}  
}
