package org.jboss.metadata.sip.spec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Web application spec metadata.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
@XmlRootElement(name="sip-app", namespace="http://www.jcp.org/xml/ns/sipservlet")
@JBossXmlSchema(
      xmlns={@XmlNs(namespaceURI = "http://www.jcp.org/xml/ns/sipservlet", prefix = "sipservlet")},
      ignoreUnresolvedFieldOrClass=false,
      namespace="http://www.jcp.org/xml/ns/sipservlet",
      elementFormDefault=XmlNsForm.QUALIFIED,
      normalizeSpace=true)
//      strict=false)
@XmlType(name="sip-appType",
      namespace="http://www.jcp.org/xml/ns/sipservlet",
      propOrder={"descriptionGroup", "distributable", "contextParams", "listeners", "servletSelection", "servlets",
      "proxyConfig", "sessionConfig", "sipSecurityContraints", "sipLoginConfig", "securityRoles", "jndiEnvironmentRefsGroup", "messageDestinations", "localEncodings"})
public class Sip11MetaData extends SipMetaData
{
   private static final long serialVersionUID = 1;
   private boolean metadataComplete;

   public boolean isMetadataComplete()
   {
      return metadataComplete;
   }

   @XmlAttribute(name="metadata-complete")
   public void setMetadataComplete(boolean metadataComplete)
   {
      this.metadataComplete = metadataComplete;
   }

}
