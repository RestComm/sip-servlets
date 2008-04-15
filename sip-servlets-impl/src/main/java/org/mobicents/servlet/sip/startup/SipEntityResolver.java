
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright (c) Ericsson AB, 2004-2007. All rights reserved.
 */


package org.mobicents.servlet.sip.startup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SipEntityResolver implements EntityResolver
{
   private static final String SIP_XSD_FILE = "org/mobicents/servlet/sip/loading/sip-app-1.1.xsd";
   private boolean             validate     = true;

   /**
    * {@inheritDoc}
    */
   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      if (this.validate == false) { return new InputSource(new ByteArrayInputStream((new String("<!-- -->"))
            .getBytes())); }
      InputStream inputStream = null;
      URL sipXsdFile = SipEntityResolver.class.getClassLoader().getResource(SIP_XSD_FILE);
      inputStream = sipXsdFile.openStream();
      return new InputSource(inputStream);
   }
}
