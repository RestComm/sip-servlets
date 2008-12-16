/**
 * 
 */
package org.jboss.deployment;


import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.sip.spec.SipMetaData;

/**
 * An ObjectModelFactoryDeployer for translating sip.xml descriptors into
 * SipMetaData instances.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision:$
 */
public class SipAppParsingDeployer  extends SchemaResolverDeployer<SipMetaData>
{
   public SipAppParsingDeployer ()
   {
      super(SipMetaData.class);
      setName("sip.xml");
   }

   /**
    * Get the virtual file path for the web-app descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the web-app descriptor
    */
   public String getSipXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the web-app descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is web.xml
    * to be found in the WEB-INF metdata path.
    * 
    * @param webXmlPath - new virtual file path for the web-app descriptor
    */
   public void setSipXmlPath(String sipXmlPath)
   {
      setName(sipXmlPath);
   }

}