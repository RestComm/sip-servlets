/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
