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
 * @author jean.deruelle@gmail.com
 */
public class SipAppParsingDeployer  extends SchemaResolverDeployer<SipMetaData>
{
   public SipAppParsingDeployer ()
   {
      super(SipMetaData.class);
      setName("sip.xml");
   }

   /**
    * Get the virtual file path for the sip-app descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the sip-app descriptor
    */
   public String getSipXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the sip-app descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is sip.xml
    * to be found in the WEB-INF metdata path.
    * 
    * @param sipXmlPath - new virtual file path for the sip-app descriptor
    */
   public void setSipXmlPath(String sipXmlPath)
   {
      setName(sipXmlPath);
   }

}