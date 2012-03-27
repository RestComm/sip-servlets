/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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