/*
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
      if (this.validate == false) { return new InputSource(new ByteArrayInputStream("<!-- -->".getBytes())); }
      InputStream inputStream = null;
      URL sipXsdFile = SipEntityResolver.class.getClassLoader().getResource(SIP_XSD_FILE);
      inputStream = sipXsdFile.openStream();
      return new InputSource(inputStream);
   }
}
