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

package org.mobicents.servlet.sip.address;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.log4j.Logger;

/**
 * Copied from Apache Excalibur project.
 * Source code available at http://www.google.com/codesearch?hl=en&q=+excalibur+decodePath+show:sK_gDY0W5Rw:OTjCHAiSuF0:th3BdHtpX20&sa=N&cd=1&ct=rc&cs_p=http://apache.edgescape.com/excalibur/excalibur-sourceresolve/source/excalibur-sourceresolve-1.1-src.zip&cs_f=excalibur-sourceresolve-1.1/src/java/org/apache/excalibur/source/SourceUtil.java
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class RFC2396UrlDecoder {

	private static final String UTF_8 = "UTF-8";
	static final BitSet CHARACHTERS_DONT_NEED_ECNODING;
	static final int CHARACTER_CASE_DIFF = ('a' - 'A');
	private final static Logger logger = Logger.getLogger(RFC2396UrlDecoder.class.getCanonicalName());
	
	/** Initialize the BitSet */
	static {
		CHARACHTERS_DONT_NEED_ECNODING = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++) {
			CHARACHTERS_DONT_NEED_ECNODING.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++) {
			CHARACHTERS_DONT_NEED_ECNODING.set(i);
		}
		for (i = '0'; i <= '9'; i++) {
			CHARACHTERS_DONT_NEED_ECNODING.set(i);
		}
		CHARACHTERS_DONT_NEED_ECNODING.set('-');
		CHARACHTERS_DONT_NEED_ECNODING.set('_');
		CHARACHTERS_DONT_NEED_ECNODING.set('.');
		CHARACHTERS_DONT_NEED_ECNODING.set('*');
		CHARACHTERS_DONT_NEED_ECNODING.set('"');
	}

	    
    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format.
     *
     * @param   s   <code>String</code> to be translated.
     * @return  the translated <code>String</code>.
     */
    public static String encode(String s) {
		final StringBuffer out = new StringBuffer(s.length());
		final ByteArrayOutputStream buf = new ByteArrayOutputStream(32);
		final OutputStreamWriter writer = new OutputStreamWriter(buf);
		for (int i = 0; i < s.length(); i++) {
			int c = s.charAt(i);
			if (CHARACHTERS_DONT_NEED_ECNODING.get(c)) {
				out.append((char) c);
			} else {
				try {
					writer.write(c);
					writer.flush();
				} catch (IOException e) {
					buf.reset();
					continue;
				}
				byte[] ba = buf.toByteArray();
				for (int j = 0; j < ba.length; j++) {
					out.append('%');
					char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
					// converting to use uppercase letter as part of
					// the hex value if ch is a letter.
					if (Character.isLetter(ch)) {
						ch -= CHARACTER_CASE_DIFF;
					}
					out.append(ch);
					ch = Character.forDigit(ba[j] & 0xF, 16);
					if (Character.isLetter(ch)) {
						ch -= CHARACTER_CASE_DIFF;
					}
					out.append(ch);
				}
				buf.reset();
			}
		}

		return out.toString();
	}

	
    /**
     * Decode a path.
     *
     * <p>Interprets %XX (where XX is hexadecimal number) as UTF-8 encoded bytes.
     * <p>The validity of the input path is not checked (i.e. characters that
     * were not encoded will not be reported as errors).
     * <p>This method differs from URLDecoder.decode in that it always uses UTF-8
     * (while URLDecoder uses the platform default encoding, often ISO-8859-1),
     * and doesn't translate + characters to spaces.
     *
     * @param uri the path to decode
     * @return the decoded path
     */
    public static String decode(String uri) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("uri to decode " + uri);
    	}
    	if(uri == null) {
    		// fix by Hauke D. Issue 410
//    		throw new NullPointerException("uri cannot be null !");
    		return null;
    	}
        StringBuffer translatedUri = new StringBuffer(uri.length());
        byte[] encodedchars = new byte[uri.length() / 3];
        int i = 0;
        int length = uri.length();
        int encodedcharsLength = 0;
        while (i < length) {
            if (uri.charAt(i) == '%') {
                //we must process all consecutive %-encoded characters in one go, because they represent
                //an UTF-8 encoded string, and in UTF-8 one character can be encoded as multiple bytes
                while (i < length && uri.charAt(i) == '%') {
                    if (i + 2 < length) {
                        try {
                            byte x = (byte)Integer.parseInt(uri.substring(i + 1, i + 3), 16);
                            encodedchars[encodedcharsLength] = x;
                        } catch (NumberFormatException e) {
                        	// do not throw exception, a % could be part of a IPv6 address and still be valid
//                            throw new IllegalArgumentException("Illegal hex characters in pattern %" + uri.substring(i + 1, i + 3));
                        }
                        encodedcharsLength++;
                        i += 3;
                    } else {
                    	// do not throw exception, a % could be part of a IPv6 address and still be valid
//                        throw new IllegalArgumentException("% character should be followed by 2 hexadecimal characters.");
                    }
                }
                try {
                    String translatedPart = new String(encodedchars, 0, encodedcharsLength, UTF_8);
                    translatedUri.append(translatedPart);
                } catch (UnsupportedEncodingException e) {
                    //the situation that UTF-8 is not supported is quite theoretical, so throw a runtime exception
                    throw new IllegalArgumentException("Problem in decodePath: UTF-8 encoding not supported.");
                }
                encodedcharsLength = 0;
            } else {
                //a normal character
                translatedUri.append(uri.charAt(i));
                i++;
            }
        }
        if(logger.isDebugEnabled()) {
    		logger.debug("decoded uri " + translatedUri);
    	}
        return translatedUri.toString();
    }
}